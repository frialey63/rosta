package org.pjp.rosta.security;

public final class Session {

    public static String getUsername() {
        return "fred";
    }

    private Session() {
        // prevent instantation
    }
}
