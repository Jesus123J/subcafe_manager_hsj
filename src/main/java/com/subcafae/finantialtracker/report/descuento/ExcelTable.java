/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.descuento;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author Jesus Gutierrez
 */
public class ExcelTable {

    public static void exportToExcel(JTable t, String message, int broad) throws IOException {
        // Crear un Chooser para especificar la ruta de Guardado de archivo Excel
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de excel", "xls");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Guardar Archivo");
        chooser.setAcceptAllFileFilterUsed(false);

        // Verificamos si el usuario aprobó la selección de un archivo
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            String location = chooser.getSelectedFile().toString().concat(".xls");
            try {
                File archiveXLS = new File(location);
                if (archiveXLS.exists()) {
                    archiveXLS.delete();
                }
                archiveXLS.createNewFile();
                Workbook book = new HSSFWorkbook();
                try (FileOutputStream archive = new FileOutputStream(archiveXLS)) {
                    Sheet hoja = book.createSheet("Prueba");

                    // Crear estilo de celda para el encabezado
                    CellStyle headerStyle = book.createCellStyle();
                    Font headerFont = book.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    headerStyle.setBorderBottom(BorderStyle.THIN);
                    headerStyle.setBorderTop(BorderStyle.THIN);
                    headerStyle.setBorderLeft(BorderStyle.THIN);
                    headerStyle.setBorderRight(BorderStyle.THIN);
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);

                    // Crear estilo de celda para los datos
                    CellStyle dataStyle = book.createCellStyle();
                    dataStyle.setBorderBottom(BorderStyle.THIN);
                    dataStyle.setBorderTop(BorderStyle.THIN);
                    dataStyle.setBorderLeft(BorderStyle.THIN);
                    dataStyle.setBorderRight(BorderStyle.THIN);

                    CellStyle messageStyle = book.createCellStyle();
                    Font messageFont = book.createFont();
                    messageFont.setBold(true);
                    messageStyle.setFont(messageFont);
                    messageStyle.setAlignment(HorizontalAlignment.CENTER);
                    messageStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    // Agregar mensaje en el rango B2:B?
                    Row messageRow = hoja.createRow(1);
                    Cell cell = messageRow.createCell(1);
                    cell.setCellValue(message.toUpperCase());
                    cell.setCellStyle(messageStyle);

                    // Combinar celdas B2:F2
                    hoja.addMergedRegion(new CellRangeAddress(1, 1, 1, broad));

                    // Proporcionar datos del jTable al encabezado
                    Row headerRow = hoja.createRow(3); // Cambiar la fila a la 4 (índice 3)

                    for (int c = 0; c < t.getColumnCount(); c++) {
                        Cell celda = headerRow.createCell(c + 1); // Cambiar la columna a la 2 (índice 1)
                        celda.setCellValue(t.getColumnName(c));
                        celda.setCellStyle(headerStyle);
                    }

                    // Escribir datos
                    for (int f = 0; f < t.getRowCount(); f++) {
                        Row fila = hoja.createRow(f + 4); // Cambiar la fila de inicio a la 5 (índice 4)
                        for (int c = 0; c < t.getColumnCount(); c++) {
                            Cell celda = fila.createCell(c + 1); // Cambiar la columna a la 2 (índice 1)
                            celda.setCellStyle(dataStyle);
                            if (t.getValueAt(f, c) == null) {
                                celda.setCellValue("");
                            }
                            if (t.getValueAt(f, c) instanceof Double) {
                                celda.setCellValue((Double) t.getValueAt(f, c));
                            } else if (t.getValueAt(f, c) instanceof Float) {
                                celda.setCellValue((Float) t.getValueAt(f, c));
                            } else {
                                celda.setCellValue(String.valueOf(t.getValueAt(f, c)));
                            }
                        }
                    }

                    // Ajustar el ancho de las columnas
                    for (int c = 0; c < t.getColumnCount(); c++) {
                        hoja.autoSizeColumn(c + 1);
                        // Incrementar el ancho en un 20% adicional
                        int currentWidth = hoja.getColumnWidth(c + 1);
                        hoja.setColumnWidth(c + 1, (int) (currentWidth * 1.2));
                    }

                    book.write(archive);
                }
                Desktop.getDesktop().open(archiveXLS);

            } catch (IOException | NumberFormatException e) {
//                Logger.getLogger(TabbedPane.class
//                        .getName()).log(Level.SEVERE, null, e);
                throw e;
            }
        }
    }

    public static void exportToExcel(JTable t1, JTable t2, String message1, String message2, int broad) throws IOException {
        // Crear un Chooser para especificar la ruta de Guardado de archivo Excel
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de excel", "xls");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Guardar Archivo");
        chooser.setAcceptAllFileFilterUsed(false);

        // Verificamos si el usuario aprobó la selección de un archivo
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            String location = chooser.getSelectedFile().toString().concat(".xls");
            try {
                File archiveXLS = new File(location);
                if (archiveXLS.exists()) {
                    archiveXLS.delete();
                }
                archiveXLS.createNewFile();
                Workbook book = new HSSFWorkbook();
                try (FileOutputStream archive = new FileOutputStream(archiveXLS)) {
                    Sheet hoja = book.createSheet("Prueba");

                    // Estilo de celda para encabezado
                    CellStyle headerStyle = book.createCellStyle();
                    Font headerFont = book.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    headerStyle.setBorderBottom(BorderStyle.THIN);
                    headerStyle.setBorderTop(BorderStyle.THIN);
                    headerStyle.setBorderLeft(BorderStyle.THIN);
                    headerStyle.setBorderRight(BorderStyle.THIN);
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);

                    // Estilo de celda para datos
                    CellStyle dataStyle = book.createCellStyle();
                    dataStyle.setBorderBottom(BorderStyle.THIN);
                    dataStyle.setBorderTop(BorderStyle.THIN);
                    dataStyle.setBorderLeft(BorderStyle.THIN);
                    dataStyle.setBorderRight(BorderStyle.THIN);

                    // Estilo para mensajes
                    CellStyle messageStyle = book.createCellStyle();
                    Font messageFont = book.createFont();
                    messageFont.setBold(true);
                    messageStyle.setFont(messageFont);
                    messageStyle.setAlignment(HorizontalAlignment.CENTER);
                    messageStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    // Procesar primera tabla
                    int currentRow = 0;

                    // Agregar mensaje 1
                    Row messageRow1 = hoja.createRow(currentRow++);
                    Cell cellMessage1 = messageRow1.createCell(1);
                    cellMessage1.setCellValue(message1.toUpperCase());
                    cellMessage1.setCellStyle(messageStyle);
                    hoja.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, broad));

                    // Encabezado de la tabla 1
                    Row headerRow1 = hoja.createRow(currentRow++);
                    for (int c = 0; c < t1.getColumnCount(); c++) {
                        Cell celda = headerRow1.createCell(c + 1);
                        celda.setCellValue(t1.getColumnName(c));
                        celda.setCellStyle(headerStyle);
                    }

                    // Datos de la tabla 1
                    for (int f = 0; f < t1.getRowCount(); f++) {
                        Row fila = hoja.createRow(currentRow++);
                        for (int c = 0; c < t1.getColumnCount(); c++) {
                            Cell celda = fila.createCell(c + 1);
                            celda.setCellStyle(dataStyle);
                            if (t1.getValueAt(f, c) instanceof Double) {
                                celda.setCellValue((Double) t1.getValueAt(f, c));
                            } else if (t1.getValueAt(f, c) instanceof Float) {
                                celda.setCellValue((Float) t1.getValueAt(f, c));
                            } else {
                                celda.setCellValue(String.valueOf(t1.getValueAt(f, c)));
                            }
                        }
                    }

                    // Espacio entre tablas
                    currentRow += 2;

                    // Procesar segunda tabla
                    // Agregar mensaje 2
                    Row messageRow2 = hoja.createRow(currentRow++);
                    Cell cellMessage2 = messageRow2.createCell(1);
                    cellMessage2.setCellValue(message2.toUpperCase());
                    cellMessage2.setCellStyle(messageStyle);
                    hoja.addMergedRegion(new CellRangeAddress(currentRow - 1, currentRow - 1, 1, broad));

                    // Encabezado de la tabla 2
                    Row headerRow2 = hoja.createRow(currentRow++);
                    for (int c = 0; c < t2.getColumnCount(); c++) {
                        Cell celda = headerRow2.createCell(c + 1);
                        celda.setCellValue(t2.getColumnName(c));
                        celda.setCellStyle(headerStyle);
                    }

                    // Datos de la tabla 2
                    for (int f = 0; f < t2.getRowCount(); f++) {
                        Row fila = hoja.createRow(currentRow++);
                        for (int c = 0; c < t2.getColumnCount(); c++) {
                            Cell celda = fila.createCell(c + 1);
                            celda.setCellStyle(dataStyle);
                            if (t2.getValueAt(f, c) instanceof Double) {
                                celda.setCellValue((Double) t2.getValueAt(f, c));
                            } else if (t2.getValueAt(f, c) instanceof Float) {
                                celda.setCellValue((Float) t2.getValueAt(f, c));
                            } else {
                                celda.setCellValue(String.valueOf(t2.getValueAt(f, c)));
                            }
                        }
                    }

                    // Ajustar ancho de columnas
                    for (int c = 0; c < Math.max(t1.getColumnCount(), t2.getColumnCount()); c++) {
                        hoja.autoSizeColumn(c + 1);
                        int currentWidth = hoja.getColumnWidth(c + 1);
                        hoja.setColumnWidth(c + 1, (int) (currentWidth * 1.2));
                    }

                    book.write(archive);
                }
                Desktop.getDesktop().open(archiveXLS);

            } catch (IOException e) {

            }
        }
    }

}
