/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.dao.RegistroDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.Loan;
import com.subcafae.finantialtracker.data.entity.RegistroTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.model.ModelManageBond;
import com.subcafae.finantialtracker.report.bond.LeerExcelConFileChooser;
import com.subcafae.finantialtracker.report.bond.ReporteAbono;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
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
public class ControllerManageBond extends ModelManageBond implements ActionListener, KeyListener, ChangeListener, ListSelectionListener {

    public ControllerManageBond(ComponentManageBond componentManageBond, UserTb user, ViewMain viewMain) {

        super(componentManageBond, user);
        this.viewMain = viewMain;
        componentManageBond.jComboSearchWorker.getEditor().getEditorComponent().addKeyListener(this);
        componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent().addKeyListener(this);
//        componentManageBond.jButtonCheck.addActionListener(this);
//        componentManageBond.jButtonClean.addActionListener(this);
        componentManageBond.jButtonReportBond.addActionListener(this);
        componentManageBond.jButtonRegisterBond.addActionListener(this);
        componentManageBond.jButtonRegistroConcepto.addActionListener(this);
        componentManageBond.jTabbedPane1.addChangeListener(this);
        componentManageBond.jButtonEliminarConcept.addActionListener(this);
        componentManageBond.jButtonEliminarBono.addActionListener(this);
        componentManageBond.jButtonRenunciarBono.addActionListener(this);
        componentManageBond.jButton1.addActionListener(this);
        componentManageBond.jTableListBonos.getSelectionModel().addListSelectionListener(this);
        componentManageBond.jTableListDetalle.getSelectionModel().addListSelectionListener(this);
        componentManageBond.jButtonRegistorAbondForExcel.addActionListener(this);
        componentManageBond.jButtonShowList.addActionListener(this);

        // KeyListeners para autocompletado
        componentManageBond.jTextFieldSearchSoliBond.addKeyListener(this);
        componentManageBond.jTextFieldEliminarBono.addKeyListener(this);
        componentManageBond.jTextFieldRenunciarBono.addKeyListener(this);
        componentManageBond.searchConcept.addKeyListener(this);

//        componentManageBond.jDialog1.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                if (componentManageBond.jTableListBonos.getRowCount() == 1) {
//                    try {
//                        List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();
//                        List<AbonoTb> listAbono = abonoDao.findAllAbonos().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(componentManageBond.jTableListBonos.getValueAt(0, 0).toString())).collect(Collectors.toList());
//               
//                        DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListBonos.getModel();
//                        model.setRowCount(0);
//                        for (AbonoTb abonoTb : listAbono) {
//                            model.addRow(new Object[]{
//                                abonoTb.getSoliNum(),
//                                listServi.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get().getDescription(),
//                                new EmployeeDao().findById(Integer.valueOf(abonoTb.getEmployeeId())).map(mapper -> mapper.getFullName()).get(),
//                                abonoTb.getDues(),
//                                abonoTb.getMonthly(),
//                                abonoTb.getStatus()
//                            });
//                        }
//                    } catch (SQLException ex) {
//                        //Logger.getLogger(ControllerManageBond.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                } else {
//                    insertListTableBono();
//                }
//            }
//        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentManageBond.jButtonRegistorAbondForExcel)) {
            localDate = componentManageBond.dateStart1.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            new LeerExcelConFileChooser().method(user, super.viewMain, localDate);
        }
        if (e.getSource().equals(componentManageBond.jButtonShowList)) {
            try {
                if (componentManageBond.dateStart.getDate() != null && componentManageBond.dateFinaly.getDate() != null) {
                    insertListTableBono(componentManageBond.dateStart.getDate(), componentManageBond.dateFinaly.getDate());
                } else {
                    // Si no hay fechas, mostrar los ultimos 100 registros
                    insertListTableBonoLast(100);
                }
            } catch (Exception ee) {
                System.out.println("Error -> " + ee.getMessage());
                // Si hay error, mostrar los ultimos 100 registros
                insertListTableBonoLast(100);
            }
        }
        if (e.getSource().equals(componentManageBond.jButton1)) {
            if (componentManageBond.jTextFieldSearchSoliBond.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el número de solicitud ", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String numSolicitud = componentManageBond.jTextFieldSearchSoliBond.getText();

            ViewMain.loading.setModal(true);
            ViewMain.loading.setLocationRelativeTo(viewMain);

            new Thread(() -> {
                try {
                    List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();
                    List<AbonoTb> listAbono = abonoDao.findAllAbonos().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(numSolicitud)).collect(Collectors.toList());

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (listAbono.isEmpty()) {
                            ViewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "No se encontró número de solicitud ", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListBonos.getModel();
                        model.setRowCount(0);
                        for (AbonoTb abonoTb : listAbono) {
                            try {
                                model.addRow(new Object[]{
                                    abonoTb.getSoliNum(),
                                    listServi.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get().getDescription(),
                                    new EmployeeDao().findById(Integer.valueOf(abonoTb.getEmployeeId())).map(mapper -> mapper.getFullName()).get(),
                                    abonoTb.getDues(),
                                    abonoTb.getMonthly(),
                                    abonoTb.getStatus()
                                });
                            } catch (Exception ex) {
                                System.out.println("Error al agregar fila: " + ex.getMessage());
                            }
                        }
                        ViewMain.loading.dispose();
                    });
                } catch (Exception ex) {
                    System.out.println("Error -> " + ex.getMessage());
                    ex.printStackTrace();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrió un problema: " + ex.getMessage(), "GESTIÓN ABONOS", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

            ViewMain.loading.setVisible(true);
        }
        if (e.getSource().equals(componentManageBond.jButtonEliminarBono)) {
            if (componentManageBond.jTextFieldEliminarBono.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el número de solicitud del bono para eliminar", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Confirmacion antes de eliminar
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar el bono con número de solicitud: " + componentManageBond.jTextFieldEliminarBono.getText() + "?",
                "CONFIRMAR ELIMINACIÓN", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                boolean verr = new AbonoDao().deleteAbonoIfNotUsed(componentManageBond.jTextFieldEliminarBono.getText());
                if (verr) {
                    JOptionPane.showMessageDialog(null, "Se elimino el abono", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                    componentManageBond.jTextFieldEliminarBono.setText("");
                    reloadSoliNumsAbono();
                }
                return;
            } catch (SQLException ex) {
                System.out.println("Error -> " + ex);
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        if (e.getSource().equals(componentManageBond.jButtonRenunciarBono)) {
            if (componentManageBond.jTextFieldRenunciarBono.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el número de solicitud del bono para renunciar", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // Confirmacion antes de renunciar
            int confirm = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea renunciar al bono con número de solicitud: " + componentManageBond.jTextFieldRenunciarBono.getText() + "?",
                "CONFIRMAR RENUNCIA", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                new AbonoDao().renounceAbono(componentManageBond.jTextFieldRenunciarBono.getText(), user.getId(), LocalDate.now().toString());
                componentManageBond.jTextFieldRenunciarBono.setText("");
                reloadSoliNumsAbono();
            } catch (SQLException ex) {
                System.out.println("Error -> " + ex);
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        if (e.getSource().equals(componentManageBond.jButtonEliminarConcept)) {

            if (componentManageBond.jTextFieldElimanarCecepto.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el codigo del concepto para eliminar", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                if (new ServiceConceptDao().deleteServiceConceptIfNotUsed(componentManageBond.jTextFieldElimanarCecepto.getText())) {
                    JOptionPane.showMessageDialog(null, "Se elimino el concepto", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                    insertListTableConcept();
                    return;
                }
            } catch (SQLException ex) {

                System.out.println("Error -> " + ex);
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
                // Logger.getLogger(ControllerManageBond.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (e.getSource().equals(componentManageBond.jButtonReportBond)) {

            if (componentManageBond.searchConcept.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Escriba el nombre del concepto", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            ReporteAbono reporteAbono = new ReporteAbono();
            reporteAbono.searchService(componentManageBond.searchConcept.getText());

        }
        if (e.getSource().equals(componentManageBond.jButtonRegistroConcepto)) {
            if (componentManageBond.jTextFieldDescription.getText().isBlank() //                    || componentManageBond.jTextFieldVenta.getText().isBlank()
                    //                    || componentManageBond.jTextFieldPrecioCosto.getText().isBlank()
                    //                    || componentManageBond.jTextFieldPrioridad.getText().isBlank()
                    //                    || componentManageBond.jTextFieldUnidades.getText().isBlank()
                    ) {

                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }
            if (!componentManageBond.jRadioButtonPaga.isSelected() && !componentManageBond.jRadioButtonSeOmitePago.isSelected()) {
                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }

            insertServiceConepto();

        }

        if (e.getSource().equals(componentManageBond.jButtonRegisterBond)) {

            if ((employee == null && Concept == null)
                    || componentManageBond.jTextFieldMonthly.getText().isBlank()) {

                JOptionPane.showMessageDialog(null, "Complete todos los datos para registrar", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                return;
            }

            localDate = componentManageBond.dateStart1.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            AbonoTb abono = new AbonoTb();

            System.out.println("Concept -> " + Concept.getId());
            abono.setDues(Integer.parseInt(componentManageBond.jTextFieldDues.getSelectedItem().toString()));
            abono.setEmployeeId(String.valueOf(employee.getEmployeeId()));
            abono.setCreatedAt(localDate.toString());
            abono.setCreatedBy(user.getId());
            abono.setDiscountFrom(componentManageBond.jComboDescont.getSelectedItem().toString());
            abono.setServiceConceptId(String.valueOf(Concept.getId()));

            abono.setMonthly(Double.valueOf(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldMonthly.getText()))));
            abono.setPaymentDate(localDate.withDayOfMonth(localDate.lengthOfMonth()).toString());
            abono.setStatus("Pendiente");

            //liempiar los componentes de clean
            Concept = null;
            employee = null;

            ((JTextField) componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent()).setText("");
            ((JTextField) componentManageBond.jComboSearchWorker.getEditor().getEditorComponent()).setText("");
            componentManageBond.jLabelCodeWorker.setText("");
            componentManageBond.jLabelShowDNIWorker.setText("");
            componentManageBond.jLabelShowDNIWorker1.setText("");
            componentManageBond.jTextFieldMonthly.setText("");

            insertDao(abono);

        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getSource().equals(componentManageBond.jComboSearchWorker.getEditor().getEditorComponent())) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (componentManageBond.jComboSearchWorker.getSelectedIndex() != -1) {
                    try {
                        employee = listEmployee.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentManageBond.jComboSearchWorker.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();
                    } catch (Exception eee) {
                        employee = null;
                    }
                    if (employee == null) {
                        return;
                    }
                    componentManageBond.jLabelCodeWorker.setText(employee.getEmploymentStatusCode());
                    componentManageBond.jLabelShowDNIWorker.setText(employee.getNationalId());

                }
            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {
                insertListCombo(componentManageBond.jComboSearchWorker);
            }

        }

        if (e.getSource().equals(componentManageBond.jComboBoxSearchConceptBond.getEditor().getEditorComponent())) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (componentManageBond.jComboBoxSearchConceptBond.getSelectedIndex() != -1) {
                    try {
                        String[] cadena = componentManageBond.jComboBoxSearchConceptBond.getSelectedItem().toString().split(" - ");
                        Concept = listConcept.stream().filter(predicate -> predicate.getCodigo().equalsIgnoreCase(cadena[0])).findFirst().get();
                    } catch (Exception eee) {
                        Concept = null;
                    }
                    if (Concept == null) {
                        return;
                    }
                    componentManageBond.jLabelShowDNIWorker1.setText(Concept.getCodigo() + " - " + Concept.getDescription());
                }

            }

            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {
                insertListCombo(componentManageBond.jComboBoxSearchConceptBond);
            }

        }

        // 
        // if (e.getSource().equals(componentManageBond.)) {
        // }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Autocompletado para jTextFieldSearchSoliBond
        if (e.getSource().equals(componentManageBond.jTextFieldSearchSoliBond)) {
            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                showAutocompleteSearchSoliBond();
            }
        }
        // Autocompletado para jTextFieldEliminarBono
        if (e.getSource().equals(componentManageBond.jTextFieldEliminarBono)) {
            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                showAutocompleteEliminarBono();
            }
        }
        // Autocompletado para jTextFieldRenunciarBono
        if (e.getSource().equals(componentManageBond.jTextFieldRenunciarBono)) {
            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                showAutocompleteRenunciarBono();
            }
        }
        // Autocompletado para searchConcept
        if (e.getSource().equals(componentManageBond.searchConcept)) {
            if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z)
                    || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9)
                    || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9)
                    || e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_SPACE) {
                showAutocompleteSearchConcept();
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e
    ) {

        switch (componentManageBond.jTabbedPane1.getSelectedIndex()) {
            case 0:

                break;
            case 1:
                insertListTableConcept();
                break;

            case 2:
                //  insertListTableBono();
                break;
        }

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

        if (e.getValueIsAdjusting()) {

            if (e.getSource().equals(componentManageBond.jTableListDetalle.getSelectionModel())) {

                int indexDetalle = componentManageBond.jTableListDetalle.getSelectedRow();

                if (indexDetalle != -1) {

                    componentManageBond.jTableListDetalle.repaint();

                    String numSol = componentManageBond.jTableListEmple.getValueAt(0, 0).toString();

                    try {
                        Optional<AbonoTb> encontrado = new AbonoDao().findAllAbonos().stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(numSol)).findFirst();

                        if (encontrado.isPresent()) {

                            if (encontrado.get().getStatus().equalsIgnoreCase("Pendiente")) {

                                List<AbonoDetailsTb> bonodetails = new AbonoDao().getListAbonoBySoli(numSol);

                                if (!bonodetails.stream().filter(predicate -> predicate.getDues() == Integer.parseInt(componentManageBond.jTableListDetalle.getValueAt(indexDetalle, 2).toString())).findFirst().get().getState().equalsIgnoreCase("Pagado")) {

                                    JTextField text = new JTextField();
                                    Border bordeConTitulo = BorderFactory.createTitledBorder("Escriba el monto");
                                    text.setBorder(bordeConTitulo);

                                    TextFieldValidator.applyDecimalFilter(text);

                                    int opction = JOptionPane.showConfirmDialog(null, text, "INFORMACION", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                    if (opction != JOptionPane.OK_OPTION) {
                                        return;
                                    }

                                    if (!text.getText().isBlank()) {

                                        AbonoDetailsTb elecD = bonodetails.stream().filter(predicate -> predicate.getDues() == Integer.parseInt(componentManageBond.jTableListDetalle.getValueAt(indexDetalle, 2).toString())).findFirst().get();

                                        Double monto = elecD.getMonthly();
                                        Double parcial = elecD.getPayment();

                                        Double finalMonto = Double.valueOf(String.format("%.2f", (monto - parcial)));
                                        Double montoEx = Double.valueOf(text.getText());

                                        if (finalMonto < montoEx) {
                                            JOptionPane.showMessageDialog(null, "El monto ingresado es mayor al monto pendiente", "GESTION DE BONO", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }

                                        // Mostrar loading y deshabilitar dialog
                                        componentManageBond.jDialog1.setEnabled(false);
                                        ViewMain.loading.setModal(true);
                                        ViewMain.loading.setLocationRelativeTo(componentManageBond.jDialog1);

                                        final String numSolFinal = numSol;
                                        final AbonoDetailsTb elecDFinal = elecD;
                                        final Double montoFinal = monto;
                                        final Double montoExFinal = montoEx;

                                        new Thread(() -> {
                                            try {
                                                String name = componentManageBond.jTableListEmple.getValueAt(0, 2).toString();
                                                EmployeeTb empl = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getFullName().equalsIgnoreCase(name)).findFirst().get();
                                                RegistroTb registroTb = new RegistroTb(empl.getEmployeeId(), montoExFinal);

                                                new RegistroDao().insertarRegistroCompleto(registroTb, null, elecDFinal, montoExFinal);
                                                new AbonoDetailsDao().updateLoanStateByLoandetailId(elecDFinal.getId(), montoFinal, montoExFinal);
                                                System.out.println("Se registro");

                                                javax.swing.SwingUtilities.invokeLater(() -> {
                                                    try {
                                                        DefaultTableModel modelFirts = (DefaultTableModel) componentManageBond.jTableListEmple.getModel();
                                                        modelFirts.setRowCount(0);
                                                        List<AbonoTb> list = abonoDao.findAllAbonos();
                                                        List<AbonoTb> listFind = list.stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(numSolFinal)).collect(Collectors.toList());

                                                        List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();

                                                        modelFirts.addRow(new Object[]{
                                                            listFind.get(0).getSoliNum(),
                                                            listServi.stream().filter(predicate -> predicate.getId() == Integer.parseInt(listFind.get(0).getServiceConceptId())).findFirst().get().getDescription(),
                                                            new EmployeeDao().findById(Integer.valueOf(listFind.get(0).getEmployeeId())).map(mapper -> mapper.getFullName()).get(),
                                                            listFind.get(0).getDues(),
                                                            listFind.get(0).getMonthly(),
                                                            listFind.get(0).getStatus()
                                                        });

                                                        DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListDetalle.getModel();
                                                        model.setRowCount(0);

                                                        for (AbonoDetailsTb bonodetail : new AbonoDao().getListAbonoBySoli(numSolFinal)) {
                                                            model.addRow(new Object[]{
                                                                bonodetail.getMonthly(),
                                                                bonodetail.getPayment(),
                                                                bonodetail.getDues(),
                                                                bonodetail.getPaymentDate(),
                                                                bonodetail.getState()
                                                            });
                                                        }

                                                        componentManageBond.jTableListDetalle.repaint();
                                                    } catch (Exception ex) {
                                                        System.out.println("Error actualizando tabla: " + ex.getMessage());
                                                    } finally {
                                                        componentManageBond.jDialog1.setEnabled(true);
                                                        ViewMain.loading.dispose();
                                                    }
                                                });
                                            } catch (Exception ex) {
                                                System.out.println("Error -> " + ex.getMessage());
                                                javax.swing.SwingUtilities.invokeLater(() -> {
                                                    componentManageBond.jDialog1.setEnabled(true);
                                                    ViewMain.loading.dispose();
                                                    JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTION DE BONO", JOptionPane.ERROR_MESSAGE);
                                                });
                                            }
                                        }).start();

                                        ViewMain.loading.setVisible(true);
                                        return;

                                    }

                                }

                            } else if (encontrado.get().getStatus().equalsIgnoreCase("Pagado")) {
                                JOptionPane.showMessageDialog(null, "ABONO CON ESTATUS PAGADO", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                                return;
                            } else if (encontrado.get().getStatus().equalsIgnoreCase("REN")) {
                                JOptionPane.showMessageDialog(null, "ABONO CON ESTATUS RENUNCIADO", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);
                                return;
                            }

                        }

                    } catch (SQLException ex) {

                    }

                }
            }

            if (e.getSource().equals(componentManageBond.jTableListBonos.getSelectionModel())) {

                int index = componentManageBond.jTableListBonos.getSelectedRow();
                System.out.println("Se preciono");

                if (index != -1) {

                    componentManageBond.jTableListBonos.repaint();

                    String numSol = componentManageBond.jTableListBonos.getValueAt(index, 0).toString();

                    try {
                        List<AbonoDetailsTb> bonodetails = new AbonoDao().getListAbonoBySoli(numSol);
                        DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListDetalle.getModel();

                        DefaultTableModel modelFirts = (DefaultTableModel) componentManageBond.jTableListEmple.getModel();
                        modelFirts.setRowCount(0);
                        model.setRowCount(0);

                        modelFirts.addRow(new Object[]{
                            componentManageBond.jTableListBonos.getValueAt(index, 0).toString(),
                            componentManageBond.jTableListBonos.getValueAt(index, 1).toString(),
                            componentManageBond.jTableListBonos.getValueAt(index, 2).toString(),
                            componentManageBond.jTableListBonos.getValueAt(index, 3).toString(),
                            componentManageBond.jTableListBonos.getValueAt(index, 4).toString(),
                            componentManageBond.jTableListBonos.getValueAt(index, 5).toString()});
//                                model.addRow(new Object[]{
//                    abonoTb.getSoliNum(),
//                    listServi.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get().getDescription(),
//                    new EmployeeDao().findById(Integer.valueOf(abonoTb.getEmployeeId())).map(mapper -> mapper.getFirstName().concat(" " + mapper.getLastName())).get(),
//                    abonoTb.getDues(),
//                    abonoTb.getMonthly(),
//                    abonoTb.getStatus()
//                });
                        for (AbonoDetailsTb bonodetail : bonodetails) {
                            model.addRow(new Object[]{
                                bonodetail.getMonthly(),
                                bonodetail.getPayment(),
                                bonodetail.getDues(),
                                bonodetail.getPaymentDate(),
                                bonodetail.getState()
                            });
                        }
                        componentManageBond.jDialog1.setSize(980, 530);
                        componentManageBond.jDialog1.setResizable(false);
                        componentManageBond.jDialog1.setLocationRelativeTo(null);
                        componentManageBond.jDialog1.setModal(true);
                        componentManageBond.jDialog1.setVisible(true);

                        componentManageBond.jDialog1.toFront();

                    } catch (SQLException ex) {
                        System.out.println("Error -> " + ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE BONO", JOptionPane.OK_OPTION);

                    }

                }
            }
        }
    }

}
