
package com.magenta.cine.ui.model;

import com.magenta.cine.model.Pelicula;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PeliculaTableModel extends AbstractTableModel {
    private final String[] cols = {"Miniatura","ID","Título","Director","Género","Año","Duración","Clasif.","Activo"};
    private java.util.List<Pelicula> data;

    public PeliculaTableModel(List<Pelicula> data) { this.data = data; }
    public void setData(List<Pelicula> data) { this.data = data; fireTableDataChanged(); }
    public Pelicula get(int row) { return data.get(row); }

    @Override public int getRowCount() { return data == null ? 0 : data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Object.class;
            case 1 -> Integer.class;
            case 4 -> String.class;
            case 5 -> Integer.class;
            case 6 -> Integer.class;
            case 8 -> Boolean.class;
            default -> Object.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 8;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 8 && rowIndex >= 0 && rowIndex < getRowCount()) {
            Pelicula p = data.get(rowIndex);
            boolean value = Boolean.TRUE.equals(aValue);
            p.setActivo(value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override public Object getValueAt(int r, int c) {
        Pelicula p = data.get(r);
        return switch (c) {
            case 0 -> p.getPortadaPath();
            case 1 -> p.getId();
            case 2 -> p.getTitulo();
            case 3 -> p.getDirector();
            case 4 -> p.getGeneroNombre();
            case 5 -> p.getAnio();
            case 6 -> p.getDuracionMin();
            case 7 -> p.getClasificacionCodigo();
            case 8 -> p.isActivo();
            default -> "";
        };
    }
}
