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
        componentManageWorker.jButtonCancelarEdicion.addActionListener(this);
        componentManageWorker.jTableListEmployee.getSelectionModel().addListSelectionListener(this);
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
                if (componentManageWorker.combStart.getDate() != null && componentManageWorker.comboFinaly.getDate() != null) {
                    // Si hay fechas seleccionadas, filtrar por rango
                    tableList(new java.sql.Date(componentManageWorker.combStart.getDate().getTime()), new java.sql.Date(componentManageWorker.comboFinaly.getDate().getTime()));
                } else {
                    // Si no hay fechas, mostrar los últimos 100 registros
                    tableListLast(100);
                }
            } catch (Exception ee) {
                // En caso de error, mostrar los últimos 100 registros
                tableListLast(100);
            }
        }
        
        if (e.getSource().equals(componentManageWorker.jButtonEliminarPerson)) {
            if (componentManageWorker.textFieldNameDeleteUser.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene el campo con un dni", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirmación antes de eliminar
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro que desea eliminar al empleado con DNI: " + componentManageWorker.textFieldNameDeleteUser.getText() + "?",
                "CONFIRMAR ELIMINACIÓN",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                boolean deleted = new EmployeeDao().deleteEmployeeIfNotUsed(componentManageWorker.textFieldNameDeleteUser.getText());
                if (deleted) {
                    JOptionPane.showMessageDialog(null, "Empleado eliminado correctamente", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
                    componentManageWorker.textFieldNameDeleteUser.setText("");
                    loadEmployeeList(); // Recargar lista de autocompletado
                    tableListLast(100); // Refrescar tabla
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
            }
        }

        if (e.getSource().equals(componentManageWorker.jButtonRegisterWorker)) {
            insertEmployee();
        }

        if (e.getSource().equals(componentManageWorker.jButtonCancelarEdicion)) {
            exitEditMode();
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
        if (!e.getValueIsAdjusting() && !isEditMode) { // No preguntar si ya está en modo edición
            if (e.getSource().equals(componentManageWorker.jTableListEmployee.getSelectionModel())) {
                int index = componentManageWorker.jTableListEmployee.getSelectedRow();
                if (index != -1) {
                    String fullName = componentManageWorker.jTableListEmployee.getValueAt(index, 0).toString();
                    String dni = componentManageWorker.jTableListEmployee.getValueAt(index, 1).toString();
                    String status = componentManageWorker.jTableListEmployee.getValueAt(index, 2).toString();

                    // Preguntar si desea editar el empleado
                    int option = JOptionPane.showConfirmDialog(
                            null,
                            "¿Desea editar los datos del empleado: " + fullName + "?",
                            "EDITAR TRABAJADOR",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        try {
                            // Obtener datos completos del empleado
                            Optional<EmployeeTb> empleado = new EmployeeDao().findById(dni);
                            if (empleado.isPresent()) {
                                EmployeeTb emp = empleado.get();
                                java.util.Date startDate = java.sql.Date.valueOf(emp.getStartDate());
                                enterEditMode(dni, fullName, emp.getGender(), status, startDate, index);
                            } else {
                                // Si no se encuentra, usar datos de la tabla
                                enterEditMode(dni, fullName, null, status, new Date(), index);
                            }
                        } catch (SQLException ex) {
                            System.out.println("Error -> " + ex.getMessage());
                            // Usar datos de la tabla si hay error
                            enterEditMode(dni, fullName, null, status, new Date(), index);
                        }
                    } else {
                        // Limpiar selección si no quiere editar
                        componentManageWorker.jTableListEmployee.clearSelection();
                    }
                }
            }
        }
    }
}
