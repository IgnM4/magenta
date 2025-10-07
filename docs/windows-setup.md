## Pasos rápidos (Windows) para dejar la BD lista y conectarla con MySQL Workbench

1. Instala MySQL Server y MySQL Workbench desde el instalador oficial.
2. Configura MySQL para escuchar en el puerto 3307: editar `my.ini` (normalmente en C:\ProgramData\MySQL\MySQL Server 8.0\) y en la sección [mysqld] agregar `port=3306` y `bind-address=127.0.0.1`, luego reinicia el servicio `MySQL80`.
3. Abre una consola CMD como administrador y navega a `C:\Users\ignac\Documents\magenta\magenta\scripts`.
4. Ejecuta `import_db.bat` y cuando se pida la contraseña de `root` ingrésala. Esto creará la base `magenta`, el usuario `app_admin` y cargará `schema.sql` y `data.sql`.
5. En MySQL Workbench crea una conexión con Host `127.0.0.1`, Port `3306`, User `app_admin` y contraseña `AppAdmin#3306`. Prueba la conexión.

Notas:
- Si tu `mysql.exe` no está en la ruta por defecto, edita `import_db.bat` y ajusta la variable `MYSQL_BIN`.
- El script `create_db_user.sql` crea la base y el usuario `app_admin` con acceso a la base `magenta`.
