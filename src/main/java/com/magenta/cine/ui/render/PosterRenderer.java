package com.magenta.cine.ui.render;

import com.magenta.cine.util.Images;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Map;

public class PosterRenderer extends JLabel implements TableCellRenderer {

    private final int width;
    private final int height;
    private final Map<String, Icon> cache;

    public PosterRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        cache = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Icon> eldest) {
                return size() > 128;
            }
        };
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setIcon(obtenerIcono(value));
        setText(null);
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        return this;
    }

    private Icon obtenerIcono(Object value) {
        String key = value == null ? "__empty__" : String.valueOf(value);
        return cache.computeIfAbsent(key, k -> Images.loadThumb(value == null ? null : String.valueOf(value), width, height));
    }
}
