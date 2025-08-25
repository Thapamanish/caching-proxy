package com.example.demo.controller;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

import static com.example.demo.controller.CacheHeader.HIT;
import static java.util.stream.Collectors.toUnmodifiableMap;

@Builder
public final class CachedResponseEntity extends ResponseEntity<String> {
    public static final String X_CACHE = "X-Cache";
    private ResponseEntity<String> delegate;

    public CachedResponseEntity(final ResponseEntity<String> delegate) {
        super(delegate.getBody(), delegate.getStatusCode());
        this.delegate = delegate;

    }

    @Override
    @NonNull
    public HttpHeaders getHeaders() {
        final Map<String, List<String>> originalHeaders = this.delegate.getHeaders()
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(X_CACHE))
                .collect(toUnmodifiableMap(Map.Entry::getKey,
                        Map.Entry::getValue
                ));


        final HttpHeaders cacheInfoHeaders = new HttpHeaders();
        cacheInfoHeaders.putAll(originalHeaders);
        cacheInfoHeaders.add(X_CACHE, HIT.toString());
        return cacheInfoHeaders;
    }
}
