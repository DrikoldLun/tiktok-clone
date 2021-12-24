package com.lunz.service.impl;

import com.lunz.base.BaseInfoProperties;
import com.lunz.enums.MessageEnum;
import com.lunz.mo.MessageMO;
import com.lunz.pojo.Users;
import com.lunz.repository.MessageRepository;
import com.lunz.service.MsgService;
import com.lunz.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

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

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        // 可以设置example然后findall，此处通过继承MongoRepo类自定义条件进行查询
        Pageable pageable = PageRequest.of(page,pageSize);
        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId,pageable);
        for (MessageMO msg : list) {
            // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于前端标记互粉
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }
                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP+":"+toUserId+":"+msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend",true);
                } else {
                    map.put("isFriend",false);
                }
                msg.setMsgContent(map);
            }
        }

        return list;
    }
}
