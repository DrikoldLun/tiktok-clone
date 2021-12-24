package com.lunz.repository;

import com.lunz.mo.MessageMO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO,String> {
    // 集成了很多方法
}
