/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageBond;
import com.subcafae.finantialtracker.report.bond.ReporteAbono;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageBond extends ModelManageBond implements ActionListener, KeyListener, ChangeListener {

    public ControllerManageBond(ComponentManageBond componentManageBond, UserTb user) {
        super(componentManageBond, user);
        componentManageBond.jComboSearchWorker.getEditor().getEditorComponent().addKeyListener(this);
        componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent().addKeyListener(this);
//        componentManageBond.jButtonCheck.addActionListener(this);
//        componentManageBond.jButtonClean.addActionListener(this);
        componentManageBond.jButtonReportBond.addActionListener(this);
        componentManageBond.jButtonRegisterBond.addActionListener(this);
        componentManageBond.jButtonRegistroConcepto.addActionListener(this);
        componentManageBond.jTabbedPane1.addChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageBond.jButtonReportBond)) {
            if (componentManageBond.searchConcept.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el nombre del concepto", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            ReporteAbono reporteAbono = new ReporteAbono();
            reporteAbono.searchService(componentManageBond.searchConcept.getText());

        }
        if (e.getSource().equals(componentManageBond.jButtonRegistroConcepto)) {
            if (componentManageBond.jTextFieldDescription.getText().isBlank()
                    || componentManageBond.jTextFieldVenta.getText().isBlank()
                    || componentManageBond.jTextFieldPrecioCosto.getText().isBlank()
                    || componentManageBond.jTextFieldPrioridad.getText().isBlank()
                    || componentManageBond.jTextFieldUnidades.getText().isBlank()) {

                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }
            if (!componentManageBond.jRadioButtonPaga.isSelected() && !componentManageBond.jRadioButtonSeOmitePago.isSelected()) {
                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }

            insertServiceConepto();

        }

        if (e.getSource().equals(componentManageBond.jButtonRegisterBond)) {

            if ((employee == null && Concept == null)
                    || componentManageBond.jTextFieldDues.getText().isBlank()
                    || componentManageBond.jTextFieldMonthly.getText().isBlank()) {

                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }

            AbonoTb abono = new AbonoTb();

            abono.setDues(Integer.parseInt(componentManageBond.jTextFieldDues.getText()));
            abono.setEmployeeId(String.valueOf(employee.getEmployeeId()));
            abono.setCreatedAt(LocalDate.now().toString());
            abono.setCreatedBy(user.getId());
            abono.setDiscountFrom(componentManageBond.jComboDescont.getSelectedItem().toString());
            abono.setServiceConceptId(String.valueOf(Concept.getId()));
            abono.setMonthly(Double.valueOf(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldMonthly.getText()))));
            abono.setPaymentDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).toString());
            abono.setStatus("Pendiente");

            //liempiar los componentes de clean
            Concept = null;
            employee = null;

            ((JTextField) componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent()).setText("");
            ((JTextField) componentManageBond.jComboSearchWorker.getEditor().getEditorComponent()).setText("");
            componentManageBond.jLabelCodeWorker.setText("");
            componentManageBond.jLabelShowDNIWorker.setText("");
            componentManageBond.jLabelShowDNIWorker1.setText("");
            componentManageBond.jTextFieldDues.setText("");
            componentManageBond.jTextFieldMonthly.setText("");

            insertDao(abono);

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getSource().equals(componentManageBond.jComboSearchWorker.getEditor().getEditorComponent())) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (componentManageBond.jComboSearchWorker.getSelectedIndex() != -1) {
                    try {
                        employee = listEmployee.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageBond.jComboSearchWorker.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();
                    } catch (Exception eee) {
                        employee = null;
                    }
                    if (employee == null) {
                        return;
                    }
                    componentManageBond.jLabelCodeWorker.setText(employee.getEmploymentStatusCode());
                    componentManageBond.jLabelShowDNIWorker.setText(employee.getNationalId());

                }
            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {
                insertListCombo(componentManageBond.jComboSearchWorker);
            }

        }

        if (e.getSource().equals(componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent())) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (componentManageBond.jComboBoxSearchConceptBond.getSelectedIndex() != -1) {
                    try {
                        String[] cadena = componentManageBond.jComboBoxSearchConceptBond.getSelectedItem().toString().split(" - ");
                        Concept = listConcept.stream().filter(predicate -> predicate.getId() == Integer.parseInt(cadena[0]) && predicate.getDescription().equalsIgnoreCase(cadena[1])).findFirst().get();
                    } catch (Exception eee) {
                        Concept = null;
                    }
                    if (Concept == null) {
                        return;
                    }
                    componentManageBond.jLabelShowDNIWorker1.setText(Concept.getId() + " - " + Concept.getDescription());
                }

            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {
                insertListCombo(componentManageBond.jComboBoxSearchConceptBond);
            }

        }

        // 
        // if (e.getSource().equals(componentManageBond.)) {
        // }
    }

    @Override
    public void keyReleased(KeyEvent e
    ) {
    }

    @Override
    public void stateChanged(ChangeEvent e
    ) {

        switch (componentManageBond.jTabbedPane1.getSelectedIndex()) {
            case 0:

                break;
            case 1:
                insertListTableConcept();
                break;
        }

    }

}
