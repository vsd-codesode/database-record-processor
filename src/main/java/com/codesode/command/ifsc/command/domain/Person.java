package com.codesode.command.ifsc.command.domain;

import jakarta.persistence.*;

import java.util.Date;


/**
 * A simple POJO representing a Person.
 * TODO replace this class with your domain object
 */
@Entity
@Table(name = "PEOPLE")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE_TIME")
    private Date createdDateTime;

    public Person() {
        this.createdDateTime = new Date();
    }

    public Person(String name) {
        this.name = name;
        this.createdDateTime = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdDateTime=" + createdDateTime +
                '}';
    }
}