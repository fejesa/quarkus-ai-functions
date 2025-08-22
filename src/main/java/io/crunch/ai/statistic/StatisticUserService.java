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
           "type": "EXACT",
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
           "type": "SIMILAR",
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
    public UserSearchResult simpleSearch(UserQuery query) {
        Log.info("Searching for user with simple query: " + query);
        if ("NoMatch".equals(query.firstName())) {
            // Simulate no match found
            return new NoMatchResult(new Person(query.firstName(), query.lastName(), query.birthDate()));
        } else if ("ExactMatch".equals(query.firstName())) {
            // Simulate exact match found
            return new ExactMatchResult("My-Ext-123",
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()),
                    new Address("Germany", "Berlin", "10115", "Street Name", new Random().nextInt(1, 100) + "")
                    ));
        } else {
            // Simulate similar matches found
            return new SimilarMatchesResult(List.of(
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()), new Address("Germany", "Hamburg", "20095", "Sample Str.", "10")),
                    new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()), new Address("Germany", "Munich", "80331", "Another Str.", "11"))
            ));
        }

    }

    @Tool(name = "extendedUserSearch", value = {
            """
            Search for a user and its external id in the statistic database based on person and address data.
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
               "type": "EXACT",
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
         """
    })
    public UserSearchResult extendedSearch(ExtendedUserQuery query) {
        Log.info("Searching for user with extended query: " + query);
        // Simulate extended search logic
        return new ExactMatchResult("My-Ext-ABCD",
                new MatchUser(new Person(query.firstName(), query.lastName(), query.birthDate()),
                        new Address(query.country(), query.city(), query.zipCode(), query.street(), query.houseNumber())
                ));
    }
}
