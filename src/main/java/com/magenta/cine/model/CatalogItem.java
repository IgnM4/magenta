
package com.magenta.cine.model;

public class CatalogItem {
    private Integer id;
    private String nombre;

    public CatalogItem(Integer id, String nombre) {
        this.id = id; this.nombre = nombre;
    }
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    @Override public String toString() { return nombre; }
}
