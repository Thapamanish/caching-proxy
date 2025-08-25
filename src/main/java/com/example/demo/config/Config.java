package com.example.demo.config;

/*
*This Config class provides the configuration for
* our caching proxy application.
*/

import com.example.demo.service.ProxyService;
import com.example.demo.util.CustomCacheResolver;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class Config {
    @Value("${global.origin}")
    private String origin;
    
    private static final String MISS = "MISS";
    private static final String HIT = "HIT";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("cache1");
    }

    @Bean
    public CacheResolver cacheResolver(final CacheManager cacheManager) {
        return new CustomCacheResolver(new SimpleCacheResolver(cacheManager));
    }

    @Bean
    public ClientHttpRequestInterceptor clientHttpRequestInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            @NonNull
            public ClientHttpResponse intercept(@NonNull final org.springframework.http.HttpRequest request,
                                                @NonNull final byte[] body,
                                                @NonNull final ClientHttpRequestExecution execution) throws IOException {
                ClientHttpResponse resp = execution.execute(request, body);
                return getResponseWithXCacheMissHeader(resp);
            }
        };
    }

    private ClientHttpResponse getResponseWithXCacheMissHeader(final ClientHttpResponse resp) {
        return new ClientHttpResponse() {
            @Override
            public HttpHeaders getHeaders() {
                final HttpHeaders originalHeaders = resp.getHeaders();
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.put("X-Cache", List.of(MISS.toString()));
                httpHeaders.putAll(originalHeaders);
                return httpHeaders;
            }

            @Override
            public InputStream getBody() throws IOException {
                return resp.getBody();
            }

            @Override
            public HttpStatusCode getStatusCode() throws IOException {
                return resp.getStatusCode();
            }

            @Override
            public String getStatusText() throws IOException {
                return resp.getStatusText();
            }

            @Override
            public void close() {
                resp.close();
            }
        };
    }


    @Bean
    public RestClient restClient(final ClientHttpRequestInterceptor interceptor) {

        return RestClient.builder().baseUrl(origin).requestInterceptor(interceptor).build();
    }

    @Bean
    public RestClientAdapter restClientAdapter(final RestClient restClient) {
        return RestClientAdapter.create(restClient);
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(final RestClientAdapter restClientAdapter) {
        return HttpServiceProxyFactory.builderFor(restClientAdapter).build();
    }

    // Comment out the old proxy service that uses HttpServiceProxyFactory
    // @Bean
    // public ProxyService proxyService(final HttpServiceProxyFactory httpServiceProxyFactory) {
    //     return httpServiceProxyFactory.createClient(ProxyService.class);
    // }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
