package com.notesapp.smartnotesapp.dto;

import java.time.LocalDateTime;

import com.notesapp.smartnotesapp.entity.Note;

// ✅ Safe DTO — no user data exposed to frontend
public class NoteResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt; // ✅ added
    private LocalDateTime updatedAt; // ✅ added

    public NoteResponse(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.createdAt = note.getCreatedAt(); // ✅ added
        this.updatedAt = note.getUpdatedAt(); // ✅ added
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}