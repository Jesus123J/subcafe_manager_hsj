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
import com.subcafae.finantialtracker.view.component.ComponentSearchEmpl;
import com.sun.jna.platform.win32.WinBase;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.time.YearMonth;
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
                // ENTER en usuario -> mover foco a contraseña
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    componentLogin.jPasswordPassword.requestFocusInWindow();
                }
            }
        });
        componentLogin.jPasswordPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                componentLogin.jLabel1.setText("");
                // ENTER en contraseña -> ejecutar login
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    logIn();
                }
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
        try {
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
        } catch (Exception ex) {
            System.out.println("Error -> " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al iniciar sesión. Verifique la conexión.", "ERROR", JOptionPane.ERROR_MESSAGE);
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

        // Configurar estilos modernos y comportamiento del JDesktopPane
        setupModernDesktopPane();

        // Listener para ajustar JInternalFrames cuando se redimensiona el JDesktopPane
        viewMain.jDesktopPane1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustInternalFrames();
            }
        });
    }

    // Ajustar posicion de JInternalFrames para que siempre sean visibles
    private void adjustInternalFrames() {
        int desktopWidth = viewMain.jDesktopPane1.getWidth();
        int desktopHeight = viewMain.jDesktopPane1.getHeight();

        for (JInternalFrame frame : viewMain.jDesktopPane1.getAllFrames()) {
            if (!frame.isVisible()) continue;

            int frameX = frame.getX();
            int frameY = frame.getY();
            int frameWidth = frame.getWidth();
            int frameHeight = frame.getHeight();

            // Barra de titulo debe estar siempre visible (donde estan los botones cerrar, minimizar)
            int titleBarHeight = 30;
            int minVisibleWidth = 150; // Minimo visible para poder arrastrar y ver botones

            // Asegurar que Y nunca sea negativo (barra de titulo siempre visible)
            if (frameY < 0) {
                frameY = 0;
            }

            // Asegurar que la barra de titulo no se salga por abajo
            if (frameY > desktopHeight - titleBarHeight) {
                frameY = Math.max(0, desktopHeight - titleBarHeight);
            }

            // Asegurar que al menos minVisibleWidth pixeles sean visibles horizontalmente
            if (frameX + frameWidth < minVisibleWidth) {
                frameX = minVisibleWidth - frameWidth;
            }

            // Si el frame se sale por la derecha, ajustar para que sea visible
            if (frameX > desktopWidth - minVisibleWidth) {
                frameX = desktopWidth - minVisibleWidth;
            }

            // Si el frame es mas grande que el desktop, redimensionarlo
            if (frameWidth > desktopWidth && desktopWidth > 100) {
                frame.setSize(desktopWidth - 20, frameHeight);
                frameX = 10;
            }
            if (frameHeight > desktopHeight && desktopHeight > 100) {
                frame.setSize(frameWidth, desktopHeight - 20);
                frameY = 10;
            }

            // Aplicar nueva posicion si cambio
            if (frameX != frame.getX() || frameY != frame.getY()) {
                frame.setLocation(frameX, frameY);
            }
        }
    }

    // Configurar comportamiento del JDesktopPane
    private void setupModernDesktopPane() {
        // Configurar el DesktopManager personalizado para mejor comportamiento
        viewMain.jDesktopPane1.setDesktopManager(new javax.swing.DefaultDesktopManager() {
            @Override
            public void dragFrame(javax.swing.JComponent f, int newX, int newY) {
                // Limitar el arrastre para que la barra de titulo siempre sea visible
                int desktopWidth = viewMain.jDesktopPane1.getWidth();
                int desktopHeight = viewMain.jDesktopPane1.getHeight();
                int frameWidth = f.getWidth();

                // No permitir que Y sea negativo
                if (newY < 0) newY = 0;

                // No permitir que la barra de titulo se salga por abajo
                if (newY > desktopHeight - 30) newY = desktopHeight - 30;

                // Mantener al menos 100px visibles horizontalmente
                if (newX + frameWidth < 100) newX = 100 - frameWidth;
                if (newX > desktopWidth - 100) newX = desktopWidth - 100;

                super.dragFrame(f, newX, newY);
            }
        });
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

        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                EmployeeDao empleadoDao = new EmployeeDao();
                List<EmployeeTb> employees = empleadoDao.findAll();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();

                    if (employees.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No hay empleados registrados", "GESTIÓN DE PAGOS", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    centerInternalComponent(new ComponentSearchEmpl("GESTIÓN DE PAGOS", employees, false, viewMain));
                });

            } catch (Exception ex) {
                System.out.println("Error -> " + ex.getMessage());
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "Ocurrió un error al cargar empleados: " + ex.getMessage(), "GESTIÓN DE PAGOS", JOptionPane.WARNING_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
    }

    public void generateExcel() {

        String[] contractTypeOptions = {"CAS", "Nombrado"};

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

        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {

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
                loan = new LoanDao().getAllLoans().stream().filter(predicate -> predicate.getState().equalsIgnoreCase("Aceptado") && predicate.getStateLoan().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());

                if (employees.isEmpty()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        viewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "No hay Trabajador tipo ".concat(contractType));
                    });
                    return;
                }

                Map<String, DatosPersona> mapaDniDatos = new HashMap<>(); // Cambiar a clave String si el DNI es String

                for (AbonoTb abono : bonos) {

                    int id = Integer.parseInt(abono.getEmployeeId());

                    for (EmployeeTb employee1 : employees) {

                        if (employee1.getEmployeeId() == id) {

                            for (AbonoDetailsTb detalle : new AbonoDao().getListAbonoBySoli(abono.getSoliNum())) {

                                if (abono.getId() == detalle.getAbonoID()) {

                                    Date utilDate = null;

                                    if (!detalle.getState().equalsIgnoreCase("Pagado")) {

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
                                        } else if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()) && LocalDate.now().getYear() == fechaVencimiento.getYear()) {

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

                }

                for (LoanTb loan1 : loan) {

                    for (EmployeeTb employee1 : employees) {
                        if (loan1.getPaymentResponsibility().equalsIgnoreCase("EMPLOYEE")) {

                            if (employee1.getNationalId().equalsIgnoreCase(loan1.getEmployeeId())) {

                                for (LoanDetailsTb loanDetail1 : new LoanDetailsDao().findLoanDetailsByLoanId(loan1.getId())) {

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
                                        } else if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()) && LocalDate.now().getYear() == fechaVencimiento.getYear()) {

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

                                for (LoanDetailsTb loanDetail1 : new LoanDetailsDao().findLoanDetailsByLoanId(loan1.getId())) {

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

                                        } else if (LocalDate.now().getMonth().equals(fechaVencimiento.getMonth()) && LocalDate.now().getYear() == fechaVencimiento.getYear()) {

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

                // Ordenar alfabéticamente por nombre
                datosLista.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));

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

                if (datosLista.isEmpty()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        viewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "No hay datos para generar el reporte de tipo " + contractType, "REPORTE DESCUENTO", JOptionPane.INFORMATION_MESSAGE);
                    });
                    return;
                }

                ExcelExporter exporter = new ExcelExporter();
                exporter.generateExcel(listExecel);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                });

            } catch (Exception ex) {
                System.out.println("Error -> " + ex.getMessage());
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "Ocurrió un error al generar el reporte: " + ex.getMessage(), "REPORTE DESCUENTO", JOptionPane.WARNING_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
    }

    public void reportDeuda() {

        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                List<EmployeeTb> listEmployee = new EmployeeDao().findAll();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();

                    if (listEmployee.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No hay ningún empleado.", "GESTIÓN DE DEUDAS", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    centerInternalComponent(new ComponentSearchEmpl("GESTIÓN DE DEUDAS", listEmployee, true, viewMain));
                });

            } catch (Exception ex) {
                System.out.println("Error -> " + ex.getMessage());
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "Ocurrió un problema al cargar la lista de empleados: " + ex.getMessage(), "GESTIÓN DE DEUDAS", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
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
                nombreArchivoCargado = archivo.getName(); // Guardar nombre del archivo

                viewMain.loading.setModal(true);
                viewMain.loading.setLocationRelativeTo(viewMain);

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

                        //if (localDate.getYear() == LocalDate.now().getYear() && localDate.getMonth().equals(LocalDate.now().getMonth())) {
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

//                        } else {
//                            viewMain.jLabelCode.setText("");
//                            JOptionPane.showMessageDialog(null, "FECHA INCOPATIBLE CON EL ACTUAL");
//                        }
                        final int finalCount = count;
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            viewMain.jLabelCantidad.setText(String.valueOf(finalCount));
                            viewMain.loading.dispose();
                        });
                    } catch (Exception e) {
                        System.out.println("Error -> " + e.getMessage());
                        e.printStackTrace();
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            viewMain.loading.dispose();
                            JOptionPane.showMessageDialog(null, "ERROR EN NOMBRE DEL DOCUMENTO: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();

                viewMain.loading.setVisible(true);
            }

        } else {
            JOptionPane.showMessageDialog(null, "Archivo no compatible");
        }
    }

    private String mes;
    private String anio;
    private String nombreArchivoCargado;

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
            mes = linea.substring(4, 6).trim();
            anio = linea.substring(6, 8).trim();

            String nombre = linea.substring(8, posMonto).replaceAll("[0-9]", "").trim();
            String montoStr = linea.substring(8, posMonto).trim().replaceAll("[^0-9]", "");

            double monto = Double.parseDouble(montoStr) / 100;

            if (nombre.contains("\uFFFD")) {
                nombre = nombre.replaceAll("\uFFFD", "Ñ");
            }

            List<EmployeeTb> empleadoFind = new EmployeeDao().findEmployeesByFullName(nombre);

            if (empleadoFind.size() == 1) {

                mapCom.put(empleadoFind.get(0), monto);
                model.addRow(new Object[]{
                    count,
                    empleadoFind.get(0).getEmploymentStatusCode(),
                    mes + "/" + anio,
                    empleadoFind.get(0).getFullName(),
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

        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(
                null,
                "Atención:\n\n"
                + "El archivo cargado pertenece al mes y año: " + mes + "/" + anio + ".\n"
                + "Los descuentos se aplicarán únicamente a cuotas vencidas hasta este mes inclusive.\n\n"
                + "¿Deseas continuar con el proceso?",
                "Confirmación requerida",
                JOptionPane.YES_NO_OPTION
        )) {
            return;
        }

        ViewMain.loading.setModal(true);
        ViewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            boolean ver = false;
            int cat = 0;
            int registrosProcesados = 0;
            double montoTotalLote = 0.0;

            // Corregir la creación de la fecha de referencia
            int añoCompleto = Integer.parseInt(anio);
            if (Integer.parseInt(anio) < 100) {
                // Asumir que son años 2000+
                añoCompleto = 2000 + Integer.parseInt(anio);
            }

            LocalDate fechaReferencia = LocalDate.of(añoCompleto, Integer.parseInt(mes), 1).withDayOfMonth(1).plusMonths(1).minusDays(1);

            // Crear el lote de carga masiva
            RegistroDao registroDao = new RegistroDao();
            String usuarioCarga = (usser != null && usser.getUsername() != null) ? usser.getUsername() : "SISTEMA";
            int loteId = registroDao.crearLoteCarga(nombreArchivoCargado, mes, String.valueOf(añoCompleto), usuarioCarga);

            if (loteId == -1) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    ViewMain.loading.dispose();
                    JOptionPane.showMessageDialog(null, "Error al crear el lote de carga", "ERROR", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            System.out.println("Lote creado con ID: " + loteId);

            for (Map.Entry<EmployeeTb, Double> entry : mapCom.entrySet()) {
                try {
                    double cantidad = entry.getValue(); // monto pagado desde archivo

                    // Validar si el monto es cero o negativo
                    if (cantidad <= 0.0) {
                        continue;
                    }

                    RegistroTb registroTb = new RegistroTb(entry.getKey().getEmployeeId(), cantidad);

                    List<LoanDetailsTb> prestamosPendientes = new ArrayList<>();
                    List<AbonoDetailsTb> bonosPendientes = new ArrayList<>();

                    // ======================= OBTENER PRÉSTAMOS ============================
                    List<LoanTb> prestamoList = new LoanDao().findLoansByEmployeeId(entry.getKey().getNationalId());

                    for (LoanTb loanTb : prestamoList) {
                        // VALIDACIÓN: Si el préstamo ya está PAGADO, no procesar sus cuotas
                        if (loanTb.getStateLoan() != null && loanTb.getStateLoan().equalsIgnoreCase("Pagado")) {
                            System.out.println("⚠ Préstamo #" + loanTb.getSoliNum() + " ya está PAGADO. Omitiendo cuotas.");
                            continue; // Saltar este préstamo completamente
                        }

                        List<LoanDetailsTb> detalles = new LoanDetailsDao().findLoanDetailsByLoanId(loanTb.getId());

                        for (LoanDetailsTb d : detalles) {
                            if (d.getState().equalsIgnoreCase("Pendiente") || d.getState().equalsIgnoreCase("Parcial")) {
                                // Solo incluir préstamos con fecha hasta el mes del archivo
                                LocalDate fechaPago = d.getPaymentDate().toLocalDate();
                                if (!fechaPago.isAfter(fechaReferencia)) {
                                    prestamosPendientes.add(d);
                                }
                            }
                        }
                    }

                    // ORDENAR PRÉSTAMOS: vencidos → del mes del archivo
                    prestamosPendientes.sort((p1, p2) -> {
                        LocalDate f1 = p1.getPaymentDate().toLocalDate();
                        LocalDate f2 = p2.getPaymentDate().toLocalDate();

                        boolean v1 = f1.isBefore(fechaReferencia.withDayOfMonth(1));
                        boolean v2 = f2.isBefore(fechaReferencia.withDayOfMonth(1));

                        if (v1 != v2) {
                            return v1 ? -1 : 1;
                        }
                        return f1.compareTo(f2);
                    });

                    // ======================= OBTENER BONOS ============================
                    List<AbonoTb> abonoList = new AbonoDao().findAbonosByEmployeeAndCurrentYear(entry.getKey().getEmployeeId().toString());

                    List<AbonoDetailsTb> firstServ = new ArrayList<>();
                    List<AbonoDetailsTb> secondServ = new ArrayList<>();

                    for (AbonoTb abonoTb : abonoList) {
                        // VALIDACIÓN: Si el abono ya está PAGADO, no procesar sus cuotas
                        if (abonoTb.getStatus() != null && abonoTb.getStatus().equalsIgnoreCase("Pagado")) {
                            System.out.println("⚠ Abono #" + abonoTb.getSoliNum() + " ya está PAGADO. Omitiendo cuotas.");
                            continue; // Saltar este abono completamente
                        }

                        ServiceConceptTb serve = new ServiceConceptDao().getAllServiceConcepts()
                                .stream()
                                .filter(s -> s.getId() == Integer.parseInt(abonoTb.getServiceConceptId()))
                                .findFirst().orElse(null);

                        if (serve != null) {
                            List<AbonoDetailsTb> detalles = new AbonoDetailsDao().findAbonoDetailsByAbonoId(abonoTb.getId());

                            if (!serve.getPriorityConcept().equalsIgnoreCase("Segundo")) {
                                firstServ.addAll(detalles);
                            } else {
                                secondServ.addAll(detalles);
                            }
                        }
                    }

                    // FILTRAR BONOS hasta el mes del archivo
                    List<AbonoDetailsTb> bonosOrdenados = new ArrayList<>();

                    bonosOrdenados.addAll(
                            ordenarBonosPorVencimiento(firstServ, fechaReferencia)
                    );
                    bonosOrdenados.addAll(
                            ordenarBonosPorVencimiento(secondServ, fechaReferencia)
                    );

                    bonosPendientes.addAll(bonosOrdenados);

                    // ======================= DESCONTAR MONTO ============================
                    List<LoanDetailsTb> prestamosPagados = new ArrayList<>();
                    List<AbonoDetailsTb> bonosPagados = new ArrayList<>();

                    // ---- PAGAR PRÉSTAMOS ----
                    for (LoanDetailsTb prestamo : prestamosPendientes) {
                        if (cantidad <= 0) {
                            break;
                        }

                        double pagadoActual = prestamo.getPayment();
                        double faltante = prestamo.getMonthlyFeeValue() - pagadoActual;

                        // Validar que haya un monto faltante real para pagar
                        if (faltante > 0) {
                            if (cantidad >= faltante) {
                                new LoanDetailsDao().updateLoanStateByLoandetailId(prestamo.getId(), prestamo.getMonthlyFeeValue(), faltante);
                                prestamo.setMonto(faltante);
                                prestamosPagados.add(prestamo);
                                cantidad -= faltante;
                            } else {
                                new LoanDetailsDao().updateLoanStateByLoandetailId(prestamo.getId(), prestamo.getMonthlyFeeValue(), cantidad);
                                prestamo.setMonto(cantidad);
                                prestamosPagados.add(prestamo);
                                cantidad = 0;
                            }
                        }
                    }

                    // ---- PAGAR BONOS ----
                    for (AbonoDetailsTb bono : bonosPendientes) {
                        if (cantidad <= 0) {
                            break;
                        }

                        double pagadoActual = bono.getPayment();
                        double faltante = bono.getMonthly() - pagadoActual;

                        // Validar que haya un monto faltante real para pagar
                        if (faltante > 0) {
                            if (cantidad >= faltante) {
                                new AbonoDetailsDao().updateLoanStateByLoandetailId(bono.getId(), bono.getMonthly(), faltante);
                                bono.setMonto(faltante);
                                bonosPagados.add(bono);
                                cantidad -= faltante;
                            } else {
                                new AbonoDetailsDao().updateLoanStateByLoandetailId(bono.getId(), bono.getMonthly(), cantidad);
                                bono.setMonto(cantidad);
                                bonosPagados.add(bono);
                                cantidad = 0;
                            }
                        }
                    }

                    // ======================= INSERT DEL REGISTRO ============================
                    // Solo insertar registro si realmente se procesó algún pago
                    if (!prestamosPagados.isEmpty() || !bonosPagados.isEmpty()) {
                        cat = registroDao.insertarRegistroCompletoConLote(registroTb, prestamosPagados, bonosPagados, loteId);
                        if (cat == 1) {
                            ver = true;
                            registrosProcesados++;
                            montoTotalLote += entry.getValue();
                        }
                    }

                } catch (Exception ex) {
                    System.out.println("Error -> " + ex.getMessage());
                    ex.printStackTrace();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        ViewMain.loading.dispose();
                        JOptionPane.showMessageDialog(null, "Ocurrió un error: " + ex.getMessage(), "MENSAJE", JOptionPane.WARNING_MESSAGE);
                    });
                    return;
                }
            }

            // Actualizar estadísticas del lote
            final int regProc = registrosProcesados;
            final double montoTotal = montoTotalLote;
            registroDao.actualizarEstadisticasLote(loteId, regProc, montoTotal);

            final boolean resultado = ver;
            final int loteIdFinal = loteId;
            javax.swing.SwingUtilities.invokeLater(() -> {
                ViewMain.loading.dispose();
                if (!resultado) {
                    JOptionPane.showMessageDialog(null, "No hubo nada que descontar", "MENSAJE", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "<html><body>" +
                        "<h3 style='color:#27AE60;'>CARGA MASIVA COMPLETADA</h3>" +
                        "<p><b>Lote ID:</b> " + loteIdFinal + "</p>" +
                        "<p><b>Registros procesados:</b> " + regProc + "</p>" +
                        "<p><b>Monto total:</b> S/ " + String.format("%.2f", montoTotal) + "</p>" +
                        "</body></html>",
                        "MENSAJE", JOptionPane.INFORMATION_MESSAGE);
                }
            });

        }).start();

        ViewMain.loading.setVisible(true);
    }

    private List<AbonoDetailsTb> ordenarBonosPorVencimiento(List<AbonoDetailsTb> lista, LocalDate fechaReferencia) {

        List<AbonoDetailsTb> pendientes = lista.stream()
                .filter(b -> {
                    String state = b.getState();
                    boolean estadoValido = state.equalsIgnoreCase("Pendiente") || state.equalsIgnoreCase("Parcial");

                    if (!estadoValido) {
                        return false;
                    }

                    // Filtrar por fecha
                    try {
                        LocalDate fechaPago = LocalDate.parse(b.getPaymentDate());
                        return !fechaPago.isAfter(fechaReferencia);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return pendientes.stream()
                .sorted((b1, b2) -> {
                    try {
                        LocalDate f1 = LocalDate.parse(b1.getPaymentDate());
                        LocalDate f2 = LocalDate.parse(b2.getPaymentDate());

                        boolean v1 = f1.isBefore(fechaReferencia.withDayOfMonth(1));
                        boolean v2 = f2.isBefore(fechaReferencia.withDayOfMonth(1));

                        if (v1 != v2) {
                            return v1 ? -1 : 1;
                        }
                        return f1.compareTo(f2);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Método para revertir un pago realizado
     * Muestra un diálogo para elegir entre revertir pago individual o carga masiva
     */
    public void revertirPago() {
        // Preguntar qué tipo de reversión desea hacer
        String[] opciones = {"Pago Individual", "Carga Masiva (Lote)", "Cancelar"};
        int tipoReversion = JOptionPane.showOptionDialog(
            viewMain,
            "<html><body style='width: 300px;'>" +
            "<h3 style='color:#C0392B;'>SELECCIONE TIPO DE REVERSIÓN</h3>" +
            "<p><b>Pago Individual:</b> Revertir un solo recibo por su código</p>" +
            "<p><b>Carga Masiva:</b> Revertir todos los pagos de un lote de archivo</p>" +
            "</body></html>",
            "REVERTIR PAGO",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );

        if (tipoReversion == 1) {
            // Carga masiva
            revertirCargaMasiva();
            return;
        } else if (tipoReversion != 0) {
            // Cancelar o cerrar
            return;
        }

        // Continuar con pago individual
        // Panel personalizado para el diálogo
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setLayout(new java.awt.GridLayout(3, 1, 5, 10));
        panel.setPreferredSize(new java.awt.Dimension(400, 120));

        javax.swing.JLabel lblInstrucciones = new javax.swing.JLabel(
            "<html><b style='color:#C0392B;'>⚠ ADVERTENCIA: Esta acción es irreversible</b><br/>" +
            "Ingrese el código del recibo a revertir:</html>"
        );

        javax.swing.JTextField txtCodigoRecibo = new javax.swing.JTextField();
        txtCodigoRecibo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        txtCodigoRecibo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(52, 73, 94), 2),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        javax.swing.JLabel lblEjemplo = new javax.swing.JLabel(
            "<html><i style='color:gray;'>Ejemplo: P/0001-00001273</i></html>"
        );

        panel.add(lblInstrucciones);
        panel.add(txtCodigoRecibo);
        panel.add(lblEjemplo);

        // Mostrar diálogo de confirmación inicial
        int opcion = JOptionPane.showConfirmDialog(
            viewMain,
            panel,
            "REVERTIR PAGO INDIVIDUAL",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        String codigoRecibo = txtCodigoRecibo.getText().trim();
        if (codigoRecibo.isEmpty()) {
            JOptionPane.showMessageDialog(viewMain,
                "Debe ingresar un código de recibo válido",
                "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mostrar loading y procesar en hilo separado
        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                // Buscar el registro por código
                com.subcafae.finantialtracker.data.dao.RegistroDao registroDao =
                    new com.subcafae.finantialtracker.data.dao.RegistroDao();

                var registroOpt = registroDao.findByCodigo(codigoRecibo);

                if (registroOpt.isEmpty()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        viewMain.loading.dispose();
                        JOptionPane.showMessageDialog(viewMain,
                            "No se encontró ningún registro con el código: " + codigoRecibo,
                            "REGISTRO NO ENCONTRADO", JOptionPane.WARNING_MESSAGE);
                    });
                    return;
                }

                var registro = registroOpt.get();

                // Obtener detalles del pago
                String detallesPago = registroDao.obtenerDetallesPagoParaRevertir(registro.getId());

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();

                    // Mostrar confirmación con detalles
                    String mensajeConfirmacion =
                        "<html><body style='width: 350px;'>" +
                        "<h3 style='color:#C0392B;'>¿ESTÁ SEGURO DE REVERTIR ESTE PAGO?</h3>" +
                        "<hr/>" +
                        "<b>Código:</b> " + registro.getCodigo() + "<br/>" +
                        "<b>Fecha:</b> " + registro.getFechaRegistro() + "<br/>" +
                        "<b>Monto:</b> S/ " + String.format("%.2f", registro.getAmount()) + "<br/>" +
                        "<hr/>" +
                        "<b>Detalles del pago:</b><br/>" + detallesPago +
                        "<hr/>" +
                        "<p style='color:#E74C3C;'><b>Esta acción eliminará:</b></p>" +
                        "<ul>" +
                        "<li>El registro de pago</li>" +
                        "<li>Los detalles asociados (préstamos/abonos)</li>" +
                        "<li>Actualizará los saldos de las cuotas afectadas</li>" +
                        "</ul>" +
                        "</body></html>";

                    int confirmacion = JOptionPane.showConfirmDialog(
                        viewMain,
                        mensajeConfirmacion,
                        "CONFIRMAR REVERSIÓN DE PAGO",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );

                    if (confirmacion != JOptionPane.YES_OPTION) {
                        return;
                    }

                    // Solicitar motivo de la reversión
                    javax.swing.JPanel panelMotivo = new javax.swing.JPanel();
                    panelMotivo.setLayout(new java.awt.BorderLayout(5, 10));
                    panelMotivo.setPreferredSize(new java.awt.Dimension(400, 150));

                    javax.swing.JLabel lblMotivo = new javax.swing.JLabel(
                        "<html><b style='color:#C0392B;'>MOTIVO DE LA REVERSIÓN</b><br/>" +
                        "<i style='color:gray;'>Ingrese la razón por la cual se revierte este pago:</i></html>"
                    );

                    javax.swing.JTextArea txtMotivo = new javax.swing.JTextArea(4, 30);
                    txtMotivo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                    txtMotivo.setLineWrap(true);
                    txtMotivo.setWrapStyleWord(true);
                    txtMotivo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(52, 73, 94), 2),
                        javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));

                    javax.swing.JScrollPane scrollMotivo = new javax.swing.JScrollPane(txtMotivo);

                    panelMotivo.add(lblMotivo, java.awt.BorderLayout.NORTH);
                    panelMotivo.add(scrollMotivo, java.awt.BorderLayout.CENTER);

                    int opcionMotivo = JOptionPane.showConfirmDialog(
                        viewMain,
                        panelMotivo,
                        "MOTIVO DE REVERSIÓN - " + codigoRecibo,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );

                    if (opcionMotivo != JOptionPane.OK_OPTION) {
                        return;
                    }

                    String motivoReversion = txtMotivo.getText().trim();
                    if (motivoReversion.isEmpty()) {
                        JOptionPane.showMessageDialog(viewMain,
                            "Debe ingresar un motivo para la reversión del pago.",
                            "MOTIVO REQUERIDO", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Segunda confirmación de seguridad
                    int confirmacionFinal = JOptionPane.showConfirmDialog(
                        viewMain,
                        "<html><body style='width: 300px;'>" +
                        "<h2 style='color:#C0392B;'>⚠ ÚLTIMA ADVERTENCIA</h2>" +
                        "<p>¿Realmente desea eliminar el pago <b>" + codigoRecibo + "</b>?</p>" +
                        "<p><b>Motivo:</b> " + motivoReversion + "</p>" +
                        "<p style='color:red;'><b>Esta acción NO se puede deshacer.</b></p>" +
                        "</body></html>",
                        "CONFIRMAR ELIMINACIÓN",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE
                    );

                    if (confirmacionFinal != JOptionPane.YES_OPTION) {
                        return;
                    }

                    // Obtener usuario actual (si está disponible)
                    String usuarioActual = "SISTEMA";
                    try {
                        if (usser != null && usser.getUsername() != null) {
                            usuarioActual = usser.getUsername();
                        }
                    } catch (Exception e) {
                        // Si no hay usuario activo, usar SISTEMA
                    }
                    final String usuarioFinal = usuarioActual;
                    final String motivoFinal = motivoReversion;

                    // Ejecutar reversión directamente (ya estamos en EDT)
                    // Usar SwingWorker para no bloquear la UI
                    new javax.swing.SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            return registroDao.revertirPago(registro.getId(), usuarioFinal, motivoFinal);
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean exito = get();
                                if (exito) {
                                    JOptionPane.showMessageDialog(viewMain,
                                        "<html><body>" +
                                        "<h3 style='color:#27AE60;'>✓ PAGO REVERTIDO EXITOSAMENTE</h3>" +
                                        "<p>El pago <b>" + codigoRecibo + "</b> ha sido eliminado.</p>" +
                                        "<p>Los saldos de las cuotas han sido actualizados.</p>" +
                                        "</body></html>",
                                        "OPERACIÓN EXITOSA", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(viewMain,
                                        "Ocurrió un error al revertir el pago.\nPor favor, verifique los datos e intente nuevamente.",
                                        "ERROR", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(viewMain,
                                    "Error al revertir el pago: " + ex.getMessage(),
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(viewMain,
                        "Error al buscar el registro: " + ex.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
    }

    /**
     * Método para revertir una carga masiva completa
     * Muestra un diálogo con los lotes disponibles y permite seleccionar uno para revertir
     */
    public void revertirCargaMasiva() {
        com.subcafae.finantialtracker.data.dao.RegistroDao registroDao =
            new com.subcafae.finantialtracker.data.dao.RegistroDao();

        // Obtener lotes activos
        java.util.List<String[]> lotes = registroDao.obtenerLotesActivos();

        if (lotes.isEmpty()) {
            JOptionPane.showMessageDialog(viewMain,
                "No hay lotes de carga masiva disponibles para revertir.",
                "SIN LOTES", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear tabla para mostrar lotes
        String[] columnas = {"ID", "Archivo", "Mes", "Año", "Registros", "Monto Total", "Fecha Carga", "Usuario"};
        Object[][] datos = new Object[lotes.size()][8];
        for (int i = 0; i < lotes.size(); i++) {
            datos[i] = lotes.get(i);
        }

        javax.swing.JTable tablaLotes = new javax.swing.JTable(datos, columnas);
        tablaLotes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablaLotes.setRowHeight(25);
        tablaLotes.getTableHeader().setReorderingAllowed(false);

        // Ajustar anchos de columnas
        tablaLotes.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tablaLotes.getColumnModel().getColumn(1).setPreferredWidth(120); // Archivo
        tablaLotes.getColumnModel().getColumn(2).setPreferredWidth(40);  // Mes
        tablaLotes.getColumnModel().getColumn(3).setPreferredWidth(50);  // Año
        tablaLotes.getColumnModel().getColumn(4).setPreferredWidth(70);  // Registros
        tablaLotes.getColumnModel().getColumn(5).setPreferredWidth(100); // Monto
        tablaLotes.getColumnModel().getColumn(6).setPreferredWidth(140); // Fecha
        tablaLotes.getColumnModel().getColumn(7).setPreferredWidth(80);  // Usuario

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(tablaLotes);
        scrollPane.setPreferredSize(new java.awt.Dimension(750, 300));

        javax.swing.JPanel panelPrincipal = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        panelPrincipal.add(new javax.swing.JLabel(
            "<html><b style='color:#C0392B;'>SELECCIONE UN LOTE PARA REVERTIR</b><br/>" +
            "<i style='color:gray;'>Esta acción eliminará todos los registros de pago del lote seleccionado.</i></html>"),
            java.awt.BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, java.awt.BorderLayout.CENTER);

        int opcion = JOptionPane.showConfirmDialog(
            viewMain,
            panelPrincipal,
            "REVERTIR CARGA MASIVA",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        int filaSeleccionada = tablaLotes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(viewMain,
                "Debe seleccionar un lote de la tabla.",
                "SELECCIÓN REQUERIDA", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loteId = Integer.parseInt(lotes.get(filaSeleccionada)[0]);
        String nombreArchivo = lotes.get(filaSeleccionada)[1];
        String mesLote = lotes.get(filaSeleccionada)[2];
        String anioLote = lotes.get(filaSeleccionada)[3];
        String cantidadReg = lotes.get(filaSeleccionada)[4];
        String montoTotal = lotes.get(filaSeleccionada)[5];

        // Mostrar detalles del lote
        String detallesLote = registroDao.obtenerDetalleLoteParaRevertir(loteId);

        int confirmacion = JOptionPane.showConfirmDialog(
            viewMain,
            detallesLote,
            "CONFIRMAR REVERSIÓN DEL LOTE " + loteId,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        // Solicitar motivo de la reversión
        javax.swing.JPanel panelMotivo = new javax.swing.JPanel();
        panelMotivo.setLayout(new java.awt.BorderLayout(5, 10));
        panelMotivo.setPreferredSize(new java.awt.Dimension(400, 150));

        javax.swing.JLabel lblMotivo = new javax.swing.JLabel(
            "<html><b style='color:#C0392B;'>MOTIVO DE LA REVERSIÓN</b><br/>" +
            "<i style='color:gray;'>Ingrese la razón por la cual se revierte este lote:</i></html>"
        );

        javax.swing.JTextArea txtMotivo = new javax.swing.JTextArea(4, 30);
        txtMotivo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        txtMotivo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(52, 73, 94), 2),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        javax.swing.JScrollPane scrollMotivo = new javax.swing.JScrollPane(txtMotivo);

        panelMotivo.add(lblMotivo, java.awt.BorderLayout.NORTH);
        panelMotivo.add(scrollMotivo, java.awt.BorderLayout.CENTER);

        int opcionMotivo = JOptionPane.showConfirmDialog(
            viewMain,
            panelMotivo,
            "MOTIVO DE REVERSIÓN - LOTE " + loteId,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcionMotivo != JOptionPane.OK_OPTION) {
            return;
        }

        String motivoReversion = txtMotivo.getText().trim();
        if (motivoReversion.isEmpty()) {
            JOptionPane.showMessageDialog(viewMain,
                "Debe ingresar un motivo para la reversión del lote.",
                "MOTIVO REQUERIDO", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmación final
        int confirmacionFinal = JOptionPane.showConfirmDialog(
            viewMain,
            "<html><body style='width: 350px;'>" +
            "<h2 style='color:#C0392B;'>⚠ ÚLTIMA ADVERTENCIA</h2>" +
            "<p>¿Realmente desea revertir el lote <b>#" + loteId + "</b>?</p>" +
            "<p><b>Archivo:</b> " + nombreArchivo + "</p>" +
            "<p><b>Período:</b> " + mesLote + "/" + anioLote + "</p>" +
            "<p><b>Registros:</b> " + cantidadReg + "</p>" +
            "<p><b>Monto Total:</b> S/ " + montoTotal + "</p>" +
            "<p><b>Motivo:</b> " + motivoReversion + "</p>" +
            "<hr/>" +
            "<p style='color:red;'><b>Esta acción NO se puede deshacer.</b></p>" +
            "</body></html>",
            "CONFIRMAR ELIMINACIÓN DE LOTE",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );

        if (confirmacionFinal != JOptionPane.YES_OPTION) {
            return;
        }

        // Ejecutar reversión
        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        String usuarioActual = (usser != null && usser.getUsername() != null) ? usser.getUsername() : "SISTEMA";
        final String usuarioFinal = usuarioActual;
        final String motivoFinal = motivoReversion;
        final int loteIdFinal = loteId;

        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return registroDao.revertirLoteCarga(loteIdFinal, usuarioFinal, motivoFinal);
            }

            @Override
            protected void done() {
                viewMain.loading.dispose();
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(viewMain,
                            "<html><body>" +
                            "<h3 style='color:#27AE60;'>✓ LOTE REVERTIDO EXITOSAMENTE</h3>" +
                            "<p>El lote <b>#" + loteIdFinal + "</b> ha sido eliminado.</p>" +
                            "<p>Todos los registros y saldos han sido actualizados.</p>" +
                            "</body></html>",
                            "OPERACIÓN EXITOSA", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(viewMain,
                            "Ocurrió un error al revertir el lote.\nPor favor, verifique los datos e intente nuevamente.",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(viewMain,
                        "Error al revertir el lote: " + ex.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();

        viewMain.loading.setVisible(true);
    }

    /**
     * Método para detectar y corregir pagos duplicados
     * Busca préstamos y abonos marcados como "Pagado" que tienen pagos adicionales indebidos
     */
    public void corregirPagosDuplicados() {
        // Mostrar diálogo inicial
        int opcionInicial = JOptionPane.showConfirmDialog(
            viewMain,
            "<html><body style='width: 400px;'>" +
            "<h3 style='color:#D35400;'>CORRECCIÓN DE PAGOS DUPLICADOS</h3>" +
            "<p>Esta herramienta detecta y corrige pagos duplicados en:</p>" +
            "<ul>" +
            "<li><b>Préstamos:</b> Cuotas con pagos superiores al monto mensual</li>" +
            "<li><b>Abonos:</b> Cuotas con pagos superiores al monto mensual</li>" +
            "</ul>" +
            "<p style='color:#C0392B;'><b>⚠ Se recomienda hacer backup antes de continuar.</b></p>" +
            "<p>¿Desea buscar pagos duplicados?</p>" +
            "</body></html>",
            "CORREGIR PAGOS DUPLICADOS",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (opcionInicial != JOptionPane.YES_OPTION) {
            return;
        }

        viewMain.loading.setModal(true);
        viewMain.loading.setLocationRelativeTo(viewMain);

        new Thread(() -> {
            try {
                com.subcafae.finantialtracker.data.dao.RegistroDao registroDao =
                    new com.subcafae.finantialtracker.data.dao.RegistroDao();

                // Buscar duplicados
                java.util.List<Object[]> duplicadosPrestamos = registroDao.buscarPagosDuplicadosPrestamos();
                java.util.List<Object[]> duplicadosAbonos = registroDao.buscarPagosDuplicadosAbonos();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();

                    if (duplicadosPrestamos.isEmpty() && duplicadosAbonos.isEmpty()) {
                        JOptionPane.showMessageDialog(viewMain,
                            "<html><body>" +
                            "<h3 style='color:#27AE60;'>✓ NO SE ENCONTRARON DUPLICADOS</h3>" +
                            "<p>No hay pagos duplicados en el sistema.</p>" +
                            "</body></html>",
                            "RESULTADO", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    // Mostrar resumen de duplicados encontrados
                    StringBuilder resumen = new StringBuilder();
                    resumen.append("<html><body style='width: 600px;'>");
                    resumen.append("<h3 style='color:#D35400;'>DUPLICADOS ENCONTRADOS</h3>");

                    if (!duplicadosPrestamos.isEmpty()) {
                        resumen.append("<h4 style='color:#2980B9;'>PRÉSTAMOS (" + duplicadosPrestamos.size() + " cuotas):</h4>");
                        resumen.append("<table border='1' cellpadding='3'>");
                        resumen.append("<tr><th>Solicitud</th><th>Cuota</th><th>Pagado</th><th>Mensual</th><th>Tipo</th><th>Registros</th></tr>");

                        int maxShow = Math.min(duplicadosPrestamos.size(), 15);
                        for (int i = 0; i < maxShow; i++) {
                            Object[] dup = duplicadosPrestamos.get(i);
                            String tipoDup = dup.length > 5 ? String.valueOf(dup[5]) : "EXCESO";
                            int cantReg = dup.length > 6 ? ((Number)dup[6]).intValue() : 0;
                            resumen.append("<tr>");
                            resumen.append("<td>").append(dup[0]).append("</td>"); // SoliNum
                            resumen.append("<td>").append(dup[1]).append("</td>"); // Cuota
                            resumen.append("<td>S/ ").append(String.format("%.2f", dup[2])).append("</td>"); // Payment
                            resumen.append("<td>S/ ").append(String.format("%.2f", dup[3])).append("</td>"); // MonthlyFeeValue
                            if ("REGISTRO_MULTIPLE".equals(tipoDup)) {
                                resumen.append("<td style='color:orange;'>").append(cantReg).append(" pagos</td>");
                                resumen.append("<td style='color:red;'>").append(cantReg).append("</td>");
                            } else {
                                double exceso = ((Number)dup[2]).doubleValue() - ((Number)dup[3]).doubleValue();
                                resumen.append("<td style='color:blue;'>Exceso</td>");
                                resumen.append("<td style='color:red;'>S/ ").append(String.format("%.2f", exceso)).append("</td>");
                            }
                            resumen.append("</tr>");
                        }
                        if (duplicadosPrestamos.size() > 15) {
                            resumen.append("<tr><td colspan='6'>... y ").append(duplicadosPrestamos.size() - 15).append(" más</td></tr>");
                        }
                        resumen.append("</table><br/>");
                    }

                    if (!duplicadosAbonos.isEmpty()) {
                        resumen.append("<h4 style='color:#8E44AD;'>ABONOS (" + duplicadosAbonos.size() + " cuotas):</h4>");
                        resumen.append("<table border='1' cellpadding='3'>");
                        resumen.append("<tr><th>Solicitud</th><th>Cuota</th><th>Pagado</th><th>Mensual</th><th>Tipo</th><th>Registros</th></tr>");

                        int maxShow = Math.min(duplicadosAbonos.size(), 15);
                        for (int i = 0; i < maxShow; i++) {
                            Object[] dup = duplicadosAbonos.get(i);
                            String tipoDup = dup.length > 5 ? String.valueOf(dup[5]) : "EXCESO";
                            int cantReg = dup.length > 6 ? ((Number)dup[6]).intValue() : 0;
                            resumen.append("<tr>");
                            resumen.append("<td>").append(dup[0]).append("</td>"); // SoliNum
                            resumen.append("<td>").append(dup[1]).append("</td>"); // Cuota
                            resumen.append("<td>S/ ").append(String.format("%.2f", dup[2])).append("</td>"); // Payment
                            resumen.append("<td>S/ ").append(String.format("%.2f", dup[3])).append("</td>"); // Monthly
                            if ("REGISTRO_MULTIPLE".equals(tipoDup)) {
                                resumen.append("<td style='color:orange;'>").append(cantReg).append(" pagos</td>");
                                resumen.append("<td style='color:red;'>").append(cantReg).append("</td>");
                            } else {
                                double exceso = ((Number)dup[2]).doubleValue() - ((Number)dup[3]).doubleValue();
                                resumen.append("<td style='color:blue;'>Exceso</td>");
                                resumen.append("<td style='color:red;'>S/ ").append(String.format("%.2f", exceso)).append("</td>");
                            }
                            resumen.append("</tr>");
                        }
                        if (duplicadosAbonos.size() > 15) {
                            resumen.append("<tr><td colspan='6'>... y ").append(duplicadosAbonos.size() - 15).append(" más</td></tr>");
                        }
                        resumen.append("</table>");
                    }

                    resumen.append("<br/><p style='color:#C0392B;'><b>¿Desea corregir estos pagos duplicados?</b></p>");
                    resumen.append("<p><i>Los pagos con EXCESO serán ajustados al valor mensual.</i></p>");
                    resumen.append("<p><i>Los pagos con REGISTROS MÚLTIPLES serán eliminados (se mantiene 1).</i></p>");
                    resumen.append("</body></html>");

                    int confirmar = JOptionPane.showConfirmDialog(
                        viewMain,
                        resumen.toString(),
                        "CONFIRMAR CORRECCIÓN",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );

                    if (confirmar != JOptionPane.YES_OPTION) {
                        return;
                    }

                    // Solicitar motivo
                    String motivo = JOptionPane.showInputDialog(
                        viewMain,
                        "Ingrese el motivo de la corrección:",
                        "MOTIVO DE CORRECCIÓN",
                        JOptionPane.QUESTION_MESSAGE
                    );

                    if (motivo == null || motivo.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(viewMain,
                            "Debe ingresar un motivo para la corrección.",
                            "MOTIVO REQUERIDO", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Ejecutar corrección
                    viewMain.loading.setModal(true);
                    viewMain.loading.setLocationRelativeTo(viewMain);

                    String usuarioActual = (usser != null && usser.getUsername() != null) ? usser.getUsername() : "SISTEMA";
                    final String usuarioFinal = usuarioActual;
                    final String motivoFinal = motivo.trim();

                    new javax.swing.SwingWorker<int[], Void>() {
                        @Override
                        protected int[] doInBackground() throws Exception {
                            int prestamosCorregidos = registroDao.corregirPagosDuplicadosPrestamos(usuarioFinal, motivoFinal);
                            int abonosCorregidos = registroDao.corregirPagosDuplicadosAbonos(usuarioFinal, motivoFinal);
                            return new int[]{prestamosCorregidos, abonosCorregidos};
                        }

                        @Override
                        protected void done() {
                            viewMain.loading.dispose();
                            try {
                                int[] resultados = get();
                                JOptionPane.showMessageDialog(viewMain,
                                    "<html><body>" +
                                    "<h3 style='color:#27AE60;'>✓ CORRECCIÓN COMPLETADA</h3>" +
                                    "<p><b>Cuotas de préstamos corregidas:</b> " + resultados[0] + "</p>" +
                                    "<p><b>Cuotas de abonos corregidas:</b> " + resultados[1] + "</p>" +
                                    "<p><i>Los pagos excedentes han sido ajustados.</i></p>" +
                                    "</body></html>",
                                    "OPERACIÓN EXITOSA", JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(viewMain,
                                    "Error al corregir duplicados: " + ex.getMessage(),
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();

                    viewMain.loading.setVisible(true);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    viewMain.loading.dispose();
                    JOptionPane.showMessageDialog(viewMain,
                        "Error al buscar duplicados: " + ex.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        viewMain.loading.setVisible(true);
    }
}