package com.notesapp.smartnotesapp.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notesapp.smartnotesapp.ai.AgentService;
import com.notesapp.smartnotesapp.ai.AiNoteService;
import com.notesapp.smartnotesapp.ai.RagService;
import com.notesapp.smartnotesapp.dto.AiRequest;
import com.notesapp.smartnotesapp.dto.AiResponse;
import com.notesapp.smartnotesapp.entity.UserEntity;
import com.notesapp.smartnotesapp.repository.UserRepository;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiNoteService aiNoteService;
    private final RagService ragService;
    private final AgentService agentService;
    private final UserRepository userRepository;

    public AiController(AiNoteService aiNoteService, RagService ragService, AgentService agentService, UserRepository userRepository) {
        this.aiNoteService = aiNoteService;
        this.ragService = ragService;
        this.agentService = agentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/summarize")
    public ResponseEntity<AiResponse> summarize(
            @RequestBody AiRequest request,
            Principal principal) {

        if (principal == null) { 
            return ResponseEntity.status(401)
                    .body(AiResponse.failure("Unauthorized"));
        }

        String title = request.getTitle();
        String content = request.getContent();

        if (title == null || title.isBlank() ||
            content == null || content.isBlank() ||
            content.trim().length() < 20) {
            return ResponseEntity.badRequest()
                    .body(AiResponse.failure("Title and content are required, and content must be at least 20 characters"));
        }

        try {
            String summary = aiNoteService.summarize(title, content);
            return ResponseEntity.ok(AiResponse.success(summary));
        } catch (Exception e) {
            e.printStackTrace(); // add this
            return ResponseEntity.status(500)
                    .body(AiResponse.failure("AI service unavailable, try again later"));
        }
    }

    // POST /api/ai/search
    @PostMapping("/search")
    public ResponseEntity<?> semanticSearch(
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        if (principal == null) return ResponseEntity.status(401).body(
            Map.of("error", "Unauthorized")
        );

        String query = request.get("query");

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Query cannot be empty")
            );
        }

        try {
            Long userId = userRepository.findByUsername(principal.getName())
                .map(UserEntity::getId)
                .orElse(null);
            String result = ragService.semanticSearch(query, userId);
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            System.out.println("RAG ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body(
                Map.of("error", "Search unavailable, try again later")
            );
        }
    }
    
    @PostMapping("/agent")
    public ResponseEntity<?> agentChat(
            @RequestBody Map<String, String> request,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(401)
                .body(Map.of("error", "Unauthorized"));

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"));
        }

        try {
            // ✅ pass username from JWT
            String username = principal.getName();
            String response = agentService.chat(message, username);
            return ResponseEntity.ok(Map.of("result", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Agent unavailable"));
        }
    }
}