package com.saasexam.subject;

import com.saasexam.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class SubjectDtos {

    private SubjectDtos() {
    }

    public record SubjectRequest(
            @NotBlank @Size(max = 50) String name,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status) {
    }

    public record SubjectResponse(String id, String orgId, String classId, String name, String status,
                                  LocalDateTime updatedOn) {

        static SubjectResponse of(Subject s) {
            return new SubjectResponse(s.getId(), s.getOrgId(), s.getClassId(), s.getName(), s.getStatus(),
                    s.getUpdatedOn());
        }
    }
}
