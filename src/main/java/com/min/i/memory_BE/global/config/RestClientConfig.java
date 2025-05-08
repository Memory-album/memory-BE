package com.min.i.memory_BE.global.config;

import com.min.i.memory_BE.global.error.exception.FastApiServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RestClientConfig {
    
    @Value("${fastapi.server.url}")
    private String fastApiUrl;
    
    @Value("${fastapi.connection.timeout:5000}")
    private int connectionTimeout;
    
    @Value("${fastapi.read.timeout:30000}")
    private int readTimeout;
    
    @Bean
    public RestTemplate restTemplate() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(connectionTimeout, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(readTimeout, TimeUnit.MILLISECONDS))
                .build();
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(config);
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
                clientBuilder.build());
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is4xxClientError() || 
                       response.getStatusCode().is5xxServerError();
            }
            
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                throw new FastApiServiceException(
                    "FastAPI 서버 오류: " + response.getStatusCode() + " " + 
                    StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8)
                );
            }
        });
        
        return restTemplate;
    }
} 