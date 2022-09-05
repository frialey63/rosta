package org.pjp.rosta.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class CrunchifyRandomPasswordGeneratorTest {

    @Test
    void testGeneratePasswordEmpty() {
        CrunchifyRandomPasswordGenerator generator = new CrunchifyRandomPasswordGenerator(0);

        String password = generator.generatePassword(0);

        assertThat(password, is(emptyString()));
    }

    @Test
    void testGeneratePassword8() {
        CrunchifyRandomPasswordGenerator generator = new CrunchifyRandomPasswordGenerator(0);

        String password = generator.generatePassword(8);

        assertThat(password.length(), is(8));
    }

}
