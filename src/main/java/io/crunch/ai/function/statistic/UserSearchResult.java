package io.crunch.ai.function.statistic;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(UserSearchResultTypeIdResolver.class)
public sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult {}

@UserSearchResultSubType("NONEMATCH")
record NoMatchResult(Person person) implements UserSearchResult {}

@UserSearchResultSubType("SIMILARMATCH")
record SimilarMatchesResult(List<MatchUser> users) implements UserSearchResult {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimilarMatchesResult(List<MatchUser> similars))) return false;
        return Objects.equals(Set.copyOf(users), Set.copyOf(similars));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(users);
    }
}

@UserSearchResultSubType("EXACTMATCH")
record ExactMatchResult(MatchUser user) implements UserSearchResult {}

record MatchUser(Person person, Address address, @JsonAlias({"score", "similarityScore"}) Double score, String explanation, String externalId) {
    /**
     * Two MatchUser objects are considered equal if only their person, address, and externalId fields are equal.
     * The score and explanation fields are ignored in the equality check.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatchUser matchUser)) return false;
        return Objects.equals(person, matchUser.person)
                && Objects.equals(address, matchUser.address)
                && Objects.equals(externalId, matchUser.externalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, address, externalId);
    }
}

class UserSearchResultTypeIdResolver extends TypeIdResolverBase {

    @Override
    public String idFromValue(Object value) {
        var ann = value.getClass().getAnnotation(UserSearchResultSubType.class);
        return ann != null ? ann.value() : null;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        for (Class<?> subtype : UserSearchResult.class.getPermittedSubclasses()) {
            var ann = subtype.getAnnotation(UserSearchResultSubType.class);
            if (ann != null && ann.value().equals(id)) {
                return context.constructType(subtype);
            }
        }
        throw new IllegalArgumentException("Unknown type id: " + id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
