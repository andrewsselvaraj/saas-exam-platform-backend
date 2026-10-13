package com.saasexam.exam.client;

import com.saasexam.common.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Reads subjects from the catalog service. Deleted subjects come back as {@link NotFoundException}. */
@Component
public class CatalogClient {

    /** The fields of the catalog's subject that this service needs. */
    public record SubjectRef(String id, String orgId, String classId) {
    }

    private final RestClient http;

    public CatalogClient(RestClient.Builder builder, @Value("${services.catalog.url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    public SubjectRef getSubject(String id) {
        return http.get().uri("/api/subjects/{id}", id).retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) -> {
                    throw new NotFoundException("Subject", id);
                })
                .body(SubjectRef.class);
    }
}
