# Verificación de conectividad en MySQL Workbench

1. **Abrir la conexión** configurada con el mismo host, puerto y base de datos usados por la aplicación (`jdbc:mysql://localhost:3306/magenta`).
2. Ejecutar la instrucción de salud de esquema:

   ```sql
   USE magenta;
   SELECT id, titulo, anio FROM pelicula ORDER BY anio DESC LIMIT 5;
   ```

3. Confirmar que los resultados coinciden con los registros iniciales cargados por `data.sql` (por ejemplo, `Interestelar`, `Spider-Man 2`, `Alien`).
4. Registrar la evidencia guardando la pestaña de resultados o tomando una captura.

> 💡 Si el catálogo está vacío, vuelve a ejecutar `src/main/resources/schema.sql` seguido de `data.sql` y repite la consulta.
