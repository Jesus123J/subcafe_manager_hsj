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
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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
    private final AbonoDao abonoDao = new AbonoDao();
    private final AbonoDetailsDao abonoDetailsDao = new AbonoDetailsDao();
    protected ComponentManageBond componentManageBond;

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

        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldUnidades);
        TextFieldValidator.applyIntegerFilter(componentManageBond.jTextFieldDues);
        TextFieldValidator.applyDecimalFilter(componentManageBond.jTextFieldPrioridad);

    }

    private void insertListEmployeeCombo(List<EmployeeTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (EmployeeTb employeeTb : employeeTbs) {
            String cadena = employeeTb.getNationalId().concat(" - " + employeeTb.getFirstName().concat(employeeTb.getLastName())).toLowerCase().trim();

            if (cadena.contains(textSearch.toLowerCase().trim())) {
                comboBox.addItem(employeeTb.getNationalId() + " - " + employeeTb.getFirstName().concat(" " + employeeTb.getLastName()));
            }
        }
    }

    private void insertListServiceCombo(List<ServiceConceptTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (ServiceConceptTb serviceConceptTb : employeeTbs) {
            
            String cadena = "".concat(serviceConceptTb.getId() + " - ").concat(serviceConceptTb.getDescription()).trim();
            
            if (cadena.contains(textSearch.trim())) {
                comboBox.addItem(serviceConceptTb.getId() + " - " + serviceConceptTb.getDescription());
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
            } else if (combo.equals(componentManageBond.jComboSearchConceptBond)) {
                listConcept = null;
                listConcept = serviceConceptDao.getAllServiceConcepts();
                textSearch = ((JTextField) combo.getEditor().getEditorComponent()).getText();
                insertListServiceCombo(listConcept, combo, textSearch);
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
            serviceConceptTb.setCostPrice(Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldPrecioCosto.getText()))));
            serviceConceptTb.setSalePrice(Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldVenta.getText()))));
            serviceConceptTb.setPriority(Integer.parseInt(componentManageBond.jTextFieldPrioridad.getText()));
            serviceConceptTb.setUnid(Integer.parseInt(componentManageBond.jTextFieldUnidades.getText()));
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
            abono.setId(id);

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
                serviceConceptTb.getDescription(),
                serviceConceptTb.getUnid(),
                serviceConceptTb.getPriority(),
                serviceConceptTb.getSalePrice(),
                serviceConceptTb.getCostPrice()
            });
        }
    }
}
