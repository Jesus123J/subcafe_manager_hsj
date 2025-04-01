package com.subcafae.finantialtracker.report.bond;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.events.*;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import com.itextpdf.kernel.font.PdfFont;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

public class ReporteAbono {

    ServiceConceptDao serviceConceptDAO = new ServiceConceptDao();
    AbonoDao abonoDAO = new AbonoDao();
    EmployeeDao employeeDao = new EmployeeDao();

    public void ReporteAbono(String conceptoName) {

        // createPdfDocument(statusAbonoEmpleado(conceptoName));
    }

    public List<AbonoDetailsTb> filterAbonoDetails(String solinumber) {

        try {
            System.out.println("SOli " + solinumber);
            List<AbonoDetailsTb> listAbonoDetails = abonoDAO.getListAbonoBySoli(solinumber);

            System.out.println("List -> " + listAbonoDetails.toString());

            return listAbonoDetails;

        } catch (SQLException ex) {
            System.out.println("Error -> " + ex.getMessage());
            // Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void searchService(String nameConcepto) {
        try {
            ServiceConceptTb service = serviceConceptDAO.findServiceConceptByCodigo(nameConcepto.toUpperCase());

            if (service == null) {
                return;
            }

            String[] contractTypeOptions = {"CAS", "Nombrado"};

            String contractType = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecciona el tipo de trabajador:",
                    "Tipo de Trabajador",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    contractTypeOptions,
                    contractTypeOptions[0]
            );
            if (contractType == null) {
                return;
            }

            if (contractType.equals("CAS")) {

                contractType = "2028";
            } else {
                contractType = "2154";
            }

            System.out.println("Service ");

            List<AbonoTb> listAbono = abonoDAO.getListAbonoTByConcepAndCodeEm(service.getId(), contractType);

            System.out.println("Abono ");

            if (listAbono.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontro ningun bono a este concepto a este tipo contrato " + contractType, "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int statusMo = JOptionPane.showConfirmDialog(null, "Mostrar solo el mes actual ? ", "", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            boolean statusMonth = false;

            if (statusMo == JOptionPane.YES_OPTION) {
                statusMonth = true;
            }

            saveDocument(contractType, listAbono, statusMonth, service.getDescription());

        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un Problema", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void saveDocument(String contraty, List<AbonoTb> listAbono, boolean statusMonth, String concept) throws IOException {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Abonos");

        String defaultName = "reporte_abonos_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        fileChooser.setSelectedFile(new File(defaultName + ".pdf"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            String destPath = fileToSave.getAbsolutePath();
            if (!destPath.toLowerCase().endsWith(".pdf")) {
                destPath += ".pdf";
            } else             try (PdfWriter writer = new PdfWriter(destPath); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf, PageSize.A4.rotate())) {

                pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new Header());

                document.setMargins(100, 30, 30, 30); // Aumenta margen superior (100 px)

                document.add(new Paragraph("REPORTE DE ABONOS")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16));
                try {
                    document.add(new Paragraph("CONCEPTO: " + concept)
                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));

                    document.add(new Paragraph("AÑO DE LIQUIDACIÓN: " + "" + LocalDate.now().getYear())
                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));
                    document.add(new Paragraph("MODALIDAD: " + (contraty.equalsIgnoreCase("2028") ? "CONTRATO ADMINISTRATIVO DE SERVICIO" : "NOMBRADO"))
                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));

                    String[] headers = {
                        "SOLICITUD", "DNI", "APELLIDO Y NOMBRE", "CUOTA", "MONTO",
                        "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
                        "JUL", "AGO", "SET", "OCT", "NOV", "DIC",
                        "TOTAL", "EST"
                    };

                    Map<Month, Integer> monthColumns = new HashMap<>();
                    monthColumns.put(Month.JANUARY, 5);
                    monthColumns.put(Month.FEBRUARY, 6);
                    monthColumns.put(Month.MARCH, 7);
                    monthColumns.put(Month.APRIL, 8);
                    monthColumns.put(Month.MAY, 9);
                    monthColumns.put(Month.JUNE, 10);
                    monthColumns.put(Month.JULY, 11);
                    monthColumns.put(Month.AUGUST, 12);
                    monthColumns.put(Month.SEPTEMBER, 13);
                    monthColumns.put(Month.OCTOBER, 14);
                    monthColumns.put(Month.NOVEMBER, 15);
                    monthColumns.put(Month.DECEMBER, 16);

                    Table table = new Table(headers.length).useAllAvailableWidth();

                    for (String header : headers) {

                        if (header.equalsIgnoreCase("EST")) {
                            table.addHeaderCell(
                                    new Cell()
                                            .add(new Paragraph(header)).setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(1f)).setBorderBottom(new SolidBorder(1f))
                                            .setPadding(4).setFontSize(7f)
                                            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER) // Fuente explícita
                            );
                            break;
                        }
                        table.addHeaderCell(
                                new Cell()
                                        .add(new Paragraph(header)).setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(1f)).setBorderBottom(new SolidBorder(1f))
                                        .setPadding(4).setFontSize(7f)
                                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER) // Fuente explícita
                        );

                    }
                    //

                    Map<Month, Integer> reverseMonthColumns = new HashMap<>();
                    monthColumns.forEach((mes, col) -> reverseMonthColumns.put(mes, col));

                    Map<Integer, Month> columnToMonth = new HashMap<>();
                    for (int col = 5; col <= 16; col++) {
                        columnToMonth.put(col, Month.of(col - 4));
                    }

                    LocalDate currentMonth = LocalDate.now().plusMonths(1);

                    Double totalAmount = 0.0;

                    for (AbonoTb abono : listAbono) {

                        Optional<EmployeeTb> employee = employeeDao.findById(Integer.valueOf(abono.getEmployeeId()));

                        table.addCell(createStyledCell(abono.getSoliNum()));
                        table.addCell(createStyledCell(employee.get().getNationalId()));
                        table.addCell(createStyledCell(employee.get().getFirstName().concat(" ".concat(employee.get().getLastName()))));
                        table.addCell(createStyledCell(abono.getDues()));
                        table.addCell(createStyledCell(String.format("%,.2f", abono.getMonthly())));

                        //
                        List<AbonoDetailsTb> detalles = filterAbonoDetails(abono.getSoliNum());

                        Map<Month, String> contenidoPorMes = new HashMap<>();

                        double total = 0.0;
                        boolean status = false;

                        List<Double> listMonto = new ArrayList<>();
                        List<Double> listAport = new ArrayList<>();

                        for (AbonoDetailsTb detalle : detalles) {

                            if (detalle.getAbonoID() == abono.getId()) {

                                listMonto.add(detalle.getMonthly() - detalle.getPayment());

                                System.out.println("Ingreso");

                                LocalDate fechaPago = LocalDate.parse(detalle.getPaymentDate());

                                if (fechaPago != null) {

                                    Month mesPago = fechaPago.getMonth();

                                    if (abono.getStatus().equalsIgnoreCase("REN")) {
                                        System.out.println("Renuncia");
                                        if (detalle.getState().equalsIgnoreCase("Pagado")) {
                                            listAport.add(abono.getMonthly());
                                        } else if (detalle.getState().equalsIgnoreCase("Parcial")) {
                                            listAport.add(detalle.getPayment());
                                        }
                                        status = true;
                                    }

                                    //2025-03-31
                                    if (mesPago == currentMonth.getMonth()) {

                                        contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                    } else {

                                        if (currentMonth.getMonthValue() > fechaPago.getMonthValue()
                                                && currentMonth.getYear() >= fechaPago.getYear()) {

                                            contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                        } else {

                                            contenidoPorMes.put(mesPago,
                                                    fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                        }
                                    }
                                }
                            }
                        }
                        System.out.println("List monto -> " + listMonto.toString());
                        int count = 0;

                        for (int col = 5; col <= 16; col++) {
                            int cont = col;

                            Month mesActual = monthColumns.entrySet().stream()
                                    .filter(entry -> entry.getValue() == cont)
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(null);

                            if (mesActual != null && contenidoPorMes.containsKey(mesActual)) {

                                System.out.println("Mes encontrados ->  " + contenidoPorMes.get(mesActual));

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                LocalDate fechaPago = LocalDate.parse(contenidoPorMes.get(mesActual), formatter);

                                if (status) {

                                    LocalDate fechaRenu = LocalDate.parse(abono.getModifiedAt(), DateTimeFormatter.ISO_LOCAL_DATE);

                                    if (statusMonth) {
                                        if (fechaPago.getMonth() == currentMonth.getMonth()) {
                                            table.addCell(createStyledCell("-"));
                                        } else {
                                            table.addCell(createStyledCell(""));
                                        }
                                    } else {
                                        if (fechaPago.getMonth() == fechaRenu.getMonth() || fechaPago.isBefore(fechaRenu)) {
                                            if (fechaPago.getMonth() == fechaRenu.getMonth()) {
                                                table.addCell(createStyledCell("-"));
                                            } else {
                                                table.addCell(createStyledCell(listMonto.get(count)));
                                            }
                                        } else {
                                            table.addCell(createStyledCell("-"));
                                        }
                                    }

                                } else {
                                    if (statusMonth) {

                                        if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {
                                            total += listMonto.get(count);

                                        }

                                        if (fechaPago.getMonth() == currentMonth.getMonth()) {
                                            table.addCell(createStyledCell(listMonto.get(count)).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
                                        } else {
                                            table.addCell(createStyledCell(""));
                                        }

                                    } else {
                                        if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {

                                            if (fechaPago.getMonth() == currentMonth.getMonth()) {
                                                table.addCell(createStyledCell(listMonto.get(count)).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
                                            } else {
                                                table.addCell(createStyledCell(listMonto.get(count)));
                                            }

                                            total += listMonto.get(count);

                                        } else {

                                            table.addCell(createStyledCell(""));
                                        }
                                    }
                                }
                                count++;

                            } else {
                                table.addCell(createStyledCell(""));
                            }
                        }

                        String estado = abono.getStatus().equalsIgnoreCase("Pagado") ? "CANC" : "PEND";

                        if (status) {
                            estado = abono.getStatus();

                            total = listAport.stream()
                                    .mapToDouble(Double::doubleValue) // Convierte cada elemento a double primitivo
                                    .sum(); // Suma todos los elementos

                        }

                        table.addCell(createStyledCell(String.format("%,.2f", total)));
                        table.addCell(createStyledCell(estado));

                        totalAmount += total;
                    }

                    for (String header : headers) {
                        Cell cell;
                        if (header.equalsIgnoreCase("SOLICITUD")) {
                            cell = new Cell().add(new Paragraph("Registros : ")
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setFontSize(10f)
                                    .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                            );
                        } else if (header.equalsIgnoreCase("DNI")) {
                            cell = new Cell().add(new Paragraph(String.valueOf(listAbono.size()))
                                    .setTextAlignment(TextAlignment.LEFT)
                                    .setFontSize(10f)
                                    .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                            );
                        } else if (header.equalsIgnoreCase("DIC")) {
                            cell = new Cell().add(new Paragraph("TOTAL : ")
                                    .setTextAlignment(TextAlignment.RIGHT)
                                    .setFontSize(10f)
                                    .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                            );
                        } else if (header.equalsIgnoreCase("TOTAL")) {
                            cell = new Cell().add(new Paragraph(String.valueOf(totalAmount))
                                    .setTextAlignment(TextAlignment.LEFT)
                                    .setFontSize(10f)
                                    .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                            );
                        } else {
                            cell = new Cell().add(new Paragraph(""));
                        }

                        cell.setBorder(Border.NO_BORDER);
                        table.addCell(cell);
                    }

//                table.setKeepTogether(true);
//                table.setKeepWithNext(true);
//                
                    document.add(table.setBorderBottom(new SolidBorder(0.5f)));
                    document.add(new Paragraph());

                    try {
                        document.close();
                        Thread.sleep(1000);
                        Desktop.getDesktop().open(new File(destPath));

                    } catch (Exception ex) {
                        System.out.println("Error -> " + ex.getMessage());
                    }

                    JOptionPane.showMessageDialog(null, "PDF generado exitosamente!");
                } catch (Exception e) {
                    System.out.println("Error " + e.getCause());
                    System.out.println("Error -> " + e.getMessage());

                    JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Generación de PDF cancelada");
        }

    }

    private Cell createStyledCell(Object content) {
        String text = content != null ? content.toString() : "";
        try {
            return new Cell()
                    .add(new Paragraph(text).setFontSize(7f))
                    .setPadding(3f)
                    .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)).setBorderLeft(new SolidBorder(1f)).setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
                    .setTextAlignment(TextAlignment.CENTER);
        } catch (IOException ex) {
            Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    static class Header implements IEventHandler {

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            try {

                PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
                pdfCanvas.beginText()
                        .setFontAndSize(font, 10)
                        .moveText(pageSize.getLeft() + 40, pageSize.getTop() - 30)
                        .showText("Reporte de Abonos")
                        .moveText(320, 0)
                        .showText("Fecha: " + fechaActual)
                        .moveText(370, 0)
                        .showText("Página: " + pageNumber)
                        .endText();

                pdfCanvas.moveTo(pageSize.getLeft() + 30, pageSize.getTop() - 35)
                        .lineTo(pageSize.getRight() - 30, pageSize.getTop() - 35)
                        .setStrokeColor(ColorConstants.BLACK)
                        .setLineWidth(1)
                        .stroke();

            } catch (IOException ex) {
                Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
            }

            pdfCanvas.release();
        }

    }

    private LocalDate convertDateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
}
