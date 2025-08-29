package io.crunch.ai.function.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Represents the immutable postal address of a user, including country, city,
 * zip code, street, and house number.
 * <p>
 * This record is designed for both:
 * <ul>
 *     <li><b>Domain and persistence model:</b> As an {@link Embeddable},
 *     it can be embedded into JPA entities (e.g., {@code StatisticUser}, {@code InstituteUser}),
 *     providing consistent storage of user addresses across multiple database tables.</li>
 *
 *     <li><b>AI/LLM integration:</b> The {@link Description} and {@link JsonProperty} annotations
 *     ensure structured and validated data exchange between LangChain4j models and JSON serialization.
 *     Models are instructed to always include all fields, never omit or invent values, and return the
 *     address strictly as a JSON object (not a string).</li>
 * </ul>
 *
 * <h2>Usage Rules (AI Integration)</h2>
 * <ul>
 *     <li>All fields—{@code country}, {@code city}, {@code zipCode}, {@code street}, and {@code houseNumber}—
 *     are <b>mandatory</b> and must be provided in full.</li>
 *     <li>Values must come directly from either the database (original user) or the candidate user result set.
 *     They must never be replaced, dropped, or substituted by the model.</li>
 *     <li>When performing similarity checks, this record is typically used alongside
 *     the {@link Person} record as the {@code original} versus {@code candidate} address.</li>
 * </ul>
 *
 * <h2>Serialization</h2>
 * <ul>
 *     <li>All fields are marked {@link JsonProperty#required()} to guarantee complete serialization/deserialization.</li>
 *     <li>Example JSON representation:
 *     <pre>{@code
 *     {
 *       "country": "Germany",
 *       "city": "Berlin",
 *       "zipCode": "10115",
 *       "street": "Invalidenstraße",
 *       "houseNumber": "44"
 *     }
 *     }</pre>
 *     </li>
 * </ul>
 *
 * <h2>Example in AI Tool Flow</h2>
 * <pre>
 * // Original vs candidate address comparison
 * {
 *   "original": {
 *     "country": "Germany",
 *     "city": "Berlin",
 *     "zipCode": "10115",
 *     "street": "Invalidenstraße",
 *     "houseNumber": "44"
 *   },
 *   "candidate": {
 *     "country": "Germany",
 *     "city": "Berlin",
 *     "zipCode": "10117",
 *     "street": "Chausseestraße",
 *     "houseNumber": "15"
 *   }
 * }
 * </pre>
 *
 * @param country     the country of the address (never {@code null} or empty).
 * @param city        the city of the address (never {@code null} or empty).
 * @param zipCode     the postal/ZIP code (never {@code null} or empty).
 * @param street      the street name (never {@code null} or empty).
 * @param houseNumber the house or building number (never {@code null} or empty).
 */
@Embeddable
@Description("The user's address")
public record Address(
        @Column(name = "country")
        @Description("The user's address country")
        @JsonProperty(required = true)
        String country,

        @Column(name = "city")
        @Description("The user's address city")
        @JsonProperty(required = true)
        String city,

        @Column(name = "zip_code")
        @Description("The user's address zip code")
        @JsonProperty(required = true)
        String zipCode,

        @Column(name = "street")
        @Description("The user's address street")
        @JsonProperty(required = true)
        String street,

        @Column(name = "house_number")
        @Description("The user's address house number")
        @JsonProperty(required = true)
        String houseNumber
) { }
