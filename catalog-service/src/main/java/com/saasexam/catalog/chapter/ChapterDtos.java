package com.saasexam.catalog.chapter;

import com.saasexam.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ChapterDtos {

    private ChapterDtos() {
    }

    public record ChapterRequest(
            @NotBlank @Size(max = 50) String name,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status) {
    }

    public record ChapterResponse(String id, String orgId, String classId, String subjectId, String name,
                                  String status, LocalDateTime updatedOn) {

        static ChapterResponse of(Chapter c) {
            return new ChapterResponse(c.getId(), c.getOrgId(), c.getClassId(), c.getSubjectId(), c.getName(),
                    c.getStatus(), c.getUpdatedOn());
        }
    }
}
