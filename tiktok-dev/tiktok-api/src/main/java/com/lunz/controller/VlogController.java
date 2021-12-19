package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.VlogBO;
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
}
