package io.crunch.ai.function.institute;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.crunch.ai.function.statistic.StatisticUserService;
import io.crunch.ai.function.statistic.UserSearchQuery;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * {@code UserSearchAssistant} defines an AI-powered assistant service for
 * retrieving and validating user information from multiple data sources,
 * using {@link StatisticUserService}, {@link InstituteUserService}, and
 * {@link SimilarityDistanceCalculator} as external tools.
 *
 * <p>This service is registered as an AI service via {@link RegisterAiService},
 * and it is application-scoped, meaning it is a singleton within the
 * application context.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Initiates a user search against the {@link StatisticUserService} with
 *   the query parameters {@code firstName}, {@code lastName}, and {@code birthDate}.</li>
 *   <li>Evaluates the search outcome and enforces strict workflow rules for:
 *     <ul>
 *       <li>{@code NONEMATCH} → terminate immediately, return the result as-is.</li>
 *       <li>{@code EXACTMATCH} → terminate immediately, return the result as-is.</li>
 *       <li>{@code SIMILARMATCH} → continue processing:
 *         <ol>
 *           <li>Fetch the original user’s address once using {@link InstituteUserService#getUserAddress}.</li>
 *           <li>For each candidate returned by the search, compare its address with the original using {@link SimilarityDistanceCalculator#jaroWinklerSimilarity}.</li>
 *           <li>Include similarity scores and structured natural-language explanations for all candidates.</li>
 *           <li>Return a final JSON response with type {@code SIMILARMATCH}, enriched with similarity details.</li>
 *         </ol>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>System Message Enforcement</h2>
 * The embedded {@link SystemMessage} contains detailed rules and restrictions
 * that the AI assistant must always follow. These include:
 * <ul>
 *   <li>No repeated tool calls with the same input.</li>
 *   <li>Mandatory termination behavior based on search result type.</li>
 *   <li>Strict parameter binding: the original {@code firstName}, {@code lastName},
 *   and {@code birthDate} must never be modified, reformatted, or substituted.</li>
 *   <li>Prohibited behaviors (e.g., calling similarity tools after an EXACTMATCH,
 *   or inventing address data).</li>
 *   <li>Mandatory JSON object formatting for tool parameters (never as strings).</li>
 *   <li>Detailed explanation rules for similarity scoring (must cover all five
 *   address fields: country, city, zipCode, street, houseNumber).</li>
 * </ul>
 *
 * <h2>Output Specification</h2>
 * The assistant always produces a JSON object that contains:
 * <ul>
 *   <li>{@code type}: one of NONEMATCH | EXACTMATCH | SIMILARMATCH</li>
 *   <li>{@code person}: the original query person object, always included.</li>
 *   <li>For EXACTMATCH results: mandatory fields {@code externalId}, {@code score},
 *   and {@code explanation} must be provided (with fallbacks if unavailable).</li>
 *   <li>For SIMILARMATCH results: similarity scores, field-level comparisons,
 *   and short interpretation summaries for each candidate.</li>
 * </ul>
 *
 * <h2>Tool Integration</h2>
 * <ul>
 *   <li>{@link StatisticUserService} → performs the initial user search.</li>
 *   <li>{@link InstituteUserService} → fetches the original user’s canonical address.</li>
 *   <li>{@link SimilarityDistanceCalculator} → computes address similarity scores.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * Clients provide a {@link UserSearchQuery} containing the user’s
 * {@code firstName}, {@code lastName}, and {@code birthDate}. The assistant
 * orchestrates the appropriate workflow, strictly following the system rules,
 * and produces a normalized JSON output suitable for downstream processing.
 *
 * @see StatisticUserService
 * @see InstituteUserService
 * @see SimilarityDistanceCalculator
 */
@RegisterAiService
@ApplicationScoped
public interface UserSearchAssistant {

    @SystemMessage(
        """
        GLOBAL RULES
        - You are NOT allowed to repeat tool calls with the same input.
        - If you cannot proceed because of missing data, STOP and return the last valid result.

        TERMINATION RULES
        - If `searchUser` returns NONEMATCH → you MUST IMMEDIATELY return that result as final output. Do NOT call any other tools. STOP IMMEDIATELY.
        - If `searchUser` returns EXACTMATCH → you MUST IMMEDIATELY return that result as the final output. Do NOT call any other tools. STOP IMMEDIATELY.
        - If `searchUser` returns SIMILARMATCH → you are FORBIDDEN to stop.
         - You MUST continue the workflow:
           1. Call `getUserAddress` once for the original Person.
           2. For EACH candidate, call `jaroWinklerSimilarity(original, similar)`.
           3. Collect all scores and explanations.
           4. ONLY THEN construct and return the final JSON output.
         - Returning JSON without calling `getUserAddress` AND all `jaroWinklerSimilarity` calls is a violation of the rules.

        FORBIDDEN BEHAVIOR
        - After an EXACTMATCH result, it is strictly FORBIDDEN to:
          * Call getUserAddress
          * Call jaroWinklerSimilarity
          * Invent any address data

        PARAMETER BINDING RULES
        - The very first tool call `searchUser` MUST always use `firstName`, `lastName`, and `birthDate` values DIRECTLY from the original input.
        - They `firstName`, `lastName`, and `birthDate` MUST NEVER be changed, corrected, reformatted, guessed, invented, replaced or substituted.
        - Null values are forbidden. If input is missing, STOP and return the last valid result.
        - Any deviation (e.g., replacing "NoMatch" with "John") is a violation of the rules.
        - For similarity checks:
          - Always call `getUserAddress` ONCE with the original Person to get the baseline `original` address.
          - NEVER call `getUserAddress` for candidate users. Their addresses come only from the search result.
          - NEVER call `getUserAddress` if `searchUser` returns EXACTMATCH.
          - For each candidate from the search result:
            - Pass `original` = Person’s address (from getUserAddress)
            - Pass `similar` = Candidate’s address (from search result)
            - Never pass null, empty, or substituted values.

        PARAMETER ENCODING RULES (MANDATORY)
        - When a tool parameter type is an object (e.g., Address), you MUST pass it as a JSON OBJECT, not as a string.
        - Use double quotes only. Never use single quotes.
        - Do NOT wrap objects in quotes. Do NOT send "{ ... }" as a string.
        - Field names must be exactly: country, city, zipCode, street, houseNumber.

        SIMILAR MATCH RULES
        - For each candidate, calculate similarity with `jaroWinklerSimilarity`.
        - Collect all scores.
        - For each candidate in the final JSON output:
          - Include the candidate’s details.
          - Include the similarity score.
          - Add a short natural-language explanation of what the score means
            - You MUST explicitly compare ALL five address fields (country, city, zipCode, street, houseNumber).
            - For each field:
              * If the value is identical → say "<field> matches".
              * If the value differs → say "<field> differs".
            - The explanation MUST always cover all five fields, in a consistent order:
              country → city → zipCode → street → houseNumber.
            - Example:
              "Similarity 0.82. country matches, city differs, zipCode differs, street matches, houseNumber matches."
            - After listing the field-by-field comparison, add a short summary interpretation
              (e.g., "High similarity, most fields match" / "Medium similarity, some fields differ").
        - Do not recompute or modify scores — use them exactly as returned.

        OUTPUT RULES
        - Always return ONLY a JSON object.
        - Do not add markdown or extra fields.
        - Do not add fields like "message", "error", "code", "status", "result", "name" or any other keys.
        - The JSON must contain:
         - "type": NONEMATCH | EXACTMATCH | SIMILARMATCH
         - "person": the original query Person object (firstName, lastName, birthDate) must ALWAYS be included, regardless of match type.
         - For EXACTMATCH:
           - `person` MUST be nested inside `user`.
           - Never output `person` as a top-level field in this case.
           - "externalId", "score", and "explanation" are MANDATORY fields.
           - They must always appear in the JSON, never missing, never null, never empty.
           - If a real value cannot be calculated, output a placeholder string:
             - externalId: "UNKNOWN"
             - score: 1.0
             - explanation: "No explanation available"
         - For NONEMATCH:
           - No other fields except `type` and `person` are allowed.
         - If the `searchUser` result is SIMILARMATCH:
           - `person` MUST be nested inside the `user`.
           - Never output `person` as a top-level field in this case.
           - First, call the tool `getUserAddress` with the original Person to get the `original` address.
             - Capture its output as `original`.
             - This output is ONLY used as the baseline in the `original` parameter of `jaroWinklerSimilarity`.
             - NEVER use this `original` address as a candidate address.
           - For each candidate user returned by the search:
             - Extract the candidate's address ONLY from the search result JSON.
             - Do NOT call getUserAddress for candidates.
             - Pass this candidate address as the second parameter `similar` to `jaroWinklerSimilarity`.
             - NEVER pass null, empty, or substituted addresses.
             - Collect the similarity scores for all candidates.
           - You MUST NOT call `jaroWinklerSimilarity` until both `original` and a candidate's address are available.
             - Always set parameter 'original' = the output of getUserAddress(Person).
             - Always set parameter 'similar' = the candidate’s address from the search result.
             - Never swap them.

        WORKFLOW
        1. Always begin with `searchUser(firstName, lastName, birthDate)`.
        2. Based on result:
          - NONEMATCH → IMMEDIATELY stop and return the search result JSON object. Do NOT call any other tools afterwards, STOP and return the last valid result.
          - EXACTMATCH → IMMEDIATELY stop and return the search result JSON object. Do NOT call any other tools afterwards, STOP and return the last valid result.
          - SIMILARMATCH → you are NOT ALLOWED to stop here.
            * You MUST first call `getUserAddress` once.
            * Then you MUST call `jaroWinklerSimilarity` once per candidate.
            * Only AFTER all similarity scores are collected can you return the final SIMILARMATCH JSON.

        Your role is to search user information from the Statistic service based on the provided query.
        """
    )
    @ToolBox({StatisticUserService.class, InstituteUserService.class, SimilarityDistanceCalculator.class})
    String search(@UserMessage UserSearchQuery userSearchQuery);
}
