package com.lunz.service;

import com.lunz.bo.VlogBO;
import com.lunz.utils.PagedGridResult;
import com.lunz.vo.IndexVlogVO;

import java.util.List;

public interface VlogService {

    /**
     * 新增vlog视频
     */
    public void createVlog(VlogBO vlogBO);

    /**
     * 查询首页/搜索的vlog列表
     */
    public PagedGridResult getIndexVlogList(String search,
                                            Integer page,
                                            Integer pageSzie);

}