package com.lunz.base;

import com.github.pagehelper.PageInfo;
import com.lunz.utils.PagedGridResult;
import com.lunz.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
public class BaseInfoProperties {
    @Autowired
    public RedisOperator redis;

    public static final Integer COMMON_START_PAGE_ZERO = 0;
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    // 短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    // 已入库的短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS_INDB = "redis_vlog_comment_counts_indb";
    // 短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    // 已入库的短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS_INDB = "redis_vlog_comment_liked_counts_indb";
    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";
    // 博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";

    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOG_BE_LIKED_COUNTS_INDB = "redis_vlog_be_liked_counts_indb";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS_INDB = "redis_vloger_be_liked_counts_indb";

    // 用户是否喜欢/点赞视频，取代数据库的关联关系，1：喜欢， 0：不喜欢（默认）redis_user_like_vlog:{userId}:{vlogId}
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";

    public PagedGridResult setterPagedGrid(List<?> list,
                                           Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list); // 每行显示的内容
        gridResult.setPage(page); // 页号
        gridResult.setRecords(pageList.getTotal()); // 总记录数
        gridResult.setTotal(pageList.getPages()); // 总页数
        return gridResult;
    }

    public Integer countsFlushed(String id,String recordType,Integer countsThre) {
        // 点赞完毕，获得当前在redis中的总数
        // 比如获得总计数为 1k/1w/10w，假定阈值（配置）为2k
        // 此时1k满足2000，则触发入库
        String countsStrIndb = redis.get(recordType+"_indb:"+id);
        String countsStr = redis.get(recordType+":"+id);
        log.info("======" + recordType+":"+id + "======");
        Integer counts = 0;
        Integer counts_indb = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (StringUtils.isNotBlank(countsStrIndb)) {
                counts_indb = Integer.valueOf(countsStrIndb);
            }
            if (Math.abs(counts-counts_indb) >= countsThre) {
                redis.set(recordType+"_indb:"+id,""+counts);
                return counts;
            }
        }
        return null;
    }
}
