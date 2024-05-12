package com.codesode.command.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "command.feature.record-processor")
public record RecordProcessorCommandProperties(
        boolean enabled,
        String sourceDir
) {

}
