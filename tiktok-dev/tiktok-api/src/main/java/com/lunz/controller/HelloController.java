package com.lunz.controller;

import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "helloController的测试接口")
public class HelloController {
    @Autowired
    private SMSUtils smsUtils;

    @ApiOperation(value = "sms-sms操作的测试路由")
    @GetMapping("sms")
    public Object sms() throws Exception {
        String code = "123456";
        smsUtils.sendSMS("17551084670",code);
        return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_CREATE_ERROR);
    }
}
