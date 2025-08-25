package com.example.demo.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;

public interface ProxyService {
    @GetExchange(value = "/products")
    ResponseEntity<String> getProducts();
    
    // Generic method for any path
    ResponseEntity<String> getResource(String path);
}
