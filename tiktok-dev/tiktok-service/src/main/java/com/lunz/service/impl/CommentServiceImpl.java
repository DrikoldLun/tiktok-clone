package com.lunz.service.impl;

import com.github.pagehelper.PageHelper;
import com.lunz.base.BaseInfoProperties;
import com.lunz.base.RabbitMQConfig;
import com.lunz.bo.CommentBO;
import com.lunz.enums.MessageEnum;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.CommentMapper;
import com.lunz.mapper.CommentMapperCustom;
import com.lunz.mo.MessageMO;
import com.lunz.pojo.Comment;
import com.lunz.pojo.Vlog;
import com.lunz.service.CommentService;
import com.lunz.service.VlogService;
import com.lunz.utils.JsonUtils;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Sid sid;

    @Override
    public Comment getCommment(String commentId) {
        return commentMapper.selectByPrimaryKey(commentId);
    }

    @Override
    public CommentVO createComment(CommentBO commentBO) {
        String commentId = sid.nextShort();
        Comment comment = new Comment();

        // 不使用BeanUtils拷贝属性的原因：属性不多，显示写明有利于阅读分析
        comment.setId(commentId);

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        // redis操作放在service中，评论总数的累加
        redis.increment(REDIS_VLOG_COMMENT_COUNTS+":"+commentBO.getVlogId(),1);

        // 留言后的最新评论需要返回给前端做展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment,commentVO);

        // 评论/回复
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId",commentBO.getVlogId());
        msgContent.put("vlogCover",vlog.getCover());
        msgContent.put("commentId",commentId);
        msgContent.put("commentContent",commentBO.getContent());
        MessageEnum type = MessageEnum.COMMENT_VLOG; // 评论
        String toId = commentBO.getVlogerId();
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            type = MessageEnum.REPLY_YOU; // 回复
            toId = getCommment(commentBO.getFatherCommentId()).getCommentUserId();
        }

        // msgService.createMsg(commentVO.getCommentUserId(),toId,type,msgContent);
        // 优化：使用mq异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentVO.getCommentUserId());
        messageMO.setToUserId(toId);
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg."+type.enValue,
                JsonUtils.objectToJson(messageMO));

        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("vlogId",vlogId);
        PageHelper.startPage(page,pageSize);
        List<CommentVO> list = commentMapperCustom.getCommentList(map);

        for (CommentVO cv : list) {
            String commentId = cv.getCommentId();
            // 当前短视频某个评论的点赞总数
            String countsStr = redis.get(REDIS_VLOG_COMMENT_LIKED_COUNTS+":"+commentId);
            if (StringUtils.isNotBlank(countsStr)) {
                cv.setLikeCounts(Integer.valueOf(countsStr));
            } else {
                cv.setLikeCounts(0);
            }

            String doILike = redis.get(REDIS_USER_LIKE_COMMENT+":"+userId+":"+commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }
        return setterPagedGrid(list,page);
    }

    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        // 限定只有发布该评论的用户才可删除该评论
        pendingDelete.setCommentUserId(commentUserId);
        commentMapper.delete(pendingDelete);
        // redis操作放在service中，评论总数的累减
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS+":"+vlogId,1);
    }
}
