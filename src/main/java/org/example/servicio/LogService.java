package org.example.servicio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LogService {

    private static final Logger LOGGER = Logger.getLogger("org.example.POSpanaderia");

    static {
        try {
            Path logs = Path.of("logs");
            Files.createDirectories(logs);
            FileHandler handler = new FileHandler(logs.resolve("pospanaderia-%g.log").toString(), 1_000_000, 5, true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(true);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo inicializar el archivo de logs", e);
        }
    }

    private LogService() {
    }

    public static void error(String mensaje, Throwable error) {
        LOGGER.log(Level.SEVERE, mensaje, error);
    }

    public static void warn(String mensaje, Throwable error) {
        LOGGER.log(Level.WARNING, mensaje, error);
    }
}
