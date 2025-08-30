package io.crunch.ai.function.institute;

import jakarta.ws.rs.QueryParam;

public record UserSearchQuery(
        @QueryParam("firstName") String firstName,
        @QueryParam("lastName") String lastName,
        @QueryParam("birthDate") String birthDate) {
}
