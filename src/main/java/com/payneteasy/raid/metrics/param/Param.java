package com.payneteasy.raid.metrics.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.payneteasy.raid.metrics.util.Strings.hasText;

public enum Param {

    STORCLI64_BIN_PATH      ("/opt/MegaRAID/storcli/storcli64"),
    STORCLI64_WORKING_DIR   ("/opt/MegaRAID/storcli"),
    SHOW_ALL_DEVICE         ("/c0/v0"), // /c0/v239
    SHOW_TEMPERATURE_DEVICE ("/c0"),
    METRICS_PORT            ("9093"),
    METRICS_THREADS_COUNT   ("2"),
    HTTP_THREADS_COUNT      ("10"),
    ;

    private static final Logger LOG = LoggerFactory.getLogger(Param.class);

    private final String defaultValue;

    Param(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValue() {
        String name = name();

        {
            String value = System.getProperty(name);
            if (hasText(value)) {
                return value;
            }
        }

        {
            String value = System.getenv(name);
            if (hasText(value)) {
                return value;
            }
        }

        return defaultValue;
    }

    public int getIntValue() {
        return Integer.parseInt(getValue());
    }

    public static void logValues() {
        LOG.info("Using values:");
        for (Param param : values()) {
            LOG.info("    {} = {}", param, param.getValue());
        }
    }
}
