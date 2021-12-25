package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.base.RabbitMQConfig;
import com.lunz.bo.CommentBO;
import com.lunz.enums.MessageEnum;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.mo.MessageMO;
import com.lunz.pojo.Comment;
import com.lunz.pojo.Vlog;
import com.lunz.service.CommentService;
import com.lunz.service.MsgService;
import com.lunz.service.VlogService;
import com.lunz.utils.JsonUtils;
import com.lunz.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Api(tags = "CommentController评论模块的接口")
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {

    @Autowired
    CommentService commentService;

    @Autowired
    VlogService vlogService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {
        CommentVO commentVO = commentService.createComment(commentBO);
        return GraceJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {
        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS+":"+vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return GraceJSONResult.ok(Integer.valueOf(countsStr));
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(commentService.queryVlogComments(vlogId,userId,page,pageSize));
    }

    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                @RequestParam String commentId,
                                @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId,commentId,vlogId);
        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId) {
        redis.increment(REDIS_VLOG_COMMENT_LIKED_COUNTS+":"+commentId,1);
        redis.set(REDIS_USER_LIKE_COMMENT+":"+userId+":"+commentId,"1");
        // 系统消息-点赞评论
        Comment comment = commentService.getCommment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId",comment.getVlogId());
        msgContent.put("vlogCover",vlog.getCover());
        msgContent.put("commentId",commentId);
        //msgService.createMsg(userId,comment.getCommentUserId(),MessageEnum.LIKE_COMMENT.type,msgContent);
        // 优化：使用mq异步解耦
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(comment.getCommentUserId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg."+MessageEnum.LIKE_COMMENT.enValue,
                JsonUtils.objectToJson(messageMO));

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId,
                                @RequestParam String userId) {
        redis.decrement(REDIS_VLOG_COMMENT_LIKED_COUNTS+":"+commentId,1);
        redis.del(REDIS_USER_LIKE_COMMENT+":"+userId+":"+commentId);
        return GraceJSONResult.ok();
    }
}
