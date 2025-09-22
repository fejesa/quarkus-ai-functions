package io.crunch.ai.function.institute;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.inject.Singleton;

@Singleton
public class InstituteChatMemoryProvider implements ChatMemoryProvider {

    private final ChatMemory chatMemory;

    public InstituteChatMemoryProvider() {
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(50);
    }

    @Override
    public ChatMemory get(Object memoryId) {
        return chatMemory;
    }
}
