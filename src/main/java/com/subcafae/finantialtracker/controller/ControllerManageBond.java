/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageBond;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDate;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageBond extends ModelManageBond implements ActionListener, KeyListener {

    public ControllerManageBond(ComponentManageBond componentManageBond, UserTb user) {
        super(componentManageBond, user);
        componentManageBond.jComboSearchWorker.getEditor().getEditorComponent().addKeyListener(this);
        componentManageBond.jButtonCheck.addActionListener(this);
        componentManageBond.jButtonClean.addActionListener(this);
        componentManageBond.jButtonRegisterBond.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        if (e.getSource().equals(componentManageBond.)) {
//            
//        }

        if (e.getSource().equals(componentManageBond.jButtonClean)) {

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource().equals(componentManageBond.jComboSearchWorker.getEditor().getEditorComponent())) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                AbonoTb abono = new AbonoTb();
                abono.setServiceConceptId("");
                abono.setDues(Integer.parseInt(componentManageBond.jTextFieldDues.getText()));
                abono.setMonthly(Double.valueOf(componentManageBond.jTextFieldMonthly.getText()));
                abono.setDiscountFrom(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).toString());
                abono.setCreatedAt(LocalDate.now().toString());
                abono.setCreatedBy(user.getId());
                abono.setModifiedBy(user.getId());
                abono.setModifiedAt(LocalDate.now().toString());
                insertDao(abono);
            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {

            }

        }
        
        // 
        
       // if (e.getSource().equals(componentManageBond.)) {
            
       // }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
