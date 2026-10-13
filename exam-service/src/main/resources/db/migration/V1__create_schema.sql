-- Exam service schema. One row per answered question; rows sharing exam_session_id form one exam attempt.
-- The fk_* columns hold ids owned by the catalog and question services; they are not foreign keys.
-- exam_selected_correct records whether the chosen option was correct at submission time, so attempts stay
-- scoreable after questions or answers change. exam_taken_by is a free-text user identifier until the user
-- module exists.
CREATE TABLE exam_info (
    pk_exam_id                 VARCHAR(50)  NOT NULL PRIMARY KEY,
    exam_session_id            VARCHAR(50)  NOT NULL,
    fk_org_id                  VARCHAR(50)  NOT NULL,
    fk_class_id                VARCHAR(50)  NOT NULL,
    fk_subject_id              VARCHAR(50)  NOT NULL,
    fk_question_id             VARCHAR(50)  NOT NULL,
    fk_user_selected_answer_id VARCHAR(50)  NOT NULL,
    exam_selected_correct      BOOLEAN      NOT NULL,
    exam_taken_by              VARCHAR(50)  NOT NULL,
    exam_taken_date_time       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    exam_status                VARCHAR(50)  NOT NULL DEFAULT 'A'
);

CREATE INDEX idx_exam_session ON exam_info (exam_session_id);
CREATE INDEX idx_exam_subject_taker ON exam_info (fk_subject_id, exam_taken_by);
