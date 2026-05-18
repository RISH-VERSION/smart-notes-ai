package com.notesapp.smartnotesapp.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notesapp.smartnotesapp.ai.RagService;
import com.notesapp.smartnotesapp.dto.NoteRequest;
import com.notesapp.smartnotesapp.dto.NoteResponse;
import com.notesapp.smartnotesapp.service.NoteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final RagService ragService;

    // Constructor injection — both services injected
    public NoteController(NoteService noteService, RagService ragService) {
        this.noteService = noteService;
        this.ragService = ragService;
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            Map.of("error", "Unauthorized", "status", 401)
        );
    }

    @PostMapping
    public ResponseEntity<?> createNote(
            @Valid @RequestBody NoteRequest request,
            Principal principal) {

        if (principal == null) return unauthorizedResponse();

        try {
            // Step 1 — save note to MySQL
            NoteResponse saved = noteService.createNote(principal.getName(), request);

            // Step 2 — index in ChromaDB for semantic search
            // In try-catch so note creation doesn't fail if indexing fails
            // Interview tip: this is a fallback strategy —
            // core feature (save note) works even if AI feature (index) fails
            try {
                ragService.indexNote(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getContent()
                );
            } catch (Exception e) {
                // Fallback — log but don't break note creation
                System.out.println("Indexing failed for note " + saved.getId() + ": " + e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", "Bad Request", "message", e.getMessage(), "status", 400)
            );
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllNotes(Principal principal) {
        if (principal == null) return unauthorizedResponse();
        return ResponseEntity.ok(noteService.getAllNotes(principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request,
            Principal principal) {

        if (principal == null) return unauthorizedResponse();

        try {
            // Step 1 — update note in MySQL
            NoteResponse saved = noteService.updateNote(principal.getName(), id, request);

            // Step 2 — re-index updated note in ChromaDB
            // Old embedding replaced with new one automatically
            try {
                ragService.indexNote(
                    saved.getId(),
                    saved.getTitle(),
                    saved.getContent()
                );
            } catch (Exception e) {
                System.out.println("Re-indexing failed for note " + saved.getId() + ": " + e.getMessage());
            }

            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Not Found", "message", e.getMessage(), "status", 404)
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(
            @PathVariable Long id,
            Principal principal) {

        if (principal == null) return unauthorizedResponse();

        try {
            // Step 1 — delete from MySQL
            noteService.deleteNote(principal.getName(), id);

            // Step 2 — remove from ChromaDB too
            // Keeps vector store in sync with MySQL
            try {
                ragService.deleteNoteIndex(id);
            } catch (Exception e) {
                System.out.println("ChromaDB delete failed for note " + id + ": " + e.getMessage());
            }

            return ResponseEntity.ok(
                Map.of("message", "Note deleted successfully", "status", 200)
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "Not Found", "message", e.getMessage(), "status", 404)
            );
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(
            @RequestParam String keyword,
            Principal principal) {

        if (principal == null) return unauthorizedResponse();

        return ResponseEntity.ok(
            noteService.searchNotes(principal.getName(), keyword)
        );
    }
}
