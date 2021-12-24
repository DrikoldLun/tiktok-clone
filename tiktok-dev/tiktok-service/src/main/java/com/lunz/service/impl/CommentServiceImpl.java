package com.lunz.service.impl;

import com.github.pagehelper.PageHelper;
import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.CommentBO;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.CommentMapper;
import com.lunz.mapper.CommentMapperCustom;
import com.lunz.pojo.Comment;
import com.lunz.service.CommentService;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
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
    private Sid sid;

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
        System.out.println(new Date());

        commentMapper.insert(comment);

        // redis操作放在service中，评论总数的累加
        redis.increment(REDIS_VLOG_COMMENT_COUNTS+":"+commentBO.getVlogId(),1);

        // 留言后的最新评论需要返回给前端做展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment,commentVO);
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
