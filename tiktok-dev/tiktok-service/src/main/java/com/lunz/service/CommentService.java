package com.lunz.service;

import com.lunz.bo.CommentBO;
import com.lunz.vo.CommentVO;

public interface CommentService {

    /**
     * 发表评论
     */
    public CommentVO createComment(CommentBO commentBO);

}
