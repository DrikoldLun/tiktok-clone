package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.VlogBO;
import com.lunz.enums.YesOrNo;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.service.VlogService;
import com.lunz.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@Api(tags = "VlogController短视频相关业务接口")
@RequestMapping("vlog")
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {
        // 作业，校验vlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    // 获取视频list，主页search为空获取所有视频，查询页根据search内容模糊查询
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page, // 页码
                                     @RequestParam Integer pageSize) { // 一页内容数量
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getIndexVlogList(search,page,pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogDetailById(vlogId));
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.YES.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(vlogId));
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.NO.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(vlogId));
    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,page,pageSize,YesOrNo.NO.type);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,page,pageSize,YesOrNo.YES.type);
        return GraceJSONResult.ok(gridResult);
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        // 我点赞的视频，关联关系保存到数据库
        vlogService.userLikeVlog(userId,vlogId);

        // 点赞后，视频和视频发布者的获赞都会+1
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS+":"+vlogId,1);
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS+":"+vlogerId,1);
        // 我点赞的视频，需要在redis中保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG+":"+userId+":"+vlogId,"1");

        return GraceJSONResult.ok();
    }
}
