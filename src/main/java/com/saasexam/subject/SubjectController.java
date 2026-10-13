package com.saasexam.subject;

import com.saasexam.subject.SubjectDtos.SubjectRequest;
import com.saasexam.subject.SubjectDtos.SubjectResponse;
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
@RequestMapping("/api")
public class SubjectController {

    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @GetMapping("/classes/{classId}/subjects")
    public List<SubjectResponse> list(@PathVariable String classId) {
        return service.listByClass(classId).stream().map(SubjectResponse::of).toList();
    }

    @PostMapping("/classes/{classId}/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse create(@PathVariable String classId, @Valid @RequestBody SubjectRequest request) {
        return SubjectResponse.of(service.create(classId, request));
    }

    @GetMapping("/subjects/{subjectId}")
    public SubjectResponse get(@PathVariable String subjectId) {
        return SubjectResponse.of(service.get(subjectId));
    }

    @PutMapping("/subjects/{subjectId}")
    public SubjectResponse update(@PathVariable String subjectId, @Valid @RequestBody SubjectRequest request) {
        return SubjectResponse.of(service.update(subjectId, request));
    }

    /** Soft-deletes the subject and its chapters, questions and answers. */
    @DeleteMapping("/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String subjectId) {
        service.delete(subjectId);
    }
}
