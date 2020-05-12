package com.leon.mediamanager.controllers;

import com.leon.mediamanager.payload.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service("internalRestService")
public class InternalRestService {

    private static final Logger logger = LoggerFactory.getLogger(InternalRestService.class);

    @Value("${downstream.storageapi}")
    String storageApiUrl;

    @Value("${sso.github.token}")
    String access_token;

    @Value("${sso.github.type}")
    String token_type;

    @Autowired
    OAuth2RestTemplate oAuth2RestTemplate;

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
        URI uri = UriComponentsBuilder.fromHttpUrl(storageApiUrl).pathSegment("api/central/access").build().toUri();
        ResponseEntity<?> msg = oAuth2RestTemplate.exchange(RequestEntity.get(uri).build(), String.class);
        logger.warn(msg.toString());
        return msg;
    }

}
