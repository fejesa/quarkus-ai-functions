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
            Use this tool when you need to obtain the full address of a person.
            Input: a Person object containing first name, last name, and birth date.
            Output: an Address object containing country, city, zipCode, street, and houseNumber.
            This tool should be called if multiple similar users are returned by the first search
            and you need more information (address data) to extend the search criteria.
        """
    )
    public Address getUserAddress(Person person) {
        Log.info("Getting user address for person: " + person);
        return new Address("123 Main St", "Springfield", "IL", "62701", "USA");
    }
}
