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
import java.sql.Date;
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

    // Variables para modo edición
    protected boolean isEditMode = false;
    protected String editingEmployeeDni = null;
    protected int editingRowIndex = -1;

    // Variables para autocompletado
    protected List<String> listEmployeeDniNames;
    protected javax.swing.JPopupMenu popupBuscarDni;
    protected javax.swing.JList<String> listaSugerenciasBuscarDni;
    protected javax.swing.DefaultListModel<String> modeloListaBuscarDni;

    protected javax.swing.JPopupMenu popupEliminar;
    protected javax.swing.JList<String> listaSugerenciasEliminar;
    protected javax.swing.DefaultListModel<String> modeloListaEliminar;

    public ModelManageWorker(ComponentManageWorker componentManageWorker, UserTb user) {
        this.componentManageWorker = componentManageWorker;
        ((AbstractDocument) componentManageWorker.textFieldDNI.getDocument()).setDocumentFilter(new NumericFilter(8));

        componentManageWorker.dcBirthDate.setDate(new java.util.Date());

        // Inicializar autocompletados
        initAutocompleteBuscarDni();
        initAutocompleteEliminar();

        // Cargar lista inicial de empleados
        loadEmployeeList();
    }

    // Cargar lista de empleados para autocompletado
    protected void loadEmployeeList() {
        listEmployeeDniNames = getAllEmployeeDniNames();
    }

    // Inicializar autocompletado para jTextFieldBuscarForDni
    private void initAutocompleteBuscarDni() {
        popupBuscarDni = new javax.swing.JPopupMenu();
        modeloListaBuscarDni = new javax.swing.DefaultListModel<>();
        listaSugerenciasBuscarDni = new javax.swing.JList<>(modeloListaBuscarDni);
        listaSugerenciasBuscarDni.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasBuscarDni.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasBuscarDni);
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 150));
        popupBuscarDni.add(scrollPane);
        popupBuscarDni.setFocusable(false);

        // Click para seleccionar
        listaSugerenciasBuscarDni.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasBuscarDni.getSelectedValue();
                    if (selected != null) {
                        String dni = selected.split(" - ")[0].trim();
                        componentManageWorker.jTextFieldBuscarForDni.setText(dni);
                        popupBuscarDni.setVisible(false);
                        componentManageWorker.jTextFieldBuscarForDni.requestFocusInWindow();
                    }
                }
            }
        });

        // Teclas para navegación
        componentManageWorker.jTextFieldBuscarForDni.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                handlePopupNavigationBuscarDni(e);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if ((e.getKeyCode() >= java.awt.event.KeyEvent.VK_A && e.getKeyCode() <= java.awt.event.KeyEvent.VK_Z)
                        || (e.getKeyCode() >= java.awt.event.KeyEvent.VK_0 && e.getKeyCode() <= java.awt.event.KeyEvent.VK_9)
                        || (e.getKeyCode() >= java.awt.event.KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= java.awt.event.KeyEvent.VK_NUMPAD9)
                        || e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    showAutocompleteBuscarDni();
                }
            }
        });
    }

    // Inicializar autocompletado para textFieldNameDeleteUser
    private void initAutocompleteEliminar() {
        popupEliminar = new javax.swing.JPopupMenu();
        modeloListaEliminar = new javax.swing.DefaultListModel<>();
        listaSugerenciasEliminar = new javax.swing.JList<>(modeloListaEliminar);
        listaSugerenciasEliminar.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasEliminar.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasEliminar);
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 150));
        popupEliminar.add(scrollPane);
        popupEliminar.setFocusable(false);

        // Click para seleccionar
        listaSugerenciasEliminar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasEliminar.getSelectedValue();
                    if (selected != null) {
                        String dni = selected.split(" - ")[0].trim();
                        componentManageWorker.textFieldNameDeleteUser.setText(dni);
                        popupEliminar.setVisible(false);
                        componentManageWorker.textFieldNameDeleteUser.requestFocusInWindow();
                    }
                }
            }
        });

        // Teclas para navegación
        componentManageWorker.textFieldNameDeleteUser.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                handlePopupNavigationEliminar(e);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if ((e.getKeyCode() >= java.awt.event.KeyEvent.VK_A && e.getKeyCode() <= java.awt.event.KeyEvent.VK_Z)
                        || (e.getKeyCode() >= java.awt.event.KeyEvent.VK_0 && e.getKeyCode() <= java.awt.event.KeyEvent.VK_9)
                        || (e.getKeyCode() >= java.awt.event.KeyEvent.VK_NUMPAD0 && e.getKeyCode() <= java.awt.event.KeyEvent.VK_NUMPAD9)
                        || e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    showAutocompleteEliminar();
                }
            }
        });
    }

    // Mostrar autocompletado para buscar DNI
    protected void showAutocompleteBuscarDni() {
        String text = componentManageWorker.jTextFieldBuscarForDni.getText().trim();
        if (text.isEmpty()) {
            popupBuscarDni.setVisible(false);
            return;
        }

        if (listEmployeeDniNames == null || listEmployeeDniNames.isEmpty()) {
            loadEmployeeList();
        }

        modeloListaBuscarDni.clear();
        int count = 0;
        for (String item : listEmployeeDniNames) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                modeloListaBuscarDni.addElement(item);
                count++;
                if (count >= 10) break;
            }
        }

        if (count > 0) {
            listaSugerenciasBuscarDni.setSelectedIndex(0);
            if (!popupBuscarDni.isVisible()) {
                popupBuscarDni.show(componentManageWorker.jTextFieldBuscarForDni, 0, componentManageWorker.jTextFieldBuscarForDni.getHeight());
            }
            componentManageWorker.jTextFieldBuscarForDni.requestFocusInWindow();
        } else {
            popupBuscarDni.setVisible(false);
        }
    }

    // Mostrar autocompletado para eliminar
    protected void showAutocompleteEliminar() {
        String text = componentManageWorker.textFieldNameDeleteUser.getText().trim();
        if (text.isEmpty()) {
            popupEliminar.setVisible(false);
            return;
        }

        if (listEmployeeDniNames == null || listEmployeeDniNames.isEmpty()) {
            loadEmployeeList();
        }

        modeloListaEliminar.clear();
        int count = 0;
        for (String item : listEmployeeDniNames) {
            if (item.toLowerCase().contains(text.toLowerCase())) {
                modeloListaEliminar.addElement(item);
                count++;
                if (count >= 10) break;
            }
        }

        if (count > 0) {
            listaSugerenciasEliminar.setSelectedIndex(0);
            if (!popupEliminar.isVisible()) {
                popupEliminar.show(componentManageWorker.textFieldNameDeleteUser, 0, componentManageWorker.textFieldNameDeleteUser.getHeight());
            }
            componentManageWorker.textFieldNameDeleteUser.requestFocusInWindow();
        } else {
            popupEliminar.setVisible(false);
        }
    }

    // Navegación popup buscar DNI
    private void handlePopupNavigationBuscarDni(java.awt.event.KeyEvent e) {
        int index = listaSugerenciasBuscarDni.getSelectedIndex();
        int size = modeloListaBuscarDni.getSize();

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && popupBuscarDni.isVisible()) {
            if (size > 0) {
                int newIndex = (index < size - 1) ? index + 1 : 0;
                listaSugerenciasBuscarDni.setSelectedIndex(newIndex);
                listaSugerenciasBuscarDni.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && popupBuscarDni.isVisible()) {
            if (size > 0) {
                int newIndex = (index > 0) ? index - 1 : size - 1;
                listaSugerenciasBuscarDni.setSelectedIndex(newIndex);
                listaSugerenciasBuscarDni.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (popupBuscarDni.isVisible()) {
                String selected = listaSugerenciasBuscarDni.getSelectedValue();
                if (selected != null) {
                    String dni = selected.split(" - ")[0].trim();
                    componentManageWorker.jTextFieldBuscarForDni.setText(dni);
                }
                popupBuscarDni.setVisible(false);
            }
            // Ejecutar búsqueda
            componentManageWorker.jButtoBuscarDni.doClick();
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE && popupBuscarDni.isVisible()) {
            popupBuscarDni.setVisible(false);
            e.consume();
        }
    }

    // Navegación popup eliminar
    private void handlePopupNavigationEliminar(java.awt.event.KeyEvent e) {
        int index = listaSugerenciasEliminar.getSelectedIndex();
        int size = modeloListaEliminar.getSize();

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && popupEliminar.isVisible()) {
            if (size > 0) {
                int newIndex = (index < size - 1) ? index + 1 : 0;
                listaSugerenciasEliminar.setSelectedIndex(newIndex);
                listaSugerenciasEliminar.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && popupEliminar.isVisible()) {
            if (size > 0) {
                int newIndex = (index > 0) ? index - 1 : size - 1;
                listaSugerenciasEliminar.setSelectedIndex(newIndex);
                listaSugerenciasEliminar.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (popupEliminar.isVisible()) {
                String selected = listaSugerenciasEliminar.getSelectedValue();
                if (selected != null) {
                    String dni = selected.split(" - ")[0].trim();
                    componentManageWorker.textFieldNameDeleteUser.setText(dni);
                }
                popupEliminar.setVisible(false);
            }
            // Ejecutar eliminación
            componentManageWorker.jButtonEliminarPerson.doClick();
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE && popupEliminar.isVisible()) {
            popupEliminar.setVisible(false);
            e.consume();
        }
    }

    // Mostrar últimos empleados
    public void tableListLast(int limit) {
        try {
            DefaultTableModel model = (DefaultTableModel) componentManageWorker.jTableListEmployee.getModel();
            model.setRowCount(0);
            List<EmployeeTb> listEmployee = getLastEmployees(limit);
            for (EmployeeTb employeeTb : listEmployee) {
                model.addRow(new Object[]{
                    employeeTb.getFullName(),
                    employeeTb.getNationalId(),
                    employeeTb.getEmploymentStatus()
                });
            }
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al cargar la lista de empleados", "GESTIÓN TRABAJADOR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cleanComponent() {
        componentManageWorker.textFieldDNI.setText("");
        componentManageWorker.textFieldName.setText("");
        componentManageWorker.dcBirthDate.setDate(new java.util.Date());
        componentManageWorker.comboBoxSex.setSelectedIndex(0);
        componentManageWorker.comboStatus.setSelectedIndex(0);
        clearEditingRowHighlight();
    }

    // Método para entrar en modo edición
    public void enterEditMode(String dni, String fullName, String gender, String status, java.util.Date startDate, int rowIndex) {
        isEditMode = true;
        editingEmployeeDni = dni;
        editingRowIndex = rowIndex;

        // Poner nombre completo directamente
        componentManageWorker.textFieldName.setText(fullName);
        componentManageWorker.textFieldDNI.setText(dni);

        // Establecer género
        if (gender != null && gender.equalsIgnoreCase("HOMBRE")) {
            componentManageWorker.comboBoxSex.setSelectedIndex(1);
        } else {
            componentManageWorker.comboBoxSex.setSelectedIndex(0);
        }

        // Establecer estado
        if (status != null && status.equalsIgnoreCase("CAS")) {
            componentManageWorker.comboStatus.setSelectedIndex(1);
        } else {
            componentManageWorker.comboStatus.setSelectedIndex(0);
        }

        // Establecer fecha
        if (startDate != null) {
            componentManageWorker.dcBirthDate.setDate(startDate);
        }

        // Marcar la fila que se está editando
        highlightEditingRow(rowIndex);

        // Cambiar texto del botón y mostrar botón cancelar
        componentManageWorker.jButtonRegisterWorker.setText("EDITAR TRABAJADOR");
        componentManageWorker.jButtonRegisterWorker.setBackground(new java.awt.Color(0, 51, 153)); // Azul para editar
        componentManageWorker.jButtonCancelarEdicion.setVisible(true);
    }

    // Marcar la fila que se está editando con color
    private void highlightEditingRow(int rowIndex) {
        componentManageWorker.jTableListEmployee.setRowSelectionInterval(rowIndex, rowIndex);
        componentManageWorker.jTableListEmployee.setSelectionBackground(new java.awt.Color(255, 255, 153)); // Amarillo claro
    }

    // Limpiar el resaltado de la fila editando
    private void clearEditingRowHighlight() {
        componentManageWorker.jTableListEmployee.clearSelection();
        componentManageWorker.jTableListEmployee.setSelectionBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
        editingRowIndex = -1;
    }

    // Método para salir del modo edición
    public void exitEditMode() {
        isEditMode = false;
        editingEmployeeDni = null;
        cleanComponent();
        componentManageWorker.jButtonRegisterWorker.setText("REGISTRAR TRABAJADOR");
        componentManageWorker.jButtonRegisterWorker.setBackground(new java.awt.Color(0, 102, 0)); // Verde para registrar
        componentManageWorker.jButtonCancelarEdicion.setVisible(false);
    }

    public void insertEmployee() {

        if (componentManageWorker.textFieldName.getText().isBlank()
                || componentManageWorker.textFieldDNI.getText().isBlank()) {

            JOptionPane.showMessageDialog(null, "Debe de llenar los campos importantes: Nombre Completo y DNI", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (componentManageWorker.textFieldDNI.getText().length() != 8) {
            JOptionPane.showMessageDialog(null, "El DNI debe de contener 8 digitos", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String fullName = componentManageWorker.textFieldName.getText().trim().toUpperCase();
            String dni = componentManageWorker.textFieldDNI.getText();
            String gender = componentManageWorker.comboBoxSex.getSelectedIndex() == 0 ? "MUJER" : "HOMBRE";
            String status = componentManageWorker.comboStatus.getSelectedIndex() == 0 ? "NOMBRADO" : "CAS";
            Date startDate = new Date(componentManageWorker.dcBirthDate.getDate().getTime());

            if (isEditMode) {
                // Modo edición - actualizar empleado existente (puede cambiar DNI también)
                boolean result = updateEmployee(editingEmployeeDni, fullName, gender, status, startDate);
                if (result) {
                    JOptionPane.showMessageDialog(null, "Empleado actualizado correctamente", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
                    loadEmployeeList(); // Recargar lista de autocompletado
                    exitEditMode();
                } else {
                    JOptionPane.showMessageDialog(null, "No se pudo actualizar el empleado", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // Modo registro - crear nuevo empleado
                EmployeeTb employee = new EmployeeTb(
                        fullName,
                        dni,
                        gender,
                        status,
                        componentManageWorker.comboStatus.getSelectedIndex() == 0 ? "2154" : "2028",
                        componentManageWorker.dcBirthDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                create(employee);
                JOptionPane.showMessageDialog(null, "Empleado registrado correctamente", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeList(); // Recargar lista de autocompletado
                cleanComponent();
            }
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "DNI ya existe", "GESTIÓN TRABAJADOR", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    public void tableList(Date date, Date date2) {
        try {
            DefaultTableModel model = (DefaultTableModel) componentManageWorker.jTableListEmployee.getModel();
            model.setRowCount(0);
            List<EmployeeTb> listEmployee = new EmployeeDao().getEmployeesByDateRange(date, date2);
            for (EmployeeTb employeeTb : listEmployee) {
                model.addRow(new Object[]{
                    employeeTb.getFullName(),
                    employeeTb.getNationalId(),
                    employeeTb.getEmploymentStatus()
                });
            }
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al cargar la lista de empleados", "GESTIÓN TRABAJADOR", JOptionPane.ERROR_MESSAGE);
        }
    }

}
