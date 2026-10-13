package com.saasexam.schoolclass;

import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.common.SoftDeleter;
import com.saasexam.common.Status;
import com.saasexam.organization.OrganizationService;
import com.saasexam.schoolclass.SchoolClassDtos.ClassRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SchoolClassService {

    private final SchoolClassRepository repository;
    private final OrganizationService organizations;
    private final SoftDeleter softDeleter;

    public SchoolClassService(SchoolClassRepository repository, OrganizationService organizations,
                              SoftDeleter softDeleter) {
        this.repository = repository;
        this.organizations = organizations;
        this.softDeleter = softDeleter;
    }

    @Transactional(readOnly = true)
    public List<SchoolClass> listByOrg(String orgId) {
        organizations.get(orgId);
        return repository.findByOrgIdAndStatusNotOrderByName(orgId, Status.DELETED);
    }

    @Transactional(readOnly = true)
    public SchoolClass get(String id) {
        return repository.findById(id)
                .filter(c -> !Status.DELETED.equals(c.getStatus()))
                .orElseThrow(() -> new NotFoundException("Class", id));
    }

    public SchoolClass create(String orgId, ClassRequest request) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(IdGenerator.next());
        schoolClass.setOrgId(organizations.get(orgId).getId());
        apply(schoolClass, request);
        return repository.save(schoolClass);
    }

    public SchoolClass update(String id, ClassRequest request) {
        SchoolClass schoolClass = get(id);
        apply(schoolClass, request);
        return schoolClass;
    }

    public void delete(String id) {
        get(id);
        softDeleter.delete(SoftDeleter.Level.CLASS, id);
    }

    private static void apply(SchoolClass schoolClass, ClassRequest request) {
        schoolClass.setName(request.name().trim());
        if (request.status() != null) schoolClass.setStatus(request.status());
    }
}
