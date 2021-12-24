package com.lunz.service.impl;

import com.github.pagehelper.PageHelper;
import com.lunz.base.BaseInfoProperties;
import com.lunz.enums.MessageEnum;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.FansMapper;
import com.lunz.mapper.FansMapperCustom;
import com.lunz.pojo.Fans;
import com.lunz.service.FansService;
import com.lunz.service.MsgService;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.FansVO;
import com.lunz.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Autowired
    private MsgService msgService;

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
        // 系统消息：关注
        msgService.createMsg(myId,vlogerId, MessageEnum.FOLLOW_YOU.type,null); // 关注不需要额外信息
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        // 判断对方是否关注我，如果关注我则取消朋友关系
        Fans fan = queryFansRelationship(myId,vlogerId);
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            // 抹除双方的朋友关系，自己的关系删除即可
            Fans pendingFan = queryFansRelationship(vlogerId,myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }
        // 删除自己的关注关联表记录
        fansMapper.delete(fan);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        Fans vloger = queryFansRelationship(myId,vlogerId);
        return vloger != null;
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

    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        PageHelper.startPage(page,pageSize);
        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);
        return setterPagedGrid(list,page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        /**
         * <判断粉丝是否是我的朋友（互粉互关）>
         *     普通做法：
         *     多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下会出现性能问题
         *
         *     常规做法：
         *     1. 避免过多的表关联查询，先查询我的粉丝列表，获得fanslist
         *     2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList获得每一个粉丝，再去数据库查询我是否关注他
         *     3. 如果我也关注他，说明我俩互关（互粉），则标记flag为true，否则false
         *
         *     高端做法：
         *     1. 关注、取关时，关联关系保存在redis中，不要依赖数据库
         *     2. 数据库查询后，直接循环查询redis，这样可以避免第二次循环查询数据库的尴尬局面
         */

        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        PageHelper.startPage(page,pageSize);
        List<FansVO> list = fansMapperCustom.queryMyFans(map);
        for (FansVO f:list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP+":"+myId+":"+f.getFanId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list,page);
    }
}
