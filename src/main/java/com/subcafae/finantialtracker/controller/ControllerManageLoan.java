/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.dao.LoanDetailsDao;
import com.subcafae.finantialtracker.data.dao.RegistroDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.Loan;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.data.entity.RegistroTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageLoan;
import com.subcafae.finantialtracker.report.descuento.ExcelExporter;
import com.subcafae.finantialtracker.report.descuento.ExcelTable;
import com.subcafae.finantialtracker.report.loanReport.CompromisoPago;
import com.subcafae.finantialtracker.report.loanReport.CompromisoPagoAval;
import com.subcafae.finantialtracker.report.loanReport.SolicitudPrestamo;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerManageLoan extends ModelManageLoan implements ActionListener, KeyListener, ChangeListener, ListSelectionListener {

    public ControllerManageLoan(ComponentManageLoan componentManageLoan, UserTb user) {

        super(componentManageLoan);
        componentManageLoan.dateStart1.setDate(new Date());
        ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).addKeyListener(this);
        ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).addKeyListener(this);
        componentManageLoan.jTextFieldSearchLoanNum.addKeyListener(this);

        componentManageLoan.buttonRegisterLoan.addActionListener(this);
        componentManageLoan.buttonCleanApplicant.addActionListener(this);
        componentManageLoan.buttonCleanAval.addActionListener(this);
        componentManageLoan.jTabbedPane1.addChangeListener(this);
        componentManageLoan.jButtonCalcularDemo.addActionListener(this);
        componentManageLoan.jButtonReportLiquidation.addActionListener(this);
        componentManageLoan.jTableLoanList.getSelectionModel().addListSelectionListener(this);
        componentManageLoan.jTableListLoanDetails.getSelectionModel().addListSelectionListener(this);
        componentManageLoan.jButtonReporteCompromisoAval.addActionListener(this);
        componentManageLoan.jButtonReporteCompromisoPago.addActionListener(this);
        componentManageLoan.jButtonSolicitudLoan.addActionListener(this);
        componentManageLoan.jButtonExcelList.addActionListener(this);
        componentManageLoan.jButtonExcelCompleto.addActionListener(this);
        componentManageLoan.jButtonBuscar.addActionListener(this);
        componentManageLoan.jButton1.addActionListener(this);
        componentManageLoan.jButtonShowList.addActionListener(this);
