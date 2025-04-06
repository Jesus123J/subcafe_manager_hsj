/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import static com.subcafae.finantialtracker.data.entity.EmployeeTb.EmploymentStatus.NOMBRADO;
import static com.subcafae.finantialtracker.data.entity.EmployeeTb.Gender.MUJER;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.util.NumericFilter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import org.mariadb.jdbc.util.StringUtils;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageWorker extends EmployeeDao {

    public ComponentManageWorker componentManageWorker;

    public ModelManageWorker(ComponentManageWorker componentManageWorker , UserTb user) {
        this.componentManageWorker = componentManageWorker;
        ((AbstractDocument) componentManageWorker.textFieldDNI.getDocument()).setDocumentFilter(new NumericFilter(8));
        ((AbstractDocument) componentManageWorker.textFieldNameDeleteUser.getDocument()).setDocumentFilter(new NumericFilter(8));
        ((AbstractDocument) componentManageWorker.textFieldPhone.getDocument()).setDocumentFilter(new NumericFilter(9));
        componentManageWorker.dcBirthDate.setDate(new Date());

    }

    public void cleanComponent() {
        componentManageWorker.textFieldDNI.setText("");
        componentManageWorker.textFieldLastName.setText("");
        componentManageWorker.textFieldName.setText("");
        componentManageWorker.textFieldPhone.setText("");
    }

    public void insertEmployee() {

        if (componentManageWorker.textFieldName.getText().isBlank()
                || componentManageWorker.textFieldLastName.getText().isBlank()
                || componentManageWorker.textFieldDNI.getText().isBlank()) {

            JOptionPane.showMessageDialog(null, "Debe de llenar los campos importantes que es nombre, apellido y DNI ", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (componentManageWorker.textFieldDNI.getText().length() != 8) {
            JOptionPane.showMessageDialog(null, "El DNI debe de contener 8 digitos ", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!componentManageWorker.textFieldPhone.getText().isBlank()) {
            if (componentManageWorker.textFieldPhone.getText().length() != 9) {
                JOptionPane.showMessageDialog(null, "El celular debe de contener 9 digitos ", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        try {

            EmployeeTb employee = new EmployeeTb(componentManageWorker.textFieldName.getText(),
                    componentManageWorker.textFieldLastName.getText(),
                    componentManageWorker.textFieldDNI.getText(),
                    componentManageWorker.textFieldPhone.getText() == null ? "" : componentManageWorker.textFieldPhone.getText(),
                    componentManageWorker.comboBoxSex.getSelectedIndex() == 0 ? "MUJER" : "HOMBRE",
                    componentManageWorker.comboStatus.getSelectedIndex() == 0 ? "NOMBRADO" : "CAS",
                    componentManageWorker.comboStatus.getSelectedIndex() == 0 ? "2154" : "2028", componentManageWorker.dcBirthDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            create(employee);
            cleanComponent();
            tableList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "DNI ya existe", "GESTIÓN TRABAJDOR", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    public void tableList() {
        try {
            DefaultTableModel model = (DefaultTableModel) componentManageWorker.jTableListEmployee.getModel();
            model.setRowCount(0);
            List<EmployeeTb> listEmployee = new EmployeeDao().findAll();
            for (EmployeeTb employeeTb : listEmployee) {
                model.addRow(new Object[]{
                    employeeTb.getFirstName(),
                    employeeTb.getLastName(),
                    employeeTb.getNationalId(),
                    employeeTb.getPhoneNumber(),
                    employeeTb.getEmploymentStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error en el listado de personas", "GESTIÓN TRABAJDOR", JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
