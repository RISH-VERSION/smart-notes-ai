package com.notesapp.smartnotesapp.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.notesapp.smartnotesapp.evaluation.RagEvaluationService;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;


@Service
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final GeminiService geminiService;
    private final RagEvaluationService ragEvaluationService;
    
    public RagService(
            @Value("${chroma.url}") String chromaUrl,
            @Value("${chroma.collection}") String collection,
            GeminiService geminiService,
            RagEvaluationService ragEvaluationService) {

        this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        this.embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl)
                .collectionName(collection)
                .build();
        this.geminiService = geminiService;
        this.ragEvaluationService = ragEvaluationService;
    }
    
    
    public void indexNote(Long noteId, String title, String content) {
        String text = "Title: " + title + "\nContent: " + content;
        
        Document document = Document.from(text);
        
        DocumentSplitter splitter = DocumentSplitters.recursive(200, 20);
        
        List<TextSegment> segments = splitter.split(document);
        
        for (TextSegment segment : segments) {
            TextSegment segmentWithMeta = TextSegment.from(
                segment.text(),
                Metadata.from("noteId", noteId.toString())
            );
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            embeddingStore.add(embedding, segmentWithMeta);
        }
    }

    public String semanticSearch(String query, Long userId) {
    	//embed(query) → returns Response<Embedding> (wrapper with metadata, token usage info etc.)
    	//.content() → extracts the actual Embedding from inside that wrapper
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .minScore(0.5)
                .build();
        
        /*embeddingStore.search(searchRequest) → returns EmbeddingSearchResult → 
         .matches() → extracts and gives you the List<EmbeddingMatch<TextSegment>> from inside it.*/
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.search(searchRequest).matches();
        
        if (matches.isEmpty()) {
            return "No relevant notes found for your search.";
        }

        StringBuilder context = new StringBuilder();
        List<String> chunks = new java.util.ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            context.append(match.embedded().text()).append("\n\n");
            chunks.add(match.embedded().text());
        }

        String prompt = """
                You are a smart notes assistant.
                Answer the user's question using ONLY the notes provided below.
                If the answer is not in the notes, say "I couldn't find that in your notes."
                User question: %s
                Relevant notes:
                %s
                """.formatted(query, context.toString());

        String answer = geminiService.generate(prompt);

        ragEvaluationService.evaluateAndLogAsync(query, chunks, answer, answer, userId);

        return answer;
    }

    public void deleteNoteIndex(Long noteId) {
        embeddingStore.removeAll(
                MetadataFilterBuilder
                        .metadataKey("noteId")
                        .isEqualTo(noteId.toString())
        );
    }
}
