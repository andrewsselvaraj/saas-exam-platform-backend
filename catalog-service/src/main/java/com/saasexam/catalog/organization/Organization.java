package com.saasexam.catalog.organization;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** A school or other organisation: the root of the hierarchy. */
@Entity
@Table(name = "org_info")
public class Organization {

    @Id
    @Column(name = "pk_org_id")
    private String id;

    @Column(name = "org_name", nullable = false)
    private String name;

    @Column(name = "org_email", nullable = false)
    private String email;

    @Column(name = "org_updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "org_updated_on", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "org_status", nullable = false)
    private String status = Status.ACTIVE;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedOn = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
