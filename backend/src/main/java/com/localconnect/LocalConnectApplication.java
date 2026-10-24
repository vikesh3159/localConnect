package com.localconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LocalConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalConnectApplication.class, args);
    }
}

