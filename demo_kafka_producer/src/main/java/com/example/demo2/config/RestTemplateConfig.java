package com.example.demo2.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

 	/**
	 * @methodName restTemplate
     * @param      RestTemplateBuilder restTemplateBuilder
	 * @return     org.springframework.web.client.RestTemplate
	 * @decription org.springframework.web.client.RestTemplate 를 정의하고 Bean 등록.
	 *             Timeout, Client Connection Config  등을 정의
	 */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(2000);
        httpRequestFactory.setReadTimeout(3000);
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .build();
        httpRequestFactory.setHttpClient(httpClient);
        return new RestTemplate(httpRequestFactory);
    }
}