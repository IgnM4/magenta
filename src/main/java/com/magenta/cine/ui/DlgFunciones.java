package com.magenta.cine.ui;

import com.magenta.cine.dao.FuncionDao;
import com.magenta.cine.model.Funcion;
import com.magenta.cine.model.Pelicula;
import com.magenta.cine.ui.model.FuncionTableModel;
import com.magenta.cine.util.Dialogs;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DlgFunciones extends JDialog {

    private final FuncionDao dao = new FuncionDao();
    private final Pelicula pelicula;

    private final JSpinner spFecha;
    private final JSpinner spHora;
    private final JTextField txtSala = new JTextField(12);
    private final JFormattedTextField txtPrecio = new JFormattedTextField(new DecimalFormat("#,##0.00"));
    private final FuncionTableModel model = new FuncionTableModel();
    private final JTable table = new JTable(model);
    private final JLabel lblContador = new JLabel();
    private final JButton btnAgregar = new JButton("Agregar función");
    private final JButton btnEliminar = new JButton("Eliminar");

    public DlgFunciones(Window owner, Pelicula pelicula) {
        super(owner, "Funciones - " + pelicula.getTitulo(), ModalityType.APPLICATION_MODAL);
        this.pelicula = pelicula;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(owner);

        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        spFecha = new JSpinner(dateModel);
        spFecha.setEditor(new JSpinner.DateEditor(spFecha, "yyyy-MM-dd"));

        SpinnerDateModel timeModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE);
        spHora = new JSpinner(timeModel);
        spHora.setEditor(new JSpinner.DateEditor(spHora, "HH:mm"));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel lblTitulo = new JLabel(pelicula.getTitulo(), SwingConstants.LEFT);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(lblTitulo.getFont().getStyle() | java.awt.Font.BOLD, 16f));
        top.add(lblTitulo, BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filtros.add(new JLabel("Fecha:"));
        filtros.add(spFecha);
        filtros.add(new JLabel("Hora:"));
        filtros.add(spHora);
        filtros.add(new JLabel("Sala:"));
        filtros.add(txtSala);
        filtros.add(new JLabel("Precio:"));
        txtPrecio.setColumns(8);
        filtros.add(txtPrecio);
        filtros.add(btnAgregar);
        top.add(filtros, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(new EmptyBorder(8, 12, 8, 12));
        lblContador.setHorizontalAlignment(SwingConstants.LEFT);
        bottom.add(lblContador, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEliminar.setEnabled(false);
        actions.add(btnEliminar);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        actions.add(btnCerrar);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarFuncion());
        btnEliminar.addActionListener(e -> eliminarFuncion());
        spFecha.addChangeListener(e -> cargarFunciones());

        ajustarColumnas();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnEliminar.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        txtSala.setText("");
        txtPrecio.setValue(null);
        cargarFunciones();
    }

    private void ajustarColumnas() {
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(100);
        columns.getColumn(1).setPreferredWidth(200);
        columns.getColumn(2).setPreferredWidth(100);
    }

    private void cargarFunciones() {
        LocalDate fecha = getFechaSeleccionada();
        btnAgregar.setEnabled(false);
        btnEliminar.setEnabled(false);
        new SwingWorker<List<Funcion>, Void>() {
            @Override
            protected List<Funcion> doInBackground() throws Exception {
                return dao.listarPorPeliculaYFecha(peliculaId(), fecha);
            }

            @Override
            protected void done() {
                try {
                    List<Funcion> funciones = get();
                    model.setData(funciones);
                    actualizarEstado(funciones.size());
                } catch (Exception ex) {
                    actualizarEstado(model.getRowCount());
                    Dialogs.error("No se pudieron cargar las funciones: " + ex.getMessage(), ex);
                }
            }
        }.execute();
    }

    private void actualizarEstado(int cantidad) {
        lblContador.setText(String.format("Funciones registradas: %d / 7", cantidad));
        btnAgregar.setEnabled(cantidad < 7);
        btnEliminar.setEnabled(table.getSelectedRow() >= 0);
    }

    private void agregarFuncion() {
        LocalDate fecha = getFechaSeleccionada();
        LocalTime hora = getHoraSeleccionada();
        String sala = txtSala.getText().trim();
        BigDecimal precio = null;
        String precioTexto = txtPrecio.getText() == null ? "" : txtPrecio.getText().trim();
        if (!precioTexto.isEmpty()) {
            try {
                txtPrecio.commitEdit();
                Object value = txtPrecio.getValue();
                if (value instanceof Number number) {
                    precio = toBigDecimal(number);
                } else if (value != null) {
                    precio = new BigDecimal(value.toString());
                }
            } catch (ParseException ex) {
                Dialogs.warn("Precio inválido. Usa números con decimales opcionales.");
                return;
            }
            if (precio != null) {
                precio = precio.setScale(2, RoundingMode.HALF_UP);
                if (precio.signum() < 0) {
                    Dialogs.warn("El precio no puede ser negativo.");
                    return;
                }
            }
        }

        Funcion nueva = new Funcion(peliculaId(), fecha, hora, sala.isBlank() ? null : sala, precio);
        btnAgregar.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.crear(nueva);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.info("Función agregada.");
                    cargarFunciones();
                } catch (Exception ex) {
                    btnAgregar.setEnabled(true);
                    if (ex.getCause() instanceof SQLException sqlEx) {
                        Dialogs.warn(sqlEx.getMessage());
                    } else if (ex instanceof SQLException sqlEx) {
                        Dialogs.warn(sqlEx.getMessage());
                    } else {
                        Dialogs.error("No se pudo agregar la función: " + ex.getMessage(), ex);
                    }
                }
            }
        }.execute();
    }

    private void eliminarFuncion() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Dialogs.warn("Selecciona una función para eliminar.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Funcion funcion = model.get(modelRow);
        if (!Dialogs.confirm("¿Eliminar la función de las " + funcion.getHora() + "?")) {
            return;
        }

        btnEliminar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                dao.eliminar(funcion.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.info("Función eliminada.");
                    cargarFunciones();
                } catch (Exception ex) {
                    btnEliminar.setEnabled(true);
                    Dialogs.error("No se pudo eliminar la función: " + ex.getMessage(), ex);
                }
            }
        }.execute();
    }

    private LocalDate getFechaSeleccionada() {
        Date date = (Date) spFecha.getValue();
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime getHoraSeleccionada() {
        Date date = (Date) spHora.getValue();
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private long peliculaId() {
        if (pelicula.getId() == null) {
            throw new IllegalStateException("La película no tiene ID asignado.");
        }
        return pelicula.getId().longValue();
    }

    private BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal big) {
            return big;
        }
        if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte) {
            return BigDecimal.valueOf(number.longValue());
        }
        return BigDecimal.valueOf(number.doubleValue());
    }
}
