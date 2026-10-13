package com.saasexam.catalog.organization;

import com.saasexam.catalog.organization.OrganizationDtos.OrganizationRequest;
import com.saasexam.catalog.organization.OrganizationDtos.OrganizationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orgs")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrganizationResponse> list() {
        return service.list().stream().map(OrganizationResponse::of).toList();
    }

    @GetMapping("/{orgId}")
    public OrganizationResponse get(@PathVariable String orgId) {
        return OrganizationResponse.of(service.get(orgId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(@Valid @RequestBody OrganizationRequest request) {
        return OrganizationResponse.of(service.create(request));
    }

    @PutMapping("/{orgId}")
    public OrganizationResponse update(@PathVariable String orgId, @Valid @RequestBody OrganizationRequest request) {
        return OrganizationResponse.of(service.update(orgId, request));
    }

    /** Soft-deletes the organisation and everything below it. */
    @DeleteMapping("/{orgId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String orgId) {
        service.delete(orgId);
    }
}
