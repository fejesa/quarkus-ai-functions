package io.crunch.ai.function.institute;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crunch.ai.function.statistic.UserSearchQuery;
import io.quarkiverse.langchain4j.scorer.junit5.AiScorer;
import io.quarkiverse.langchain4j.scorer.junit5.ScorerConfiguration;
import io.quarkiverse.langchain4j.testing.scorer.EvaluationSample;
import io.quarkiverse.langchain4j.testing.scorer.Parameter.NamedParameter;
import io.quarkiverse.langchain4j.testing.scorer.Samples;
import io.quarkiverse.langchain4j.testing.scorer.Scorer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.crunch.ai.function.statistic.UserSearchResultUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
@AiScorer
class UserSearchAssistantTest {

    @Inject
    UserSearchAssistant userSearchAssistant;

    @Test
    void scoreSearch(@ScorerConfiguration(concurrency = 2) Scorer scorer) throws JsonProcessingException {
        Samples<String> samples = new Samples<>(
            EvaluationSample.<String>builder()
                .withName("NoneMatch")
                .withParameter(new NamedParameter("firstName", "Alice"))
                .withParameter(new NamedParameter("lastName", "Johnson"))
                .withParameter(new NamedParameter("birthDate", "1990-05-21"))
                .withExpectedOutput(
                        nonMatchAsString(person("Alice", "Johnson", "1990-05-21"))).build(),
            EvaluationSample.<String>builder()
                .withName("ExactMatch")
                .withParameter(new NamedParameter("firstName", "Clara"))
                .withParameter(new NamedParameter("lastName", "Meier"))
                .withParameter(new NamedParameter("birthDate", "2000-07-21"))
                .withParameter(new NamedParameter("country", "Germany"))
                .withParameter(new NamedParameter("city", "Hamburg"))
                .withParameter(new NamedParameter("zipCode", "20095"))
                .withParameter(new NamedParameter("street", "Sample Str."))
                .withParameter(new NamedParameter("houseNumber", "10"))
                .withParameter(new NamedParameter("externalId", "EXT-1001"))
                .withParameter(new NamedParameter("explanation", ""))
                .withExpectedOutput(
                    exactMatchAsString(matchUser(
                            person("Clara", "Meier", "2000-07-21"),
                            address("Germany", "Hamburg", "20095", "Sample Str.", "10"),
                            1.0, "No explanation available", "EXT-1001"))).build()
        );

        var strategy = new UserSearchEvaluationStrategy();
        var report = scorer.evaluate(
                samples,
                parameters -> userSearchAssistant.search(new UserSearchQuery(parameters.get("firstName"), parameters.get("lastName"), parameters.get("birthDate"))),
                strategy);

        assertThat(report.score()).isGreaterThan(99.0);
    }
}
