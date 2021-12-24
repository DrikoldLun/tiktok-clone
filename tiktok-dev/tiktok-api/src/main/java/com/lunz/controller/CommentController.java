package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.CommentBO;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.service.CommentService;
import com.lunz.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@Api(tags = "CommentController评论模块的接口")
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {

    @Autowired
    CommentService commentService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {
        CommentVO commentVO = commentService.createComment(commentBO);
        return GraceJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) throws Exception {
        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS+":"+vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return GraceJSONResult.ok(Integer.valueOf(countsStr));
    }
}
