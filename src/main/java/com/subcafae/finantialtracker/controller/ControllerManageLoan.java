/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageLoan;
import com.subcafae.finantialtracker.report.loanReport.SolicitudPrestamo;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageLoan extends ModelManageLoan implements ActionListener, KeyListener, ChangeListener, ListSelectionListener {

    public ControllerManageLoan(ComponentManageLoan componentManageLoan, UserTb user) {
        super(componentManageLoan);
        ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).addKeyListener(this);
        ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).addKeyListener(this);
        componentManageLoan.buttonRegisterLoan.addActionListener(this);
        componentManageLoan.buttonCleanApplicant.addActionListener(this);
        componentManageLoan.buttonCleanAval.addActionListener(this);
        componentManageLoan.jTabbedPane1.addChangeListener(this);
        componentManageLoan.jButtonCalcularDemo.addActionListener(this);
        componentManageLoan.jButtonReportLiquidation.addActionListener(this);
        componentManageLoan.jTableLoanList.getSelectionModel().addListSelectionListener(this);
        componentManageLoan.jButtonReporteCompromisoAval.addActionListener(this);
        componentManageLoan.jButtonReporteCompromisoPago.addActionListener(this);
        componentManageLoan.jButtonSolicitudLoan.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(componentManageLoan.jButtonSolicitudLoan)) {

            JOptionPane optionPane = new JOptionPane("Descargando...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

            JDialog dialog = optionPane.createDialog(null, "Información");

            new Thread(() -> {
                try {

                    Optional<LoanTb> loanSearch;
                    try {
                        loanSearch = findLoan(componentManageLoan.textSearchLoanSoli.getText());
                    } catch (SQLException ex) {
                        System.out.println("Error /| " + ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Ocurrio un problema");
                        return;
                    }

                    if (!loanSearch.isPresent()) {
                        JOptionPane.showMessageDialog(null, "No se encontró número de solicitud");
                        return;
                    }

                    dialog.setModal(false);
                    dialog.setVisible(true);

                    // Número de solicitud detalle
                    // Nombre del empleado y DNI del empleado
                    Optional<EmployeeTb> employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());

                    Optional<EmployeeTb> employeeA = null;
                    try {
                        employeeA = new EmployeeDao().findById(loanSearch.get().getGuarantorIds());

                    } catch (Exception eee) {

                    }

                    String avalName = ".....................................................................";
                    String avalDni = "...............";
                    String avalService = "...............";
                    SolicitudPrestamo solipres = new SolicitudPrestamo();

                    String principalName = employeeR.get().getFirstName().concat(" " + employeeR.get().getLastName());
                    String principalDni = employeeR.get().getNationalId();
                    String principalService = "Ordinario";

                    // Si el aval no es "00000000", buscamos los datos reales
                    if (employeeA.isPresent()) {
                        avalName = employeeA.get().getFirstName().concat(" " + employeeA.get().getLastName());
                        avalDni = employeeA.get().getNationalId();
                        avalService = "Ordinario";
                    }

                    solipres.solicitudPrestamo(
                            loanSearch.get().getSoliNum(),
                            principalName,
                            principalDni,
                            principalService,
                            loanSearch.get().getAmountWithdrawn(),
                            calcularMontoPendientePorLoanId(loanSearch.get().getRefinanceParentId()),
                            8,
                            4,
                            //                            loanSearch.getTotalInterest(),
                            //                            loanSearch.getMonthlyFeeValue(),
                            loanSearch.get().getType(),
                            loanSearch.get().getDues(),
                            avalName,
                            avalDni,
                            avalService
                    );

                    // double totalPagar = (loanDet.getMonthlyIntableFundFee() + loanDet.getMonthlyCapitalInstallment()) * loanR.getDues();
                    // cuotaMensualConFondoIntangible * meses
                } catch (SQLException ex) {

                } finally {
                    dialog.dispose();
                }
            }).start();

        }

//        if (e.getSource().equals(componentManageLoan.jButtonReporteCompromisoPago)) {
//
//            CompromisoPago compromisoPago = new CompromisoPago();
//
//            Optional<LoanTb> loanSearch;
//            try {
//                loanSearch = findLoan(componentManageLoan.textSearchLoanSoli.getText());
//            } catch (SQLException ex) {
//                
//                System.out.println("Error /| " + ex.getMessage());
//                
//                JOptionPane.showMessageDialog(null, "Ocurrio un problema");
//                return;
//            }
////            if (txtRNumberReport.getText().isEmpty()) {
//
//                JOptionPane.showMessageDialog(null, "Rellena todas las casillas");
////            } else {
//                JOptionPane optionPane = new JOptionPane("Descargando...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
//                JDialog dialog = optionPane.createDialog(null, "Información");
//
//                dialog.setModal(false);
//                dialog.setVisible(true);
//
//                new Thread(() -> {
//                    try {
//                        Loan loanR = loanDAO.search("SoliNum", txtRNumberReport.getText());
//
//                        if (loanR == null) {
//                            JOptionPane.showMessageDialog(null, "No existe el numero de solicitud " + txtRNumberReport.getText());
//                            return;
//                        }
//
//                        Employee employeeR = employeeDAO.search("Dni", loanR.getEmployeeId());
//
//                        compromisoPago.compromisoPago(txtRNumberReport.getText(), employeeR.getName(), employeeR.getDni());
//                    } finally {
//                        dialog.dispose();
//                    }
//                }).start();
////            }
//        }
//
//        if (e.getSource().equals(componentManageLoan.jButtonReporteCompromisoAval)) {
//
//            CompromisoPagoAval compromisoAval = new CompromisoPagoAval();
//
//            if (txtRNumberReport.getText().isEmpty()) {
//
//                JOptionPane.showMessageDialog(null, "Rellena todas las casillas");
//
//            } else {
//                JOptionPane optionPane = new JOptionPane("Descargando...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
//                JDialog dialog = optionPane.createDialog(null, "Información");
//
//                dialog.setModal(false);
//                dialog.setVisible(true);
//
//                new Thread(() -> {
//                    try {
//
//                        LoanTb loanR = loanDAO.search("SoliNum", txtRNumberReport.getText());
//
//                        if (loanR == null) {
//                            JOptionPane.showMessageDialog(null, "No existe el numero de solicitud " + txtRNumberReport.getText());
//                            return;
//                        }
//
//                        Employee employeeR = employeeDAO.search("Dni", loanR.getEmployeeId());
//
//                        Employee guarantorR = employeeDAO.search("Dni", loanR.getAvalId());
//
//                        if (guarantorR == null) {
//                            compromisoAval.compromisoPagoAval(txtRNumberReport.getText(), "              ", "             ", employeeR.getName(), employeeR.getDni());
//                            return;
//                        }
//
//                        compromisoAval.compromisoPagoAval(txtRNumberReport.getText(), guarantorR.getName(), guarantorR.getDni(), employeeR.getName(), employeeR.getDni());
//                    } finally {
//                        dialog.dispose();
//                    }
//                }).start();
//            }
//        }

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
                    || componentManageLoan.textAmountLoan.getText().isBlank()) {

                JOptionPane.showMessageDialog(null, "Rellene los campos para poder registrar", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return;

            }

            insertDataLoan(employeeApplicant, employeeAval, componentManageLoan.textAmountLoan, componentManageLoan.jComboBoxCuotas.getSelectedItem());
        }
        if (e.getSource().equals(componentManageLoan.jButtonCalcularDemo)) {
            if (componentManageLoan.jTextFieldMontoDemostration.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Rellene los campos para poder el calculo demo", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (componentManageLoan.textRefinanciamientoDemostration.getText().isBlank()) {
                componentManageLoan.textRefinanciamientoDemostration.setText("0.0");
            }

            generateLoan(Integer.parseInt(String.valueOf(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem())),
                    Double.valueOf(String.format("%.2f", Double.valueOf(componentManageLoan.jTextFieldMontoDemostration.getText()))),
                    Double.valueOf(String.format("%.2f", Double.valueOf(componentManageLoan.textRefinanciamientoDemostration.getText()))),
                    Boolean.TRUE);
        }
        if (e.getSource().equals(componentManageLoan.jButtonReportLiquidation)) {

            if (componentManageLoan.jTextFieldMontoDemostration.getText().isBlank()) {
                return;
            }
            if (componentManageLoan.textRefinanciamientoDemostration.getText().isBlank()) {
                return;
            }

            generateExcelLiquidación(componentManageLoan.jTextFieldMontoDemostration.getText(), componentManageLoan.textRefinanciamientoDemostration.getText() == null ? "0.0" : componentManageLoan.textRefinanciamientoDemostration.getText(),
                    Integer.valueOf(componentManageLoan.jComboBoxCuotasDemonstration.getSelectedItem().toString()), null, Boolean.TRUE);
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
                if (componentManageLoan.comboBoxApplicant.getSelectedIndex() != -1) {

                    try {
                        employeeApplicant = listEmployeeApplicant.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();
                    } catch (Exception eee) {
                    }
                    //((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).setText(componentManageLoan.comboBoxApplicant.getSelectedItem().toString());
                    try {
                        if (employeeAval.getNationalId().equalsIgnoreCase(employeeApplicant.getNationalId())) {
                            JOptionPane.showMessageDialog(null, "Esta repitiendo el mismo trabajador en el Aval", "GESTIÓN PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    } catch (Exception ee) {
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
                if (componentManageLoan.comboBoxAval.getSelectedIndex() != -1) {

                    employeeAval = listEmployeeAval.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();

                    try {
                        if (employeeAval.getNationalId().equalsIgnoreCase(employeeApplicant.getNationalId())) {
                            JOptionPane.showMessageDialog(null, "Esta repitiendo en el trabajador solicitado", "GESTIÓN PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }
                    } catch (Exception ee) {
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

    @Override
    public void stateChanged(ChangeEvent e) {
        int index = componentManageLoan.jTabbedPane1.getSelectedIndex();
        switch (index) {
            case 0 -> {
            }
            case 1 -> {
            }
            case 2 ->
                fillLoanTable(componentManageLoan.jTableLoanList);
        }

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            int index = componentManageLoan.jTableLoanList.getSelectedRow();
            if (index != -1) {
                // Actualiza la tabla para reflejar cualquier cambio visual
                componentManageLoan.jTableLoanList.repaint();

                String status = componentManageLoan.jTableLoanList.getValueAt(index, 5).toString();
                String soliNum = componentManageLoan.jTableLoanList.getValueAt(index, 0).toString();

                if (status.equalsIgnoreCase("Pendiente")) {

                    // Crear un ComboBox con las opciones
                    JComboBox<String> comboBox = new JComboBox<>(new String[]{"Aceptado", "Denegado"});

                    // Mostrar un JOptionPane con el ComboBox
                    int option = JOptionPane.showConfirmDialog(
                            null,
                            comboBox,
                            "GESTIÓN PRÉSTAMO - Cambiar Estado",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Verificar si el usuario hizo clic en "OK"
                    if (option == JOptionPane.OK_OPTION) {
                        // Obtener la opción seleccionada del ComboBox
                        String newStatus = (String) comboBox.getSelectedItem();

                        // Lógica según la opción seleccionada
                        if (newStatus != null) {

                            switch (newStatus) {
                                case "Aceptado" -> {
                                    {
                                        try {
//                                         

                                            Optional<LoanTb> loan = findLoan(soliNum);

//                                            if (loan.get().getAmount() == 0.0) {
//                                                double montofinal = Math.abs(loan.get().getOriginalAmount() - loan.get().getAmount());
//                                            }
                                            if (loan.get().getRefinanceParentId() != null) {
                                                System.out.println("Refinanciamiento total deuda" + calcularMontoPendientePorLoanId(loan.get().getRefinanceParentId()));
                                                generateLoan(loan.get().getDues(), loan.get().getRequestedAmount(),
                                                        calcularMontoPendientePorLoanId(loan.get().getRefinanceParentId()), Boolean.FALSE);
                                            } else {
                                                generateLoan(loan.get().getDues(), loan.get().getRequestedAmount(),
                                                        0.0, Boolean.FALSE);
                                            }

                                            //insertDataLoan(loan.get(), loan.get(), loan.get().getOriginalAmount(), 3);
                                            LoanDetailsTb loanDetailsTb = new LoanDetailsTb();

                                            loanDetailsTb.setTotalInterest(interesTotal);
                                            loanDetailsTb.setTotalIntangibleFund(fondoIntangibleTotal);
                                            loanDetailsTb.setMonthlyCapitalInstallment(capitalSinInteresMensual);
                                            loanDetailsTb.setMonthlyInterestFee(interesMensual);

                                            loanDetailsTb.setMonthlyIntangibleFundFee(fondoIntangibleTotal / loan.get().getDues());
                                            loanDetailsTb.setMonthlyFeeValue(cuotaMensualConFondoIntangible);

                                            registerAcept(loanDetailsTb, loan.get(), 1);

                                        } catch (SQLException ex) {
                                            System.out.println("Error -> " + ex.getMessage());
                                        }
                                    }

                                }

                                case "Denegado" -> {
                                    // Lógica para estado denegado
                                    try {
                                        System.out.println("Denegado");
                                        // Lógica para estado aceptado
                                        updateSoliNumStatus(soliNum, newStatus, 1);

                                    } catch (SQLException ex) {
                                        System.out.println("Error -> " + ex.getMessage());
                                    }

                                    JOptionPane.showMessageDialog(null, "Estado cambiado a: Denegado");
                                }
                            }
                            // Actualizar la tabla después de un cambio
                            fillLoanTable(componentManageLoan.jTableLoanList);
                        }
                    }
                } else {
                    componentManageLoan.jDialog1.setModal(true);
                    componentManageLoan.jDialog1.setResizable(false);
                    componentManageLoan.jDialog1.setSize(684, 475);
                    componentManageLoan.jDialog1.setLocationRelativeTo(null);
                    componentManageLoan.jDialog1.setVisible(true);
                }
            }

        }
    }

}
