package io.crunch.ai.statistic;

import jakarta.ws.rs.QueryParam;

public record UserQuery(
        @QueryParam("firstName") String firstName,
        @QueryParam("lastName") String lastName,
        @QueryParam("birthDate") String birthDate) {
}
