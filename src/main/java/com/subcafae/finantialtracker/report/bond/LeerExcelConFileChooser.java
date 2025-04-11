/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.bond;

/**
 *
 * @author Jesus Gutierrez
 */
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.User;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.view.ViewMain;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeerExcelConFileChooser {

    public void method(UserTb user, ViewMain viewMain) {
        // Crear un JFileChooser para seleccionar el archivo
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo Excel");
        int seleccion = fileChooser.showOpenDialog(null);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            List<RegistroExcel> registros = leerExcel(archivoSeleccionado);

            // Si la lista está vacía, significa que hubo errores
            if (registros.isEmpty()) {
                JOptionPane.showMessageDialog(null, "❌ ERROR: El documento tiene datos incorrectos. No se guardó nada.");
            } else {

                try {

                    JComboBox<String> combo = new JComboBox<String>();

                    List<ServiceConceptTb> list = new ServiceConceptDao().getAllServiceConcepts();

                    for (ServiceConceptTb serviceConceptTb : list) {
                        combo.addItem(serviceConceptTb.getCodigo() + " - " + serviceConceptTb.getDescription());
                    }

                    int option = JOptionPane.showConfirmDialog(null, combo, "SELECCIÓN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (option == JOptionPane.OK_OPTION) {
                        viewMain.setEnabled(false);
                        ViewMain.loading.setVisible(true);
                        new Thread(() -> {
                            try {
                                List<EmployeeTb> emple = new EmployeeDao().findAll();
                                for (RegistroExcel registro : registros) {

                                    Optional<EmployeeTb> data = emple.stream().filter(predicate
                                            -> predicate.getNationalId().equalsIgnoreCase(registro.getDni())).findFirst();
                                    System.out.println("Data -> " + data);
                                    if (!data.isPresent()) {
                                        System.out.println("No se encontro usuario");
                                        EmployeeTb employee = new EmployeeTb();
                                        employee.setNationalId(registro.getDni());
                                        employee.setFullName(registro.getDatos());
                                        employee.setEmploymentStatusCode("2028");
                                        employee.setEmploymentStatus("CAS");
                                        employee.setStartDate(LocalDate.now());
                                        new EmployeeDao().create(employee);
                                        emple = new EmployeeDao().findAll();
                                    }
                                    AbonoTb abono = new AbonoTb();

                                    abono.setDues(registro.getCuotas());
                                    abono.setEmployeeId(String.valueOf(emple.stream().filter(predicate
                                            -> predicate.getNationalId().equalsIgnoreCase(registro.getDni())).findFirst().get().getEmployeeId()));

                                    abono.setCreatedAt(LocalDate.now().toString());
                                    abono.setCreatedBy(user.getId());
                                    abono.setDiscountFrom("BOLETA DE HABERES");
                                    abono.setServiceConceptId(String.valueOf(list.get(combo.getSelectedIndex()).getId()));

                                    abono.setMonthly(registro.getMonto());
                                    abono.setPaymentDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).toString());
                                    abono.setStatus("Pendiente");

                                    Integer dataa = new AbonoDao().insertAbono(abono);
                                    if (dataa != null || dataa != -1) {
                                        abono.setId(dataa);
                                        new AbonoDetailsDao().insertAbonoDetail(abono, user.getId());
                                    }

                                }
                            } catch (SQLException ex) {
                                viewMain.setEnabled(true);
                                ViewMain.loading.dispose();
                                System.out.println("Error -> " + ex.getMessage());
                                JOptionPane.showMessageDialog(null, "Error");
                            }
                            viewMain.setEnabled(true);
                            ViewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "✅ Registros cargados correctamente:");

                        }).start();

                    }
                } catch (SQLException ex) {
                    ViewMain.loading.dispose();
                    System.out.println("Error -> " + ex.getMessage());
                    JOptionPane.showMessageDialog(null, "Error");
                }
            }
        } else {

            JOptionPane.showMessageDialog(null, "No se seleccionó ningún archivo.");
        }
    }

    private static List<RegistroExcel> leerExcel(File archivo) {
        List<RegistroExcel> listaRegistros = new ArrayList<>();
        boolean errorDetectado = false;

        try (FileInputStream file = new FileInputStream(archivo); Workbook workbook = new XSSFWorkbook(file)) {

            Sheet hoja = workbook.getSheetAt(0); // Primera hoja

            // Iterar sobre las filas desde la fila 4 (índice 3)
            for (int i = 3; i < hoja.getPhysicalNumberOfRows(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    String dni = obtenerValorCelda(fila.getCell(0));
                    String datos = obtenerValorCelda(fila.getCell(1));
                    double monto = convertirADouble(fila.getCell(2));
                    int cuotas = (int) convertirADouble(fila.getCell(3));

                    // Validaciones: Si algún valor es incorrecto, activamos la bandera de error
                    if (dni.isEmpty() || datos.isEmpty() || monto <= 0 || cuotas <= 0) {
                        JOptionPane.showMessageDialog(null, "❌ ERROR en fila " + (i + 1) + ": Datos inválidos detectados.", "GESTIÓN DE ABONOS", JOptionPane.INFORMATION_MESSAGE);
                        errorDetectado = true;
                        break; // Salimos del bucle para evitar guardar datos incorrectos
                    }

                    // Agregar a la lista solo si no hubo errores
                    listaRegistros.add(new RegistroExcel(dni, datos, monto, cuotas));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Si detectamos errores, limpiamos la lista para no almacenar datos incorrectos
        if (errorDetectado) {
            listaRegistros.clear();
        }

        return listaRegistros;
    }

    private static String obtenerValorCelda(Cell celda) {
        if (celda == null) {
            return "";
        }

        switch (celda.getCellType()) {
            case STRING:
                return celda.getStringCellValue().trim();
            case NUMERIC:
                if (celda.getColumnIndex() == 0) { // Si es la columna de DNI, convertir a String sin decimales
                    return String.valueOf((long) celda.getNumericCellValue());
                }
                return String.valueOf(celda.getNumericCellValue());
            default:
                return "";
        }
    }

    private static double convertirADouble(Cell celda) {
        if (celda == null) {
            return 0.0;
        }

        switch (celda.getCellType()) {
            case NUMERIC:
                return celda.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(celda.getStringCellValue().trim()); // Convierte texto a número si es posible
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "❌ ERROR: '" + celda.getStringCellValue() + "' no es un número válido.");
                    return 0.0; // Valor por defecto si la conversión falla
                }
            default:
                return 0.0;
        }
    }
}
