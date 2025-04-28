/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import com.subcafae.finantialtracker.controller.ControllerManageBond;
import com.subcafae.finantialtracker.controller.ControllerManageLoan;
import com.subcafae.finantialtracker.controller.ControllerManageWorker;
import com.subcafae.finantialtracker.controller.ControllerUser;
import com.subcafae.finantialtracker.data.dao.AbonoDao;
import com.subcafae.finantialtracker.data.dao.AbonoDetailsDao;
import com.subcafae.finantialtracker.data.dao.EmployeeDao;
import com.subcafae.finantialtracker.data.dao.LoanDao;
import com.subcafae.finantialtracker.data.dao.LoanDetailsDao;
import com.subcafae.finantialtracker.data.dao.RegistroDao;
import com.subcafae.finantialtracker.data.dao.ServiceConceptDao;
import com.subcafae.finantialtracker.data.dao.UserDao;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.data.entity.RegistroTb;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.report.HistoryPayment.AbonoDetailResult;
import com.subcafae.finantialtracker.report.HistoryPayment.HistoryPayment;
import com.subcafae.finantialtracker.report.concept.PaymentVoucher;
import com.subcafae.finantialtracker.report.descuento.DatosPersona;
import com.subcafae.finantialtracker.report.descuento.ExcelExporter;
import com.subcafae.finantialtracker.report.deuda.ReporteDeuda;
import com.subcafae.finantialtracker.util.JpanelDarkUtil;
import com.subcafae.finantialtracker.util.TextFieldValidator;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentLogin;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import com.subcafae.finantialtracker.view.component.ComponentManageUser;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelMain {

    public Map<EmployeeTb, Double> mapCom = new HashMap<>();
    protected ComponentManageBond componentManageBond;
    protected ComponentManageLoan componentManageLoan;
    protected ComponentManageUser componentManageUser;
    protected ComponentManageWorker componentManageWorker;
    public ViewMain viewMain;
    protected ComponentLogin componentLogin = new ComponentLogin();
    public DefaultTableModel model;
    public DefaultTableModel modelFindNot;
    private final MigLayout layout = new MigLayout("fill, insets 0");
    public JpanelDarkUtil jpanelDarkUtil = new JpanelDarkUtil();
    public ComponentLogin componentLogin1 = new ComponentLogin();
    protected UserTb usser;

    public ModelMain(ViewMain viewMain) {

        try {
            model = (DefaultTableModel) viewMain.jTableDataEncontradaFile.getModel();
            modelFindNot = (DefaultTableModel) viewMain.jTableNoEncontrado.getModel();
            this.componentManageBond = new ComponentManageBond();
            this.componentManageLoan = new ComponentManageLoan();
            this.componentManageUser = new ComponentManageUser();
            this.componentManageWorker = new ComponentManageWorker();

            TextFieldValidator.applyIntegerFilter(viewMain.jTextFieldChequeVoucher);
            TextFieldValidator.applyDecimalFilter(viewMain.jTextFieldMountVoucher);
            TextFieldValidator.applyIntegerFilter(viewMain.jTextFieldCuentaVoucher);

//        new ControllerManageLoan(componentManageLoan, user);
//        new ControllerManageBond(componentManageBond, user);
//        new ControllerManageWorker(componentManageWorker);
            this.viewMain = viewMain;
            init();
            combo();
        } catch (Exception e) {
        }

    }

    private void init() {

        componentLogin.jButtonIngreso.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logIn();
            }
        });
        componentLogin.jTextFieldUser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                componentLogin.jLabel1.setText("");
            }
        });
        componentLogin.jPasswordPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                componentLogin.jLabel1.setText("");
            }
        });
        viewMain.loading.setUndecorated(true);
        viewMain.loading.setSize(412, 115);
        viewMain.loading.setLocationRelativeTo(null);

        //  
        viewMain.setTitle("Subcafae - HSJ");
        ImageIcon icon = new ImageIcon(getClass().getResource("/IconGeneral/logoIcon.png"));

        viewMain.setIconImage(icon.getImage());
        //
        insert();
        //
        viewMain.setSize(1500, 800);
        viewMain.toFront();
        viewMain.setLocationRelativeTo(null);
        viewMain.setVisible(true);
    }

    public void eliminarCOmponent() {
        for (JInternalFrame frame : viewMain.jDesktopPane1.getAllFrames()) {
            System.out.println("Se elimna");
            viewMain.jDesktopPane1.remove(frame);
        }
        viewMain.jDesktopPane1.revalidate();
        viewMain.jDesktopPane1.repaint();
    }

    public void logIn() {

        if (new String(componentLogin.jPasswordPassword.getPassword()).isBlank()
                || componentLogin.jTextFieldUser.getText().isBlank()) {
            JOptionPane.showMessageDialog(null, "Llene los datos", "MENSAJE", JOptionPane.WARNING_MESSAGE);
            return;
        }
//
        usser = new UserDao().getUserByUsername(componentLogin.jTextFieldUser.getText(), new String(componentLogin.jPasswordPassword.getPassword()));

        if (usser != null) {
            componentLogin.jLabel1.setText("");
            if (usser.getState().equalsIgnoreCase("0")) {
                JOptionPane.showMessageDialog(null, "CUENTA BLOQUIADA", "MENSAJE", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!usser.getRol().equals("ADMINISTRADOR")) {
                viewMain.jMenuManageUser.setEnabled(false);
            } else {
                viewMain.jMenuManageUser.setEnabled(true);
            }
            if (!usser.getRol().equals("SUPER ADMINISTRADOR")) {
                viewMain.jMenuManageUser.setEnabled(false);
            } else {
                viewMain.jMenuManageUser.setEnabled(true);
            }
            new ControllerManageLoan(componentManageLoan, usser);
            new ControllerManageBond(componentManageBond, usser, viewMain);
            new ControllerManageWorker(componentManageWorker, usser);
            new ControllerUser(componentManageUser, usser);

            viewMain.jMenuBar1.setVisible(true);
            jpanelDarkUtil.setVisible(false);

        } else {
            componentLogin.jLabel1.setText("ERROR REVISE EL USUARIO O CONTRASEÑA");
        }
    }

    public void showLogin() {
        viewMain.jMenuBar1.setVisible(false);
        jpanelDarkUtil.setVisible(true);
    }

    public void insert() {
        viewMain.jMenuBar1.setVisible(false);

        viewMain.jLayeredPane1.setLayout(layout);
        //viewMain.setBackground(Color.WHITE);
        jpanelDarkUtil.add(componentLogin);
        //viewMain.jLayeredPane1.setLayer(jpanelDarkUtil, JLayeredPane.PALETTE_LAYER);

        viewMain.jLayeredPane1.add(jpanelDarkUtil, "pos 0 0 100% 100%");
        viewMain.jLayeredPane1.add(viewMain.jDesktopPane1, "pos 0 0 100% 100%");
        //
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

                String[] employee = empleadoDao.findAll()
                        .stream().filter(predicate -> predicate.getEmploymentStatus().equalsIgnoreCase(nombresBuscar)
                        ).map(
                                map -> map.getNationalId().concat(" - ".concat(map.getFullName()))
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

                viewMain.loading.setVisible(true);

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

                                    }
                                    System.out.println("fecha de vencimeinto  " + utilDate.toString());

                                    LocalDate fechaVencimiento = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                    if (LocalDate.now().isAfter(fechaVencimiento)) {
                                        System.out.println("Se guarda");
                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFullName(), employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(), detalle.getMonthly() - detalle.getPayment(), 0.0),
                                                (existente, nuevo) -> {
                                                    existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                    return existente;
                                                }
                                        );
                                    }

                                    if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth())) {

                                        mapaDniDatos.merge(
                                                employee1.getNationalId(),
                                                new DatosPersona(employee1.getFullName(), employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(), detalle.getMonthly() - detalle.getPayment(), 0.0),
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
                        if (loan1.getPaymentResponsibility().equalsIgnoreCase("EMPLOYEE")) {

                            if (employee1.getNationalId().equalsIgnoreCase(loan1.getEmployeeId())) {

                                for (LoanDetailsTb loanDetail1 : loanDetails) {

                                    if (loan1.getId() == loanDetail1.getLoanId() && (loanDetail1.getState().equalsIgnoreCase("Pendiente") || loanDetail1.getState().equalsIgnoreCase("Parcial"))) {

                                        LocalDate fechaVencimiento = loanDetail1.getPaymentDate().toLocalDate();

                                        System.out.println("Mes actual -> " + LocalDate.now().getMonth());
                                        System.out.println("Mes de pago -> " + fechaVencimiento.getMonth());

                                        if (LocalDate.now().isAfter(fechaVencimiento)) {

                                            mapaDniDatos.merge(
                                                    employee1.getNationalId(),
                                                    new DatosPersona(employee1.getFullName(),
                                                            employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                            loanDetail1.getMonthlyFeeValue() - loanDetail1.getPayment(), 0.0),
                                                    (existente, nuevo) -> {
                                                        existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                        return existente;
                                                    }
                                            );
                                        }

                                        if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth())) {

                                            mapaDniDatos.merge(
                                                    employee1.getNationalId(),
                                                    new DatosPersona(employee1.getFullName(),
                                                            employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                            loanDetail1.getMonthlyFeeValue() - loanDetail1.getPayment(), 0.0),
                                                    (existente, nuevo) -> {
                                                        existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                        return existente;
                                                    }
                                            );
                                        }

                                    }
                                }
                            }

                        } else {

                            if (employee1.getNationalId().equalsIgnoreCase(loan1.getGuarantorIds())) {

                                for (LoanDetailsTb loanDetail1 : loanDetails) {

                                    if (loan1.getId() == loanDetail1.getLoanId() && (loanDetail1.getState().equalsIgnoreCase("Pendiente") || loanDetail1.getState().equalsIgnoreCase("Parcial"))) {

                                        LocalDate fechaVencimiento = loanDetail1.getPaymentDate().toLocalDate();

                                        System.out.println("Mes actual -> " + LocalDate.now().getMonth());
                                        System.out.println("Mes de pago -> " + fechaVencimiento.getMonth());

                                        if (LocalDate.now().isAfter(fechaVencimiento)) {

                                            mapaDniDatos.merge(
                                                    employee1.getNationalId(),
                                                    new DatosPersona(employee1.getFullName(),
                                                            employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                            loanDetail1.getMonthlyFeeValue() - loanDetail1.getPayment(), 0.0),
                                                    (existente, nuevo) -> {
                                                        existente.sumarMonto(nuevo.getMonto(), nuevo.getPrestamo());
                                                        return existente;
                                                    }
                                            );
                                        }

                                        if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth())) {

                                            mapaDniDatos.merge(
                                                    employee1.getNationalId(),
                                                    new DatosPersona(employee1.getFullName(),
                                                            employee1.getNationalId() + " - " + employee1.getEmploymentStatusCode(),
                                                            loanDetail1.getMonthlyFeeValue() - loanDetail1.getPayment(), 0.0),
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

                String[] namee = listEmployee.stream().map(name -> name.getNationalId().concat(" - " + name.getFullName())).toArray(String[]::new);

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
                System.out.println("Entrando -------- ");
                EmployeeTb employeeFind = listEmployee.stream().filter(predicate -> predicate.getNationalId().equalsIgnoreCase(nombresBuscar.split("-")[0].trim())).findFirst().get();
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
                            modelBono.setMonto(String.valueOf(allAbonoDetail.getMonthly() - allAbonoDetail.getPayment()));
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
                            modelPrestamo.setMonto(String.valueOf(allLoanDetail.getMonthlyFeeValue() - allLoanDetail.getPayment()));
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
                System.out.println("Error -> " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un error", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                viewMain.loading.dispose();
                // Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();

    }

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
            return new EmployeeDao().findAll().stream().anyMatch(predicate -> viewMain.jComboBoxSearchClient.getSelectedItem().toString().equalsIgnoreCase(predicate.getFullName() + " - " + predicate.getNationalId()));
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
            if (item.getFullName().toLowerCase().contains(text.toLowerCase())
                    || item.getNationalId().toLowerCase().contains(text.toLowerCase())) {
                comboBox.addItem(item.getFullName() + " - " + item.getNationalId());
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

    public boolean esTxt(String archivo) {
        return archivo.toLowerCase().endsWith(".txt") && new File(archivo).isFile();
    }

    public void procesarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo TXT");

        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto", "txt"));

        int seleccion = fileChooser.showOpenDialog(null);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            if (esTxt(fileChooser.getSelectedFile().getAbsolutePath())) {
                File archivo = fileChooser.getSelectedFile();

                viewMain.loading.setVisible(true);
                viewMain.jInternalPagoPrestamosOtros.setEnabled(false);
                new Thread(() -> {

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

                        model.setRowCount(0);
                        modelFindNot.setRowCount(0);
                        viewMain.jLabelCode.setText(archivo.getName().substring(0, 4).equalsIgnoreCase("2154") ? "DOCUMENTO TIPO NOMBRADO" : "DOCUMENTO TIPO CAS");
                        String linea;
                        int count = 0;
                        String ver = null;

                        // Convertir a LocalDate, añadiendo el día por defecto
                        LocalDate localDate = null;
                        try {
                            localDate = LocalDate.parse("01/" + archivo.getName().substring(4, 6) + "/" + archivo.getName().substring(6, 8), DateTimeFormatter.ofPattern("dd/MM/yy"));
                        } catch (Exception e) {
                            return;
                        }

                        if (localDate.getYear() == LocalDate.now().getYear() && localDate.getMonth().equals(LocalDate.now().getMonth())) {

                            while ((linea = reader.readLine()) != null) { // Leer línea por línea
                                count++;
                                System.out.println("Línea completa: " + linea); // Imprimir línea para depuración

                                ver = extraerDatos(linea, count); // Procesar cada línea
                                if (ver == null || ver.equalsIgnoreCase("2") || ver.equalsIgnoreCase("4")) {
                                    break; // Detener si es necesario
                                }

                            }

                            switch (ver) {
                                case "2":
                                    viewMain.jLabelCode.setText("");
                                    viewMain.jLabelCantidad.setText("");
                                    model.setRowCount(0);
                                    modelFindNot.setRowCount(0);
                                    JOptionPane.showMessageDialog(null, "Ocurrió un problema", "ERROR GENERAL", JOptionPane.WARNING_MESSAGE);
                                    break;

                                case "4":
                                    viewMain.jLabelCode.setText("");
                                    viewMain.jLabelCantidad.setText("");
                                    model.setRowCount(0);
                                    modelFindNot.setRowCount(0);
                                    JOptionPane.showMessageDialog(null, "ERROR DE PROCESO DE UNA LÍNEA DOCUMENTO CORRUPTO", "ERROR DE DOCUMENTO", JOptionPane.WARNING_MESSAGE);
                                    break;
                            }

                        } else {
                            viewMain.jLabelCode.setText("");
                            JOptionPane.showMessageDialog(null, "FECHA INCOPATIBLE CON EL ACTUAL");
                        }
                        viewMain.jLabelCantidad.setText(String.valueOf(count)); // Actualizar etiqueta con el conteo
                        viewMain.loading.dispose();
                        viewMain.jInternalPagoPrestamosOtros.setEnabled(true);
                    } catch (IOException e) {
                        System.out.println("Error -> " + e.getMessage());
                        viewMain.loading.dispose();
                        viewMain.jInternalPagoPrestamosOtros.setEnabled(true);
                        JOptionPane.showMessageDialog(null, "ERROR EN NOMBRE DEL DOCUMENTO");
                    }
                }).start();

            }

        } else {
            JOptionPane.showMessageDialog(null, "Archivo no compatible");
        }
    }

    private String extraerDatos(String linea, int count) {
        try {
            int posMonto = linea.indexOf("0604");

            if (posMonto == -1) {
                mapCom.clear();
                model.setRowCount(0);
                modelFindNot.setRowCount(0);
                return "4";
            }
            String code = linea.substring(0, 4).trim();
            String mes = linea.substring(4, 6).trim();
            String anio = linea.substring(6, 8).trim();

            String nombre = linea.substring(8, posMonto).replaceAll("[0-9]", "").trim();
            String montoStr = linea.substring(8, posMonto).trim().replaceAll("[^0-9]", "");

            double monto = Double.parseDouble(montoStr) / 100;

            List<EmployeeTb> empleadoFind = new EmployeeDao().findEmployeesByFullName(nombre);

            if (empleadoFind.size() == 1) {

                mapCom.put(empleadoFind.getFirst(), monto);
                model.addRow(new Object[]{
                    count,
                    empleadoFind.getFirst().getEmploymentStatusCode(),
                    mes + "/" + anio,
                    empleadoFind.getFirst().getFullName(),
                    monto
                });
            }

            if (empleadoFind.isEmpty()) {
                modelFindNot.addRow(new Object[]{
                    count,
                    code,
                    mes + "/" + anio,
                    nombre,
                    monto
                });
            }

            return "1";

        } catch (Exception e) {

            model.setRowCount(0);
            modelFindNot.setRowCount(0);
            mapCom.clear();
            System.out.println("Error /-> " + e.getMessage());
            return "2";
        }
    }

    public void procedRegistroDesc() {

        if (mapCom.isEmpty()) {
            JOptionPane.showMessageDialog(null, "ERROR NO HAY USUARIOS A CUAL DESCONTAR", "ERROR DE DOCUMENTO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ViewMain.loading.setVisible(true);

        new Thread(() -> {
            for (Map.Entry<EmployeeTb, Double> entry : mapCom.entrySet()) {

                try {

                    RegistroTb registroTb = new RegistroTb(entry.getKey().getEmployeeId(), entry.getValue());
                    List<LoanDetailsTb> prestamos = new ArrayList<>();
                    List<AbonoDetailsTb> bonos = new ArrayList<>();

                    List<AbonoTb> abonoList = new AbonoDao().findAbonosByEmployeeAndCurrentYear(entry.getKey().getEmployeeId().toString());
                    List<LoanTb> prestamoList = new LoanDao().findLoansByEmployeeId(entry.getKey().getNationalId());

                    for (LoanTb loanTb : prestamoList) {
                        for (LoanDetailsTb loanDetailsTb : new LoanDetailsDao().findLoanDetailsByLoanId(loanTb.getId())) {
                            if (loanDetailsTb.getState().equalsIgnoreCase("Pendiente") || loanDetailsTb.getState().equalsIgnoreCase("Parcial")) {
                                LocalDate fechaVencimiento = loanDetailsTb.getPaymentDate().toLocalDate();
                                if (LocalDate.now().isAfter(fechaVencimiento) || (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()))) {
                                    System.out.println("Se encuentra un prestamo pendiente");
                                    prestamos.add(loanDetailsTb);
                                }
                            }
                        }
                    }

                    List<AbonoDetailsTb> firtsServ = new ArrayList<>();
                    List<AbonoDetailsTb> secondServ = new ArrayList<>();

                    for (AbonoTb abonoTb : abonoList) {

                        ServiceConceptTb serve = new ServiceConceptDao().getAllServiceConcepts().stream().filter(predicate -> predicate.getId() == Integer.parseInt(abonoTb.getServiceConceptId())).findFirst().get();
                        if (!serve.getPriorityConcept().equalsIgnoreCase("Segundo")) {
                            for (AbonoDetailsTb abonoDetailsTb : new AbonoDetailsDao().findAbonoDetailsByAbonoId(abonoTb.getId())) {
                                firtsServ.add(abonoDetailsTb);
                            }

                        } else {
                            for (AbonoDetailsTb abonoDetailsTb : new AbonoDetailsDao().findAbonoDetailsByAbonoId(abonoTb.getId())) {
                                secondServ.add(abonoDetailsTb);
                            }
                        }
                    }

                    //
                    for (AbonoDetailsTb abonoDetailsTb : firtsServ) {

                        if (abonoDetailsTb.getState().equalsIgnoreCase("Pendiente") || abonoDetailsTb.getState().equalsIgnoreCase("Parcial")) {

                            LocalDate payment = LocalDate.parse(abonoDetailsTb.getPaymentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                            if (LocalDate.now().isAfter(payment) || (LocalDate.now().getMonth().equals(payment.getMonth()))) {
                                System.out.println("Se encontro una deuda de abono primer");
                                bonos.add(abonoDetailsTb);
                            }
                        }
                    }

                    for (AbonoDetailsTb abonoDetailsTb : secondServ) {

                        if (abonoDetailsTb.getState().equalsIgnoreCase("Pendiente") || abonoDetailsTb.getState().equalsIgnoreCase("Parcial")) {

                            LocalDate payment = LocalDate.parse(abonoDetailsTb.getPaymentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                            if (LocalDate.now().isAfter(payment) || (LocalDate.now().getMonth().equals(payment.getMonth()))) {
                                System.out.println("Se encontro una deuda de abono de segundo plano ");
                                bonos.add(abonoDetailsTb);
                            }
                        }
                    }

//                new LoanDetailsDao().updateLoanStateByLoandetailId(loanDetailsTb.getId(), loanDetailsTb.getMonthlyFeeValue(), monto);
//                new AbonoDetailsDao().updateLoanStateByLoandetailId(abonoDetailsTb.getId(), abonoDetailsTb.getMonthly(), montoFinal);
                    double cantidad = entry.getValue();

                    List<LoanDetailsTb> prestamos2 = new ArrayList<>();
                    List<AbonoDetailsTb> bonos2 = new ArrayList<>();

                    for (LoanDetailsTb prestamo : prestamos) {
                        if (cantidad == 0.0) {
                            break;
                        }
                        double monotp = prestamo.getPayment();
                        double monotoPayme = prestamo.getMonthlyFeeValue() - monotp;

                        if (cantidad >= monotoPayme) {
                            new LoanDetailsDao().updateLoanStateByLoandetailId(prestamo.getId(), prestamo.getMonthlyFeeValue(), monotoPayme);
                            prestamo.setMonto(monotoPayme);
                            prestamos2.add(prestamo);
                            cantidad -= monotoPayme;
                        } else {
                            new LoanDetailsDao().updateLoanStateByLoandetailId(prestamo.getId(), prestamo.getMonthlyFeeValue(), cantidad);
                            prestamo.setMonto(cantidad); 
                            prestamos2.add(prestamo);
                            cantidad -= cantidad;
                        }
                    }

                    for (AbonoDetailsTb bono : bonos) {
                        if (cantidad == 0.0) {
                            break;
                        }

                        double monotp = bono.getPayment();
                        double monotoPayme = bono.getMonthly() - monotp;

                        if (cantidad >= monotoPayme) {
                            new AbonoDetailsDao().updateLoanStateByLoandetailId(bono.getId(), bono.getMonthly(), monotoPayme);
                            bono.setMonto(monotoPayme);
                            bonos2.add(bono);
                            cantidad -= monotoPayme;
                        } else {
                           
                            new AbonoDetailsDao().updateLoanStateByLoandetailId(bono.getId(), bono.getMonthly(), cantidad);
                            bono.setMonto(cantidad); 
                            bonos2.add(bono);
                            cantidad -= cantidad;
                        }

                    }

                    new RegistroDao().insertarRegistroCompleto(registroTb, prestamos2, bonos2);

                } catch (SQLException ex) {
                    ViewMain.loading.setVisible(false);
                    JOptionPane.showMessageDialog(null, "Ocurrio un error", "MENSAJE", JOptionPane.WARNING_MESSAGE);
                    Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            ViewMain.loading.setVisible(false);
            JOptionPane.showMessageDialog(null, "Se registro", "MENSAJE", JOptionPane.WARNING_MESSAGE);

        }).start();

        //   viewMain.loading.dispose();
    }
}
