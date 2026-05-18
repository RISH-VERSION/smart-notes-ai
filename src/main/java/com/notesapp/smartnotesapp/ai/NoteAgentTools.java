package com.notesapp.smartnotesapp.ai;

import org.springframework.stereotype.Component;

import com.notesapp.smartnotesapp.entity.UserEntity;
import com.notesapp.smartnotesapp.repository.NoteRepository;
import com.notesapp.smartnotesapp.repository.UserRepository;

import dev.langchain4j.agent.tool.Tool;

// This class defines the TOOLS the agent can use
// LangChain4j automatically detects @Tool methods
// and gives them to the AI to call when needed
@Component
public class NoteAgentTools {

    private final NoteRepository noteRepository;
    private final RagService ragService;
    private final UserRepository userRepository;

    public NoteAgentTools(NoteRepository noteRepository, RagService ragService, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.ragService = ragService;
        this.userRepository = userRepository;
    }

 // ✅ renamed from searchNotes to findNotes
    @Tool("Search notes by keyword or topic")
    public String findNotes(
            @dev.langchain4j.agent.tool.P("search keyword") String query,
            @dev.langchain4j.agent.tool.P("the username") String username) {
        Long userId = userRepository.findByUsername(username)
            .map(UserEntity::getId)
            .orElse(null);
        return ragService.semanticSearch(query, userId);
    }
    
    @Tool("Count total notes for a user")
    public String countNotes(
            @dev.langchain4j.agent.tool.P("the username") String username) {
        long count = noteRepository.countByUserUsername(username);
        return "You have " + count + " notes in total.";
    }

    @Tool("Get recent note titles for a user")
    public String getRecentNotes(
            @dev.langchain4j.agent.tool.P("the username") String username) {
        return noteRepository.findTop3ByUserUsernameOrderByCreatedAtDesc(username)
                .stream()
                .map(note -> "- " + note.getTitle())
                .reduce("", (a, b) -> a + "\n" + b);
    }
}