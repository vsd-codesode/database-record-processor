package com.codesode.command.ifsc.command.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "command.feature.db-ingestion")
public record DbIngestionProperties(
        boolean enabled,
        int pageSize,
        String exportLocation
) {

}
