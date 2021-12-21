package com.lunz.service.impl;

import com.github.pagehelper.PageHelper;
import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.VlogBO;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.VlogMapper;
import com.lunz.mapper.VlogMapperCustom;
import com.lunz.pojo.Vlog;
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

    @Override
    public PagedGridResult getIndexVlogList(String search,
                                              Integer page,
                                              Integer pageSize) {
        PageHelper.startPage(page,pageSize); // 针对方法的拦截，相当于在sql查询中limit
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search",search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
        return setterPagedGrid(list,page);
    }

    @Override
    public IndexVlogVO getVlogDetailById(String vlogId) {
        Map<String,Object> map = new HashMap<>();
        map.put("vlogId",vlogId);
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        if (list != null && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
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
}
