package com.shopwave;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ShopWaveStarterApplicationTest — smoke test for the full Spring context.
 *
 * @SpringBootTest loads the ENTIRE application context (all beans, DB, etc.)
 * This test simply verifies that the application starts without errors.
 * If any bean is misconfigured, this test will fail.
 *
 * It's the most basic test you can write for a Spring Boot app —
 * "does the application even start?"
 */
@SpringBootTest
class ShopWaveStarterApplicationTest {

    @Test
    void contextLoads() {
        // If this method runs without throwing an exception, the test passes.
        // Spring has verified that all beans are wired correctly.
    }
}
