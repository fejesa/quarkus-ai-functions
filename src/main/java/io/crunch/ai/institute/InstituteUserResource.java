package io.crunch.ai.institute;

import io.crunch.ai.statistic.UserQuery;
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
    public String search(@BeanParam @Valid UserQuery query) {
        Log.info("Received user search request: " + query);
        // TODO: fetch user from institute service and then call the agent
        return userService.search(query);
    }
}
