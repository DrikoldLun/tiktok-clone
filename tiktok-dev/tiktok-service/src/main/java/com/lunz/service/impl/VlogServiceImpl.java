package com.lunz.service.impl;

import com.github.pagehelper.PageHelper;
import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.VlogBO;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.MyLikedVlogMapper;
import com.lunz.mapper.VlogMapper;
import com.lunz.mapper.VlogMapperCustom;
import com.lunz.pojo.MyLikedVlog;
import com.lunz.pojo.Vlog;
import com.lunz.service.FansService;
import com.lunz.service.VlogService;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private FansService fansService;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        String vid = sid.nextShort();
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO,vlog);
        vlog.setId(vid);
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type); // 默认公开
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        vlogMapper.insert(vlog);
    }

    private void checklikefollowstatus(String userId,IndexVlogVO v) {
        String vlogId = v.getVlogId();
        if (StringUtils.isNotBlank(userId)) {
            // 判断用户是否关注该博主
            v.setDoIFollowVloger(fansService.queryDoIFollowVloger(userId,v.getVlogerId()));
            // 判断当前用户是否点赞过视频
            v.setDoILikeThisVlog(doILikeVlog(userId,vlogId));
        }
        // 获得当前视频被点赞过的总数
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));
    }

    @Override
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize) {
        PageHelper.startPage(page,pageSize); // 针对方法的拦截，相当于在sql查询中limit
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search",search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
        for (IndexVlogVO v:list) {
            // 判断当前用户是否点赞过视频
            checklikefollowstatus(userId,v);
        }
        return setterPagedGrid(list,page);
    }

    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS+":"+vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    private boolean doILikeVlog(String myId,String vlogId) {
        String doILike = redis.get(REDIS_USER_LIKE_VLOG+":"+myId+":"+vlogId);
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            return true;
        }
        return false;
    }

    @Override
    public IndexVlogVO getVlogDetailById(String userId,String vlogId) {
        Map<String,Object> map = new HashMap<>();
        map.put("vlogId",vlogId);
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        if (list != null && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            checklikefollowstatus(userId,vlogVO);
            return vlogVO;
        }
        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",vlogId);
        criteria.andEqualTo("vlogerId",userId);
        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);
        vlogMapper.updateByExampleSelective(pendingVlog,example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId",userId);
        criteria.andEqualTo("isPrivate",yesOrNo);
        PageHelper.startPage(page,pageSize);
        List<Vlog> list = vlogMapper.selectByExample(example);
        return setterPagedGrid(list,page);
    }

    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        String rid = sid.nextShort();
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setUserId(userId);
        likedVlog.setVlogId(vlogId);
        myLikedVlogMapper.insert(likedVlog);
    }

    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        // 复合主键查询记录并删除
        likedVlog.setUserId(userId);
        likedVlog.setVlogId(vlogId);
        myLikedVlogMapper.delete(likedVlog);
    }

    @Override
    public PagedGridResult queryMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);
        return setterPagedGrid(list,page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);
        for (IndexVlogVO v:list) {
            checklikefollowstatus(myId,v);
        }
        return setterPagedGrid(list,page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);
        for (IndexVlogVO v:list) {
            checklikefollowstatus(myId,v);
        }
        return setterPagedGrid(list,page);
    }
}
