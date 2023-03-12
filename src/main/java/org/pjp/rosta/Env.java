package org.pjp.rosta;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Env {

    private static final Logger LOGGER = LoggerFactory.getLogger(Env.class);

    public static String ROSTA_DATA;

    static {
        String rostaData = System.getenv("ROSTA_DATA");

        LOGGER.info("ROSTA_DATA = {}", rostaData);

        ROSTA_DATA = Strings.isBlank(rostaData) ? "." : rostaData;
    }

    private Env() {
        // prevent instantiation
    }
}
