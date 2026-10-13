package com.saasexam.common;

/**
 * A level of the school hierarchy that the catalog service owns. Rows in other services refer to these levels only
 * by id (columns {@code fk_<column>_id}); when the catalog deletes a node it tells the other services which level
 * and id went away.
 */
public enum HierarchyLevel {
    ORG("org"), CLASS("class"), SUBJECT("subject"), CHAPTER("chapter");

    private final String column;

    HierarchyLevel(String column) {
        this.column = column;
    }

    /** The column prefix used for this level, e.g. {@code class} for {@code fk_class_id}. */
    public String column() {
        return column;
    }
}
