/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.model.ModelMain;
import com.subcafae.finantialtracker.view.ViewMain;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerMain extends ModelMain implements ActionListener, MouseListener {

    public ControllerMain() {
        super(new ViewMain());
        viewMain.jMenuMangeBond.addMouseListener(this);
        viewMain.jMenuMangeLoan.addMouseListener(this);
        viewMain.jMenuManageWorker.addMouseListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource().equals(viewMain.jMenuMangeBond)) {
            centerInternalComponent(componentManageBond);
        }
        if (e.getSource().equals(viewMain.jMenuMangeLoan)) {
            centerInternalComponent(componentManageLoan);
        }
        if (e.getSource().equals(viewMain.jMenuManageWorker)) {
            centerInternalComponent(componentManageWorker);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Click");
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
