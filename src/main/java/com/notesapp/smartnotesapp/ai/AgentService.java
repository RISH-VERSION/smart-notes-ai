package com.notesapp.smartnotesapp.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

@Service
public class AgentService {
    private final NoteAgent agent;

    public AgentService(
            @Value("${groq.api.key}") String apiKey,
            NoteAgentTools tools) {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("meta-llama/llama-4-scout-17b-16e-instruct")
                .temperature(0.0)
                .maxTokens(1024)
                .build();

        this.agent = AiServices.builder(NoteAgent.class)
                .chatLanguageModel(model)
                .tools(tools)
                .build();
    }

    public String chat(String userMessage, String username) {
        String enrichedMessage = "My username is: " + username + ". " + userMessage;
        return agent.chat(enrichedMessage);
    }
}