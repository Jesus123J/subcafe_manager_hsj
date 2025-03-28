/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.loanReport;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import java.awt.Desktop;
import java.io.File;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author Jesus Gutierrez
 */
public class ExcelDemo {

    public void excelDemo(
            String refinanciamiento,
            double prestamoSoli,
            double montoGir,
            double interes,
            double fondoInta,
            double descuentoMens,
            int meses,
            String dniSoli
    ) {
        String EmployeeDNI = dniSoli;
        JTextField text = new JTextField();
        if (dniSoli == null) {

            TextFieldValidator.applyDecimalFilter(text);
            int option = JOptionPane.showConfirmDialog(null, text, "Escribir DNI del trabajdor", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                EmployeeDNI = text.getText();
            }
        }
        // Crear el workbook y la hoja
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Liquidación_Préstamo_" + EmployeeDNI);

        // Estilos básicos
        CellStyle largeTitleStyle = createCellStyle(workbook, true, 20, HorizontalAlignment.CENTER);
        CellStyle titleStyle = createCellStyle(workbook, true, 16, HorizontalAlignment.CENTER);
        CellStyle subTitleStyle = createCellStyle(workbook, true, 8, HorizontalAlignment.LEFT); // Cambiado a izquierda
        CellStyle headerStyle = createCellStyle(workbook, true, 10, HorizontalAlignment.LEFT); // Alineado a la izquierda
        CellStyle borderedStyle = createBorderedCellStyle(workbook); // Estilo con bordes visibles
        CellStyle centeredStyle = createCellStyle(workbook, false, 10, HorizontalAlignment.CENTER);

        // 1. SUBCAFAE - HOSPITAL SAN JOSÉ (Título principal)
        createMergedCell(sheet, 0, 1, 0, 3, "SUB-CAFAE - HOSPITAL SAN JOSE", largeTitleStyle);

        // 2. Subtítulos secundarios con reducción de tamaño y negrita
        createMergedCell(sheet, 2, 2, 0, 3,
                "SUB COMITE DE ADMINISTRACIÓN DE FONDOS DE ASISTENCIA Y ESTÍMULOS - SAN JOSE",
                subTitleStyle);
        createMergedCell(sheet, 3, 3, 0, 3,
                "LAS MAGNOLIAS 476 - CARMEN DE LA LEGUA - REYNOSO, RUC 20505869525", subTitleStyle);
        createMergedCell(sheet, 4, 4, 0, 3, "TEL. 957067860", subTitleStyle);

        // 3. LIQUIDACIÓN DE PRÉSTAMO DEL SUB-CAFAE
        createMergedCell(sheet, 6, 7, 0, 5, "LIQUIDACIÓN DE PRÉSTAMO DEL SUB-CAFAE", largeTitleStyle);

        LocalDate fechaActual = LocalDate.now();

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 4. SOLICITUD y fecha
        createMergedCell(sheet, 10, 10, 3, 5, "SOLICITUD N° " + "DEMO", titleStyle);
        Row dateRow = sheet.createRow(11);
        createCell(dateRow, 5, fechaActual.format(formato), centeredStyle);

        // Employee trabajador = new EmployeeD().search("Dni", EmployeeDNI);
        Optional<EmployeeTb> trabajador = null;
        try {
            trabajador = new EmployeeDao().findById(EmployeeDNI);
            if (trabajador.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se encontro trabajador", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            System.out.println("Message -> " + ex.getMessage());
        }

// 5. Información personal
        String[][] personalInfo = {
            {"APELLIDOS Y NOMBRES :", trabajador.get().getFirstName().concat(" " + trabajador.get().getLastName())},
            {"DNI :", trabajador.get().getNationalId()},
            {"MODALIDAD :", trabajador.get().getEmploymentStatus()},
            {"PRÉSTAMO :", "ORDINARIO"}
        };
        int personalInfoRowStart = 13;
        for (String[] info : personalInfo) {
            Row row = sheet.createRow(personalInfoRowStart++);
            createCell(row, 0, info[0], headerStyle);
            createMergedCell(sheet, personalInfoRowStart - 1, personalInfoRowStart - 1, 1, 3, info[1], null);
        }

        // 6. Encabezado de MONTOS
        createMergedCell(sheet, personalInfoRowStart + 1, personalInfoRowStart + 1, 4, 5, "MONTOS", titleStyle);

        // 7. Agregar los textos de los montos en las filas 20 a 27 (columnas A a D)
        String[] montoTextos = {
            "SALDO DEUDA ANTERIOR SUBCAFAE",
            "PRÉSTAMO SOLICITADO",
            "MONTO A GIRAR",
            "INTERESES",
            "FONDO INTANGIBLE",
            "TOTAL PRÉSTAMO",
            "DESCUENTO MENSUAL",
            "MESES A PAGAR"
        };

        // Agregar los textos a las filas 20 a 27
        int rowIndex = personalInfoRowStart + 2; // Comenzamos después del encabezado "MONTOS"
        for (String texto : montoTextos) {
            Row row = sheet.createRow(rowIndex++);
            // Aquí se usa el estilo con bordes en las celdas combinadas
            createMergedCell(sheet, rowIndex - 1, rowIndex - 1, 0, 3, texto, borderedStyle);

            // Ajustamos la altura de las filas 20 a 27 (1.5 veces el tamaño por defecto)
            if (rowIndex >= 20 && rowIndex <= 27) {
                row.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 1.5f); // 150% de la altura por defecto
            }
        }

        // 8. Agregar los montos en columnas E y F (filas 20 a 27)
        String[][] amounts = {
            {"", refinanciamiento},
            {"", String.valueOf(prestamoSoli)},
            {"", String.valueOf(montoGir)},
            {"", String.valueOf(interes)},
            {"", String.valueOf(fondoInta)},
            {"", String.valueOf(prestamoSoli + interes + fondoInta)},
            {String.valueOf(descuentoMens), ""},
            {String.valueOf(meses), ""}
        };

        rowIndex = personalInfoRowStart + 2; // Comenzamos después de los textos
        for (String[] amount : amounts) {
            Row row = sheet.getRow(rowIndex++);
            createCell(row, 4, amount[0], borderedStyle); // Columna E
            createCell(row, 5, amount[1], borderedStyle); // Columna F
        }

        // Ajustar el tamaño de las columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);

        // Ajustar el ancho de las columnas B y D (150% del tamaño por defecto)
        sheet.setColumnWidth(1, sheet.getColumnWidth(1) * 3 / 2); // Columna B al 150%
        sheet.setColumnWidth(3, sheet.getColumnWidth(3) * 3 / 2); // Columna D al 150%

        // Ajustar el ancho de las columnas E y F para que se vean bien
        sheet.setColumnWidth(4, sheet.getColumnWidth(4) * 3 / 2);
        sheet.setColumnWidth(5, sheet.getColumnWidth(5) * 3 / 2);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo Excel");
        fileChooser.setSelectedFile(new File("Liquidacion_Prestamo_" + EmployeeDNI + ".xlsx")); // Nombre preseleccionado

        boolean guardadoExitoso = false;

        while (!guardadoExitoso) {
            int userSelection = fileChooser.showSaveDialog(null);

            // Si el usuario selecciona un archivo y hace clic en "Guardar"
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                    guardadoExitoso = true;

                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(fileToSave);
                    }
                    JOptionPane.showMessageDialog(null, "Archivo Excel generado exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    if (e.getMessage().contains("being used by another process")) {
                        JOptionPane.showMessageDialog(null, "El archivo está abierto o en uso. Por favor, cierre el archivo o elija otro nombre.", "Archivo en uso", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error al guardar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        break; // Salir del bucle en caso de un error grave
                    }
                }
            } else {
                // Si el usuario cancela el diálogo, salir del bucle
                break;
            }
        }

    }

    private static CellStyle createCellStyle(Workbook workbook, boolean bold, int fontSize, HorizontalAlignment alignment) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints((short) fontSize);
        style.setFont(font);
        style.setAlignment(alignment);
        return style;
    }

    private static CellStyle createBorderedCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex()); // Color negro
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }

    private static void createMergedCell(Sheet sheet, int rowStart, int rowEnd, int colStart, int colEnd,
            String value, CellStyle style) {
        Row row = sheet.getRow(rowStart) == null ? sheet.createRow(rowStart) : sheet.getRow(rowStart);
        Cell cell = row.createCell(colStart);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, colStart, colEnd));
    }

    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
