package com.codesode.command.ifsc.command.domain.repo;

import com.codesode.command.ifsc.command.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, String> {
}
