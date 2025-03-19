/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.report.loanReport.ExcelDemo;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageLoan extends LoanDao {

    public ComponentManageLoan componentManageLoan;
    protected List<EmployeeTb> listEmployeeApplicant;
    protected List<EmployeeTb> listEmployeeAval;
    protected EmployeeTb employeeApplicant;
    protected EmployeeTb employeeAval;

    public ModelManageLoan(ComponentManageLoan componentManageLoan) {
        super(Conexion.getConnection());
        this.componentManageLoan = componentManageLoan;
        TextFieldValidator.applyDecimalFilter(componentManageLoan.textAmountLoan);
        componentManageLoan.buttonCleanApplicant.setEnabled(false);
        componentManageLoan.buttonCleanAval.setEnabled(false);

    }

    public void insertListEmployeeAvalComboBox() {
        try {
            listEmployeeAval = new EmployeeDao(Conexion.getConnection()).findAll();
            String textSearch = ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).getText();
            insertListEmployeeCombo(listEmployeeAval, componentManageLoan.comboBoxAval, textSearch);
            ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).setText(textSearch);
            componentManageLoan.comboBoxAval.showPopup();
        } catch (SQLException ex) {
            System.out.println("Message" + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema ", "Gestion de Prestamo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void insertListEmployeeCombo(List<EmployeeTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (EmployeeTb employeeTb : employeeTbs) {
            if (employeeTb.getNationalId().concat(" - " + employeeTb.getFirstName().concat(" " + employeeTb.getLastName())).toLowerCase().trim().replace(" - ", "").contains(textSearch.toLowerCase().trim())) {
                comboBox.addItem(employeeTb.getNationalId() + " - " + employeeTb.getFirstName().concat(" " + employeeTb.getLastName()));
            }
        }
    }

    public void insertListEmployeeApplicantComboBox() {
        try {
            listEmployeeApplicant = new EmployeeDao(Conexion.getConnection()).findAll();
            String textSearch = ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).getText();
            insertListEmployeeCombo(listEmployeeApplicant, componentManageLoan.comboBoxApplicant, textSearch);
            ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).setText(textSearch);
            componentManageLoan.comboBoxApplicant.showPopup();

        } catch (SQLException ex) {
            System.out.println("Message" + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema ", "Gestion de Prestamo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void insertDataLoan(EmployeeTb employeeApplicant, EmployeeTb employeeAval, JTextField textAmountLoan, Object selectedItem) {
        try {

            LoanTb loan = new LoanTb(
                    employeeApplicant.getNationalId(),
                    employeeAval.getNationalId(),
                    Double.parseDouble("0.0"),
                    Double.parseDouble(String.format("%.2f", textAmountLoan)),
                    Integer.parseInt(selectedItem.toString()),
                    LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                    LoanTb.LoanState.Pendiente,
                    null,
                    1,
                    LocalDateTime.now(),
                    null,
                    null,
                    "Ordinario");

            insert(loan);

        } catch (Exception ex) {
            System.out.println("Message -> " + ex.getMessage());
        }
    }

    protected void generateLoanDemo() {

        int meses = Integer.parseInt(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem().toString());

        double montoPrestamo = Double.parseDouble(componentManageLoan.jTextFieldMontoDemostration.getText());

        double refinanciado = componentManageLoan.textRefinanciamientoDemostration.getText() != null && !componentManageLoan.textRefinanciamientoDemostration.getText().isEmpty() ? Double.parseDouble(componentManageLoan.textRefinanciamientoDemostration.getText()) : 0;

        double tasaNominalMensual = 0.016;

        double tasaNominalPeriodo = Math.pow(1 + tasaNominalMensual, meses) - 1;

        double interesMensual = (tasaNominalPeriodo * montoPrestamo) / meses;

        double montoPrestadoChange = montoPrestamo - refinanciado;
        //Valor agregado '* meses'
        double fondoIntangibleMensual = (Math.ceil(montoPrestamo / 500.0) * 0.50) * meses;

        //el interesTotal tiene que ir antes 
        double interesTotal = interesMensual * meses;

        double cuotaMensualConFondoIntangible = (Double.parseDouble(String.format("%.2f", interesTotal)) + fondoIntangibleMensual);

        //El capital mensual tiene que calcularse despues de la cuota mensual para sumarse y que salga el valor dividiendo con los meses
        double capitalMensual = (Double.parseDouble(String.format("%.2f", cuotaMensualConFondoIntangible)) + montoPrestamo) / meses;
        double capitalSinInteresMensual = montoPrestamo / meses;

        //txtMonthyDue
        //txtTotalPaymentDemo
        componentManageLoan.jTextFieldCapitalMensual.setText(String.format("%.2f", capitalSinInteresMensual));
        componentManageLoan.jTextFieldInteresMensual.setText(String.format("%.2f", interesMensual));
        componentManageLoan.jTextFieldInteresTotal.setText(String.format("%.2f", interesTotal));
        componentManageLoan.jTextFieldTotalPagar.setText("" + (Double.parseDouble(String.format("%.2f", cuotaMensualConFondoIntangible)) + montoPrestamo));
        componentManageLoan.jTextFieldMontoDemostration.setText(String.format("%.2f", montoPrestamo));
        componentManageLoan.jTextFieldCuotaMensual.setText(String.format("%.2f", capitalMensual));
        componentManageLoan.jTextFieldMontoPrestar.setText(String.format("%.2f", montoPrestadoChange));
        componentManageLoan.jTextFieldMontoGirar.setText(String.format("%.2f", montoPrestadoChange));
    }

    public void generateExcelLiquidación() {
        // TODO add your handling code here:
        if (componentManageLoan.jTextFieldMontoDemostration.getText().isBlank() || componentManageLoan.textRefinanciamientoDemostration.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Rellena todas las casillas y presiona el botón 'Calcular Demo'");
            return;
        }
        try {
            // Llamar a la función de generación de demo de préstamo
            generateLoanDemo();

            // Crear una instancia de ExcelDemo
            ExcelDemo excelD = new ExcelDemo();

            // Calcular fondo intangible
            double fondoIntangible = (Math.ceil(Double.parseDouble(componentManageLoan.jTextFieldMontoDemostration.getText()) / 500.0) * 0.50) * Integer.parseInt(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem().toString());

            // Llamar al método excelDemo con los valores convertidos
            excelD.excelDemo(
                    Double.parseDouble(componentManageLoan.jTextFieldMontoPrestar.getText()),
                    Double.parseDouble(componentManageLoan.jTextFieldMontoGirar.getText()),
                    Double.parseDouble(componentManageLoan.jTextFieldInteresTotal.getText()),
                    fondoIntangible,
                    Double.parseDouble(componentManageLoan.jTextFieldCuotaMensual.getText()),
                    Integer.parseInt(String.valueOf(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem())), null
            );
            JOptionPane.showMessageDialog(null, "Creado exitosamente.");
        } catch (NumberFormatException e) {
            // Mostrar un mensaje si alguna conversión falla
            JOptionPane.showMessageDialog(null, "Error: Asegúrate de que todos los valores sean numéricos.");
        }
    }
}
