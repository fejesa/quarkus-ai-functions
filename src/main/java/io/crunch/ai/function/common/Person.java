package io.crunch.ai.function.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Represents immutable personal information of a user, including
 * first name, last name, and birth date.
 * <p>
 * This record serves a dual purpose:
 * <ul>
 *     <li><b>Domain and persistence model:</b> Annotated with {@link Embeddable},
 *     it is designed to be embedded within JPA entities (e.g., {@code StatisticUser}, {@code InstituteUser}),
 *     ensuring that personal attributes are stored consistently across different tables.</li>
 *
 *     <li><b>AI/LLM integration:</b> The {@link Description} and {@link JsonProperty} annotations
 *     provide structured metadata for LangChain4j models and JSON serialization/deserialization,
 *     ensuring that the model receives and produces strictly defined fields
 *     without omission, invention, or modification.</li>
 * </ul>
 *
 * <h2>Usage Rules (AI Integration)</h2>
 * <ul>
 *     <li>All three fields—{@code firstName}, {@code lastName}, and {@code birthDate}—are
 *     <b>mandatory</b> and must always be provided with exact values from the original query input.</li>
 *     <li>Values must never be normalized, invented, substituted, or corrected by the model.
 *     Even if the input looks invalid (e.g., "NoMatch"), it must be passed unchanged.</li>
 *     <li>This record is typically used as the input key when searching users
 *     in the <i>StatisticUserService</i> or <i>InstituteUserService</i>.</li>
 * </ul>
 *
 * <h2>Serialization</h2>
 * <ul>
 *     <li>When serialized to JSON (e.g., in API requests/responses or LLM tool calls),
 *     all fields are required due to {@link JsonProperty#required()}.</li>
 *     <li>Example JSON representation:
 *     <pre>{@code
 *     {
 *       "firstName": "John",
 *       "lastName": "Doe",
 *       "birthDate": "1985-03-25"
 *     }
 *     }</pre>
 *     </li>
 * </ul>
 *
 * <h2>Example in AI Tool Invocation</h2>
 * <pre>
 * // Example usage in searchUser tool input:
 * {
 *   "firstName": "Alice",
 *   "lastName": "Smith",
 *   "birthDate": "1990-07-12"
 * }
 * </pre>
 *
 * @param firstName the user's first name (never {@code null} or empty).
 * @param lastName the user's last name (never {@code null} or empty).
 * @param birthDate the user's birth date (format as provided, never {@code null} or empty).
 */
@Description("The user's personal information, for example, first name, last name, and birth date")
@Embeddable
public record Person(
        @Column(name = "first_name", nullable = false)
        @Description("The user's first name")
        @JsonProperty(required = true)
        String firstName,

        @Column(name = "last_name", nullable = false)
        @Description("The user's last name")
        @JsonProperty(required = true)
        String lastName,

        @Column(name = "birth_date", nullable = false)
        @Description("The user's birth date")
        @JsonProperty(required = true)
        String birthDate
) { }
