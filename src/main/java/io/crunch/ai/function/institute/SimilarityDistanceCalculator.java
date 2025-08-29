package io.crunch.ai.function.institute;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.function.common.Address;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

/**
 * {@code SimilarityDistanceCalculator} provides functionality for computing similarity scores
 * between two {@link Address} objects using the Jaro-Winkler distance algorithm.
 *
 * <h2>Integration with LLMs</h2>
 * <p>
 * The {@link #jaroWinklerSimilarity(Address, Address)} method is exposed as a tool via
 * the {@link Tool} annotation, making it available for invocation by Large Language Models (LLMs)
 * through the LangChain4j function-calling mechanism.
 * </p>
 *
 * <h3>Key Rules for Usage</h3>
 * <ul>
 *   <li>The first parameter must always be the user's own address obtained from {@code getUserAddress(Person)}.</li>
 *   <li>The second parameter must always be a candidate address from search results.</li>
 *   <li>Each (original, candidate) pair must be compared only once; repeated comparisons are not allowed.</li>
 *   <li>Parameters must be passed as proper JSON objects, not stringified blobs.</li>
 *   <li>The output is a numeric similarity score in the range [0.0, 1.0], where {@code 1.0} indicates exact equality.</li>
 * </ul>
 *
 * <h2>Algorithm</h2>
 * <p>
 * The Jaro-Winkler similarity algorithm measures how similar two strings are.
 * The inputs are normalized (converted to uppercase, whitespace trimmed, concatenated fields)
 * before computing the similarity score.
 * </p>
 *
 * @see org.apache.commons.text.similarity.JaroWinklerSimilarity
 */
@ApplicationScoped
public class SimilarityDistanceCalculator {

    private static final JaroWinklerSimilarity SIMILARITY = new JaroWinklerSimilarity();

    @Tool(
        name = "jaroWinklerSimilarity",
        value = """
        Use this tool to compare two Address objects.
           The FIRST parameter MUST ALWAYS be the result of the getUserAddress(Person), called 'original'.
           The SECOND parameter MUST ALWAYS be the candidate address from the search result, called 'similar'.
           RULES:
           - Each (original, similar) pair must be compared ONLY ONCE.
           - Do not repeat comparisons for the same addresses.
           - Always provide it as a proper JSON object.
           - Never pass it as a stringified blob.
           Input: requires two separate Address objects:
             - 'original' (Address) → the address returned by getUserAddress(Person).
             - 'similar' (Address) → the address of a candidate user from the search results.
           Output: a numeric similarity score between 0.0 and 1.0, where 1.0 means identical.
       """
    )
    public double jaroWinklerSimilarity(@P(value = "The user's address", required = true) Address original,
                                        @P(value = "The address of a candidate user from the search results that should be compared", required = true) Address similar) {
        Log.info("Calculating the similarity between original address: " + original + " and similar address: " + similar);
        String normalized1 = normalize(original);
        String normalized2 = normalize(similar);
        var score = SIMILARITY.apply(normalized1, normalized2);
        Log.info("Calculated similarity score: " + score);
        return score;
    }

    /**
     * Normalizes an {@link Address} into a concatenated uppercase string representation.
     * <p>
     * Fields included in normalization:
     * <ul>
     *   <li>Country</li>
     *   <li>City</li>
     *   <li>Zip code</li>
     *   <li>Street</li>
     *   <li>House number</li>
     * </ul>
     * </p>
     * Null values are treated as empty strings.
     *
     * @param address the {@link Address} to normalize, may be {@code null}.
     * @return a normalized, uppercase concatenation of address fields, or an empty string if the address is null.
     */
    private String normalize(Address address) {
        if (address == null) {
            return "";
        }
        return String.join("",
                toUpper(address.country()),
                toUpper(address.city()),
                toUpper(address.zipCode()),
                toUpper(address.street()),
                toUpper(address.houseNumber())
        ).trim();
    }

    /**
     * Converts a string value to uppercase and trims whitespace.
     * Null values are safely converted to an empty string.
     *
     * @param value the input string, may be {@code null}.
     * @return the uppercase and trimmed string, or an empty string if the input is null.
     */
    private String toUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
