/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelMain;
import com.subcafae.finantialtracker.report.concept.PaymentVoucher;
import com.subcafae.finantialtracker.view.ViewMain;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerMain extends ModelMain implements ActionListener, MouseListener {

    public ControllerMain() {
        super(new ViewMain());

//        viewMain.jInternalFrame1.addInternalFrameListener(new InternalFrameAdapter() {
//            @Override
//            public void internalFrameClosed(InternalFrameEvent e) {
//                PaymentVoucher.cleanUnusedVouchers();
//            }
//        });
        //Report
        viewMain.jButtonSearchDocument.addActionListener(this);
        viewMain.jLabelConstanciaEntrega.addMouseListener(this);
        viewMain.jLabelHistoryPayment.addMouseListener(this);
        viewMain.jLabelReportDesc.addMouseListener(this);
        viewMain.jLabelReportDeuda.addMouseListener(this);
        viewMain.jMenuPago.addMouseListener(this);

        viewMain.jButtonProcesoDescuent.addActionListener(this);
        viewMain.jMenuMangeBond.addMouseListener(this);
        viewMain.jMenuMangeLoan.addMouseListener(this);
        viewMain.jMenuManageWorker.addMouseListener(this);
        viewMain.jMenuManageUser.addMouseListener(this);
        viewMain.jMenu2.addMouseListener(this);

        viewMain.jButton3.addActionListener(this);
        viewMain.jButton4.addActionListener(this);
        viewMain.jButton1.addActionListener(this);
        viewMain.jButton2.addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource().equals(viewMain.jButtonProcesoDescuent)) {
                procedRegistroDesc();
            }
            if (e.getSource().equals(viewMain.jButtonSearchDocument)) {

                procesarArchivo();

            }
            if (e.getSource().equals(viewMain.jButton2)) {
                if (!viewMain.jTextFieldNumeroVoucher.getText().isBlank()) {
                    PaymentVoucher.cleanUnusedVouchers();
                    viewMain.jTextFieldNumeroVoucher.setText("");
                    viewMain.jButton3.setEnabled(true);
                }
            }
            if (e.getSource().equals(viewMain.jButton1)) {
                if (validarCamposVoucher()) {
                    viewMain.jButton2.setEnabled(false);
                    viewMain.jButton3.setEnabled(true);
                    if (viewMain.jButton1.getText().equalsIgnoreCase("EDITAR")) {
                        String documentDni = viewMain.jComboBoxSearchClient.getSelectedItem().toString().split(" - ")[1];
                        String nameLastName = viewMain.jComboBoxSearchClient.getSelectedItem().toString().split(" - ")[0];

                        PaymentVoucher paymentVoucher = new PaymentVoucher(
                                viewMain.jTextFieldNumeroVoucher.getText(), viewMain.jTextFieldCuentaVoucher.getText(),
                                viewMain.jTextFieldChequeVoucher.getText(), viewMain.jLabel82.getText(),
                                Double.valueOf(viewMain.jTextFieldMountVoucher.getText()),
                                viewMain.jTextAreaDetalleVoucher.getText(), documentDni, nameLastName, viewMain.cbConRegDateStartVoucher.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                                usser.getId()
                        );

                        boolean satus = paymentVoucher.updateVoucher();
                        if (satus) {
                            JOptionPane.showMessageDialog(null, "Se actualizó el voucher");
                        }

                        paymentVoucher.imprintVoucher();

                        cleanVoucher();

                    } else {
                        String documentDni = viewMain.jComboBoxSearchClient.getSelectedItem().toString().split(" - ")[1];
                        String nameLastName = viewMain.jComboBoxSearchClient.getSelectedItem().toString().split(" - ")[0];

                        PaymentVoucher paymentVoucher = new PaymentVoucher(
                                viewMain.jTextFieldNumeroVoucher.getText(), viewMain.jTextFieldCuentaVoucher.getText(),
                                viewMain.jTextFieldChequeVoucher.getText(), viewMain.jLabel82.getText(),
                                Double.valueOf(viewMain.jTextFieldMountVoucher.getText()),
                                viewMain.jTextAreaDetalleVoucher.getText(), documentDni, nameLastName, viewMain.cbConRegDateStartVoucher.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                                usser.getId()
                        );

                        paymentVoucher.generateVoucher();

                        cleanVoucher();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "LLene todos los campos");
                }
            }
            if (e.getSource().equals(viewMain.jButton4)) {
                viewMain.jButton4.setEnabled(false);
                viewMain.jButton3.setEnabled(true);
                viewMain.jComboBox1.removeAllItems();
                cleanVoucher();
            }
            if (e.getSource().equals(viewMain.jButton3)) {
                viewMain.jButton1.setText("IMPRIMIR");
                cleanVoucher();
                viewMain.jButton3.setEnabled(false);
                viewMain.jButton2.setEnabled(true);
                viewMain.cbConRegDateStartVoucher.setDate(new Date());
                viewMain.jTextFieldNumeroVoucher.setText(PaymentVoucher.generateAndReserveVoucher());
            }
        } catch (Exception ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al procesar la acción", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            if (e.getSource().equals(viewMain.jMenu2)) {
                componentLogin.jPasswordPassword.setText("");
                componentLogin.jTextFieldUser.setText("");
                eliminarCOmponent();
                showLogin();
            }

            if (e.getSource().equals(viewMain.jMenuManageUser)) {
                if (usser.getRol().equalsIgnoreCase("ADMINISTRADOR")) {
                    centerInternalComponent(componentManageUser);
                }
                if (usser.getRol().equalsIgnoreCase("SUPER ADMINISTRADOR")) {
                    centerInternalComponent(componentManageUser);
                }
            }
            if (e.getSource().equals(viewMain.jMenuPago)) {
                viewMain.jLabelCode.setText("");
                viewMain.jLabelCantidad.setText("");
                model.setRowCount(0);
                modelFindNot.setRowCount(0);
                mapCom.clear();
                centerInternalComponent(viewMain.jInternalPagoPrestamosOtros);
            }
            if (e.getSource().equals(viewMain.jMenuMangeBond)) {
                centerInternalComponent(componentManageBond);
            }
            if (e.getSource().equals(viewMain.jMenuMangeLoan)) {
                centerInternalComponent(componentManageLoan);
            }
            if (e.getSource().equals(viewMain.jMenuManageWorker)) {
                centerInternalComponent(componentManageWorker);
            }
        } catch (Exception ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al abrir el componente", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Click");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        try {
            if (e.getSource().equals(viewMain.jLabelConstanciaEntrega)) {
                viewMain.cbConRegDateStartVoucher.setDate(new Date());
                centerInternalComponent(viewMain.jInternalFrame1);
            }

            if (e.getSource().equals(viewMain.jLabelHistoryPayment)) {
                historyPayment();
            }
            if (e.getSource().equals(viewMain.jLabelReportDesc)) {
                generateExcel();
            }
            if (e.getSource().equals(viewMain.jLabelReportDeuda)) {
                reportDeuda();
            }
        } catch (Exception ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al procesar la acción", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
