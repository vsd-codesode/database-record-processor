package com.codesode.command.ifsc.command;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class IfscValidatorCommandApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(IfscValidatorCommandApplication.class).run(args);
    }

}
