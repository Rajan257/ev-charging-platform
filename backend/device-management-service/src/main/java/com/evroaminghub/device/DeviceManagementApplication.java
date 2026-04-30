package com.evroaminghub.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableKafka
@EnableScheduling
public class DeviceManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceManagementApplication.class, args);
    }
}
