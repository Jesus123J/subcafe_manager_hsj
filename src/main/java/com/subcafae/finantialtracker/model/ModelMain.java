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
import com.subcafae.finantialtracker.data.entity.UserTb;
import com.subcafae.finantialtracker.report.HistoryPayment.HistoryPayment;
import com.subcafae.finantialtracker.report.descuento.DatosPersona;
import com.subcafae.finantialtracker.report.descuento.ExcelExporter;
import com.subcafae.finantialtracker.report.deuda.ReporteDeuda;
import com.subcafae.finantialtracker.view.ViewMain;
import com.subcafae.finantialtracker.view.component.ComponentManageBond;
import com.subcafae.finantialtracker.view.component.ComponentManageLoan;
import com.subcafae.finantialtracker.view.component.ComponentManageUser;
import com.subcafae.finantialtracker.view.component.ComponentManageWorker;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelMain {

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
    }

    private void init() {
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

        new Thread(new Runnable() {
            @Override
            public void run() {
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

                List<EmployeeTb> listFilter;
                try {
                    listFilter = empleadoDao.findAll().stream().filter(predicate -> predicate.getEmploymentStatus().equalsIgnoreCase(nombresBuscar)).collect(Collectors.toList());
                    System.out.println("Empleado -> " + listFilter.toString());
                    String[] employee = new String[listFilter.size()];

                    for (int i = 0; i < listFilter.size(); i++) {
                        employee[i] = listFilter.get(i).getNationalId() + " - " + listFilter.get(i).getFirstName() + listFilter.get(i).getLastName();
                    }
                    if (employee.length == 0) {
                        JOptionPane.showMessageDialog(null, "No hay Trabajador tipo ".concat(nombresBuscar));
                        return;
                    }
                    String nombre = (String) JOptionPane.showInputDialog(
                            null,
                            "Selecciona el tipo de trabajador:",
                            "Listado de Trabajador",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            employee,
                            employee[0]
                    );

                    if (nombresBuscar == null) {
                        JOptionPane.showMessageDialog(null, "No se seleccionó ningún trabajador.");
                        return;
                    }

                    String dniSeleccionado = nombre.split(" - ")[0].trim(); // Extraer el DNI del empleado

                    HistoryPayment historyPayment = new HistoryPayment();
                    historyPayment.HistoryPatment(dniSeleccionado);

                } catch (SQLException ex) {
                    Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }).start();

    }

    public void generateExcel() {

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

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Formato que coincide con el texto

        List<EmployeeTb> employees = null;
        List<AbonoDetailsTb> abonoDetailses = null;
        List<LoanTb> loan = null;
        List<LoanDetailsTb> loanDetails = null;
        List<AbonoTb> bonos = null;
        try {
            bonos = new AbonoDao().findAllAbonos();
            employees = new EmployeeDao().findAll().stream().filter(predicate -> predicate.getEmploymentStatus().equals(contractType)).collect(Collectors.toList());
            abonoDetailses = new AbonoDetailsDao().getAllAbonoDetails().stream().filter(predicate -> predicate.getState().equalsIgnoreCase("Pendiente")).collect(Collectors.toList());
            loan = new LoanDao().getAllLoans();

            if (employees.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No hay Trabajador tipo ".concat(contractType));
                return;
            }
            loanDetails = new LoanDetailsDao().getAllLoanDetails();

            System.out.println("List empledo -> " + employees);
            System.out.println("List abono -> " + bonos);
            System.out.println("");

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

                    if (employee1.getEmployeeId() == Integer.parseInt(loan1.getEmployeeId())) {

                        for (LoanDetailsTb loanDetail1 : loanDetails) {

                            if (loan1.getId() == loanDetail1.getLoanId()) {

                                LocalDate fechaVencimiento = loanDetail1.getPaymentDate().toLocalDate();
                                System.out.println("Fecha de vencimeinto -> " + fechaVencimiento);

                                if (LocalDate.now().isAfter(fechaVencimiento)) {
                                    System.out.println("Se guarda");
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
                                    System.out.println("Se guarda");
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

            Object[][] data = new Object[datosLista.size()][3];

            System.out.println("Imprirmir data");

            for (int i = 0; i < datosLista.size(); i++) {
                System.out.println("Dni -> " + datosLista.get(i).getDni());
                System.out.println("Nombre -> " + datosLista.get(i).getNombre());
                System.out.println("Monto -> " + datosLista.get(i).getMonto());

                data[i][0] = datosLista.get(i).getDni();
                data[i][1] = datosLista.get(i).getNombre();
                data[i][2] = datosLista.get(i).getMonto();
            }

            List<Object[]> reportData = new ArrayList<>();
            DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMYY");
            String monthYear = LocalDate.now().format(monthYearFormatter);

            for (int i = 0; i < data.length; i++) {
                //2028012543718205 ABRAMONTE HOLGUIN PAULA MARIA                2018/11/04 50 84    50.00     0.00  4200.00000000
//            System.out.println("Data -> " + data[i][0]);
//            System.out.println("Data -> " + data[i][1]);
//            System.out.println("Data -> " + data[i][2]);
                Object[] row = new Object[10];

                row[0] = String.valueOf(data[i][0]).split("-")[1] + monthYear;
                row[1] = String.valueOf(data[i][0]).split("-")[0];
                row[2] = data[i][1];
                row[3] = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date());
                row[4] = 50;
                row[5] = 84;
                row[6] = String.format("%.2f", Double.parseDouble(String.valueOf(data[i][2])));
                row[7] = "0.00";
                row[8] = row[7] = String.format("%.2f", Double.parseDouble(String.valueOf(data[i][2])) * 84);
                row[9] = "000000";

                reportData.add(row);

            }

            Object[][] data2 = reportData.toArray(new Object[0][]);

            ExcelExporter exporter = new ExcelExporter();
            exporter.generateExcel(data2);
        } catch (SQLException ex) {
            System.out.println("Error -> " + ex.getMessage());
            //  Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void reportDeuda() {
        List<LoanTb> loans;

        try {
            ReporteDeuda reporteDeuda = new ReporteDeuda();

            List<EmployeeTb> listEmployee = new EmployeeDao().findAll();

            loans = new LoanDao().getAllLoans();

            Set<String> empleadosConPrestamo = loans.stream()
                    .map(LoanTb::getEmployeeId)
                    .collect(Collectors.toSet());

            List<AbonoTb> abonos = new AbonoDao().findAllAbonos();

            Set<String> empleadosConAbono = abonos.stream()
                    .map(AbonoTb::getEmployeeId)
                    .collect(Collectors.toSet());

            Set<String> empleadosUnicos = new HashSet<>();

            empleadosUnicos.addAll(empleadosConPrestamo);
            empleadosUnicos.addAll(empleadosConAbono);

            System.out.println("Map -> " + empleadosUnicos.toString());

            String[] nombres = empleadosUnicos.stream()
                    .map(dni -> {
                        System.out.println("DNI -> " + dni);
                        EmployeeTb employee = listEmployee.stream().filter(predicate
                                -> predicate.getEmployeeId() == Integer.parseInt(dni)
                                || predicate.getNationalId().equalsIgnoreCase(dni)
                        ).findFirst().get();
                        return employee != null ? employee.getFirstName().concat(" " + employee.getLastName()) : "";
                    })
                    .filter(nombre -> !nombre.isEmpty())
                    .distinct()
                    .toArray(String[]::new);

            if (nombres.length == 0) {
                JOptionPane.showMessageDialog(null, "No hay empleados con préstamos o abonos.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String nombresBuscar = (String) JOptionPane.showInputDialog(
                    null,
                    "Selecciona el tipo de trabajador:",
                    "Tipo de Trabajador",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    nombres,
                    nombres[0]
            );

            if (nombresBuscar == null) {
                JOptionPane.showMessageDialog(null, "No seleccionaste ningún empleado.", "Información", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JOptionPane optionPane = new JOptionPane("Descargando...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
            JDialog dialog = optionPane.createDialog(null, "Información");
            dialog.setModal(false);
            dialog.setVisible(true);

            new Thread(() -> {
                // Employee employeeR = employeeDAO.search("Name", nombresBuscar);
                EmployeeTb employeeR = listEmployee.stream().filter(predicate -> predicate.getFirstName().concat(" " + predicate.getLastName()).equalsIgnoreCase(nombresBuscar)).findFirst().get();
                //ABONOS

                List<AbonoTb> conceptos_ob = abonos.stream().filter(predicate -> predicate.getEmployeeId().equalsIgnoreCase(String.valueOf(employeeR.getEmployeeId()))).collect(Collectors.toList());

                String[] conceptos_id = new String[conceptos_ob.size()];
                String[] conceptos = null;
                System.out.println("conceptos_ob.size " + conceptos_ob.size());

                if (conceptos_ob.size() != 0) {

                    for (int i = 0; i < conceptos_ob.size(); i++) {
                        conceptos_id[i] = conceptos_ob.get(i).getServiceConceptId();
                    }

                    conceptos_id = new HashSet<>(Arrays.asList(conceptos_id)).toArray(new String[0]);
                    conceptos_id = Arrays.stream(conceptos_id)
                            .sorted(Comparator.comparingInt(Integer::parseInt))
                            .toArray(String[]::new);

                    System.out.println(Arrays.toString(conceptos_id));

                    try {
                        conceptos = new ServiceConceptDao().searchByIds(nombres, "description");

                    } catch (SQLException e) {

                    }

                    System.out.println(Arrays.toString(conceptos));

                } else {
                    conceptos = new String[1];
                    conceptos[0] = "Vacio";

                }
                // PRESTAMOS

                System.out.println("List -> " + employeeR.toString());
                System.out.println("List loans -> " + loans.toString());

                try {

                    LoanTb loanR = loans.stream().filter(predicate -> predicate.getEmployeeId().equalsIgnoreCase(employeeR.getNationalId())).findFirst().get();

                    System.out.println("Loan -> " + loanR);

                    String soliNum = "Vacio";  // Valor por defecto si no hay préstamo

                    double loanAmount = 0;    // Valor por defecto si no hay préstamo
                    int loanDues = 0;         // Valor por defecto si no hay préstamo
                    String[][] prestamoData = new String[1][3];  // Default size to prevent IndexOutOfBounds
                    String[][] fondoData = new String[1][3];     // Default size to prevent IndexOutOfBounds
                    List<LoanDetailsTb> loanDetails = null;

                    if (loanR != null) {
                        soliNum = loanR.getSoliNum();
                        loanAmount = loanR.getAmountWithdrawn();
                        loanDues = loanR.getDues();

                        LoanTb find = loans.stream().filter(predicate -> predicate.getSoliNum().equalsIgnoreCase(loanR.getSoliNum())).findFirst().get();

                        try {
                            loanDetails = new LoanDetailsDao().getAllLoanDetails().stream().filter(predicate -> predicate.getLoanId() == find.getId()).collect(Collectors.toList());
                        } catch (SQLException ex) {
                            //Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        prestamoData = new String[loanDetails.size()][3]; // Adjust array size based on dues
                        fondoData = new String[loanDetails.size()][3];    // Adjust array size based on dues

                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                        for (int i = 0; i < loanDetails.size(); i++) {

                            LoanDetailsTb detail = loanDetails.get(i);

                            System.out.println("Indice -> " + i);
                            // detail.getPaymentDate().format(dateFormatter)
                            String paymentDateString = (detail.getPaymentDate() != null) ? detail.getPaymentDate().toString() : "Fecha no disponible";

                            String cuotaLabel = String.format("%03d/%03d", detail.getDues(), loanR.getDues());

                            // getMonthlyIntableFundFee
                            Double MonthlyFee = detail.getMonthlyFeeValue() - detail.getMonthlyIntangibleFundFee();

                            //Porque no se pone el total
                            // ????????
                            prestamoData[i][0] = cuotaLabel;
                            prestamoData[i][1] = paymentDateString;
                            System.out.println("Patment -> " + prestamoData[i][1]);

                            prestamoData[i][2] = String.valueOf(MonthlyFee);// String.valueOf(MonthlyFee);

                            fondoData[i][0] = cuotaLabel;
                            fondoData[i][1] = paymentDateString;
                            //MonthlyFeeValue
                            fondoData[i][2] = String.valueOf(detail.getMonthlyIntangibleFundFee());
                        }
                    } else {
                        // Si no tiene préstamo, se llenan los datos con 0 o vacío
                        prestamoData[0][0] = "0/0";
                        prestamoData[0][1] = "0";
                        prestamoData[0][2] = "0";

                        fondoData[0][0] = "0/0";
                        fondoData[0][1] = "0";
                        fondoData[0][2] = "0";
                    }
                    if (loanDetails == null) {
                        try {
                            reporteDeuda.reporteDeuda(
                                    "000000",
                                    employeeR.getFirstName().concat(" " + employeeR.getLastName()),
                                    employeeR.getNationalId().toLowerCase(),
                                    conceptos,
                                    conceptos_id,
                                    soliNum, // SoliNum
                                    loanAmount, // Devolver 0 si no tiene préstamo
                                    loanDues, // Devolver 0 si no tiene préstamo
                                    (prestamoData.length > 0 ? prestamoData[0][1] : "0"), // Fecha de último pago
                                    prestamoData,
                                    fondoData
                            );
                        } catch (SQLException ex) {
                            Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        try {
                            reporteDeuda.reporteDeuda(
                                    "00000",
                                    employeeR.getFirstName().concat(" " + employeeR.getLastName()),
                                    employeeR.getNationalId().toLowerCase(),
                                    conceptos,
                                    conceptos_id,
                                    soliNum, // SoliNum
                                    loanAmount, // Devolver 0 si no tiene préstamo
                                    loanDues, // Devolver 0 si no tiene préstamo
                                    (prestamoData.length > 0 ? prestamoData[loanDetails.size() - 1][1] : "0"), // Fecha de último pago
                                    prestamoData,
                                    fondoData
                            );
                        } catch (SQLException ex) {
                            Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (Exception e) {

                }

                dialog.dispose();
            }).start();

        } catch (SQLException ex) {
            //Logger.getLogger(ModelMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
