
package com.atc.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.atc")
@EntityScan(basePackages = {
    "com.atc.model",
    "com.atc.history"
})
@EnableJpaRepositories(basePackages = "com.atc.repository")
public class ATCApplication {
    public static void main(String[] args) {
        SpringApplication.run(ATCApplication.class, args);
    }
}
