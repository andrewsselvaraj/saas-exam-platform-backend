package com.saasexam.questionbank.question;

import com.saasexam.common.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class QuestionDtos {

    private QuestionDtos() {
    }

    /**
     * Create or replace a question. {@code chapterId} must be a chapter of the same subject, or null for none.
     * {@code answers}: on create, the initial options; on update, null keeps the existing options and a list
     * replaces them. {@code status}: null means active on create and unchanged on update.
     */
    public record QuestionRequest(
            @Size(max = 50) String chapterId,
            @NotBlank @Size(max = 10000) String text,
            @NotBlank @Pattern(regexp = "text|image") String type,
            @Size(max = 50) String updatedBy,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status,
            List<@Valid AnswerRequest> answers) {
    }

    public record AnswerRequest(
            @NotBlank String text,
            boolean correct,
            @Size(max = 50) String updatedBy,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status) {
    }

    public record QuestionResponse(String id, String orgId, String classId, String subjectId, String chapterId,
                                   String text, String type, String status, String updatedBy,
                                   LocalDateTime updatedOn, List<AnswerResponse> answers) {

        static QuestionResponse of(Question q, List<Answer> answers) {
            return new QuestionResponse(q.getId(), q.getOrgId(), q.getClassId(), q.getSubjectId(), q.getChapterId(),
                    q.getText(), q.getType(), q.getStatus(), q.getUpdatedBy(), q.getUpdatedOn(),
                    answers.stream().map(AnswerResponse::of).toList());
        }
    }

    public record AnswerResponse(String id, String questionId, String text, boolean correct, String status,
                                 String updatedBy, LocalDateTime updatedOn) {

        static AnswerResponse of(Answer a) {
            return new AnswerResponse(a.getId(), a.getQuestionId(), a.getText(), a.isCorrect(), a.getStatus(),
                    a.getUpdatedBy(), a.getUpdatedOn());
        }
    }
}
