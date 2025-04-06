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
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        Map<String, List<RegistroDetailsModel>> listFilte = listRegister.stream().collect(Collectors.groupingBy(RegistroDetailsModel::getCodigo));

        for (Map.Entry<String, List<RegistroDetailsModel>> entry : listFilte.entrySet()) {

            List<String> date = new ArrayList<>();
            Map<String, String> mapAndPaymeny = new HashMap<>();

            for (RegistroDetailsModel registroDetailsModel : entry.getValue()) {
                if (registroDetailsModel.getConceptLoan() != null) {
                    mapAndPaymeny.put(registroDetailsModel.getConceptLoan(),
                            registroDetailsModel.getAmountPar().toString() + " - "
                            + registroDetailsModel.getMontoLoan().toString());

                    // SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                    date.add(registroDetailsModel.getFechaVLoan());
                } else {
                    mapAndPaymeny.put(registroDetailsModel.getConceptBond(),
                            registroDetailsModel.getAmountPar().toString() + " - "
                            + registroDetailsModel.getMontoBond().toString());

                    date.add(registroDetailsModel.getFechaVBond());
                }
            }

            ModelPayment modePayment = new ModelPayment();
            //getMapAndPaymeny
            modePayment.setCodeDocument(entry.getKey());
            modePayment.setFecha(entry.getValue().getFirst().getFechaRegistro());
            modePayment.setAmountPaymentAndLoan(date);
            modePayment.setMapAndPaymeny(mapAndPaymeny);
            modePayment.setAmountDocument(String.format("%.2f", entry.getValue().getFirst().getAmount()));

            moPayments.add(modePayment);
        }

        return moPayments;
    }

    public void HistoryPatment(String dni) {
        comp(dataPaymentAndLoan(dni));

    }

    public void comp(List<ModelPayment> data) {

        System.out.println(" data " + data.toString());

        String userHome = System.getProperty("user.home");
        String dest = userHome
                + "/Documents/historial_pag.pdf";

        PdfWriter writer;
        PdfDocument pdf;
        Document document = null;
        try {
            writer = new PdfWriter(dest);

            pdf = new PdfDocument(writer);
            document = new Document(pdf, PageSize.A4);

            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new Header());

            document.setMargins(100, 30, 30, 30);
            document.add(new Paragraph("HISTORIAL DE PAGOS")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("DNI: " + firstEmployee.getNationalId()));
            document.add(new Paragraph("Apellidos y Nombres: " + firstEmployee.getLastName() + " " + firstEmployee.getFirstName()));
        } catch (FileNotFoundException ex) {

        }
        PdfFont monoFont = null;
        try {
            monoFont = PdfFontFactory.createFont(StandardFonts.COURIER);
        } catch (IOException ex) {

        }
        try {
            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2, 2, 2}));
            mainTable.setWidth(UnitValue.createPercentValue(50));

            // Encabezados .setVerticalAlignment(VerticalAlignment.MIDDLE)
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Fecha").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(60f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Documento").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(70f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Monto Planilla").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(70f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Concepto de Pago").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(80f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Vencimiento").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(70f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Monto Pagado").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(60f).setBold());
            mainTable.addHeaderCell(new Cell().add(new Paragraph("Monto Original").setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setFontSize(7f).setTextAlignment(TextAlignment.CENTER).setMinWidth(60f).setBold());

            for (ModelPayment payment : data) {
                // Agregar las celdas a la tabla principal
                mainTable.addCell(
                        new Cell().add(
                                new Paragraph(
                                        payment.getFecha()).setFont(
                                        monoFont)).setVerticalAlignment(
                                        VerticalAlignment.MIDDLE).setTextAlignment(
                                        TextAlignment.CENTER).setFontSize(7f)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f));
                mainTable.addCell(new Cell().add(new Paragraph(payment.getCodeDocument())
                        .setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setFontSize(7f)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)).setMarginTop(5f);
                mainTable.addCell(new Cell().add(new Paragraph(
                        payment.getAmountDocument()).setFont(monoFont)).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).setFontSize(7f)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f));
                //mainTable.addCell(new Cell(1, 3).add(subTable)); // La sub-tabla ocupa 3 columnas 

                List<String> dateList = payment.getAmountPaymentAndLoan();
                List<Map.Entry<String, String>> amountList = new ArrayList<>(payment.getMapAndPaymeny().entrySet());

                int maxRows = Math.max(dateList.size(), amountList.size());
// Obtener el ancho de la columna principal

// Crear la subtabla con 3 columnas
                Table subTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}));
                subTable.setWidth(UnitValue.createPercentValue(100));

// Agregar celdas con un ancho fijo
                for (int i = 0; i < maxRows; i++) {
                    Cell conceptCell = new Cell().add(new Paragraph(i < amountList.size() ? amountList.get(i).getKey() : " ")).setFontSize(7f)
                            .setTextAlignment(TextAlignment.LEFT).setMinWidth(80f).setMaxWidth(80f);
                    subTable.addCell(conceptCell).setBorder(Border.NO_BORDER);

                    Cell dateCell = new Cell().add(new Paragraph(i < dateList.size() ? dateList.get(i) : " ")).setFontSize(7f)
                            .setTextAlignment(TextAlignment.CENTER).setMinWidth(70f).setMaxWidth(70f);
                    subTable.addCell(dateCell).setBorder(Border.NO_BORDER);

                    Cell amountParcialCell = new Cell().add(new Paragraph(i < amountList.size() ? amountList.get(i).getValue().toString().split("-")[0].trim().toString() : " "))
                            .setTextAlignment(TextAlignment.CENTER).setMinWidth(60f).setMaxWidth(60f).setFontSize(7f);
                    subTable.addCell(amountParcialCell).setBorder(Border.NO_BORDER);

                    Cell amountTotalCell = new Cell().add(new Paragraph(i < amountList.size() ? amountList.get(i).getValue().toString().split("-")[1].trim().toString() : " "))
                            .setTextAlignment(TextAlignment.CENTER).setMinWidth(60f).setMaxWidth(60f).setFontSize(7f);
                    subTable.addCell(amountTotalCell).setBorder(Border.NO_BORDER);
                }

// Crear la celda que contendrá la subtabla
                Cell subTableCell = new Cell(1, 4).add(subTable);
                subTableCell.setBorder(Border.NO_BORDER);
                subTableCell.setPadding(0);
                subTableCell.setWidth(50); // Fijar el ancho para que no se desajuste
                mainTable.addCell(subTableCell).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f));
            }

            document.add(mainTable);
            document.add(new LineSeparator(new SolidLine(1f)) // Grosor de 1 punto
                    .setMarginTop(10).setMarginTop(10));

            document.close();

            System.out.println("Reporte generado en: " + dest);
            try {
                Desktop.getDesktop().open(new File(dest));
            } catch (IOException ex) {
                //Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Exception e) {
            System.out.println(" error " + e.getMessage());
        }

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
                        .showText("HISTORIAL DE PAGOS")
                        .moveText(200, 0)
                        .showText("Fecha: " + fechaActual)
                        .moveText(250, 0)
                        .showText("Página: " + pageNumber)
                        .endText();

                pdfCanvas.moveTo(pageSize.getLeft() + 30, pageSize.getTop() - 35)
                        .lineTo(pageSize.getRight() - 30, pageSize.getTop() - 35)
                        .setStrokeColor(ColorConstants.BLACK)
                        .setLineWidth(1)
                        .stroke();

            } catch (IOException ex) {
                //  Logger.getLogger(ReporteAbono.class.getName()).log(Level.SEVERE, null, ex);
            }

            pdfCanvas.release();
        }

    }
}
