/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.model.ModelManageWorker;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageWorker.jButtonRegisterWorker)) {
            insertEmployee();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(componentManageWorker.jTabbedPane)) {
            int index = componentManageWorker.jTabbedPane.getSelectedIndex();
            if (index == 0) {
                System.out.println("Info");
                componentManageWorker.dcBirthDate.setDate(new Date());
            }
        }

    }

}
