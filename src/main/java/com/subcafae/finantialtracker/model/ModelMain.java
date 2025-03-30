/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.controller.ControllerManageBond;
import com.subcafae.finantialtracker.controller.ControllerManageLoan;
import com.subcafae.finantialtracker.controller.ControllerManageWorker;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.dao.LoanDetailsDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.report.HistoryPayment.HistoryPayment;
import com.subcafae.finantialtracker.report.concept.PaymentVoucher;
import com.subcafae.finantialtracker.report.descuento.DatosPersona;
import com.subcafae.finantialtracker.report.descuento.ExcelExporter;
import com.subcafae.finantialtracker.report.deuda.ReporteDeuda;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import com.subcafae.finantialtracker.view.component.ComponentManageUser;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Jesus Gutierrez
 */
public  class ModelMain {

    protected ComponentManageBond componentManageBond;
    protected ComponentManageLoan componentManageLoan;
    protected ComponentManageUser componentManageUser;
    protected ComponentManageWorker componentManageWorker;
    public ViewMain viewMain;

    public ModelMain(ViewMain viewMain, UserTb user) {
        user.setId(1);

        this.componentManageBond = new ComponentManageBond();
        this.componentManageLoan = new ComponentManageLoan();
        this.componentManageUser = new ComponentManageUser();
        this.componentManageWorker = new ComponentManageWorker();

        new ControllerManageLoan(componentManageLoan, user);
        new ControllerManageBond(componentManageBond, user);
        new ControllerManageWorker(componentManageWorker);

        this.viewMain = viewMain;
        init();
        combo();
    }

    private void init() {
        viewMain.loading.setUndecorated(true);
        viewMain.loading.setSize(412, 115);
        viewMain.loading.setLocationRelativeTo(null);

        //  
        viewMain.setSize(1500, 800);
        viewMain.toFront();
        viewMain.setLocationRelativeTo(null);
        viewMain.setVisible(true);
    }

