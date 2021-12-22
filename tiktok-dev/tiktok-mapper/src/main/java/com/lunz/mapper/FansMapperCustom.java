package com.lunz.mapper;

import com.lunz.my.mapper.MyMapper;
import com.lunz.pojo.Fans;
import com.lunz.vo.FansVO;
import com.lunz.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {
    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String,Object> map);
    public List<FansVO> queryMyFans(@Param("paramMap") Map<String,Object> map);
}