/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
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
        super(new Conexion());
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
            
            
            
            
            //insert(employeeAval, null, "Pendiente", Double.parseDouble(textAmountLoan.getText()), 0, Integer.parseInt(selectedItem.toString()), 0);
      
        } catch (Exception ex) {
            System.out.println("Message -> " + ex.getMessage());
        }
    }
}
