package io.crunch.ai.institute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crunch.ai.statistic.UserSearchQuery;
import io.crunch.ai.statistic.UserSearchResult;
import io.quarkus.logging.Log;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstituteUserResource {

    private final AiUserService userService;

    private final InstituteUserService instituteUserService;

    public InstituteUserResource(AiUserService aiUserService, InstituteUserService instituteUserService) {
        this.userService = aiUserService;
        this.instituteUserService = instituteUserService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserSearchResult search(@BeanParam @Valid UserSearchQuery query) {
        Log.info("Received user search request: " + query);
        try {
            if (instituteUserService.isValidInstituteUser(query.firstName(), query.lastName(), query.birthDate())) {
                var result = userService.search(query);
                Log.info("User search result: " + result);
                return new ObjectMapper().readValue(result, UserSearchResult.class);
            }
            Log.warn("No valid institute user found for person: " + query.firstName() + " " + query.lastName() + ", birthDate=" + query.birthDate());
            throw new WebApplicationException("No valid institute user found for the given person", 400);
        } catch (JsonProcessingException e) {
            Log.error("Error processing user search request", e);
            throw new WebApplicationException("Failed to process user search request", e, 500);
        }
    }

}
