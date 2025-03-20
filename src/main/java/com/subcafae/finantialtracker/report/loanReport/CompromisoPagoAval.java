package com.subcafae.finantialtracker.report.loanReport;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompromisoPagoAval {
    
    public void compromisoPagoAval(String requestNumber, String nameAval, String dniAval, String nameEmployee, String dniEmployee) {
        try {
            // Crear un escritor de PDF
            String userHome = System.getProperty("user.home");
            String name = userHome + 
                    "/Documents/compromiso_de_pago_aval_"+dniAval+".pdf";
                //    "/Downloads/compromiso_de_pago_aval_"+dniAval+".pdf";
            PdfWriter writer = new PdfWriter(name);
            LocalDate today = LocalDate.now();
            int day = today.getDayOfMonth(), year = today.getYear();
            String month = today.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            month = month.substring(0, 1).toUpperCase() + month.substring(1);
            String date = day + " de " + month + " de " + year;
            // Crear un documento PDF
            PdfDocument pdfDoc = new PdfDocument(writer);
            try (Document document = new Document(pdfDoc)) {
                // Agregar el contenido al documento
                document.add(new Paragraph("COMPROMISO DE PAGO AVAL")
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("\n\n"));
                document.add(new Paragraph(
                        nameAval + " identificado(a) con DNI Nº " + dniAval + " trabajador(a) del "
                        + "Hospital San José - Callao, en mi calidad de GARANTE del Sr.(Sra) "
                        + nameEmployee + ", del Sub Comité de Administración del Fondo de Asistencia y "
                        + "Estímutlo del Hospital San José (SUBCAFAE-HSJ), en salvaguarda "
                        + "de los intereses de todos los trabajadores beneficiarios del SUBCAFAE-HSJ, "
                        + "autorizo al SUBCAFAE-HSJ, para que a través de la Planilla de Remuneraciones "
                        + "y/o Incentivos del Hospital San José - Callao, se descuente el monto que corresponda"
                        + " a la(s) cuota(s) mensual(s) que dejara de pagar el (a), PRESTATARIO(A), "
                        + "a la que garantizo. (Ref.: Solicitud de Préstamo N° " + requestNumber + ")")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Asimismo, AUTORIZO al (SUBCAFAE - HSJ), sino cumplo con pagar la(s) cuotas mensuales de préstamo, "
                        + "que solicito el PRESTATARIO se cobre el monto dejado de pagar a la fecha, de cualquier "
                        + "planilla adicional que se pudiera generar por todo tipo de beneficio, bonificación, "
                        + "incentivo o derecho a mi favor.")
                        .setFontSize(11));
                document.add(new Paragraph("1.  CTS (Compensación por tiempo de servicio)\n2.  Vacaciones truncas\n3.  "
                        + "Otro beneficio legal que corresponda al trabajador")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Es parte de mi responsabilidad como GARANTE verificar con el PRESTATARIO que mensualmente se "
                        + "realicen los descuentos del préstamo solicitado y que figuren en la boleta de pago.")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Si mi incumplimiento de pago fuera mayor a 3 meses, seré inhabilitado por 02 años para recibir "
                        + "futuros préstamos o ser garante.")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "En señal de conformidad con lo anteriormente manifestado, ratifico mi compromiso y firmo la presente.")
                        .setFontSize(11));

                document.add(new Paragraph(
                        "Callao, " + date)
                        .setFontSize(11));
                document.add(new Paragraph("\n\n\n\n\n\n\n\n\n\n\n"));

                document.add(new Paragraph(
                        "___________________")
                        .setFontSize(11)
                        .setBold()
                        .setFirstLineIndent(17)
                        .setMarginBottom(-2));
                document.add(new Paragraph(
                        "GARANTE")
                        .setFontSize(11)
                        .setFirstLineIndent(45)
                        .setMarginTop(-2)
                        .setMarginBottom(-2));

                document.add(new Paragraph(
                        "DNI Nº " + dniAval + "                                                          "
                        + "(Ref.: Solicitud de Préstamo Nº " + requestNumber + ")")
                        .setFontSize(11)
                        .setFirstLineIndent(35)
                        .setMarginTop(-2));
                document.close();
            }
            try {
                Desktop.getDesktop().open(new File(name));
            } catch (IOException ex) {
                //Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);//
            }
        } catch (FileNotFoundException ex) {
            //Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
