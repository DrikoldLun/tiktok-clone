package com.lunz.controller;

import com.lunz.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Api(tags = "HelloController 消息功能模块的接口")
@RefreshScope
public class HelloController {

    @GetMapping("port")
    public Object port() {
        return GraceJSONResult.ok("Current port is 8080");
    }

    /*@Value("${alibaba.teacher.name}")
    private String name;
    @Value("${alibaba.teacher.age}")
    private Integer age;

    @GetMapping("/info")
    public Object info() {
        return name + "-" + age;
    }*/

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
