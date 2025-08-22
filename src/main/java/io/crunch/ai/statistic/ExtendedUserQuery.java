package io.crunch.ai.statistic;

import dev.langchain4j.model.output.structured.Description;

public record ExtendedUserQuery(
        @Description("The user's first name") String firstName,
        @Description("The user's last name") String lastName,
        @Description("The user's birth date in yyyy-MM-dd format") String birthDate,
        @Description("Address country") String country,
        @Description("Address city") String city,
        @Description("Address postal code") String zipCode,
        @Description("Address street") String street,
        @Description("Address house number") String houseNumber,
        @Description("Highest similarity score value of the address") double similarityScore) {
}
