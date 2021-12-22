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

    // 获取视频list，主页search为空获取所有视频(推荐页)，查询页根据search内容模糊查询
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue="") String userId,
                                     @RequestParam(defaultValue="") String search,
                                     @RequestParam Integer page, // 页码
                                     @RequestParam Integer pageSize) { // 一页内容数量
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getIndexVlogList(userId,search,page,pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    // 获取关注页视频list
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam(defaultValue="") String myId,
                                     @RequestParam Integer page, // 页码
                                     @RequestParam Integer pageSize) { // 一页内容数量
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId,page,pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    // 获取朋友页视频list
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam(defaultValue="") String myId,
                                      @RequestParam Integer page, // 页码
                                      @RequestParam Integer pageSize) { // 一页内容数量
        // 默认赋值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId,page,pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId,vlogId));
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.YES.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId,vlogId));
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.NO.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId,vlogId));
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

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(vlogService.queryMyLikedVlogList(userId,page,pageSize));
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

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String vlogerId,
                                  @RequestParam String vlogId) {
        // 我点赞的视频，关联关系保存到数据库
        vlogService.userUnLikeVlog(userId,vlogId);

        // 视频和视频发布者的获赞都会-1
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS+":"+vlogId,1);
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS+":"+vlogerId,1);
        // 我取消点赞的视频，删除在redis中的关联关系
        redis.del(REDIS_USER_LIKE_VLOG+":"+userId+":"+vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }
}