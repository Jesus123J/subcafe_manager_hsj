/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.HistoryPayment;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDetailsDao;
import com.subcafae.finantialtracker.data.dao.RegistroDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.RegistroDetailsModel;
import com.subcafae.finantialtracker.model.ListDataReport;
import com.subcafae.finantialtracker.model.ReportPayementData;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Jesus Gutierrez
 */
public class HistoryPayment {

    private EmployeeTb firstEmployee;
    private final RegistroDao registroDAO = new RegistroDao();

    public List<ModelPayment> dataPaymentAndLoan(String dni) {

        List<ModelPayment> moPayments = new ArrayList<>();

        try {
            firstEmployee = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getNationalId().equalsIgnoreCase(dni)).findFirst().get();
        } catch (SQLException ex) {
            //  Logger.getLogger(HistoryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (firstEmployee == null) {
            return null;
        }

        List<RegistroDetailsModel> listRegister = registroDAO.findRegisterDetailsByEmployeeId(firstEmployee.getEmployeeId().toString());
        // Usar LinkedHashMap para mantener el orden de inserción (que viene ordenado por fecha DESC desde la query)
        Map<String, List<RegistroDetailsModel>> listFilte = listRegister.stream()
                .collect(Collectors.groupingBy(
                        RegistroDetailsModel::getCodigo,
                        java.util.LinkedHashMap::new,
                        Collectors.toList()
                ));
        System.out.println(listFilte.toString());
        for (Map.Entry<String, List<RegistroDetailsModel>> entry : listFilte.entrySet()) {

            Map<String, String> date = new HashMap<>();
            Map<String, String> mapAndPaymeny = new HashMap<>();

            for (RegistroDetailsModel registroDetailsModel : entry.getValue()) {
                // Verificar si es un préstamo (conceptLoan no es null)
                if (registroDetailsModel.getConceptLoan() != null && !registroDetailsModel.getConceptLoan().trim().isEmpty()) {
                    String conceptoLoan = registroDetailsModel.getConceptLoan().trim();
                    String montoLoan = registroDetailsModel.getMontoLoan() != null
                            ? registroDetailsModel.getMontoLoan().toString() : "0.00";
                    String amountPar = registroDetailsModel.getAmountPar() != null
                            ? registroDetailsModel.getAmountPar().toString() : "0.00";

                    mapAndPaymeny.put(conceptoLoan, amountPar + " - " + montoLoan);
                    date.put(conceptoLoan, registroDetailsModel.getFechaVLoan());
                }
                // Verificar si es un bono/abono (conceptBond no es null)
                else if (registroDetailsModel.getConceptBond() != null && !registroDetailsModel.getConceptBond().trim().isEmpty()) {
                    String conceptoBond = registroDetailsModel.getConceptBond().trim();
                    String montoBond = registroDetailsModel.getMontoBond() != null
                            ? registroDetailsModel.getMontoBond().toString() : "0.00";
                    String amountPar = registroDetailsModel.getAmountPar() != null
                            ? registroDetailsModel.getAmountPar().toString() : "0.00";

                    mapAndPaymeny.put(conceptoBond, amountPar + " - " + montoBond);
                    date.put(conceptoBond, registroDetailsModel.getFechaVBond());
                }
                // Si ambos son null, omitir este registro (registro sin detalle asociado)
            }

            ModelPayment modePayment = new ModelPayment();
            //getMapAndPaymeny
            modePayment.setCodeDocument(entry.getKey());
            modePayment.setFecha(entry.getValue().get(0).getFechaRegistro());
            modePayment.setAmountPaymentAndLoan(date);
            modePayment.setMapAndPaymeny(mapAndPaymeny);
            modePayment.setAmountDocument(String.format("%.2f", entry.getValue().get(0).getAmount()));

            moPayments.add(modePayment);
        }

        return moPayments;
    }

    public void HistoryPatment(String dni) {
        comp(dataPaymentAndLoan(dni));

    }

    private void document_history(List<ModelPayment> data) {
        try {

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("lastName_parameter", firstEmployee.getFullName());
            parametros.put("dni_parameter", firstEmployee.getNationalId());

            List<Map<String, ?>> detalles = new ArrayList<>();

            List<Map<String, ?>> detalles_var  = new ArrayList<>();
            
            for (ModelPayment payment : data) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("date_field", payment.getFecha()); // Debe ser String
                fila.put("document_field", payment.getCodeDocument());
                fila.put("amount_field", payment.getAmountDocument());

                // Subdetalle (otra lista de maps)
                List<Map<String, Object>> subDetalles = new ArrayList<>();

                Map<String, String> dateList = payment.getAmountPaymentAndLoan();

                List<Map.Entry<String, String>> amountList = new ArrayList<>(payment.getMapAndPaymeny().entrySet());

                for (int i = 0; i < amountList.size(); i++) {
                    Map<String, Object> subFila = new HashMap<>();
                    subFila.put("concept_payment_field", amountList.get(i).getKey());
                    subFila.put("vencimiento_field", dateList.get(amountList.get(i).getKey()));
                    subFila.put("amount_payment_field", amountList.get(i).getValue().toString().split("-")[0].trim().toString());
                    subFila.put("amount_pri_field", amountList.get(i).getValue().toString().split("-")[1].trim().toString());
                    subDetalles.add(subFila);
                }

                fila.put("detalles_sub", subDetalles); // importante: esta clave debe coincidir con el JRXML
                detalles.add(fila);
            }
            detalles_var.add(Map.of("detalles" , detalles));
            
            System.out.println("Salio");

            InputStream reporteStream = getClass().getResourceAsStream("/reports/report_payment.jrxml");

            // Compilar el reporte desde el archivo .jrxml
            JasperReport jasperReport = JasperCompileManager.compileReport(reporteStream);

            System.out.println("Model -> " + detalles.toString());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detalles_var);
            // Llenar el reporte con los datos
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            // Crear una instancia personalizada del visor
            JasperViewer jasperViewer = new JasperViewer(jasperPrint, false);

            // Obtener el JFrame interno del visor
            JFrame frame = (JFrame) jasperViewer.getContentPane().getParent().getParent().getParent();

            // Cambiar el título de la ventana
            frame.setTitle("REPORTE DE DEUDA");

            // Cambiar el ícono de la ventana
            ImageIcon icon = new ImageIcon(getClass().getResource("/IconGeneral/logoIcon.png")); // asegúrate de que este recurso exista

            frame.setIconImage(icon.getImage());

            // Mostrar el visor al frente
            jasperViewer.setVisible(true);
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(true);
            frame.setAlwaysOnTop(false);
        } catch (JRException ex) {
            Logger.getLogger(HistoryPayment.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void comp(List<ModelPayment> data) {

        try {
            document_history(data);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un Problema");
        }

    }
}
