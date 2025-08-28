package io.crunch.ai.function.statistic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;

import java.io.IOException;

public class UserSearchResultUtil {

    public static String nonMatchAsString(Person person) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(noneMatch(person));
    }

    public static String exactMatchAsString(MatchUser matchUser) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(exactMatch(matchUser));
    }

    public static Person person(String firstName, String lastName, String birthDate) {
        return new Person(firstName, lastName, birthDate);
    }

    private static UserSearchResult exactMatch(MatchUser matchUser) {
        return new ExactMatchResult(matchUser);
    }

    public static Address address(String country, String city, String zipCode, String street, String houseNumber) {
        return new Address(country, city, zipCode, street, houseNumber);
    }

    public static MatchUser matchUser(Person person, Address address, double score, String explanation, String externalId) {
        return new MatchUser(person, address, score, explanation, externalId);
    }

    private static UserSearchResult noneMatch(Person person) {
        return new NoMatchResult(person);
    }

    public static UserSearchResult fromString(String json) throws IOException {
        return new ObjectMapper().readValue(json, UserSearchResult.class);
    }
}
