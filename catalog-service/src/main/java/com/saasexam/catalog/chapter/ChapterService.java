package com.saasexam.catalog.chapter;

import com.saasexam.catalog.SoftDeleter;
import com.saasexam.catalog.chapter.ChapterDtos.ChapterRequest;
import com.saasexam.catalog.subject.Subject;
import com.saasexam.catalog.subject.SubjectService;
import com.saasexam.common.HierarchyLevel;
import com.saasexam.common.IdGenerator;
import com.saasexam.common.NotFoundException;
import com.saasexam.common.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChapterService {

    private final ChapterRepository repository;
    private final SubjectService subjects;
    private final SoftDeleter softDeleter;

    public ChapterService(ChapterRepository repository, SubjectService subjects, SoftDeleter softDeleter) {
        this.repository = repository;
        this.subjects = subjects;
        this.softDeleter = softDeleter;
    }

    @Transactional(readOnly = true)
    public List<Chapter> listBySubject(String subjectId) {
        subjects.get(subjectId);
        return repository.findBySubjectIdAndStatusNotOrderByName(subjectId, Status.DELETED);
    }

    @Transactional(readOnly = true)
    public Chapter get(String id) {
        return repository.findById(id)
                .filter(c -> !Status.DELETED.equals(c.getStatus()))
                .orElseThrow(() -> new NotFoundException("Chapter", id));
    }

    public Chapter create(String subjectId, ChapterRequest request) {
        Subject parent = subjects.get(subjectId);
        Chapter chapter = new Chapter();
        chapter.setId(IdGenerator.next());
        chapter.setOrgId(parent.getOrgId());
        chapter.setClassId(parent.getClassId());
        chapter.setSubjectId(parent.getId());
        apply(chapter, request);
        return repository.save(chapter);
    }

    public Chapter update(String id, ChapterRequest request) {
        Chapter chapter = get(id);
        apply(chapter, request);
        return chapter;
    }

    /** Soft-deletes the chapter together with the questions (and answers) filed under it. */
    public void delete(String id) {
        get(id);
        softDeleter.delete(HierarchyLevel.CHAPTER, id);
    }

    private static void apply(Chapter chapter, ChapterRequest request) {
        chapter.setName(request.name().trim());
        if (request.status() != null) chapter.setStatus(request.status());
    }
}
