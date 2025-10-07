@echo off
REM Importa create_db_user.sql, schema.sql y data.sql usando el cliente mysql en puerto 3307.
REM Ajusta %MYSQL_BIN% si tu mysql.exe está en otra ruta.

SET MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
IF NOT EXIST %MYSQL_BIN% (
  SET MYSQL_BIN=mysql
)

n:: Parametros: %1 => host (default 127.0.0.1), %2 => port (default 3307)
SET HOST=%~1
IF "%HOST%"=="" SET HOST=127.0.0.1
SET PORT=%~2
IF "%PORT%"=="" SET PORT=3307

n:: Ejecutar scripts (se pide contraseña de root)
echo Ejecutando create_db_user.sql ...
%MYSQL_BIN% -h %HOST% -P %PORT% -u root -p < "%~dp0create_db_user.sql"

necho Importando schema.sql ...
%MYSQL_BIN% -h %HOST% -P %PORT% -u app_admin -pAppAdmin#3307 < "%~dp0..\src\main\resources\schema.sql"

necho Importando data.sql ...
%MYSQL_BIN% -h %HOST% -P %PORT% -u app_admin -pAppAdmin#3307 < "%~dp0..\src\main\resources\data.sql"

necho Import completo.
pause
