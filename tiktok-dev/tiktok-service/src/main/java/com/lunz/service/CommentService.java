package com.lunz.service;

import com.lunz.bo.CommentBO;
import com.lunz.pojo.Comment;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.CommentVO;

public interface CommentService {

    /**
     * 查询评论
     */
    public Comment getCommment(String commentId);

    /**
     * 发表评论
     */
    public CommentVO createComment(CommentBO commentBO);

    /**
     * 查询评论的列表
     */
    public PagedGridResult queryVlogComments(String vlogId,
                                             String userId,
                                             Integer page,
                                             Integer pageSize);

    /**
     * 删除评论
     */
    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId);
}
