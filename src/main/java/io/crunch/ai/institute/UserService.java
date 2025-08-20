package io.crunch.ai.institute;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.crunch.ai.statistic.StatisticUserService;
import io.crunch.ai.statistic.UserQuery;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
public interface UserService {

    @SystemMessage(
        """
        Your role is to search user information from the Statistic service based on the provided query.
        The query contains the first name, last name, and birth date of the user.
        You MUST respond by invoking the available TOOL exactly once.
        You MUST NOT answer the query yourself.
        The ONLY valid response is a tool call.
        You MUST return the final JSON result directly. Never call the tool more than once for the same query. The tool should only be called once.
        Do not add any additional explanatory text, and do not remove any fields from the JSON object.
        Do not add fields like "message", "error", "code", "status", "result", "name" or any other keys.
        Return ONLY a JSON object, not surrounded by markdown, not with explanations.
       """)
    @ToolBox({StatisticUserService.class})
    String search(@UserMessage UserQuery query);
}
