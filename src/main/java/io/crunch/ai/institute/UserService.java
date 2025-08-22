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
        Your role is to search user information from the Statistic service based on the provided query.
        Rules:
        1. The query contains the first name, last name, and birth date of the user.
        2. You MUST respond by invoking the available TOOLS. You MUST NOT answer the query yourself.
        3. The ONLY valid response is a tool call. Do not invent or guess data.
        4. When multiple similar users are returned:
          a. First, call the tool `getUserAddress` with the original Person.
             - Capture its output as `original`.
             - This MUST be passed as the first parameter in every later call to `jaroWinklerSimilarity`.
          b. For each candidate user returned by the search, extract the candidate's address from the result.
             - Pass this candidate address as the second parameter `similar` to `jaroWinklerSimilarity`.
             - NEVER pass null or empty objects.
          c. You MUST NOT call `jaroWinklerSimilarity` until both `original` and a candidate's address are available.
            - Always set parameter 'original' = the output of getUserAddress(Person).
            - Always set parameter 'similar' = the candidate user’s address from the search result.
            - Never swap them.
        5. It is allowed to call multiple different tools in sequence to complete the task.
           - Do not call the same tool twice with the same input.
        6. Once all required tool calls are completed, return the final JSON result directly.
        7. Do not add any additional explanatory text, and do not remove any fields from the JSON object.
        8. Do not add fields like "message", "error", "code", "status", "result", "name" or any other keys.
        9. Return ONLY a JSON object, not surrounded by markdown, not with explanations.
       """)
    @ToolBox({StatisticUserService.class, InstituteUserService.class, SimilarityDistanceCalculator.class})
    String search(@UserMessage UserQuery query);
}
