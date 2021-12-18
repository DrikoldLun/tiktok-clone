package com.lunz.interceptor;

import com.lunz.controller.BaseInfoProperties;
import com.lunz.exceptions.GraceException;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    // 在访问controller之前会到达这里，我们只需要调用该方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获得用户IP
        String userIp = IPUtil.getRequestIp(request);

        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE+":"+userIp);
        if (keyIsExist) {
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率太高！");
            return false;
        }
        /**
         * true: 请求放行
         * false: 请求拦截
         */
        return true;
    }

    // 渲染视图前到达这里
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 渲染完毕后进入这里
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
