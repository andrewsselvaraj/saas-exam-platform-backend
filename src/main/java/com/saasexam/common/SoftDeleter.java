package com.saasexam.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Soft-deletes a node of the hierarchy together with everything below it. Every child table carries the ids of
 * all its ancestors (fk_org_id, fk_class_id, ...), so each level is a single UPDATE per descendant table.
 * Exam records are history and are left untouched.
 */
@Component
public class SoftDeleter {

    public enum Level {
        ORG("org", 0), CLASS("class", 1), SUBJECT("subject", 2), CHAPTER("chapter", 3), QUESTION("question", 4);

        final String prefix;
        final int depth;

        Level(String prefix, int depth) {
            this.prefix = prefix;
            this.depth = depth;
        }
    }

    /** Tables from top to bottom of the hierarchy: {table, status column prefix}. */
    private static final List<String[]> TABLES = List.of(
            new String[]{"org_info", "org"},
            new String[]{"class_info", "class"},
            new String[]{"subject_info", "subject"},
            new String[]{"chapter_info", "chapter"},
            new String[]{"question_info", "question"},
            new String[]{"answer_info", "answer"});

    private final JdbcTemplate jdbc;

    public SoftDeleter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void delete(Level level, String id) {
        for (int i = level.depth; i < TABLES.size(); i++) {
            String table = TABLES.get(i)[0];
            String statusColumn = TABLES.get(i)[1] + "_status";
            String idColumn = (i == level.depth ? "pk_" : "fk_") + level.prefix + "_id";
            jdbc.update("UPDATE " + table + " SET " + statusColumn + " = ? WHERE " + idColumn + " = ?",
                    Status.DELETED, id);
        }
    }
}
