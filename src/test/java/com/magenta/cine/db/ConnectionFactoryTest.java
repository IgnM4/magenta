package com.magenta.cine.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionFactoryTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.pass");
        System.clearProperty("db.connectTimeoutSeconds");
    }

    @Test
    void shouldPreferSystemPropertiesOverDefaults() {
        System.setProperty("db.url", "jdbc:mysql://example.org:3307/magenta");
        System.setProperty("db.user", "alice");
        System.setProperty("db.pass", "secret");

        ConnectionFactory.DbConfig config = ConnectionFactory.resolveConfig();

        assertEquals("jdbc:mysql://example.org:3307/magenta", config.url());
        assertEquals("alice", config.user());
        assertEquals("secret", config.password());
        assertEquals("jdbc:mysql://example.org:3307/magenta", config.sanitizedUrl());
        assertEquals("example.org:3307/magenta", config.hostDescription());
    }

    @Test
    void sanitizedUrlShouldRemoveCredentials() {
        System.setProperty("db.url", "jdbc:mysql://user:pass@localhost:3307/magenta?allowPublicKeyRetrieval=true");

        ConnectionFactory.DbConfig config = ConnectionFactory.resolveConfig();

        assertEquals("jdbc:mysql://localhost:3307/magenta", config.sanitizedUrl());
        assertEquals("localhost:3307/magenta", config.hostDescription());
    }

    @Test
    void emptyPasswordShouldBeRespected() {
        System.setProperty("db.pass", "   ");

        ConnectionFactory.DbConfig config = ConnectionFactory.resolveConfig();

        assertEquals("", config.password());
    }

    @Test
    void testConnectionFailureReturnsDiagnosticMessage() {
        System.setProperty("db.url", "jdbc:mysql://127.0.0.1:65000/magenta?connectTimeout=1000");
        System.setProperty("db.user", "user");
        System.setProperty("db.pass", "pwd");

        ConnectionHealth health = ConnectionFactory.testConnection();

        assertFalse(health.success());
        assertTrue(health.consoleMessage().contains("jdbc:mysql://127.0.0.1:65000/magenta"));
        assertTrue(health.userMessage().startsWith("No fue posible conectar"));
    }

    @Test
    void defaultConnectTimeoutShouldBeThreeSeconds() {
        Duration timeout = ConnectionFactory.resolveConnectTimeout();

        assertEquals(Duration.ofSeconds(3), timeout);
    }

    @Test
    void invalidConnectTimeoutFallsBackToDefault() {
        System.setProperty("db.connectTimeoutSeconds", "abc");

        Duration timeout = ConnectionFactory.resolveConnectTimeout();

        assertEquals(Duration.ofSeconds(3), timeout);
    }

    @Test
    void nonPositiveConnectTimeoutFallsBackToDefault() {
        System.setProperty("db.connectTimeoutSeconds", "0");

        Duration timeout = ConnectionFactory.resolveConnectTimeout();

        assertEquals(Duration.ofSeconds(3), timeout);
    }

    @Test
    void customConnectTimeoutIsApplied() {
        System.setProperty("db.connectTimeoutSeconds", "7");

        Duration timeout = ConnectionFactory.resolveConnectTimeout();

        assertEquals(Duration.ofSeconds(7), timeout);
    }
}
