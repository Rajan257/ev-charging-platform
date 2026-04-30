package com.evroaminghub.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class StationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StationServiceApplication.class, args);
    }
}
