package com.pm.borrowerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class BorrowerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BorrowerServiceApplication.class, args);
    }

}
