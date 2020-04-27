package com.leon.mediamanager;

import com.leon.service.RESTfulService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class MediamanagerApplication {

	private static final Logger logger = LoggerFactory.getLogger(MediamanagerApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(MediamanagerApplication.class, args);
	}

}
