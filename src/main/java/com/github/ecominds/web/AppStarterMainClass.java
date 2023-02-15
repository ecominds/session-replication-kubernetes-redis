/**
 * @author	: Rajiv Kumar
 * @project	: boot-webapp
 * @since	: 1.0.0
 * @date	: 09-Feb-2023
 */

package com.github.ecominds.web;

import javax.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableRedisHttpSession
public class AppStarterMainClass {

	public static void main(String[] args) {
		log.info("Starting Application...");
		SpringApplication.run(AppStarterMainClass.class, args);
	}
	
	@Bean
	public CommandLineRunner runner() {
		return args -> {
			log.info("AppStarterMainClass started...");
		};
	}

	@PreDestroy
	public void onExit() {
		log.info("Aplication context destroyed");
	}
}