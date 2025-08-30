package io.crunch.ai.function.statistic;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

/**
 * Represents the result of a user search in the statistics service.
 * <p>
 * This sealed interface defines a polymorphic hierarchy of possible outcomes
 * when querying users. It is annotated with Jackson’s {@link JsonTypeInfo} to
 * allow polymorphic serialization and deserialization, using a custom
 * {@link UserSearchResultTypeIdResolver}.
 * <p>
 * The type of result is discriminated by the {@code type} property in the JSON payload:
 * <ul>
 *   <li>{@code NONEMATCH} → {@link NoMatchResult}</li>
 *   <li>{@code SIMILARMATCH} → {@link SimilarMatchesResult}</li>
 *   <li>{@code EXACTMATCH} → {@link ExactMatchResult}</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Jackson polymorphic handling is fully explicit and type-safe.</li>
 *   <li>The sealed interface ensures only the three permitted subclasses can represent search results.</li>
 *   <li>The type IDs are controlled via {@link UserSearchResultSubType} annotations on the subtypes.</li>
 * </ul>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(UserSearchResultTypeIdResolver.class)
public sealed interface UserSearchResult permits NoMatchResult, SimilarMatchesResult, ExactMatchResult {}

/**
 * Represents the case where no user matches the given search criteria.
 * <p>
 * Contains only the original {@link Person} query that was used for the lookup.
 */
@UserSearchResultSubType("NONEMATCH")
@JsonIgnoreProperties(ignoreUnknown = true)
record NoMatchResult(Person person) implements UserSearchResult {}

/**
 * Represents the case where multiple users are similar to the query but none is an exact match.
 * <p>
 * Contains a list of {@link MatchUser} candidates. The equality semantics are
 * defined in a set-like manner: two {@code SimilarMatchesResult} instances are
 * considered equal if they contain the same set of users, regardless of order.
 *
 * <h2>Equality Contract</h2>
 * <ul>
 *   <li>Order of users in the list does not matter.</li>
 *   <li>Duplicates are collapsed by using {@link Set#copyOf(List)} internally.</li>
 *   <li>{@link MatchUser#equals(Object)} defines equality between candidates.</li>
 * </ul>
 */
@UserSearchResultSubType("SIMILARMATCH")
@JsonIgnoreProperties(ignoreUnknown = true)
record SimilarMatchesResult(@JsonAlias({"similarUsers", "users", "candidates"}) List<MatchUser> users) implements UserSearchResult {

    public SimilarMatchesResult {
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("Users list must not be null or empty");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimilarMatchesResult(List<MatchUser> similars))) return false;
        return users != null && Objects.equals(Set.copyOf(users), Set.copyOf(similars));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(users);
    }
}

/**
 * Represents the case where exactly one user matches the query.
 * <p>
 * Contains the matched {@link MatchUser} entry.
 */
@UserSearchResultSubType("EXACTMATCH")
@JsonIgnoreProperties(ignoreUnknown = true)
record ExactMatchResult(MatchUser user) implements UserSearchResult {}

/**
 * Represents a single user candidate in a search result.
 * <p>
 * Each match contains:
 * <ul>
 *   <li>{@link Person} → the person’s identifying attributes (first name, last name, birth date).</li>
 *   <li>{@link Address} → the associated address of the user.</li>
 *   <li>{@code score} → similarity score, may be provided under multiple aliases
 *       (JSON properties {@code score} or {@code similarityScore}).</li>
 *   <li>{@code explanation} → human-readable reasoning why this candidate was matched.</li>
 *   <li>{@code externalId} → stable system identifier for this user.</li>
 * </ul>
 *
 * <h2>Equality Contract</h2>
 * <ul>
 *   <li>Only {@code person}, {@code address}, and {@code externalId} are considered.</li>
 *   <li>{@code score} and {@code explanation} are explicitly ignored in equality checks.</li>
 *   <li>This design allows different similarity computations to be compared consistently.</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record MatchUser(Person person, Address address, @JsonAlias({"score", "similarityScore"}) Double score, String explanation, String externalId) {

    public MatchUser {
        if (person == null) {
            throw new IllegalArgumentException("Person must not be null");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address must not be null");
        }
        if (externalId == null) {
            throw new IllegalArgumentException("ExternalId must not be null");
        }
    }

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

/**
 * Custom Jackson type ID resolver for {@link UserSearchResult}.
 * <p>
 * This resolver maps between the {@code type} property in JSON and the concrete
 * Java subtype. It uses the {@link UserSearchResultSubType} annotation on each
 * permitted subclass to determine the type identifier.
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>{@link #idFromValue(Object)} returns the annotation value of the current class.</li>
 *   <li>{@link #typeFromId(DatabindContext, String)} performs the inverse mapping by
 *       scanning permitted subclasses of {@link UserSearchResult} and matching their
 *       {@link UserSearchResultSubType} value.</li>
 *   <li>Unknown type IDs will result in an {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * <h2>Mechanism</h2>
 * This resolver enforces a strict, annotation-driven mapping instead of relying
 * on class names or defaults, making the serialization contract stable and explicit.
 */
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
