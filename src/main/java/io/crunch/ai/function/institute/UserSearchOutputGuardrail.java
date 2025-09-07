package io.crunch.ai.function.institute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import io.crunch.ai.function.statistic.UserSearchResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSearchOutputGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        try {
            var result = responseFromLLM.text();
            Log.info("User search result: " + result);
            new ObjectMapper().readValue(result, UserSearchResult.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            Log.error("Invalid user search JSON", e);
            return retry("Invalid user search JSON");
        }
        return success();
    }
}
