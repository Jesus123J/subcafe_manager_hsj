/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.descuento;

import com.subcafae.finantialtracker.data.entity.Loan;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    public static void exportToExcel(List<Loan> loans, String message, int broad) throws IOException {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de Excel", "xls");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Guardar Archivo");
        chooser.setAcceptAllFileFilterUsed(false);

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
                    Sheet hoja = book.createSheet("Préstamos");

                    // Encabezado de la tabla con estilos
                    Row headerRow = hoja.createRow(3);
                    String[] columnas = {"FECHA DE APROBACION", "N° SOLICITUD", "SOLICITANTE", "GARANTE", "REFINANCIADO",
                        "MONTO SOLICITADO", "MONTO GIRADO", "CANT. CUOTAS", "INTERES TOTAL",
                        "FONDO INTANGIBLE TOTAL", "CUOTA MENSUAL CAPITAL", "CUOTA INTERES MENSUAL",
                        "CUOTA FONDO INTANGIBLE MENSUAL", "VALOR CUOTA MENSUAL", "ESTADO", "RESPONSABILIDAD DE PAGO"};

                    CellStyle headerStyle = book.createCellStyle();
                    Font headerFont = book.createFont();
                    headerFont.setBold(true);
                    headerStyle.setFont(headerFont);
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);
                    headerStyle.setBorderBottom(BorderStyle.THIN);

                    for (int c = 0; c < columnas.length; c++) {
                        Cell celda = headerRow.createCell(c);
                        celda.setCellValue(columnas[c]);
                        celda.setCellStyle(headerStyle);
                    }

                    // Variables para almacenar sumas
                    double totalRefinanciado = 0, totalMontoSolicitado = 0, totalMontoGirado = 0,
                            totalInteresTotal = 0, totalFondoIntangible = 0, totalCuotaCapital = 0,
                            totalCuotaInteres = 0, totalCuotaFondoIntangible = 0, totalValorCuota = 0;

                    // Filtrar préstamos por estado "Aceptado"
                    int rowNum = 4;
                    for (Loan loan : loans) {

                        if (loan.getRequestedAmount() != loan.getAmountWithdrawn()) {
                            if (loan.getRefinanciado() == null) {
                                if (!loan.getAmountWithdrawn().toString().equals("0.00")) {
                                    Double daod = Double.parseDouble(loan.getRequestedAmount().toString()) - Double.parseDouble(loan.getAmountWithdrawn().toString());
                                    loan.setRefinanciado(BigDecimal.valueOf(daod));
                                }
                            }
                        }
                        if ("Aceptado".equalsIgnoreCase(loan.getState()) && loan.getModificado() != null) {
                            
                            System.out.println("Model - > " + loan.toString());
                            
                            Row fila = hoja.createRow(rowNum++);
                            
                            fila.createCell(0).setCellValue(loan.getModificado());
                            fila.createCell(1).setCellValue(loan.getSoliNum());
                            fila.createCell(2).setCellValue(loan.getSolicitorName());
                            fila.createCell(3).setCellValue(loan.getGuarantorName() == null ? "" : loan.getGuarantorName());

                            double refinanciado = loan.getRefinanciado() == null ? 0 : loan.getRefinanciado().doubleValue();
                           
                            fila.createCell(4).setCellValue(refinanciado);
                            totalRefinanciado += refinanciado;

                            double montoSolicitado = loan.getRequestedAmount().doubleValue();
                            fila.createCell(5).setCellValue(montoSolicitado);
                            totalMontoSolicitado += montoSolicitado;

                            double montoGirado = loan.getAmountWithdrawn() == null || loan.getAmountWithdrawn().doubleValue() == 0.00
                                    ? loan.getRequestedAmount().doubleValue() : loan.getAmountWithdrawn().doubleValue();

                            fila.createCell(6).setCellValue(montoGirado);
                            totalMontoGirado += montoGirado;

                            fila.createCell(7).setCellValue(loan.getCantCuota());

                            double interesTotal = loan.getInterTo() == null ? 0 : loan.getInterTo().doubleValue();
                            fila.createCell(8).setCellValue(interesTotal);
                            totalInteresTotal += interesTotal;

                            double fondoIntangible = loan.getFondoTo() == null ? 0 : loan.getFondoTo().doubleValue();
                            fila.createCell(9).setCellValue(fondoIntangible);
                            totalFondoIntangible += fondoIntangible;

                            double cuotaMensualCapital = loan.getCuotaMenSin() == null ? 0 : loan.getCuotaMenSin().doubleValue();
                            fila.createCell(10).setCellValue(cuotaMensualCapital);
                            totalCuotaCapital += cuotaMensualCapital;

                            double cuotaInteresMensual = loan.getCuotaInter() == null ? 0 : loan.getCuotaInter().doubleValue();
                            fila.createCell(11).setCellValue(cuotaInteresMensual);
                            totalCuotaInteres += cuotaInteresMensual;

                            double cuotaFondoIntangible = loan.getCuotaFond() == null ? 0 : loan.getCuotaFond().doubleValue();
                            fila.createCell(12).setCellValue(cuotaFondoIntangible);
                            totalCuotaFondoIntangible += cuotaFondoIntangible;

                            double valorCuotaMensual = loan.getValor() == null ? 0 : loan.getValor().doubleValue();
                            fila.createCell(13).setCellValue(valorCuotaMensual);
                            totalValorCuota += valorCuotaMensual;

                            fila.createCell(14).setCellValue(loan.getState());
                            fila.createCell(15).setCellValue(loan.getPaymentResponsibility());
                        }
                    }

                    // Fila de totales, alineada con las columnas correspondientes
                    double[] totales = {totalRefinanciado, totalMontoSolicitado, totalMontoGirado, totalInteresTotal,
                        totalFondoIntangible, totalCuotaCapital, totalCuotaInteres, totalCuotaFondoIntangible, totalValorCuota};

                    CellStyle boldStyle = book.createCellStyle();
                    Font boldFont = book.createFont();
                    boldFont.setBold(true);
                    boldStyle.setFont(boldFont);

                    Row totalRow = hoja.createRow(rowNum);

                    Cell name = totalRow.createCell(3);
                    name.setCellValue("TOTAL : ");
                    name.setCellStyle(boldStyle);

                    Cell totalCell = totalRow.createCell(4);
                    totalCell.setCellValue(totales[0]);
                    totalCell.setCellStyle(boldStyle);

                    Cell a = totalRow.createCell(5);
                    a.setCellValue(totales[1]);
                    a.setCellStyle(boldStyle);

                    Cell b = totalRow.createCell(6);
                    b.setCellValue(totales[2]);
                    b.setCellStyle(boldStyle);

                    Cell c = totalRow.createCell(8);
                    c.setCellValue(totales[3]);
                    c.setCellStyle(boldStyle);

                    Cell d = totalRow.createCell(9);
                    d.setCellValue(totales[4]);
                    d.setCellStyle(boldStyle);

                    Cell e = totalRow.createCell(10);
                    e.setCellValue(totales[5]);
                    e.setCellStyle(boldStyle);
                    Cell f = totalRow.createCell(11);
                    f.setCellValue(totales[6]);
                    f.setCellStyle(boldStyle);

                    Cell g = totalRow.createCell(12);
                    g.setCellValue(totales[7]);
                    g.setCellStyle(boldStyle);

                    Cell df = totalRow.createCell(13);
                    df.setCellValue(totales[8]);
                    df.setCellStyle(boldStyle);

                    // Ajustar automáticamente el ancho de las columnas
                    for (int das = 0; das < columnas.length; das++) {
                        hoja.autoSizeColumn(das);
                    }

                    book.write(archive);
                }
                Desktop.getDesktop().open(archiveXLS);

            } catch (IOException | NumberFormatException e) {
                throw e;
            }
        }
    }

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
