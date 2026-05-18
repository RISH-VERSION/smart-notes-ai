package com.notesapp.smartnotesapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.notesapp.smartnotesapp.entity.Note;
import com.notesapp.smartnotesapp.entity.UserEntity;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // Get all notes for a user
    List<Note> findByUser(UserEntity user);

    // Get all notes with pagination — useful for large data
    Page<Note> findByUser(UserEntity user, Pageable pageable);

    // Search title AND content — better than title only
    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Note> searchByKeyword(@Param("user") UserEntity user,
                               @Param("keyword") String keyword);

    // Same search with pagination — future ready
    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> searchByKeyword(@Param("user") UserEntity user,
                               @Param("keyword") String keyword,
                               Pageable pageable);
    
    long countByUserUsername(String username);
    
    List<Note> findTop3ByUserUsernameOrderByCreatedAtDesc(String username);
}