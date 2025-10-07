
# Magenta Cine — Semana 8 (NetBeans + MySQL)

Proyecto para evaluación S8:
- BD normalizada (género y clasificación como catálogos, FKs + índices).
- Conexión JDBC (mysql-connector-j 9.1.0).
- GUI Swing con menú, Crear/Modificar/Eliminar, y **Listar en JTable con filtros** (género + rango de años).
- DAO con consultas parametrizadas (`PreparedStatement`).

## Configuración de la conexión

Puedes parametrizar la conexión mediante **variables de entorno** o propiedades JVM:

| Tipo | Clave | Valor por defecto |
| --- | --- | --- |
| Variable de entorno | `DB_URL` | `jdbc:mysql://localhost:3307/magenta?useSSL=false&allowPublicKeyRetrieval=true` |
| Variable de entorno | `DB_USER` | `root` |
| Variable de entorno | `DB_PASS` | `12345` |
| Variable de entorno | `DB_CONNECT_TIMEOUT_SECONDS` | `3` |
| Propiedad JVM | `db.url`, `db.user`, `db.pass` | Igual que los valores por defecto |
| Propiedad JVM | `db.connectTimeoutSeconds` | `3` |

Ejemplo (NetBeans → Properties → Run → VM Options):

```
-Ddb.url=jdbc:mysql://localhost:3307/magenta?useSSL=false&allowPublicKeyRetrieval=true \
-Ddb.user=root -Ddb.pass=<TU_PASSWORD>
```

> 🔁 Los valores inválidos o menores/iguales a cero para `db.connectTimeoutSeconds` (o `DB_CONNECT_TIMEOUT_SECONDS`) se
> ignoran y la aplicación vuelve automáticamente al valor por defecto de 3 segundos.

## Verificación de conectividad

1. Ejecuta `mvn -q exec:java` (o corre `com.magenta.cine.app.Main` desde tu IDE).
   * La consola mostrará `Conexión exitosa a jdbc:mysql://...` cuando la prueba de salud (`SELECT 1`) sea correcta.
   * Si falla, el programa registra el error y despliega un diálogo con sugerencias de corrección.
2. Abre MySQL Workbench y sigue los pasos descritos en [`docs/workbench-verification.md`](docs/workbench-verification.md) para validar la conexión manualmente.

## Inicialización de datos
1. Ejecuta `src/main/resources/schema.sql` para crear el esquema e índices (incluye la restricción única de títulos).
2. Ejecuta `src/main/resources/data.sql` para cargar los catálogos y registros de ejemplo (idempotente).

## Uso rápido (nuevas funciones)

1. Ejecuta `mvn -q exec:java` y abre **Películas → Listar** para gestionar el estado *Activo* y filtrar con "Mostrar solo activas" (la miniatura se actualiza automáticamente).
2. Desde el listado selecciona una película y presiona **Funciones…** para programar hasta 7 funciones por día con hora, sala y precio.
3. Al crear o modificar una película utiliza **Seleccionar portada…** para copiar la imagen al directorio `~/magenta/posters/` y mostrar la miniatura en el catálogo.

## Ejecución y pruebas

```bash
mvn test            # Ejecuta pruebas unitarias de configuración y validaciones
mvn -q exec:java    # Inicia la aplicación con verificación automática de BD
```
