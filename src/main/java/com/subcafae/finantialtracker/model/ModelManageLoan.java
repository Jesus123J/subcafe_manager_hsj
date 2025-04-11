/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.Loan;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.report.loanReport.ExcelDemo;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

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

        this.componentManageLoan = componentManageLoan;

        TextFieldValidator.applyIntegerFilter(componentManageLoan.textSearchLoanSoli);
        TextFieldValidator.applyIntegerFilter(componentManageLoan.jTextFieldSearchLoanNum);
        TextFieldValidator.applyDecimalFilter(componentManageLoan.textAmountLoan);
        TextFieldValidator.applyDecimalFilter(componentManageLoan.textRefinanciamientoDemostration);
        TextFieldValidator.applyDecimalFilter(componentManageLoan.jTextFieldMontoDemostration);

        componentManageLoan.buttonCleanApplicant.setEnabled(false);
        componentManageLoan.buttonCleanAval.setEnabled(false);

        desingJDialog();

    }

    public void insertListEmployeeAvalComboBox() {
        try {
            listEmployeeAval = findAll();
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
            if (employeeTb.getNationalId().concat(" - " + employeeTb.getFullName()).toLowerCase().trim().replace(" - ", "").contains(textSearch.toLowerCase().trim())) {
                comboBox.addItem(employeeTb.getNationalId() + " - " + employeeTb.getFullName());
            }
        }
    }

    public void insertListEmployeeApplicantComboBox() {
        try {
            listEmployeeApplicant = findAll();
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
            LoanTb loan;
            loan = new LoanTb(
                    employeeApplicant.getNationalId(),
                    employeeAval != null ? employeeAval.getNationalId() : null,
                    Double.parseDouble("0.0"),
                    Double.parseDouble(componentManageLoan.textAmountLoan.getText()),
                    Integer.parseInt(selectedItem.toString()),
                    LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                    "Pendiente",
                    null,
                    1,
                    new Date(new java.util.Date().getTime()),
                    null,
                    null,
                    "Ordinario", "EMPLOYEE");

            Double refinan = createLoanWithStateValidation(loan);

            if (refinan == null) {
                return;
            }

            System.out.println("Meses dados -> " + loan.getDues());
            generateExcelLiquidación(componentManageLoan.textAmountLoan.getText(),
                    refinan.toString(), loan.getDues(), loan.getEmployeeId(), Boolean.FALSE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "GESTIÓN PRESTAMO", JOptionPane.ERROR_MESSAGE);
            System.out.println("Message -> " + ex.getMessage());
        }
    }

    protected Double fondoIntangibleTotal;
    protected Double fondoIntagle;
    protected Double capitalSinInteresMensual;
    protected Double capitalMensual;
    protected Double cuotaMensualConFondoIntangible;
    protected Double interesTotal;
    protected Double montoPrestadoChange;
    protected Double interesMensual;
    protected Double tasaNominalPeriodo;
    protected Double tasaNominalMensual = 0.016;

    protected void generateLoan(int dues, Double montoPrestado, Double refinanciadoText, Boolean demo) {

        int meses = dues;

        double montoPrestamo = montoPrestado;

        if (refinanciadoText > montoPrestado) {
            JOptionPane.showMessageDialog(null, "Error el refinanciamiento es mas grande", "Gestión Prestamos", JOptionPane.WARNING_MESSAGE);
            montoPrestadoChange = 0.0;
            return;
        }

        montoPrestadoChange = Math.abs(Double.parseDouble(String.format("%.2f", montoPrestamo - refinanciadoText)));

        tasaNominalPeriodo = Double.valueOf(String.format("%.2f", Math.pow(1 + tasaNominalMensual, meses) - 1));

        interesMensual = Double.valueOf(String.format("%.2f", (tasaNominalPeriodo * montoPrestamo) / meses));

        //Valor agregado '* meses'
        fondoIntangibleTotal = (Math.ceil(montoPrestamo / 500.0) * 0.50) * meses;

        // el interesTotal tiene que ir antes 
        interesTotal = interesMensual * meses;

        cuotaMensualConFondoIntangible = Double.valueOf(String.format("%.2f", (Double.valueOf(String.format("%.2f", interesTotal)) + fondoIntangibleTotal)));

        //El capital mensual tiene que calcularse despues de la cuota mensual para sumarse y que salga el valor dividiendo con los meses
        capitalMensual = Double.valueOf(String.format("%.2f", (Double.parseDouble(String.format("%.2f", cuotaMensualConFondoIntangible)) + montoPrestamo) / meses));

        capitalSinInteresMensual = Double.valueOf(String.format("%.2f", montoPrestamo / meses));

        if (demo) {
            insertDataDemo(montoPrestadoChange, montoPrestado, meses);
        }
        //txtMonthyDue
        //txtTotalPaymentDemo
    }

    public void insertDataDemo(Double montoPrestamo, Double monto, int meses) {
        componentManageLoan.jTextFieldCapitalMensual.setText(capitalSinInteresMensual.toString());
        componentManageLoan.jTextFieldInteresMensual.setText(interesMensual.toString());
        componentManageLoan.jTextFieldInteresTotal.setText(interesTotal.toString());
        componentManageLoan.jTextFieldTotalPagar.setText(String.format("%.2f", (capitalMensual * meses)));
        componentManageLoan.jTextFieldCuotaMensual.setText(capitalMensual.toString());
        componentManageLoan.jTextFieldMontoPrestar.setText(monto.toString());
        componentManageLoan.jTextFieldMontoGirar.setText(montoPrestadoChange.toString());
    }

    public void generateExcelLiquidación(String prestamo, String refinanaciamiento, Integer meses, String dni, Boolean demo) {
        if (refinanaciamiento == null) {
            refinanaciamiento = "0.0";
        }
        // TODO add your handling code here:
        if (prestamo.isBlank()
                || refinanaciamiento.isBlank()) {
            JOptionPane.showMessageDialog(null, "Rellena todas las casillas y presiona el botón 'Calcular Demo'");
            return;
        }
        try {
            // Llamar a la función de generación de demo de préstamo
            generateLoan(
                    meses,
                    Double.valueOf(String.format("%.2f", Double.valueOf(prestamo))),
                    Double.valueOf(String.format("%.2f", Double.valueOf(refinanaciamiento))), demo);

            //System.out.println("Ingresa");
            if (montoPrestadoChange == 0.0) {
                return;
            }

            System.out.println("No deve continuar");

            // Crear una instancia de ExcelDemo
            ExcelDemo excelD = new ExcelDemo();

//              excelD.excelDemo(
//                    Double.parseDouble(componentManageLoan.jTextFieldMontoPrestar.getText()),
//                    Double.parseDouble(componentManageLoan.jTextFieldMontoGirar.getText()),
//                    Double.parseDouble(componentManageLoan.jTextFieldInteresTotal.getText()),
//                    fondoIntangible,
//                    Double.parseDouble(componentManageLoan.jTextFieldCuotaMensual.getText()),
//                    Integer.parseInt(String.valueOf(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem())), null
//            );
            // Calcular fondo intangible
            //double fondoIntangible = (Math.ceil(Double.parseDouble(componentManageLoan.jTextFieldMontoDemostration.getText()) / 500.0) * 0.50) * Integer.parseInt(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem().toString());
            // Llamar al método excelDemo con los valores convertidos
            System.out.println("Fondo intangible -> " + fondoIntangibleTotal);
            System.out.print("refinancimientpo -> " + refinanaciamiento);

            excelD.excelDemo(
                    refinanaciamiento,
                    Double.parseDouble(String.format("%.2f", Double.parseDouble(prestamo))),
                    montoPrestadoChange,
                    interesTotal,
                    fondoIntangibleTotal,
                    capitalMensual,
                    meses, dni
            );
        } catch (NumberFormatException e) {
            // Mostrar un mensaje si alguna conversión falla
            JOptionPane.showMessageDialog(null, "Error: Asegúrate de que todos los valores sean numéricos.");
        }
    }

    public void registerAcept(LoanDetailsTb loanDetailsTb, LoanTb loan, int user) {
        try {
            System.out.println("Data -> " + loanDetailsTb.toString());
            System.out.println("Data loan -> " + loan.toString());
            insertMultipleLoanDetails(loanDetailsTb, loan, user);

            updateSoliNumStatus(loan.getSoliNum(), "Aceptado", user);
            JOptionPane.showMessageDialog(null, "Estado cambiado a: Aceptado");
        } catch (SQLException ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema");
        }
    }

    public void insertTablet(java.util.Date dateStart , java.util.Date dateFinaly) {
        List<Loan> list = getAllLoanss(new java.sql.Date(dateStart.getTime()), new java.sql.Date(dateFinaly.getTime()));
        DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
        model.setRowCount(0);
        for (Loan loan : list) {
            model.addRow(new Object[]{
                loan.getSoliNum(),
                loan.getSolicitorName(),
                loan.getGuarantorName(),
                loan.getRequestedAmount(),
                loan.getAmountWithdrawn(),
                loan.getState(),
                loan.getPaymentResponsibility()
            });
        }
    }

    public void desingJDialog() {
        componentManageLoan.jDialog1.setSize(980, 530);
        componentManageLoan.jDialog1.setResizable(false);
        componentManageLoan.jDialog1.setLocationRelativeTo(null);

    }
}
