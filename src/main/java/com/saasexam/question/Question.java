package com.saasexam.question;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** A question within a subject, optionally filed under one of the subject's chapters. */
@Entity
@Table(name = "question_info")
public class Question {

    @Id
    @Column(name = "pk_question_id")
    private String id;

    @Column(name = "fk_org_id", nullable = false, updatable = false)
    private String orgId;

    @Column(name = "fk_class_id", nullable = false, updatable = false)
    private String classId;

    @Column(name = "fk_subject_id", nullable = false, updatable = false)
    private String subjectId;

    @Column(name = "fk_chapter_id")
    private String chapterId;

    /** The question text, or the image reference when {@link #type} is {@code image}. */
    @Column(name = "question_name", nullable = false, length = 10000)
    private String text;

    @Column(name = "question_type", nullable = false)
    private String type;

    @Column(name = "question_updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "question_updated", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "question_status", nullable = false)
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
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
