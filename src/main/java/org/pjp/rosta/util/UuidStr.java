package org.pjp.rosta.util;

import java.util.UUID;

public final class UuidStr {

    public static String random() {
        return UUID.randomUUID().toString();
    }

    private UuidStr() {
        // prevent instantiation
    }
}
