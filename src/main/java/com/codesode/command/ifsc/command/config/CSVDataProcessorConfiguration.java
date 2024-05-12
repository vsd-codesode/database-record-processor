package com.codesode.command.ifsc.command.config;

import com.codesode.command.ifsc.command.service.CSVFileTransformerService;
import com.codesode.command.ifsc.command.service.PaymentMethodProcessor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.*;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(name = "command.feature.record-processor.enabled", havingValue = "true")
@EnableConfigurationProperties(value = RecordProcessorCommandProperties.class)
public class CSVDataProcessorConfiguration {

    private final RecordProcessorCommandProperties recordProcessorCommandProperties;
    private final String sourceDir;

    public CSVDataProcessorConfiguration(RecordProcessorCommandProperties recordProcessorCommandProperties,
                                         @Value(value = "${command.working-dir}") String sourceDir) {
        this.recordProcessorCommandProperties = recordProcessorCommandProperties;
        this.sourceDir = sourceDir;
    }

    @Bean
    public DirectChannelSpec processFileChannel() {
        return MessageChannels.direct("processFileChannel");
    }

    @Bean
    public QueueChannelSpec recordProcessingQueue() {
        return MessageChannels.queue("recordProcessingQueue", 1000);
    }

    @Bean
    public DirectChannelSpec successResultWriterChannel() {
        return MessageChannels.direct("successResultWriterChannel");
    }

    @Bean
    public DirectChannelSpec failedResultWriterChannel() {
        return MessageChannels.direct("failedResultWriterChannel");
    }


    @Bean
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(Paths.get(recordProcessorCommandProperties.sourceDir()).toFile());
        source.setFilter(new SimplePatternFileListFilter("*.csv"));
        source.setFilter(new AcceptOnceFileListFilter<>());
        return source;
    }

    @Bean
    public IntegrationFlow csvFileProcessingFlow(MessageSource<File> fileReadingMessageSource,
                                                 CSVFileTransformerService csvFileTransformerService,
                                                 QueueChannelSpec recordProcessingQueue) {
        return IntegrationFlow
                .from(fileReadingMessageSource, e -> e.poller(Pollers.fixedDelay(1000)))
                .enrich(spec -> spec.header("ExecutionTimer", StopWatch.createStarted()))
                .transform(Files.toStringTransformer())
                .transform(csvFileTransformerService::readFromCsv)
                .split()
                .channel(recordProcessingQueue)
                .get();
    }

    @Bean
    public ThreadPoolTaskScheduler taskResultTaskScheduler() {
        final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }

    @Bean
    public PollerSpec bridgePoller() {
        return Pollers
                .fixedDelay(0)
                .taskExecutor(Executors.newFixedThreadPool(20))
                .receiveTimeout(Long.MAX_VALUE);
    }

    @Bean
    public IntegrationFlow recordProcessorFlow(PaymentMethodProcessor paymentMethodProcessor,
                                               CSVFileTransformerService csvFileTransformerService,
                                               QueueChannelSpec recordProcessingQueue) {
        return IntegrationFlow.from(recordProcessingQueue)
                .bridge(bridge -> bridge.poller(bridgePoller()))
                .log(LoggingHandler.Level.INFO, "file-processor", Message::getHeaders)
                .handle(paymentMethodProcessor, "processRecord")
                .route(Message.class,
                        message -> Objects.equals("DONE", message.getHeaders().get("processingStatus")),
                        m -> {
                            m.subFlowMapping(true, sf -> sf.gateway(processedRecordFlw()));
                            m.subFlowMapping(false, sf -> sf.gateway(failedRecordFlow()));
                        })
                .aggregate()
                .transform(csvFileTransformerService::transform)
                .log(LoggingHandler.Level.INFO, "execution-timer", message -> {
                    final int timeTaken = Optional.ofNullable(((StopWatch) message.getHeaders().get("ExecutionTimer")))
                            .map(watch -> watch.getTime(TimeUnit.SECONDS))
                            .map(Math::round)
                            .orElse(0);
                    return "File processing completed in " + timeTaken + " seconds";
                })
                .get();
    }

    @Bean
    public IntegrationFlow processedRecordFlw() {
        return flow -> flow
                .handle(Files.outboundAdapter(Path.of(sourceDir, "success").toFile())
                        .fileExistsMode(FileExistsMode.REPLACE)
                        .deleteSourceFiles(true))
                .log(LoggingHandler.Level.INFO, "Success File", Message::getPayload)
                .handle((payload, headers) -> payload);
    }

    @Bean
    public IntegrationFlow failedRecordFlow() {
        return flow -> flow
                .handle(Files.outboundAdapter(Path.of(sourceDir, "failed").toFile())
                        .fileExistsMode(FileExistsMode.REPLACE)
                        .deleteSourceFiles(true))
                .log(LoggingHandler.Level.INFO, "Failed File", Message::getPayload)
                .handle((payload, headers) -> payload);
    }
}
