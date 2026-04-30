package com.evroaminghub.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class EnergyReportingApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergyReportingApplication.class, args);
    }
}
