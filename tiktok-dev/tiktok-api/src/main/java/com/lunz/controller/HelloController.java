package com.lunz.controller;

// import com.lunz.config.RabbitMQConfig;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.utils.SMSUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "helloController的测试接口")
public class HelloController {
    @Autowired
    private SMSUtils smsUtils;

    @GetMapping("sms")
    public Object sms() throws Exception {
        String code = "123456";
        smsUtils.sendSMS("17551084670",code);
        return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_CREATE_ERROR);
    }

    /*@Autowired
    public RabbitTemplate rabbitTemplate;


    @GetMapping("produce1")
    public GraceJSONResult produce1() throws Exception{
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.send",
                "我发了一个消息~");

        *//**
         * 路由规则
         * route-key
         * display.*.*
         *      display.a.b
         *      display.publish.msg
         *      display.a.b.c匹配不到
         *      * 代表一个占位符
         *
         * display.#
         *      display.a.b
         *      display.a.b.c.d
         *      display.public.msg
         *      display.delete.msg.do
         *      # 代表多个占位符
         *//*

        return GraceJSONResult.ok();
    }

    @GetMapping("produce2")
    public GraceJSONResult produce2() throws Exception{
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.delete",
                "我删除了一个消息~");

        return GraceJSONResult.ok();
    }*/

}
