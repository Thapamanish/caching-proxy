package com.example.demo.controller;

import org.springframework.lang.NonNull;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;

public interface ResponseEntityCache extends Cache {
    @Override
    default void put(@NonNull Object key, @NonNull Object value) {
        put(key, (ResponseEntity<String>) value);
    }

    void put(@NonNull Object key, ResponseEntity<String> value);

}
