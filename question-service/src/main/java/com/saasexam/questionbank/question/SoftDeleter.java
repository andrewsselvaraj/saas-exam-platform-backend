package com.saasexam.questionbank.question;

import com.saasexam.common.HierarchyLevel;
import com.saasexam.common.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Soft-deletes questions together with their answers. Both tables carry the ids of all catalog ancestors, so the
 * questions under a deleted org, class, subject or chapter are a single UPDATE per table.
 */
@Component
public class SoftDeleter {

    private final JdbcTemplate jdbc;

    public SoftDeleter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Deletes one question and its answers. */
    public void deleteQuestion(String questionId) {
        update("question_info", "question_status", "pk_question_id", questionId);
        update("answer_info", "answer_status", "fk_question_id", questionId);
    }

    /** Deletes every question and answer filed under a catalog node that was deleted. */
    public void deleteBelow(HierarchyLevel level, String id) {
        String column = "fk_" + level.column() + "_id";
        update("question_info", "question_status", column, id);
        update("answer_info", "answer_status", column, id);
    }

    private void update(String table, String statusColumn, String idColumn, String id) {
        jdbc.update("UPDATE " + table + " SET " + statusColumn + " = ? WHERE " + idColumn + " = ?", Status.DELETED, id);
    }
}
