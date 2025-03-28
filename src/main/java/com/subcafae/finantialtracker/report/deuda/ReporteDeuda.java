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

    public void reporteDeuda(
            String requestNumber,
            String nameEmployee,
            String dniEmployee,
            String[] conceptos,
            String[] ids,
            String requestNumberLoan,
            double debtLoan,
            int duesLoan,
            String fvencimientoLoan,
            String[][] prestamoData,
            String[][] fondoData
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
                document.add(new Paragraph("DNI : " + dniEmployee));
                document.add(new Paragraph("Apellidos y Nombres : " + nameEmployee));

                boolean hasAbono = conceptos != null && conceptos.length > 0 && !"Vacio".equals(conceptos[0]);
                boolean hasLoan = requestNumberLoan != null && !"Vacio".equals(requestNumberLoan);

                // Mostrar secciones de acuerdo a los datos disponibles
                if (hasAbono) {
                    mostrarSeccionAbonos(document, conceptos, ids, nameEmployee);
                }

                if (hasLoan) {
                    System.out.println("debtLoan " + debtLoan);
                    System.out.println("requestNumberLoan " + requestNumberLoan);
                    System.out.println("debtLoan " + debtLoan);
                    System.out.println("duesLoan " + duesLoan);
                    System.out.println("fvencimientoLoan " + fvencimientoLoan);
                    if (fvencimientoLoan == null) {
                        JOptionPane.showMessageDialog(null, "No se encontraron prestamos aprobados. Recuerda aceptar prestamos si tiene uno");
                        return;
                    } else {
                        mostrarSeccionPrestamos(document, requestNumberLoan, debtLoan, duesLoan, fvencimientoLoan, prestamoData, fondoData);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No se encontraron prestamos. Recuerda aceptar prestamos si es que tiene");
                }

                if (!hasAbono && !hasLoan) {
                    document.add(new Paragraph("No se encontraron deudas de abonos ni préstamos para este empleado. Recuerda aceptar el prestamo, si has generado uno.")
                            .setTextAlignment(TextAlignment.CENTER));
                }
            }

            abrirArchivo(fileName);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReporteDeuda.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
// Método para mostrar abonos

    private void mostrarSeccionAbonos(Document document, String[] conceptos, String[] ids, String nameEmployee) throws SQLException {
        document.add(new Paragraph("SECCIÓN DE ABONOS").setBold().setFontSize(12));

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        AbonoDetailsDao abonoDao = new AbonoDetailsDao();
        double totalAbonos = 0.0;

        for (int j = 0; j < conceptos.length; j++) {

            EmployeeTb emplo = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFirstName().concat(" " + predicate.getLastName()).equalsIgnoreCase(nameEmployee)).findFirst().get();

            List<AbonoTb> abonoLis = new AbonoDao().findAllAbonos().stream().filter(predicate -> Integer.parseInt(predicate.getEmployeeId()) == emplo.getEmployeeId() && predicate.getStatus().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());

            if (abonoLis.isEmpty()) {
                continue;
            }
            for (AbonoTb abonoLi : abonoLis) {

                // Título del concepto
               List<AbonoDetailsTb> abonoL =  abonoDao.getAllAbonoDetails().stream().filter(predicate -> predicate.getAbonoID() == abonoLi.getId() && predicate.getState().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());
                
                document.add(new Paragraph("CONCEPTO : " + conceptos[j])
                        .setBold()
                        .setFontColor(new DeviceRgb(255, 255, 255))
                        .setBackgroundColor(new DeviceRgb(0, 0, 0))
                        .setTextAlignment(TextAlignment.CENTER));

                // Detalles del abono
                document.add(new Paragraph("Num. Solicitud : " + abonoLi.getSoliNum()));

                document.add(new Paragraph("Num. Cuotas Pendientes : " + abonoL.size()));

                document.add(new Paragraph("Vencimiento : " + abonoLi.getPaymentDate()))
                        .setTextAlignment(TextAlignment.RIGHT);

                // Tabla de detalles del abono
                Table table = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));
                table.setWidth(UnitValue.createPercentValue(100));
                table.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
                table.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));
                table.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

                for (AbonoDetailsTb allAbonoDetail : abonoDao.getAllAbonoDetails()) {

                    if (allAbonoDetail.getAbonoID() == abonoLi.getId()) {

                        
                            table.addCell(new Cell().add(new Paragraph( abonoLi.getDues() + "/" + allAbonoDetail.getDues())));
                            table.addCell(new Cell().add(new Paragraph(allAbonoDetail.getPaymentDate())));

                            table.addCell(new Cell().add(new Paragraph(String.valueOf(allAbonoDetail.getMonthly()))));
                            
                            totalAbonos += allAbonoDetail.getMonthly();  // Acumulando la deuda
                       
                    }
                }
                
                document.add(table);

                // Mostrar el total de deuda
                document.add(new Paragraph("TOTAL DE DEUDA DE ABONOS: " + String.format("%.2f", totalAbonos))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBold());

                totalAbonos = 0.0;
                
                
            }

        }
    }

