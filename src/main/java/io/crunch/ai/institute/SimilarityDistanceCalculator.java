package io.crunch.ai.institute;

import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.common.Address;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

@ApplicationScoped
public class SimilarityDistanceCalculator {

    private static final JaroWinklerSimilarity SIMILARITY = new JaroWinklerSimilarity();

    @Tool(
        name = "jaroWinklerSimilarity",
        value = """
           Use this tool to compare two Address objects.
           The FIRST parameter MUST ALWAYS be the result of the getUserAddress(Person), called 'original'.
           The SECOND parameter MUST ALWAYS be the candidate address from the search result, called 'similar'.
           RULE: Each (original, similar) pair must be compared ONLY ONCE. Do not repeat comparisons for the same addresses.
           Input:
             - 'original' (Address) → the address returned by getUserAddress(Person).
             - 'similar' (Address) → the address of a candidate user from the search results.
           Output: a numeric similarity score between 0.0 and 1.0, where 1.0 means identical.
       """
    )
    public double jaroWinklerSimilarity(Address original, Address similar) {
        Log.info("Calculating the similarity between original address: " + original + " and similar address: " + similar);
        String normalized1 = normalize(original);
        String normalized2 = normalize(similar);
        var score = SIMILARITY.apply(normalized1, normalized2);
        Log.info("Calculated similarity score: " + score);
        return score;
    }

    private String normalize(Address address) {
        if (address == null) {
            return "";
        }
        return String.join(" ",
                safeUpper(address.country()),
                safeUpper(address.city()),
                safeUpper(address.zipCode()),
                safeUpper(address.street()),
                safeUpper(address.houseNumber())
        ).trim();
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
