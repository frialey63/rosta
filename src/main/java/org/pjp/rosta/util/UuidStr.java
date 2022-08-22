package org.pjp.rosta.util;

import java.util.UUID;

public final class UuidStr {

    public static String getNew() {
        return UUID.randomUUID().toString();
    }

    private UuidStr() {
        // prevent instantiation
    }
}
