package com.example.demo;

import java.util.Optional;

public interface PairKeyValueRepository {
    Optional<PairKeyValue> findPairKeyValueByKey(String key);
}
