package com.notesapp.smartnotesapp.repository;

import com.notesapp.smartnotesapp.entity.RagEvaluationLog;  // change to your actual package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagEvaluationLogRepository extends JpaRepository<RagEvaluationLog, Long> {

    // Find all logs for a specific user
    List<RagEvaluationLog> findByUserIdOrderByEvaluatedAtDesc(Long userId);

    // Find low-scoring interactions (useful for debugging your RAG pipeline)
    @Query("SELECT r FROM RagEvaluationLog r WHERE r.ragasScore < :threshold ORDER BY r.evaluatedAt DESC")
    List<RagEvaluationLog> findLowScoringInteractions(double threshold);
}
