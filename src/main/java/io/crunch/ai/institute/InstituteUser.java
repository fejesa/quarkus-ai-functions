package io.crunch.ai.institute;

import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "INSTITUTE_USER")
public class InstituteUser extends PanacheEntity {

    @Embedded
    private Person person;

    @Embedded
    private Address address;

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
