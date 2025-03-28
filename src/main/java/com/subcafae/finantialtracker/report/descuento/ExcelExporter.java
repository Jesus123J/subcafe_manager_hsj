package com.subcafae.finantialtracker.report.descuento;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExporter {

    public void generateExcel(Object[][] data) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte");

        String[] header = {
            "CODIGO", "DNI", "NOMBRE DE TRABAJADOR", "FECHA", "N°CUOTA", "PLAZOS", "CUOTA", "APORTE", "TOTAL A PRESTAR", "N°OPERACION"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);

                if (data[i][j] instanceof Number) {
                    cell.setCellValue(((Number) data[i][j]).doubleValue());
                } else {
                    cell.setCellValue((String) data[i][j]);
                }
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo Excel");
        fileChooser.setSelectedFile(new File("reporte.xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            boolean success = false;
            while (!success) {
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    JOptionPane.showMessageDialog(null, "Archivo Excel guardado exitosamente.");
                    success = true;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            "El archivo está en uso o no se puede sobrescribir. Por favor, elige otro nombre.",
                            "Error", JOptionPane.ERROR_MESSAGE);

                    // Mostrar nuevamente el JFileChooser
                    userSelection = fileChooser.showSaveDialog(null);
                    if (userSelection != JFileChooser.APPROVE_OPTION) {
                        break; // Si el usuario cancela, salimos del bucle
                    }
                    file = fileChooser.getSelectedFile();
                }
            }
        }

    }
}
