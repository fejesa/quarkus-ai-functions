package io.crunch.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

public record Person(
        @Description("The user's first name") @JsonProperty(required = true) String firstName,
        @Description("The user's last name") @JsonProperty(required = true) String lastName,
        @Description("The user's birth date") @JsonProperty(required = true) String birthDate) {
}
