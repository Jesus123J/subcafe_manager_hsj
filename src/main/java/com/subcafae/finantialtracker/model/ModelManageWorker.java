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
import com.subcafae.finantialtracker.util.NumericFilter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import org.mariadb.jdbc.util.StringUtils;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelManageWorker extends EmployeeDao {

    public ComponentManageWorker componentManageWorker;

    public ModelManageWorker(ComponentManageWorker componentManageWorker) {
        super(Conexion.getConnection());
        this.componentManageWorker = componentManageWorker;
        ((AbstractDocument) componentManageWorker.textFieldDNI.getDocument()).setDocumentFilter(new NumericFilter(8));
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
        try {

            EmployeeTb employee = new EmployeeTb(componentManageWorker.textFieldName.getText(),
                    componentManageWorker.textFieldLastName.getText(),
                    componentManageWorker.textFieldDNI.getText(),
                    componentManageWorker.textFieldPhone.getText(),
                    componentManageWorker.comboBoxSex.getSelectedIndex() != 0 ? MUJER : EmployeeTb.Gender.HOMBRE,
                    componentManageWorker.comboStatus.getSelectedIndex() != 0 ? NOMBRADO : EmployeeTb.EmploymentStatus.CAS,
                    componentManageWorker.comboStatus.getSelectedIndex() != 0 ? "2154" : "2028", componentManageWorker.dcBirthDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            create(employee);
            cleanComponent();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "DNI ya existe", "GESTIÓN TRABAJDOR", JOptionPane.INFORMATION_MESSAGE);
        }

    }

}
