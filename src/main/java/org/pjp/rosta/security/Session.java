package org.pjp.rosta.security;

public final class Session {

    public static String getUsername() {
        return "Fred";
    }

    private Session() {
        // prevent instantation
    }
}
