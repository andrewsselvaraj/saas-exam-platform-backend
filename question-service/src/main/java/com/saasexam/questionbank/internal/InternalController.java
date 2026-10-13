package com.saasexam.questionbank.internal;

import com.saasexam.common.HierarchyLevel;
import com.saasexam.questionbank.question.QuestionCount;
import com.saasexam.questionbank.question.QuestionDtos.QuestionResponse;
import com.saasexam.questionbank.question.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service API used by the catalog and exam services. The gateway does not route {@code /internal/**},
 * so these endpoints are reachable only from inside the deployment.
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

    public record HierarchyDeletion(@NotNull HierarchyLevel level, @NotBlank String id) {
    }

    private final QuestionService service;

    public InternalController(QuestionService service) {
        this.service = service;
    }

    /** Live question counts of an organisation per (subject, chapter), for the catalog's tree view. */
    @GetMapping("/question-counts")
    public List<QuestionCount> questionCounts(@RequestParam String orgId) {
        return service.countByOrg(orgId);
    }

    /** The catalog deleted a node: delete the questions and answers filed under it. */
    @PostMapping("/hierarchy-deletions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hierarchyDeleted(@Valid @RequestBody HierarchyDeletion deletion) {
        service.deleteBelow(deletion.level(), deletion.id());
    }

    /** Live questions with their live answers; unknown or deleted ids are left out. Used to score exam attempts. */
    @GetMapping("/questions")
    public List<QuestionResponse> questions(@RequestParam List<String> ids) {
        return service.findLive(ids);
    }
}
