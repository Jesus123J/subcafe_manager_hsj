/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.model.ModelManageWorker;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageWorker extends ModelManageWorker implements ActionListener, ChangeListener {

    public ControllerManageWorker(ComponentManageWorker componentManageWorker) {
        super(componentManageWorker);
        componentManageWorker.jButtonRegisterWorker.addActionListener(this);
        componentManageWorker.jTabbedPane.addChangeListener(this);
        componentManageWorker.jButtonEliminarPerson.addActionListener(this);
        tableList();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageWorker.jButtonEliminarPerson)) {
            if (componentManageWorker.textFieldNameDeleteUser.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene el campo con un dni", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                new EmployeeDao().deleteEmployeeIfNotUsed(componentManageWorker.textFieldNameDeleteUser.getText());
                tableList();
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
                tableList();
                componentManageWorker.dcBirthDate.setDate(new Date());
            }
        }

    }

}
