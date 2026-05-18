package com.notesapp.smartnotesapp.ai;

import org.springframework.stereotype.Service;

@Service
public class AiNoteService {

    private final GeminiService geminiService;

    public AiNoteService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String summarize(String title, String content) {
        String prompt = """
            You are a smart notes assistant.
            Summarize the following note in 2-3 concise sentences.
            Return only the summary, nothing else.

            Title: %s
            Content: %s
            """.formatted(title, content);

        return geminiService.generate(prompt);
    }

    public String generateTags(String title, String content) {
        String prompt = """
            You are a smart notes assistant.
            Generate up to 4 short relevant tags for this note.
            Return only lowercase comma-separated tags, nothing else.

            Title: %s
            Content: %s
            """.formatted(title, content);

        return geminiService.generate(prompt);
    }
}
