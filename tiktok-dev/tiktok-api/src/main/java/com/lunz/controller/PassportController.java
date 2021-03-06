package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.RegistLoginBO;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.pojo.Users;
import com.lunz.service.UserService;
import com.lunz.utils.IPUtil;
import com.lunz.utils.SMSUtils;
import com.lunz.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("passport")
@Api(tags = "PassportController通行证接口模块")
public class PassportController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(mobile)) {
            return GraceJSONResult.ok();
        }
        // TODO 获得用户IP
        String userIp = IPUtil.getRequestIp(request);
        System.out.println(userIp);
        // TODO 获得用户IP，根据用户IP进行限制，限制用户在60s之内只能获取一次验证码
        redis.setnx60s(MOBILE_SMSCODE+":"+userIp,userIp);
        String code = (int) ((Math.random()*9+1)*100000) + "";
        // smsUtils.sendSMS(mobile,code);
        log.info(code);
        // TODO 把验证码放入到redis中，用于后续验证
        redis.set(MOBILE_SMSCODE+":"+mobile,code,30*60); // 30min后验证码失效
        return GraceJSONResult.ok();
    }

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO,
                                 // BindingResult result, // 对代码有侵入性
                                 HttpServletRequest request) throws Exception {
        /* 通过GraceExceptionHandler异常捕捉返回封装Json
        // 1st step. 判断bindingresult中是否保存了错误的验证信息
        if (result.hasErrors()) {
            Map<String,String> map = getErrors(result);
            return GraceJSONResult.errorMap(map);
        }
         */
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        // 1. 从redis中获取验证码进行校验是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE+":"+mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 查询数据库，判断用户是否存在
        Users user = userService.queryMobileIsExist(mobile);
        if (user == null) {
            // 2.1 如果用户为空，表示没有注册过，则为null，需要注册信息入库
            user = userService.createUser(mobile);
        }

        // 3. 如果用户存在，可以继续下方业务，可以保存用户信息和会话信息
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN+":"+user.getId(),uToken);

        // 4. 用户登录注册成功以后，删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE+":"+mobile);

        // 5. 返回用户信息，包含token令牌
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setUserToken(uToken);

        return GraceJSONResult.ok(userVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) throws Exception {
        // 后端只需要清除用户的token信息，前端也需要清除本地app中的用户信息和token信息
        redis.del(REDIS_USER_TOKEN+":"+userId);
        return GraceJSONResult.ok();
    }
}
