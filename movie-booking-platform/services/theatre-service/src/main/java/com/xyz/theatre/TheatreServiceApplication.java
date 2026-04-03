package com.xyz.theatre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.xyz.theatre", "com.xyz.common"})
@EnableJpaAuditing
@EnableCaching
public class TheatreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheatreServiceApplication.class, args);
    }
}
