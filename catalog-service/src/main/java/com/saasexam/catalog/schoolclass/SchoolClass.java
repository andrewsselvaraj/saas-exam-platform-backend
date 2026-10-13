package com.saasexam.catalog.schoolclass;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** A class (grade / course group) within an organisation. */
@Entity
@Table(name = "class_info")
public class SchoolClass {

    @Id
    @Column(name = "pk_class_id")
    private String id;

    @Column(name = "fk_org_id", nullable = false, updatable = false)
    private String orgId;

    @Column(name = "class_name", nullable = false)
    private String name;

    @Column(name = "class_updated", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "class_status", nullable = false)
    private String status = Status.ACTIVE;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedOn = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
