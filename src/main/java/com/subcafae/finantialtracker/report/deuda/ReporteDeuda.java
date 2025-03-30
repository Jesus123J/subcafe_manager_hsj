
package com.subcafae.finantialtracker.report.deuda;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

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

    public void reporteDeuda( //            String requestNumber,
            //            String nameEmployee,
            //            String dniEmployee,
            //            String[] conceptos,
            //            String[] ids,
            //            String requestNumberLoan,
            //            double debtLoan,
            //            int duesLoan,
            //            String fvencimientoLoan,
            //            String[][] prestamoData,
            //            String[][] fondoData
            String nameEmployee,
            String dni,
            List<ReporteDeuda> listAbono, List<ReporteDeuda> listPrestamo
    ) throws SQLException {

        try {

            String userHome = System.getProperty("user.home");
            String fileName = userHome + "/Documents/Reporte_Deuda_" + nameEmployee + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdfDoc = new PdfDocument(writer);

            try (Document document = new Document(pdfDoc)) {
                // Título
                document.add(new Paragraph("REPORTE DE DEUDA - SUBCAFAE HSJ")
                        .setBold()
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER));

                // Detalles del empleado
                document.add(new Paragraph("DNI : " + dni));
                document.add(new Paragraph("Apellidos y Nombres : " + nameEmployee));

                mostrarSeccionAbonos(document, listAbono);

                mostrarSeccionPrestamos(document, listPrestamo);

//                if (!hasAbono && !hasLoan) {
//                    document.add(new Paragraph("No se encontraron deudas de abonos ni préstamos para este empleado. Recuerda aceptar el prestamo, si has generado uno.")
//                            .setTextAlignment(TextAlignment.CENTER));
//                }
            }

            abrirArchivo(fileName);

        } catch (FileNotFoundException ex) {

            //  Logger.getLogger(ReporteDeuda.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
// Método para mostrar abonos

    private void mostrarSeccionAbonos(Document document, List<ReporteDeuda> listAbono) throws SQLException {

        if (listAbono.isEmpty()) {
            return;
        }

        document.add(new Paragraph("SECCIÓN DE ABONOS").setBold().setFontSize(12));

        double totalAbonos = 0.0;

        document.add(new Paragraph("CONCEPTO : " + listAbono.getFirst().getConceptBono())
                .setBold()
                .setFontColor(new DeviceRgb(255, 255, 255))
                .setBackgroundColor(new DeviceRgb(0, 0, 0))
                .setTextAlignment(TextAlignment.CENTER));

        // Detalles del abono
        document.add(new Paragraph("Num. Solicitud : " + listAbono.getFirst().getNumSoli()));

        document.add(new Paragraph("Num. Cuotas Pendientes : " + listAbono.size()));

        document.add(new Paragraph("Vencimiento : " + listAbono.getLast().getFechaVencimiento()))
                .setTextAlignment(TextAlignment.RIGHT);

        // Tabla de detalles del abono
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));

        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

        for (int i = 0; i < listAbono.size(); i++) {

            table.addCell(new Cell().add(new Paragraph(listAbono.get(i).getDetalleCouta() + "/" + listAbono.size())));
            table.addCell(new Cell().add(new Paragraph(listAbono.get(i).getFechaVencimiento())));
            table.addCell(new Cell().add(new Paragraph(listAbono.get(i).getMonto())));

            totalAbonos += Double.parseDouble(listAbono.get(i).getMonto());  // Acumulando la deuda

        }

        document.add(table);

        // Mostrar el total de deuda
        document.add(new Paragraph("TOTAL DE DEUDA DE ABONOS: " + String.format("%.2f", totalAbonos))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());

    }

// Método para mostrar préstamos
    private void mostrarSeccionPrestamos(Document document, List<ReporteDeuda> listPrestamo) {

        if (listPrestamo.isEmpty()) {
            return;
        }
        // String requestNumberLoan, double debtLoan, int duesLoan, String fvencimientoLoan, String[][] prestamoData, String[][] fondoData
        double totalPrestamo = 0.0;

        document.add(new Paragraph("SECCIÓN DE PRÉSTAMOS").setBold().setFontSize(12));

        // Detalles del préstamo
        document.add(new Paragraph("CONCEPTO : PRÉSTAMO " + listPrestamo.getFirst().getNumSoli())
                .setBold()
                .setFontColor(new DeviceRgb(255, 255, 255))
                .setBackgroundColor(new DeviceRgb(0, 0, 0))
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Num. Solicitud : " + listPrestamo.getFirst().getNumSoli()));
        document.add(new Paragraph("Num. Cuotas Pendientes : " + listPrestamo.size()));
        document.add(new Paragraph("Vencimiento : " + listPrestamo.getLast().getFechaVencimiento()).setTextAlignment(TextAlignment.RIGHT));

        // Tabla de detalles del préstamo
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));

        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));

        table.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

        for (int i = 0; i < listPrestamo.size(); i++) {
            table.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getDetalleCouta() + "/" + listPrestamo.size())).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getFechaVencimiento())).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getMonto())).setTextAlignment(TextAlignment.CENTER));
            totalPrestamo += Double.parseDouble(listPrestamo.get(i).getMonto());
        }
        document.add(table);

        // Mostrar el total de deuda del préstamo
        document.add(new Paragraph("TOTAL DE DEUDA DEL PRÉSTAMO: " + String.format("%.2f", totalPrestamo))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());

        // Detalles del fondo intangible
        document.add(new Paragraph("CONCEPTO : Fondo Intangible ")
                .setBold()
                .setFontColor(new DeviceRgb(255, 255, 255))
                .setBackgroundColor(new DeviceRgb(0, 0, 0))
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Num. Solicitud : " + listPrestamo.getFirst().getNumSoli()));
        document.add(new Paragraph("Num. Cuotas Pendientes : " + listPrestamo.size()));
        document.add(new Paragraph("Vencimiento : " + listPrestamo.getLast().getFechaVencimiento()).setTextAlignment(TextAlignment.RIGHT));

        // Tabla de detalles del fondo intangible
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));
        table2.setWidth(UnitValue.createPercentValue(100));
        table2.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
        table2.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));
        table2.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

        double totalFondoIntangible = 0.0;

        for (int i = 0; i < listPrestamo.size(); i++) {
            table2.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getDetalleCouta() + "/" + listPrestamo.size())).setTextAlignment(TextAlignment.CENTER));
            table2.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getFechaVencimiento())).setTextAlignment(TextAlignment.CENTER));
            table2.addCell(new Cell().add(new Paragraph(listPrestamo.get(i).getFondo())).setTextAlignment(TextAlignment.CENTER));
            totalFondoIntangible += Double.parseDouble(listPrestamo.get(i).getFondo());
        }

        document.add(table2);

        // Mostrar el total de deuda del fondo intangible
        document.add(new Paragraph("TOTAL DEL FONDO INTANGIBLE: " + String.format("%.2f", totalFondoIntangible))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());
    }

// Método para abrir archivo
    private void abrirArchivo(String filePath) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
