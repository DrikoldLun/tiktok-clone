package com.lunz.service;

import java.util.Map;

public interface MsgService {
    /**
     * 创建消息
     */
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent);
}
