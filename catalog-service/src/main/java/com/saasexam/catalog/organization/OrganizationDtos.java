package com.saasexam.catalog.organization;

import com.saasexam.common.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class OrganizationDtos {

    private OrganizationDtos() {
    }

    /** {@code status} is optional: omitted on create means active, omitted on update means unchanged. */
    public record OrganizationRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Email @Size(max = 50) String email,
            @Size(max = 50) String updatedBy,
            @Pattern(regexp = Status.SETTABLE_PATTERN) String status) {
    }

    public record OrganizationResponse(String id, String name, String email, String status,
                                       String updatedBy, LocalDateTime updatedOn) {

        static OrganizationResponse of(Organization o) {
            return new OrganizationResponse(o.getId(), o.getName(), o.getEmail(), o.getStatus(),
                    o.getUpdatedBy(), o.getUpdatedOn());
        }
    }
}
