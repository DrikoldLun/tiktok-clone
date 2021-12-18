package com.lunz.service;

import com.lunz.bo.UpdatedUserBO;
import com.lunz.pojo.Users;

public interface UserService {

    /**
     * 判断用户是否存在，如果存在则返回用户信息
     */
    public Users queryMobileIsExist(String Mobile);

    /**
     * 创建用户信息并且返回用户对象
     */
    public Users createUser(String mobile);

    /**
     * 根据用户主键查询用户信息
     */
    public Users getUser(String userId);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);
}
