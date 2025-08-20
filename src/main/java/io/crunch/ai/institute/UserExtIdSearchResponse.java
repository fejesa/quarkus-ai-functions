package io.crunch.ai.institute;

import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;

import java.util.List;

public record UserExtIdSearchResponse(String extId, Person person, List<AddressResponse> addresses) {

    public record AddressResponse(Double score, Address address) {
    }
}
