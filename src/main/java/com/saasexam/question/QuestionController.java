package com.saasexam.question;

import com.saasexam.question.QuestionDtos.AnswerRequest;
import com.saasexam.question.QuestionDtos.AnswerResponse;
import com.saasexam.question.QuestionDtos.QuestionRequest;
import com.saasexam.question.QuestionDtos.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    // ---- questions -------------------------------------------------------------------------------------------

    /** All questions of a subject (with or without a chapter), each with its answer options. */
    @GetMapping("/subjects/{subjectId}/questions")
    public Page<QuestionResponse> listBySubject(@PathVariable String subjectId,
                                                @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return service.listBySubject(subjectId, pageable);
    }

    @GetMapping("/chapters/{chapterId}/questions")
    public Page<QuestionResponse> listByChapter(@PathVariable String chapterId,
                                                @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return service.listByChapter(chapterId, pageable);
    }

    @PostMapping("/subjects/{subjectId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@PathVariable String subjectId, @Valid @RequestBody QuestionRequest request) {
        return service.create(subjectId, request);
    }

    @GetMapping("/questions/{questionId}")
    public QuestionResponse get(@PathVariable String questionId) {
        return service.getWithAnswers(questionId);
    }

    @PutMapping("/questions/{questionId}")
    public QuestionResponse update(@PathVariable String questionId, @Valid @RequestBody QuestionRequest request) {
        return service.update(questionId, request);
    }

    @DeleteMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String questionId) {
        service.delete(questionId);
    }

    // ---- answers ---------------------------------------------------------------------------------------------

    @GetMapping("/questions/{questionId}/answers")
    public List<AnswerResponse> listAnswers(@PathVariable String questionId) {
        return service.listAnswers(questionId).stream().map(AnswerResponse::of).toList();
    }

    @PostMapping("/questions/{questionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerResponse addAnswer(@PathVariable String questionId, @Valid @RequestBody AnswerRequest request) {
        return AnswerResponse.of(service.addAnswer(questionId, request));
    }

    @GetMapping("/answers/{answerId}")
    public AnswerResponse getAnswer(@PathVariable String answerId) {
        return AnswerResponse.of(service.getAnswer(answerId));
    }

    @PutMapping("/answers/{answerId}")
    public AnswerResponse updateAnswer(@PathVariable String answerId, @Valid @RequestBody AnswerRequest request) {
        return AnswerResponse.of(service.updateAnswer(answerId, request));
    }

    @DeleteMapping("/answers/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(@PathVariable String answerId) {
        service.deleteAnswer(answerId);
    }
}
