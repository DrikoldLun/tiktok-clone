package com.lunz.repository;

import com.lunz.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO,String> {
    // 集成了很多方法，类似于JPA

    // 通过实现Repository，自定义条件查询，此处Pageable意为实现分页能力
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toUserId, Pageable pageable);
}
