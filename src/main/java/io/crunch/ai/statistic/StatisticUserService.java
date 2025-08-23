package io.crunch.ai.statistic;

import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Random;

@ApplicationScoped
public class StatisticUserService {

    @Tool(name = "searchUser", value = {
        """
        THIS TOOL IS ALWAYS THE FIRST TOOL TO CALL.
        - If the result type is NOMATCH or EXACTMATCH → YOU MUST RETURN IT IMMEDIATELY AS FINAL OUTPUT. DO NOT call any other tools afterwards.
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
           "type": "NOMATCH",
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
        if ("NoMatch".equals(firstName)) {
            // Simulate no match found
            return new NoMatchResult(new Person(firstName, lastName, birthDate));
        } else if ("ExactMatch".equals(firstName)) {
            // Simulate exact match found
            return new ExactMatchResult("My-Ext-123",
                    new MatchUser(new Person(firstName, lastName, birthDate),
                    new Address("Germany", "Berlin", "10115", "Street Name", new Random().nextInt(1, 100) + ""),
                    1.0, "exact match, same person"));
        } else {
            // Simulate similar matches found
            return new SimilarMatchesResult(List.of(
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Hamburg", "20095", "Sample Str.", "10"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Munich", "80331", "Another Str.", "11"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Cologne", "50667", "Different Str.", "12"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Frankfurt", "60311", "Other Str.", "13"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Stuttgart", "70173", "New Str.", "14"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Düsseldorf", "40213", "Old Str.", "15"), 0.0, ""),
                    new MatchUser(new Person(firstName, lastName, birthDate), new Address("Germany", "Dortmund", "44135", "Main Str.", "16"), 0.0, "")
            ));
        }
    }
}
