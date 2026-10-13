-- Question service schema. question_info / answer_info correspond to the dump's question_info_v_two /
-- answer_info_v_two. The fk_org_id / fk_class_id / fk_subject_id / fk_chapter_id columns hold catalog-service ids:
-- they are not foreign keys, because those tables live in the catalog's database.
-- Status values: 'A' = active, 'I' = inactive, 'D' = deleted (soft delete).

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
    question_status     VARCHAR(50)    NOT NULL DEFAULT 'A'
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
    CONSTRAINT ans_ques FOREIGN KEY (fk_question_id) REFERENCES question_info (pk_question_id)
);

CREATE INDEX idx_question_org ON question_info (fk_org_id);
CREATE INDEX idx_question_class ON question_info (fk_class_id);
CREATE INDEX idx_question_subject ON question_info (fk_subject_id);
CREATE INDEX idx_question_chapter ON question_info (fk_chapter_id);
CREATE INDEX idx_answer_question ON answer_info (fk_question_id);
CREATE INDEX idx_answer_org ON answer_info (fk_org_id);
CREATE INDEX idx_answer_class ON answer_info (fk_class_id);
CREATE INDEX idx_answer_subject ON answer_info (fk_subject_id);
CREATE INDEX idx_answer_chapter ON answer_info (fk_chapter_id);
