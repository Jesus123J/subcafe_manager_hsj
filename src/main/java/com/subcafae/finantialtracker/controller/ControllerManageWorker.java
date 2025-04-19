/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageWorker;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageWorker extends ModelManageWorker implements ActionListener, ChangeListener, ListSelectionListener {

    public ControllerManageWorker(ComponentManageWorker componentManageWorker, UserTb user) {
        super(componentManageWorker, user);
        componentManageWorker.jButtonRegisterWorker.addActionListener(this);
        componentManageWorker.jTabbedPane.addChangeListener(this);
        componentManageWorker.jButtonEliminarPerson.addActionListener(this);
        componentManageWorker.jButtonBuscarforDate.addActionListener(this);
        componentManageWorker.jButtoBuscarDni.addActionListener(this);
        componentManageWorker.jTableListEmployee.getSelectionModel().addListSelectionListener(this);
        //tableList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageWorker.jButtoBuscarDni)) {
            if (componentManageWorker.jTextFieldBuscarForDni.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene el campo con un dni", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                Optional<EmployeeTb> emple = new EmployeeDao().findById(componentManageWorker.jTextFieldBuscarForDni.getText());
                if (!emple.isPresent()) {
                    JOptionPane.showMessageDialog(null, "No se encontró el empleado con el DNI proporcionado.", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    DefaultTableModel model = (DefaultTableModel) componentManageWorker.jTableListEmployee.getModel();
                    model.setRowCount(0);
                    model.addRow(new Object[]{
                        emple.get().getFullName(),
                        emple.get().getNationalId(),
                        emple.get().getEmploymentStatus()
                    });
                }
            } catch (SQLException ex) {

                System.out.println("ERROR -> " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (e.getSource().equals(componentManageWorker.jButtonBuscarforDate)) {
            try {

                if (componentManageWorker.combStart.getDate() != null || componentManageWorker.comboFinaly.getDate() != null) {

                    tableList(new java.sql.Date(componentManageWorker.combStart.getDate().getTime()), new java.sql.Date(componentManageWorker.comboFinaly.getDate().getTime()));

                } else {
                    JOptionPane.showMessageDialog(null, "Rellene el campo con la fecha inicial y la fecha final", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ee) {
                JOptionPane.showMessageDialog(null, "Rellene las fechas para mostrar la lista");
                return;
            }
        }
        
        if (e.getSource().equals(componentManageWorker.jButtonEliminarPerson)) {
            if (componentManageWorker.textFieldNameDeleteUser.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene el campo con un dni", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                new EmployeeDao().deleteEmployeeIfNotUsed(componentManageWorker.textFieldNameDeleteUser.getText());
                // tableList();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);

                //Logger.getLogger(ControllerManageWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (e.getSource().equals(componentManageWorker.jButtonRegisterWorker)) {
            insertEmployee();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(componentManageWorker.jTabbedPane)) {
            int index = componentManageWorker.jTabbedPane.getSelectedIndex();
            if (index == 0) {
                //tableList();
                componentManageWorker.dcBirthDate.setDate(new Date());
            }
        }

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            if (e.getSource().equals(componentManageWorker.jTableListEmployee.getSelectionModel())) {
                int index = componentManageWorker.jTableListEmployee.getSelectedRow();
                if (index != -1) {

                    componentManageWorker.jTableListEmployee.repaint();

                    String dni = componentManageWorker.jTableListEmployee.getValueAt(index, 1).toString();
                    // Crear un ComboBox con las opciones
                    JComboBox<String> comboBox = new JComboBox<>(new String[]{"CAS", "NOMBRADO"});

                    // Mostrar un JOptionPane con el ComboBox
                    int option = JOptionPane.showConfirmDialog(
                            null,
                            comboBox,
                            "CAMBIAR TIPO DE CONTRATO",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if (option == JOptionPane.OK_OPTION) {
                        String newStatus = (String) comboBox.getSelectedItem();

                        new EmployeeDao().updateEmploymentStatusByDNI(dni, newStatus);
                        JOptionPane.showMessageDialog(null, "Se cambio el estado ");

                    }

                }
            }

        }
    }
}
