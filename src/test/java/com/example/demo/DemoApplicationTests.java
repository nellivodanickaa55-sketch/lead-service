package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Тест не зависит ни от файловой базы, ни от secrets.properties
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "app.mail.from=test@example.com",
        "app.mail.to=test@example.com"
})
class DemoApplicationTests {

    @Test
    void contextLoads() {
        // Тест проходит, если приложение успешно поднялось
    }
}