package com.lunz.controller;

import com.lunz.config.MinIOConfig;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.utils.MinIOUtils;
import com.lunz.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
@Slf4j
@RestController
@Api(tags = "文件上传测试的接口")
public class FileController {
    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("upload")
    public GraceJSONResult upload(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,file.getInputStream());
        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + fileName;
        return GraceJSONResult.ok(imgUrl);
    }
}
 */
