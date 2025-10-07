
package com.magenta.cine.app;

import com.magenta.cine.ui.FrmCrearPelicula;
import com.magenta.cine.ui.FrmEliminarPelicula;
import com.magenta.cine.ui.FrmListarPeliculas;
import com.magenta.cine.ui.FrmModificarPelicula;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainMenu extends JFrame {
    private final JLabel lblStatus = new JLabel();

    public MainMenu() {
        this("Listo para operar.");
    }

    public MainMenu(String statusMessage) {
        super("Magenta | Menú principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 260);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(520, 240));

        JMenuBar bar = new JMenuBar();
        JMenu mPeliculas = new JMenu("Películas");
        JMenuItem miCrear = new JMenuItem("Crear");
        JMenuItem miListar = new JMenuItem("Listar (JTable + filtros)");
        JMenuItem miModificar = new JMenuItem("Modificar");
        JMenuItem miEliminar = new JMenuItem("Eliminar");
        mPeliculas.add(miCrear);
        mPeliculas.add(miListar);
        mPeliculas.add(miModificar);
        mPeliculas.add(miEliminar);
        bar.add(mPeliculas);
        setJMenuBar(bar);

        JPanel center = new JPanel(new BorderLayout());
        JLabel lblHint = new JLabel("Usa el menú Películas para navegar", SwingConstants.CENTER);
        lblHint.setBorder(new EmptyBorder(12, 12, 12, 12));
        center.add(lblHint, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblStatus.setText(statusMessage);
        lblStatus.setForeground(new Color(34, 139, 34));
        statusPanel.add(lblStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        miCrear.addActionListener(e -> new FrmCrearPelicula().setVisible(true));
        miListar.addActionListener(e -> new FrmListarPeliculas().setVisible(true));
        miModificar.addActionListener(e -> new FrmModificarPelicula().setVisible(true));
        miEliminar.addActionListener(e -> new FrmEliminarPelicula().setVisible(true));
    }
}
