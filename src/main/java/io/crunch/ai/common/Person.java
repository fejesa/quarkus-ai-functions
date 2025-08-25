package io.crunch.ai.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Person(
        @Column(name = "first_name", nullable = false) @Description("The user's first name") @JsonProperty(required = true) String firstName,
        @Column(name = "last_name", nullable = false) @Description("The user's last name") @JsonProperty(required = true) String lastName,
        @Column(name = "birth_date", nullable = false) @Description("The user's birth date") @JsonProperty(required = true) String birthDate) {
}
