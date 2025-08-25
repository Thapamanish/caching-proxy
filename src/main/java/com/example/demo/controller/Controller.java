package com.example.demo.controller;

import com.example.demo.service.ProxyService;
import com.example.demo.service.CacheManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private final ProxyService proxyService;
    private final CacheManagerService cacheManager;

    //Spring uses constructor injection to inject an instance of ProxyService into this controller.
    @Autowired
    public Controller(final ProxyService proxyService, CacheManagerService cacheManager) {
        this.proxyService = proxyService;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/products")
    public ResponseEntity<String> products() {
        return proxyService.getProducts();
    }
    
    @GetMapping("/{path:.*}")
    public ResponseEntity<String> proxyRequest(@PathVariable String path) {
        return proxyService.getResource("/" + path);
    }
    
    @PostMapping("/admin/clear-cache")
    public ResponseEntity<String> clearCache() {
        int cacheSize = cacheManager.getCacheSize();
        cacheManager.clearCache();
        return ResponseEntity.ok("Cache cleared successfully! Removed " + cacheSize + " entries.");
    }
    
    @GetMapping("/admin/cache-stats")
    public ResponseEntity<String> getCacheStats() {
        return ResponseEntity.ok("Cache size: " + cacheManager.getCacheSize() + " entries");
    }

}
