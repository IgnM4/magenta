package com.magenta.cine.ui;

import com.magenta.cine.dao.PeliculaDao;
import com.magenta.cine.model.CatalogItem;
import com.magenta.cine.model.Pelicula;
import com.magenta.cine.util.Dialogs;
import com.magenta.cine.util.Images;
import com.magenta.cine.util.Validators;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;

public class FrmModificarPelicula extends JFrame {
    private final PeliculaDao dao = new PeliculaDao();

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtTitulo = new JTextField(25);
    private final JTextField txtDirector = new JTextField(20);
    private final JComboBox<CatalogItem> cbGenero = new JComboBox<>();
    private final JTextField txtAnio = new JTextField(6);
    private final JTextField txtDuracion = new JTextField(6);
    private final JComboBox<CatalogItem> cbClasif = new JComboBox<>();
    private final JTextField txtFiltro = new JTextField(18);
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> lstPeliculas = new JList<>(listModel);
    private final JLabel lblResultados = new JLabel("Coincidencias");
    private final JLabel lblPortada = new JLabel();

    private String rutaPortadaActual;
    private String rutaPortadaSeleccionada;

    public FrmModificarPelicula() {
        super("Magenta | Modificar película");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(new JLabel("ID:"));
        top.add(txtId);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setMnemonic('B');
        top.add(btnBuscar);
        top.add(new JLabel(" Filtrar título:"));
        top.add(txtFiltro);
        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setMnemonic('F');
        top.add(btnFiltrar);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, gbc, 0, "Título:", txtTitulo);
        addRow(form, gbc, 1, "Director:", txtDirector);
        addRow(form, gbc, 2, "Género:", cbGenero);
        addRow(form, gbc, 3, "Año:", txtAnio);
        addRow(form, gbc, 4, "Duración (min):", txtDuracion);
        addRow(form, gbc, 5, "Clasificación:", cbClasif);

        JPanel panelPortada = new JPanel(new BorderLayout(6, 6));
        lblPortada.setPreferredSize(new Dimension(96, 144));
        lblPortada.setHorizontalAlignment(SwingConstants.CENTER);
        lblPortada.setVerticalAlignment(SwingConstants.CENTER);
        lblPortada.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        JButton btnSeleccionarPortada = new JButton("Seleccionar portada…");
        btnSeleccionarPortada.addActionListener(e -> seleccionarPortada());
        panelPortada.add(lblPortada, BorderLayout.CENTER);
        panelPortada.add(btnSeleccionarPortada, BorderLayout.SOUTH);
        addRow(form, gbc, 6, "Portada:", panelPortada);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.add(form, BorderLayout.CENTER);

        lstPeliculas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstPeliculas.setVisibleRowCount(10);
        JScrollPane sp = new JScrollPane(lstPeliculas);
        sp.setPreferredSize(new Dimension(280, 220));

        JPanel listPanel = new JPanel(new BorderLayout(6, 6));
        lblResultados.setBorder(new EmptyBorder(0, 0, 4, 0));
        listPanel.add(lblResultados, BorderLayout.NORTH);
        listPanel.add(sp, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formWrapper, listPanel);
        split.setResizeWeight(0.6);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setMnemonic('G');
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setMnemonic('L');
        bottom.add(btnLimpiar);
        bottom.add(btnGuardar);

        content.add(top, BorderLayout.NORTH);
        content.add(split, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(btnGuardar);

        btnBuscar.addActionListener(e -> buscarPorId());
        btnFiltrar.addActionListener(e -> filtrar());
        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());

