package io.crunch.ai.function.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Description("The user's address")
public record Address(
        @Column(name = "country") @Description("The user's address country") @JsonProperty(required = true) String country,
        @Column(name = "city") @Description("The user's address city") @JsonProperty(required = true) String city,
        @Column(name = "zip_code") @Description("The user's address zip code") @JsonProperty(required = true) String zipCode,
        @Column(name = "street") @Description("The user's address street") @JsonProperty(required = true) String street,
        @Column(name = "house_number") @Description("The user's address house number") @JsonProperty(required = true) String houseNumber) {
}
