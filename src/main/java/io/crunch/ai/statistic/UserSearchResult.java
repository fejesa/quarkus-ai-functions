package io.crunch.ai.statistic;

import dev.langchain4j.model.output.structured.Description;
import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;

import java.util.List;

public sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult, EmptyUserSearchResult {
}

@Description("A user without including external ID and address. This includes only the person details.")
record NoMatchResult(Person person) implements UserSearchResult {
}

@Description("A list of users with similar attributes to the search criteria, but not an exact match. " +
        "This includes a list of users with their person details and address.")
record SimilarMatchesResult(List<MatchUser> users) implements UserSearchResult {
}

@Description("A user with an exact match to the search criteria, including external ID, person details, and address.")
record ExactMatchResult(String externalId, MatchUser user) implements UserSearchResult {
}

record EmptyUserSearchResult() implements UserSearchResult {
}

record MatchUser(Person person, Address address) {
}
