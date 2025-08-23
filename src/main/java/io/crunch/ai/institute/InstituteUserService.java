package io.crunch.ai.institute;

import dev.langchain4j.agent.tool.Tool;
import io.crunch.ai.common.Address;
import io.crunch.ai.common.Person;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

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
    public Address getUserAddress(Person person) {
        Log.info("Getting user address for person: " + person);
        return new Address("123 Main St", "Springfield", "IL", "62701", "USA");
    }
}
