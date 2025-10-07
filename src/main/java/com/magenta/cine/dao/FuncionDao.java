package com.magenta.cine.dao;

import com.magenta.cine.db.ConnectionFactory;
import com.magenta.cine.model.Funcion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FuncionDao {

    public List<Funcion> listarPorPeliculaYFecha(long peliculaId, LocalDate fecha) throws SQLException {
        String sql = """
            SELECT id, pelicula_id, fecha, hora, sala, precio
            FROM funcion
            WHERE pelicula_id = ? AND fecha = ?
            ORDER BY hora
        """;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, peliculaId);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                List<Funcion> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public void crear(Funcion f) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            int count = contarPorDia(c, f.getPeliculaId(), f.getFecha());
            if (count >= 7) {
                throw new SQLException("La película ya tiene 7 funciones registradas para " + f.getFecha() + ".");
            }

            String sql = """
                INSERT INTO funcion (pelicula_id, fecha, hora, sala, precio)
                VALUES (?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, f.getPeliculaId());
                ps.setDate(2, Date.valueOf(f.getFecha()));
                ps.setTime(3, Time.valueOf(f.getHora()));
                ps.setString(4, f.getSala());
                if (f.getPrecio() != null) {
                    ps.setBigDecimal(5, f.getPrecio());
                } else {
                    ps.setNull(5, java.sql.Types.DECIMAL);
                }

                ps.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException ex) {
                throw new SQLException("Ya existe una función a las " + f.getHora() + " para esa fecha.", ex);
            }
        }
    }

    public void eliminar(long funcionId) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM funcion WHERE id = ?")) {
            ps.setLong(1, funcionId);
            ps.executeUpdate();
        }
    }

    public boolean existeChoque(long peliculaId, LocalDate fecha, LocalTime hora) throws SQLException {
        String sql = "SELECT 1 FROM funcion WHERE pelicula_id = ? AND fecha = ? AND hora = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, peliculaId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int contarPorDia(long peliculaId, LocalDate fecha) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection()) {
            return contarPorDia(c, peliculaId, fecha);
        }
    }

    private int contarPorDia(Connection c, long peliculaId, LocalDate fecha) throws SQLException {
        String sql = "SELECT COUNT(*) FROM funcion WHERE pelicula_id = ? AND fecha = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, peliculaId);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Funcion map(ResultSet rs) throws SQLException {
        Funcion f = new Funcion();
        f.setId(rs.getLong("id"));
        f.setPeliculaId(rs.getLong("pelicula_id"));
        f.setFecha(rs.getDate("fecha").toLocalDate());
        f.setHora(rs.getTime("hora").toLocalTime());
        f.setSala(rs.getString("sala"));
        if (rs.getBigDecimal("precio") != null) {
            f.setPrecio(rs.getBigDecimal("precio"));
        }
        return f;
    }
}
