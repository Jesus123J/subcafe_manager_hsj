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
import java.sql.SQLException;
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

        ((JTextField) componentManageLoan.comboBoxApplicant.getEditor().getEditorComponent()).addKeyListener(this);
        ((JTextField) componentManageLoan.comboBoxAval.getEditor().getEditorComponent()).addKeyListener(this);

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
        componentManageLoan.jDialog1.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (componentManageLoan.jTableLoanList.getRowCount() == 1) {

                    List<Loan> listTable = new LoanDao().getAllLoanss().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(componentManageLoan.jTableLoanList.getValueAt(0, 0).toString())).collect(Collectors.toList());

                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
                    model.setRowCount(0);

                    for (Loan loan : listTable) {

                        model.addRow(new Object[]{
                            loan.getSoliNum(),
                            loan.getSolicitorName(),
                            loan.getGuarantorName(),
                            loan.getRequestedAmount(),
                            loan.getAmountWithdrawn(),
                            loan.getState(),
                            loan.getPaymentResponsibility()
                        });
                    }

                } else {
                    fillLoanTable(componentManageLoan.jTableLoanList);
                }
            }
        });

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
        if (e.getSource().equals(componentManageLoan.jButton1)) {

            int index = componentManageLoan.jTableLoanList.getSelectedRow();

            if (index != -1) {

                if (componentManageLoan.jTableLoanList.getValueAt(index, 2).toString().isBlank()) {

                    JOptionPane.showMessageDialog(null, "No contiene Aval");
                    return;
                }
                if (JOptionPane.showConfirmDialog(null, "Desea pasar la responsabilidad al aval ?", "MENSAJE", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                    String soliNum = componentManageLoan.jTableLoanList.getValueAt(index, 0).toString();

                    new LoanDetailsDao().updatePaymentResponsibilityToGuarantor(soliNum);
                    List<Loan> list = new LoanDao().getAllLoanss();
                    List<Loan> listTableFind = list.stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(soliNum)).collect(Collectors.toList());

                    if (componentManageLoan.jTableLoanList.getRowCount() > 0) {

                        if (!list.isEmpty()) {

                            DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
                            model.setRowCount(0);

                            for (Loan loan : list) {

                                model.addRow(new Object[]{
                                    loan.getSoliNum(),
                                    loan.getSolicitorName(),
                                    loan.getGuarantorName(),
                                    loan.getRequestedAmount(),
                                    loan.getAmountWithdrawn(),
                                    loan.getState(),
                                    loan.getPaymentResponsibility()
                                });
                            }
                        }

                    } else {
                        if (!listTableFind.isEmpty()) {

                            DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
                            model.setRowCount(0);

                            for (Loan loan : listTableFind) {

                                model.addRow(new Object[]{
                                    loan.getSoliNum(),
                                    loan.getSolicitorName(),
                                    loan.getGuarantorName(),
                                    loan.getRequestedAmount(),
                                    loan.getAmountWithdrawn(),
                                    loan.getState(),
                                    loan.getPaymentResponsibility()
                                });
                            }
                        }
                    }

                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList1.getModel();
                    model.setRowCount(0);

                    model.addRow(new Object[]{
                        componentManageLoan.jTableLoanList.getValueAt(index, 0).toString(),
                        componentManageLoan.jTableLoanList.getValueAt(index, 1).toString(),
                        componentManageLoan.jTableLoanList.getValueAt(index, 2) != null ? componentManageLoan.jTableLoanList.getValueAt(index, 2).toString() : "",
                        componentManageLoan.jTableLoanList.getValueAt(index, 3).toString(),
                        componentManageLoan.jTableLoanList.getValueAt(index, 4).toString(),
                        componentManageLoan.jTableLoanList.getValueAt(index, 5).toString(),
                        componentManageLoan.jTableLoanList.getValueAt(index, 6).toString()
                    });

                }

            }

        }
        if (e.getSource().equals(componentManageLoan.jButtonBuscar)) {
            if (componentManageLoan.jTextFieldSearchLoanNum.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");
                return;
            }

            try {

                ViewMain.loading.setVisible(true);

                List<Loan> listTable = new LoanDao().getAllLoanss().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(componentManageLoan.jTextFieldSearchLoanNum.getText())).collect(Collectors.toList());

                if (!listTable.isEmpty()) {

                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList.getModel();
                    model.setRowCount(0);

                    for (Loan loan : listTable) {

                        model.addRow(new Object[]{
                            loan.getSoliNum(),
                            loan.getSolicitorName(),
                            loan.getGuarantorName(),
                            loan.getRequestedAmount(),
                            loan.getAmountWithdrawn(),
                            loan.getState(),
                            loan.getPaymentResponsibility()
                        });
                    }
                    ViewMain.loading.dispose();
                } else {
                    ViewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "No se encontro numero de solicitud");
                }

            } catch (Exception ex) {
                ViewMain.loading.dispose();
                System.out.println("Error /| " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un problema");
                // Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (e.getSource().equals(componentManageLoan.jButtonExcelList)) {
            try {
                ExcelTable.exportToExcel(componentManageLoan.jTableLoanList, "PRESTAMOS", 6);
            } catch (IOException ex) {
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

            ViewMain.loading.setVisible(true);

            new Thread(() -> {
                try {

                    if (componentManageLoan.textSearchLoanSoli.getText().isBlank()) {
                        ViewMain.loading.dispose();

                        JOptionPane.showMessageDialog(null, "Escriba un numero de solicitud");

                        return;
                    }

                    Optional<LoanTb> loanSearch;
                    try {
                        loanSearch = findLoan(componentManageLoan.textSearchLoanSoli.getText());
                    } catch (SQLException ex) {
                        ViewMain.loading.dispose();
                        System.out.println("Error /| " + ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Ocurrio un problema");
                        return;
                    }

                    if (!loanSearch.isPresent()) {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "No se encontró número de solicitud");
                        return;
                    }

                    // Número de solicitud detalle
                    // Nombre del empleado y DNI del empleado
                    Optional<EmployeeTb> employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());

                    Optional<EmployeeTb> employeeA = null;
                    try {
                        employeeA = new EmployeeDao().findById(loanSearch.get().getGuarantorIds());

                    } catch (Exception eee) {
                        ViewMain.loading.dispose();
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
                    ViewMain.loading.dispose();
                }
            }).start();

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

            ViewMain.loading.setVisible(true);

            new Thread(() -> {
                try {

                    Optional<EmployeeTb> employeeR = null;
                    try {
                        employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());
                    } catch (SQLException ex) {
                        ViewMain.loading.dispose();
                        //  Logger.getLogger(ControllerManageLoan.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    compromisoPago.compromisoPago(loanSearch.get().getSoliNum(),
                            employeeR.get().getFirstName().concat(" ".concat(employeeR.get().getLastName())),
                            employeeR.get().getNationalId());

                } finally {
                    ViewMain.loading.dispose();
                }
            }).start();

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
            ViewMain.loading.setVisible(true);

            new Thread(() -> {
                try {
                    Optional<EmployeeTb> employeeR = null;
                    Optional<EmployeeTb> guarantorR = null;

                    try {
                        employeeR = new EmployeeDao().findById(loanSearch.get().getEmployeeId());
                        try {
                            guarantorR = new EmployeeDao().findById(Integer.valueOf(loanSearch.get().getGuarantorIds()));
                        } catch (Exception eee) {
                            guarantorR = null;
                        }
                    } catch (SQLException ex) {

                    }
                    if (guarantorR == null) {
                        compromisoAval.compromisoPagoAval(loanSearch.get().getSoliNum(), "              ", "             ", employeeR.get().getFirstName().concat(" ".concat(employeeR.get().getLastName())), employeeR.get().getNationalId());
                        return;
                    }

                    compromisoAval.compromisoPagoAval(loanSearch.get().getSoliNum(), guarantorR.get().getFirstName().concat(" ".concat(employeeR.get().getLastName())), guarantorR.get().getNationalId(), employeeR.get().getFirstName().concat(" ".concat(employeeR.get().getLastName())), employeeR.get().getNationalId());
                } finally {
                    ViewMain.loading.dispose();

                }
            }).start();

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

            if (employeeApplicant == null || employeeAval == null
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

    }

    @Override
    public void stateChanged(ChangeEvent e
    ) {
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
    public void valueChanged(ListSelectionEvent e
    ) {
        if (e.getValueIsAdjusting()) {

            if (e.getSource().equals(componentManageLoan.jTableLoanList.getSelectionModel())) {

                int index = componentManageLoan.jTableLoanList.getSelectedRow();
                if (index != -1) {
                    // Actualiza la tabla para reflejar cualquier cambio visual
                    componentManageLoan.jTableLoanList.repaint();

                    String status = componentManageLoan.jTableLoanList.getValueAt(index, 5).toString();
                    String soliNum = componentManageLoan.jTableLoanList.getValueAt(index, 0).toString();

                    if (status.equalsIgnoreCase("Pendiente")) {
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

                        DefaultTableModel modelList = (DefaultTableModel) componentManageLoan.jTableListLoanDetails.getModel();
                        modelList.setRowCount(0);
                        DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList1.getModel();
                        model.setRowCount(0);

                        model.addRow(new Object[]{
                            componentManageLoan.jTableLoanList.getValueAt(index, 0).toString(),
                            componentManageLoan.jTableLoanList.getValueAt(index, 1).toString(),
                            componentManageLoan.jTableLoanList.getValueAt(index, 2) != null ? componentManageLoan.jTableLoanList.getValueAt(index, 2).toString() : "",
                            componentManageLoan.jTableLoanList.getValueAt(index, 3).toString(),
                            componentManageLoan.jTableLoanList.getValueAt(index, 4).toString(),
                            componentManageLoan.jTableLoanList.getValueAt(index, 5).toString(),
                            componentManageLoan.jTableLoanList.getValueAt(index, 6).toString()
                        });

                        methodListDeta(soliNum);

                        componentManageLoan.jDialog1.setModal(true);
                        componentManageLoan.jDialog1.setResizable(false);
                        componentManageLoan.jDialog1.setSize(980, 490);
                        componentManageLoan.jDialog1.setLocationRelativeTo(null);
                        componentManageLoan.jDialog1.setVisible(true);
                    }
                }

            }

            if (e.getSource().equals(componentManageLoan.jTableListLoanDetails.getSelectionModel())) {
                System.out.println("Action");
                int index = componentManageLoan.jTableListLoanDetails.getSelectedRow();

                if (index != -1) {

                    // Actualiza la tabla para reflejar cualquier cambio visual
                    componentManageLoan.jTableListLoanDetails.repaint();

                    try {

                        Optional<LoanTb> loan;
                        try {
                            String soliNum = componentManageLoan.jTableLoanList1.getValueAt(0, 0).toString();

                            loan = findLoan(soliNum);

                            List<LoanDetailsTb> listDetails = new LoanDetailsDao().findLoanDetailsByLoanId(loan.get().getId());

                            if (!componentManageLoan.jTableListLoanDetails.getValueAt(index, 4).toString().equalsIgnoreCase("Pagado")) {

                                int couta = Integer.parseInt(componentManageLoan.jTableListLoanDetails.getValueAt(index, 2).toString());

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
                                LoanDetailsTb tableDetails = listDetails.stream().filter(predicate -> predicate.getDues() == couta).findFirst().get();

                                if (!text.getText().isBlank()) {

                                    Double monto = Double.valueOf(componentManageLoan.jTableListLoanDetails.getValueAt(index, 0).toString());
                                    Double montoParcial = Double.valueOf(componentManageLoan.jTableListLoanDetails.getValueAt(index, 1).toString());
                                    Double finalMonto = Double.parseDouble(String.format("%.2f", (monto - montoParcial)));
                                    Double montoEx = Double.valueOf(text.getText());

                                    EmployeeTb empl = new EmployeeTb();
                                    if (componentManageLoan.jTableLoanList1.getValueAt(0, 6).toString().equalsIgnoreCase("AVAL")) {
                                        String name = componentManageLoan.jTableLoanList1.getValueAt(0, 2).toString();
                                        empl = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFirstName().concat(" " + predicate.getLastName()).equalsIgnoreCase(name)).findFirst().get();

                                    } else {
                                        String name = componentManageLoan.jTableLoanList1.getValueAt(0, 1).toString();
                                        empl = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFirstName().concat(" " + predicate.getLastName()).equalsIgnoreCase(name)).findFirst().get();
                                    }

                                    if (finalMonto > montoEx) {
                                        RegistroTb registroTb = new RegistroTb(empl.getEmployeeId(), montoEx);
                                        new RegistroDao().insertarRegistroCompleto(registroTb, tableDetails, null , montoEx);
                                        new LoanDetailsDao().updateLoanStateByLoandetailId(tableDetails.getId(), monto, montoEx);
                                    }
                                    if (Objects.equals(finalMonto, montoEx)) {
                                        RegistroTb registroTb = new RegistroTb(empl.getEmployeeId(), montoEx);
                                        new RegistroDao().insertarRegistroCompleto(registroTb, tableDetails, null , montoEx);
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

                                    List<Loan> listt = new LoanDao().getAllLoanss();
                                    List<Loan> listTableFind = listt.stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(soliNum)).collect(Collectors.toList());

                                    DefaultTableModel model = (DefaultTableModel) componentManageLoan.jTableLoanList1.getModel();
                                    model.setRowCount(0);

                                    model.addRow(new Object[]{
                                        listTableFind.getFirst().getSoliNum(),
                                        listTableFind.getFirst().getSolicitorName(),
                                        listTableFind.getFirst().getGuarantorName(),
                                        listTableFind.getFirst().getRequestedAmount(),
                                        listTableFind.getFirst().getAmountWithdrawn(),
                                        listTableFind.getFirst().getState(),
                                        listTableFind.getFirst().getPaymentResponsibility()
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
