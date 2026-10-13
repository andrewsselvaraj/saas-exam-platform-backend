package com.saasexam.catalog.client;

import com.saasexam.common.HierarchyLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Calls the question service's internal API. */
@Component
public class QuestionBankClient {

    /** Live question count of one (subject, chapter) pair; chapterId is null for questions without a chapter. */
    public record QuestionCount(String subjectId, String chapterId, long count) {
    }

    private record Deletion(HierarchyLevel level, String id) {
    }

    private final RestClient http;

    public QuestionBankClient(RestClient.Builder builder, @Value("${services.question.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public List<QuestionCount> countByOrg(String orgId) {
        return http.get().uri("/internal/question-counts?orgId={orgId}", orgId).retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    /** Soft-deletes the questions and answers filed under the given catalog node. */
    public void deleteBelow(HierarchyLevel level, String id) {
        http.post().uri("/internal/hierarchy-deletions").body(new Deletion(level, id)).retrieve().toBodilessEntity();
    }
}
