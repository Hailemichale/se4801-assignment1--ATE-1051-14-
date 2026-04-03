package com.shopwave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the ShopWave Starter application.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration        — marks this as a source of bean definitions
 *   - @EnableAutoConfiguration — tells Spring Boot to auto-configure based on classpath
 *   - @ComponentScan        — scans this package (and sub-packages) for Spring components
 */
@SpringBootApplication
public class ShopWaveStarterApplication {

    public static void main(String[] args) {
        // This bootstraps the entire Spring application context
        SpringApplication.run(ShopWaveStarterApplication.class, args);
    }
}
