package com.example.demo.util;

import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;

public class CustomCacheResolver implements CacheResolver {
    private final CacheResolver delegate;

    public CustomCacheResolver(final CacheResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public Collection<CustomHeaderResponseCache> resolveCaches(final CacheOperationInvocationContext<?> context) {
        return delegate.resolveCaches(context)
                .stream()
                .map(CustomHeaderResponseCache::new)
                .toList();
    }
}
