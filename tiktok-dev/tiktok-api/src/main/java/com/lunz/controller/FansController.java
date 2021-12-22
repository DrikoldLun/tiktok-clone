package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.pojo.Users;
import com.lunz.service.FansService;
import com.lunz.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "FansController粉丝相关业务接口")
@RequestMapping("fans")
public class FansController extends BaseInfoProperties {

    @Autowired
    private FansService fansService;

    @Autowired
    private UserService userService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        // 判断两个id是否为空
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // 判断当前用户，自己不能关注自己
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        // 判断两个id对应的用户是否存在
        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);

        // fixme: 两个用户id的数据库查询后的判断，是分开好，还是合并判断好
        if (vloger == null || myInfo == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        // 保存粉丝关系到数据库
        fansService.doFollow(myId,vlogerId);
        return GraceJSONResult.ok();
    }

}
