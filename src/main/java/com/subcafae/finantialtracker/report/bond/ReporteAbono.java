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
import javax.swing.filechooser.FileNameExtensionFilter;

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
            String[] fechaOptions = {"Todos los abonos", "Filtrar por periodo de carga", "Filtrar por rango de fechas"};

            String opcionFecha = (String) JOptionPane.showInputDialog(
                    null,
                    "<html><b>Paso 2 de 3: Filtro por fecha (Opcional)</b><br><br>" +
                    "Concepto: <b>" + service.getDescription() + "</b><br>" +
                    "Tipo contrato: <b>" + contractType + "</b><br><br>" +
                    "<b>¿Cómo desea filtrar los abonos?</b><br><br>" +
                    "• <b>Todos los abonos:</b> Muestra todos los registrados del concepto<br>" +
                    "• <b>Por periodo:</b> Selecciona un mes/año con datos existentes<br>" +
                    "• <b>Por rango:</b> Selecciona fecha desde y hasta</html>",
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

            if (opcionFecha.equals("Filtrar por periodo de carga")) {
                // Obtener fechas disponibles de la BD
                List<String> fechasDisponibles = abonoDAO.getAvailableDates(service.getId(), contractCode);

                if (fechasDisponibles.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "No se encontraron abonos registrados para este concepto y tipo de contrato.",
                        "REPORTE DE ABONO",
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String[] nombresAbreviados = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

                String fechaSeleccionada = (String) JOptionPane.showInputDialog(
                    null,
                    "<html><b>Paso 2 de 3: Seleccionar Periodo de Carga</b><br><br>" +
                    "Concepto: <b>" + service.getDescription() + "</b><br>" +
                    "Tipo contrato: <b>" + contractType + "</b><br><br>" +
                    "Periodos con abonos cargados:</html>",
                    "REPORTE DE ABONO - Seleccionar Periodo",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    fechasDisponibles.toArray(new String[0]),
                    fechasDisponibles.get(0)
                );

                if (fechaSeleccionada == null) {
                    return;
                }

                // Parsear "Marzo 2025 (15 abonos)" -> LocalDate(2025, 3, 1)
                String[] partes = fechaSeleccionada.split(" ");
                int mesIdx = 0;
                for (int idx = 0; idx < nombresAbreviados.length; idx++) {
                    if (nombresAbreviados[idx].equals(partes[0])) {
                        mesIdx = idx + 1;
                        break;
                    }
                }
                int anioSel = Integer.parseInt(partes[1]);
                LocalDate fechaFiltro = LocalDate.of(anioSel, mesIdx, 1);

                listAbono = abonoDAO.getListAbonoTByConcepAndCodeEm(service.getId(), contractCode,
                    java.sql.Date.valueOf(fechaFiltro));

            } else if (opcionFecha.equals("Filtrar por rango de fechas")) {
                // JDialog con dos date pickers (desde - hasta)
                com.toedter.calendar.JDateChooser dateDesde = new com.toedter.calendar.JDateChooser();
                com.toedter.calendar.JDateChooser dateHasta = new com.toedter.calendar.JDateChooser();
                dateDesde.setDateFormatString("dd/MM/yyyy");
                dateHasta.setDateFormatString("dd/MM/yyyy");
                dateDesde.setDate(new Date());
                dateHasta.setDate(new Date());
                dateDesde.setPreferredSize(new java.awt.Dimension(170, 35));
                dateHasta.setPreferredSize(new java.awt.Dimension(170, 35));

                javax.swing.JPanel panelRango = new javax.swing.JPanel(new java.awt.GridBagLayout());
                java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
                gbc.insets = new java.awt.Insets(8, 8, 8, 8);
                gbc.anchor = java.awt.GridBagConstraints.WEST;

                gbc.gridx = 0; gbc.gridy = 0;
                panelRango.add(new javax.swing.JLabel("Desde:"), gbc);
                gbc.gridx = 1;
                panelRango.add(dateDesde, gbc);

                gbc.gridx = 0; gbc.gridy = 1;
                panelRango.add(new javax.swing.JLabel("Hasta:"), gbc);
                gbc.gridx = 1;
                panelRango.add(dateHasta, gbc);

                javax.swing.JDialog dialogRango = new javax.swing.JDialog((java.awt.Frame) null, "REPORTE DE ABONO - Rango de Fechas", true);
                dialogRango.setLayout(new java.awt.BorderLayout(10, 10));
                dialogRango.getRootPane().setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
                dialogRango.add(panelRango, java.awt.BorderLayout.CENTER);

                javax.swing.JPanel panelBotones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
                javax.swing.JButton btnOk = new javax.swing.JButton("Aceptar");
                javax.swing.JButton btnCancelar = new javax.swing.JButton("Cancelar");
                final boolean[] aceptado = {false};

                btnOk.addActionListener(ev -> { aceptado[0] = true; dialogRango.dispose(); });
                btnCancelar.addActionListener(ev -> dialogRango.dispose());
                panelBotones.add(btnOk);
                panelBotones.add(btnCancelar);
                dialogRango.add(panelBotones, java.awt.BorderLayout.SOUTH);

                dialogRango.pack();
                dialogRango.setLocationRelativeTo(null);
                dialogRango.setVisible(true);

                if (!aceptado[0]) {
                    return;
                }

                if (dateDesde.getDate() == null || dateHasta.getDate() == null) {
                    JOptionPane.showMessageDialog(null,
                        "Debe seleccionar ambas fechas (desde y hasta).",
                        "REPORTE DE ABONO",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                listAbono = abonoDAO.getListAbonoTByConcepAndCodeEmRange(service.getId(), contractCode,
                    new java.sql.Date(dateDesde.getDate().getTime()),
                    new java.sql.Date(dateHasta.getDate().getTime()));

            } else {
                // Todos los abonos sin filtro
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
