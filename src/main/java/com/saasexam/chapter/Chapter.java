package com.saasexam.chapter;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_info")
public class Chapter {

    @Id
    @Column(name = "pk_chapter_id")
    private String id;

    @Column(name = "fk_org_id", nullable = false, updatable = false)
    private String orgId;

    @Column(name = "fk_class_id", nullable = false, updatable = false)
    private String classId;

    @Column(name = "fk_subject_id", nullable = false, updatable = false)
    private String subjectId;

    @Column(name = "chapter_name", nullable = false)
    private String name;

    @Column(name = "chapter_updated", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "chapter_status", nullable = false)
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
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
