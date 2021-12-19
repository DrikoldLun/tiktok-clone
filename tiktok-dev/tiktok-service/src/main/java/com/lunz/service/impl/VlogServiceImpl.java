package com.lunz.service.impl;

import com.lunz.bo.VlogBO;
import com.lunz.enums.YesOrNo;
import com.lunz.mapper.VlogMapper;
import com.lunz.mapper.VlogMapperCustom;
import com.lunz.pojo.Vlog;
import com.lunz.service.VlogService;
import com.lunz.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl implements VlogService {
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
    public List<IndexVlogVO> getIndexVlogList(String search) {
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search",search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);
        return list;
    }
}
