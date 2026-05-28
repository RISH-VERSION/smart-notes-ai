package com.notesapp.smartnotesapp.ai;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class RagasClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RagasClient(WebClient.Builder builder, ObjectMapper objectMapper) {
    	this.webClient = builder
    		    .baseUrl(System.getenv().getOrDefault("RAGAS_URL", "http://localhost:8001"))
    		    .build();
        this.objectMapper = objectMapper;
    }

    // --- EvaluationRequest ---
    public static class EvaluationRequest {
        private List<String> questions;
        private List<String> answers;
        private List<List<String>> contexts;
        @JsonProperty("ground_truths")
        private List<String> groundTruths;

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private List<String> questions;
            private List<String> answers;
            private List<List<String>> contexts;
            private List<String> groundTruths;

            public Builder questions(List<String> q) { this.questions = q; return this; }
            public Builder answers(List<String> a) { this.answers = a; return this; }
            public Builder contexts(List<List<String>> c) { this.contexts = c; return this; }
            public Builder groundTruths(List<String> g) { this.groundTruths = g; return this; }
            public EvaluationRequest build() {
                EvaluationRequest r = new EvaluationRequest();
                r.questions = this.questions;
                r.answers = this.answers;
                r.contexts = this.contexts;
                r.groundTruths = this.groundTruths;
                return r;
            }
        }

        public List<String> getQuestions() { return questions; }
        public List<String> getAnswers() { return answers; }
        public List<List<String>> getContexts() { return contexts; }
        public List<String> getGroundTruths() { return groundTruths; }
    }

    // --- EvaluationResponse ---
    public static class EvaluationResponse {
        private Scores scores;
        @JsonProperty("per_question")
        private List<Map<String, Object>> perQuestion;
        @JsonProperty("sample_count")
        private int sampleCount;
        private String status;

        public Scores getScores() { return scores; }
        public void setScores(Scores scores) { this.scores = scores; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<Map<String, Object>> getPerQuestion() { return perQuestion; }
        public void setPerQuestion(List<Map<String, Object>> perQuestion) { this.perQuestion = perQuestion; }
        public int getSampleCount() { return sampleCount; }
        public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
    }

    // --- Scores ---
    public static class Scores {
    	private Double faithfulness;
        @JsonProperty("answer_relevancy")
        private Double answerRelevancy;
        @JsonProperty("context_precision")
        private Double contextPrecision;
        @JsonProperty("context_recall")
        private Double contextRecall;
        @JsonProperty("ragas_score")
        private Double ragasScore;

        public Double getFaithfulness() { return faithfulness; }
        public void setFaithfulness(Double v) { this.faithfulness = v; }
        public Double getAnswerRelevancy() { return answerRelevancy; }
        public void setAnswerRelevancy(Double v) { this.answerRelevancy = v; }
        public Double getContextPrecision() { return contextPrecision; }
        public void setContextPrecision(Double v) { this.contextPrecision = v; }
        public Double getContextRecall() { return contextRecall; }
        public void setContextRecall(Double v) { this.contextRecall = v; }
        public Double getRagasScore() { return ragasScore; }
        public void setRagasScore(Double v) { this.ragasScore = v; }
    }

    public Mono<EvaluationResponse> evaluateAsync(EvaluationRequest request) {
        return webClient.post()
            .uri("/evaluate")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(raw -> System.out.println("RAGAS RAW JSON: " + raw))
            .map(raw -> {
                try { return objectMapper.readValue(raw, EvaluationResponse.class); }
                catch (Exception e) { return buildFallbackResponse(); }
            })
            .doOnError(e -> System.err.println("RAGAS evaluation failed: " + e.getMessage()))
            .onErrorReturn(buildFallbackResponse());
    }

    private EvaluationResponse buildFallbackResponse() {
        EvaluationResponse r = new EvaluationResponse();
        r.setStatus("unavailable");
        return r;
    }
}