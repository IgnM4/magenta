
CREATE DATABASE IF NOT EXISTS magenta;
USE magenta;

CREATE TABLE IF NOT EXISTS genero (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS clasificacion (
  id INT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pelicula (
  id INT PRIMARY KEY AUTO_INCREMENT,
  titulo VARCHAR(150) NOT NULL,
  director VARCHAR(120) NOT NULL,
  genero_id INT NOT NULL,
  anio INT NOT NULL,
  duracion_min INT NOT NULL,
  clasificacion_id INT NOT NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pelicula_genero FOREIGN KEY (genero_id) REFERENCES genero(id),
  CONSTRAINT fk_pelicula_clasif FOREIGN KEY (clasificacion_id) REFERENCES clasificacion(id)
);

CREATE UNIQUE INDEX uq_pelicula_titulo ON pelicula(titulo);
CREATE INDEX idx_pelicula_anio ON pelicula(anio);
CREATE INDEX idx_pelicula_genero_id ON pelicula(genero_id);
CREATE INDEX idx_pelicula_clasif_id ON pelicula(clasificacion_id);

-- Ajustes S8: bandera de estado y portada por película (compatibles con MySQL < 8.0)
SET @sql := IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pelicula' AND COLUMN_NAME = 'activo') = 0,
  'ALTER TABLE pelicula ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pelicula' AND COLUMN_NAME = 'portada_path') = 0,
  'ALTER TABLE pelicula ADD COLUMN portada_path VARCHAR(500) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Cartelera (funciones por película)
CREATE TABLE IF NOT EXISTS funcion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pelicula_id INT NOT NULL,
  fecha DATE NOT NULL,
  hora TIME NOT NULL,
  sala VARCHAR(50) NULL,
  precio DECIMAL(10,2) NULL,
  CONSTRAINT fk_funcion_pelicula FOREIGN KEY (pelicula_id) REFERENCES pelicula(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT ux_funcion_unica UNIQUE (pelicula_id, fecha, hora)
) ENGINE=InnoDB;

SET @needs_fix := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'funcion'
     AND COLUMN_NAME = 'pelicula_id' AND DATA_TYPE <> 'int'
);

SET @sql := IF(@needs_fix > 0,
  'ALTER TABLE funcion DROP FOREIGN KEY fk_funcion_pelicula',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(@needs_fix > 0,
  'ALTER TABLE funcion MODIFY COLUMN pelicula_id INT NOT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(@needs_fix > 0,
  'ALTER TABLE funcion ADD CONSTRAINT fk_funcion_pelicula FOREIGN KEY (pelicula_id) REFERENCES pelicula(id) ON DELETE CASCADE ON UPDATE CASCADE',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'funcion' AND INDEX_NAME = 'idx_funcion_pelicula_fecha') = 0,
  'CREATE INDEX idx_funcion_pelicula_fecha ON funcion(pelicula_id, fecha)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
