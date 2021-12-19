package com.lunz.controller;

import com.lunz.bo.VlogBO;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.service.VlogService;
import com.lunz.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@Api(tags = "VlogController短视频相关业务接口")
@RequestMapping("vlog")
public class VlogController {

    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {
        // 作业，校验vlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String search) {
        List<IndexVlogVO> list = vlogService.getIndexVlogList(search);
        return GraceJSONResult.ok(list);
    }
}
