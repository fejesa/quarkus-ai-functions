package io.crunch.ai.function.institute;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkiverse.langchain4j.scorer.junit5.AiScorer;
import io.quarkiverse.langchain4j.scorer.junit5.ScorerConfiguration;
import io.quarkiverse.langchain4j.testing.scorer.EvaluationSample;
import io.quarkiverse.langchain4j.testing.scorer.Parameter.NamedParameter;
import io.quarkiverse.langchain4j.testing.scorer.Samples;
import io.quarkiverse.langchain4j.testing.scorer.Scorer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.crunch.ai.function.statistic.UserSearchResultUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
@AiScorer
class UserSearchAssistantTest {

    @Inject
    UserSearchAssistant userSearchAssistant;

    @Test
    void scoreSearch(@ScorerConfiguration Scorer scorer) throws JsonProcessingException {
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
                .withExpectedOutput(
                    exactMatchAsString(matchUser(
                        person("Clara", "Meier", "2000-07-21"),
                        address("Germany", "Hamburg", "20095", "Sample Str.", "10"),
                        1.0, "No explanation available", "EXT-1001"))).build(),
            EvaluationSample.<String>builder()
                .withName("SimilarMatches")
                .withParameter(new NamedParameter("firstName", "Anna"))
                .withParameter(new NamedParameter("lastName", "Schmidt"))
                .withParameter(new NamedParameter("birthDate", "1993-09-12"))
                .withExpectedOutput(
                    similarMatchesAsString(List.of(
                        matchUser(
                            person("Anna", "Schmidt", "1993-09-12"),
                            address("Germany", "Berlin", "10117", "Mozart strasse", "78A"),
                            0.0, "", ""),
                        matchUser(
                            person("Anna", "Schmidt", "1993-09-12"),
                            address("Germany", "Potsdam", "10117", "Unter den Linden", "56"),
                            0.0, "", ""),
                        matchUser(
                            person("Anna", "Schmidt", "1993-09-12"),
                            address("Austria", "Vienna", "10117", "Unter den Linden", "13"),
                            0.0, "", "")))).build()
        );

        var report = scorer.evaluate(
                samples,
                parameters -> userSearchAssistant.search(new UserSearchQuery(parameters.get("firstName"), parameters.get("lastName"), parameters.get("birthDate"))),
                new UserSearchEvaluationStrategy());

        assertThat(report.score()).isGreaterThan(66.0);
    }
}
