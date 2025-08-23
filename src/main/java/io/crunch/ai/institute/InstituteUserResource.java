package io.crunch.ai.institute;

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

    private final UserService userService;

    public InstituteUserResource(UserService userService) {
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserSearchResult search(@BeanParam @Valid UserSearchQuery query) {
        Log.info("Received user search request: " + query);
        // TODO: fetch user from institute service and then call the agent
        try {
            var result = userService.search(query);
            Log.info("User search result: " + result);
            var userSearchResult = new ObjectMapper().readValue(result, UserSearchResult.class);
            return userSearchResult;
        } catch (Exception e) {
            Log.error("Error processing user search request", e);
            throw new WebApplicationException("Failed to process user search request", e, 500);
        }
    }

}
