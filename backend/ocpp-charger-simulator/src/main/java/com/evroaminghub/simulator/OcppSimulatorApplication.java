package com.evroaminghub.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OcppSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(OcppSimulatorApplication.class, args);
    }
}
