package com.lunz.controller;

import com.lunz.base.BaseInfoProperties;
import com.lunz.bo.UpdatedUserBO;
import com.lunz.config.MinIOConfig;
import com.lunz.enums.FileTypeEnum;
import com.lunz.enums.UserInfoModifyType;
import com.lunz.grace.result.GraceJSONResult;
import com.lunz.grace.result.ResponseStatusEnum;
import com.lunz.pojo.Users;
import com.lunz.service.UserService;
import com.lunz.utils.MinIOUtils;
import com.lunz.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
public class UserInfoController extends BaseInfoProperties {
    @Autowired
    private UserService userService;

    @GetMapping("query")
    public Object query(@RequestParam String userId) throws Exception {
        Users user = userService.getUser(userId);
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(user,userVO);

        // 我的关注博主总数量
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
        String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;

        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;

        userVO.setMyFollowsCounts(myFollowsCounts);
        userVO.setMyFansCounts(myFansCounts);
        userVO.setTotalLikeMeCounts(totalLikeMeCounts);

        return GraceJSONResult.ok(userVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                          @RequestParam Integer type)
            throws Exception {
        // type不存在会抛出异常
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);
        return GraceJSONResult.ok(newUserInfo);
    }

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception {
        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // unique file name
        String fileName = userId+"_"+type+"_"+file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,file.getInputStream());
        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + fileName;
        // 修改图片地址到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);
        Users originalUser = userService.getUser(userId);
        if (type == FileTypeEnum.BGIMG.type) {
            // 删除MinIO原图
            MinIOUtils.removeFile(minIOConfig.getBucketName(), FilenameUtils.getName(originalUser.getBgImg()));
            // 将新图url写入db
            updatedUserBO.setBgImg(imgUrl);
        } else {
            MinIOUtils.removeFile(minIOConfig.getBucketName(), FilenameUtils.getName(originalUser.getFace()));
            updatedUserBO.setFace(imgUrl);
        }
        Users user = userService.updateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(user);
    }
}
