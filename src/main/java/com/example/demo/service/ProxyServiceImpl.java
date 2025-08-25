package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class ProxyServiceImpl implements ProxyService {
    
    private final RestTemplate restTemplate;
    private final String originUrl;
    private final CacheManagerService cacheManager;
    
    @Autowired
    public ProxyServiceImpl(RestTemplate restTemplate, @Value("${global.origin}") String originUrl, CacheManagerService cacheManager) {
        this.restTemplate = restTemplate;
        this.originUrl = originUrl;
        this.cacheManager = cacheManager;
    }
    
    @Override
    public ResponseEntity<String> getProducts() {
        // Keep the existing method for backward compatibility
        return getResource("/products");
    }
    
    @Override
    public ResponseEntity<String> getResource(String path) {
        // Check cache first
        ResponseEntity<String> cachedResponse = cacheManager.getCachedResponse(path);
        if (cachedResponse != null) {
            // Cache HIT - return cached response with HIT header
            return cacheManager.addCacheHeader(cachedResponse, CacheManagerService.CACHE_HIT);
        }
        
        // Cache MISS - fetch from origin server
        String fullUrl = UriComponentsBuilder
            .fromHttpUrl(originUrl)
            .path(path)
            .build()
            .toUriString();
            
        // Create a GET request to the origin server
        RequestEntity<Void> request = RequestEntity
            .method(HttpMethod.GET, URI.create(fullUrl))
            .build();
            
        // Forward the request to the origin server
        ResponseEntity<String> originResponse = restTemplate.exchange(request, String.class);
        
        // Cache the response for future requests
        cacheManager.cacheResponse(path, originResponse);
        
        // Return response with MISS header
        return cacheManager.addCacheHeader(originResponse, CacheManagerService.CACHE_MISS);
    }
}
