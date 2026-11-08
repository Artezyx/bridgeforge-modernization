package dev.bridgeforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BridgeForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BridgeForgeApplication.class, args);
    }
}
