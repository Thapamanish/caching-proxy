package com.example.demo.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CacheManagerService {
    
    private final ConcurrentMap<String, ResponseEntity<String>> cache = new ConcurrentHashMap<>();
    
    public static final String CACHE_HIT = "HIT";
    public static final String CACHE_MISS = "MISS";
    
    public ResponseEntity<String> getCachedResponse(String key) {
        return cache.get(key);
    }
    
    public void cacheResponse(String key, ResponseEntity<String> response) {
        cache.put(key, response);
    }
    
    public ResponseEntity<String> addCacheHeader(ResponseEntity<String> response, String cacheStatus) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(response.getHeaders());
        headers.set("X-Cache", cacheStatus);
        
        return ResponseEntity.status(response.getStatusCode())
                .headers(headers)
                .body(response.getBody());
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public int getCacheSize() {
        return cache.size();
    }
}
