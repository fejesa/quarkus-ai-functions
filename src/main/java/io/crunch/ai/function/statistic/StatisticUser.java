package io.crunch.ai.function.statistic;

import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "STATISTIC_USER")
public class StatisticUser extends PanacheEntity {

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Embedded
    private Person person;

    @Embedded
    private Address address;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
