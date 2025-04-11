/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.controller;

import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.UserDao;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.User;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.view.component.ComponentManageUser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class ControllerUser implements ActionListener {

    private List<EmployeeTb> listEmployee;
    private ComponentManageUser componentUser;
    private EmployeeTb emplo;
    private UserTb user;

    public ControllerUser(ComponentManageUser componentManageUser, UserTb user) {

        this.componentUser = componentManageUser;
        this.user = user;

        componentUser.jButtonRegisterUser.addActionListener(this);
        componentUser.jButtonBloquiarUser.addActionListener(this);

        componentUser.jComboBoxBuscarEmpleado.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (componentUser.jComboBoxBuscarEmpleado.getSelectedIndex() != -1) {
                        try {
                            emplo = listEmployee.stream().filter(predicate -> predicate.getNationalId().trim().equals(((JTextField) componentUser.jComboBoxBuscarEmpleado.getEditor().getEditorComponent()).getText().split("-")[0].trim())).findFirst().get();
                            if (emplo != null) {
                                componentManageUser.jLabelDni.setText(emplo.getNationalId());
                                componentManageUser.jLabelName.setText(emplo.getFullName());
                            } else {
                                componentManageUser.jLabelDni.setText("");
                                componentManageUser.jLabelName.setText("");
                            }

                        } catch (Exception ee) {
                            emplo = null;
                            componentManageUser.jLabelDni.setText("");
                            componentManageUser.jLabelName.setText("");
                        }

                    } else {
                        emplo = null;
                        componentManageUser.jLabelDni.setText("");
                        componentManageUser.jLabelName.setText("");
                    }
                }
                if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z) // Letras
                        || (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) // Números
                        || (e.getKeyCode() >= KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                        || e.getKeyCode() == KeyEvent.VK_BACK_SPACE // Borrar
                        || e.getKeyCode() == KeyEvent.VK_SPACE) { //

                    componentManageUser.jLabelDni.setText("");
                    componentManageUser.jLabelName.setText("");
                    insertListCombo(componentUser.jComboBoxBuscarEmpleado);

                }
            }

        });

        clear();
        listTable();
    }

    public void listTable() {

        List<User> list = new UserDao().getAllUsers();
        DefaultTableModel model = (DefaultTableModel) componentUser.jTableListUse.getModel();
        model.setRowCount(0);

        for (User user1 : list) {

            model.addRow(new Object[]{
                user1.getUsername(),
                user1.getEmployeeName(),
                user1.getRol(),
                user1.getState().equalsIgnoreCase("1") ? "ACTIVADO" : "BLOQUIADO"
            });
        }
    }

    public void insertListCombo(JComboBox combo) {
        try {
            String textSearch = "";
            listEmployee = null;
            listEmployee = new EmployeeDao().findAll();
            textSearch = ((JTextField) combo.getEditor().getEditorComponent()).getText();
            insertListEmployeeCombo(listEmployee, combo, textSearch);

            ((JTextField) combo.getEditor().getEditorComponent()).setText(textSearch);
            combo.showPopup();

        } catch (SQLException ex) {
            System.out.println("Message" + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema ", "Gestion de Prestamo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void insertListEmployeeCombo(List<EmployeeTb> employeeTbs, JComboBox comboBox, String textSearch) {
        comboBox.removeAllItems();
        for (EmployeeTb employeeTb : employeeTbs) {
            String cadena = employeeTb.getNationalId().concat(" - " + employeeTb.getFullName().toLowerCase().trim());

            if (cadena.contains(textSearch.toLowerCase().trim())) {
                comboBox.addItem(employeeTb.getNationalId() + " - " + employeeTb.getFullName());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(componentUser.jButtonBloquiarUser)) {
            if (componentUser.jTextFieldUserBlock.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "LLENE CON EL NOMBRE DE USUARIO", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            boolean ver = new UserDao().toggleUserState(componentUser.jTextFieldUserBlock.getText());
            if (!ver) {
                JOptionPane.showMessageDialog(null, "NO SE LOGRO CAMBIAR", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
            }
            if (ver) {
                JOptionPane.showMessageDialog(null, "SE CAMBIO DE ESTADO", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
            }
            componentUser.jTextFieldUserBlock.setText("");
            listTable();
        }
        
        if (e.getSource().equals(componentUser.jButtonRegisterUser)) {
            if (componentUser.jTextFieldUser.getText().isBlank()
                    || new String(componentUser.jPasswordFieldEscribaContrase.getPassword()).isBlank()
                    || new String(componentUser.jPasswordFieldRepitaContrase.getPassword()).isBlank()) {
                JOptionPane.showMessageDialog(null, "LLENE TODOS LOS DATOS", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (!new String(componentUser.jPasswordFieldEscribaContrase.getPassword()).equalsIgnoreCase(new String(componentUser.jPasswordFieldRepitaContrase.getPassword()))) {
                JOptionPane.showMessageDialog(null, "LAS CONTRASEÑAS NO SON LAS MISMAS", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (emplo == null) {
                JOptionPane.showMessageDialog(null, "BUSQUE EMPLEADO", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            boolean userr = new UserDao().createUser(componentUser.jTextFieldUser.getText(), new String(componentUser.jPasswordFieldEscribaContrase.getPassword()), emplo.getEmployeeId(), componentUser.jComboBoxState.getSelectedItem().toString());

            if (userr) {
                JOptionPane.showMessageDialog(null, "SE REGISTRO EL USURIO", "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
            }
            listTable();
            clear();
        }
    }

    public void clear() {
        componentUser.jTextFieldUser.setText("");
        componentUser.jPasswordFieldEscribaContrase.setText("");
        componentUser.jPasswordFieldRepitaContrase.setText("");
        ((JTextField) componentUser.jComboBoxBuscarEmpleado.getEditor().getEditorComponent()).setText("");
        emplo = null;
    }
}
