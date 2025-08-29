package io.crunch.ai.function.institute;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.function.common.Address;
import io.crunch.ai.function.common.Person;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

/**
 * Service responsible for retrieving and validating institute user information.
 * <p>
 * This class integrates with the LangChain4j tool system, exposing a tool
 * ({@link #getUserAddress(Person)}) that provides the original query person's
 * full {@link Address} for similarity-based matching flows.
 * </p>
 *
 * <h2>Key Responsibilities</h2>
 * <ul>
 *   <li>Expose the {@code getUserAddress} tool for retrieving the original person's address.</li>
 *   <li>Enforce business rules regarding when and how this tool should be invoked in similarity match flows.</li>
 *   <li>Provide validation methods to check if a given person exists in the institute's records.</li>
 *   <li>Encapsulate the retrieval logic of {@link InstituteUser} entities from the persistence layer.</li>
 * </ul>
 *
 * <h2>LangChain4j Integration</h2>
 * The {@code getUserAddress} method is annotated as a {@link Tool}, making it available
 * to the model during reasoning. It is strictly used to retrieve the "original" user's address
 * and must <strong>never</strong> be used for candidate users.
 *
 * <h2>Rules Enforced in the Tool</h2>
 * <ul>
 *   <li>If a {@code SIMILARMATCH} result is returned, this tool <strong>must</strong> be called exactly once.</li>
 *   <li>The final output must not be generated until this tool has been invoked and its result
 *       integrated into similarity comparisons.</li>
 *   <li>Candidate user addresses are never retrieved through this tool; they come directly from the search results.</li>
 *   <li>The input {@link Person} must not be {@code null} or empty.</li>
 * </ul>
 *
 * <h2>Transactional Behavior</h2>
 * The {@code getUserAddress} method is transactional and ensures database operations are
 * executed within a transaction boundary. By default, it is not necessary to annotate, but some test scenarios require it.
 *
 * @see Person
 * @see Address
 * @see InstituteUser
 */
@ApplicationScoped
public class InstituteUserService {

    @Tool(name = "getUserAddress",
          value = """
            Use this tool to obtain the full address of the ORIGINAL query person.
            Input: a Person object containing:
            - firstName (non-null, non-empty, exact from input)
            - lastName (non-null, non-empty, exact from input)
            - birthDate (non-null, non-empty, exact from input)
            Output: Returns the ORIGINAL person's Address as a JSON OBJECT (not a string) with fields:
               {
                 "country": "<string>",
                 "city": "<string>",
                 "zipCode": "<string>",
                 "street": "<string>",
                 "houseNumber": "<string>"
               }

            RULES:
             - If `searchUser` returns SIMILARMATCH, you are REQUIRED to call this tool exactly once.
             - Skipping this tool call in a SIMILARMATCH flow is a violation of the rules.
             - Final output MUST NOT be produced until after this tool is called and its result is used in similarity checks.
             - NEVER call this tool for candidate users — their addresses come directly from the search results.
             - The returned address is called 'original' and will be used as the baseline for similarity checks.
             - The Person input to this tool MUST NEVER be null, empty, or substituted.
             - Passing null is a violation of the rules.
        """
    )
    @Transactional
    public Address getUserAddress(@P(value = "The person whose address should be fetched.", required = true) Person person) {
        Log.info("Getting user address for person: " + person);
        return getInstituteUser(person.firstName(), person.lastName(), person.birthDate())
                .map(InstituteUser::getAddress)
                .orElseThrow(() -> new NoInstituteUserFound("No user found for person: " + person));
    }

    public boolean isValidInstituteUser(String firstName, String lastName, String birthDate) {
        return getInstituteUser(firstName, lastName, birthDate).isPresent();
    }

    private Optional<InstituteUser> getInstituteUser(String firstName, String lastName, String birthDate) {
        return InstituteUser.find("person.firstName = ?1 and person.lastName = ?2 and person.birthDate = ?3", firstName, lastName, birthDate)
                .singleResultOptional()
                .map(e -> (InstituteUser) e);
    }
}
