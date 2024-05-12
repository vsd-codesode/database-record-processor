package com.codesode.command.processor.domain.repo;

import com.codesode.command.processor.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, String> {
}
