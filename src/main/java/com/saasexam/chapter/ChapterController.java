package com.saasexam.chapter;

import com.saasexam.chapter.ChapterDtos.ChapterRequest;
import com.saasexam.chapter.ChapterDtos.ChapterResponse;
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
public class ChapterController {

    private final ChapterService service;

    public ChapterController(ChapterService service) {
        this.service = service;
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public List<ChapterResponse> list(@PathVariable String subjectId) {
        return service.listBySubject(subjectId).stream().map(ChapterResponse::of).toList();
    }

    @PostMapping("/subjects/{subjectId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterResponse create(@PathVariable String subjectId, @Valid @RequestBody ChapterRequest request) {
        return ChapterResponse.of(service.create(subjectId, request));
    }

    @GetMapping("/chapters/{chapterId}")
    public ChapterResponse get(@PathVariable String chapterId) {
        return ChapterResponse.of(service.get(chapterId));
    }

    @PutMapping("/chapters/{chapterId}")
    public ChapterResponse update(@PathVariable String chapterId, @Valid @RequestBody ChapterRequest request) {
        return ChapterResponse.of(service.update(chapterId, request));
    }

    /** Soft-deletes the chapter and the questions and answers filed under it. */
    @DeleteMapping("/chapters/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String chapterId) {
        service.delete(chapterId);
    }
}
