package com.saasexam.catalog.schoolclass;

import com.saasexam.common.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class SchoolClassDtos {

    private SchoolClassDtos() {
    }

    public record ClassRequest(
            @NotBlank @Size(max = 50) String name,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status) {
    }

    public record ClassResponse(String id, String orgId, String name, String status, LocalDateTime updatedOn) {

        static ClassResponse of(SchoolClass c) {
            return new ClassResponse(c.getId(), c.getOrgId(), c.getName(), c.getStatus(), c.getUpdatedOn());
        }
    }
}
