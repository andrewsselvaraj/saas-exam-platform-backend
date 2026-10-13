package com.saasexam.common;

/** Values of the {@code *_status} columns. Rows are never physically deleted; they are marked {@link #DELETED}. */
public final class Status {

    public static final String ACTIVE = "A";
    public static final String INACTIVE = "I";
    public static final String DELETED = "D";

    /** Validation pattern for statuses a client may set directly (deletion goes through DELETE endpoints). */
    public static final String SETTABLE_PATTERN = "[AI]";

    private Status() {
    }
}
