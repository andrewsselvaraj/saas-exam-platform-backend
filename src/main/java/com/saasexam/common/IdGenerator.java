package com.saasexam.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates 13-character hex ids in the same shape as PHP's {@code uniqid()}, which produced every id in the
 * legacy data (8 hex digits of epoch seconds followed by 5 hex digits of microseconds). Ids are strictly
 * increasing within this JVM.
 */
public final class IdGenerator {

    private static final AtomicLong LAST_MICROS = new AtomicLong();

    private IdGenerator() {
    }

    public static String next() {
        long now = System.currentTimeMillis() * 1000;
        long micros = LAST_MICROS.updateAndGet(last -> Math.max(last + 1, now));
        return String.format("%08x%05x", micros / 1_000_000, micros % 1_000_000);
    }
}
