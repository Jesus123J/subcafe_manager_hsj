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
import com.subcafae.finantialtracker.view.component.PaneComparedTime;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
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
import javax.swing.JTextField;
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
                JOptionPane.showMessageDialog(null,
                    "No se encontró el concepto: " + nameConcepto,
                    "REPORTE DE ABONO",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Paso 1: Seleccionar tipo de trabajador
            String[] contractTypeOptions = {"CAS", "Nombrado"};

            String contractType = (String) JOptionPane.showInputDialog(
                    null,
                    "<html><b>Paso 1 de 3: Seleccione el tipo de contrato</b><br><br>" +
                    "Concepto seleccionado: <b>" + service.getDescription() + "</b><br><br>" +
                    "Elija la modalidad de contrato para filtrar los trabajadores:</html>",
                    "REPORTE DE ABONO - Tipo de Contrato",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    contractTypeOptions,
                    contractTypeOptions[0]
            );
            if (contractType == null) {
                return;
            }

            String contractCode = contractType.equals("CAS") ? "2028" : "2154";

            // Paso 2: Filtrar por fecha (OPCIONAL)
            String[] fechaOptions = {"Todos los abonos", "Filtrar por fecha de creación"};

            String opcionFecha = (String) JOptionPane.showInputDialog(
                    null,
                    "<html><b>Paso 2 de 3: Filtro por fecha (Opcional)</b><br><br>" +
                    "Concepto: <b>" + service.getDescription() + "</b><br>" +
                    "Tipo contrato: <b>" + contractType + "</b><br><br>" +
                    "<b>¿Desea filtrar los abonos por fecha de creación?</b><br><br>" +
                    "• <b>Todos los abonos:</b> Muestra todos los abonos registrados del concepto<br>" +
                    "• <b>Filtrar por fecha:</b> Solo muestra abonos creados desde una fecha específica</html>",
                    "REPORTE DE ABONO - Filtro de Fecha",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    fechaOptions,
                    fechaOptions[0]
            );

            if (opcionFecha == null) {
                return;
            }

            List<AbonoTb> listAbono;

            if (opcionFecha.equals("Filtrar por fecha de creación")) {
                // Crear panel principal con mejor diseño y tamaño adecuado
                javax.swing.JPanel panelPrincipal = new javax.swing.JPanel();
                panelPrincipal.setLayout(new java.awt.BorderLayout(10, 15));
                panelPrincipal.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
                panelPrincipal.setPreferredSize(new java.awt.Dimension(480, 320));

                // Panel de información superior con estilo (compatible con tema oscuro)
                javax.swing.JPanel panelInfo = new javax.swing.JPanel();
                panelInfo.setLayout(new java.awt.BorderLayout(5, 10));
                panelInfo.setBackground(new java.awt.Color(50, 55, 60));
                panelInfo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 122, 204), 1),
                    javax.swing.BorderFactory.createEmptyBorder(12, 15, 12, 15)
                ));

                javax.swing.JLabel lblTitulo = new javax.swing.JLabel(
                    "<html><div style='text-align: center;'>" +
                    "<span style='font-size: 13px; font-weight: bold; color: #E0E0E0;'>" +
                    "FILTRAR ABONOS POR FECHA DE CREACIÓN</span></div></html>"
                );
                lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                panelInfo.add(lblTitulo, java.awt.BorderLayout.NORTH);

                javax.swing.JLabel lblDescripcion = new javax.swing.JLabel(
                    "<html><div style='margin-top: 8px; text-align: center;'>" +
                    "<p style='font-size: 11px; color: #B0B0B0;'>Se mostrarán los abonos del concepto:</p>" +
                    "<p style='font-size: 12px; font-weight: bold; color: #64B5F6; margin: 8px 0;'>" +
                    "\"" + service.getDescription() + "\"</p>" +
                    "<p style='font-size: 11px; color: #B0B0B0;'>" +
                    "Registrados en el <b>mes y año</b> de la fecha que seleccione.</p></div></html>"
                );
                lblDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                panelInfo.add(lblDescripcion, java.awt.BorderLayout.CENTER);

                panelPrincipal.add(panelInfo, java.awt.BorderLayout.NORTH);

                // Panel central con el selector de fecha (compatible con tema oscuro)
                javax.swing.JPanel panelFecha = new javax.swing.JPanel();
                panelFecha.setLayout(new java.awt.GridBagLayout());
                panelFecha.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(80, 85, 90)),
                        " Seleccione la Fecha ",
                        javax.swing.border.TitledBorder.CENTER,
                        javax.swing.border.TitledBorder.TOP,
                        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                        new java.awt.Color(180, 180, 180)
                    ),
                    javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
                gbc.insets = new java.awt.Insets(8, 10, 8, 10);
                gbc.anchor = java.awt.GridBagConstraints.CENTER;

                // Etiqueta para el campo de fecha
                javax.swing.JLabel lblFecha = new javax.swing.JLabel("Fecha de creación:");
                lblFecha.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
                gbc.gridx = 0;
                gbc.gridy = 0;
                panelFecha.add(lblFecha, gbc);

                // Selector de fecha con mejor tamaño
                com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
                dateChooser.setDateFormatString("dd / MM / yyyy");
                dateChooser.setPreferredSize(new java.awt.Dimension(180, 38));
                dateChooser.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
                ((JTextField) dateChooser.getDateEditor().getUiComponent()).setEditable(false);
                ((JTextField) dateChooser.getDateEditor().getUiComponent()).setHorizontalAlignment(javax.swing.JTextField.CENTER);
                ((JTextField) dateChooser.getDateEditor().getUiComponent()).setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

                gbc.gridx = 1;
                gbc.gridy = 0;
                panelFecha.add(dateChooser, gbc);

                // Nota informativa (compatible con tema oscuro)
                javax.swing.JLabel lblNota = new javax.swing.JLabel(
                    "<html><div style='text-align: center; margin-top: 10px;'>" +
                    "<p style='color: #909090; font-size: 10px;'><i>" +
                    "Ejemplo: Si selecciona 15/03/2025, se mostrarán</i></p>" +
                    "<p style='color: #909090; font-size: 10px;'><i>" +
                    "todos los abonos creados en Marzo 2025</i></p></div></html>"
                );
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.insets = new java.awt.Insets(15, 10, 5, 10);
                panelFecha.add(lblNota, gbc);

                panelPrincipal.add(panelFecha, java.awt.BorderLayout.CENTER);

                // Mostrar diálogo con tamaño adecuado
                int result = JOptionPane.showConfirmDialog(
                    null,
                    panelPrincipal,
                    "REPORTE DE ABONO - Paso 2: Seleccionar Fecha",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) {
                    return;
                }

                if (dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(null,
                        "<html><div style='text-align: center;'>" +
                        "<p><b>Debe seleccionar una fecha para continuar.</b></p><br>" +
                        "<p>Si no desea filtrar por fecha, seleccione</p>" +
                        "<p><b>'Todos los abonos'</b> en el paso anterior.</p></div></html>",
                        "REPORTE DE ABONO",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                listAbono = abonoDAO.getListAbonoTByConcepAndCodeEm(service.getId(), contractCode,
                    new java.sql.Date(dateChooser.getDate().getTime()));
            } else {
                // Obtener todos los abonos sin filtro de fecha
                listAbono = abonoDAO.getListAbonoTByConcepAndCodeEm(service.getId(), contractCode, null);
            }

            if (listAbono == null || listAbono.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                    "<html>No se encontraron abonos con los siguientes criterios:<br><br>" +
                    "• Concepto: <b>" + service.getDescription() + "</b><br>" +
                    "• Tipo de contrato: <b>" + contractType + "</b><br><br>" +
                    "Verifique que existan abonos registrados con estos filtros.</html>",
                    "REPORTE DE ABONO",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Paso 3: Opciones de visualización
            String[] opcionesVisualizacion = {
                "Mostrar solo el mes actual",
                "Mostrar todo el historial del año"
            };

            String opcionVista = (String) JOptionPane.showInputDialog(
                    null,
                    "<html><b>Paso 3 de 3: Opciones de visualización</b><br><br>" +
                    "Concepto: <b>" + service.getDescription() + "</b><br>" +
                    "Tipo contrato: <b>" + contractType + "</b><br>" +
                    "Registros encontrados: <b>" + listAbono.size() + "</b><br><br>" +
                    "<b>¿Cómo desea visualizar los pagos en el reporte?</b><br><br>" +
                    "• <b>Solo mes actual:</b> Resalta únicamente los pagos del mes en curso<br>" +
                    "• <b>Todo el historial:</b> Muestra los pagos de todos los meses del año</html>",
                    "REPORTE DE ABONO - Visualización",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcionesVisualizacion,
                    opcionesVisualizacion[1]
            );

            if (opcionVista == null) {
                return;
            }

            boolean statusMonth = opcionVista.equals("Mostrar solo el mes actual");

            saveDocument(contractCode, listAbono, statusMonth, service.getDescription(), LocalDate.now());

        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Ocurrió un problema al generar el reporte:\n" + e.getMessage(),
                "REPORTE DE ABONO - Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveDocument(String contraty, List<AbonoTb> listAbono, boolean statusMonth, String concept, LocalDate start) throws IOException {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Abonos");

        String defaultName = "reporte_abonos_" + start.format(DateTimeFormatter.ISO_DATE);
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

                    LocalDate currentMonth = start;

                    Double totalAmount = 0.0;

                    for (AbonoTb abono : listAbono) {

                        Optional<EmployeeTb> employee = employeeDao.findById(Integer.valueOf(abono.getEmployeeId()));

                        table.addCell(createStyledCell(abono.getSoliNum()));
                        table.addCell(createStyledCell(employee.get().getNationalId()));
                        table.addCell(createStyledCell(employee.get().getFullName()));
                        table.addCell(createStyledCell(abono.getDues()));
                        table.addCell(createStyledCell(String.format("%,.2f", abono.getMonthly())));

                        //
                        List<AbonoDetailsTb> detalles = filterAbonoDetails(abono.getSoliNum());

                        Map<Month, String> contenidoPorMes = new HashMap<>();

                        double total = 0.0;
                        boolean status = false;

                        //List<Double> listMonto = new ArrayList<>();
                        List<Double> listAport = new ArrayList<>();
                        List<LocalDate> listDate = new ArrayList<>();

                        Map<LocalDate, Double> detalleApob = new HashMap<>();

                        for (AbonoDetailsTb detalle : detalles) {

                            if (detalle.getAbonoID() == abono.getId()) {

                                //listMonto.add(detalle.getMonthly());
                                System.out.println("Ingreso");

                                LocalDate fechaPago = LocalDate.parse(detalle.getPaymentDate());

                                if (fechaPago != null) {

                                    Month mesPago = fechaPago.getMonth();

                                    if (abono.getStatus().equalsIgnoreCase("REN")) {

                                        if (detalle.getState().equalsIgnoreCase("Pagado")) {
                                            listDate.add(fechaPago);
                                            listAport.add(abono.getMonthly());
                                            detalleApob.put(fechaPago, abono.getMonthly());

                                        } else if (detalle.getState().equalsIgnoreCase("Parcial")) {
                                            listDate.add(fechaPago);
                                            listAport.add(detalle.getPayment());
                                            detalleApob.put(fechaPago, detalle.getPayment());
                                        }
                                        if (detalle.getState().equalsIgnoreCase("Pendiente")) {
                                            listDate.add(fechaPago);
                                            listAport.add(0.0);
                                            detalleApob.put(fechaPago, 0.0);
                                        }
                                        
                                        contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                        
                                        status = true;
                                    }

                                    //2025-03-31
                                    if (!status) {

                                        if (mesPago == currentMonth.getMonth()) {

                                            if (detalle.getState().equalsIgnoreCase("Pagado")) {
                                                listDate.add(fechaPago);
                                                listAport.add(abono.getMonthly());
                                                detalleApob.put(fechaPago, abono.getMonthly());

                                            } else if (detalle.getState().equalsIgnoreCase("Parcial")) {
                                                listDate.add(fechaPago);
                                                listAport.add(detalle.getPayment());
                                                detalleApob.put(fechaPago, detalle.getPayment());
                                            }
                                            if (detalle.getState().equalsIgnoreCase("Pendiente")) {
                                                listDate.add(fechaPago);
                                                listAport.add(0.0);
                                                detalleApob.put(fechaPago, 0.0);
                                            }
                                            contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                                        } else {

                                            if (currentMonth.getMonthValue() > fechaPago.getMonthValue()
                                                    && currentMonth.getYear() >= fechaPago.getYear()) {

                                                if (detalle.getState().equalsIgnoreCase("Pagado")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(abono.getMonthly());
                                                    detalleApob.put(fechaPago, abono.getMonthly());

                                                } else if (detalle.getState().equalsIgnoreCase("Parcial")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(detalle.getPayment());
                                                    detalleApob.put(fechaPago, detalle.getPayment());
                                                }
                                                if (detalle.getState().equalsIgnoreCase("Pendiente")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(0.0);
                                                    detalleApob.put(fechaPago, 0.0);
                                                }

                                                contenidoPorMes.put(mesPago, fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                            } else {

                                                if (detalle.getState().equalsIgnoreCase("Pagado")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(abono.getMonthly());
                                                    detalleApob.put(fechaPago, abono.getMonthly());

                                                } else if (detalle.getState().equalsIgnoreCase("Parcial")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(detalle.getPayment());
                                                    detalleApob.put(fechaPago, detalle.getPayment());
                                                }
                                                if (detalle.getState().equalsIgnoreCase("Pendiente")) {
                                                    listDate.add(fechaPago);
                                                    listAport.add(0.0);
                                                    detalleApob.put(fechaPago, 0.0);
                                                }

                                                contenidoPorMes.put(mesPago,
                                                        fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //  System.out.println("List monto -> " + listMonto.toString());
                        System.out.println("Pago " + listAport.toString());
                        System.out.println("Mapa -> " + detalleApob.toString());

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
                                    System.out.println("Renunciaaaaaaaaaaaa");

                                    LocalDate fechaRenu = LocalDate.parse(abono.getModifiedAt(), DateTimeFormatter.ISO_LOCAL_DATE);
                                    System.out.println("Renunciaaaaaaaaaaaa");
                                    if (statusMonth) {
                                        System.out.println("Renunciaaaaaaaaaaaa");
                                        if (fechaPago.getMonth() == currentMonth.getMonth()) {

                                            //table.addCell(createStyledCell("-"));
                                             table.addCell(createStyledCell(detalleApob.get(fechaPago)));
                                        } else {
                                             table.addCell(createStyledCell(""));
                                        }
                                    } else {
                                        if (fechaPago.getMonth() == fechaRenu.getMonth() || fechaPago.isBefore(fechaRenu)) {
                                            if (fechaPago.getMonth() == fechaRenu.getMonth()) {
                                                
                                              //  table.addCell(createStyledCell("-"));
                                                    table.addCell(createStyledCell(detalleApob.get(fechaPago)));
                                            } else {
                                                //table.addCell(createStyledCell(listAport.get(count)));
                                                System.out.println("Renuncia");
                                                table.addCell(createStyledCell(detalleApob.get(fechaPago)));
                                            }

                                        } else {
                                            table.addCell(createStyledCell(""));
                                        }
                                    }

                                } else {
                                    System.out.println("Normal");
                                    if (statusMonth) {

                                        if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {
                                            //total += listAport.get(countPay);

                                        }

                                        if (fechaPago.getMonth() == currentMonth.getMonth()) {
                                            table.addCell(createStyledCell(detalleApob.get(fechaPago))).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
                                        } else {
                                            table.addCell(createStyledCell(""));
                                        }

                                    } else {
                                        if (fechaPago.getMonth() == currentMonth.getMonth() || fechaPago.isBefore(currentMonth)) {

                                            if (fechaPago.getMonth() == currentMonth.getMonth()) {
                                                table.addCell(createStyledCell(detalleApob.get(fechaPago)).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
                                            } else {
                                                table.addCell(createStyledCell(detalleApob.get(fechaPago)));
                                            }

                                        } else {
                                            table.addCell(createStyledCell(""));
                                        }
                                    }
                                }

                                total += detalleApob.get(fechaPago);

                            } else {
                                table.addCell(createStyledCell(""));
                            }
                        }

                        String estado = abono.getStatus().equalsIgnoreCase("Pagado") ? "CANC" : "PEND";

                        if (status) {

                            estado = abono.getStatus();

                            total = 0;
//                                    listAport.stream()
//                                    .mapToDouble(Double::doubleValue) // Convierte cada elemento a double primitivo
//                                    .sum(); // Suma todos los elementos

                        }

                        totalAmount += total;

                        table.addCell(createStyledCell(String.format("%,.2f", total)));
                        table.addCell(createStyledCell(estado));

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
                            cell = new Cell().add(new Paragraph(String.format("%.2f" , totalAmount))
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
