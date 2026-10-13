package com.saasexam.catalog.subject;

import com.saasexam.catalog.SoftDeleter;
import com.saasexam.catalog.schoolclass.SchoolClass;
import com.saasexam.catalog.schoolclass.SchoolClassService;
import com.saasexam.catalog.subject.SubjectDtos.SubjectRequest;
import com.saasexam.common.HierarchyLevel;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.common.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository repository;
    private final SchoolClassService classes;
    private final SoftDeleter softDeleter;

    public SubjectService(SubjectRepository repository, SchoolClassService classes, SoftDeleter softDeleter) {
        this.repository = repository;
        this.classes = classes;
        this.softDeleter = softDeleter;
    }

    @Transactional(readOnly = true)
    public List<Subject> listByClass(String classId) {
        classes.get(classId);
        return repository.findByClassIdAndStatusNotOrderByName(classId, Status.DELETED);
    }

    @Transactional(readOnly = true)
    public Subject get(String id) {
        return repository.findById(id)
                .filter(s -> !Status.DELETED.equals(s.getStatus()))
                .orElseThrow(() -> new NotFoundException("Subject", id));
    }

    public Subject create(String classId, SubjectRequest request) {
        SchoolClass parent = classes.get(classId);
        Subject subject = new Subject();
        subject.setId(IdGenerator.next());
        subject.setOrgId(parent.getOrgId());
        subject.setClassId(parent.getId());
        apply(subject, request);
        return repository.save(subject);
    }

    public Subject update(String id, SubjectRequest request) {
        Subject subject = get(id);
        apply(subject, request);
        return subject;
    }

    public void delete(String id) {
        get(id);
        softDeleter.delete(HierarchyLevel.SUBJECT, id);
    }

    private static void apply(Subject subject, SubjectRequest request) {
        subject.setName(request.name().trim());
        if (request.status() != null) subject.setStatus(request.status());
    }
}
