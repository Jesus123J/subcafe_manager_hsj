package com.subcafae.finantialtracker.report.concept;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;

import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.borders.Border;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ReporteConcepto {

    public void reporteConcepto(
            String numberVoucher,
            String bank,
            String numAccount,
            String numCheck,
            String nameLastName,
            double amount,
            String details,
            LocalDate hoy
    ) {

        hoy.getDayOfMonth();

        PdfWriter writer = null;
        try {
            System.out.println("Imagen");

            String userHome = System.getProperty("user.home");
            String destino = userHome
                    + "/Documents/voucher_de_pago_" + numberVoucher + ".pdf";
            writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderEventHandler());

            // ** Crear tabla para el encabezado con 2 columnas **
            float[] columnWidths = {400f, 150f}; // Definir ancho de columnas
            Table table = new Table(columnWidths);

            // ** Columna 1: Encabezado de texto **
            Cell cell1 = new Cell()
                    .add(new Paragraph("SUBCAFAE - HOSPITAL SAN JOSE")
                            .setFontSize(14)
                            .setBold())
                    .add(new Paragraph("SUB COMITE DE ADMINISTRACION DE FONDOS DE ASISTENCIA Y ESTIMULOS - SAN JOSE")
                            .setFontSize(8)
                            .setBold())
                    .add(new Paragraph("Las Magnolias 476 - Carmen de la Legua - Reynoso, RUC: 20505869525")
                            .setFontSize(8)
                            .setBold())
                    .add(new Paragraph("TEL: 636-7789")
                            .setFontSize(8)
                            .setBold());

            cell1.setBorder(Border.NO_BORDER); // Quitar bordes
            table.addCell(cell1);
           
            // Agregar la tabla al documento
            document.add(table);

            // Agregar una línea separadora
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10));

            // Tabla de información general (fila única)
            // Fuentes
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Tabla contenedora principal (3 columnas)
            Table mainNumberVoucher = new Table(UnitValue.createPercentArray(new float[]{60, 81, 40}))
                    .setBorder(Border.NO_BORDER)
                    .setWidth(UnitValue.createPercentValue(100)); // Ocupar todo el ancho disponible

// Primera y segunda celda vacía
            mainNumberVoucher.addCell(new Cell().setBorder(Border.NO_BORDER));
            mainNumberVoucher.addCell(new Cell().setBorder(Border.NO_BORDER));

