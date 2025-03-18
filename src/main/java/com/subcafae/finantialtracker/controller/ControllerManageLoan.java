/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.model.ModelManageLoan;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageLoan extends ModelManageLoan implements ActionListener, KeyListener {

    public ControllerManageLoan(ComponentManageLoan componentManageLoan) {
        super(componentManageLoan);
        ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).addKeyListener(this);
        ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).addKeyListener(this);
        componentManageLoan.buttonRegisterLoan.addActionListener(this);
        componentManageLoan.buttonCleanApplicant.addActionListener(this);
        componentManageLoan.buttonCleanAval.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageLoan.buttonCleanApplicant)) {
            employeeApplicant = new EmployeeTb();
            componentManageLoan.comboBoxApplicant.setEnabled(true);
            componentManageLoan.buttonCleanApplicant.setEnabled(false);
            componentManageLoan.buttonConfirmApplicant.setEnabled(true);
        }
        if (e.getSource().equals(componentManageLoan.buttonCleanAval)) {
            employeeAval = new EmployeeTb();
            componentManageLoan.comboBoxAval.setEnabled(true);
            componentManageLoan.buttonCleanAval.setEnabled(false);
            componentManageLoan.buttonConfirmAval.setEnabled(true);
        }
        if (e.getSource().equals(componentManageLoan.buttonRegisterLoan)) {

            if (componentManageLoan.comboBoxApplicant.getSelectedIndex() == -1
                    || componentManageLoan.comboBoxAval.getSelectedIndex() == -1
                    || componentManageLoan.textAmountLoan.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene los campos para poder registrar", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return;

            }
            insertDataLoan(employeeApplicant, employeeAval, componentManageLoan.textAmountLoan, componentManageLoan.jComboBoxCuotas.getSelectedItem());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource().equals(((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()))) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                System.out.println("Applicant se preciono enter");
                System.out.println("index " + componentManageLoan.comboBoxApplicant.getSelectedIndex());
                if (componentManageLoan.comboBoxApplicant.getSelectedIndex() != -1) {

                    System.out.println("Cantidad -> applic " + listEmployeeApplicant.size());

                    try {
                        employeeApplicant = listEmployeeApplicant.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();
                    } catch (Exception eee) {
                    }

                    System.out.println("Class applicant -> " + employeeApplicant.toString());
                    
                    //((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).setText(componentManageLoan.comboBoxApplicant.getSelectedItem().toString());
                    try {
                        if (employeeAval.getNationalId().equalsIgnoreCase(employeeApplicant.getNationalId())) {
                            JOptionPane.showMessageDialog(null, "No repita el mismo trabajador en el Aval", "GESTIÓN PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    } catch (Exception ee) {
                        System.out.println("Message -> " + ee.getMessage());
                        System.out.println("Error");
                    }

                    componentManageLoan.comboBoxApplicant.setEnabled(false);
                    componentManageLoan.buttonCleanApplicant.setEnabled(true);
                    componentManageLoan.buttonConfirmApplicant.setEnabled(false);
                }
            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z) // Letras
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) // Números
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE // Borrar
                    || e.getKeyCode() == KeyEvent.VK_SPACE) { //
                insertListEmployeeApplicantComboBox();
            }
        }
        if (e.getSource().equals(((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()))) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                System.out.println("Aval se preciono enter");
                System.out.println("index " + componentManageLoan.comboBoxAval.getSelectedIndex());
                if (componentManageLoan.comboBoxAval.getSelectedIndex() != -1) {
                    System.out.println("Se inserta aval");

                    System.out.println("Cantidad -> applic " + listEmployeeAval.size());

                    try {
                        employeeAval = listEmployeeAval.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();

                    } catch (Exception ee) {
                        System.out.println("Message -> " + ee.getMessage());
                    }

                    System.out.println("Class aval -> " + employeeAval.toString());
                    try {
                        if (employeeAval.getNationalId().equalsIgnoreCase(employeeApplicant.getNationalId())) {
                            JOptionPane.showMessageDialog(null, "No repita el mismo trabajador en el Aval", "GESTIÓN PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    } catch (Exception ee) {
                        System.out.println("Message -> " + ee.getMessage());
                        System.out.println("Error");

                    }

                    //    ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).setText(componentManageLoan.comboBoxAval.getSelectedItem().toString());
                    componentManageLoan.comboBoxAval.setEnabled(false);
                    componentManageLoan.buttonCleanAval.setEnabled(true);
                    componentManageLoan.buttonConfirmAval.setEnabled(false);
                }
            }
            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z) // Letras
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) // Números
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE // Borrar
                    || e.getKeyCode() == KeyEvent.VK_SPACE) { //
                insertListEmployeeAvalComboBox();

            }

        }

    }

}
