package org.example.magazynieruz;

import org.example.magazynieruz.config.AlertConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AlertConfig.class)
public class MagazynieruzApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagazynieruzApplication.class, args);
    }

}
