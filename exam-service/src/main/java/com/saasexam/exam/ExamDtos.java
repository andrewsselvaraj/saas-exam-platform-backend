package com.saasexam.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class ExamDtos {

    private ExamDtos() {
    }

    /** One completed exam attempt: the option chosen for each answered question of the subject. */
    public record ExamSubmission(
            @NotBlank @Size(max = 50) String subjectId,
            @NotBlank @Size(max = 50) String takenBy,
            @NotEmpty List<@Valid Choice> choices) {
    }

    public record Choice(@NotBlank String questionId, @NotBlank String answerId) {
    }

    public record ExamResult(String sessionId, String orgId, String classId, String subjectId, String takenBy,
                             LocalDateTime takenOn, int totalQuestions, int correctCount, double scorePercent,
                             List<ChoiceResult> choices) {
    }

    public record ChoiceResult(String questionId, String selectedAnswerId, boolean correct) {
    }

    public record ExamSummary(String sessionId, String subjectId, String takenBy, LocalDateTime takenOn,
                              int totalQuestions, int correctCount, double scorePercent) {
    }
}
