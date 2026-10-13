-- Catalog service schema, derived from sql/m2itssql_complete.sql (hierarchy tables only; user/admin/role tables are
-- not included). Questions, answers and exams live in their own services and refer to these rows by id only.
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

CREATE INDEX idx_class_org ON class_info (fk_org_id);
CREATE INDEX idx_subject_class ON subject_info (fk_class_id);
CREATE INDEX idx_chapter_subject ON chapter_info (fk_subject_id);