// Celda alineada a la derecha
            Cell cell3 = new Cell()
                    .add(new Paragraph(numberVoucher)
                            .setFontSize(10)
                            .setBold()
                            .setFontColor(ColorConstants.RED))
                    .setTextAlignment(TextAlignment.RIGHT) // Alinear texto a la derecha
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT); // Alinear celda a la derecha

            mainNumberVoucher.addCell(cell3);

            document.add(mainNumberVoucher);

            Table mainHeaderTable = new Table(UnitValue.createPercentArray(new float[]{5, 50, 30})).setMarginBottom(10);

            // Columna izquierda: vacía
            mainHeaderTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            // Columna central: Título centrado
            Paragraph titulo = new Paragraph("VOUCHER DE PAGO")
                    .setFont(boldFont)
                    .setFontSize(30)
                    .setTextAlignment(TextAlignment.CENTER);

            mainHeaderTable.addCell(new Cell()
                    .add(titulo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            // Columna derecha: Tabla de fecha
            DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("MM");

            // Configurar estilo de celdas
            Cell headerCell = new Cell()
                    .setBackgroundColor(WebColors.getRGBColor("#D3D3D3")) // Gris claro
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1);

            Cell valueCell = new Cell()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(0);

            // Crear tabla de fecha con ancho específico
            Table fechaTable = new Table(new float[]{50, 120, 50}) // Ancho de columnas en puntos
                    .setWidth(UnitValue.createPointValue(150)).setHeight(40) // Ancho total de la tabla
                    .setMarginRight(0)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);

            // Encabezados con fondo gris
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("DIA").setFont(boldFont)));
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("MES").setFont(boldFont)));
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("AÑO").setFont(boldFont)));

            // Valores centrados
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(String.valueOf(hoy.getDayOfMonth()))));
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(formatoMes.format(hoy))));
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(String.valueOf(hoy.getYear()))));

            // Contenedor para la tabla alineada a la derecha
            mainHeaderTable.addCell(new Cell()
                    .add(fechaTable)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingRight(0)
                    .setMarginRight(0));

            document.add(mainHeaderTable);

            document.add(new Paragraph("\nGIRADO A:  " + nameLastName.toUpperCase()).setFontSize(9).setMargin(0));
            document.add(new Paragraph("  BANCO: " + bank + "      CUENTA N°: " + numAccount + "    N°CHEQUE: " + numCheck + "  MONTO: " + amount
            ).setFontSize(9).setMargin(0)
            );
            document.add(new Paragraph("IMPORTE:   " + convertirMoneda(Math.round(amount * 100.0) / 100.0)).setFontSize(9).setMargin(0));

            document.add(new Paragraph("\n"));

            Table concepto = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            concepto.addCell(new Paragraph("CONCEPTO")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));

            concepto.addCell(new Paragraph(details)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.JUSTIFIED) // Ajusta el texto
                    .setBorder(Border.NO_BORDER));

            document.add(concepto);
            document.add(new Paragraph("\n"));

            Table contabilidad = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            contabilidad.addCell(new Paragraph("CONTABILIDAD PATRIMONIAL")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));
            document.add(contabilidad);

            Table firstRow = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1})).useAllAvailableWidth();

            firstRow.addCell(createCell("DEBE", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("HABER", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("GLOSA", TextAlignment.CENTER, 15, 2, 3, new DeviceRgb(211, 211, 211)));

            firstRow.addCell(createCell("CUENTA", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("C.C", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("IMPORTE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));

            firstRow.addCell(createCell("CUENTA", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("C.C", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("IMPORTE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));

            for (int i = 1; i < 36; i++) {
                if (i % 7 == 0 && i != 0) {
                    firstRow.addCell(createCell("", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(255, 255, 255)));
                } else {
                    firstRow.addCell(createCell("", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
                }
            }

            document.add(firstRow);
            document.add(new Paragraph("\n"));
            Table firma = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
            for (int i = 0; i < 3; i++) {
                firma.addCell(createCell("", TextAlignment.CENTER, 100, 1, 1, new DeviceRgb(255, 255, 255)));
            }

            firma.addCell(createCell("TESORERO", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
            firma.addCell(createCell("V°B° PRESIDENTE SUB CAFAE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
            firma.addCell(createCell("CONTADOR", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));

            document.add(firma);

            document.add(new Paragraph("\n"));

            Table cheque = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            cheque.addCell(new Paragraph("RECIBÍ CHEQUE EFECTIVO CONFORME")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));

            cheque.addCell(new Cell().add(new Paragraph("NOMBRE: ______________________________________"
                    + "\nFECHA:     ______________________________________"
                    + "\nD.N.I. N°    ______________________________________                                                   ____________________"
            ))
                    .add(new Paragraph("FIRMA                          .").setTextAlignment(TextAlignment.RIGHT))
                    .setMargin(10)
                    .setFontSize(10)
                    .setHeight(80));

            document.add(cheque);

            document.close();
            try {
                Desktop.getDesktop().open(new File(destino));
            } catch (IOException ex) {
                //  Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void reporteConcepto(
            String nombre,
            double monto,
            int cuotas,
            double saldo,
            String codigo,
            int modalidad, LocalDate hoy
    ) {
        hoy = LocalDate.now();
        PdfWriter writer = null;
        try {
            String userHome = System.getProperty("user.home");
            String destino = userHome
                    + "/Documents/concepto_subcomite_" + codigo + ".pdf";
            writer = new PdfWriter(destino);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new HeaderEventHandler());

            // ** Crear tabla para el encabezado con 2 columnas **
            float[] columnWidths = {400f, 150f}; // Definir ancho de columnas
            Table table = new Table(columnWidths);

            // ** Columna 1: Encabezado de texto **
            Cell cell1 = new Cell()
                    .add(new Paragraph("SUBCAFAE - HOSPITAL SAN JOSE")
                            .setFontSize(14)
                            .setBold())
                    .add(new Paragraph("SUB COMITE DE ADMINISTRACION DE FONDOS DE ASISTENCIA Y ESTIMULOS - SAN JOSE")
                            .setFontSize(8)
                            .setBold())
                    .add(new Paragraph("Las Magnolias 476 - Carmen de la Legua - Reynoso, RUC: 20505869525")
                            .setFontSize(8)
                            .setBold())
                    .add(new Paragraph("TEL: 636-7789")
                            .setFontSize(8)
                            .setBold());

            cell1.setBorder(Border.NO_BORDER); // Quitar bordes
            table.addCell(cell1);

            // ** Columna 2: Texto adicional (Ejemplo: "FACTURA N° 001-000123") **
            Cell cell2 = new Cell()
                    .add(new Paragraph("IMAGEN")
                            .setFontSize(10)
                            .setBold()
                            .setFontColor(ColorConstants.RED))
                    .setTextAlignment(TextAlignment.RIGHT);

            cell2.setBorder(Border.NO_BORDER); // Quitar bordes

            table.addCell(cell2);

            // Agregar la tabla al documento
            document.add(table);

            // Agregar una línea separadora
            document.add(new LineSeparator(new SolidLine(1f))
                    .setMarginTop(10));

            // Tabla de información general (fila única)
            // Fuentes
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Tabla contenedora principal (3 columnas)
            Table mainNumberVoucher = new Table(UnitValue.createPercentArray(new float[]{60, 81, 40}))
                    .setBorder(Border.NO_BORDER)
                    .setWidth(UnitValue.createPercentValue(100)); // Ocupar todo el ancho disponible

// Primera y segunda celda vacía
            mainNumberVoucher.addCell(new Cell().setBorder(Border.NO_BORDER));
            mainNumberVoucher.addCell(new Cell().setBorder(Border.NO_BORDER));

// Celda alineada a la derecha
            Cell cell3 = new Cell()
                    .add(new Paragraph("N° 001-000123")
                            .setFontSize(10)
                            .setBold()
                            .setFontColor(ColorConstants.RED))
                    .setTextAlignment(TextAlignment.RIGHT) // Alinear texto a la derecha
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT); // Alinear celda a la derecha

            mainNumberVoucher.addCell(cell3);

            document.add(mainNumberVoucher);

            Table mainHeaderTable = new Table(UnitValue.createPercentArray(new float[]{5, 50, 30})).setMarginBottom(10);

            // Columna izquierda: vacía
            mainHeaderTable.addCell(new Cell().setBorder(Border.NO_BORDER));

            // Columna central: Título centrado
            Paragraph titulo = new Paragraph("VOUCHER DE PAGO")
                    .setFont(boldFont)
                    .setFontSize(30)
                    .setTextAlignment(TextAlignment.CENTER);

            mainHeaderTable.addCell(new Cell()
                    .add(titulo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));

            // Columna derecha: Tabla de fecha
            DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("MM");

            // Configurar estilo de celdas
            Cell headerCell = new Cell()
                    .setBackgroundColor(WebColors.getRGBColor("#D3D3D3")) // Gris claro
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(1);

            Cell valueCell = new Cell()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(0);

            // Crear tabla de fecha con ancho específico
            Table fechaTable = new Table(new float[]{50, 120, 50}) // Ancho de columnas en puntos
                    .setWidth(UnitValue.createPointValue(150)).setHeight(40) // Ancho total de la tabla
                    .setMarginRight(0)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);

            // Encabezados con fondo gris
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("DIA").setFont(boldFont)));
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("MES").setFont(boldFont)));
            fechaTable.addHeaderCell(headerCell.clone(true)
                    .add(new Paragraph("AÑO").setFont(boldFont)));

            // Valores centrados
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(String.valueOf(hoy.getDayOfMonth()))));
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(formatoMes.format(hoy))));
            fechaTable.addCell(valueCell.clone(true)
                    .add(new Paragraph(String.valueOf(hoy.getYear()))));

            // Contenedor para la tabla alineada a la derecha
            mainHeaderTable.addCell(new Cell()
                    .add(fechaTable)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPaddingRight(0)
                    .setMarginRight(0));

            document.add(mainHeaderTable);

            document.add(new Paragraph("\nGIRADO A:  " + nombre.toUpperCase()).setFontSize(9).setMargin(0));
            document.add(new Paragraph("BANCO:       B.NACIÓN        CUENTA N°: ___________        N°CHEQUE: _____________        MONTO: " + monto).setFontSize(9).setMargin(0));
            document.add(new Paragraph("IMPORTE:   " + convertirMoneda(Math.round(monto * 100.0) / 100.0)).setFontSize(9).setMargin(0));

            document.add(new Paragraph("\n"));

            Table concepto = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            concepto.addCell(new Paragraph("CONCEPTO")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));

            cuotas = 10;
            concepto.addCell(new Paragraph("PRESTAMO: " + modalidad + "\n"
                    + monto + " + " + (monto * 0.18) + " (intereses)"
                    + "\nSALDO: S/. " + saldo
                    + "\nA GIRAR: S/. " + monto
                    + "\n" + cuotas + " CUOTAS DE S/. " + (monto + (monto * 0.18)) / cuotas).setFontSize(9));

            document.add(concepto);
            document.add(new Paragraph("\n"));

            Table contabilidad = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            contabilidad.addCell(new Paragraph("CONTABILIDAD PATRIMONIAL")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));
            document.add(contabilidad);

            Table firstRow = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1})).useAllAvailableWidth();

            firstRow.addCell(createCell("DEBE", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("HABER", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("GLOSA", TextAlignment.CENTER, 15, 2, 3, new DeviceRgb(211, 211, 211)));

            firstRow.addCell(createCell("CUENTA", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("C.C", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("IMPORTE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));

            firstRow.addCell(createCell("CUENTA", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("C.C", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));
            firstRow.addCell(createCell("IMPORTE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(211, 211, 211)));

            for (int i = 1; i < 36; i++) {
                if (i % 7 == 0 && i != 0) {
                    firstRow.addCell(createCell("", TextAlignment.CENTER, 15, 1, 3, new DeviceRgb(255, 255, 255)));
                } else {
                    firstRow.addCell(createCell("", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
                }
            }

            document.add(firstRow);
            document.add(new Paragraph("\n"));
            Table firma = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
            for (int i = 0; i < 3; i++) {
                firma.addCell(createCell("", TextAlignment.CENTER, 100, 1, 1, new DeviceRgb(255, 255, 255)));
            }

            firma.addCell(createCell("TESORERO", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
            firma.addCell(createCell("V°B° PRESIDENTE SUB CAFAE", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));
            firma.addCell(createCell("CONTADOR", TextAlignment.CENTER, 15, 1, 1, new DeviceRgb(255, 255, 255)));

            document.add(firma);

            document.add(new Paragraph("\n"));

            Table cheque = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            cheque.addCell(new Paragraph("RECIBÍ CHEQUE EFECTIVO CONFORME")
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBorder(Border.NO_BORDER));

            cheque.addCell(new Cell().add(new Paragraph("NOMBRE: ______________________________________"
                    + "\nFECHA:     ______________________________________"
                    + "\nD.N.I. N°    ______________________________________                                                   ____________________"
            ))
                    .add(new Paragraph("FIRMA                          .").setTextAlignment(TextAlignment.RIGHT))
                    .setMargin(10)
                    .setFontSize(10)
                    .setHeight(80));

            document.add(cheque);

            document.close();
            try {
                Desktop.getDesktop().open(new File(destino));
            } catch (IOException ex) {
                //  Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ReporteConcepto.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static class HeaderEventHandler implements IEventHandler {

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            Rectangle pageSize = page.getPageSize();
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            float yPosition = pageSize.getTop() - 20, yImage = 0;

            try {
                InputStream inputStream = getClass().getResourceAsStream("/logo_blanco.png");
                if (inputStream != null) {
                    ImageData imagenData = ImageDataFactory.create(inputStream.readAllBytes());

                    float desiredHeight = 50;
                    float width = imagenData.getWidth() * desiredHeight / imagenData.getHeight();

                    float xImage = pageSize.getRight() - width - 20;
                    yImage = pageSize.getTop() - desiredHeight - 20;

                    canvas.addImageFittedIntoRectangle(imagenData, new Rectangle(xImage, yImage, width, desiredHeight), true);
                } else {
                    System.err.println("Imagen no encontrada");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            pdfCanvas.beginText();
            try {
                pdfCanvas.setFontAndSize(PdfFontFactory.createFont(), 9);
            } catch (IOException ex) {
//                Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
            }

            String n_solicitud = "00005";
            float textYPosition = yImage - 20;
            pdfCanvas.moveText(pageSize.getRight() - 65, textYPosition);
            pdfCanvas.showText(String.format("N° : %s", n_solicitud));

            pdfCanvas.moveText(-85, -15);
            pdfCanvas.showText("Fecha de impresión: " + fechaActual);

            pdfCanvas.endText();
            pdfCanvas.release();

        }
    }

    // Método para crear celdas personalizadas.
    private static Cell createCell(String content, TextAlignment alignment, float height, int row, int col, DeviceRgb color) {
        return new Cell(row, col)
                .add(new Paragraph(content))
                .setMargin(0)
                .setFontSize(9)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHeight(height)
                .setBackgroundColor(color);
    }

    private static final String[] UNIDADES = {
        "", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
        "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE"
    };

    private static final String[] DECENAS = {
        "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA", "CIEN"
    };

    private static final String[] CENTENAS = {
        "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    public static String convertir(int numero) {
        if (numero == 0) {
            return "CERO";
        }
        if (numero >= 1_000_000) {
            return convertir(numero / 1_000_000) + " MILLONES " + convertir(numero % 1_000_000);
        }
        if (numero >= 1_000) {
            if (numero >= 2_000) {
                return convertir(numero / 1_000) + " MIL " + convertir(numero % 1_000);
            } else {
                return "MIL " + convertir(numero % 1_000);
            }
        }
        if (numero >= 100) {
            if (numero == 100) {
                return "CIEN";
            } else {
                return CENTENAS[numero / 100] + " " + convertir(numero % 100);
            }
        }
        if (numero >= 20) {
            return DECENAS[(numero / 10) - 2] + (numero % 10 != 0 ? " Y " + UNIDADES[numero % 10] : "");
        }
        return UNIDADES[numero];
    }

    public static String convertirMoneda(double numero) {
        int enteros = (int) numero;
        int decimales = (int) Math.round((numero - enteros) * 100);
        return convertir(enteros) + " CON " + (decimales < 10 ? "0" + decimales : decimales) + "/100 SOLES";
    }

}
