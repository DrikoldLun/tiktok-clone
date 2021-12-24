package com.lunz.service.impl;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.CommentBO;
import com.lunz.mapper.CommentMapper;
import com.lunz.pojo.Comment;
import com.lunz.service.CommentService;
import com.lunz.vo.CommentVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

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
}
