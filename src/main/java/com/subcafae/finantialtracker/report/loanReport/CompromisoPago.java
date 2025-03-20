package com.subcafae.finantialtracker.report.loanReport;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompromisoPago {
    // Compromiso de Pago
    public void compromisoPago(String requestNumber, String nameEmployee, String dniEmployee) {
        try {
            String userHome = System.getProperty("user.home");
            String name = userHome + "/Documents/compromiso_de_pago_"+dniEmployee+".pdf";
            //String name = userHome + "/Downloads/compromiso_de_pago_"+dniEmployee+".pdf";
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
                document.add(new Paragraph("COMPROMISO DE PAGO")
                        .setFontSize(18)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("\n\n"));
                document.add(new Paragraph(
                        nameEmployee + ", identificado(a) con DNI Nº " + dniEmployee + " trabajador(a) del Hospital "
                        + "San José - Callao, en mi calidad de PRESTATARIO(A) del Sub Comité de Administración "
                        + "del Fondo de Asistencia y Estímulo del Hospital San José (SUBCAFAE - HSJ) y en "
                        + "salvaguarda de los intereses de todos los trabajadores beneficiarios del SUBCAFAE-HSJ, "
                        + "me comprometo a depositar directamente en el Banco de la Nación, Cuenta Corriente Nº 0-000-164569, "
                        + "perteneciente al SUBCAFAE - HSJ, el monto que corresponda a las cuotas mensuales por concepto de "
                        + "devolución de préstamo recibido si a través de la Planilla de Remuneraciones y/o Incentivos del Hospital "
                        + "San José - Callao, no se llegara a efectivizar el descuento total.")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Realizado el depósito entregaré el voucher original, en la Oficina del SUBCAFAE-HSJ, quedándome "
                        + "con una copia del mismo (en calidad de cargo), el cual estará debidamente sellada y "
                        + "firmada por personal de la mencionada oficina.")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Asimismo, AUTORIZO al (SUBCAFAE - HSJ), sino cumplo con pagar mis cuotas mensuales de préstamo "
                        + "que solicite descuento, se cobre el monto dejado de pagar a la fecha de cualquier "
                        + "planilla adicional que se pudiera generar por todo tipo de beneficio, bonificación, "
                        + "incentivo o derecho a mi favor.")
                        .setFontSize(11));
                document.add(new Paragraph("1.  CTS (Compensación por tiempo de servicio)\n2.  Vacaciones truncas\n3.  Otro beneficio legal que corresponda al trabajador")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "Es parte de mi responsabilidad como prestatario verificar mensualmente los descuentos que figuran "
                        + "en mi boleta de pago.")
                        .setFontSize(11));
                document.add(new Paragraph(
                        "El incumplimiento de mi compromiso de pago, generará el cobro de la deuda pendiente a mi GARANTE, "
                        + "si lo tuviera y de no ser así, mora sobre la cuota pendiente, equivalente al 2 % mensual.")
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
                document.add(new Paragraph("\n\n\n\n\n\n"));

                document.add(new Paragraph(
                        "____________________")
                        .setFontSize(11)
                        .setBold()
                        .setFirstLineIndent(17)
                        .setMarginBottom(-2));
                document.add(new Paragraph(
                        nameEmployee)
                        .setFontSize(11)
                        .setFirstLineIndent(30)
                        .setMarginTop(-2)
                        .setMarginBottom(-2));

                document.add(new Paragraph(
                        "DNI Nº " + dniEmployee + "                                                          (Ref.: Solicitud de Préstamo Nº " + requestNumber + ")")
                        .setFontSize(11)
                        .setFirstLineIndent(30)
                        .setMarginTop(-2));
                document.close();
            }

            Desktop.getDesktop().open(new File(name));
        } catch (IOException ex) {
          //  Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
