package org.pjp.rosta.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Crunchify.com
 * Best way to generate very secure random Password automatically
 */
public final class CrunchifyRandomPasswordGenerator {

    // SecureRandom() constructs a secure random number generator (RNG) implementing the default random number algorithm.
    private final SecureRandom crunchifyRandomNo = new SecureRandom();

    private final List<Character> crunchifyValueObj = new ArrayList<>();

    private boolean special = true;

    private boolean numeric = true;

    private boolean uppercase = true;

    private boolean lowercase = true;

    /*
     *  Just initialize ArrayList crunchifyValueObj and add ASCII Decimal Values
     */
    public CrunchifyRandomPasswordGenerator(int rotate) {
        if (special) {
            // Adding special ASCII Decimal value between 33 and 47
            for (int i = 33; i < 48; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (numeric) {
            // Adding numeric ASCII Decimal value between 48 and 58
            for (int i = 48; i < 58; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (special) {
            // Adding special ASCII Decimal value between 58 and 64
            for (int i = 58; i < 65; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (uppercase) {
            // Adding uppercase ASCII Decimal value between 65 and 90
            for (int i = 65; i < 91; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (special) {
            // Adding special ASCII Decimal value between 91 and 96
            for (int i = 91; i < 97; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (lowercase) {
            // Adding lowercase ASCII Decimal value between 97 and 122
            for (int i = 97; i < 123; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        if (special) {
            // Adding special ASCII Decimal value between 123 and 127
            for (int i = 123; i < 127; i++) {
                crunchifyValueObj.add((char) i);
            }
        }

        // rotate() rotates the elements in the specified list by the specified distance. This will create strong password
        Collections.rotate(crunchifyValueObj, rotate);
    }

    public static void main(String[] args) {
        CrunchifyRandomPasswordGenerator passwordGenerator = new CrunchifyRandomPasswordGenerator(5);

        // Let's print total 8 passwords
        for (int loop = 1; loop <= 8; loop++) {
            System.out.println(passwordGenerator.generatePassword(8));
        }
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(crunchifyGetRandom());
        }

        return password.toString();
    }

    // Get Char value from above added Decimal values
    private char crunchifyGetRandom() {
        return crunchifyValueObj.get(crunchifyRandomNo.nextInt(crunchifyValueObj.size()));
    }

}