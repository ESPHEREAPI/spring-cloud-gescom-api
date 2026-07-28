package com.mcommerce.config_serveur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServeurApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServeurApplication.class, args);
	}

}
