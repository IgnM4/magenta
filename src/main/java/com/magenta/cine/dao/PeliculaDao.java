
package com.magenta.cine.dao;

import com.magenta.cine.db.ConnectionFactory;
import com.magenta.cine.model.CatalogItem;
import com.magenta.cine.model.Pelicula;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PeliculaDao {

    private static final Logger LOGGER = Logger.getLogger(PeliculaDao.class.getName());
    private static final String COLUMN_EXISTS_SQL = """
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
         WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
    """;

    private static volatile String baseSelectCache;
    private static volatile Boolean activoColumnPresent;
    private static volatile Boolean portadaColumnPresent;

    public List<CatalogItem> listarGeneros() throws SQLException {
        String sql = "SELECT id, nombre FROM genero ORDER BY nombre";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CatalogItem> out = new ArrayList<>();
            while (rs.next()) out.add(new CatalogItem(rs.getInt(1), rs.getString(2)));
            return out;
        }
    }

    public List<CatalogItem> listarClasificaciones() throws SQLException {
        String sql = "SELECT id, codigo FROM clasificacion ORDER BY codigo";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CatalogItem> out = new ArrayList<>();
            while (rs.next()) out.add(new CatalogItem(rs.getInt(1), rs.getString(2)));
            return out;
        }
    }

    public int crear(Pelicula p) throws SQLException {
        String sql = """
            INSERT INTO pelicula (titulo, director, genero_id, anio, duracion_min, clasificacion_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = ConnectionFactory.getConnection()) {
            String titulo = sanitize(p.getTitulo());
            String director = sanitize(p.getDirector());
            validarTituloUnico(c, titulo, null);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, titulo);
                ps.setString(2, director);
                ps.setInt(3, p.getGeneroId());
                ps.setInt(4, p.getAnio());
                ps.setInt(5, p.getDuracionMin());
                ps.setInt(6, p.getClasificacionId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public boolean existeTitulo(String titulo, Integer excluirId) throws SQLException {
        String sql = "SELECT 1 FROM pelicula WHERE titulo = ?" + (excluirId != null ? " AND id <> ?" : "");
        String value = sanitize(titulo);
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            if (excluirId != null) {
                ps.setInt(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void validarTituloUnico(Connection c, String titulo, Integer excluirId) throws SQLException {
        if (titulo == null) {
            return;
        }
        String sql = "SELECT id FROM pelicula WHERE titulo = ?" + (excluirId != null ? " AND id <> ?" : "");
        String value = sanitize(titulo);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, value);
            if (excluirId != null) {
                ps.setInt(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SQLIntegrityConstraintViolationException("Ya existe una película con el mismo título.");
                }
            }
        }
    }

    public Pelicula buscarPorId(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = baseSelect(c) + " WHERE p.id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return map(rs);
                }
            }
        }
    }

    public boolean modificar(Pelicula p) throws SQLException {
        String sql = """
            UPDATE pelicula
               SET titulo=?, director=?, genero_id=?, anio=?, duracion_min=?, clasificacion_id=?
             WHERE id=?
        """;
        try (Connection c = ConnectionFactory.getConnection()) {
            String titulo = sanitize(p.getTitulo());
            String director = sanitize(p.getDirector());
            validarTituloUnico(c, titulo, p.getId());
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, titulo);
                ps.setString(2, director);
                ps.setInt(3, p.getGeneroId());
                ps.setInt(4, p.getAnio());
                ps.setInt(5, p.getDuracionMin());
                ps.setInt(6, p.getClasificacionId());
                ps.setInt(7, p.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM pelicula WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Pelicula> listarConFiltros(String generoNombre, Integer anioDesde, Integer anioHasta) throws SQLException {
        return listarConFiltros(generoNombre, anioDesde, anioHasta, null);
    }

    public List<Pelicula> listarConFiltros(String generoNombre, Integer anioDesde, Integer anioHasta, Boolean soloActivas) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            StringBuilder sql = new StringBuilder(baseSelect(c) + " WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (generoNombre != null && !generoNombre.isBlank() && !"Todos".equalsIgnoreCase(generoNombre)) {
                sql.append(" AND g.nombre = ?");
                params.add(generoNombre.trim());
            }
            if (anioDesde != null) { sql.append(" AND p.anio >= ?"); params.add(anioDesde); }
            if (anioHasta != null) { sql.append(" AND p.anio <= ?"); params.add(anioHasta); }
            if (Boolean.TRUE.equals(soloActivas) && supportsActivoColumn(c)) {
                sql.append(" AND p.activo = 1");
            }
            sql.append(" ORDER BY p.anio DESC, p.titulo ASC");

            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    List<Pelicula> out = new ArrayList<>();
                    while (rs.next()) out.add(map(rs));
                    return out;
                }
            }
        }
    }

    public List<Pelicula> listarPorTitulo(String filtro) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = baseSelect(c) + " WHERE p.titulo LIKE ? ORDER BY p.titulo";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                String pattern = "%" + (filtro == null ? "" : filtro.trim()) + "%";
                ps.setString(1, pattern);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Pelicula> list = new ArrayList<>();
                    while (rs.next()) list.add(map(rs));
                    return list;
                }
            }
        }
    }

    public List<Pelicula> listar(boolean soloActivas) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            StringBuilder sql = new StringBuilder(baseSelect(c));
            if (soloActivas && supportsActivoColumn(c)) {
                sql.append(" WHERE p.activo = 1");
            }
            sql.append(" ORDER BY p.titulo ASC");
            try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<Pelicula> list = new ArrayList<>();
                    while (rs.next()) list.add(map(rs));
                    return list;
                }
            }
        }
    }

    public void actualizarActivo(long peliculaId, boolean activo) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            if (!supportsActivoColumn(c)) {
                LOGGER.warning("La columna 'pelicula.activo' no está disponible. Ejecuta src/main/resources/schema.sql para "
                        + "actualizar la base de datos.");
                return;
            }
            String sql = "UPDATE pelicula SET activo=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setBoolean(1, activo);
                ps.setLong(2, peliculaId);
                ps.executeUpdate();
            }
        }
    }

    public void actualizarPortada(long peliculaId, String rutaPortada) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            if (!supportsPortadaColumn(c)) {
                LOGGER.warning("La columna 'pelicula.portada_path' no está disponible. Ejecuta src/main/resources/schema.sql "
                        + "para actualizar la base de datos.");
                return;
            }
            String sql = "UPDATE pelicula SET portada_path=? WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, rutaPortada);
                ps.setLong(2, peliculaId);
                ps.executeUpdate();
            }
        }
    }

    private Pelicula map(ResultSet rs) throws SQLException {
        return new Pelicula(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("director"),
                rs.getInt("genero_id"),
                rs.getString("genero"),
                rs.getInt("anio"),
                rs.getInt("duracion_min"),
                rs.getInt("clasificacion_id"),
                rs.getString("clasif"),
                rs.getBoolean("activo"),
                rs.getString("portada_path")
        );
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim();
    }

    private static String baseSelect(Connection c) throws SQLException {
        ensureSchemaMetadata(c);
        return baseSelectCache;
    }

    private static boolean supportsActivoColumn(Connection c) throws SQLException {
        ensureSchemaMetadata(c);
        return Boolean.TRUE.equals(activoColumnPresent);
    }

    private static boolean supportsPortadaColumn(Connection c) throws SQLException {
        ensureSchemaMetadata(c);
        return Boolean.TRUE.equals(portadaColumnPresent);
    }

    private static void ensureSchemaMetadata(Connection c) throws SQLException {
        if (baseSelectCache != null) {
            return;
        }
        synchronized (PeliculaDao.class) {
            if (baseSelectCache != null) {
                return;
            }
            boolean activo = hasColumn(c, "pelicula", "activo");
            boolean portada = hasColumn(c, "pelicula", "portada_path");
            activoColumnPresent = activo;
            portadaColumnPresent = portada;
            if (!activo) {
                LOGGER.warning("La columna 'pelicula.activo' no está disponible. Ejecuta src/main/resources/schema.sql para "
                        + "actualizar la base de datos.");
            }
            if (!portada) {
                LOGGER.warning("La columna 'pelicula.portada_path' no está disponible. Ejecuta src/main/resources/schema.sql "
                        + "para actualizar la base de datos.");
            }
            baseSelectCache = buildBaseSelect(activo, portada);
        }
    }

    private static boolean hasColumn(Connection c, String table, String column) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(COLUMN_EXISTS_SQL)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static String buildBaseSelect(boolean activo, boolean portada) {
        String activoSelect = activo ? ",p.activo" : ",1 AS activo";
        String portadaSelect = portada ? ",p.portada_path" : ",NULL AS portada_path";
        return """
            SELECT p.id,p.titulo,p.director,p.genero_id,g.nombre AS genero,p.anio,p.duracion_min,
                   p.clasificacion_id,c.codigo AS clasif%s%s
              FROM pelicula p
              JOIN genero g ON p.genero_id=g.id
              JOIN clasificacion c ON p.clasificacion_id=c.id
        """.formatted(activoSelect, portadaSelect);
    }
}
