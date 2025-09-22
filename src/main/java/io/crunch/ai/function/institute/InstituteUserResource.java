package io.crunch.ai.function.institute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.crunch.ai.function.statistic.UserSearchResult;
import io.quarkus.logging.Log;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Optional;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstituteUserResource {

    private final UserSearchAssistant searchAssistant;

    private final InstituteUserService instituteUserService;

    private final ChatMemoryProvider chatMemoryProvider;

    public InstituteUserResource(UserSearchAssistant searchAssistant, InstituteUserService instituteUserService, ChatMemoryProvider chatMemoryProvider) {
        this.searchAssistant = searchAssistant;
        this.instituteUserService = instituteUserService;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<UserSearchResult> search(@BeanParam @Valid UserSearchQuery query) {
        Log.info("Received user search request: " + query);
        var sessionId = UUID.randomUUID().toString();
        try {
            if (instituteUserService.isValidInstituteUser(query.firstName(), query.lastName(), query.birthDate())) {
                var result = searchAssistant.search(sessionId, query);
                Log.info("User search result: " + result);
                return RestResponse.ResponseBuilder.ok(getResultEntity(result)).build();
            }
            Log.warn("No valid institute user found for person: " + query.firstName() + " " + query.lastName() + ", birthDate=" + query.birthDate());
            return RestResponse.notFound();
        } catch (JsonProcessingException e) {
            Log.error("Error processing user search request", e);
            throw new WebApplicationException("Failed to process user search request", e, 500);
        } finally {
            Optional.ofNullable(chatMemoryProvider.get(sessionId)).ifPresent(ChatMemory::clear);
        }
    }

    private UserSearchResult getResultEntity(String result) throws JsonProcessingException {
        return new ObjectMapper().readValue(result, UserSearchResult.class);
    }

}
