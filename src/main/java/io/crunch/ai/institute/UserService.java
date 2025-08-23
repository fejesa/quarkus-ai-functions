package io.crunch.ai.institute;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.crunch.ai.statistic.StatisticUserService;
import io.crunch.ai.statistic.UserSearchQuery;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
public interface UserService {

    @SystemMessage(
            """
            GLOBAL RULES
            - You are NOT allowed to repeat tool calls with the same input.
            - If you cannot proceed because of missing data, STOP and return the last valid result.

            TERMINATION RULES
            - If the first tool `searchUser` returns NOMATCH, IMMEDIATELY return that result as the final output. Do NOT call any other tools.
            - If the first tool returns EXACTMATCH, IMMEDIATELY return that result as the final output. Do NOT call any other tools.
            - Continuing beyond these cases is an error.

            PARAMETER RULES
            - The first tool call MUST always use `firstName`, `lastName`, and `birthDate` DIRECTLY from the Person input.
              - These three parameters are MANDATORY.
              - They MUST NEVER be null, empty, omitted, or substituted.
              - They MUST match the input exactly, character-for-character.
            - For similarity checks:
              - Always call `getUserAddress` ONCE with the original Person to get the baseline `original` address.
              - NEVER call `getUserAddress` for candidate users. Their addresses come only from the search result.
              - For each candidate from the search result:
                - Pass `original` = Person’s address (from getUserAddress)
                - Pass `similar` = Candidate’s address (from search result)
                - Never pass null, empty, or substituted values.

            SIMILAR MATCH RULES
            - For each candidate, calculate similarity with `jaroWinklerSimilarity`.
            - Collect all scores.
            - For each candidate in the final JSON output:
              - Include the candidate’s details.
              - Include the similarity score.
              - Add a short natural-language explanation of what the score means
                (e.g., "high similarity, likely same person", "medium similarity, partial match", "low similarity, unlikely match").
            - Do not recompute or modify scores — use them exactly as returned.

            OUTPUT RULES
            - Always return ONLY a JSON object.
            - Do not add markdown or extra fields.
            - Do not add fields like "message", "error", "code", "status", "result", "name" or any other keys.
            - The JSON must contain:
             - "type": NOMATCH | EXACTMATCH | SIMILARMATCH
             - "person": the original query Person object (firstName, lastName, birthDate) must ALWAYS be included, regardless of match type.
             - For EXACTMATCH:
               - `person` MUST be nested inside `user`.
               - Never output `person` as a top-level field in this case.
             - If SIMILARMATCH: a list of candidates, each with fields:
               - candidate user details
               - similarityScore
               - explanation

            Your role is to orchestrate the tool calls correctly and return the structured JSON as described.
            """
    )
    @ToolBox({StatisticUserService.class, InstituteUserService.class, SimilarityDistanceCalculator.class})
    String search(@UserMessage UserSearchQuery userSearchQuery);

}
