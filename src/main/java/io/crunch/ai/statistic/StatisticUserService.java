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

    @Tool(name = "searchUserByFirstNameAndLastNameAndBirthDate", value = {
        """
        THIS TOOL IS ALWAYS THE FIRST TOOL TO CALL.
        - If the result type is NOMATCH or EXACTMATCH → YOU MUST RETURN IT IMMEDIATELY AS FINAL OUTPUT. DO NOT call any other tools afterwards.
        - You must call this tool exactly once at the start of processing.
        - Never skip it.
        - Never replace it with extendedUserSearch.

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
             }
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
               }
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
               }
             }
           ]
         }
     """
    })
    public UserSearchResult searchUserByFirstNameAndLastNameAndBirthDate(UserQuery query) {
        Log.info("Searching for user with simple query: " + query);
        if ("NoMatch".equals(query.firstName())) {
            // Simulate no match found
            return new NoMatchResult(new Person(query.firstName(), query.lastName(), query.birthDate()));
        } else if ("ExactMatch".equals(query.firstName())) {
            // Simulate exact match found
            return new ExactMatchResult("My-Ext-123",
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()),
                    new Address("Germany", "Berlin", "10115", "Street Name", new Random().nextInt(1, 100) + "")
                    ), 0.0);
        } else {
            // Simulate similar matches found
            return new SimilarMatchesResult(List.of(
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()), new Address("Germany", "Hamburg", "20095", "Sample Str.", "10")),
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()), new Address("Germany", "Munich", "80331", "Another Str.", "11"))
            ));
        }

    }

    @Tool(name = "extendedUserSearch",
          value = """
        Use this tool to perform an extended user search in the Statistic database based both person and address data.
        Always return a valid JSON object. Do not add any additional explanatory text, and do not remove any fields from the JSON object.

        WHEN TO USE:
        - Never as the first tool.
        - After comparing addresses with `jaroWinklerSimilarity`,
          select the single candidate user with the HIGHEST similarity score.
        - Do not call this tool multiple times. Call it EXACTLY once with the best candidate.

        INPUT:
        - ExtendedUserQuery object with the following fields:
            * firstName (string)   → from the original Person
            * lastName (string)    → from the original Person
            * birthDate (string)   → from the original Person
            * country (string)     → from the selected candidate’s Address (never null)
            * city (string)        → from the selected candidate’s Address (never null)
            * zipCode (string)     → from the selected candidate’s Address (never null)
            * street (string)      → from the selected candidate’s Address (never null)
            * houseNumber (string) → from the selected candidate’s Address (never null)
            * similarityScore (number) → the similarity score of the selected candidate

        OUTPUT:
        - Always return a JSON object with one of the following cases:
            {
              "type": "EXACTMATCH",
              "externalId": "<externalId>",
              "score": <similarityScore>,
              "user": {
                "person": {
                  "firstName": "<firstName>",
                  "lastName": "<lastName>",
                  "birthDate": "<birthDate>"
                },
                "address": {
                  "country": "<country>",
                  "city": "<city>",
                  "zipCode": "<zipCode>",
                  "street": "<street>",
                  "houseNumber": "<houseNumber>"
                }
              }
            }

        RULES:
        - Copy all address fields directly from the chosen candidate.
        - Never invent or leave fields empty.
        - Always include the `similarityScore` field with the exact numeric value from the calculation.
        - Never call this tool before all similarity scores are calculated.
        - Never call this tool more than once.
    """
    )
    public UserSearchResult extendedSearch(ExtendedUserQuery query) {
        Log.info("Searching for user with extended query: " + query);
        // Simulate extended search logic
        return new ExactMatchResult("My-Ext-ABCD",
                new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()),
                        new Address(query.country(), query.city(), query.zipCode(), query.street(), query.houseNumber())
                ), query.similarityScore());
    }
}
