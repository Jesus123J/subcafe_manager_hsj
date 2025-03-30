package com.subcafae.finantialtracker.report.descuento;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    private String codigo;
    private String dni;
    private String nameWork;
    private String date;
    private String numberCuota;
    private String plazos;
    private String cuota;
    private String aporte;
    private String totalPrestar;
    private String numberOperacion;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNameWork() {
        return nameWork;
    }

    public void setNameWork(String nameWork) {
        this.nameWork = nameWork;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumberCuota() {
        return numberCuota;
    }

    public void setNumberCuota(String numberCuota) {
        this.numberCuota = numberCuota;
    }

    public String getPlazos() {
        return plazos;
    }

    public void setPlazos(String plazos) {
        this.plazos = plazos;
    }

    public String getCuota() {
        return cuota;
    }

    public void setCuota(String cuota) {
        this.cuota = cuota;
    }

    public String getAporte() {
        return aporte;
    }

    public void setAporte(String aporte) {
        this.aporte = aporte;
    }

    public String getTotalPrestar() {
        return totalPrestar;
    }

    public void setTotalPrestar(String totalPrestar) {
        this.totalPrestar = totalPrestar;
    }

    public String getNumberOperacion() {
        return numberOperacion;
    }

    public void setNumberOperacion(String numberOperacion) {
        this.numberOperacion = numberOperacion;
    }

    public void generateExcel(List<ExcelExporter> excelExporter) {


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

        for (int i = 0; i < excelExporter.size(); i++) {
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(excelExporter.get(i).getCodigo());
            row.createCell(1).setCellValue(excelExporter.get(i).getDni());
            row.createCell(2).setCellValue(excelExporter.get(i).getNameWork());
            row.createCell(3).setCellValue(excelExporter.get(i).getDate());
            row.createCell(4).setCellValue(excelExporter.get(i).getNumberCuota());
            row.createCell(5).setCellValue(excelExporter.get(i).getPlazos());
            row.createCell(6).setCellValue(excelExporter.get(i).getCuota());
            row.createCell(7).setCellValue(excelExporter.get(i).getAporte());
            row.createCell(8).setCellValue(excelExporter.get(i).getTotalPrestar());
            row.createCell(9).setCellValue(excelExporter.get(i).getNumberOperacion());

        }

//        for (int i = 0; i < data.length; i++) {
//            Row row = sheet.createRow(i + 1);
//
//            for (int j = 0; j < data[i].length; j++) {
//                Cell cell = row.createCell(j);
//
//                if (data[i][j] instanceof Number) {
//                    cell.setCellValue(((Number) data[i][j]).doubleValue());
//                } else {
//                    cell.setCellValue((String) data[i][j]);
//                }
//            }
//        }
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

    @Override
    public String toString() {
        return "ExcelExporter{" + "codigo=" + codigo + ", dni=" + dni + ", nameWork=" + nameWork + ", date=" + date + ", numberCuota=" + numberCuota + ", plazos=" + plazos + ", cuota=" + cuota + ", aporte=" + aporte + ", totalPrestar=" + totalPrestar + ", numberOperacion=" + numberOperacion + '}';
    }

}
