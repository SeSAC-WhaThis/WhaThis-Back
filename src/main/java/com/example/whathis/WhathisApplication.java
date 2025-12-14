package com.example.whathis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WhathisApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhathisApplication.class, args);
    }

}
