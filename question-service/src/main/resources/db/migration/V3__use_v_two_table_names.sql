-- Use the live MySQL table names (question_info_v_two / answer_info_v_two) so the same entities work on H2 and
-- on the existing m2its database. Flyway only runs on H2; it is turned off against MySQL.
ALTER TABLE question_info RENAME TO question_info_v_two;
ALTER TABLE answer_info RENAME TO answer_info_v_two;
