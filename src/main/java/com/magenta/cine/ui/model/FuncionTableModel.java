package com.magenta.cine.ui.model;

import com.magenta.cine.model.Funcion;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FuncionTableModel extends AbstractTableModel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final String[] cols = {"Hora", "Sala", "Precio"};
    private List<Funcion> data = new ArrayList<>();

    public void setData(List<Funcion> data) {
        this.data = data == null ? new ArrayList<>() : data;
        fireTableDataChanged();
    }

    public Funcion get(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Funcion f = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> f.getHora() == null ? "" : TIME_FORMATTER.format(f.getHora());
            case 1 -> f.getSala();
            case 2 -> f.getPrecio();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 2 -> java.math.BigDecimal.class;
            default -> String.class;
        };
    }
}
