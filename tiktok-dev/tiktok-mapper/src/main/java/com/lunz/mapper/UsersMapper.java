package com.lunz.mapper;

import com.lunz.my.mapper.MyMapper;
import com.lunz.pojo.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersMapper extends MyMapper<Users> {
}