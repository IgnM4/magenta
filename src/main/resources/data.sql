
USE magenta;

INSERT IGNORE INTO genero (nombre) VALUES
('Acción'),('Aventura'),('Ciencia Ficción'),('Drama'),('Comedia'),('Terror'),('Musical');

INSERT IGNORE INTO clasificacion (codigo) VALUES ('TE'),('TE+7'),('+14'),('+18');

INSERT INTO pelicula (titulo, director, genero_id, anio, duracion_min, clasificacion_id)
SELECT * FROM (
    SELECT 'Interestelar' AS titulo,'Christopher Nolan' AS director,
           (SELECT id FROM genero WHERE nombre='Ciencia Ficción') AS genero_id,
           2014 AS anio, 169 AS duracion_min,
           (SELECT id FROM clasificacion WHERE codigo='TE+7') AS clasificacion_id
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo='Interestelar');

INSERT INTO pelicula (titulo, director, genero_id, anio, duracion_min, clasificacion_id)
SELECT * FROM (
    SELECT 'Spider-Man 2','Sam Raimi',
           (SELECT id FROM genero WHERE nombre='Acción'),
           2004, 127,
           (SELECT id FROM clasificacion WHERE codigo='TE+7')
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo='Spider-Man 2');

INSERT INTO pelicula (titulo, director, genero_id, anio, duracion_min, clasificacion_id)
SELECT * FROM (
    SELECT 'Alien','Ridley Scott',
           (SELECT id FROM genero WHERE nombre='Terror'),
           1979, 117,
           (SELECT id FROM clasificacion WHERE codigo='+14')
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM pelicula WHERE titulo='Alien');
