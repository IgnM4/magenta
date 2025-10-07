-- Script para crear la base de datos y el usuario de la aplicación en MySQL (Windows/macOS/Linux)
-- Ejecutar como root o con privilegios suficientes.

CREATE DATABASE IF NOT EXISTS magenta CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'app_admin'@'%' IDENTIFIED BY 'AppAdmin#3307';
GRANT ALL PRIVILEGES ON magenta.* TO 'app_admin'@'%';
FLUSH PRIVILEGES;

-- Nota: en sistemas donde se requiera autenticación por plugin diferente, ajustar IDENTIFIED BY/PLUGIN.
