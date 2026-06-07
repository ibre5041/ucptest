package com.foobar.sharddemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

final class OracleTracing {

    private OracleTracing() {
    }

    static void enable() {
        // UCP diagnosability.
        //
        // Logging is always enabled.
        // Ring-buffer tracing is opt-in because some Oracle/UCP combinations
        // can fail while initializing the trace actor/thread.
        System.setProperty("oracle.ucp.diagnostic.enableLogging", "true");
        System.setProperty("oracle.ucp.diagnostic.loggingLevel", "FINEST");
        System.setProperty("oracle.ucp.diagnostic.bufferSize", "4096");
        System.setProperty("oracle.ucp.diagnostic.enableTrace",
                Boolean.getBoolean("demo.enable.ucp.trace") ? "true" : "false");

        // Oracle JDBC diagnosability.
        System.setProperty("oracle.jdbc.diagnostic.enableLogging", "true");
        System.setProperty("oracle.jdbc.Trace", "true");

        // Make sure JUL is configured before any pool/loggers emit records.
        try (InputStream in = OracleTracing.class.getResourceAsStream("/oracle-logging.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource /oracle-logging.properties");
            }
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load oracle-logging.properties", e);
        }
    }
}
