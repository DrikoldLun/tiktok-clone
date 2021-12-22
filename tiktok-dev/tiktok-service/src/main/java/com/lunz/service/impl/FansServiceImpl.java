package com.lunz.service.impl;

import com.lunz.enums.YesOrNo;
import com.lunz.mapper.FansMapper;
import com.lunz.pojo.Fans;
import com.lunz.service.FansService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class FansServiceImpl implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {
        String fid = sid.nextShort();
        Fans fan = new Fans();
        fan.setId(fid);
        fan.setFanId(myId);
        fan.setVlogerId(vlogerId);
        // 判断对方是否关注我，如果关注我则双方互为朋友关系
        Fans vloger = queryFansRelationship(vlogerId,myId);
        if (vloger != null) {
            fan.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKey(vloger);
        } else {
            fan.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fan);
    }

    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("fanId",fanId);
        criteria.andEqualTo("vlogerId",vlogerId);
        List list = fansMapper.selectByExample(example);
        Fans fan = null;
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            fan = (Fans) list.get(0);
        }
        return fan;
    }
}
