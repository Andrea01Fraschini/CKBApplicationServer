package com.example.demo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface PairKeyValueRepository extends MongoRepository<PairKeyValue, String> {

    @Query("{'key': '?0'}")
    Optional<PairKeyValue> findPairKeyValueByKey(String key);
}
