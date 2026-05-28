package com.notesapp.smartnotesapp.evaluation;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notesapp.smartnotesapp.ai.RagasClient;
import com.notesapp.smartnotesapp.entity.RagEvaluationLog;
import com.notesapp.smartnotesapp.repository.RagEvaluationLogRepository;

@Service
public class RagEvaluationService {

    private final RagasClient ragasClient;
    private final RagEvaluationLogRepository logRepository;
    private final ObjectMapper objectMapper;

    public RagEvaluationService(RagasClient ragasClient,
                                 RagEvaluationLogRepository logRepository,
                                 ObjectMapper objectMapper) {
        this.ragasClient = ragasClient;
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
    }

    public void evaluateAndLogAsync(String question, List<String> retrievedChunks,
                                     String geminiAnswer, String groundTruth, Long userId) {

        RagasClient.EvaluationRequest req = RagasClient.EvaluationRequest.builder()
            .questions(List.of(question))
            .answers(List.of(geminiAnswer))
            .contexts(List.of(retrievedChunks))
            .groundTruths(List.of(groundTruth))
            .build();

        ragasClient.evaluateAsync(req)
            .subscribe(response -> {
                if ("success".equals(response.getStatus())) {
                    RagasClient.Scores s = response.getScores();
                    try { System.out.println("RAW SCORES: " + objectMapper.writeValueAsString(s)); } catch (Exception e) {}
                    RagEvaluationLog log = new RagEvaluationLog();
                    log.setQuestion(question);
                    log.setAnswer(geminiAnswer);
                    log.setContextsJson(toJson(retrievedChunks));
                    log.setFaithfulness(s.getFaithfulness());
                    log.setAnswerRelevancy(s.getAnswerRelevancy());
                    log.setContextPrecision(s.getContextPrecision());
                    log.setRagasScore(s.getRagasScore());
                    log.setEvaluatedAt(Instant.now());
                    log.setUserId(userId);
                    logRepository.save(log);
                    System.out.println("RAGAS scores — F:" + s.getFaithfulness() +
                        " AR:" + s.getAnswerRelevancy() +
                        " CP:" + s.getContextPrecision() +
                        " Overall:" + s.getRagasScore());
                }
            });
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { return "[]"; }
    }
}