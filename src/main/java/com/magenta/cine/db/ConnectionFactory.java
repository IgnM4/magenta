
package com.magenta.cine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(ConnectionFactory.class.getName());
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/magenta?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "ignacio";
    private static final String DEFAULT_PASS = "ClaveFuerte#2025!";
    private static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 3L;
    private static final int MIN_CONNECT_TIMEOUT_MILLIS = 500;

    private ConnectionFactory() {}

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver MySQL no encontrado", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        DbConfig config = resolveConfig();
        Duration connectTimeout = resolveConnectTimeoutDuration();
        int timeoutMillis = toMillis(connectTimeout);
        Properties props = new Properties();
        props.setProperty("user", config.user());
        props.setProperty("password", config.password());
        props.setProperty("connectTimeout", String.valueOf(timeoutMillis));
        props.setProperty("socketTimeout", String.valueOf(timeoutMillis));
        LOGGER.fine(() -> "Abriendo conexión a " + config.sanitizedUrl());
        return DriverManager.getConnection(config.url(), props);
    }

    static DbConfig resolveConfig() {
        String url = choose(DEFAULT_URL, System.getProperty("db.url"), System.getenv("DB_URL"));
        String user = choose(DEFAULT_USER, System.getProperty("db.user"), System.getenv("DB_USER"));
        String pass = chooseAllowingEmpty(DEFAULT_PASS,
                System.getProperty("db.pass"),
                System.getenv("DB_PASS"));
        return new DbConfig(url, user, pass);
    }

    public static ConnectionHealth testConnection() {
        DbConfig config = resolveConfig();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
            statement.execute();
            String console = "Conexión exitosa a " + config.sanitizedUrl();
            String userMessage = "Conexión establecida con " + config.hostDescription();
            return new ConnectionHealth(true, console, userMessage);
        } catch (SQLException ex) {
            String console = "No fue posible conectar a " + config.sanitizedUrl() + ". Detalle: " + ex.getMessage();
            LOGGER.log(Level.WARNING, console, ex);
            String userMessage = "No fue posible conectar a la base de datos en " + config.hostDescription()
                    + ". Verifica disponibilidad, credenciales o el archivo schema.sql.";
            return new ConnectionHealth(false, console, userMessage);
        }
    }

    private static String choose(String defaultValue, String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return defaultValue;
    }

    private static String chooseAllowingEmpty(String defaultValue, String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null) {
                String trimmed = candidate.trim();
                if (!trimmed.isEmpty()) {
                    return trimmed;
                }
                return "";
            }
        }
        return defaultValue;
    }

    static Duration resolveConnectTimeout() {
        return resolveConnectTimeoutDuration();
    }

    private static int toMillis(Duration duration) {
        long millis = duration.toMillis();
        if (millis < MIN_CONNECT_TIMEOUT_MILLIS) {
            millis = MIN_CONNECT_TIMEOUT_MILLIS;
        }
        return (int) Math.min(Integer.MAX_VALUE, millis);
    }

    static record DbConfig(String url, String user, String password) {
        String sanitizedUrl() {
            if (url == null) {
                return "jdbc:mysql://";
            }
            int params = url.indexOf('?');
            String base = params >= 0 ? url.substring(0, params) : url;
            return base.replaceAll("(?<=//)[^/@]*@", "");
        }

        String hostDescription() {
            String sanitized = sanitizedUrl();
            int scheme = sanitized.indexOf("//");
            String hostDb = scheme >= 0 ? sanitized.substring(scheme + 2) : sanitized;
            if (hostDb.isBlank()) {
                return "la base de datos configurada";
            }
            return hostDb;
        }
    }

    private static Duration resolveConnectTimeoutDuration() {
        String override = choose(null,
                System.getProperty("db.connectTimeoutSeconds"),
                System.getenv("DB_CONNECT_TIMEOUT_SECONDS"));
        if (override != null) {
            try {
                long seconds = Long.parseLong(override);
                if (seconds > 0) {
                    return Duration.ofSeconds(seconds);
                }
                LOGGER.warning(() -> "El tiempo de espera de conexión debe ser positivo. Valor recibido: \""
                        + override + "\". Se utilizará el valor por defecto de "
                        + DEFAULT_CONNECT_TIMEOUT_SECONDS + " segundos.");
            } catch (NumberFormatException ex) {
                LOGGER.warning(() -> "Valor inválido para db.connectTimeoutSeconds/DB_CONNECT_TIMEOUT_SECONDS: \""
                        + override + "\". Se utilizará el valor por defecto de "
                        + DEFAULT_CONNECT_TIMEOUT_SECONDS + " segundos.");
            }
        }
        return Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS);
    }
}
