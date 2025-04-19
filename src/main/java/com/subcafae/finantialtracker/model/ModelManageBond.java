/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageBond {

    private final ServiceConceptDao serviceConceptDao = new ServiceConceptDao();
    protected final AbonoDao abonoDao = new AbonoDao();
    private final AbonoDetailsDao abonoDetailsDao = new AbonoDetailsDao();
    protected ComponentManageBond componentManageBond;
    protected ViewMain viewMain;
    protected UserTb user;
    protected EmployeeTb employee;
    protected ServiceConceptTb Concept;
    protected List<EmployeeTb> listEmployee;
    protected List<ServiceConceptTb> listConcept;

    public ModelManageBond(ComponentManageBond componentManageBond, UserTb user) {
        this.user = user;
        this.componentManageBond = componentManageBond;

        TextFieldValidator.applyDecimalFilter(componentManageBond.jTextFieldPrecioCosto);
        TextFieldValidator.applyDecimalFilter(componentManageBond.jTextFieldVenta);
        TextFieldValidator.applyDecimalFilter(componentManageBond.jTextFieldMonthly);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldElimanarCecepto);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldEliminarBono);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldRenunciarBono);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldSearchSoliBond);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldUnidades);
        TextFieldValidator.applyDecimalFilter(componentManageBond.jTextFieldPrioridad);

    }

    private void insertListEmployeeCombo(List<EmployeeTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (EmployeeTb employeeTb : employeeTbs) {
            String cadena = employeeTb.getNationalId().concat(" - " + employeeTb.getFullName()).toLowerCase().trim();

            if (cadena.contains(textSearch.toLowerCase().trim())) {
                comboBox.addItem(employeeTb.getNationalId() + " - " + employeeTb.getFullName());
            }
        }
    }

    private void insertListServiceCombo(List<ServiceConceptTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (ServiceConceptTb serviceConceptTb : employeeTbs) {
            String cadena = serviceConceptTb.getCodigo().concat(" - " + serviceConceptTb.getDescription()).trim();
            if (cadena.trim().toUpperCase().contains(textSearch.trim().toUpperCase())) {
                comboBox.addItem(serviceConceptTb.getCodigo() + " - " + serviceConceptTb.getDescription());
            }
        }
    }

    public void insertListCombo(JComboBox combo) {
        try {
            String textSearch = "";
            if (combo.equals(componentManageBond.jComboSearchWorker)) {
                listEmployee = null;
                listEmployee = new EmployeeDao().findAll();
                textSearch = ((JTextField) combo.getEditor().getEditorComponent()).getText();
                insertListEmployeeCombo(listEmployee, combo, textSearch);
            } else {
                listConcept = null;
                listConcept = serviceConceptDao.getAllServiceConcepts();
                textSearch = ((JTextField) combo.getEditor().getEditorComponent()).getText();
                insertListServiceCombo(listConcept, combo, textSearch);
            }

            ((JTextField) combo.getEditor().getEditorComponent()).setText(textSearch);
            combo.showPopup();

        } catch (SQLException ex) {
            System.out.println("Message" + ex.getMessage());

            JOptionPane.showMessageDialog(null, "Ocurrio un problema ", "Gestion de Prestamo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void cleanDataConceptComponent() {

        componentManageBond.jTextFieldDescription.setText("");
        componentManageBond.jTextFieldPrecioCosto.setText("");
        componentManageBond.jTextFieldVenta.setText("");
        componentManageBond.jTextFieldPrioridad.setText("");
        componentManageBond.jTextFieldUnidades.setText("");
        componentManageBond.jRadioButtonPaga.setSelected(false);
        componentManageBond.jRadioButtonSeOmitePago.setSelected(false);
    }

    public void insertServiceConepto() {
        try {
            
            ServiceConceptTb serviceConceptTb = new ServiceConceptTb();
            
            serviceConceptTb.setDescription(componentManageBond.jTextFieldDescription.getText());
            serviceConceptTb.setCostPrice(componentManageBond.jTextFieldPrecioCosto.getText().isBlank() ? 0.0 :Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldPrecioCosto.getText()))));
            serviceConceptTb.setSalePrice(componentManageBond.jTextFieldVenta.getText().isBlank() ? 0.0 : Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldVenta.getText()))));
            serviceConceptTb.setPriority(componentManageBond.jTextFieldPrioridad.getText().isBlank() ? 0 : Integer.parseInt(componentManageBond.jTextFieldPrioridad.getText()));
            serviceConceptTb.setUnid(componentManageBond.jTextFieldUnidades.getText().isBlank() ? 0 :Integer.parseInt(componentManageBond.jTextFieldUnidades.getText()));
            serviceConceptTb.setPriorityConcept(componentManageBond.jRadioButtonPaga.isSelected() ? "Primero" : "Segundo");
            serviceConceptTb.setCreatedBy(user.getId());
            serviceConceptTb.setCreatedAt(LocalDate.now().toString());

            serviceConceptDao.insert(serviceConceptTb);
            cleanDataConceptComponent();
            insertListTableConcept();

        } catch (NumberFormatException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
        }
    }

    public void insertDao(AbonoTb abono) {
        try {

            Integer id = abonoDao.insertAbono(abono);
            if (id == null) {
                return;
            }
            if (id == -1) {
                JOptionPane.showMessageDialog(null, "Ya existe un abono pendiente. No se puede registrar otro.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            abono.setId(id);
            JOptionPane.showMessageDialog(null, "Se registró el abono correctamente.",
                    "GESTIÓN DE ABONOS", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Abono -> " + abono.toString());

            abonoDetailsDao.insertAbonoDetail(abono, user.getId());

        } catch (SQLException ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
        }
    }

    public void insertListTableConcept() {

        try {
            listConcept = serviceConceptDao.getAllServiceConcepts();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
        }
        DefaultTableModel model = ((DefaultTableModel) componentManageBond.jTableListConcept.getModel());
        model.setRowCount(0);
        for (ServiceConceptTb serviceConceptTb : listConcept) {
            model.addRow(new Object[]{
                serviceConceptTb.getCodigo(),
                serviceConceptTb.getDescription(),
                serviceConceptTb.getUnid(),
                serviceConceptTb.getPriority(),
                serviceConceptTb.getSalePrice(),
                serviceConceptTb.getCostPrice()
            });
        }
    }

    public void insertListTableBono(Date start , Date finaly) {
        try {
            ViewMain.loading.setVisible(true);
            List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();
            List<AbonoTb> listAbono = abonoDao.findAllAbonos(new java.sql.Date(start.getTime()), new java.sql.Date(finaly.getTime()));
            DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListBonos.getModel();
            model.setRowCount(0);
            for (AbonoTb abonoTb : listAbono) {
                model.addRow(new Object[]{
                    abonoTb.getSoliNum(),
                    listServi.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get().getDescription(),
                    new EmployeeDao().findById(Integer.valueOf(abonoTb.getEmployeeId())).map(mapper -> mapper.getFullName()).get(),
                    abonoTb.getDues(),
                    abonoTb.getMonthly(),
                    abonoTb.getStatus()
                });
            }
            ViewMain.loading.dispose();
        } catch (SQLException ex) {
            ViewMain.loading.dispose();
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
        }
    }
}
