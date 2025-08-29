package io.crunch.ai.function.statistic;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.function.common.Person;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Service class that integrates with the {@code StatisticUser} repository and provides
 * an AI-tool-enabled entry point for searching users in the statistic database.
 * <p>
 * This class is exposed as an AI Tool via {@link Tool} annotation under the name {@code searchUser}.
 * It is intended to be the very first step in any AI-driven person resolution workflow
 * (see {@code RULES} in the {@link Tool} description). All downstream resolution logic
 * (e.g., fetching original addresses, running similarity checks) depends on the result of this call.
 *
 * <h2>Usage Rules for {@code searchUser}</h2>
 * <ul>
 *   <li><b>Mandatory First Call:</b> This tool must always be called first. Skipping it violates the contract.</li>
 *   <li><b>Finalization Cases:</b>
 *     <ul>
 *       <li>If the result type is {@code NONEMATCH} → return it immediately as the final output.</li>
 *       <li>If the result type is {@code EXACTMATCH} → return it immediately as the final output.</li>
 *     </ul>
 *   </li>
 *   <li><b>Person Input:</b>
 *     <ul>
 *       <li>{@code firstName}, {@code lastName}, and {@code birthDate} must be passed <i>exactly as received</i>.</li>
 *       <li>No normalization, correction, invention, or substitution is allowed, even if the input looks malformed.</li>
 *     </ul>
 *   </li>
 *   <li><b>Result Handling:</b>
 *     <ul>
 *       <li>All results must be returned as valid JSON objects with the exact field structure specified.</li>
 *       <li>No explanatory text should be added outside the JSON.</li>
 *       <li>Required fields such as {@code externalId}, {@code score}, and {@code explanation} must always be present (never null or missing).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Possible Result Types</h2>
 * <ol>
 *   <li>
 *     <b>NONEMATCH</b> — No matching user found.
 *     <pre>
 *     {
 *       "type": "NONEMATCH",
 *       "person": {
 *         "firstName": "John",
 *         "lastName": "Doe",
 *         "birthDate": "1980-01-01"
 *       }
 *     }
 *     </pre>
 *   </li>
 *
 *   <li>
 *     <b>EXACTMATCH</b> — Exactly one matching user found.
 *     <ul>
 *       <li>The {@code person} object must be nested inside {@code user}.</li>
 *       <li>{@code externalId}, {@code score}, and {@code explanation} must always be provided.</li>
 *     </ul>
 *     <pre>
 *     {
 *       "type": "EXACTMATCH",
 *       "externalId": "12345",
 *       "user": {
 *         "person": { ... },
 *         "address": { ... },
 *         "score": 1.0,
 *         "explanation": "Exact match found"
 *       }
 *     }
 *     </pre>
 *   </li>
 *
 *   <li>
 *     <b>SIMILARMATCH</b> — Multiple similar users found.
 *     <ul>
 *       <li>Returned as a JSON array under {@code users}.</li>
 *       <li>No {@code externalId} at the top level.</li>
 *     </ul>
 *     <pre>
 *     {
 *       "type": "SIMILARMATCH",
 *       "users": [
 *         {
 *           "person": { ... },
 *           "address": { ... },
 *           "score": 0.85,
 *           "explanation": "High similarity on name, different city"
 *         },
 *         { ... }
 *       ]
 *     }
 *     </pre>
 *   </li>
 * </ol>
 */
@ApplicationScoped
public class StatisticUserService {

