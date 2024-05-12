package com.codesode.command.processor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ValidatorCommandApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ValidatorCommandApplication.class).run(args);
    }

}
