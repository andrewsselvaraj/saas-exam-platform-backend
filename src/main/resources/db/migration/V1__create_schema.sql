-- H2 schema derived from sql/m2itssql_complete.sql (hierarchy tables only; user/admin/role tables are not included).
-- question_info / answer_info correspond to the dump's question_info_v_two / answer_info_v_two.
-- Status values: 'A' = active, 'I' = inactive, 'D' = deleted (soft delete).

CREATE TABLE org_info (
    pk_org_id      VARCHAR(50)  NOT NULL PRIMARY KEY,
    org_name       VARCHAR(50)  NOT NULL,
    org_email      VARCHAR(50)  NOT NULL,
    -- Kept for the future user/auth module; not exposed by the API.
    org_password   VARCHAR(50)  NOT NULL DEFAULT '',
    org_updated_by VARCHAR(50)  NOT NULL,
    org_updated_on TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    org_status     VARCHAR(50)  NOT NULL DEFAULT 'A'
);

CREATE TABLE class_info (
    pk_class_id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    fk_org_id     VARCHAR(50)  NOT NULL,
    class_name    VARCHAR(50)  NOT NULL,
    class_updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    class_status  VARCHAR(50)  NOT NULL DEFAULT 'A',
    CONSTRAINT class_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id)
);

CREATE TABLE subject_info (
    pk_subject_id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    fk_org_id       VARCHAR(50)  NOT NULL,
    fk_class_id     VARCHAR(50)  NOT NULL,
    subject_name    VARCHAR(50)  NOT NULL,
    subject_updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    subject_status  VARCHAR(50)  NOT NULL DEFAULT 'A',
    CONSTRAINT sub_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id),
    CONSTRAINT sub_class FOREIGN KEY (fk_class_id) REFERENCES class_info (pk_class_id)
);

CREATE TABLE chapter_info (
    pk_chapter_id   VARCHAR(50)  NOT NULL PRIMARY KEY,
    fk_org_id       VARCHAR(50)  NOT NULL,
    fk_class_id     VARCHAR(50)  NOT NULL,
    fk_subject_id   VARCHAR(50)  NOT NULL,
    chapter_name    VARCHAR(50)  NOT NULL,
    chapter_updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    chapter_status  VARCHAR(50)  NOT NULL DEFAULT 'A',
    CONSTRAINT chap_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id),
    CONSTRAINT chap_class FOREIGN KEY (fk_class_id) REFERENCES class_info (pk_class_id),
    CONSTRAINT chap_sub FOREIGN KEY (fk_subject_id) REFERENCES subject_info (pk_subject_id)
);

-- fk_chapter_id is nullable: most legacy questions belong directly to a subject.
CREATE TABLE question_info (
    pk_question_id      VARCHAR(50)    NOT NULL PRIMARY KEY,
    fk_org_id           VARCHAR(50)    NOT NULL,
    fk_class_id         VARCHAR(50)    NOT NULL,
    fk_subject_id       VARCHAR(50)    NOT NULL,
    fk_chapter_id       VARCHAR(50),
    question_name       VARCHAR(10000) NOT NULL,
    question_type       VARCHAR(50)    NOT NULL,
    question_updated_by VARCHAR(50)    NOT NULL,
    question_updated    TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    question_status     VARCHAR(50)    NOT NULL DEFAULT 'A',
    CONSTRAINT ques_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id),
    CONSTRAINT ques_class FOREIGN KEY (fk_class_id) REFERENCES class_info (pk_class_id),
    CONSTRAINT ques_sub FOREIGN KEY (fk_subject_id) REFERENCES subject_info (pk_subject_id),
    CONSTRAINT ques_chap FOREIGN KEY (fk_chapter_id) REFERENCES chapter_info (pk_chapter_id)
);

CREATE TABLE answer_info (
    pk_answer_id             VARCHAR(50)  NOT NULL PRIMARY KEY,
    fk_org_id                VARCHAR(50)  NOT NULL,
    fk_class_id              VARCHAR(50)  NOT NULL,
    fk_subject_id            VARCHAR(50)  NOT NULL,
    fk_chapter_id            VARCHAR(50),
    fk_question_id           VARCHAR(50)  NOT NULL,
    answers                  CLOB         NOT NULL,
    -- 'Yes' when this option is the correct answer, otherwise 'No'.
    correct_answer           VARCHAR(500) NOT NULL,
    answer_updated_by        VARCHAR(50)  NOT NULL,
    answer_updated_date_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    answer_status            VARCHAR(50)  NOT NULL DEFAULT 'A',
    CONSTRAINT ans_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id),
    CONSTRAINT ans_class FOREIGN KEY (fk_class_id) REFERENCES class_info (pk_class_id),
    CONSTRAINT ans_sub FOREIGN KEY (fk_subject_id) REFERENCES subject_info (pk_subject_id),
    CONSTRAINT ans_chap FOREIGN KEY (fk_chapter_id) REFERENCES chapter_info (pk_chapter_id),
    CONSTRAINT ans_ques FOREIGN KEY (fk_question_id) REFERENCES question_info (pk_question_id)
);

-- One row per answered question; rows sharing exam_session_id form one exam attempt.
-- exam_taken_by is a free-text user identifier until the user module exists.
CREATE TABLE exam_info (
    pk_exam_id                 VARCHAR(50)  NOT NULL PRIMARY KEY,
    exam_session_id            VARCHAR(50)  NOT NULL,
    fk_org_id                  VARCHAR(50)  NOT NULL,
    fk_class_id                VARCHAR(50)  NOT NULL,
    fk_subject_id              VARCHAR(50)  NOT NULL,
    fk_question_id             VARCHAR(50)  NOT NULL,
    fk_user_selected_answer_id VARCHAR(50)  NOT NULL,
    exam_taken_by              VARCHAR(50)  NOT NULL,
    exam_taken_date_time       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    exam_status                VARCHAR(50)  NOT NULL DEFAULT 'A',
    CONSTRAINT ex_org FOREIGN KEY (fk_org_id) REFERENCES org_info (pk_org_id),
    CONSTRAINT ex_class FOREIGN KEY (fk_class_id) REFERENCES class_info (pk_class_id),
    CONSTRAINT ex_subject FOREIGN KEY (fk_subject_id) REFERENCES subject_info (pk_subject_id),
    CONSTRAINT ex_question FOREIGN KEY (fk_question_id) REFERENCES question_info (pk_question_id),
    CONSTRAINT ex_answer FOREIGN KEY (fk_user_selected_answer_id) REFERENCES answer_info (pk_answer_id)
);

CREATE INDEX idx_class_org ON class_info (fk_org_id);
CREATE INDEX idx_subject_class ON subject_info (fk_class_id);
CREATE INDEX idx_chapter_subject ON chapter_info (fk_subject_id);
CREATE INDEX idx_question_subject ON question_info (fk_subject_id);
CREATE INDEX idx_question_chapter ON question_info (fk_chapter_id);
CREATE INDEX idx_answer_question ON answer_info (fk_question_id);
CREATE INDEX idx_exam_session ON exam_info (exam_session_id);
CREATE INDEX idx_exam_subject_taker ON exam_info (fk_subject_id, exam_taken_by);
