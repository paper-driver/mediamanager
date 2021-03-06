package com.leon.mediamanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
//@EnableOAuth2Sso
public class MediamanagerApplication {

	private static final Logger logger = LoggerFactory.getLogger(MediamanagerApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(MediamanagerApplication.class, args);
	}

	@Bean
	@ConfigurationProperties("security.oauth2.client")
	protected ClientCredentialsResourceDetails oAuthDetails() {
		return new ClientCredentialsResourceDetails();
	}

	@Bean(name="customRestTemplate")
	protected RestTemplate restTemplate() {
		return new OAuth2RestTemplate(oAuthDetails());
	}

//	@Bean
//	OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext, OAuth2ProtectedResourceDetails details) {
//		return new OAuth2RestTemplate(details, oauth2ClientContext);
//	}

}
