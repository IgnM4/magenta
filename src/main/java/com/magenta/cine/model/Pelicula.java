
package com.magenta.cine.model;

public class Pelicula {
    private Integer id;
    private String titulo;
    private String director;
    private Integer generoId;
    private String generoNombre;
    private Integer anio;
    private Integer duracionMin;
    private Integer clasificacionId;
    private String clasificacionCodigo;
    private boolean activo = true;
    private String portadaPath;

    public Pelicula() {}

    public Pelicula(Integer id, String titulo, String director, Integer generoId, String generoNombre,
                    Integer anio, Integer duracionMin, Integer clasificacionId, String clasificacionCodigo) {
        this.id = id; this.titulo = titulo; this.director = director;
        this.generoId = generoId; this.generoNombre = generoNombre;
        this.anio = anio; this.duracionMin = duracionMin;
        this.clasificacionId = clasificacionId; this.clasificacionCodigo = clasificacionCodigo;
    }

    public Pelicula(Integer id, String titulo, String director, Integer generoId, String generoNombre,
                    Integer anio, Integer duracionMin, Integer clasificacionId, String clasificacionCodigo,
                    boolean activo, String portadaPath) {
        this(id, titulo, director, generoId, generoNombre, anio, duracionMin, clasificacionId, clasificacionCodigo);
        this.activo = activo;
        this.portadaPath = portadaPath;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public Integer getGeneroId() { return generoId; }
    public void setGeneroId(Integer generoId) { this.generoId = generoId; }
    public String getGeneroNombre() { return generoNombre; }
    public void setGeneroNombre(String generoNombre) { this.generoNombre = generoNombre; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getDuracionMin() { return duracionMin; }
    public void setDuracionMin(Integer duracionMin) { this.duracionMin = duracionMin; }
    public Integer getClasificacionId() { return clasificacionId; }
    public void setClasificacionId(Integer clasificacionId) { this.clasificacionId = clasificacionId; }
    public String getClasificacionCodigo() { return clasificacionCodigo; }
    public void setClasificacionCodigo(String clasificacionCodigo) { this.clasificacionCodigo = clasificacionCodigo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getPortadaPath() { return portadaPath; }
    public void setPortadaPath(String portadaPath) { this.portadaPath = portadaPath; }

    @Override
    public String toString() {
        return String.format("%d | %s (%d) - %s - %s",
                id, titulo, anio, director, generoNombre);
    }
}
