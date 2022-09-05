package org.pjp.rosta.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class UuidStrTest {

    /**
     * Regular expression, in {@link String} form, to match a Universally Unique Identifier (UUID), in
     * a case-insensitive fashion.
     */
    public static final String UUID_STRING =
        "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    /**
     * Regular expression to match any Universally Unique Identifier (UUID), in a case-insensitive
     * fashion.
     */
    public static final Pattern UUID = Pattern.compile(UUID_STRING);
    /**
     * Regular expression, in {@link String} form, to match a Version 4 Universally Unique Identifier
     * (UUID), in a case-insensitive fashion.
     */
    public static final String UUID_V4_STRING =
        "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-[89abAB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}";
    /**
     * Regular expression to match a Version 4 Universally Unique Identifier (UUID), in a
     * case-insensitive fashion.
     */
    public static final Pattern UUID_V4 = Pattern.compile(UUID_V4_STRING);

    @Test
    void testRandom() {
        String uuidStr = UuidStr.random();

        assertNotNull(uuidStr);
        assertTrue(UUID_V4.matcher(uuidStr).matches());
    }

}