// Método para mostrar préstamos
    private void mostrarSeccionPrestamos(Document document, String requestNumberLoan, double debtLoan, int duesLoan, String fvencimientoLoan, String[][] prestamoData, String[][] fondoData) {

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));

        double totalPrestamo = 0.0;

        if (prestamoData != null && prestamoData.length > 0) {
            for (String[] row : prestamoData) {
                for (String cell : row) {
                    if (cell == null) {
                        cell = ""; // Manejar valores nulos
                    }
                    System.out.println("Data para cada celda -> " + cell);
                    table.addCell(new Cell().add(new Paragraph(cell)).setTextAlignment(TextAlignment.CENTER));
                }
                try {
                    if (debtLoan != 0) {
                        totalPrestamo += Double.parseDouble(row[2]);
                    } else {
                        JOptionPane.showMessageDialog(null, "No se encontraron datos de préstamos. Recuerda aceptar el préstamo pendiente para continuar.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Error al procesar el monto en los datos del préstamo.");
                }
            }
        } else {
            document.add(new Paragraph("No se encontraron datos de préstamos. Recuerda aceptar el préstamo pendiente para continuar.")
                    .setBold().setTextAlignment(TextAlignment.CENTER));
        }

        document.add(new Paragraph("SECCIÓN DE PRÉSTAMOS").setBold().setFontSize(12));

        // Detalles del préstamo
        document.add(new Paragraph("CONCEPTO : PRÉSTAMO " + requestNumberLoan)
                .setBold()
                .setFontColor(new DeviceRgb(255, 255, 255))
                .setBackgroundColor(new DeviceRgb(0, 0, 0))
                .setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Num. Solicitud : " + requestNumberLoan));
        document.add(new Paragraph("Num. Cuotas Pendientes : " + duesLoan));
        document.add(new Paragraph("Vencimiento : " + fvencimientoLoan).setTextAlignment(TextAlignment.RIGHT));

        // Tabla de detalles del préstamo
        table.setWidth(UnitValue.createPercentValue(100));
        table.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));

        table.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

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
        document.add(new Paragraph("Num. Solicitud : " + requestNumberLoan));
        document.add(new Paragraph("Num. Cuotas Pendientes : " + duesLoan));
        document.add(new Paragraph("Vencimiento : " + fvencimientoLoan).setTextAlignment(TextAlignment.RIGHT));

        // Tabla de detalles del fondo intangible
        Table table2 = new Table(UnitValue.createPercentArray(new float[]{4, 4, 4}));
        table2.setWidth(UnitValue.createPercentValue(100));
        table2.addHeaderCell(new Cell().add(new Paragraph("Detalle de Cuota").setBold().setTextAlignment(TextAlignment.CENTER)));
        table2.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setBold().setTextAlignment(TextAlignment.CENTER)));
        table2.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setTextAlignment(TextAlignment.CENTER)));

        double totalFondoIntangible = 0.0;

        if (fondoData != null && fondoData.length > 0) {
            for (String[] row : fondoData) {
                for (String cell : row) {
                    if (cell == null) {
                        cell = ""; // Manejar valores nulos
                    }
                    table2.addCell(new Cell().add(new Paragraph(cell)).setTextAlignment(TextAlignment.CENTER));
                }
                try {
                    totalFondoIntangible += Double.parseDouble(row[2]); // Validar que row[2] sea un número
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Error al procesar el monto en los datos del fondo intangible.");
                }
            }
        } else {
            document.add(new Paragraph("No se encontraron datos de fondo intangible.").setBold().setTextAlignment(TextAlignment.CENTER));
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

    public static void main(String[] args) {
        String fileName = System.getProperty("user.home") + "/Documents/voucher.pdf";

        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Configuración de fuentes
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Encabezado principal
            Paragraph hospital = new Paragraph()
                    .add(new Text("SUBCAFAE-HOSPITAL SAN JOSE\n")
                            .setFont(boldFont)
                            .setFontSize(14))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(hospital);

            // Subencabezado
            Paragraph comite = new Paragraph()
                    .add(new Text("SUB COMITE DE ADMINISTRACION DE FONDOS DE ABSTENCIAY ESTIMULOS-SAN JOSE\n\n")
                            .setFont(boldFont)
                            .setFontSize(11))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(comite);

            // Título del voucher
            Paragraph titulo = new Paragraph()
                    .add(new Text("VOUCHER DE PAGO\n\n")
                            .setFont(boldFont)
                            .setFontSize(16))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(titulo);

            // Cuerpo del documento
            Paragraph giradoa = new Paragraph()
                    .add(new Text("GIRADOA: ")
                            .setFont(boldFont)
                            .setFontSize(12))
                    .add(new Text("BURGOS GALLEGOS JOSE OCTAVIO\n\n")
                            .setFont(regularFont)
                            .setFontSize(12))
                    .setMarginLeft(50)
                    .setMarginBottom(15);
            document.add(giradoa);

            Paragraph contabilidad = new Paragraph()
                    .add(new Text("CONTABUIDAD PATRIMONIAL\n\n")
                            .setFont(regularFont)
                            .setFontSize(12))
                    .setMarginLeft(50)
                    .setMarginBottom(15);
            document.add(contabilidad);

            Paragraph tesorero = new Paragraph()
                    .add(new Text("TESORERO\n\n")
                            .setFont(regularFont)
                            .setFontSize(12))
                    .setMarginLeft(50)
                    .setMarginBottom(40);
            document.add(tesorero);

            // Firma alineada a la derecha
            Paragraph firma = new Paragraph()
                    .add(new Text("V'SPRESIDENTE SUB CAFAE")
                            .setFont(regularFont)
                            .setFontSize(12))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginRight(50);
            document.add(firma);

            document.close();

            // Abrir el PDF automáticamente
            if (Desktop.isDesktopSupported()) {
                File file = new File(fileName);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
