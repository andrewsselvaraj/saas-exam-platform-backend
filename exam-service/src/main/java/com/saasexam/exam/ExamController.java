package com.saasexam.exam;

import com.saasexam.exam.ExamDtos.ExamResult;
import com.saasexam.exam.ExamDtos.ExamSubmission;
import com.saasexam.exam.ExamDtos.ExamSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService service;

    public ExamController(ExamService service) {
        this.service = service;
    }

    /** Records a completed attempt and returns its score. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamResult submit(@Valid @RequestBody ExamSubmission submission) {
        return service.submit(submission);
    }

    @GetMapping("/{sessionId}")
    public ExamResult get(@PathVariable String sessionId) {
        return service.get(sessionId);
    }

    @GetMapping
    public List<ExamSummary> list(@RequestParam String subjectId, @RequestParam(required = false) String takenBy) {
        return service.list(subjectId, takenBy);
    }
}
