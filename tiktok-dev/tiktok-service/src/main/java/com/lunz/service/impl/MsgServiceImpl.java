package com.lunz.service.impl;

import com.lunz.mo.MessageMO;
import com.lunz.pojo.Users;
import com.lunz.repository.MessageRepository;
import com.lunz.service.MsgService;
import com.lunz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class MsgServiceImpl implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Override
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent) {
        MessageMO messageMO = new MessageMO();
        // MongoDB会自动生成主键不需要指定
        Users fromUser = userService.getUser(fromUserId);
        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());

        messageMO.setToUserId(toUserId);

        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setMsgType(msgType);

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }
}
