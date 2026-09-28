package com.subcafae.finantialtracker.view.component;

import com.subcafae.finantialtracker.data.dao.DeudaTiendaDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * DEUDAS TIENDA (Sub Cafe) — vista en tiempo real de lo que los trabajadores
 * compraron a credito en la tienda. Lee la misma base de datos; no escribe.
 *
 * Pestanas:
 *   1. Deudores      : trabajador, pendiente del mes, acumulado, total + sus compras
 *   2. Compras       : ultimas compras a credito registradas por el POS o a mano
 *   3. Cierres       : lo que la tienda cerro por mes (candidato a descuento en planilla)
 *
 * Se refresca sola cada 10 segundos mientras esta abierta.
 */
public class ComponentDeudasTienda extends JInternalFrame {

    private static final int INTERVALO_MS = 10_000;
    private static final SimpleDateFormat FMT_FECHA = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    // OJO: el "." de "S/." no puede ir dentro del patron de DecimalFormat
    // (lo toma como separador decimal). El prefijo se agrega en soles().
    private static final DecimalFormat FMT_NUMERO = new DecimalFormat("#,##0.00",
            java.text.DecimalFormatSymbols.getInstance(java.util.Locale.US));

    private static String soles(BigDecimal v) {
        return "S/. " + FMT_NUMERO.format(v == null ? BigDecimal.ZERO : v);
    }

    private final DeudaTiendaDao dao = new DeudaTiendaDao();
    private final Timer timer = new Timer(INTERVALO_MS, e -> refrescar());

    private final JLabel lblDeudores = kpi("0", "Trabajadores con deuda");
    private final JLabel lblPendiente = kpi("S/. 0.00", "Pendiente del mes");
    private final JLabel lblAcumulado = kpi("S/. 0.00", "Acumulado (cerrado)");
    private final JLabel lblTotal = kpi("S/. 0.00", "Deuda total");
    private final JLabel lblEstado = new JLabel(" ");
    private final JTextField txtFiltro = new JTextField(22);
    private final JCheckBox chkAuto = new JCheckBox("Auto-actualizar (10 s)", true);
    private final JButton btnActualizar = new JButton("Actualizar");

    private final DefaultTableModel mDeudores = modelo(
            "DNI", "Trabajador", "Condicion", "Empleado #", "Pendiente mes", "Consumos", "Ultimo consumo", "Acumulado", "TOTAL");
    private final DefaultTableModel mMovs = modelo(
            "Fecha", "Descripcion / compra", "Monto", "Origen", "Estado", "Registro", "Periodo");
    private final DefaultTableModel mRecientes = modelo(
            "Fecha", "DNI", "Trabajador", "Compra / consumo", "Monto", "Origen", "Estado");
    private final DefaultTableModel mCierres = modelo(
            "Periodo", "Fecha cierre", "DNI", "Trabajador", "Empleado #", "Consumos", "Monto", "Estado en FinantialTracker");

    private final JTable tDeudores = tabla(mDeudores);
    private final JTable tMovs = tabla(mMovs);
    private final JTable tRecientes = tabla(mRecientes);
    private final JTable tCierres = tabla(mCierres);

    /** ids de cliente en el mismo orden que las filas de tDeudores. */
    private final java.util.List<String> idsDeudores = new java.util.ArrayList<>();
    private String clienteSeleccionado;
    private boolean cargando;

