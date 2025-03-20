package com.subcafae.finantialtracker.report.loanReport;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolicitudPrestamo {
    
    // Solicitud de Préstamo
    public void solicitudPrestamo(
            String requestNumber, 
            String principalName, 
            String principalDni, 
            String campo, 
            double montoPrestamo, 
            double refinanciado,
            double interesMensual, //est
            double cuotaMensual,
            String type, 
            int dues, 
            String nameAval, 
            String dniAval, 
            String campoAval) {
        
        try {
            
            String userHome = System.getProperty("user.home");
            String name = userHome + "/Documents/solicitud_prestamo_"+requestNumber+".pdf";
            //String name = userHome + "/Documents/solicitud_prestamo_"+requestNumber+".pdf";
            System.out.println("destiny: " + name);
            PdfWriter writer = new PdfWriter(name);

            LocalDate today = LocalDate.now();
            int day = today.getDayOfMonth(), year = today.getYear();

            String month = today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            month = month.substring(0, 1).toUpperCase() + month.substring(1);

            String date = day + " de " + month + " de " + year;
            
            // Crear un documento PDF
            PdfDocument pdfDoc = new PdfDocument(writer);
            try (Document document = new Document(pdfDoc)) {
                
                // Título principal
                document.add(new Paragraph("SOLICITUD DE PRÉSTAMO SUB CAFAE")
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                
                document.add(new Paragraph("Tipo de Préstamo: " + type + "                                                                                                                                              N°: " + requestNumber)
                        .setFontSize(9)
                        .setTextAlignment(TextAlignment.LEFT));
                
                // A.- SOLICITUD
                Table tableA = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                tableA.addCell(new Paragraph("A.- SOLICITUD")
                        .setFontSize(10)
                        .setBold()
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBorder(Border.NO_BORDER));
                document.add(tableA);

                document.add(new Paragraph(
                        "SEÑOR PRESIDENTE DEL SUBCAFAE DEL HOSPITAL \"SAN JOSÉ\", CALLAO S.P")
                        .setFontSize(9)
                        .setBold());
                
                document.add(new Paragraph(
                        "Yo, " + principalName + ", identificado(a) con DNI N° " + principalDni + "; " +
                        "Servidor Designado al Hospital \"San José\" Callao, del servicio de " + campo + " solicito un préstamo de S/" + String.format("%.2f", montoPrestamo) + " soles para ser descontado en "+ dues +" meses, "+
                        "por motivos PERSONALES para lo cual adjunto boletas de pago como sustento a la presente solicitud para que el personal CAS la presente solicitud deberá ser evaluada.\n "+
                        "Asimismo, conforme a lo establecido en el inciso “C” de la 3ra Disposición Transitoria de la ley 28411 (Ley del Sistema Nacional de Presupuesto); autorizo que se me "+
                        "descuente por planilla de haberes, incentivos laborales o planilla CAS el referido préstamo más un interés por el monto de S/. " + String.format("%.2f", cuotaMensual*dues) + " siendo mi descuento"+
                        " mensual de S/. " + String.format("%.2f", cuotaMensual )  + " durante el número de meses arriba indicado. Mi autorización es extendida bajo la cláusula de destaque y/o origen.\n" +
                        "Agradeciendo anticipadamente la aceptación a la presente.\n" +
                        "Atentamente,\n")
                        .setFontSize(9));

                document.add(new Paragraph("Firma: _________________________      Huella digital: _________________________").setFontSize(9).setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("Callao, "+ date).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
                
                 // Tabla de datos
                Table table = new Table(UnitValue.createPercentArray(3)).useAllAvailableWidth();
                table.addCell(new Cell().add(new Paragraph("MONTO PRÉSTAMO").setFontSize(9)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addCell(new Cell().add(new Paragraph("(-) DEUDA TOTAL ANTERIOR").setFontSize(9)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addCell(new Cell().add(new Paragraph("NETO A GIRAR").setFontSize(9)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                
                table.addCell(new Cell().add(new Paragraph("S/. " + String.format("%.2f", montoPrestamo)).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("S/. " + String.format("%.2f", refinanciado)).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph("S/. " + String.format("%.2f", montoPrestamo - refinanciado)).setFontSize(9)));
                
                document.add(table);
                document.add(new Paragraph("\n").setFontSize(4));

                // B.- EVALUACION DE LA CAPACIDAD DE PAGO
                Table tableB = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                tableB.addCell(new Paragraph("B.- EVALUACIÓN DE LA CAPACIDAD DE PAGO - UNIDAD DE PERSONAL")
                        .setFontSize(10)
                        .setBold()
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBorder(Border.NO_BORDER));
                document.add(tableB);

                document.add(new Paragraph(
                        "El TAP, " + principalName + " del Servicio de " + campo + " (Destacadado del IEEP: ____________________________) \n" + 
                        "a) Cuenta con disponibilidad económica de S/............... para que se efectúe el descuento mensuamente durante (...............) meses, de la Planilla de (............................), de acuerdo al anexo 1 del Manual de Préstamos del SUBCAFAE.\n" + 
                        "b) El TAP solicitante NO cuenta con disponibilidad económica ......\n\n")
                        .setFontSize(9));

                document.add(new Paragraph("Firma: _________________________      V°B°: _________________________").setFontSize(9).setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("Callao, "+ date).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
                               
                // Carta de compromiso del aval
                Table tableCard = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                tableCard.addCell(new Paragraph("CARTA DE COMPROMISO DEL AVAL EL PRÉSTAMO")
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBorder(Border.NO_BORDER)
                        .setFontSize(9));
                document.add(tableCard);
                
                document.add(new Paragraph(
                        "Yo, " + nameAval + ", Identificado con DNI N°" + dniAval + " Trabajador(a) nombrado(a) del Hospital San José - Callao, " +
                        "en el servicio de " + campoAval + " al solicitante por la cantidad de S/ " + String.format("%.2f", montoPrestamo)  + " soles, por el préstamo solicitado al SUBCAFAE del Hospital \"San Jose\" - Callao. En caso de incumplimiento del pago de la deuda por el solicitante, " + 
                        "AUTORIZO a que se me descuente de mis haberes y/o incentivos de acuerdo a la Ley, por la cantidad insoluta del préstamo otorgado.\n\n")
                        .setFontSize(9));
                
                document.add(new Paragraph("Firma: _________________________      V°B°: _________________________").setFontSize(9).setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("Callao, "+ date).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
                                
                // C.- APROBACIÓN POR EL COMITÉ DEL SUBCAFAE
                Table tableD = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
                tableD.addCell(new Paragraph("C.- APROBACIÓN POR EL COMITÉ DEL SUBCAFAE (Reunion del ......./....../...... )")
                        .setFontSize(9)
                        .setBold()
                        .setTextAlignment(TextAlignment.LEFT)
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setBorder(Border.NO_BORDER));
                document.add(tableD);
                

                Table firstRow = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1, 2, 2}));
                firstRow.setWidth(UnitValue.createPercentValue(100));

                firstRow.addCell(createCell("Aceptada", TextAlignment.CENTER, 30, 1)); // Altura de 40
                firstRow.addCell(createEmptyCell());
                firstRow.addCell(createCell("Diferida", TextAlignment.CENTER, 30, 1));
                firstRow.addCell(createEmptyCell());
                firstRow.addCell(createCell("Denegada", TextAlignment.CENTER, 30, 1));
                firstRow.addCell(createEmptyCell());
                firstRow.addCell(createEmptyCell());
                firstRow.addCell(createEmptyCell());


                firstRow.addCell(createCell("Por.....................................................", TextAlignment.LEFT, 15, 6)); // Altura de 15
                firstRow.addCell(createCell("V°B° Presidente", TextAlignment.CENTER, 15, 1)); // Altura de 15
                firstRow.addCell(createCell("V°B° Tesorero", TextAlignment.CENTER, 15, 1)); // Altura de 15

                // Agregar la tabla principal al documento
                document.add(firstRow);
                  
                document.add(new Paragraph("\n\nFirma: _________________________      V°B°: _________________________").setFontSize(9).setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("Callao, "+ date).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
                
                document.close();
            }

            try {
                Desktop.getDesktop().open(new File(name));
            } catch (IOException ex) {
               // Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(CompromisoPagoAval.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Método para crear celdas personalizadas.
    private static Cell createCell(String content, TextAlignment alignment, float height, int col) {
        return new Cell(0, col)
                .add(new Paragraph(content))
                .setMargin(0)
                .setFontSize(9)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHeight(height); 
    }

    // Método para crear celdas vacías sin bordes
    private static Cell createEmptyCell() {
        return new Cell();
    }
    
}
