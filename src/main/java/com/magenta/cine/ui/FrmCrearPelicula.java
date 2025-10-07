
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

public class FrmCrearPelicula extends JFrame {
    private final PeliculaDao dao = new PeliculaDao();
    private final JTextField txtTitulo = new JTextField(25);
    private final JTextField txtDirector = new JTextField(20);
    private final JComboBox<CatalogItem> cbGenero = new JComboBox<>();
    private final JTextField txtAnio = new JTextField(6);
    private final JTextField txtDuracion = new JTextField(6);
    private final JComboBox<CatalogItem> cbClasif = new JComboBox<>();
    private final JLabel lblPortada = new JLabel();

    private String rutaPortadaSeleccionada;

    public FrmCrearPelicula() {
        super("Magenta | Crear película");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setResizable(true);

        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

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
        actualizarPreviewPortada(null);
        JButton btnSeleccionarPortada = new JButton("Seleccionar portada…");
        btnSeleccionarPortada.addActionListener(e -> seleccionarPortada());
        panelPortada.add(lblPortada, BorderLayout.CENTER);
        panelPortada.add(btnSeleccionarPortada, BorderLayout.SOUTH);
        addRow(form, gbc, 6, "Portada:", panelPortada);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setMnemonic('L');
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setMnemonic('G');
        bottom.add(btnLimpiar); bottom.add(btnGuardar);

        content.add(form, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
        setLocationRelativeTo(null);

        txtTitulo.setToolTipText("Ingresa un título único");
        txtAnio.setToolTipText("Entre 1900 y 2100");
        txtDuracion.setToolTipText("Duración en minutos (1-600)");

        getRootPane().setDefaultButton(btnGuardar);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());

        SwingUtilities.invokeLater(this::cargarCatalogos);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1;
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

    private void guardar() {
        try {
            String titulo = Validators.requireNonBlankTrimmed(txtTitulo.getText(), "Título");
            String director = Validators.requireNonBlankTrimmed(txtDirector.getText(), "Director");
            int anio = Validators.parseInt(txtAnio.getText(), "Año", 1900, 2100);
            int dur = Validators.parseInt(txtDuracion.getText(), "Duración", 1, 600);

            CatalogItem g = (CatalogItem) cbGenero.getSelectedItem();
            CatalogItem c = (CatalogItem) cbClasif.getSelectedItem();
            if (g == null || c == null) { Dialogs.error("Debes seleccionar género y clasificación"); return; }

            if (dao.existeTitulo(titulo, null)) {
                Dialogs.error("Ya existe una película con ese título.");
                return;
            }

            Pelicula p = new Pelicula(null, titulo, director, g.getId(), g.getNombre(),
                    anio, dur, c.getId(), c.getNombre());
            int nuevoId = dao.crear(p);
            if (nuevoId > 0 && rutaPortadaSeleccionada != null) {
                dao.actualizarPortada(nuevoId, rutaPortadaSeleccionada);
            }
            Dialogs.info("Película creada.");
            limpiar();
        } catch (IllegalArgumentException ex) {
            Dialogs.error(ex.getMessage());
        } catch (SQLIntegrityConstraintViolationException ex) {
            Dialogs.error("Ya existe una película con ese título.");
        } catch (SQLException ex) {
            Dialogs.error("No se pudo crear la película: " + ex.getMessage(), ex);
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

    private void limpiar() {
        txtTitulo.setText(""); txtDirector.setText(""); txtAnio.setText(""); txtDuracion.setText("");
        if (cbGenero.getItemCount() > 0) cbGenero.setSelectedIndex(0);
        if (cbClasif.getItemCount() > 0) cbClasif.setSelectedIndex(0);
        rutaPortadaSeleccionada = null;
        actualizarPreviewPortada(null);
    }

    private void actualizarPreviewPortada(String ruta) {
        lblPortada.setIcon(Images.loadThumb(ruta, 96, 144));
    }
}
