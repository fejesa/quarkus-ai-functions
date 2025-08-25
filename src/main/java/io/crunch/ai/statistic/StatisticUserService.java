package io.crunch.ai.statistic;

import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.common.Person;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

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
    public UserSearchResult searchUser(String firstName, String lastName, String birthDate) {
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
        return new SimilarMatchesResult(hits.stream().map(this::toMatchUser).toList());
    }

    private ExactMatchResult toExactMatchResult(List<StatisticUser> hits) {
        return new ExactMatchResult(hits.getFirst().getExternalId(), toMatchUser(hits.getFirst()));
    }

    private NoMatchResult toNoMatchResult(String firstName, String lastName, String birthDate) {
        return new NoMatchResult(new Person(firstName, lastName, birthDate));
    }

    private MatchUser toMatchUser(StatisticUser user) {
        return new MatchUser(user.getPerson(), user.getAddress(), 0.0, "");
    }
}
