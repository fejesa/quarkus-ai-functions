package io.crunch.ai.statistic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(UserSearchResultTypeIdResolver.class)
public sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult {}

@UserSearchResultSubType("NOMATCH")
record NoMatchResult(Person person) implements UserSearchResult {}

@UserSearchResultSubType("SIMILARMATCH")
record SimilarMatchesResult(List<MatchUser> users) implements UserSearchResult {}

@UserSearchResultSubType("EXACTMATCH")
record ExactMatchResult(String externalId, MatchUser user) implements UserSearchResult {}

record MatchUser(Person person, Address address, Double score, String explanation) {}

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
