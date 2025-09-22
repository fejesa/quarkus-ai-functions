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
    void scoreExactMatch(@ScorerConfiguration Scorer scorer) throws JsonProcessingException {
        var samples = new Samples<>(
            EvaluationSample.<String>builder()
                .withName("ExactMatch")
                .withParameter(new NamedParameter("firstName", "Clara"))
                .withParameter(new NamedParameter("lastName", "Meier"))
                .withParameter(new NamedParameter("birthDate", "2000-07-21"))
                .withExpectedOutput(
                    exactMatchAsString(matchUser(
                        person("Clara", "Meier", "2000-07-21"),
                        address("Germany", "Hamburg", "20095", "Sample Str.", "10"),
                        1.0, "No explanation available", "EXT-1001"))).build()
        );

        score(scorer, samples, 99.0);
    }

    @Test
    void scoreNoneMatch(@ScorerConfiguration Scorer scorer) throws JsonProcessingException {
        var samples = new Samples<>(
            EvaluationSample.<String>builder()
                .withName("NoneMatch")
                .withParameter(new NamedParameter("firstName", "Alice"))
                .withParameter(new NamedParameter("lastName", "Johnson"))
                .withParameter(new NamedParameter("birthDate", "1990-05-21"))
                .withExpectedOutput(
                        nonMatchAsString(person("Alice", "Johnson", "1990-05-21"))).build()
        );

        score(scorer, samples, 99.0);
    }

    @Test
    void scoreSimilarMatch(@ScorerConfiguration Scorer scorer) throws JsonProcessingException {
        var samples = new Samples<>(
            EvaluationSample.<String>builder()
                .withName("SimilarMatches")
                .withParameter(new NamedParameter("firstName", "Peter"))
                .withParameter(new NamedParameter("lastName", "Weber"))
                .withParameter(new NamedParameter("birthDate", "1982-04-08"))
                .withExpectedOutput(
                    similarMatchesAsString(List.of(
                        matchUser(
                            person("Peter", "Weber", "1982-04-08"),
                            address("Germany", "Munich", "80332", "Sendlinger Strasse", "56"),
                            0.0, "", ""),
                        matchUser(
                            person("Peter", "Weber", "1982-04-08"),
                            address("Germany", "Munich", "80331", "Sendlinger Strase", "12A"),
                            0.0, "", ""),
                        matchUser(
                            person("Peter", "Weber", "1982-04-08"),
                            address("Austria", "Salzburg", "80331", "Sendlinger Strasse", "12"),
                            0.0, "", ""),
                        matchUser(
                            person("Peter", "Weber", "1982-04-08"),
                            address("Germany", "Augsburg", "80331", "Sendlinger Strasse", "11"),
                            0.0, "", "")))).build()
        );

        score(scorer, samples, 99.0);
    }

    private void score(Scorer scorer, Samples<String> samples, double threshold) {
        var report = scorer.evaluate(
                samples,
                parameters -> userSearchAssistant.search(new UserSearchQuery(parameters.get("firstName"), parameters.get("lastName"), parameters.get("birthDate"))),
                new UserSearchEvaluationStrategy());

        assertThat(report.score()).isGreaterThan(threshold);
    }
}
