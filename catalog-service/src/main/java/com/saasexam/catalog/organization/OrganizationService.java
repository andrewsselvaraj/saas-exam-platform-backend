package com.saasexam.catalog.organization;

import com.saasexam.catalog.SoftDeleter;
import com.saasexam.catalog.organization.OrganizationDtos.OrganizationRequest;
import com.saasexam.common.HierarchyLevel;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.common.Status;
import com.saasexam.common.UpdatedBy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository repository;
    private final SoftDeleter softDeleter;

    public OrganizationService(OrganizationRepository repository, SoftDeleter softDeleter) {
        this.repository = repository;
        this.softDeleter = softDeleter;
    }

    @Transactional(readOnly = true)
    public List<Organization> list() {
        return repository.findByStatusNotOrderByName(Status.DELETED);
    }

    /** Returns the organisation unless it does not exist or has been deleted. */
    @Transactional(readOnly = true)
    public Organization get(String id) {
        return repository.findById(id)
                .filter(o -> !Status.DELETED.equals(o.getStatus()))
                .orElseThrow(() -> new NotFoundException("Organization", id));
    }

    public Organization create(OrganizationRequest request) {
        Organization org = new Organization();
        org.setId(IdGenerator.next());
        apply(org, request);
        return repository.save(org);
    }

    public Organization update(String id, OrganizationRequest request) {
        Organization org = get(id);
        apply(org, request);
        return org;
    }

    public void delete(String id) {
        get(id);
        softDeleter.delete(HierarchyLevel.ORG, id);
    }

    private static void apply(Organization org, OrganizationRequest request) {
        org.setName(request.name().trim());
        org.setEmail(request.email().trim());
        org.setUpdatedBy(UpdatedBy.orSystem(request.updatedBy()));
        if (request.status() != null) org.setStatus(request.status());
    }
}
