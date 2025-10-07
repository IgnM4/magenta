
package com.magenta.cine.ui;

import com.magenta.cine.dao.PeliculaDao;
import com.magenta.cine.model.CatalogItem;
import com.magenta.cine.model.Pelicula;
import com.magenta.cine.ui.model.PeliculaTableModel;
import com.magenta.cine.ui.render.PosterRenderer;
import com.magenta.cine.util.Dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FrmListarPeliculas extends JFrame {
    private final PeliculaDao dao = new PeliculaDao();

    private final JComboBox<Object> cbGenero = new JComboBox<>();
    private final JTextField txtDesde = new JTextField(6);
    private final JTextField txtHasta = new JTextField(6);
    private final JCheckBox chkSoloActivas = new JCheckBox("Mostrar solo activas", true);
    private final PeliculaTableModel model = new PeliculaTableModel(new ArrayList<>());
    private final JTable table = new JTable(model);
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(cardLayout);
    private final JLabel lblEmpty = new JLabel("Sin resultados para los filtros aplicados", SwingConstants.CENTER);
    private final JButton btnFunciones = new JButton("Funciones…");

    private boolean suppressToggleEvents = false;
    private int editingActiveRow = -1;
    private boolean previousActiveValue = false;

    public FrmListarPeliculas() {
        super("Magenta | Listar películas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBorder(new EmptyBorder(12, 12, 6, 12));
        filtros.add(new JLabel("Género:")); filtros.add(cbGenero);
        filtros.add(new JLabel("Año desde:")); filtros.add(txtDesde);
        filtros.add(new JLabel("hasta:")); filtros.add(txtHasta);
        filtros.add(chkSoloActivas);
        JButton btnBuscar = new JButton("Buscar");
        filtros.add(btnBuscar);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(110);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane sp = new JScrollPane(table);
        lblEmpty.setBorder(new EmptyBorder(40, 12, 40, 12));
        centerPanel.add(sp, "table");
        centerPanel.add(lblEmpty, "empty");

        add(filtros, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFunciones.setEnabled(false);
        south.add(btnFunciones);
        add(south, BorderLayout.SOUTH);

        setSize(880, 520);
        setLocationRelativeTo(null);

        btnBuscar.addActionListener(e -> buscar());
        chkSoloActivas.addActionListener(e -> buscar());
        btnFunciones.addActionListener(e -> abrirFunciones());

        configurarColumnas();
        configurarEventosActivo();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnFunciones.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        SwingUtilities.invokeLater(() -> {
            cargarGeneros();
            buscar();
        });
    }

    private void cargarGeneros() {
        try {
            cbGenero.removeAllItems();
            cbGenero.addItem("Todos");
            for (CatalogItem g : dao.listarGeneros()) cbGenero.addItem(g.getNombre());
        } catch (SQLException e) {
            Dialogs.error("Error cargando géneros: " + e.getMessage(), e);
        }
    }

    private void buscar() {
        try {
            String genero = cbGenero.getSelectedItem() instanceof String ? (String) cbGenero.getSelectedItem() : "Todos";
            Integer desde = parseOrNull(txtDesde.getText());
            Integer hasta = parseOrNull(txtHasta.getText());
            Boolean soloActivas = chkSoloActivas.isSelected() ? Boolean.TRUE : null;
            List<Pelicula> data = dao.listarConFiltros(genero, desde, hasta, soloActivas);
            suppressToggleEvents = true;
            model.setData(data);
            suppressToggleEvents = false;
            table.clearSelection();
            btnFunciones.setEnabled(false);
            if (data.isEmpty()) {
                cardLayout.show(centerPanel, "empty");
            } else {
                cardLayout.show(centerPanel, "table");
            }
        } catch (SQLException ex) {
            Dialogs.error("Error al listar: " + ex.getMessage(), ex);
        }
    }

    private Integer parseOrNull(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.valueOf(s); }
        catch (NumberFormatException e) { Dialogs.error("Años deben ser numéricos"); return null; }
    }

    private void configurarColumnas() {
        PosterRenderer posterRenderer = new PosterRenderer(64, 96);
        table.getColumnModel().getColumn(0).setCellRenderer(posterRenderer);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setMaxWidth(90);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setMaxWidth(80);

        JCheckBox check = new JCheckBox();
        check.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultCellEditor editor = new DefaultCellEditor(check);
        editor.setClickCountToStart(1);
        table.getColumnModel().getColumn(8).setCellEditor(editor);
        table.getColumnModel().getColumn(8).setCellRenderer(table.getDefaultRenderer(Boolean.class));
    }

    private void configurarEventosActivo() {
        table.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("tableCellEditor".equals(evt.getPropertyName()) && table.isEditing()) {
                    int viewRow = table.getEditingRow();
                    int viewCol = table.getEditingColumn();
                    if (viewRow >= 0 && viewCol == 8) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        editingActiveRow = modelRow;
                        previousActiveValue = model.get(modelRow).isActivo();
                    }
                }
            }
        });

        model.addTableModelListener(e -> {
            if (suppressToggleEvents) {
                return;
            }
            if (e.getColumn() == 8 && e.getFirstRow() >= 0) {
                int row = e.getFirstRow();
                Pelicula pelicula = model.get(row);
                boolean nuevoValor = pelicula.isActivo();
                boolean valorAnterior = (row == editingActiveRow) ? previousActiveValue : !nuevoValor;
                actualizarEstadoActivo(pelicula, row, valorAnterior, nuevoValor);
                editingActiveRow = -1;
            }
        });
    }

    private void actualizarEstadoActivo(Pelicula pelicula, int row, boolean valorAnterior, boolean valorNuevo) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.actualizarActivo(pelicula.getId(), valorNuevo);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.info("Estado actualizado");
                } catch (Exception ex) {
                    suppressToggleEvents = true;
                    pelicula.setActivo(valorAnterior);
                    model.fireTableRowsUpdated(row, row);
                    suppressToggleEvents = false;
                    Dialogs.error("No se pudo actualizar el estado: " + ex.getMessage(), ex);
                }
            }
        }.execute();
    }

    private void abrirFunciones() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Dialogs.warn("Selecciona una película para gestionar funciones.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Pelicula pelicula = model.get(modelRow);
        DlgFunciones dlg = new DlgFunciones(this, pelicula);
        dlg.setVisible(true);
    }
}
