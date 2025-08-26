package io.crunch.ai.function.institute;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crunch.ai.function.OllamaTestResource;
import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;
import io.crunch.ai.function.statistic.StatisticUserService;
import io.crunch.ai.function.statistic.UserSearchResult;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
@QuarkusTestResource(OllamaTestResource.class)
class InstituteUserResourceTest {

    @InjectSpy
    StatisticUserService statisticUserService;

    @InjectSpy
    InstituteUserService instituteUserService;

    @InjectSpy
    SimilarityDistanceCalculator similarityDistanceCalculator;

    @Test
    void whenNoInstituteUserFoundReturnsError() {
        await()
            .atMost(1, MINUTES)
            .pollInterval(Durations.FIVE_SECONDS)
            .untilAsserted(() -> {
                given()
                    .param("firstName", "no-exist-first-name")
                    .param("lastName", "no-exist-lastname")
                    .param("birthDate", "1990-01-01").when()
                    .get("/users")
                    .then()
                    .statusCode(Response.Status.NOT_FOUND.getStatusCode());
                verify(statisticUserService, never()).searchUser(anyString(), anyString(), anyString());
            });
    }

    @Test
    void whenStatisticUserDoesNotExistThenReturnsNoneMatch() {
        await()
            .atMost(1, MINUTES)
            .pollInterval(Durations.FIVE_SECONDS)
            .untilAsserted(() -> {
                var response = given()
                    .param("firstName", "Alice")
                    .param("lastName", "Johnson")
                    .param("birthDate", "1990-05-21").when()
                    .get("/users")
                    .then()
                    .contentType(MediaType.APPLICATION_JSON)
                    .extract().response().body().asString();
                var result = new ObjectMapper().readValue(response, UserSearchResult.class);
                assertThat(result).isInstanceOf(UserSearchResult.class);
                assertThat(response).contains("Alice", "Johnson", "1990-05-21", "NONEMATCH");
                verify(statisticUserService, times(1)).searchUser("Alice", "Johnson", "1990-05-21");
                verify(similarityDistanceCalculator, never()).jaroWinklerSimilarity(any(), any());
            });
    }

    @Test
    void whenStatisticUserFoundThenReturnsExactMatch() {
        await()
            .atMost(1, MINUTES)
            .pollInterval(Durations.FIVE_SECONDS)
            .untilAsserted(() -> {
                var response = given()
                    .param("firstName", "Clara")
                    .param("lastName", "Meier")
                    .param("birthDate", "2000-07-21").when()
                    .get("/users")
                    .then()
                    .contentType(MediaType.APPLICATION_JSON)
                    .extract().response().body().asString();
                var result = new ObjectMapper().readValue(response, UserSearchResult.class);
                assertThat(result).isInstanceOf(UserSearchResult.class);
                assertThat(response).contains("Clara", "Meier", "2000-07-21", "EXACTMATCH", "EXT-1001");
                verify(statisticUserService, times(1)).searchUser("Clara", "Meier", "2000-07-21");
                verify(instituteUserService, never()).getUserAddress(any(Person.class));
                verify(similarityDistanceCalculator, never()).jaroWinklerSimilarity(any(), any());
            });
    }

    @Test
    void whenMultipleStatisticUserFoundThenReturnsSimilarMatches() {
        // 1982-04-08 lastName==Weber firstName==Peter
        await()
            .atMost(1, MINUTES)
            .pollInterval(Durations.FIVE_SECONDS)
            .untilAsserted(() -> {
                var response = given()
                    .param("firstName", "Peter")
                    .param("lastName", "Weber")
                    .param("birthDate", "1982-04-08").when()
                    .get("/users")
                    .then()
                    .contentType(MediaType.APPLICATION_JSON)
                    .extract().response().body().asString();
                var result = new ObjectMapper().readValue(response, UserSearchResult.class);
                assertThat(result).isInstanceOf(UserSearchResult.class);
                assertThat(response).contains("Peter", "Weber", "1982-04-08", "SIMILARMATCH");
                verify(statisticUserService, times(1)).searchUser("Peter", "Weber", "1982-04-08");
                verify(instituteUserService, times(1)).getUserAddress(new Person("Peter", "Weber", "1982-04-08"));
                verify(similarityDistanceCalculator, times(4)).jaroWinklerSimilarity(any(Address.class), any(Address.class));
            });
    }
}
