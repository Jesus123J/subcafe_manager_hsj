/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package com.subcafae.finantialtracker.view.component;

import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.dao.LoanDetailsDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.report.HistoryPayment.HistoryPayment;
import com.subcafae.finantialtracker.report.deuda.ReporteDeuda;
import com.subcafae.finantialtracker.view.ViewMain;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jesus Gutierrez
 */
public final class ComponentSearchEmpl extends javax.swing.JInternalFrame {

    /**
     * Creates new form ComponentSearchEmpl
     */
    private boolean block = true;
    private ViewMain viewMain;
    private EmployeeTb empleadoSeleccionado; // variable global
    private String nombresBuscar;
    private List<EmployeeTb> empleados;
    private boolean opcion_main;

    public ComponentSearchEmpl(String string_conditional_option, List<EmployeeTb> empleados, boolean opcion_main, ViewMain view) {
        super("GESTIÓN DE INFORMES", false, true, false);
        this.viewMain = view;
        initComponents();
        jLabelTitle.setText(string_conditional_option);
        this.opcion_main = opcion_main;
        // En caso marco la opcion de deudas
        if (opcion_main) {
            jComboBox2.setEnabled(false);
            combo_box(jComboBox1, empleados, null);
        } else {  // opcion de ver los pagos 
//            jCom
            this.empleados = empleados;
        }

    }

