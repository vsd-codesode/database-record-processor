package com.codesode.command.processor.service;

import com.codesode.command.processor.config.DbIngestionProperties;
import com.codesode.command.processor.domain.Person;
import com.codesode.command.processor.domain.repo.PersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.integration.endpoint.AbstractMessageSource;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PaginatedDataLoader extends AbstractMessageSource<Page<BigInteger>> {

    private final PersonRepository personRepository;
    private final AtomicReference<Pageable> currentPage;

    public PaginatedDataLoader(PersonRepository personRepository, DbIngestionProperties dbIngestionProperties) {
        this.personRepository = personRepository;
        currentPage = new AtomicReference<>(PageRequest.ofSize(dbIngestionProperties.pageSize()));
    }

    @Override
    protected Object doReceive() {

        final Optional<Page<Person>> pageData = Optional.ofNullable(currentPage.get()).map(personRepository::findAll);

        currentPage.set(pageData.filter(Slice::hasNext).map(Slice::nextPageable).orElse(null));

        return pageData.map(Slice::getContent).orElse(null);
    }

    @Override
    public String getComponentType() {
        return "commands:inbound-channel-adapter";
    }
}