    public ComponentDeudasTienda() {
        super("DEUDAS TIENDA - SUB CAFE (tiempo real)", true, true, true, true);
        setSize(1120, 640);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        putClientProperty("JInternalFrame.titleBarBackground", new Color(244, 247, 252));

        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(construirCabecera(), BorderLayout.NORTH);
        getContentPane().add(construirTabs(), BorderLayout.CENTER);
        getContentPane().add(construirPie(), BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> refrescar());
        txtFiltro.addActionListener(e -> refrescar());
        chkAuto.addActionListener(e -> {
            if (chkAuto.isSelected()) timer.start(); else timer.stop();
        });
        tDeudores.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tDeudores.getSelectedRow();
            if (row >= 0 && row < idsDeudores.size()) {
                clienteSeleccionado = idsDeudores.get(tDeudores.convertRowIndexToModel(row));
                cargarMovimientos();
            }
        });
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                timer.stop();
            }
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                iniciar();
            }
        });
    }

    /** Llamar al mostrar el componente: carga datos y arranca el refresco. */
    public void iniciar() {
        refrescar();
        if (chkAuto.isSelected()) timer.restart();
    }

    // ───────────────────────── UI ─────────────────────────

    private JPanel construirCabecera() {
        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjeta(lblDeudores, new Color(26, 54, 93)));
        kpis.add(tarjeta(lblPendiente, new Color(197, 48, 48)));
        kpis.add(tarjeta(lblAcumulado, new Color(214, 158, 46)));
        kpis.add(tarjeta(lblTotal, new Color(44, 62, 80)));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barra.setOpaque(false);
        barra.add(new JLabel("Buscar (DNI o nombre):"));
        txtFiltro.putClientProperty("JComponent.roundRect", true);
        barra.add(txtFiltro);
        btnActualizar.putClientProperty("JButton.buttonType", "roundRect");
        barra.add(btnActualizar);
        barra.add(chkAuto);
        JLabel nota = new JLabel("Solo lectura: las deudas se registran en la tienda (POS Sub Cafe). "
                + "El descuento en planilla se activara mas adelante.");
        nota.setForeground(new Color(113, 128, 150));
        nota.setFont(nota.getFont().deriveFont(Font.PLAIN, 11f));
        barra.add(nota);

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        norte.setBackground(Color.WHITE);
        norte.add(kpis, BorderLayout.NORTH);
        norte.add(barra, BorderLayout.SOUTH);
        return norte;
    }

    private JTabbedPane construirTabs() {
        JTabbedPane tabs = new JTabbedPane();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                titulado(new JScrollPane(tDeudores), "Trabajadores con deuda (clic para ver sus compras)"),
                titulado(new JScrollPane(tMovs), "Compras / consumos del trabajador seleccionado"));
        split.setResizeWeight(0.6);
        split.setBorder(null);
        tabs.addTab("Deudores", split);
        tabs.addTab("Compras a credito (ultimas 200)", titulado(new JScrollPane(tRecientes),
                "Cada venta a credito del POS o consumo anotado a mano, en orden de llegada"));
        tabs.addTab("Cierres mensuales (planilla)", titulado(new JScrollPane(tCierres),
                "Total por trabajador de cada mes cerrado. Es lo que se descontara por planilla cuando se active la union"));
        return tabs;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pie.setBackground(new Color(247, 250, 252));
        lblEstado.setForeground(new Color(113, 128, 150));
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.PLAIN, 11f));
        pie.add(lblEstado);
        return pie;
    }

    private static JLabel kpi(String valor, String titulo) {
        JLabel l = new JLabel("<html><div style='text-align:center'><span style='font-size:18px'><b>" + valor
                + "</b></span><br/><span style='font-size:10px;color:#718096'>" + titulo + "</span></div></html>",
                SwingConstants.CENTER);
        l.putClientProperty("titulo", titulo);
        return l;
    }

    private static void setKpi(JLabel l, String valor) {
        l.setText("<html><div style='text-align:center'><span style='font-size:18px'><b>" + valor
                + "</b></span><br/><span style='font-size:10px;color:#718096'>" + l.getClientProperty("titulo")
                + "</span></div></html>");
    }

    private static JPanel tarjeta(JLabel contenido, Color acento) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, acento),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(6, 8, 6, 8))));
        p.setPreferredSize(new Dimension(200, 58));
        p.add(contenido, BorderLayout.CENTER);
        return p;
    }

    private static JPanel titulado(Component c, String titulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(null, titulo,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 12), new Color(26, 54, 93)));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private static JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        t.setRowHeight(26);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.putClientProperty("JTable.alternateRowColor", new Color(248, 250, 252));
        if (t.getTableHeader() != null) {
            t.getTableHeader().setReorderingAllowed(false);
            t.getTableHeader().setFont(new Font("Roboto", Font.BOLD, 12));
        }
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        t.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tb, v, sel, foc, r, c);
                String s = v == null ? "" : v.toString();
                setHorizontalAlignment(s.startsWith("S/.") ? SwingConstants.RIGHT : SwingConstants.LEFT);
                if (!sel) {
                    if (s.equals("Pendiente")) comp.setForeground(new Color(197, 48, 48));
                    else if (s.equals("Cerrado")) comp.setForeground(new Color(113, 128, 150));
                    else if (s.startsWith("Error")) comp.setForeground(new Color(197, 48, 48));
                    else if (s.startsWith("Abono")) comp.setForeground(new Color(49, 130, 206));
                    else comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        });
        return t;
    }

    // ───────────────────────── Datos ─────────────────────────

    private static String monto(Object o) {
        return o == null ? "" : soles((BigDecimal) o);
    }

    private static String fecha(Object o) {
        return o == null ? "" : FMT_FECHA.format((Timestamp) o);
    }

    private static String texto(Object o) {
        return o == null ? "" : o.toString();
    }

    /** Recarga todo en segundo plano (no bloquea la UI). */
    public synchronized void refrescar() {
        if (cargando) return;
        cargando = true;
        final String filtro = txtFiltro.getText();
        lblEstado.setText("Actualizando...");
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                if (!dao.tiendaInstalada()) return null;
                return new Object[]{dao.resumen(), dao.deudores(filtro), dao.recientes(200), dao.cierres()};
            }

            @Override
            protected void done() {
                cargando = false;
                try {
                    Object[] r = get();
                    if (r == null) {
                        lblEstado.setText("La tienda aun no ha creado sus tablas en esta base de datos (arranque el backend de la tienda).");
                        return;
                    }
                    pintarResumen((DeudaTiendaDao.Resumen) r[0]);
                    pintarDeudores((List<Object[]>) r[1]);
                    pintarRecientes((List<Object[]>) r[2]);
                    pintarCierres((List<Object[]>) r[3]);
                    lblEstado.setText("Actualizado " + new SimpleDateFormat("HH:mm:ss").format(new java.util.Date())
                            + (chkAuto.isSelected() ? "  ·  se refresca cada 10 s" : ""));
                    if (clienteSeleccionado != null) cargarMovimientos();
                } catch (Exception ex) {
                    lblEstado.setText("Error al leer las deudas de la tienda: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void pintarResumen(DeudaTiendaDao.Resumen r) {
        setKpi(lblDeudores, String.valueOf(r.deudores));
        setKpi(lblPendiente, soles(r.pendienteMes) + " (" + r.consumosMes + " compras)");
        setKpi(lblAcumulado, soles(r.deudaAcumulada));
        setKpi(lblTotal, soles(r.deudaTotal));
    }

    private void pintarDeudores(List<Object[]> filas) {
        String seleccionPrevia = clienteSeleccionado;
        mDeudores.setRowCount(0);
        idsDeudores.clear();
        int filaSel = -1;
        for (Object[] f : filas) {
            idsDeudores.add((String) f[0]);
            if (f[0].equals(seleccionPrevia)) filaSel = mDeudores.getRowCount();
            mDeudores.addRow(new Object[]{
                f[1], f[2], texto(f[3]), texto(f[4]), monto(f[5]), texto(f[6]), fecha(f[7]), monto(f[8]), monto(f[9])
            });
        }
        if (filaSel >= 0) {
            int vista = tDeudores.convertRowIndexToView(filaSel);
            tDeudores.setRowSelectionInterval(vista, vista);
        } else {
            clienteSeleccionado = null;
            mMovs.setRowCount(0);
        }
    }

    private void pintarRecientes(List<Object[]> filas) {
        mRecientes.setRowCount(0);
        for (Object[] f : filas) {
            mRecientes.addRow(new Object[]{fecha(f[0]), f[1], f[2], texto(f[3]), monto(f[4]), f[5], f[6]});
        }
    }

    private void pintarCierres(List<Object[]> filas) {
        mCierres.setRowCount(0);
        for (Object[] f : filas) {
            mCierres.addRow(new Object[]{f[0], fecha(f[1]), f[2], f[3], texto(f[4]), texto(f[5]), monto(f[6]), f[7]});
        }
    }

    private void cargarMovimientos() {
        final String id = clienteSeleccionado;
        if (id == null) return;
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return dao.movimientos(id);
            }

            @Override
            protected void done() {
                try {
                    if (!id.equals(clienteSeleccionado)) return;
                    mMovs.setRowCount(0);
                    for (Object[] f : get()) {
                        mMovs.addRow(new Object[]{fecha(f[0]), texto(f[1]), monto(f[2]), f[3], f[4], texto(f[5]), f[6]});
                    }
                } catch (Exception ex) {
                    lblEstado.setText("Error al leer movimientos: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
