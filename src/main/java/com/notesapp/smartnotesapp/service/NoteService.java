package com.notesapp.smartnotesapp.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.notesapp.smartnotesapp.dto.NoteRequest;
import com.notesapp.smartnotesapp.dto.NoteResponse;
import com.notesapp.smartnotesapp.entity.Note;
import com.notesapp.smartnotesapp.entity.UserEntity;
import com.notesapp.smartnotesapp.repository.NoteRepository;
import com.notesapp.smartnotesapp.repository.UserRepository;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // FIXED: Try email first (OAuth2 users), fallback to username (JWT users)
    private UserEntity getUser(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier));
    }

    private void verifyOwnership(Note note, String identifier) {
        UserEntity user = getUser(identifier);
        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to access this note");
        }
    }

    @Transactional
    public NoteResponse createNote(String identifier, NoteRequest request) {
        UserEntity user = getUser(identifier);
        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUser(user);
        return new NoteResponse(noteRepository.save(note));
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(String identifier) {
        return noteRepository.findByUser(getUser(identifier))
                .stream()
                .map(NoteResponse::new)
                .toList();
    }

    @Transactional
    public NoteResponse updateNote(String identifier, Long id, NoteRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        verifyOwnership(note, identifier);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        return new NoteResponse(noteRepository.save(note));
    }

    @Transactional
    public void deleteNote(String identifier, Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        verifyOwnership(note, identifier);
        noteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> searchNotes(String identifier, String keyword) {
        return noteRepository.searchByKeyword(getUser(identifier), keyword)
                .stream()
                .map(NoteResponse::new)
                .toList();
    }
}
