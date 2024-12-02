package org.samo_lego.fabrictailor.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.fabrictailor.FabricTailor;

public class Logging {
    public static final Logger LOGGER = LogManager.getLogger();
    private static final String PREFIX = "[FabricTailor]";

    public static void error(String error) {
        LOGGER.error("{} An error occurred: {}", PREFIX, error);
    }

    public static void debug(String message) {
        if (FabricTailor.config.logging.debug) {
            LOGGER.info("{} Debug: {}", PREFIX, message);
        }
    }
}
