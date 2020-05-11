package com.leon.mediamanager.controllers;

import com.leon.mediamanager.payload.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("internalRestService")
public class InternalRestService {

    private static final Logger logger = LoggerFactory.getLogger(InternalRestService.class);

    @Value("${downstream.storageapi}")
    String storageApiUrl;

    @Value("${sso.github.token}")
    String access_token;

    @Value("${sso.github.type}")
    String token_type;

    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;

    public InternalRestService() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("access_token", access_token);
        headers.add("token_type", token_type);
    }

    public ResponseEntity<?> checkStorageApi() {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<?> msg = rest.exchange(storageApiUrl + "api/central/access", HttpMethod.GET, requestEntity, String.class);
        logger.warn(msg.toString());
        return msg;
    }

}
