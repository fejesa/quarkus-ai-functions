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
        
        MANDATORY PARAMETERS:
        - `firstName`, `lastName`, and `birthDate` MUST always be provided from the Person input.
        - They MUST NOT be null, empty, invented, or altered.

        Search for a user and its external id in the statistic database based only on firstName, lastName, and birthDate.
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
    public UserSearchResult searchUser(UserSearchQuery userSearchQuery) {
        Log.info("Searching for user with query: " + userSearchQuery);
        if ("NoMatch".equals(userSearchQuery.firstName())) {
            // Simulate no match found
            return new NoMatchResult(new Person(userSearchQuery.firstName(), userSearchQuery.lastName(), userSearchQuery.birthDate()));
        } else if ("ExactMatch".equals(userSearchQuery.firstName())) {
            // Simulate exact match found
            return new ExactMatchResult("My-Ext-123",
                    new MatchUser(new Person(userSearchQuery.firstName(), userSearchQuery.lastName(), userSearchQuery.birthDate()),
                    new Address("Germany", "Berlin", "10115", "Street Name", new Random().nextInt(1, 100) + ""),
                    1.0, "exact match, same person"));
        } else {
            // Simulate similar matches found
            return new SimilarMatchesResult(List.of(
                    new MatchUser(new Person(userSearchQuery.firstName(), userSearchQuery.lastName(), userSearchQuery.birthDate()), new Address("Germany", "Hamburg", "20095", "Sample Str.", "10"), 0.0, ""),
                    new MatchUser(new Person(userSearchQuery.firstName(), userSearchQuery.lastName(), userSearchQuery.birthDate()), new Address("Germany", "Munich", "80331", "Another Str.", "11"), 0.0, "")
            ));
        }
    }
}
