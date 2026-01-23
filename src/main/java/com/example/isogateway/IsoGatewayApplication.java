package com.example.isogateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IsoGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(IsoGatewayApplication.class, args);
	}

}