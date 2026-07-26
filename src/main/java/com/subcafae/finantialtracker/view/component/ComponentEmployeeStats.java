package com.subcafae.finantialtracker.view.component;

import com.subcafae.finantialtracker.data.dao.EmployeeStatsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeStatsDao.EmployeeStatsDto;
import com.subcafae.finantialtracker.util.LoadingOverlay;
import com.subcafae.finantialtracker.view.ViewMain;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Dashboard de estadisticas de un empleado (DNI peruano o extranjeria).
 *
 * Carga datos en background con LoadingOverlay y popula 4 tarjetas
 * numericas mas 4 graficos JFreeChart: 2 pies (estados de prestamos
 * y abonos) y 2 barras (cantidad mes a mes en el ultimo ano).
 */
public class ComponentEmployeeStats extends JInternalFrame {

    private static final Color BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(45, 55, 72);
    private static final Color ACCENT_PRIMARY = new Color(99, 102, 241);
    private static final Color ACCENT_SUCCESS = new Color(16, 185, 129);
    private static final Color ACCENT_WARNING = new Color(245, 158, 11);
    private static final Color ACCENT_INFO = new Color(59, 130, 246);

    private final String dni;
    private final ViewMain viewMain;

    private final JLabel headerNombre = new JLabel();
    private final JLabel headerSubtitulo = new JLabel();
    private final JLabel cardLoansValue = new JLabel("-");
    private final JLabel cardRefinancesValue = new JLabel("-");
    private final JLabel cardAbonosCountValue = new JLabel("-");
    private final JLabel cardAbonosMontoValue = new JLabel("-");
    private final JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 12, 12));

    public ComponentEmployeeStats(String dni, ViewMain viewMain) {
        super("ESTADISTICAS EMPLEADO - " + dni, true, true, true, true);
        this.dni = dni;
        this.viewMain = viewMain;
        construirLayout();
        setSize(1100, 700);
        cargarDatosEnBackground();
    }

    private void construirLayout() {
        getContentPane().setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_PRIMARY),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        headerNombre.setFont(new Font("Roboto", Font.BOLD, 18));
        headerNombre.setForeground(TEXT_DARK);
        headerNombre.setText("Cargando empleado " + dni + "...");
        headerSubtitulo.setFont(new Font("Roboto", Font.PLAIN, 12));
        headerSubtitulo.setForeground(new Color(107, 114, 128));
        JPanel headerText = new JPanel(new BorderLayout(0, 4));
        headerText.setOpaque(false);
        headerText.add(headerNombre, BorderLayout.NORTH);
        headerText.add(headerSubtitulo, BorderLayout.CENTER);
        header.add(headerText, BorderLayout.CENTER);
        getContentPane().add(header, BorderLayout.NORTH);

        // Cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        cardsRow.add(crearTarjeta("Prestamos totales", cardLoansValue, ACCENT_INFO));
        cardsRow.add(crearTarjeta("Refinanciamientos", cardRefinancesValue, ACCENT_WARNING));
        cardsRow.add(crearTarjeta("Cantidad de abonos", cardAbonosCountValue, ACCENT_PRIMARY));
        cardsRow.add(crearTarjeta("Monto total abonos", cardAbonosMontoValue, ACCENT_SUCCESS));

        // Charts panel
        chartsPanel.setOpaque(false);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(cardsRow, BorderLayout.NORTH);
        center.add(chartsPanel, BorderLayout.CENTER);
        getContentPane().add(center, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CARD_BORDER, 1),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18))));
        JLabel tit = new JLabel(titulo);
        tit.setFont(new Font("Roboto", Font.PLAIN, 11));
        tit.setForeground(new Color(107, 114, 128));
        valueLabel.setFont(new Font("Roboto", Font.BOLD, 26));
        valueLabel.setForeground(TEXT_DARK);
        card.add(tit, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void cargarDatosEnBackground() {
        LoadingOverlay.setMessage("Cargando estadisticas de " + dni);
        ViewMain.loading.setModal(true);
        ViewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                final EmployeeStatsDto stats = new EmployeeStatsDao().getStats(dni);
                SwingUtilities.invokeLater(() -> {
                    ViewMain.loading.dispose();
                    if (stats == null) {
                        JOptionPane.showMessageDialog(viewMain,
                                "No se encontro empleado con DNI: " + dni,
                                "ESTADISTICAS EMPLEADO", JOptionPane.WARNING_MESSAGE);
                        dispose();
                        return;
                    }
                    aplicarStats(stats);
                });
            } catch (Exception ex) {
                System.out.println("Error -> " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    ViewMain.loading.dispose();
                    JOptionPane.showMessageDialog(viewMain,
                            "Error cargando estadisticas: " + ex.getMessage(),
                            "ESTADISTICAS EMPLEADO", JOptionPane.ERROR_MESSAGE);
                    dispose();
                });
            }
        }).start();

        ViewMain.loading.setVisible(true);
    }

    private void aplicarStats(EmployeeStatsDto stats) {
        String nombreCompleto = stats.empleado.getFullName() == null ? "" : stats.empleado.getFullName();
        String tipo = stats.empleado.getEmploymentStatus() == null ? "-" : stats.empleado.getEmploymentStatus();
        headerNombre.setText(nombreCompleto + " (" + dni + ")");
        headerSubtitulo.setText("Tipo: " + tipo);

        cardLoansValue.setText(String.valueOf(stats.totalLoans));
        cardRefinancesValue.setText(String.valueOf(stats.totalRefinancings));
        cardAbonosCountValue.setText(String.valueOf(stats.totalAbonos));
        cardAbonosMontoValue.setText(formatoMoneda(stats.totalAbonosMonto));

        chartsPanel.removeAll();
        chartsPanel.add(crearChartPanel(buildPieChart("Prestamos por estado", stats.loansByState)));
        chartsPanel.add(crearChartPanel(buildPieChart("Abonos por estado", stats.abonosByState)));
        chartsPanel.add(crearChartPanel(buildBarChart("Prestamos por mes", "Prestamos", stats.loansByMonth, ACCENT_INFO)));
        chartsPanel.add(crearChartPanel(buildBarChart("Abonos por mes", "Abonos", stats.abonosByMonth, ACCENT_SUCCESS)));
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private static String formatoMoneda(double valor) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
        return nf.format(valor);
    }

    private static ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(450, 280));
        cp.setBackground(BG);
        cp.setBorder(BorderFactory.createLineBorder(CARD_BORDER, 1));
        cp.setMouseWheelEnabled(false);
        cp.setDomainZoomable(false);
        cp.setRangeZoomable(false);
        return cp;
    }

    private static JFreeChart buildPieChart(String titulo, Map<String, Integer> datos) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        if (datos.isEmpty()) {
            ds.setValue("Sin datos", 1);
        } else {
            datos.forEach(ds::setValue);
        }
        JFreeChart chart = ChartFactory.createPieChart(titulo, ds, true, true, false);
        chart.setBackgroundPaint(BG);
        chart.getTitle().setFont(new Font("Roboto", Font.BOLD, 13));
        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("Roboto", Font.PLAIN, 10));
        plot.setShadowPaint(null);
        return chart;
    }

    private static JFreeChart buildBarChart(String titulo, String serie, Map<String, Integer> datos, Color color) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        if (datos.isEmpty()) {
            ds.addValue(0, serie, "");
        } else {
            datos.forEach((mes, cant) -> ds.addValue(cant, serie, mes));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                titulo, "Mes", "Cantidad", ds,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(BG);
        chart.getTitle().setFont(new Font("Roboto", Font.BOLD, 13));
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(BG);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(229, 231, 235));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, color);
        renderer.setShadowVisible(false);
        plot.getDomainAxis().setTickLabelFont(new Font("Roboto", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Roboto", Font.PLAIN, 10));
        return chart;
    }
}
