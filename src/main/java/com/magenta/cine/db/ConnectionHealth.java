package com.magenta.cine.db;

/**
 * Resultado de una verificación de conectividad contra la base de datos.
 * Provee mensajes diferenciados para consola/logs y para la interfaz de usuario.
 */
public record ConnectionHealth(boolean success, String consoleMessage, String userMessage) {
}
