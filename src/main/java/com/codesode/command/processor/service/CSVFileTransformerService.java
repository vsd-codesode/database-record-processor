package com.codesode.command.processor.service;

import com.codesode.command.processor.domain.Person;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CSVFileTransformerService {

    private final CsvMapper csvMapper;

    public CSVFileTransformerService() {
        this.csvMapper = new CsvMapper();
    }

    public String transform(Object payload) {

        final CsvSchema schema = CsvSchema.builder()
                .setUseHeader(true)
                .addColumn("id")
                .addColumn("name")
                .addColumn("createdDateTime")
                .build();

        try {
            return csvMapper.writer().with(schema).writeValueAsString(payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object> readFromCsv(Object message) {
        final CsvSchema schema = CsvSchema.builder()
                .setUseHeader(true)
                .addColumn("id")
                .addColumn("name")
                .addColumn("createdDateTime")
                .build();
        try {
            return csvMapper.readerFor(Person.class).with(schema).readValues(String.valueOf(message)).readAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
