package com.saasexam.exam;

import com.saasexam.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * One answered question of an exam attempt. All records of an attempt share {@link #sessionId}. Question, answer and
 * catalog ids point into other services and are not foreign keys; whether the answer was correct is stored here so
 * history stays scoreable after the question bank changes.
 */
@Entity
@Table(name = "exam_info")
public class ExamRecord {

    @Id
    @Column(name = "pk_exam_id")
    private String id;

    @Column(name = "exam_session_id", nullable = false)
    private String sessionId;

    @Column(name = "fk_org_id", nullable = false)
    private String orgId;

    @Column(name = "fk_class_id", nullable = false)
    private String classId;

    @Column(name = "fk_subject_id", nullable = false)
    private String subjectId;

    @Column(name = "fk_question_id", nullable = false)
    private String questionId;

    @Column(name = "fk_user_selected_answer_id", nullable = false)
    private String selectedAnswerId;

    /** Whether the chosen option was correct when the attempt was submitted. */
    @Column(name = "exam_selected_correct", nullable = false)
    private boolean selectedCorrect;

    @Column(name = "exam_taken_by", nullable = false)
    private String takenBy;

    @Column(name = "exam_taken_date_time", nullable = false)
    private LocalDateTime takenOn;

    @Column(name = "exam_status", nullable = false)
    private String status = Status.ACTIVE;

    @PrePersist
    void stamp() {
        if (takenOn == null) takenOn = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getSelectedAnswerId() { return selectedAnswerId; }
    public void setSelectedAnswerId(String selectedAnswerId) { this.selectedAnswerId = selectedAnswerId; }
    public boolean isSelectedCorrect() { return selectedCorrect; }
    public void setSelectedCorrect(boolean selectedCorrect) { this.selectedCorrect = selectedCorrect; }
    public String getTakenBy() { return takenBy; }
    public void setTakenBy(String takenBy) { this.takenBy = takenBy; }
    public LocalDateTime getTakenOn() { return takenOn; }
    public void setTakenOn(LocalDateTime takenOn) { this.takenOn = takenOn; }
    public String getStatus() { return status; }
}
