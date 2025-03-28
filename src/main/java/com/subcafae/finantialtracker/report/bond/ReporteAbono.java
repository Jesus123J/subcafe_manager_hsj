//package com.subcafae.finantialtracker.report.bond;
//
//import com.itextpdf.io.font.constants.StandardFonts;
//import com.itextpdf.kernel.colors.ColorConstants;
//import com.itextpdf.kernel.pdf.*;
//import com.itextpdf.layout.*;
//import com.itextpdf.layout.element.*;
//import com.itextpdf.layout.properties.*;
//import com.itextpdf.kernel.events.*;
//import com.itextpdf.kernel.events.PdfDocumentEvent;
//import com.itextpdf.kernel.events.Event;
//import com.itextpdf.kernel.geom.Rectangle;
//import com.itextpdf.kernel.font.PdfFontFactory;
//import com.itextpdf.kernel.geom.PageSize;
//import com.itextpdf.kernel.pdf.annot.da.StandardAnnotationFont;
//import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
//import com.itextpdf.layout.borders.Border;
//import com.itextpdf.layout.borders.SolidBorder;
//import com.view.TabbedPane;
//import java.awt.Desktop;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.List;
//import com.itextpdf.kernel.font.PdfFont;
//import java.time.LocalDate;
//import java.time.Month;
//import java.time.format.DateTimeFormatter;
//import java.time.format.TextStyle;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import javax.swing.plaf.FileChooserUI;
//
//public class ReporteAbono {
//
//    ServiceConcept listConcepto;
//
//    public void ReporteAbono(String conceptoName) {
//        createPdfDocument(statusAbonoEmpleado(conceptoName));
//
//    }
//
//    public List<AbonoDetails> filterAbonoDetails(String solinumber) {
//
//        AbonoDetailsDAO abonoDetailsDAO = new AbonoDetailsDAO();
//
//        return abonoDetailsDAO.listAll().stream()
//                .filter(abono -> solinumber.equalsIgnoreCase(abono.getSoliNumber()))
//                .collect(Collectors.toList());
//    }
//
//    public List<Abono> statusAbonoEmpleado(String conceptoName) {
//
//        ServiceConceptDAO serviceConceptDAO = new ServiceConceptDAO();
//        try {
//            listConcepto = serviceConceptDAO.list().stream().filter(predicate -> predicate.getDescription().equalsIgnoreCase(conceptoName)).findFirst().get();
//        } catch (Exception e) {
//            return null;
//        }
//
//        if (listConcepto == null) {
//            JOptionPane.showMessageDialog(null, "Inserte un concepto primero");
//            return null;
//        }
//
//        AbonoDAO abonoDAO = new AbonoDAO();
//
//        String[] contractTypeOptions = {"CAS", "Nombrado"};
//
//        String contractType = (String) JOptionPane.showInputDialog(
//                null,
//                "Selecciona el tipo de trabajador:",
//                "Tipo de Trabajador",
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                contractTypeOptions,
//                contractTypeOptions[0]
//        );
//        if (contractType == null) {
//            return null;
//        }
//        if (contractType.equals("CAS")) {
//
//            contractType = "2028";
//        } else {
//            contractType = "2154";
//        }
//
//        String component = contractType;
//
//        return (List<Abono>) abonoDAO.selectAll().stream().filter(predicate -> predicate.getEmployeeStatusCode() == Integer.parseInt(component)).collect(Collectors.toList());
//    }
//
//    public void createPdfDocument(List<Abono> abonoDetailse) {
//
//        if (abonoDetailse == null) {
//            return;
//        }
//        
//        
//        boolean statusMonth = false;
//
//        int statusMo = JOptionPane.showConfirmDialog(null, "Mostrar solo el mes actual ? ", "", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
//
//        if (statusMo == JOptionPane.YES_OPTION) {
//            statusMonth = true;
//        }
//        List<Abono> abonoFilter = abonoDetailse.stream().filter(predicate -> predicate.getServiceConceptId().equalsIgnoreCase(listConcepto.getId())).collect(Collectors.toList());
//
//        if (abonoFilter.isEmpty()) {
//            return;
//        }
//        
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Guardar Reporte de Abonos");
//
//        String defaultName = "reporte_abonos_" + LocalDate.now().format(DateTimeFormatter.ISO_DATE);
//        fileChooser.setSelectedFile(new File(defaultName + ".pdf"));
//
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PDF", "pdf");
//        fileChooser.setFileFilter(filter);
//
//        int userSelection = fileChooser.showSaveDialog(null);
//
//        if (userSelection == JFileChooser.APPROVE_OPTION) {
//            File fileToSave = fileChooser.getSelectedFile();
//
//            String destPath = fileToSave.getAbsolutePath();
//            if (!destPath.toLowerCase().endsWith(".pdf")) {
//                destPath += ".pdf";
//            } else             try (PdfWriter writer = new PdfWriter(destPath); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf, PageSize.A4.rotate())) {
//
//                pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new Header());
//
//                document.setMargins(100, 30, 30, 30); // Aumenta margen superior (100 px)
//
//                document.add(new Paragraph("REPORTE DE ABONOS")
//                        .setTextAlignment(TextAlignment.CENTER)
//                        .setBold()
//                        .setFontSize(16));
//                try {
//                    document.add(new Paragraph("CONCEPTO: " + listConcepto.getDescription())
//                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));
//
//                    document.add(new Paragraph("AÑO DE LIQUIDACIÓN: " + "" + LocalDate.now().getYear())
//                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));
//                    document.add(new Paragraph("MODALIDAD: " + (abonoFilter.get(0).getEmployeeStatusCode() == 2028 ? "CONTRATO ADMINISTRATIVO DE SERVICIO" : "NOMBRADO"))
//                            .setTextAlignment(TextAlignment.LEFT).setFont(PdfFontFactory.createFont(StandardFonts.COURIER)));
//                } catch (IOException ex) {
//                    Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                String[] headers = {
//                    "SOLICITUD", "DNI", "APELLIDO Y NOMBRE", "CUOTA", "MONTO",
//                    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
//                    "JUL", "AGO", "SET", "OCT", "NOV", "DIC",
//                    "TOTAL", "EST"
//                };
//
//                Map<Month, Integer> monthColumns = new HashMap<>();
//                monthColumns.put(Month.JANUARY, 5);
//                monthColumns.put(Month.FEBRUARY, 6);
//                monthColumns.put(Month.MARCH, 7);
//                monthColumns.put(Month.APRIL, 8);
//                monthColumns.put(Month.MAY, 9);
//                monthColumns.put(Month.JUNE, 10);
//                monthColumns.put(Month.JULY, 11);
//                monthColumns.put(Month.AUGUST, 12);
//                monthColumns.put(Month.SEPTEMBER, 13);
//                monthColumns.put(Month.OCTOBER, 14);
//                monthColumns.put(Month.NOVEMBER, 15);
//                monthColumns.put(Month.DECEMBER, 16);
//
//                Table table = new Table(headers.length).useAllAvailableWidth();
//                
//                for (String header : headers) {
//
//                    if (header.equalsIgnoreCase("EST")) {
//                        table.addHeaderCell(
//                                new Cell()
//                                        .add(new Paragraph(header)).setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(1f)).setBorderBottom(new SolidBorder(1f))
//                                        .setPadding(4).setFontSize(7f)
//                                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER) // Fuente explícita
//                        );
//                        break;
//                    }
//                    table.addHeaderCell(
//                            new Cell()
//                                    .add(new Paragraph(header)).setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(1f)).setBorderBottom(new SolidBorder(1f))
//                                    .setPadding(4).setFontSize(7f)
//                                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)).setTextAlignment(TextAlignment.CENTER) // Fuente explícita
//                    );
//
//                }
//
//                Map<Month, Integer> reverseMonthColumns = new HashMap<>();
//                monthColumns.forEach((mes, col) -> reverseMonthColumns.put(mes, col));
//
//                Map<Integer, Month> columnToMonth = new HashMap<>();
//                for (int col = 5; col <= 16; col++) {
//                    columnToMonth.put(col, Month.of(col - 4));
//                }
//
//                LocalDate currentMonth = LocalDate.now().plusMonths(1);
//                Double totalAmount = 0.0;
//
//                for (Abono abono : abonoFilter) {
//                    int count = 0;
//
//                    table.addCell(createStyledCell(abono.getSoliNum()));
//                    table.addCell(createStyledCell(abono.getEmployeeDni()));
//                    table.addCell(createStyledCell(abono.getEmployeeName()));
//                    table.addCell(createStyledCell(abono.getDues()));
//                    table.addCell(createStyledCell(String.format("%,.2f", abono.getMonthly())));
//
//                    //
//                    List<AbonoDetails> detalles = filterAbonoDetails(abono.getSoliNum());
//                    System.out.println("Tamño de client -> " + detalles);
//                    Map<Month, String> contenidoPorMes = new HashMap<>();
//
//                    double total = 0.0;
//                    boolean status = false;
//                    System.out.println("Cliente ->  " + abono.getEmployeeName());
//
//                    for (AbonoDetails detalle : detalles) {
//
//                        LocalDate fechaPago = convertDateToLocalDate(detalle.getPaymentDueDate());
//
//                        if (abono.getStatus() != null && abono.getStatus().equalsIgnoreCase("REN")) {
//                            status = true;
//                        }
//
//                        if (fechaPago != null) {
//                            
//                            Month mesPago = fechaPago.getMonth();
//                            //2025-03-31
//                            if (mesPago == currentMonth.getMonth()) {
//                                count++;
//                                contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                            } else {
//
//                                if (currentMonth.getMonthValue() > fechaPago.getMonthValue()
//                                        && currentMonth.getYear() >= fechaPago.getYear()) {
//                                    count++;
//                                    contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                                } else {
//                                    count++;
//                                    contenidoPorMes.put(mesPago,
//                                            fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//                                }
//                            }
//                        }
//                    }
//
//                    for (int col = 5; col <= 16; col++) {
//                        int cont = col;
//
//                        Month mesActual = monthColumns.entrySet().stream()
//                                .filter(entry -> entry.getValue() == cont)
//                                .map(Map.Entry::getKey)
//                                .findFirst()
//                                .orElse(null);
//
//                        if (mesActual != null && contenidoPorMes.containsKey(mesActual)) {
//
//                            System.out.println("Mes encontrados ->  " + contenidoPorMes.get(mesActual));
//
//                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//                            LocalDate fechaPago = LocalDate.parse(contenidoPorMes.get(mesActual), formatter);
//                            
//                    
//                            LocalDate fechaRenu = null;
//
//                            if (status) {
//
//                                fechaRenu = LocalDate.parse(abono.getModifiedByStatus(), DateTimeFormatter.ISO_LOCAL_DATE);
//
//                                if (statusMonth) {
//                                    if (fechaPago.getMonth() == currentMonth.getMonth()) {
//                                        table.addCell(createStyledCell("-"));
//                                    } else {
//                                        table.addCell(createStyledCell(""));
//                                    }
//                                } else {
//                                    if (fechaPago.getMonth() == fechaRenu.getMonth() || fechaPago.isBefore(fechaRenu)) {
//                                        if (fechaPago.getMonth() == fechaRenu.getMonth()) {
//                                            table.addCell(createStyledCell("-"));
//                                        } else {
//                                            table.addCell(createStyledCell(abono.getMonthly()));
//                                        }
//                                    } else {
//                                        table.addCell(createStyledCell("-"));
//                                    }
//                                }
//
//                            } else {
//                                if (statusMonth) {
//
//                                    if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {
//                                        total += abono.getMonthly();
//                                        count--;
//                                    }
//
//                                    if (fechaPago.getMonth() == currentMonth.getMonth()) {
//                                        table.addCell(createStyledCell(abono.getMonthly()).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
//                                    } else {
//                                        table.addCell(createStyledCell(""));
//                                    }
//
//                                } else {
//                                    if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {
//
//                                        if (fechaPago.getMonth() == currentMonth.getMonth()) {
//                                            table.addCell(createStyledCell(abono.getMonthly()).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
//                                        } else {
//                                            table.addCell(createStyledCell(abono.getMonthly()));
//                                        }
//
//                                        total += abono.getMonthly();
//                                        count--;
//
//                                    } else {
//
//                                        table.addCell(createStyledCell(""));
//                                    }
//                                }
//                            }
//
//                        } else {
//                            table.addCell(createStyledCell(""));
//                        }
//                    }
//
//                    String estado = total == abono.getMonthly() * abono.getDues() ? "CANC" : "PEND";
//
//                    if (status) {
//                        estado = abono.getStatus();
//                        total = 0.0;
//                    }
//
//                    table.addCell(createStyledCell(String.format("%,.2f", total)));
//                    table.addCell(createStyledCell(estado));
//
//                    totalAmount += total;
//                }
//
//                for (String header : headers) {
//                    Cell cell;
//                    if (header.equalsIgnoreCase("SOLICITUD")) {
//                        cell = new Cell().add(new Paragraph("Registros : ")
//                                .setTextAlignment(TextAlignment.RIGHT)
//                                .setFontSize(10f)
//                                .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
//                        );
//                    } else if (header.equalsIgnoreCase("DNI")) {
//                        cell = new Cell().add(new Paragraph(String.valueOf(abonoFilter.size()))
//                                .setTextAlignment(TextAlignment.LEFT)
//                                .setFontSize(10f)
//                                .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
//                        );
//                    } else if (header.equalsIgnoreCase("DIC")) {
//                        cell = new Cell().add(new Paragraph("TOTAL : ")
//                                .setTextAlignment(TextAlignment.RIGHT)
//                                .setFontSize(10f)
//                                .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
//                        );
//                    } else if (header.equalsIgnoreCase("TOTAL")) {
//                        cell = new Cell().add(new Paragraph(String.valueOf(totalAmount))
//                                .setTextAlignment(TextAlignment.LEFT)
//                                .setFontSize(10f)
//                                .setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
//                        );
//                    } else {
//                        cell = new Cell().add(new Paragraph(""));
//                    }
//
//                    cell.setBorder(Border.NO_BORDER);
//                    table.addCell(cell);
//                }
//
////                table.setKeepTogether(true);
////                table.setKeepWithNext(true);
////                
//                document.add(table.setBorderBottom(new SolidBorder(0.5f)));
//                document.add(new Paragraph());
//
//                try {
//                    document.close();
//                    Thread.sleep(1000);
//                    Desktop.getDesktop().open(new File(destPath));
//
//                } catch (Exception ex) {
//                    System.out.println("Error -> " + ex.getMessage());
//                }
//
//                JOptionPane.showMessageDialog(null, "PDF generado exitosamente!");
//            } catch (Exception e) {
//                JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//
//        } else {
//            JOptionPane.showMessageDialog(null, "Generación de PDF cancelada");
//        }
//
//    }
//
//    private Cell createStyledCell(Object content) {
//        String text = content != null ? content.toString() : "";
//        try {
//            return new Cell()
//                    .add(new Paragraph(text).setFontSize(7f))
//                    .setPadding(3f)
//                    .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)).setBorderLeft(new SolidBorder(1f)).setFont(PdfFontFactory.createFont(StandardFonts.COURIER))
//                    .setTextAlignment(TextAlignment.CENTER);
//        } catch (IOException ex) {
//            Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//
//    static class Header implements IEventHandler {
//
//        @Override
//        public void handleEvent(Event event) {
//            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
//            PdfDocument pdfDoc = docEvent.getDocument();
//            PdfPage page = docEvent.getPage();
//            int pageNumber = pdfDoc.getPageNumber(page);
//            Rectangle pageSize = page.getPageSize();
//            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
//
//            String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
//
//            try {
//
//                PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
//                pdfCanvas.beginText()
//                        .setFontAndSize(font, 10)
//                        .moveText(pageSize.getLeft() + 40, pageSize.getTop() - 30)
//                        .showText("Reporte de Abonos")
//                        .moveText(320, 0)
//                        .showText("Fecha: " + fechaActual)
//                        .moveText(370, 0)
//                        .showText("Página: " + pageNumber)
//                        .endText();
//
//                pdfCanvas.moveTo(pageSize.getLeft() + 30, pageSize.getTop() - 35)
//                        .lineTo(pageSize.getRight() - 30, pageSize.getTop() - 35)
//                        .setStrokeColor(ColorConstants.BLACK)
//                        .setLineWidth(1)
//                        .stroke();
//
//            } catch (IOException ex) {
//                Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            pdfCanvas.release();
//        }
//
//    }
//
//    private LocalDate convertDateToLocalDate(Date date) {
//        if (date == null) {
//            return null;
//        }
//        return new java.sql.Date(date.getTime()).toLocalDate();
//    }
//
//}
