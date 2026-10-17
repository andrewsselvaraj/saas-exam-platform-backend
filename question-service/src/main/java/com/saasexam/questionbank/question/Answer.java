package com.saasexam.questionbank.question;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One answer option of a question. A question may have several correct options. */
@Entity
@Table(name = "answer_info_v_two")
public class Answer {

    static final String CORRECT = "Yes";
    static final String WRONG = "No";

    @Id
    @Column(name = "pk_answer_id")
    private String id;

    @Column(name = "fk_org_id", nullable = false, updatable = false)
    private String orgId;

    @Column(name = "fk_class_id", nullable = false, updatable = false)
    private String classId;

    @Column(name = "fk_subject_id", nullable = false, updatable = false)
    private String subjectId;

    @Column(name = "fk_chapter_id")
    private String chapterId;

    @Column(name = "fk_question_id", nullable = false, updatable = false)
    private String questionId;

    @Lob
    @Column(name = "answers", nullable = false)
    private String text;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(name = "answer_updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "answer_updated_date_time", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "answer_status", nullable = false)
    private String status = Status.ACTIVE;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedOn = LocalDateTime.now();
    }

    /** Copies the ancestor ids from the owning question so the denormalised columns stay consistent. */
    void attachTo(Question question) {
        orgId = question.getOrgId();
        classId = question.getClassId();
        subjectId = question.getSubjectId();
        chapterId = question.getChapterId();
        questionId = question.getId();
    }

    public boolean isCorrect() {
        return CORRECT.equalsIgnoreCase(correctAnswer);
    }

    public void setCorrect(boolean correct) {
        correctAnswer = correct ? CORRECT : WRONG;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrgId() { return orgId; }
    public String getClassId() { return classId; }
    public String getSubjectId() { return subjectId; }
    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public String getQuestionId() { return questionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