    private void metodoPagos() {

        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                HistoryPayment historyPayment = new HistoryPayment();
                historyPayment.HistoryPatment(empleadoSeleccionado.getNationalId());

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                });
            } catch (Exception ex) {
                System.out.println("Error -> " + ex.getMessage());
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "Ocurrió un problema al generar el reporte de pagos", "ERROR", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
    }

    private void combo_box(JComboBox<String> jComboBox1, List<EmployeeTb> empleados, String filtroStatus) {
        jComboBox1.setEditable(true);
        jComboBox1.setSelectedItem(null);

        // Editor de texto
        JTextField editor = (JTextField) jComboBox1.getEditor().getEditorComponent();
        editor.setText("");

        // Aplicar filtro CAS/NOMBRADO si corresponde
        List<EmployeeTb> listaFiltrada = (filtroStatus != null && !filtroStatus.isEmpty())
                ? empleados.stream()
                        .filter(emp -> emp.getEmploymentStatus().equalsIgnoreCase(filtroStatus))
                        .collect(Collectors.toList())
                : empleados;

        // Listener de teclado
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = editor.getText().trim().toLowerCase();

                if (texto.isEmpty()) {
                    jComboBox1.hidePopup();
                    jComboBox1.setModel(new DefaultComboBoxModel<>());
                    return;
                }

                // Filtrar empleados que coincidan
                List<EmployeeTb> filtrados = listaFiltrada.stream()
                        .filter(emp -> emp.getFullName().toLowerCase().contains(texto)
                        || emp.getNationalId().toLowerCase().contains(texto))
                        .collect(Collectors.toList());

                // Actualizar modelo con los resultados
                DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
                filtrados.forEach(emp -> modelo.addElement(emp.getNationalId() + " - " + emp.getFullName()));
                jComboBox1.setModel(modelo);
                editor.setText(texto);
                editor.setCaretPosition(editor.getText().length());

                if (!filtrados.isEmpty()) {
                    jComboBox1.showPopup();
                } else {
                    jComboBox1.hidePopup();
                }

                // Si presiona ENTER y solo hay un resultado → selecciona ese
                if (e.getKeyCode() == KeyEvent.VK_ENTER && filtrados.size() == 1) {
                    EmployeeTb emp = filtrados.get(0);
                    empleadoSeleccionado = emp;

                    editor.setText(emp.getNationalId() + " - " + emp.getFullName());
                    block = false;
                    jComboBox1.setEnabled(false);
                    jComboBox1.hidePopup();
                }
            }
        });

        // Cuando selecciona desde el popup
        jComboBox1.addActionListener(e -> {
            if (jComboBox1.isPopupVisible()) {
                Object valorSeleccionado = jComboBox1.getSelectedItem();
                if (valorSeleccionado != null) {
                    String seleccionado = valorSeleccionado.toString();
                    for (int i = 0; i < listaFiltrada.size(); i++) {
                        String formato = listaFiltrada.get(i).getNationalId() + " - " + listaFiltrada.get(i).getFullName();
                        if (formato.equals(seleccionado)) {
                            empleadoSeleccionado = listaFiltrada.get(i);
                            break;
                        }
                    }

                    editor.setText(seleccionado);
                    block = false;
                    jComboBox1.setEnabled(false);
                    jComboBox1.hidePopup();
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>();
        jLabelTitle = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();

        jLabelTitle.setBackground(new java.awt.Color(0, 0, 0));
        jLabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jButton1.setText("SACAR INFORME");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CAS", "NOMBRADO" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jLabel2.setText("TIPO DE CONTRATO");

        jLabel3.setText("SELECCIONE LA PERSONA");

        jButton2.setText("DESELECCIONAR");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public void metodoDeuda() {

        try {
            List<ServiceConceptTb> listService = new ServiceConceptDao().getAllServiceConcepts();
            System.out.println("Entrando -------- ");
            EmployeeTb employeeFind = empleadoSeleccionado;
            System.out.println("Entrando ");
            List<AbonoTb> listAbond = new AbonoDao().findAllAbonos().stream().filter(
                    predicate
                    -> predicate.getEmployeeId().equalsIgnoreCase(employeeFind.getEmployeeId().toString())
                    && predicate.getStatus().equalsIgnoreCase("Pendiente")
            ).collect(Collectors.toList());
            System.out.println("Entrando ");

            List<LoanTb> listLoan = new ArrayList<>();

            for (LoanTb allLoan : new LoanDao().getAllLoans()) {

                if (allLoan.getPaymentResponsibility().equalsIgnoreCase("EMPLOYEE") && allLoan.getState().equalsIgnoreCase("Aceptado") && allLoan.getStateLoan().equalsIgnoreCase("Pendiente")) {
                    if (employeeFind.getNationalId().equalsIgnoreCase(allLoan.getEmployeeId())) {
                        listLoan.add(new LoanTb(
                                allLoan.getId(),
                                allLoan.getSoliNum(),
                                allLoan.getEmployeeId(),
                                allLoan.getGuarantorIds(),
                                allLoan.getAmountWithdrawn(),
                                allLoan.getRequestedAmount(),
                                allLoan.getDues(),
                                allLoan.getPaymentDate(),
                                allLoan.getState(),
                                allLoan.getRefinanceParentId(),
                                allLoan.getCreatedBy(),
                                allLoan.getCreatedAt(),
                                allLoan.getModifiedAt(),
                                allLoan.getModifiedBy(),
                                allLoan.getType(),
                                allLoan.getPaymentResponsibility()));
                    }
                } else if (allLoan.getPaymentResponsibility().equalsIgnoreCase("GUARANTOR") && allLoan.getState().equalsIgnoreCase("Aceptado") && allLoan.getStateLoan().equalsIgnoreCase("Pendiente")) {
                    if (employeeFind.getNationalId().equalsIgnoreCase(allLoan.getGuarantorIds())) {

                        listLoan.add(new LoanTb(
                                allLoan.getId(),
                                allLoan.getSoliNum(),
                                allLoan.getEmployeeId(),
                                allLoan.getGuarantorIds(),
                                allLoan.getAmountWithdrawn(),
                                allLoan.getRequestedAmount(),
                                allLoan.getDues(),
                                allLoan.getPaymentDate(),
                                allLoan.getState(),
                                allLoan.getRefinanceParentId(),
                                allLoan.getCreatedBy(),
                                allLoan.getCreatedAt(),
                                allLoan.getModifiedAt(),
                                allLoan.getModifiedBy(),
                                allLoan.getType(),
                                allLoan.getPaymentResponsibility()));
                    }
                }

            }
            System.out.println("Entrando ");

            Map<AbonoTb, List<ReporteDeuda>> lisComAbono = new HashMap<>();
            Map<LoanTb, List<ReporteDeuda>> listComLoan = new HashMap<>();

            for (AbonoTb abonoTb : listAbond) {

                ServiceConceptTb service = listService.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get();
                //
                List<ReporteDeuda> listBonoo = new ArrayList<>();

                for (AbonoDetailsTb allAbonoDetail : new AbonoDetailsDao().getAllAbonoDetails()) {

                    if (allAbonoDetail.getAbonoID() == abonoTb.getId() && (allAbonoDetail.getState().equalsIgnoreCase("Parcial") || allAbonoDetail.getState().equalsIgnoreCase("Pendiente"))) {

                        ReporteDeuda modelBono = new ReporteDeuda();
                        modelBono.setConceptBono(service.getDescription());
                        modelBono.setDetalleCouta(String.valueOf(allAbonoDetail.getDues()));
                        modelBono.setFechaVencimiento(allAbonoDetail.getPaymentDate());
                        modelBono.setMonto(String.format("%.2f", allAbonoDetail.getMonthly() - allAbonoDetail.getPayment()));
                        modelBono.setNumSoli(abonoTb.getSoliNum());

                        listBonoo.add(modelBono);

                    }
                }

                lisComAbono.put(abonoTb, listBonoo);

            }

            for (LoanTb loanTb : listLoan) {

                List<ReporteDeuda> listPrestamo = new ArrayList<>();

                for (LoanDetailsTb allLoanDetail : new LoanDetailsDao().getAllLoanDetails()) {

                    if (allLoanDetail.getLoanId() == loanTb.getId() && (allLoanDetail.getState().equalsIgnoreCase("Parcial") || allLoanDetail.getState().equalsIgnoreCase("Pendiente"))) {

                        ReporteDeuda modelPrestamo = new ReporteDeuda();
                        System.out.println("SOli -> " + loanTb.getSoliNum());
                        modelPrestamo.setNumSoli(loanTb.getSoliNum());
                        modelPrestamo.setDetalleCouta(String.valueOf(allLoanDetail.getDues()));
                        modelPrestamo.setFechaVencimiento(allLoanDetail.getPaymentDate().toString());
                        modelPrestamo.setMonto(String.format("%.2f", allLoanDetail.getMonthlyFeeValue() - allLoanDetail.getPayment()));
                        //
                        Double montoSinFondo = allLoanDetail.getMonthlyFeeValue() - allLoanDetail.getMonthlyIntangibleFundFee();

                        if (montoSinFondo <= Double.valueOf(modelPrestamo.getMonto())) {
                            modelPrestamo.setFondo(String.valueOf(allLoanDetail.getMonthlyIntangibleFundFee()));
                        }

                        listPrestamo.add(modelPrestamo);

                    }
                    listComLoan.put(loanTb, listPrestamo);
                }
            }

            if (listComLoan.isEmpty() && lisComAbono.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No hay bonos y préstamos con el trabajador", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                viewMain.loading.dispose();
                return;
            }

            new ReporteDeuda().reporteDeuda(
                    employeeFind.getFullName(), employeeFind.getNationalId(),
                    lisComAbono, listComLoan);

            viewMain.loading.dispose();
        } catch (SQLException ex) {
            Logger.getLogger(ComponentSearchEmpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (opcion_main) {
            if (!block) {
                if (empleadoSeleccionado == null) {
                    JOptionPane.showMessageDialog(null, "Seleccione un empleado");
                } else {
                    viewMain.loading.setVisible(true);
                    metodoDeuda();
                }

            } else {
                JOptionPane.showMessageDialog(null, "Seleccione un empleado");
            }
        } else {
            metodoPagos();
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        System.out.println("empleado" + empleadoSeleccionado);
        empleadoSeleccionado = null;
        block = true;
        jComboBox1.setEnabled(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        System.out.println("Seleccion");
        String intem = jComboBox2.getSelectedItem().toString(); // CAS O NOMBRADO
        empleadoSeleccionado = null;
        block = true;
        jComboBox1.setEnabled(true);
        combo_box(jComboBox1, empleados, intem);
    }//GEN-LAST:event_jComboBox2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTitle;
    // End of variables declaration//GEN-END:variables
}
