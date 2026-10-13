package com.saasexam.exam.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;

/** Reads questions and answer options from the question service's internal API. */
@Component
public class QuestionBankClient {

    /** The fields of a question that scoring needs. */
    public record QuestionRef(String id, String subjectId, List<AnswerRef> answers) {
    }

    public record AnswerRef(String id, String questionId, boolean correct) {
    }

    private final RestClient http;

    public QuestionBankClient(RestClient.Builder builder, @Value("${services.question.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    /** Live questions among {@code ids} with their live answers; unknown or deleted ids are left out. */
    public List<QuestionRef> findLive(Collection<String> ids) {
        return http.get().uri(b -> b.path("/internal/questions").queryParam("ids", ids.toArray()).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
