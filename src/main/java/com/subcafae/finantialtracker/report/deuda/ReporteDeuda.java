package com.subcafae.finantialtracker.report.deuda;

import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class ReporteDeuda {

    private String numSoli;

    //Bono
    private String conceptBono;

    //Loan
    private String detalleCouta;
    private String fechaVencimiento;
    private String monto;

    private String fondo;

    public String getFondo() {
        return fondo;
    }

    public void setFondo(String fondo) {
        this.fondo = fondo;
    }

    public String getNumSoli() {
        return numSoli;
    }

    public void setNumSoli(String numSoli) {
        this.numSoli = numSoli;
    }

    public String getConceptBono() {
        return conceptBono;
    }

    public void setConceptBono(String conceptBono) {
        this.conceptBono = conceptBono;
    }

    public String getDetalleCouta() {
        return detalleCouta;
    }

    public void setDetalleCouta(String detalleCouta) {
        this.detalleCouta = detalleCouta;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getMonto() {
        return monto;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }

    public void reporteDeuda(
            String nameEmployee,
            String dni,
            Map<AbonoTb, List<ReporteDeuda>> lisComAbono, Map<LoanTb, List<ReporteDeuda>> listComLoan
    ) throws SQLException {

        try {
            // Parámetros para el reporte
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("name_lastname_var", nameEmployee);
            parametros.put("dni_var", dni);

            // Lista principal de datos para el reporte
            List<Map<String, Object>> dataList = new ArrayList<>();

            // Procesar sección de abonos
            procesarSeccionAbonos(dataList, lisComAbono);

            // Procesar sección de préstamos
            procesarSeccionPrestamos(dataList, listComLoan);

            if (dataList.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontraron deudas para este empleado.");
                return;
            }

            // Cargar y compilar el reporte
            InputStream reporteStream = getClass().getResourceAsStream("/reports/report_history_debt.jrxml");

            JasperReport jasperReport = JasperCompileManager.compileReport(reporteStream);

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // Crear visor
            JasperViewer jasperViewer = new JasperViewer(jasperPrint, false);

            // Configurar ventana
            JFrame frame = (JFrame) jasperViewer.getContentPane().getParent().getParent().getParent();
            frame.setTitle("REPORTE DE DEUDA");

            ImageIcon icon = new ImageIcon(getClass().getResource("/IconGeneral/logoIcon.png"));
            frame.setIconImage(icon.getImage());

            jasperViewer.setVisible(true);
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(true);
            frame.setAlwaysOnTop(false);

        } catch (JRException ex) {
            Logger.getLogger(ReporteDeuda.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error al generar el reporte: " + ex.getMessage());
        }
    }

    // Método para procesar abonos
    private void procesarSeccionAbonos(List<Map<String, Object>> dataList, Map<AbonoTb, List<ReporteDeuda>> lisComAbono) {
        if (lisComAbono.isEmpty()) {
            return;
        }

        for (Map.Entry<AbonoTb, List<ReporteDeuda>> entry : lisComAbono.entrySet()) {
            Map<String, Object> fila = new HashMap<>();

            double totalAbonos = 0.0;
            List<Map<String, Object>> detalles = new ArrayList<>();

            for (int i = 0; i < entry.getValue().size(); i++) {
                Map<String, Object> detalle = new HashMap<>();
                detalle.put("details_var", entry.getValue().get(i).getDetalleCouta() + "/" + entry.getValue().size());
                detalle.put("vencimiento_var", entry.getValue().get(i).getFechaVencimiento());
                detalle.put("amount_var", entry.getValue().get(i).getMonto());
                detalles.add(detalle);

                totalAbonos += Double.parseDouble(entry.getValue().get(i).getMonto());
            }

            fila.put("section_var", "SECCIÓN DE ABONOS");
            fila.put("concepto_var", entry.getValue().get(0).getConceptBono());
            fila.put("num_solicitud_var", entry.getValue().get(0).getNumSoli());
            fila.put("num_cuotas_pendiente_var", String.valueOf(entry.getValue().size()));
            fila.put("vencimiento_information_var", entry.getValue().get(entry.getValue().size() - 1).getFechaVencimiento());
            fila.put("total_var", String.format("%.2f", totalAbonos));
            fila.put("detalles", detalles);

            dataList.add(fila);
        }
    }

    // Método para procesar préstamos
    private void procesarSeccionPrestamos(List<Map<String, Object>> dataList, Map<LoanTb, List<ReporteDeuda>> listComLoan) {
        if (listComLoan.isEmpty()) {
            return;
        }

        for (Map.Entry<LoanTb, List<ReporteDeuda>> entry : listComLoan.entrySet()) {
            // Préstamo principal
            Map<String, Object> filaPrestamo = new HashMap<>();

            double totalPrestamo = 0.0;
            double totalFondo = 0.0;
            List<Map<String, Object>> detallesPrestamo = new ArrayList<>();
            List<Map<String, Object>> detallesFondo = new ArrayList<>();

            for (int i = 0; i < entry.getValue().size(); i++) {
                ReporteDeuda item = entry.getValue().get(i);

                // Detalle del préstamo
                Map<String, Object> detallePrestamo = new HashMap<>();
                double montoMenosFondo = Double.parseDouble(item.getMonto());
                if (item.getFondo() != null) {
                    montoMenosFondo -= Double.parseDouble(item.getFondo());
                }
                detallePrestamo.put("details_var", item.getDetalleCouta() + "/" + entry.getValue().size());
                detallePrestamo.put("vencimiento_var", item.getFechaVencimiento());
                detallePrestamo.put("amount_var", String.format("%.2f", montoMenosFondo));
                detallesPrestamo.add(detallePrestamo);

                totalPrestamo += Double.parseDouble(item.getMonto());

                // Detalle del fondo intangible
                if (item.getFondo() != null) {
                    Map<String, Object> detalleFondo = new HashMap<>();
                    detalleFondo.put("details_var", item.getDetalleCouta() + "/" + entry.getValue().size());
                    detalleFondo.put("vencimiento_var", item.getFechaVencimiento());
                    detalleFondo.put("amount_var", item.getFondo());
                    detallesFondo.add(detalleFondo);

                    totalFondo += Double.parseDouble(item.getFondo());
                }
            }

            // Agregar fila del préstamo
            filaPrestamo.put("section_var", "SECCIÓN DE PRÉSTAMOS");
            filaPrestamo.put("concepto_var", "PRÉSTAMO " + entry.getValue().get(0).getNumSoli());
            filaPrestamo.put("num_solicitud_var", entry.getValue().get(0).getNumSoli());
            filaPrestamo.put("num_cuotas_pendiente_var", String.valueOf(entry.getValue().size()));
            filaPrestamo.put("vencimiento_information_var", entry.getValue().get(entry.getValue().size() - 1).getFechaVencimiento());
            filaPrestamo.put("total_var", String.format("%.2f", totalPrestamo));
            filaPrestamo.put("detalles", detallesPrestamo);

            dataList.add(filaPrestamo);

            // Agregar fila del fondo intangible si hay datos
            if (!detallesFondo.isEmpty()) {
                Map<String, Object> filaFondo = new HashMap<>();
                filaFondo.put("section_var", "SECCIÓN DE PRÉSTAMOS");
                filaFondo.put("concepto_var", "Fondo Intangible");
                filaFondo.put("num_solicitud_var", entry.getValue().get(0).getNumSoli());
                filaFondo.put("num_cuotas_pendiente_var", String.valueOf(detallesFondo.size()));
                filaFondo.put("vencimiento_information_var", entry.getValue().get(entry.getValue().size() - 1).getFechaVencimiento());
                filaFondo.put("total_var", String.format("%.2f", totalFondo));
                filaFondo.put("detalles", detallesFondo);

                dataList.add(filaFondo);
            }
        }
    }
}
