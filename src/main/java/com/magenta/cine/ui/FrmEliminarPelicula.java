
package com.magenta.cine.ui;

import com.magenta.cine.dao.PeliculaDao;
import com.magenta.cine.model.Pelicula;
import com.magenta.cine.util.Dialogs;
import com.magenta.cine.util.Validators;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class FrmEliminarPelicula extends JFrame {
    private final PeliculaDao dao = new PeliculaDao();
    private final JTextField txtId = new JTextField(8);
    private final JTextArea txtDetalle = new JTextArea(5,34);

    public FrmEliminarPelicula() {
        super("Magenta | Eliminar película");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        txtDetalle.setEditable(false);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);

        JPanel content = new JPanel(new BorderLayout(8,8));
        content.setBorder(new EmptyBorder(12,12,12,12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.add(new JLabel("ID:")); top.add(txtId);
        JButton btnBuscar = new JButton("Buscar"); btnBuscar.setMnemonic('B'); top.add(btnBuscar);

        JPanel center = new JPanel(new BorderLayout());
        center.add(new JScrollPane(txtDetalle), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnEliminar = new JButton("Eliminar"); btnEliminar.setMnemonic('E');
        bottom.add(btnLimpiar); bottom.add(btnEliminar);

        content.add(top, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        pack(); setLocationRelativeTo(null);

        btnBuscar.addActionListener(e -> buscar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private void buscar() {
        try {
            int id = Validators.parseInt(txtId.getText(), "ID", 1, Integer.MAX_VALUE);
            Pelicula p = dao.buscarPorId(id);
            if (p == null) { txtDetalle.setText("No existe película con ID=" + id); }
            else { txtDetalle.setText(p.toString()); }
        } catch (IllegalArgumentException ex) {
            Dialogs.error(ex.getMessage());
        } catch (SQLException ex) {
            Dialogs.error("Error buscando película: " + ex.getMessage(), ex);
        }
    }

    private void eliminar() {
        try {
            int id = Validators.parseInt(txtId.getText(), "ID", 1, Integer.MAX_VALUE);
            if (!Dialogs.confirm("¿Eliminar película ID=" + id + "?")) return;
            boolean deleted = dao.eliminar(id);
            if (!deleted) {
                Dialogs.info("La película ya no existe en el catálogo.");
                txtDetalle.setText("");
                return;
            }
            Dialogs.info("Película eliminada.");
            limpiar();
        } catch (IllegalArgumentException ex) {
            Dialogs.error(ex.getMessage());
        } catch (SQLException ex) {
            Dialogs.error("No se pudo eliminar: " + ex.getMessage(), ex);
        }
    }

    private void limpiar() { txtId.setText(""); txtDetalle.setText(""); }
}
