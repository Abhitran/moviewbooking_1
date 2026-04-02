package com.xyz.theatre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.xyz.theatre", "com.xyz.common"})
@EnableJpaAuditing
@EnableCaching
=======
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.xyz.theatre", "com.xyz.common"})
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
public class TheatreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheatreServiceApplication.class, args);
    }
}