//        componentManageLoan.jDialog1.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                if (componentManageLoan.jTableLoanList.getRowCount() == 1) {
//
//                    List<Loan> listTable = new LoanDao().getAllLoanss().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(componentManageLoan.jTableLoanList.getValueAt(0, 0).toString())).collect(Collectors.toList());
//
//                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
//                    model.setRowCount(0);
//
//                    for (Loan loan : listTable) {
//
//                        model.addRow(new Object[]{
//                            loan.getSoliNum(),
//                            loan.getSolicitorName(),
//                            loan.getGuarantorName(),
//                            loan.getRequestedAmount(),
//                            loan.getAmountWithdrawn(),
//                            loan.getState(),
//                            loan.getPaymentResponsibility()
//                        });
//                    }
//
//                } else {
//                    fillLoanTable(componentManageLoan.jTableLoanList);
//                }
//            }
//        });

    }

    public void methodListDeta(String soliNum) {
        DefaultTableModel modelList = (DefaultTableModel) componentManageLoan.jTableListLoanDetails.getModel();
        modelList.setRowCount(0);
        Optional<LoanTb> loan;
        try {
            loan = findLoan(soliNum);

            List<LoanDetailsTb> listDetails = new LoanDetailsDao().findLoanDetailsByLoanId(loan.get().getId());

            Double monto = 0.0;

            for (LoanDetailsTb loanDetailsTb : listDetails) {
                if (loanDetailsTb.getState().equalsIgnoreCase("Pendiente")) {
                    monto += loanDetailsTb.getMonthlyFeeValue();
                }

                if (loanDetailsTb.getState().equalsIgnoreCase("Parcial")) {
                    monto += (loanDetailsTb.getMonthlyFeeValue() - loanDetailsTb.getPayment());
                }
                modelList.addRow(new Object[]{
                    loanDetailsTb.getMonthlyFeeValue(),
                    loanDetailsTb.getPayment(),
                    loanDetailsTb.getDues(),
                    loanDetailsTb.getPaymentDate().toString(),
                    loanDetailsTb.getState()
                });

            }
            componentManageLoan.jLabelMonto.setText(String.format("%.2f", monto));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION PRESTAMOS", JOptionPane.OK_OPTION);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e
    ) {
        if (e.getSource().equals(componentManageLoan.jButtonShowList)) {

            try {
                if (componentManageLoan.dateStart.getDate() != null || componentManageLoan.dateFinaly.getDate() != null) {
                    insertTablet(componentManageLoan.dateStart.getDate(), componentManageLoan.dateFinaly.getDate());
                } else {
                    JOptionPane.showMessageDialog(null, "Rellene las fechas para mostrar la lista");
                    return;
                }
            } catch (Exception ee) {
                System.out.println("Error -> " + ee.getMessage());
                JOptionPane.showMessageDialog(null, "Rellene las fechas para mostrar la lista");
                return;
            }
        }

        if (e.getSource().equals(componentManageLoan.jButton1)) {

            if (componentManageLoan.jTableLoanList1.getValueAt(0, 2).toString().isBlank()) {

                JOptionPane.showMessageDialog(null, "No contiene Aval");
                return;
            }

            if (JOptionPane.showConfirmDialog(null, "Desea pasar la responsabilidad al aval ?", "MENSAJE", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                String soliNum = componentManageLoan.jTableLoanList1.getValueAt(0, 0).toString();

                Boolean ver = new LoanDetailsDao().updatePaymentResponsibilityToGuarantor(soliNum);

                if (ver) {
                    componentManageLoan.jButton1.setEnabled(false);
                    componentManageLoan.jLabelShowAval.setText("LOS PAGOS EN PARTES, LO HARÁ EL AVAL");
                    componentManageLoan.jTableLoanList1.setValueAt("AVAL", 0, 6);
                }

            }

        }

        if (e.getSource().equals(componentManageLoan.jButtonBuscar)) {

            if (componentManageLoan.jTextFieldSearchLoanNum.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");
                return;
            }

            ViewMain.loading.setModal(true);
            ViewMain.loading.setLocationRelativeTo(componentManageLoan);

            new Thread(() -> {
                try {
                    List<Loan> listTable = new LoanDao().searchLoan(componentManageLoan.jTextFieldSearchLoanNum.getText());

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();

                        if (!listTable.isEmpty()) {
                            DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
                            model.setRowCount(0);

                            for (Loan loan : listTable) {

                                if (loan.getRequestedAmount() != loan.getAmountWithdrawn()) {
                                    if (loan.getRefinanciado() == null) {
                                        if (!loan.getAmountWithdrawn().toString().equals("0.00")) {
                                            Double daod = Double.parseDouble(loan.getRequestedAmount().toString()) - Double.parseDouble(loan.getAmountWithdrawn().toString());
                                            loan.setRefinanciado(BigDecimal.valueOf(daod));
                                        }
                                    }
                                }
                                model.addRow(new Object[]{
                                    loan.getModificado() == null ? "" : loan.getModificado(),
                                    loan.getSoliNum(),
                                    loan.getSolicitorName(),
                                    loan.getGuarantorName() == null ? "" : loan.getGuarantorName(),
                                    loan.getRefinanciado() == null ? "" : loan.getRefinanciado(),
                                    loan.getRequestedAmount(),
                                    loan.getAmountWithdrawn().toString().equalsIgnoreCase("0.00") ? loan.getRequestedAmount() : loan.getAmountWithdrawn(),
                                    loan.getCantCuota(),
                                    loan.getInterTo() == null ? "" : loan.getInterTo(),
                                    loan.getFondoTo() == null ? "" : loan.getFondoTo(),
                                    loan.getCuotaMenSin() == null ? "" : loan.getCuotaMenSin(),
                                    loan.getCuotaInter() == null ? "" : loan.getCuotaInter(),
                                    loan.getCuotaFond() == null ? "" : loan.getCuotaFond(),
                                    loan.getValor() == null ? "" : loan.getValor(),
                                    loan.getState(),
                                    loan.getPaymentResponsibility()
                                });
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No se encontro numero de solicitud");
                        }
                    });
                } catch (Exception ex) {
                    System.out.println("Error /| " + ex.getMessage());
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrio un problema", "ERROR", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            ViewMain.loading.setVisible(true);
        }
        if (e.getSource().equals(componentManageLoan.jButtonExcelList)) {
            try {

                if (componentManageLoan.dateStart.getDate() != null || componentManageLoan.dateFinaly.getDate() != null) {

                    List<Loan> list = getAllLoanss(new java.sql.Date(componentManageLoan.dateStart.getDate().getTime()), new java.sql.Date(componentManageLoan.dateFinaly.getDate().getTime()));
                    ExcelTable.exportToExcel(list, "PRESTAMOS", 6);
                } else {
                    JOptionPane.showMessageDialog(null, "Rellene las fechas para mostrar la lista");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Rellene las fechas para mostrar la lista");
                //Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (e.getSource().equals(componentManageLoan.jButtonExcelCompleto)) {
            try {
                ExcelTable.exportToExcel(componentManageLoan.jTableLoanList1, componentManageLoan.jTableListLoanDetails, "PRESTAMOS", "PRESTAMOS DETALLADOS", 6);
            } catch (IOException ex) {
                //Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (e.getSource().equals(componentManageLoan.jButtonSolicitudLoan)) {

            if (componentManageLoan.textSearchLoanSoli.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");
                return;
            }

            ViewMain.loading.setModal(true);
            ViewMain.loading.setLocationRelativeTo(componentManageLoan);

            new Thread(() -> {
                try {
                    Optional<LoanTb> loanSearch;
                    try {
                        loanSearch = findLoan(componentManageLoan.textSearchLoanSoli.getText());
                    } catch (SQLException ex) {
                        System.out.println("Error /| " + ex.getMessage());
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            ViewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "Ocurrio un problema");
                        });
                        return;
                    }

                    if (!loanSearch.isPresent()) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            ViewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "No se encontró número de solicitud");
                        });
                        return;
                    }

                    // Número de solicitud detalle
                    // Nombre del empleado y DNI del empleado
                    Optional<EmployeeTb> employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());

                    Optional<EmployeeTb> employeeA = null;
                    try {
                        employeeA = new EmployeeDao().findById(loanSearch.get().getGuarantorIds());
                    } catch (Exception eee) {
                        // Aval no encontrado, continuar
                    }

                    String avalName = ".....................................................................";
                    String avalDni = "...............";
                    String avalService = "...............";
                    SolicitudPrestamo solipres = new SolicitudPrestamo();

                    String principalName = employeeR.get().getFullName();
                    String principalDni = employeeR.get().getNationalId();
                    String principalService = "Ordinario";

                    // Si el aval no es "00000000", buscamos los datos reales
                    if (employeeA.isPresent()) {

                        avalName = employeeA.get().getFullName();
                        avalDni = employeeA.get().getNationalId();
                        avalService = "Ordinario";
                    }

                    double monto = 0.0;

                    if (!Objects.equals(loanSearch.get().getRequestedAmount().toString(), loanSearch.get().getAmountWithdrawn().toString())) {

                        if (loanSearch.get().getRefinanceParentId() == 0) {

                            if (loanSearch.get().getAmountWithdrawn() > 0) {

                                Double daod = Double.parseDouble(loanSearch.get().getRequestedAmount().toString()) - Double.parseDouble(loanSearch.get().getAmountWithdrawn().toString());
                                monto = daod;
                            }
                        }
                    }
                    //
                    generateLoan(loanSearch.get().getDues(), loanSearch.get().getRequestedAmount(), calcularMontoPendientePorLoanId(loanSearch.get().getRefinanceParentId()), Boolean.FALSE);

                    System.out.println("Capital mensual - >  " + capitalMensual);
                    System.out.println("Capital mensual - >  " + capitalSinInteresMensual);
                    System.out.println("Capital mensual - >  " + capitalMensual * loanSearch.get().getDues());
                    solipres.solicitudPrestamo(
                            loanSearch.get().getSoliNum(),
                            principalName,
                            principalDni,
                            principalService,
                            loanSearch.get().getRequestedAmount(),
                            monto == 0.0 ? calcularMontoPendientePorLoanId(loanSearch.get().getRefinanceParentId()) : monto,
                            cuotaMensualConFondoIntangible,
                            capitalMensual,
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
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                    });
                } catch (SQLException ex) {
                    System.out.println("Error -> " + ex.getMessage());
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrió un problema al generar la solicitud", "ERROR", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            ViewMain.loading.setVisible(true);
        }

        if (e.getSource().equals(componentManageLoan.jButtonReporteCompromisoPago)) {

            CompromisoPago compromisoPago = new CompromisoPago();

            if (componentManageLoan.textSearchLoanSoli.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");

                return;
            }

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

            ViewMain.loading.setModal(true);
            ViewMain.loading.setLocationRelativeTo(componentManageLoan);

            new Thread(() -> {
                try {
                    Optional<EmployeeTb> employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());

                    compromisoPago.compromisoPago(loanSearch.get().getSoliNum(),
                            employeeR.get().getFullName(),
                            employeeR.get().getNationalId());

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                    });
                } catch (Exception ex) {
                    System.out.println("Error -> " + ex.getMessage());
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrió un problema al generar el compromiso de pago", "ERROR", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            ViewMain.loading.setVisible(true);
        }

        if (e.getSource().equals(componentManageLoan.jButtonReporteCompromisoAval)) {

            CompromisoPagoAval compromisoAval = new CompromisoPagoAval();

            if (componentManageLoan.textSearchLoanSoli.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");
                return;
            }

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

            ViewMain.loading.setModal(true);
            ViewMain.loading.setLocationRelativeTo(componentManageLoan);

            new Thread(() -> {
                try {
                    Optional<EmployeeTb> employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());
                    Optional<EmployeeTb> guarantorR = null;

                    try {
                        guarantorR = new EmployeeDao().findById(loanSearch.get().getGuarantorIds());
                    } catch (Exception eee) {
                        // Aval no encontrado
                    }

                    if (guarantorR == null || !guarantorR.isPresent()) {
                        compromisoAval.compromisoPagoAval(loanSearch.get().getSoliNum(), employeeR.get().getFullName(), employeeR.get().getNationalId(), "              ", "             ");
                    } else {
                        compromisoAval.compromisoPagoAval(loanSearch.get().getSoliNum(), employeeR.get().getFullName(), employeeR.get().getNationalId(), guarantorR.get().getFullName(), guarantorR.get().getNationalId());
                    }

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                    });
                } catch (Exception ex) {
                    System.out.println("Error -> " + ex.getMessage());
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrió un problema al generar el compromiso de pago aval", "ERROR", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            ViewMain.loading.setVisible(true);
        }

        if (e.getSource().equals(componentManageLoan.buttonCleanApplicant)) {
            employeeApplicant = null;
            componentManageLoan.comboBoxApplicant.setEnabled(true);
            componentManageLoan.buttonCleanApplicant.setEnabled(false);
            componentManageLoan.buttonConfirmApplicant.setEnabled(true);
        }
        if (e.getSource().equals(componentManageLoan.buttonCleanAval)) {
            employeeAval = null;
            componentManageLoan.comboBoxAval.setEnabled(true);
            componentManageLoan.buttonCleanAval.setEnabled(false);
            componentManageLoan.buttonConfirmAval.setEnabled(true);
        }
        if (e.getSource().equals(componentManageLoan.buttonRegisterLoan)) {

            if (employeeApplicant == null
                    || componentManageLoan.textAmountLoan.getText().isBlank()) {

                JOptionPane.showMessageDialog(null, "Rellene los campos para poder registrar", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return;

            }

            insertDataLoan(employeeApplicant, employeeAval, componentManageLoan.textAmountLoan, componentManageLoan.jComboBoxCuotas.getSelectedItem() , componentManageLoan.dateStart1.getDate());
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
    public void keyTyped(KeyEvent e
    ) {

    }

    @Override
    public void keyPressed(KeyEvent e
    ) {

    }

    @Override
    public void keyReleased(KeyEvent e
    ) {
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

        // Autocompletado para campo de búsqueda de número de solicitud
        if (e.getSource().equals(componentManageLoan.jTextFieldSearchLoanNum)) {
            if ((e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) // Números
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) { // Borrar
                showSoliNumAutocomplete();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                popupSoliNum.setVisible(false);
            }
        }

    }

    @Override
    public void stateChanged(ChangeEvent e
    ) {
        int index = componentManageLoan.jTabbedPane1.getSelectedIndex();
        switch (index) {
            case 0 -> {
                componentManageLoan.dateStart1.setDate(new Date());
            }
            case 1 -> {
            }
            case 2 -> {

                // fillLoanTable(componentManageLoan.jTableLoanList);
            }

        }

    }

    @Override
    public void valueChanged(ListSelectionEvent e
    ) {
        if (e.getValueIsAdjusting()) {

            if (e.getSource().equals(componentManageLoan.jTableLoanList.getSelectionModel())) {

                int index = componentManageLoan.jTableLoanList.getSelectedRow();
                if (index != -1) {
                    // Actualiza la tabla para reflejar cualquier cambio visual
                    componentManageLoan.jTableLoanList.repaint();

                    String soliNum = componentManageLoan.jTableLoanList.getValueAt(index, 1).toString();
                    try {

                        Optional<LoanTb> loan = findLoan(soliNum);

                        if (loan.get().getState().equalsIgnoreCase("Pendiente")) {
                            System.out.println("Entrando");
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

                                                loanDetailsTb.setMonthlyFeeValue(capitalMensual);

                                                registerAcept(loanDetailsTb, loan.get(), 1);

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
                                    // fillLoanTable(componentManageLoan.jTableLoanList);
                                }
                            }
                        } else if (loan.get().getState().equalsIgnoreCase("Aceptado") || loan.get().getState().equalsIgnoreCase("Refinanciado")) {

                            try {
                                Loan loann = new LoanDao().searchLoan(loan.get().getSoliNum()).get(0);

                                Optional<LoanTb> showRe = new LoanDao().findLoan(soliNum);
                                if (showRe.isPresent()) {
                                    if (loann.getState().equalsIgnoreCase("Aceptado")) {

                                        if (showRe.get().getRefinanceParentId() > 0) {

                                            double monto = new LoanDao().calcularMontoPendientePorLoanId(showRe.get().getRefinanceParentId());
                                            componentManageLoan.montoRef.setText("Prestamo refinanciado deuda anterior solicitud con numero de solicitud " + new LoanDao().findLoan(showRe.get().getRefinanceParentId()).get().getSoliNum() + " -> " + monto);
                                        }
                                    }
                                }

                                if (loann.getPaymentResponsibility().equalsIgnoreCase("AVAL")) {
                                    componentManageLoan.jLabelShowAval.setText("LOS PAGOS EN PARTES, LO HARÁ EL AVAL");
                                    componentManageLoan.jButton1.setEnabled(false);
                                } else if (!loann.getPaymentResponsibility().equalsIgnoreCase("AVAL")) {
                                    componentManageLoan.jLabelShowAval.setText("LOS PAGOS EN PARTES, LO HARÁ EL SOLICITANTE");
                                    componentManageLoan.jButton1.setEnabled(true);
                                }
                                if (loann.getState().equalsIgnoreCase("Refinanciado")) {
                                    System.out.println("Bloquiando");
                                    componentManageLoan.jButton1.setEnabled(false);
                                }

                                if (new LoanDao().findLoan(soliNum).get().getStateLoan().equalsIgnoreCase("Pagado")) {
                                    componentManageLoan.jButton1.setEnabled(false);
                                }
                                if (loann.getGuarantorName() == null) {
                                    componentManageLoan.jButton1.setEnabled(false);
                                }

                                DefaultTableModel modelList = (DefaultTableModel) componentManageLoan.jTableListLoanDetails.getModel();
                                modelList.setRowCount(0);
                                DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList1.getModel();
                                model.setRowCount(0);

                                List<Loan> listTableFind = new LoanDao().searchLoan(soliNum);

                                model.addRow(new Object[]{
                                    listTableFind.get(0).getSoliNum(),
                                    listTableFind.get(0).getSolicitorName(),
                                    listTableFind.get(0).getGuarantorName(),
                                    listTableFind.get(0).getRequestedAmount(),
                                    listTableFind.get(0).getAmountWithdrawn().toString().equalsIgnoreCase("0.00") ? listTableFind.get(0).getRequestedAmount() : listTableFind.get(0).getAmountWithdrawn(),
                                    listTableFind.get(0).getState(),
                                    listTableFind.get(0).getPaymentResponsibility()
                                });

                                methodListDeta(soliNum);

                                componentManageLoan.jDialog1.setModal(true);
                                componentManageLoan.jDialog1.setResizable(false);
                                componentManageLoan.jDialog1.setSize(980, 570);
                                componentManageLoan.jDialog1.setLocationRelativeTo(null);
                                componentManageLoan.jDialog1.setVisible(true);
                            } catch (SQLException ex) {
                                Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } catch (Exception ee) {
                        System.out.println("Error -> " + ee.getMessage());
                    }
                }

            }

            if (e.getSource().equals(componentManageLoan.jTableListLoanDetails.getSelectionModel())) {

                int index = componentManageLoan.jTableListLoanDetails.getSelectedRow();

                if (index != -1) {

                    // Actualiza la tabla para reflejar cualquier cambio visual
                    componentManageLoan.jTableListLoanDetails.repaint();

                    try {

                        Optional<LoanTb> loan;
                        try {
                            String soliNum = componentManageLoan.jTableLoanList1.getValueAt(0, 0).toString();

                            loan = findLoan(soliNum);

                            if (loan.get().getState().equalsIgnoreCase("Refinanciado")) {
                                JOptionPane.showMessageDialog(null, "Préstamo refinanciado", "GÉSTION PRESTAMOS", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }

                            List<LoanDetailsTb> listDetails = new LoanDetailsDao().findLoanDetailsByLoanId(loan.get().getId());
                            int couta = Integer.parseInt(componentManageLoan.jTableListLoanDetails.getValueAt(index, 2).toString());

                            LoanDetailsTb tableDetails = listDetails.stream().filter(predicate -> predicate.getDues() == couta).findFirst().get();

                            if (!tableDetails.getState().equalsIgnoreCase("Pagado")) {

                                JTextField text = new JTextField();
                                Border bordeConTitulo = BorderFactory.createTitledBorder("Escriba el monto");
                                text.setBorder(bordeConTitulo);

                                TextFieldValidator.applyDecimalFilter(text);

                                int opction = JOptionPane.showConfirmDialog(null, text, "INFORMACION", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                if (opction != JOptionPane.OK_OPTION) {
                                    return;
                                }
                                ViewMain.loading.setVisible(true);
                                componentManageLoan.jDialog1.setEnabled(false);

                                if (!text.getText().isBlank()) {

                                    Double monto = Double.valueOf(componentManageLoan.jTableListLoanDetails.getValueAt(index, 0).toString());
                                    Double montoParcial = Double.valueOf(componentManageLoan.jTableListLoanDetails.getValueAt(index, 1).toString());
                                    Double finalMonto = Double.parseDouble(String.format("%.2f", (monto - montoParcial)));
                                    Double montoEx = Double.valueOf(text.getText());

                                    EmployeeTb empl = new EmployeeTb();
                                    if (loan.get().getPaymentResponsibility().equalsIgnoreCase("GUARANTOR")) {
                                        String name = componentManageLoan.jTableLoanList1.getValueAt(0, 2).toString();
                                        empl = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFullName().equalsIgnoreCase(name)).findFirst().get();

                                    } else {
                                        String name = componentManageLoan.jTableLoanList1.getValueAt(0, 1).toString();
                                        empl = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFullName().equalsIgnoreCase(name)).findFirst().get();
                                    }

                                    if (finalMonto > montoEx) {
                                        RegistroTb registroTb = new RegistroTb(empl.getEmployeeId(), montoEx);
                                        new RegistroDao().insertarRegistroCompleto(registroTb, tableDetails, null, montoEx);
                                        new LoanDetailsDao().updateLoanStateByLoandetailId(tableDetails.getId(), monto, montoEx);
                                    }
                                    if (Objects.equals(finalMonto, montoEx)) {
                                        RegistroTb registroTb = new RegistroTb(empl.getEmployeeId(), montoEx);
                                        new RegistroDao().insertarRegistroCompleto(registroTb, tableDetails, null, montoEx);
                                        new LoanDetailsDao().updateLoanStateByLoandetailId(tableDetails.getId(), monto, montoEx);

                                    }

                                    if (finalMonto < montoEx) {
                                        componentManageLoan.jDialog1.setEnabled(true);
                                        ViewMain.loading.dispose();
                                        JOptionPane.showMessageDialog(null, "ERROR EN EL MONTO DADO", "GÉSTION PRESTAMOS", JOptionPane.OK_OPTION);
                                    }

                                    DefaultTableModel modelList = (DefaultTableModel) componentManageLoan.jTableListLoanDetails.getModel();
                                    modelList.setRowCount(0);

                                    List<LoanDetailsTb> listDetailss = new LoanDetailsDao().findLoanDetailsByLoanId(loan.get().getId());
                                    Double montfo = 0.0;
                                    for (LoanDetailsTb loanDetailsTb : listDetailss) {
                                        if (loanDetailsTb.getState().equalsIgnoreCase("Pendiente")) {
                                            montfo += loanDetailsTb.getMonthlyFeeValue();
                                        }

                                        if (loanDetailsTb.getState().equalsIgnoreCase("Parcial")) {
                                            montfo += (loanDetailsTb.getMonthlyFeeValue() - loanDetailsTb.getPayment());
                                        }

                                        modelList.addRow(new Object[]{
                                            loanDetailsTb.getMonthlyFeeValue(),
                                            loanDetailsTb.getPayment(),
                                            loanDetailsTb.getDues(),
                                            loanDetailsTb.getPaymentDate().toString(),
                                            loanDetailsTb.getState()
                                        });

                                    }

                                    componentManageLoan.jLabelMonto.setText(String.format("%.2f", montfo));

                                    List<Loan> listTableFind = new LoanDao().searchLoan(soliNum);

                                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList1.getModel();
                                    model.setRowCount(0);

                                    model.addRow(new Object[]{
                                        listTableFind.get(0).getSoliNum(),
                                        listTableFind.get(0).getSolicitorName(),
                                        listTableFind.get(0).getGuarantorName(),
                                        listTableFind.get(0).getRequestedAmount(),
                                        listTableFind.get(0).getAmountWithdrawn().toString().equalsIgnoreCase("0.00") ? listTableFind.get(0).getRequestedAmount() : listTableFind.get(0).getAmountWithdrawn(),
                                        listTableFind.get(0).getState(),
                                        listTableFind.get(0).getPaymentResponsibility()
                                    });
                                }

                            }

                            componentManageLoan.jDialog1.setEnabled(true);
                            ViewMain.loading.dispose();

                        } catch (SQLException ex) {
                            componentManageLoan.jDialog1.setEnabled(true);
                            ViewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION PRESTAMOS", JOptionPane.OK_OPTION);
                            // Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    } catch (Exception ee) {
                        System.out.println("Error -<" + ee.getMessage());
                    }

                }

            }
        }

    }
}
