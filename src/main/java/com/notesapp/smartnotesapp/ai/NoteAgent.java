package com.notesapp.smartnotesapp.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

// LangChain4j automatically implements this interface
// It connects the ChatModel + Tools + Memory together
public interface NoteAgent {

	@SystemMessage("""
		    You are a smart notes assistant.
		    You have access to tools: countNotes, findNotes, getRecentNotes.
		    RULES:
		    - ALWAYS use tools to answer. NEVER answer from your own knowledge.
		    - For any search question, ALWAYS call findNotes tool first.
		    - Only respond based on what the tool returns.
		    - If tool returns nothing, say "I couldn't find any notes about that."
		    """)
    String chat(@UserMessage String userMessage);
}