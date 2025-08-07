package com.pm.officerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OfficerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficerServiceApplication.class, args);
	}

}