    @Tool(name = "searchUser", value = {
        """
        THIS TOOL IS ALWAYS THE FIRST TOOL TO CALL.
        - If the result type is NONEMATCH or EXACTMATCH → YOU MUST RETURN IT IMMEDIATELY AS FINAL OUTPUT. DO NOT call any other tools afterwards.
        - You must call this tool exactly once at the start of processing.
        - Never skip it.

        INPUT:
        - firstName: MUST be copied exactly as given
        - lastName: MUST be copied exactly as given
        - birthDate: MUST be copied exactly as given
        - These fields MUST NEVER be invented, substituted, normalized, or corrected.
        - Even if the input looks invalid (e.g., "NoMatch"), still pass it unchanged.

        Search for a user and its external id in the statistic database based only by Person.
        Always return a valid JSON object. Do not add any additional explanatory text, and do not remove any fields from the JSON object.

        The following cases are possible:
        - If external id does not defined, the result contains only the person data, and return the following JSON structure:
          Example:
          {
           "type": "NONEMATCH",
           "person": {
             "firstName": <firstName>,
             "lastName": <lastName>,
             "birthDate": <birthDate>
           }
          }

        - If exactly one matching user is found, you MUST return the following structure exactly.
          - The `person` object MUST be nested inside the `user` object.
          - Do not move `person` outside of `user`.
          - Do not remove or rename any fields.
          - "externalId", "score", and "explanation" MUST always be present.
            - They must never be omitted, null, or empty.
            - If unknown, use:
              - externalId: "UNKNOWN"
              - score: 1.0
              - explanation: "No explanation available"
          - If exactly one matching user is found, the result is not a JSON array, and contains the external id, and return the following structure:
              Example:
              {
               "type": "EXACTMATCH",
               "externalId": <externalId>,
               "user": {
                 "person": {
                   "firstName": <firstName>,
                   "lastName": <lastName>,
                   "birthDate": <birthDate>
                 },
                 "address": {
                   "country": <country>,
                   "city": <city>,
                   "zipCode": <zipCode>,
                   "street": <street>,
                   "houseNumber": <houseNumber>
                 },
                "score": <similarityScore>,
                "explanation": <explanation>
               }
             }

       - If multiple similar users are found, the result is a JSON array, and does not contain the external id, and return the following structure:
         Example:
         {
           "type": "SIMILARMATCH",
           "users": [
             {
               "person": {
                 "firstName": <firstName>,
                 "lastName": <lastName>,
                 "birthDate": <birthDate>
               },
               "address": {
                 "country": <country>,
                 "city": <city>,
                 "zipCode": <zipCode>,
                 "street": <street>,
                 "houseNumber": <houseNumber>
               },
               "score": <similarityScore>,
               "explanation": <explanation>
             },
             {
               "person": {
                 "firstName": <firstName>,
                 "lastName": <lastName>,
                 "birthDate": <birthDate>
               },
               "address": {
                 "country": <country>,
                 "city": <city>,
                 "zipCode": <zipCode>,
                 "street": <street>,
                 "houseNumber": <houseNumber>
               },
               "score": <similarityScore>,
               "explanation": <explanation>
             }
           ]
         }
     """
    })
    @Transactional
    public UserSearchResult searchUser(@P(value = "The user's first name", required = true) String firstName,
                                       @P(value = "The user's last name", required = true) String lastName,
                                       @P(value = "The user's birth date", required = true)  String birthDate) {
        Log.info("Searching for user with query: firstName=" + firstName + ", lastName=" + lastName + ", birthDate=" + birthDate);
        List<StatisticUser> hits = StatisticUser.find("person.firstName = ?1 and person.lastName = ?2 and person.birthDate = ?3",
                firstName,
                lastName,
                birthDate).list();
        Log.info("Found " + hits.size() + " user(s) in statistic database");
        return switch (hits.size()) {
            case 0 -> toNoMatchResult(firstName, lastName, birthDate);
            case 1 -> toExactMatchResult(hits);
            default -> toSimilarMatchesResult(hits);
        };
    }

    private SimilarMatchesResult toSimilarMatchesResult(List<StatisticUser> hits) {
        return new SimilarMatchesResult(hits.stream().map(u -> toMatchUser(u, "")).toList());
    }

    private ExactMatchResult toExactMatchResult(List<StatisticUser> hits) {
        return new ExactMatchResult(toMatchUser(hits.getFirst(), hits.getFirst().getExternalId()));
    }

    private NoMatchResult toNoMatchResult(String firstName, String lastName, String birthDate) {
        return new NoMatchResult(new Person(firstName, lastName, birthDate));
    }

    private MatchUser toMatchUser(StatisticUser user, String externalId) {
        return new MatchUser(user.getPerson(), user.getAddress(), 0.0, "", externalId);
    }
}
