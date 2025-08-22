package io.crunch.ai.institute;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.crunch.ai.statistic.StatisticUserService;
import io.crunch.ai.statistic.UserQuery;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
public interface UserService {

    @SystemMessage(
            """
            GLOBAL RULES
            - You are NOT allowed to repeat tool calls with the same input.
            - If you cannot proceed because of missing data, STOP and return the last valid result.

            ABSOLUTE TERMINATION RULES
            - If the first tool `searchUserByFirstNameAndLastNameAndBirthDate` returns `"type": "NOMATCH"`, you MUST IMMEDIATELY return that result as the final output. Do NOT call any other tools. STOP IMMEDIATELY.
            - If the first tool returns `"type": "EXACTMATCH"`, you MUST IMMEDIATELY return that result as the final output. Do NOT call any other tools. STOP IMMEDIATELY.
            - Continuing beyond these cases is an error.

            Your role is to search user information from the Statistic service based on the provided query.
            Rules:
            1. The query contains the first name, last name, and birth date of the user.
            2. You MUST respond by invoking the available TOOLS. You MUST NOT answer the query yourself.
            3. The ONLY valid response is a tool call. Do not invent or guess data.
            4. Decision rules for handling search results:
               a. If the search returns NOMATCH users:
                  - Immediately return the search result JSON object.
                  - Do NOT call any other tools afterwards.
               b. If the search returns EXACTMATCH user:
                  - Immediately return the search result JSON object.
                  - Do NOT call any other tools afterwards.
            5. If the search returns SIMILARMATCH candidate users:
               a. First, call the tool `getUserAddress` with the original Person.
                  - Capture its output as `original`.
                  - This MUST be passed as the first parameter in every later call to `jaroWinklerSimilarity`.
               b. For each candidate user returned by the search, extract the candidate's address from the result.
                  - Pass this candidate address as the second parameter `similar` to `jaroWinklerSimilarity`.
                  - NEVER pass null or empty objects.
                  - Collect the similarity scores for all candidates.
               c. You MUST NOT call `jaroWinklerSimilarity` until both `original` and a candidate's address are available.
                  - Always set parameter 'original' = the output of getUserAddress(Person).
                  - Always set parameter 'similar' = the candidate user’s address from the search result.
                  - Never swap them.
               d. After all similarities are calculated:
                  - Identify the candidate with the HIGHEST similarity score.
                  - DO NOT recompute the score; reuse the numeric value from the corresponding `jaroWinklerSimilarity` result.
               e. Construct an ExtendedUserQuery object with ALL of the following:
                  - firstName, lastName, birthDate → from the original query (Person).
                  - country, city, zipCode, street, houseNumber → EXACTLY from the chosen candidate’s address.
                     → You MUST copy each field directly. None of them may be null or missing.
                  - similarityScore → the numeric score value returned by `jaroWinklerSimilarity` for the selected candidate.
                    → You MUST copy this number exactly, without rounding or modification.
               f. Call `extendedUserSearch` EXACTLY once with this ExtendedUserQuery.
                  - Ensure the ExtendedUserQuery JSON includes the candidate’s person, full address and the similarityScore fields.
                  - Do not return until `extendedUserSearch` has been executed.
            6. It is allowed to call multiple different tools in sequence to complete the task.
               - Do not call the same tool twice with the same input.
            7. Once all required tool calls are completed, return the final JSON result directly.
            8. Do not add any additional explanatory text, and do not remove any fields from the JSON object.
            9. Do not add fields like "message", "error", "code", "status", "result", "name" or any other keys.
            10. When invoking `extendedUserSearch`, the ExtendedUserQuery MUST look like this:
                {
                  "firstName": "<from original query>",
                  "lastName": "<from original query>",
                  "birthDate": "<from original query>",
                  "country": "<from candidate>",
                  "city": "<from candidate>",
                  "zipCode": "<from candidate>",
                  "street": "<from candidate>",
                  "houseNumber": "<from candidate>",
                  "similarityScore": <numeric value from jaroWinklerSimilarity>
                }
            11. Return ONLY a JSON object, not surrounded by markdown, not with explanations.
            """
    )
    @ToolBox({StatisticUserService.class, InstituteUserService.class, SimilarityDistanceCalculator.class})
    String search(@UserMessage UserQuery query);

}
