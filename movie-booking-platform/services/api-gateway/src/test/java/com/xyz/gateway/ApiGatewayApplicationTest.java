package com.xyz.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "auth.service.url=http://localhost:8081"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