    public void centerInternalComponent(JInternalFrame jInternalFrame) {

        boolean existeVentana = false;

        JInternalFrame[] frames = viewMain.jDesktopPane1.getAllFrames();

        for (JInternalFrame frame : frames) {
            if (frame.getClass().equals(jInternalFrame.getClass())) {

                existeVentana = true;
                try {
                    frame.setSelected(true); // Llevar al frente
                    frame.toFront();
                } catch (PropertyVetoException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        if (!existeVentana) {
            jInternalFrame.setLocation(
                    (viewMain.jDesktopPane1.getWidth() - jInternalFrame.getWidth()) / 2,
                    (viewMain.jDesktopPane1.getHeight() - jInternalFrame.getHeight()) / 2
            );
            viewMain.jDesktopPane1.add(jInternalFrame);
            jInternalFrame.setVisible(true); // Usar setVisible() en lugar de show()

            try {
                jInternalFrame.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
        }
    }

    public void historyPayment() {

        new Thread(() -> {

            EmployeeDao empleadoDao = new EmployeeDao();

            String[] workerOptions = {"CAS", "Nombrado"};

            String nombresBuscar = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecciona el tipo de trabajador:",
                    "Tipo de Trabajador",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    workerOptions,
                    workerOptions[0]
            );
            if (nombresBuscar == null) {
                return;
            }

            try {
                viewMain.loading.setVisible(true);

                String[] employee = empleadoDao.findAll()
                        .stream().map(
                                map -> map.getEmploymentStatus().equalsIgnoreCase(nombresBuscar)
                                ? map.getNationalId().concat(" - ".concat(map.getFirstName().concat(" ".concat(map.getLastName())))) : ""
                        ).toArray(String[]::new);

                String nombre = (String) JOptionPane.showInputDialog(
                        null,
                        "Selecciona el tipo de trabajador:",
                        "Listado de Trabajador",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        employee,
                        employee[0]
                );

                if (nombre == null) {
                    return;
                }

                String dniSeleccionado = nombre.split(" - ")[0].trim();
                HistoryPayment historyPayment = new HistoryPayment();

                historyPayment.HistoryPatment(dniSeleccionado);

                viewMain.loading.dispose();

            } catch (SQLException ex) {
                System.out.println("Error -> " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE DESCUENTO", JOptionPane.WARNING_MESSAGE);
                viewMain.loading.dispose();
                //Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

    }

    public void generateExcel() {

        new Thread(() -> {
            try {

                String[] contractTypeOptions = {"CAS", "Nombrado"};
//                    String[] monthsOptions = {
//                        "Enero", "Febrero", "Marzo", "Abril", "Mayo",
//                        "Junio", "Julio", "Agosto", "Septiembre",
//                        "Octubre", "Noviembre", "Diciembre"
//                    };

                String contractType = (String) JOptionPane.showInputDialog(
                        null,
                        "Selecciona el tipo de trabajador:",
                        "Tipo de Trabajador",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        contractTypeOptions,
                        contractTypeOptions[0]
                );
                if (contractType == null) {
                    return;
                }

                viewMain.loading.setVisible(true);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Formato que coincide con el texto
                //
                List<ExcelExporter> listExecel = new ArrayList<>();
                DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMYY");
                String monthYear = LocalDate.now().format(monthYearFormatter);
                //
                List<EmployeeTb> employees = null;
                List<AbonoDetailsTb> abonoDetailses = null;
                List<LoanTb> loan = null;
                List<LoanDetailsTb> loanDetails = null;
                List<AbonoTb> bonos = null;

                bonos = new AbonoDao().findAllAbonos().stream().filter(predicate -> predicate.getStatus().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());
                employees = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getEmploymentStatus().equals(contractType)).collect(Collectors.toList());
                abonoDetailses = new AbonoDetailsDao().getAllAbonoDetails().stream().filter(predicate -> predicate.getState().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());
                loan = new LoanDao().getAllLoans().stream().filter(predicate -> predicate.getState().equalsIgnoreCase("Aceptado") && predicate.getStateLoan().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());

                if (employees.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hay Trabajador tipo ".concat(contractType));
                    viewMain.loading.dispose();
                    return;
                }

                loanDetails = new LoanDetailsDao().getAllLoanDetails();

                Map<String, DatosPersona> mapaDniDatos = new HashMap<>(); // Cambiar a clave String si el DNI es String

                for (AbonoTb abono : bonos) {

                    int id = Integer.parseInt(abono.getEmployeeId());

                    for (EmployeeTb employee1 : employees) {

                        if (employee1.getEmployeeId() == id) {

                            for (AbonoDetailsTb detalle : abonoDetailses) {

                                if (abono.getId() == detalle.getAbonoID()) {

                                    Date utilDate = null;
                                    try {
                                        utilDate = formatter.parse(detalle.getPaymentDate());
                                    } catch (ParseException ex) {
                                        System.out.println("Error -> " + ex.getMessage());
                                    }
                                    System.out.println("fecha de vencimeinto  " + utilDate.toString());

                                    LocalDate fechaVencimiento = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                    if (LocalDate.now().isAfter(fechaVencimiento)) {
                                        System.out.println("Se guarda");
                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFirstName().concat(" " + employee1.getLastName()), employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(), detalle.getMonthly(), 0.0),
                                                (existente, nuevo) -> {

                                                    existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                    return existente;
                                                }
                                        );
                                    }
                                    if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()) && LocalDate.now().getYear() == fechaVencimiento.getYear()) {
                                        System.out.println("Se guarda");
                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFirstName().concat(" " + employee1.getLastName()), employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(), detalle.getMonthly(), 0.0),
                                                (existente, nuevo) -> {

                                                    existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                    return existente;
                                                }
                                        );
                                    }
                                }
                            }
                        }
                    }

                }

                for (LoanTb loan1 : loan) {

                    for (EmployeeTb employee1 : employees) {

                        if (employee1.getNationalId().equalsIgnoreCase(loan1.getEmployeeId())) {

                            for (LoanDetailsTb loanDetail1 : loanDetails) {

                                if (loan1.getId() == loanDetail1.getLoanId() && (loanDetail1.getState().equalsIgnoreCase("Pendiente") || loanDetail1.getState().equalsIgnoreCase("Parcial"))) {

                                    LocalDate fechaVencimiento = loanDetail1.getPaymentDate().toLocalDate();

                                    if (LocalDate.now().isAfter(fechaVencimiento)) {

                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFirstName().concat(" " + employee1.getLastName()),
                                                        employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                        loanDetail1.getMonthlyFeeValue(), 0.0),
                                                (existente, nuevo) -> {
                                                    existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                    return existente;
                                                }
                                        );
                                    }
                                    if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()) && LocalDate.now().getYear() == fechaVencimiento.getYear()) {
                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFirstName().concat(" " + employee1.getLastName()),
                                                        employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                        loanDetail1.getMonthlyFeeValue(), 0.0),
                                                (existente, nuevo) -> {
                                                    existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                    return existente;
                                                }
                                        );
                                    }
                                }
                            }
                        }

                    }
                }

                List<DatosPersona> datosLista = new ArrayList<>(mapaDniDatos.values());

                for (int i = 0; i < datosLista.size(); i++) {

                    ExcelExporter model = new ExcelExporter();
//
                    model.setCodigo(datosLista.get(i).getDni().split("-")[1].trim() + monthYear);
                    model.setDni(datosLista.get(i).getDni().split("-")[0].trim());
                    model.setNameWork(datosLista.get(i).getNombre());
                    model.setDate(new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date()));
                    model.setNumberCuota("50");
                    model.setPlazos("84");
                    model.setCuota(String.format("%.2f", Double.valueOf(String.valueOf(datosLista.get(i).getMonto()))));
                    model.setAporte("0.00");
                    model.setTotalPrestar(String.format("%.2f", datosLista.get(i).getMonto() * 84));
                    model.setNumberOperacion("000000");
                    listExecel.add(model);
                }

                ExcelExporter exporter = new ExcelExporter();
                exporter.generateExcel(listExecel);

                viewMain.loading.dispose();

            } catch (SQLException ex) {
                System.out.println("Error -> " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                viewMain.loading.dispose();
                //  Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

    }

    public void reportDeuda() {

        new Thread(() -> {

            try {

                List<EmployeeTb> listEmployee = new EmployeeDao().findAll();

                if (listEmployee.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hay ningún empleado.", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String[] namee = listEmployee.stream().map(name -> name.getNationalId().concat(" - " + name.getFirstName().concat(" " + name.getLastName()))).toArray(String[]::new);

                String nombresBuscar = (String) JOptionPane.showInputDialog(
                        null,
                        "Listado de trabajadores",
                        "GESTIÓN DE DEUDAS",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        namee,
                        namee[0]
                );

                if (nombresBuscar == null) {
                    JOptionPane.showMessageDialog(null, "No seleccionaste ningún empleado.", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                viewMain.loading.setVisible(true);

                List<ServiceConceptTb> listService = new ServiceConceptDao().getAllServiceConcepts();
                EmployeeTb employeeFind = listEmployee.stream().filter(predicate -> predicate.getNationalId().equalsIgnoreCase(nombresBuscar.split("-")[0].trim())).findFirst().get();

                List<AbonoTb> listAbond = new AbonoDao().findAllAbonos().stream().filter(
                        predicate
                        -> predicate.getEmployeeId().equalsIgnoreCase(employeeFind.getEmployeeId().toString())
                        && predicate.getStatus().equalsIgnoreCase("Pendiente")
                ).collect(Collectors.toList());

                List<LoanTb> listLoan = new LoanDao().getAllLoans().stream().filter(
                        predicate
                        -> predicate.getEmployeeId().equalsIgnoreCase(employeeFind.getNationalId())
                        && (predicate.getState().equalsIgnoreCase("Aceptado")
                        || predicate.getStateLoan().equalsIgnoreCase("Pendiente"))
                ).collect(Collectors.toList());

                List<ReporteDeuda> listBono = new ArrayList<>();
                List<ReporteDeuda> listPrestamo = new ArrayList<>();

                for (AbonoTb abonoTb : listAbond) {
                    ServiceConceptTb service = listService.stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get();
                    //

                    for (AbonoDetailsTb allAbonoDetail : new AbonoDetailsDao().getAllAbonoDetails()) {

                        if (allAbonoDetail.getAbonoID() == abonoTb.getId() && (allAbonoDetail.getState().equalsIgnoreCase("Parcial") || allAbonoDetail.getState().equalsIgnoreCase("Pendiente"))) {

                            ReporteDeuda modelBono = new ReporteDeuda();

                            modelBono.setConceptBono(service.getDescription());
                            modelBono.setDetalleCouta(String.valueOf(allAbonoDetail.getDues()));
                            modelBono.setFechaVencimiento(allAbonoDetail.getPaymentDate());
                            modelBono.setMonto(String.valueOf(allAbonoDetail.getMonthly() - allAbonoDetail.getPayment()));
                            modelBono.setNumSoli(abonoTb.getSoliNum());

                            listBono.add(modelBono);
                        }
                    }

                }

                for (LoanTb loanTb : listLoan) {

                    for (LoanDetailsTb allLoanDetail : new LoanDetailsDao().getAllLoanDetails()) {

                        if (allLoanDetail.getLoanId() == loanTb.getId() && (allLoanDetail.getState().equalsIgnoreCase("Parcial") || allLoanDetail.getState().equalsIgnoreCase("Pendiente"))) {

                            ReporteDeuda modelPrestamo = new ReporteDeuda();

                            modelPrestamo.setNumSoli(loanTb.getSoliNum());
                            modelPrestamo.setDetalleCouta(String.valueOf(allLoanDetail.getDues()));
                            modelPrestamo.setFechaVencimiento(allLoanDetail.getPaymentDate().toString());
                            modelPrestamo.setMonto(String.valueOf(allLoanDetail.getMonthlyFeeValue() - allLoanDetail.getPayment()));
                            //
                            modelPrestamo.setFondo(String.valueOf(allLoanDetail.getMonthlyIntangibleFundFee()));

                            listPrestamo.add(modelPrestamo);

                        }
                    }
                }

                if (listBono.isEmpty() && listPrestamo.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hay bonos y préstamos con el trabajador", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                new ReporteDeuda().reporteDeuda(
                        employeeFind.getFirstName().concat(" " + employeeFind.getLastName()), employeeFind.getNationalId(),
                        listBono, listPrestamo);

                viewMain.loading.dispose();

            } catch (SQLException ex) {
                System.out.println("Error -> " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                viewMain.loading.dispose();
                // Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();

    }

    //
    public void cleanVoucher() {
        viewMain.jTextFieldNumeroVoucher.setText("");
        viewMain.jTextFieldCuentaVoucher.setText("");
        viewMain.jTextFieldChequeVoucher.setText("");
        viewMain.jTextFieldMountVoucher.setText("");
        viewMain.jTextAreaDetalleVoucher.setText("");
        viewMain.cbConRegDateStartVoucher.setDate(null);
        viewMain.jComboBox1.removeAllItems();
        viewMain.jComboBoxSearchClient.removeAllItems();
    }

    public boolean validarCamposVoucher() {
        // Verificar si los JTextField están vacíos
        if (viewMain.jTextFieldNumeroVoucher.getText().trim().isEmpty()
                || viewMain.jTextFieldCuentaVoucher.getText().trim().isEmpty()
                || viewMain.jTextFieldChequeVoucher.getText().trim().isEmpty()
                || viewMain.jTextFieldMountVoucher.getText().trim().isEmpty()
                || viewMain.jTextAreaDetalleVoucher.getText().trim().isEmpty()) {
            return false;
        }

        // Verificar si la fecha es nula
        if (viewMain.cbConRegDateStartVoucher.getDate() == null) {
            return false;
        }

        // Verificar si los JComboBox tienen elementos seleccionados
        if (viewMain.jComboBoxSearchClient.getItemCount() == 0 || viewMain.jComboBoxSearchClient.getSelectedItem() == null) {
            return false;
        }
        try {
            return new EmployeeDao().findAll().stream().anyMatch(predicate -> viewMain.jComboBoxSearchClient.getSelectedItem().toString().equalsIgnoreCase(predicate.getFirstName().concat(" " + predicate.getLastName()) + " - " + predicate.getNationalId()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "No se registro", "REGISTRO DE VOUCHER", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

    }

    public void combo() {

        JTextField textField = (JTextField) viewMain.jComboBoxSearchClient.getEditor().getEditorComponent();
        viewMain.jComboBoxSearchClient.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getSource().equals(viewMain.jComboBoxSearchClient.getEditor().getEditorComponent())) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        System.out.println("presionó flecha arriba - remitente");
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        System.out.println("presionó flecha abajo - remitente");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                String inputString = viewMain.jComboBoxSearchClient.getEditor().getItem().toString();

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    String name = (String) viewMain.jComboBoxSearchClient.getSelectedItem();
                    if (name != null) {
                        System.out.println("clienteRemitente -> " + name.split(" - ")[0]);
                    } else {

                    }
                }

                if ((evt.getKeyCode() >= KeyEvent.VK_A && evt.getKeyCode() <= KeyEvent.VK_Z) // Letras
                        || (evt.getKeyCode() >= KeyEvent.VK_0 && evt.getKeyCode() <= KeyEvent.VK_9) // Números
                        || (evt.getKeyCode() >= KeyEvent.VK_NUMPAD0 && evt.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                        || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE // Borrar
                        || evt.getKeyCode() == KeyEvent.VK_SPACE) { // Espacio

                    System.out.println("Insertando ----------- ");

                    List<EmployeeTb> employees;
                    try {
                        employees = new EmployeeDao().findAll();
                    } catch (SQLException ex) {

                        JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                        System.out.println("Error en la lista -> " + ex.getMessage());
                        return;
                        //  Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    String cadena = textField.getText();

                    actualizarComboBox(viewMain.jComboBoxSearchClient, cadena, employees);

                    textField.setText(cadena);

                    if (viewMain.jComboBoxSearchClient.getItemCount() > 0) {
                        viewMain.jComboBoxSearchClient.showPopup();
                        if (evt.getKeyCode() != 8) {
                            ((JTextComponent) viewMain.jComboBoxSearchClient.getEditor().getEditorComponent())
                                    .select(inputString.length(), viewMain.jComboBoxSearchClient.getEditor().getItem().toString().length());
                        } else {
                            viewMain.jComboBoxSearchClient.getEditor().setItem(inputString);
                        }
                    } else {
                        viewMain.jComboBoxSearchClient.addItem(inputString);
                    }
                }
            }
        });

        JTextField textField1 = (JTextField) viewMain.jComboBox1.getEditor().getEditorComponent();
        viewMain.jComboBox1.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getSource().equals(viewMain.jComboBox1.getEditor().getEditorComponent())) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        System.out.println("presionó flecha arriba - remitente");
                    }
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        System.out.println("presionó flecha abajo - remitente");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                String inputString = viewMain.jComboBox1.getEditor().getItem().toString();

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    String name = (String) viewMain.jComboBox1.getSelectedItem();
                    if (name != null) {
                        if (!name.split(" - ")[0].isBlank()) {
                            PaymentVoucher.cleanUnusedVouchers();
                            viewMain.jButton1.setText("EDITAR");
                            viewMain.jButton2.setEnabled(false);
                            viewMain.jButton3.setEnabled(false);
                            viewMain.jButton4.setEnabled(true);
                            String num = name.split(" - ")[0];
                            PaymentVoucher paymentVoucher = new PaymentVoucher();
                            PaymentVoucher paymentVoucher1 = paymentVoucher.list().stream().filter(predicate -> predicate.getNumVoucher().equalsIgnoreCase(num)).findFirst().get();
                            viewMain.jTextFieldNumeroVoucher.setText(paymentVoucher1.getNumVoucher());
                            viewMain.jTextFieldCuentaVoucher.setText(paymentVoucher1.getNumAccount());
                            viewMain.jTextFieldChequeVoucher.setText(paymentVoucher1.getNumCheck());
                            viewMain.jTextFieldMountVoucher.setText(String.valueOf(paymentVoucher1.getAmount()));
                            viewMain.jTextAreaDetalleVoucher.setText(paymentVoucher1.getDetails());
                            viewMain.cbConRegDateStartVoucher.setDate(Date.from(paymentVoucher1.getDateEntry().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                            viewMain.jComboBoxSearchClient.addItem(paymentVoucher1.getNameLastName() + " - " + paymentVoucher1.getDocumentDni());
                        }
                    } else {

                    }
                }

                if ((evt.getKeyCode() >= KeyEvent.VK_A && evt.getKeyCode() <= KeyEvent.VK_Z) // Letras
                        || (evt.getKeyCode() >= KeyEvent.VK_0 && evt.getKeyCode() <= KeyEvent.VK_9) // Números
                        || (evt.getKeyCode() >= KeyEvent.VK_NUMPAD0 && evt.getKeyCode() <= KeyEvent.VK_NUMPAD9) // Teclado numérico
                        || evt.getKeyCode() == KeyEvent.VK_BACK_SPACE // Borrar
                        || evt.getKeyCode() == KeyEvent.VK_SPACE) { // Espacio

                    System.out.println("Insertando ----------- ");
                    PaymentVoucher paymentVoucher = new PaymentVoucher();

                    List<PaymentVoucher> listVoucher = paymentVoucher.list();
                    String cadena = textField1.getText();

                    actualizarComboBoxVoucher(viewMain.jComboBox1, cadena, listVoucher);

                    textField1.setText(cadena);

                    if (viewMain.jComboBox1.getItemCount() > 0) {
                        viewMain.jComboBox1.showPopup();
                        if (evt.getKeyCode() != 8) {
                            ((JTextComponent) viewMain.jComboBox1.getEditor().getEditorComponent())
                                    .select(inputString.length(), viewMain.jComboBox1.getEditor().getItem().toString().length());
                        } else {
                            viewMain.jComboBox1.getEditor().setItem(inputString);
                        }
                    } else {
                        viewMain.jComboBox1.addItem(inputString);
                    }
                }
            }
        });
    }

    private void actualizarComboBox(JComboBox<String> comboBox, String text, List<EmployeeTb> employees) {
        comboBox.removeAllItems();  // Limpia la lista, pero NO modifica el textField

        for (EmployeeTb item : employees) {
            if (item.getFirstName().concat(" " + item.getLastName()).toLowerCase().contains(text.toLowerCase())
                    || item.getNationalId().toLowerCase().contains(text.toLowerCase())) {
                comboBox.addItem(item.getFirstName().concat(" " + item.getLastName()) + " - " + item.getNationalId());
            }
        }

        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(-1);  // No selecciona nada automáticamente
        }
    }

    private void actualizarComboBoxVoucher(JComboBox<String> comboBox, String text, List<PaymentVoucher> voucher) {
        comboBox.removeAllItems();  // Limpia la lista, pero NO modifica el textField

        for (PaymentVoucher item : voucher) {
            if (item.getNumVoucher().toLowerCase().contains(text.toLowerCase())) {
                comboBox.addItem(item.getNumVoucher() + " - " + item.getNameLastName());
            }
        }

        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(-1);  // No selecciona nada automáticamente
        }
    }
}
