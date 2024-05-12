package com.codesode.command.processor.config;


import com.codesode.command.processor.domain.Person;
import com.codesode.command.processor.domain.repo.PersonRepository;
import com.codesode.command.processor.service.CSVFileTransformerService;
import com.codesode.command.processor.service.PaginatedDataLoader;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.FileWritingMessageHandlerSpec;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;

import java.nio.file.Paths;
import java.util.List;


@Configuration
@ConditionalOnProperty(name = "command.feature.db-ingestion.enabled", havingValue = "true")
@EnableConfigurationProperties(value = {DbIngestionProperties.class})
public class CommonDataConfiguration {

    @Resource
    private DbIngestionProperties dbIngestionProperties;

    @Resource
    private PersonRepository  personRepository;

    @Bean
    public PaginatedDataLoader paginatedDataLoader() {
        return new PaginatedDataLoader(personRepository, dbIngestionProperties);
    }

    @Bean
    public FileWritingMessageHandlerSpec fileWritingMessageHandlerSpec() {
        return Files.outboundAdapter(Paths.get(dbIngestionProperties.exportLocation()).toFile());
    }

    @Bean
    public IntegrationFlow pageDataProcessorFlow(PaginatedDataLoader paginatedDataLoader,
                                                 FileWritingMessageHandlerSpec fileWritingMessageHandlerSpec,
                                                 CSVFileTransformerService transformerService) {
        return IntegrationFlow.from(paginatedDataLoader, e -> e.poller(Pollers.fixedDelay(500)))
                .<List<Person>, Object>transform(transformerService::transform)
                .log(LoggingHandler.Level.INFO, "db-ingestion", Message::getHeaders)
                .handle(fileWritingMessageHandlerSpec)
                .nullChannel();
    }
}