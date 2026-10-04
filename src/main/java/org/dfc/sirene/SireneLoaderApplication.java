package org.dfc.sirene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SireneLoaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SireneLoaderApplication.class, args);
    }
}
