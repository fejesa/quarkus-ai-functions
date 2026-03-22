package io.crunch.ai.function.institute;

import io.crunch.ai.function.statistic.UserSearchResultUtil;
import io.quarkiverse.langchain4j.testing.evaluation.EvaluationResult;
import io.quarkiverse.langchain4j.testing.evaluation.EvaluationSample;
import io.quarkiverse.langchain4j.testing.evaluation.EvaluationStrategy;
import io.quarkus.logging.Log;

import java.io.IOException;

public class UserSearchEvaluationStrategy implements EvaluationStrategy<String> {

    @Override
    public EvaluationResult evaluate(EvaluationSample<String> sample, String output) {
        Log.info("Evaluating output: " + output);
        Log.info("Expected output: " + sample.expectedOutput());
        try {
            var expected = UserSearchResultUtil.fromString(sample.expectedOutput());
            var result = UserSearchResultUtil.fromString(output);
            return EvaluationResult.fromBoolean(expected.equals(result));
        } catch (IOException e) {
            Log.error("Failed to parse JSON", e);
            return EvaluationResult.fromBoolean(false);
        }
    }
}
