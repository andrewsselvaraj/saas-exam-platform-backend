package com.saasexam.catalog.schoolclass;

import com.saasexam.catalog.schoolclass.SchoolClassDtos.ClassRequest;
import com.saasexam.catalog.schoolclass.SchoolClassDtos.ClassResponse;
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
public class SchoolClassController {

    private final SchoolClassService service;

    public SchoolClassController(SchoolClassService service) {
        this.service = service;
    }

    @GetMapping("/orgs/{orgId}/classes")
    public List<ClassResponse> list(@PathVariable String orgId) {
        return service.listByOrg(orgId).stream().map(ClassResponse::of).toList();
    }

    @PostMapping("/orgs/{orgId}/classes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassResponse create(@PathVariable String orgId, @Valid @RequestBody ClassRequest request) {
        return ClassResponse.of(service.create(orgId, request));
    }

    @GetMapping("/classes/{classId}")
    public ClassResponse get(@PathVariable String classId) {
        return ClassResponse.of(service.get(classId));
    }

    @PutMapping("/classes/{classId}")
    public ClassResponse update(@PathVariable String classId, @Valid @RequestBody ClassRequest request) {
        return ClassResponse.of(service.update(classId, request));
    }

    /** Soft-deletes the class and its subjects, chapters, questions and answers. */
    @DeleteMapping("/classes/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String classId) {
        service.delete(classId);
    }
}
