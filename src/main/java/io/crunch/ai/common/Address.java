package io.crunch.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

public record Address(
        @Description("The user's address country") @JsonProperty(required = true) String country,
        @Description("The user's address city") @JsonProperty(required = true) String city,
        @Description("The user's address zip code") @JsonProperty(required = true) String zipCode,
        @Description("The user's address street") @JsonProperty(required = true) String street,
        @Description("The user's address house number") @JsonProperty(required = true) String houseNumber) {
}
