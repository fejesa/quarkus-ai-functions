package io.crunch.ai.function.statistic;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;

import java.io.IOException;
import java.util.List;

public class UserSearchResultUtil {

    public static String nonMatchAsString(Person person) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(noneMatch(person));
    }

    public static String exactMatchAsString(MatchUser matchUser) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(exactMatch(matchUser));
    }

    public static String similarMatchesAsString(List<MatchUser> matchUsers) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(similarMatches(matchUsers));
    }

    public static Person person(String firstName, String lastName, String birthDate) {
        return new Person(firstName, lastName, birthDate);
    }

    private static SimilarMatchesResult similarMatches(List<MatchUser> matchUsers) {
        return new SimilarMatchesResult(matchUsers);
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

    public static void main(String[] args) throws JsonProcessingException {
        var s = """
                {
                  "type":"SIMILARMATCH",
                  "users":[{
                    "person":{"firstName": "Anna", "lastName": "Schmidt", "birthDate": "1993-09-12"},
                    "address":{"country":"Germany","city":"Berlin","zipCode":"10117","street":"Unter den Linden","houseNumber":"77"},
                    "similarityScore":0.8297546897546898,
                    "explanation":"country matches, city matches, zipCode differs, street matches, houseNumber differs"
                  },{
                    "person":{"firstName": "Anna", "lastName": "Schmidt", "birthDate": "1993-09-12"},
                    "address":{"country":"Germany","city":"Potsdam","zipCode":"10117","street":"Unter den Linden","houseNumber":"56"},
                    "similarityScore":0.6653852610374349,
                    "explanation":"country matches, city differs, zipCode matches, street matches, houseNumber differs"
                  },{
                    "person":{"firstName": "Anna", "lastName": "Schmidt", "birthDate": "1993-09-12"},
                    "address":{"country":"Austria","city":"Vienna","zipCode":"10117","street":"Unter den Linden","houseNumber":"13"},
                    "similarityScore":0.5788359788359788,
                    "explanation":"country differs, city matches, zipCode matches, street matches, houseNumber differs"
                  }]
                }
                """;
        var result = new ObjectMapper().readValue(s, UserSearchResult.class);
        System.out.println(result);
    }
}