        lstPeliculas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = lstPeliculas.getSelectedValue();
                if (sel != null) {
                    int id = Integer.parseInt(sel.split(" ")[0]);
                    cargarPorId(id);
                }
            }
        });

        SwingUtilities.invokeLater(this::cargarCatalogos);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, gbc);
    }

    private void cargarCatalogos() {
        try {
            cbGenero.removeAllItems();
            cbClasif.removeAllItems();
            for (CatalogItem g : dao.listarGeneros()) cbGenero.addItem(g);
            for (CatalogItem c : dao.listarClasificaciones()) cbClasif.addItem(c);
        } catch (SQLException e) {
            Dialogs.error("Error cargando catálogos: " + e.getMessage(), e);
        }
    }

    private void seleccionarPortada() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar portada");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes (.jpg, .jpeg, .png)", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                String ruta = Images.copyPosterToAppFolder(archivo);
                rutaPortadaSeleccionada = ruta;
                actualizarPreviewPortada(ruta);
            } catch (IOException ex) {
                Dialogs.error("No se pudo copiar la portada: " + ex.getMessage(), ex);
            }
        }
    }

    private void buscarPorId() {
        try {
            int id = Validators.parseInt(txtId.getText(), "ID", 1, Integer.MAX_VALUE);
            Pelicula p = dao.buscarPorId(id);
            if (p == null) {
                Dialogs.info("No existe película con ID=" + id);
                limpiar();
                return;
            }
            setForm(p);
        } catch (IllegalArgumentException ex) {
            Dialogs.error(ex.getMessage());
        } catch (SQLException ex) {
            Dialogs.error("Error buscando película: " + ex.getMessage(), ex);
        }
    }

    private void filtrar() {
        try {
            listModel.clear();
            for (Pelicula p : dao.listarPorTitulo(txtFiltro.getText())) {
                listModel.addElement(p.getId() + " " + p.getTitulo());
            }
            lblResultados.setText(listModel.isEmpty() ? "Sin coincidencias" : "Coincidencias (" + listModel.size() + ")");
        } catch (SQLException ex) {
            Dialogs.error("Error filtrando películas: " + ex.getMessage(), ex);
        }
    }

    private void cargarPorId(int id) {
        try {
            Pelicula p = dao.buscarPorId(id);
            if (p != null) {
                setForm(p);
            }
        } catch (SQLException ex) {
            Dialogs.error("Error cargando película: " + ex.getMessage(), ex);
        }
    }

    private void setForm(Pelicula p) {
        txtId.setText(String.valueOf(p.getId()));
        txtTitulo.setText(p.getTitulo());
        txtDirector.setText(p.getDirector());
        txtAnio.setText(String.valueOf(p.getAnio()));
        txtDuracion.setText(String.valueOf(p.getDuracionMin()));
        selectComboById(cbGenero, p.getGeneroId());
        selectComboById(cbClasif, p.getClasificacionId());
        rutaPortadaActual = p.getPortadaPath();
        rutaPortadaSeleccionada = rutaPortadaActual;
        actualizarPreviewPortada(rutaPortadaActual);
    }

    private void selectComboById(JComboBox<CatalogItem> cb, Integer id) {
        if (id == null) return;
        for (int i = 0; i < cb.getItemCount(); i++) {
            CatalogItem it = cb.getItemAt(i);
            if (it.getId().equals(id)) {
                cb.setSelectedIndex(i);
                return;
            }
        }
    }

    private void guardar() {
        try {
            int id = Validators.parseInt(txtId.getText(), "ID", 1, Integer.MAX_VALUE);
            String titulo = Validators.requireNonBlankTrimmed(txtTitulo.getText(), "Título");
            String director = Validators.requireNonBlankTrimmed(txtDirector.getText(), "Director");
            int anio = Validators.parseInt(txtAnio.getText(), "Año", 1900, 2100);
            int dur = Validators.parseInt(txtDuracion.getText(), "Duración", 1, 600);
            CatalogItem g = (CatalogItem) cbGenero.getSelectedItem();
            CatalogItem c = (CatalogItem) cbClasif.getSelectedItem();
            if (g == null || c == null) {
                Dialogs.error("Debes seleccionar género y clasificación");
                return;
            }

            if (dao.existeTitulo(titulo, id)) {
                Dialogs.error("Ya existe otra película con ese título.");
                return;
            }

            Pelicula p = new Pelicula(id, titulo, director, g.getId(), g.getNombre(),
                    anio, dur, c.getId(), c.getNombre());
            boolean updated = dao.modificar(p);
            if (!updated) {
                Dialogs.error("No se encontró la película para actualizar.");
                return;
            }
            if ((rutaPortadaSeleccionada != null && !rutaPortadaSeleccionada.equals(rutaPortadaActual))
                    || (rutaPortadaSeleccionada == null && rutaPortadaActual != null)) {
                dao.actualizarPortada(id, rutaPortadaSeleccionada);
                rutaPortadaActual = rutaPortadaSeleccionada;
            }
            Dialogs.info("Cambios guardados.");
            filtrar();
        } catch (IllegalArgumentException ex) {
            Dialogs.error(ex.getMessage());
        } catch (SQLIntegrityConstraintViolationException ex) {
            Dialogs.error("Ya existe otra película con ese título.");
        } catch (SQLException ex) {
            Dialogs.error("No se pudo actualizar: " + ex.getMessage(), ex);
        }
    }

    private void limpiar() {
        txtId.setText("");
        txtTitulo.setText("");
        txtDirector.setText("");
        txtAnio.setText("");
        txtDuracion.setText("");
        if (cbGenero.getItemCount() > 0) cbGenero.setSelectedIndex(0);
        if (cbClasif.getItemCount() > 0) cbClasif.setSelectedIndex(0);
        listModel.clear();
        lblResultados.setText("Coincidencias");
        rutaPortadaActual = null;
        rutaPortadaSeleccionada = null;
        actualizarPreviewPortada(null);
    }

    private void actualizarPreviewPortada(String ruta) {
        lblPortada.setIcon(Images.loadThumb(ruta, 96, 144));
    }
}
