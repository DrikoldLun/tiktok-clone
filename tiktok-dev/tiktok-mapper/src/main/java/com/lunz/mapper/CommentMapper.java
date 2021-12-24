package com.lunz.mapper;

import com.lunz.my.mapper.MyMapper;
import com.lunz.pojo.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentMapper extends MyMapper<Comment> {
}