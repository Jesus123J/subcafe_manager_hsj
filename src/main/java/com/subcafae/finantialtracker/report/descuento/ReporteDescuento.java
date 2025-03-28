package com.subcafae.finantialtracker.report.descuento;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReporteDescuento {
    public void ReporteDescuento(
                String modalidad,
                String planilla,
                String mes,
                Object[][] data,
                String codigo,
                int registros){
        String userHome = System.getProperty("user.home");
        String dest = userHome + "/Documents/reporte_descuentos_" + codigo + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("REPORTE DE DESCUENTOS POR PLANILLA DE PERSONAL")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));
            document.add(new Paragraph("\nMODALIDAD: " + modalidad).setFontSize(10));
            document.add(new Paragraph("PLANILLA: " + planilla).setFontSize(10));
            document.add(new Paragraph("MES: " + mes).setFontSize(10));
            document.add(new Paragraph("\n")); 

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1}));
            table.setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"DNI", "Apellidos y Nombres del Personal", "Monto"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold().setFontSize(10))
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(0.5f)));
            }

            for (Object[] row : data) {
                for (Object cell : row) {
                    String cellText = cell != null ? cell.toString() : "";  // Verificación de nulos
                    table.addCell(new Cell().add(new Paragraph(cellText).setFontSize(8))
                            .setBorder(Border.NO_BORDER));
                }
            }
            System.out.println(Arrays.toString(data));
            document.add(table.setBorderBottom(new SolidBorder(0.5f)));
            document.add(new Paragraph("TOTAL REG.: " + registros +
                    "                                                                                                                  Total: " + calcularTotal(data))
                    .setTextAlignment(TextAlignment.LEFT).setFontSize(10));

            document.close();
            System.out.println("Reporte generado en: " + dest);
            try {
                Desktop.getDesktop().open(new File(dest));
            } catch (IOException ex) {
              //  Logger.getLogger(TabbedPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String calcularTotal(Object[][] datos) {
        double total = 0;
        for (Object[] row : datos) {
            if (row.length >= 3 && row[2] != null) {  // Verificación de tamaño y de nulos
                try {
                    total += Double.parseDouble(row[2].toString());
                } catch (NumberFormatException e) {
                    System.err.println("Valor no numérico encontrado en la columna de Monto: " + row[2]);
                }
            }
        }
        return String.format("%.2f", total);
    }
}
