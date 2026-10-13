package com.saasexam.catalog;

import com.saasexam.catalog.client.QuestionBankClient;
import com.saasexam.common.HierarchyLevel;
import com.saasexam.common.Status;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Soft-deletes a node of the hierarchy together with everything below it. Every child table carries the ids of
 * all its ancestors (fk_org_id, fk_class_id, ...), so each level is a single UPDATE per descendant table.
 * The question service is then told to delete the questions and answers filed under the node; if that call fails
 * the exception rolls back the caller's transaction. Exam records are history and are left untouched.
 */
@Component
public class SoftDeleter {

    /** Catalog tables from top to bottom of the hierarchy: {table, status column prefix}. */
    private static final List<String[]> TABLES = List.of(
            new String[]{"org_info", "org"},
            new String[]{"class_info", "class"},
            new String[]{"subject_info", "subject"},
            new String[]{"chapter_info", "chapter"});

    private final JdbcTemplate jdbc;
    private final QuestionBankClient questionBank;

    public SoftDeleter(JdbcTemplate jdbc, QuestionBankClient questionBank) {
        this.jdbc = jdbc;
        this.questionBank = questionBank;
    }

    public void delete(HierarchyLevel level, String id) {
        for (int i = level.ordinal(); i < TABLES.size(); i++) {
            String table = TABLES.get(i)[0];
            String statusColumn = TABLES.get(i)[1] + "_status";
            String idColumn = (i == level.ordinal() ? "pk_" : "fk_") + level.column() + "_id";
            jdbc.update("UPDATE " + table + " SET " + statusColumn + " = ? WHERE " + idColumn + " = ?",
                    Status.DELETED, id);
        }
        questionBank.deleteBelow(level, id);
    }
}
