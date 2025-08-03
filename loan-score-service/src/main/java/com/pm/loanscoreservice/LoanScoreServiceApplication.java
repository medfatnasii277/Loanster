package com.pm.loanscoreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoanScoreServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanScoreServiceApplication.class, args);
	}

}
