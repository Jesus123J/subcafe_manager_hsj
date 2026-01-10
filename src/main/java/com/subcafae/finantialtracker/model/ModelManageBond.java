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
import com.toedter.calendar.JDateChooser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
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
    public LocalDate localDate;

    // Listas para autocompletado
    protected List<String> listSoliNumsAbono;
    protected List<String> listConceptDescriptions;

    // Popup para autocompletado de jTextFieldSearchSoliBond
    protected javax.swing.JPopupMenu popupSearchSoliBond;
    protected javax.swing.JList<String> listaSugerenciasSearchSoliBond;
    protected javax.swing.DefaultListModel<String> modeloListaSearchSoliBond;

    // Popup para autocompletado de jTextFieldEliminarBono
    protected javax.swing.JPopupMenu popupEliminarBono;
    protected javax.swing.JList<String> listaSugerenciasEliminarBono;
    protected javax.swing.DefaultListModel<String> modeloListaEliminarBono;

    // Popup para autocompletado de jTextFieldRenunciarBono
    protected javax.swing.JPopupMenu popupRenunciarBono;
    protected javax.swing.JList<String> listaSugerenciasRenunciarBono;
    protected javax.swing.DefaultListModel<String> modeloListaRenunciarBono;

    // Popup para autocompletado de searchConcept
    protected javax.swing.JPopupMenu popupSearchConcept;
    protected javax.swing.JList<String> listaSugerenciasSearchConcept;
    protected javax.swing.DefaultListModel<String> modeloListaSearchConcept;

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
        componentManageBond.dateStart1.setDate(new Date());

        ((JTextField) componentManageBond.dateStart1.getDateEditor().getUiComponent()).setEditable(false);

        localDate = componentManageBond.dateStart1.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Inicializar autocompletados
        initAutocompleteSearchSoliBond();
        initAutocompleteEliminarBono();
        initAutocompleteRenunciarBono();
        initAutocompleteSearchConcept();
    }

    // Inicializar autocompletado para jTextFieldSearchSoliBond
    private void initAutocompleteSearchSoliBond() {
        popupSearchSoliBond = new javax.swing.JPopupMenu();
        modeloListaSearchSoliBond = new javax.swing.DefaultListModel<>();
        listaSugerenciasSearchSoliBond = new javax.swing.JList<>(modeloListaSearchSoliBond);
        listaSugerenciasSearchSoliBond.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasSearchSoliBond.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasSearchSoliBond);
        scrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
        popupSearchSoliBond.add(scrollPane);
        popupSearchSoliBond.setFocusable(false);

        listaSugerenciasSearchSoliBond.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasSearchSoliBond.getSelectedValue();
                    if (selected != null) {
                        componentManageBond.jTextFieldSearchSoliBond.setText(selected);
                        popupSearchSoliBond.setVisible(false);
                        componentManageBond.jTextFieldSearchSoliBond.requestFocusInWindow();
                    }
                }
            }
        });

        componentManageBond.jTextFieldSearchSoliBond.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                // Pasar jButton1 para que al presionar Enter ejecute la busqueda
                handlePopupNavigation(e, listaSugerenciasSearchSoliBond, modeloListaSearchSoliBond, popupSearchSoliBond, componentManageBond.jTextFieldSearchSoliBond, componentManageBond.jButton1);
            }
        });
    }

    // Inicializar autocompletado para jTextFieldEliminarBono
    private void initAutocompleteEliminarBono() {
        popupEliminarBono = new javax.swing.JPopupMenu();
        modeloListaEliminarBono = new javax.swing.DefaultListModel<>();
        listaSugerenciasEliminarBono = new javax.swing.JList<>(modeloListaEliminarBono);
        listaSugerenciasEliminarBono.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasEliminarBono.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasEliminarBono);
        scrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
        popupEliminarBono.add(scrollPane);
        popupEliminarBono.setFocusable(false);

        listaSugerenciasEliminarBono.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasEliminarBono.getSelectedValue();
                    if (selected != null) {
                        componentManageBond.jTextFieldEliminarBono.setText(selected);
                        popupEliminarBono.setVisible(false);
                        componentManageBond.jTextFieldEliminarBono.requestFocusInWindow();
                    }
                }
            }
        });

        componentManageBond.jTextFieldEliminarBono.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                handlePopupNavigation(e, listaSugerenciasEliminarBono, modeloListaEliminarBono, popupEliminarBono, componentManageBond.jTextFieldEliminarBono, componentManageBond.jButtonEliminarBono);
            }
        });
    }

    // Inicializar autocompletado para jTextFieldRenunciarBono
    private void initAutocompleteRenunciarBono() {
        popupRenunciarBono = new javax.swing.JPopupMenu();
        modeloListaRenunciarBono = new javax.swing.DefaultListModel<>();
        listaSugerenciasRenunciarBono = new javax.swing.JList<>(modeloListaRenunciarBono);
        listaSugerenciasRenunciarBono.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasRenunciarBono.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasRenunciarBono);
        scrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
        popupRenunciarBono.add(scrollPane);
        popupRenunciarBono.setFocusable(false);

        listaSugerenciasRenunciarBono.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasRenunciarBono.getSelectedValue();
                    if (selected != null) {
                        componentManageBond.jTextFieldRenunciarBono.setText(selected);
                        popupRenunciarBono.setVisible(false);
                        componentManageBond.jTextFieldRenunciarBono.requestFocusInWindow();
                    }
                }
            }
        });

        componentManageBond.jTextFieldRenunciarBono.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                handlePopupNavigation(e, listaSugerenciasRenunciarBono, modeloListaRenunciarBono, popupRenunciarBono, componentManageBond.jTextFieldRenunciarBono, componentManageBond.jButtonRenunciarBono);
            }
        });
    }

    // Inicializar autocompletado para searchConcept
    private void initAutocompleteSearchConcept() {
        popupSearchConcept = new javax.swing.JPopupMenu();
        modeloListaSearchConcept = new javax.swing.DefaultListModel<>();
        listaSugerenciasSearchConcept = new javax.swing.JList<>(modeloListaSearchConcept);
        listaSugerenciasSearchConcept.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listaSugerenciasSearchConcept.setVisibleRowCount(8);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listaSugerenciasSearchConcept);
        scrollPane.setPreferredSize(new java.awt.Dimension(250, 150));
        popupSearchConcept.add(scrollPane);
        popupSearchConcept.setFocusable(false);

        listaSugerenciasSearchConcept.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String selected = listaSugerenciasSearchConcept.getSelectedValue();
                    if (selected != null) {
                        // Extraer solo el codigo (antes del " - ")
                        String codigo = selected.split(" - ")[0].trim();
                        componentManageBond.searchConcept.setText(codigo);
                        popupSearchConcept.setVisible(false);
                        componentManageBond.searchConcept.requestFocusInWindow();
                    }
                }
            }
        });

        componentManageBond.searchConcept.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                handlePopupNavigationConcept(e);
            }
        });
    }

    // Metodo auxiliar para navegacion de popup (numeros de solicitud)
    private void handlePopupNavigation(java.awt.event.KeyEvent e, javax.swing.JList<String> lista,
            javax.swing.DefaultListModel<String> modelo, javax.swing.JPopupMenu popup,
            JTextField textField, javax.swing.JButton button) {
        int index = lista.getSelectedIndex();
        int size = modelo.getSize();

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && popup.isVisible()) {
            if (size > 0) {
                int newIndex = (index < size - 1) ? index + 1 : 0;
                lista.setSelectedIndex(newIndex);
                lista.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && popup.isVisible()) {
            if (size > 0) {
                int newIndex = (index > 0) ? index - 1 : size - 1;
                lista.setSelectedIndex(newIndex);
                lista.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (popup.isVisible()) {
                String selected = lista.getSelectedValue();
                if (selected != null) {
                    textField.setText(selected);
                }
                popup.setVisible(false);
                e.consume();
            } else if (button != null && !textField.getText().isBlank()) {
                // Si no hay popup y hay boton, ejecutar con confirmacion
                button.doClick();
                e.consume();
            }
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE && popup.isVisible()) {
            popup.setVisible(false);
            e.consume();
        }
    }

    // Metodo auxiliar para navegacion de popup (conceptos - solo pone codigo al seleccionar)
    private void handlePopupNavigationConcept(java.awt.event.KeyEvent e) {
        int index = listaSugerenciasSearchConcept.getSelectedIndex();
        int size = modeloListaSearchConcept.getSize();

        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && popupSearchConcept.isVisible()) {
            if (size > 0) {
                int newIndex = (index < size - 1) ? index + 1 : 0;
                listaSugerenciasSearchConcept.setSelectedIndex(newIndex);
                listaSugerenciasSearchConcept.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && popupSearchConcept.isVisible()) {
            if (size > 0) {
                int newIndex = (index > 0) ? index - 1 : size - 1;
                listaSugerenciasSearchConcept.setSelectedIndex(newIndex);
                listaSugerenciasSearchConcept.ensureIndexIsVisible(newIndex);
            }
            e.consume();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (popupSearchConcept.isVisible()) {
                String selected = listaSugerenciasSearchConcept.getSelectedValue();
                if (selected != null) {
                    // Extraer solo el codigo (antes del " - ")
                    String codigo = selected.split(" - ")[0].trim();
                    componentManageBond.searchConcept.setText(codigo);
                }
                popupSearchConcept.setVisible(false);
                e.consume();
            }
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE && popupSearchConcept.isVisible()) {
            popupSearchConcept.setVisible(false);
            e.consume();
        }
    }

    // Metodos para mostrar autocompletado
    public void showAutocompleteSoliBond(JTextField textField, javax.swing.JPopupMenu popup,
            javax.swing.JList<String> lista, javax.swing.DefaultListModel<String> modelo) {
        String textSearch = textField.getText().trim();

        if (textSearch.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        if (listSoliNumsAbono == null || listSoliNumsAbono.isEmpty()) {
            listSoliNumsAbono = abonoDao.getAllSoliNums();
        }

        modelo.clear();
        int count = 0;
        for (String soliNumItem : listSoliNumsAbono) {
            if (soliNumItem.toLowerCase().contains(textSearch.toLowerCase())) {
                modelo.addElement(soliNumItem);
                count++;
                if (count >= 10) break;
            }
        }

        if (count > 0) {
            lista.setSelectedIndex(0);
            if (!popup.isVisible()) {
                popup.show(textField, 0, textField.getHeight());
            }
            textField.requestFocusInWindow();
        } else {
            popup.setVisible(false);
        }
    }

    public void showAutocompleteSearchSoliBond() {
        showAutocompleteSoliBond(componentManageBond.jTextFieldSearchSoliBond, popupSearchSoliBond,
                listaSugerenciasSearchSoliBond, modeloListaSearchSoliBond);
    }

    public void showAutocompleteEliminarBono() {
        showAutocompleteSoliBond(componentManageBond.jTextFieldEliminarBono, popupEliminarBono,
                listaSugerenciasEliminarBono, modeloListaEliminarBono);
    }

    public void showAutocompleteRenunciarBono() {
        showAutocompleteSoliBond(componentManageBond.jTextFieldRenunciarBono, popupRenunciarBono,
                listaSugerenciasRenunciarBono, modeloListaRenunciarBono);
    }

    public void showAutocompleteSearchConcept() {
        String textSearch = componentManageBond.searchConcept.getText().trim();

        if (textSearch.isEmpty()) {
            popupSearchConcept.setVisible(false);
            return;
        }

        if (listConceptDescriptions == null || listConceptDescriptions.isEmpty()) {
            listConceptDescriptions = serviceConceptDao.getAllConceptDescriptions();
        }

        modeloListaSearchConcept.clear();
        int count = 0;
        for (String desc : listConceptDescriptions) {
            if (desc.toLowerCase().contains(textSearch.toLowerCase())) {
                modeloListaSearchConcept.addElement(desc);
                count++;
                if (count >= 10) break;
            }
        }

        if (count > 0) {
            listaSugerenciasSearchConcept.setSelectedIndex(0);
            if (!popupSearchConcept.isVisible()) {
                popupSearchConcept.show(componentManageBond.searchConcept, 0, componentManageBond.searchConcept.getHeight());
            }
            componentManageBond.searchConcept.requestFocusInWindow();
        } else {
            popupSearchConcept.setVisible(false);
        }
    }

    // Recargar lista de numeros de solicitud
    public void reloadSoliNumsAbono() {
        listSoliNumsAbono = abonoDao.getAllSoliNums();
    }

    // Recargar lista de conceptos
    public void reloadConceptDescriptions() {
        listConceptDescriptions = serviceConceptDao.getAllConceptDescriptions();
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
            serviceConceptTb.setCostPrice(componentManageBond.jTextFieldPrecioCosto.getText().isBlank() ? 0.0 : Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldPrecioCosto.getText()))));
            serviceConceptTb.setSalePrice(componentManageBond.jTextFieldVenta.getText().isBlank() ? 0.0 : Double.parseDouble(String.format("%.2f", Double.valueOf(componentManageBond.jTextFieldVenta.getText()))));
            serviceConceptTb.setPriority(componentManageBond.jTextFieldPrioridad.getText().isBlank() ? 0 : Integer.parseInt(componentManageBond.jTextFieldPrioridad.getText()));
            serviceConceptTb.setUnid(componentManageBond.jTextFieldUnidades.getText().isBlank() ? 0 : Integer.parseInt(componentManageBond.jTextFieldUnidades.getText()));
            serviceConceptTb.setPriorityConcept(componentManageBond.jRadioButtonPaga.isSelected() ? "Primero" : "Segundo");
            serviceConceptTb.setCreatedBy(user.getId());
            serviceConceptTb.setCreatedAt(LocalDate.now().toString());

            serviceConceptDao.insert(serviceConceptTb);
            cleanDataConceptComponent();
            insertListTableConcept();

        } catch (NumberFormatException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Error -> " + e.getMessage(), "GÉSTION ABONOS", JOptionPane.OK_OPTION);
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

    public void insertListTableBono(Date start, Date finaly) {

        ViewMain.loading.setModal(true);
        ViewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();
                List<AbonoTb> listAbono = abonoDao.findAllAbonos(new java.sql.Date(start.getTime()), new java.sql.Date(finaly.getTime()));

                javax.swing.SwingUtilities.invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) componentManageBond.jTableListBonos.getModel();
                    model.setRowCount(0);
                    System.out.println("Diseño");
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

    // Metodo para mostrar los ultimos N abonos sin filtro de fecha
    public void insertListTableBonoLast(int limit) {

        ViewMain.loading.setModal(true);
        ViewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                List<ServiceConceptTb> listServi = new ServiceConceptDao().getAllServiceConcepts();
                List<AbonoTb> listAbono = abonoDao.getLastAbonos(limit);

                javax.swing.SwingUtilities.invokeLater(() -> {
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
                    JOptionPane.showMessageDialog(null, "Ocurrio un problema: " + ex.getMessage(), "GESTION ABONOS", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        ViewMain.loading.setVisible(true);
    }
}
