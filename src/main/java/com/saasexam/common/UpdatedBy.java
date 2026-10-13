package com.saasexam.common;

/** Resolves the {@code *_updated_by} audit value. Until the user module exists, clients may pass it explicitly. */
public final class UpdatedBy {

    public static final String SYSTEM = "system";

    private UpdatedBy() {
    }

    public static String orSystem(String updatedBy) {
        return updatedBy == null || updatedBy.isBlank() ? SYSTEM : updatedBy.trim();
    }
}
