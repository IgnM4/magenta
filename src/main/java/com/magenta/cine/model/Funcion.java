package com.magenta.cine.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Funcion {
    private Long id;
    private Long peliculaId;
    private LocalDate fecha;
    private LocalTime hora;
    private String sala;
    private BigDecimal precio;

    public Funcion() {
    }

    public Funcion(Long id, Long peliculaId, LocalDate fecha, LocalTime hora, String sala, BigDecimal precio) {
        this.id = id;
        this.peliculaId = peliculaId;
        this.fecha = fecha;
        this.hora = hora;
        this.sala = sala;
        this.precio = precio;
    }

    public Funcion(Long peliculaId, LocalDate fecha, LocalTime hora, String sala, BigDecimal precio) {
        this(null, peliculaId, fecha, hora, sala, precio);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPeliculaId() {
        return peliculaId;
    }

    public void setPeliculaId(Long peliculaId) {
        this.peliculaId = peliculaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Funcion funcion = (Funcion) o;
        return Objects.equals(id, funcion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Funcion{" +
                "id=" + id +
                ", peliculaId=" + peliculaId +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", sala='" + sala + '\'' +
                ", precio=" + precio +
                '}';
    }
}
