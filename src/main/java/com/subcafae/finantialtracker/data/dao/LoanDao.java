/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.Loan;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.xmlbeans.impl.store.Locale;

/**
 * Prestamos (loan). Fuente primaria: el backend REST
 * (/integracion/ft/prestamos). Fallback: el JDBC directo original (metodos
 * xxxDirecto) cuando el backend no esta corriendo.
 *
 * @author Jesus Gutierrez
 */
public class LoanDao extends LoanDetailsDao {

    private final Connection connection;
    public String soliNum;

    private static final DateTimeFormatter FT_FECHA_HORA_API =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoanDao() {
        this.connection = Conexion.getConnection();
    }

    // Método para obtener todos los números de solicitud para autocompletado
    public List<String> getAllSoliNums() {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/soli-nums");
            List<String> soliNums = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                soliNums.add(e.getAsString());
            }
            return soliNums;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllSoliNumsDirecto();
    }

    private List<String> getAllSoliNumsDirecto() {
        List<String> soliNums = new ArrayList<>();
        String sql = "SELECT SoliNum FROM loan ORDER BY SoliNum DESC";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String num = rs.getString("SoliNum");
                if (num != null && !num.isEmpty()) {
                    soliNums.add(num);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener números de solicitud: " + e.getMessage());
        }

        return soliNums;
    }

    // Método para buscar préstamos por EmployeeID excluyendo aquellos con PaymentResponsibility = 'GUARANTOR'

    public boolean updatePaymentResponsibility(String soliNum) {
        try {
            JsonObject resp = ApiBackend.put("/integracion/ft/prestamos/" + soliNum + "/responsabilidad-pago", null);
            JsonObject data = resp.getAsJsonObject("data");
            String resultado = data.get("resultado").getAsString();

            if ("SIN_AVAL".equals(resultado)) {
                JOptionPane.showMessageDialog(null, "No tiene un aval, no se puede cambiar el estado.", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if ("NO_CUMPLE".equals(resultado)) {
                JOptionPane.showMessageDialog(null, "El préstamo no cumple las condiciones para cambiar la responsabilidad de pago.", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return data.get("actualizado").getAsBoolean();
        } catch (ApiBackend.NoEncontradoException e) {
            return false; // el backend confirmo que el SoliNum no existe (igual que rs vacio)
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updatePaymentResponsibilityDirecto(soliNum);
    }

    private boolean updatePaymentResponsibilityDirecto(String soliNum) {
        String sqlSelect = "SELECT GuarantorId, State, StateLoan FROM loan WHERE SoliNum = ?";
        String sqlUpdate = "UPDATE loan SET PaymentResponsibility = 'GUARANTOR' WHERE SoliNum = ?";

        try (PreparedStatement selectStmt = connection.prepareStatement(sqlSelect)) {
            selectStmt.setString(1, soliNum);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                String guarantorId = rs.getString("GuarantorId");
                String state = rs.getString("State");
                String stateLoan = rs.getString("StateLoan");

                // Validaciones antes de cambiar el estado
                if (guarantorId == null || guarantorId.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No tiene un aval, no se puede cambiar el estado.", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                if (!"Aceptado".equals(state) || !"Pendiente".equals(stateLoan)) {
                    JOptionPane.showMessageDialog(null, "El préstamo no cumple las condiciones para cambiar la responsabilidad de pago.", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                // Si cumple las condiciones, se actualiza el estado
                try (PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate)) {
                    updateStmt.setString(1, soliNum);
                    int rowsUpdated = updateStmt.executeUpdate();
                    return rowsUpdated > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<LoanTb> findLoansByEmployeeId(String employeeId) throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/por-empleado?employeeId=" + employeeId);
            List<LoanTb> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                // rs.getInt("RefinanceParentID"): NULL -> 0, igual que el original
                loans.add(loanTbDesdeJson(e.getAsJsonObject(), false));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findLoansByEmployeeIdDirecto(employeeId);
    }

    private List<LoanTb> findLoansByEmployeeIdDirecto(String employeeId) throws SQLException {

        String sql = "SELECT * FROM loan WHERE State = 'Aceptado' AND StateLoan = 'Pendiente' "
                + "AND ( (PaymentResponsibility = 'EMPLOYEE' AND EmployeeID = ?) "
                + "OR (PaymentResponsibility = 'GUARANTOR' AND GuarantorId = ?) )";

        List<LoanTb> loans = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId); // Busca préstamos donde el empleado es el solicitante
            stmt.setString(2, employeeId); // Busca préstamos donde el empleado es el garante

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear el resultado al objeto LoanTb
                    LoanTb loan = new LoanTb();
                    loan.setId(rs.getInt("ID"));
                    loan.setSoliNum(rs.getString("SoliNum"));
                    loan.setEmployeeId(rs.getString("EmployeeID"));
                    loan.setGuarantorIds(rs.getString("GuarantorId"));
                    loan.setRequestedAmount(rs.getDouble("RequestedAmount"));
                    loan.setAmountWithdrawn(rs.getDouble("AmountWithdrawn"));
                    loan.setDues(rs.getInt("Dues"));
                    loan.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                    loan.setState(rs.getString("State"));
                    loan.setStateLoan(rs.getString("StateLoan"));
                    loan.setRefinanceParentId(rs.getInt("RefinanceParentID"));
                    loan.setCreatedBy(rs.getInt("CreatedBy"));
                    loan.setCreatedAt(rs.getDate("CreatedAt") != null ? rs.getDate("CreatedAt") : null);
                    loan.setModifiedAt(rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null);
                    loan.setModifiedBy(rs.getInt("ModifiedBy"));
                    loan.setType(rs.getString("Type"));
                    loan.setPaymentResponsibility(rs.getString("PaymentResponsibility"));

                    // Añadir el préstamo a la lista
                    loans.add(loan);
                }
            }
        }

        return loans;
    }

    public List<LoanTb> getAllLoans() throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos");
            List<LoanTb> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                // RefinanceParentID == 0 -> null, igual que el original
                loans.add(loanTbDesdeJson(e.getAsJsonObject(), true));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllLoansDirecto();
    }

    private List<LoanTb> getAllLoansDirecto() throws SQLException {

        String sql = "SELECT * FROM loan";
        List<LoanTb> loans = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoanTb loan = new LoanTb();

                loan.setId(rs.getInt("ID"));
                loan.setSoliNum(rs.getString("SoliNum"));
                loan.setEmployeeId(rs.getString("EmployeeID"));
                loan.setGuarantorIds(rs.getString("GuarantorId"));
                loan.setRequestedAmount(rs.getDouble("RequestedAmount"));
                loan.setAmountWithdrawn(rs.getDouble("AmountWithdrawn"));
                loan.setDues(rs.getInt("Dues"));
                loan.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                loan.setState(rs.getString("State"));

                loan.setStateLoan(rs.getString("StateLoan"));
                loan.setRefinanceParentId(rs.getInt("RefinanceParentID") == 0 ? null : rs.getInt("RefinanceParentID"));
                loan.setCreatedBy(rs.getInt("CreatedBy"));
                // Convertir CreatedAt y ModifiedAt a LocalDate
                loan.setCreatedAt(rs.getDate("CreatedAt"));
                loan.setModifiedAt(rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null);

                loan.setModifiedBy(rs.getInt("ModifiedBy"));
                loan.setType(rs.getString("Type"));
                loan.setPaymentResponsibility(rs.getString("PaymentResponsibility"));

                loans.add(loan);
            }
        }

        return loans;
    }

    public List<LoanTb> getLoansByDateRange(Date fechaInicio, Date fechaFin) {
        System.out.println("Fecha inicial -> " + fechaInicio);
        System.out.println("Fecha fin -> " + fechaFin);
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/por-rango?inicio="
                    + fechaInicio + "&fin=" + fechaFin);
            List<LoanTb> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                loans.add(loanTbDesdeJson(e.getAsJsonObject(), true));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getLoansByDateRangeDirecto(fechaInicio, fechaFin);
    }

    private List<LoanTb> getLoansByDateRangeDirecto(Date fechaInicio, Date fechaFin) {
        List<LoanTb> loans = new ArrayList<>();
        String sql = "SELECT * FROM loan WHERE CreatedAt BETWEEN ? AND ? ORDER BY CreatedAt ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                LoanTb loan = new LoanTb();

                loan.setId(rs.getInt("ID"));
                loan.setSoliNum(rs.getString("SoliNum"));
                loan.setEmployeeId(rs.getString("EmployeeID"));
                loan.setGuarantorIds(rs.getString("GuarantorId"));
                loan.setRequestedAmount(rs.getDouble("RequestedAmount"));
                loan.setAmountWithdrawn(rs.getDouble("AmountWithdrawn"));
                loan.setDues(rs.getInt("Dues"));
                loan.setPaymentDate(rs.getDate("PaymentDate").toLocalDate());
                loan.setState(rs.getString("State"));

                loan.setStateLoan(rs.getString("StateLoan"));
                loan.setRefinanceParentId(rs.getInt("RefinanceParentID") == 0 ? null : rs.getInt("RefinanceParentID"));
                loan.setCreatedBy(rs.getInt("CreatedBy"));
                // Convertir CreatedAt y ModifiedAt a LocalDate
                loan.setCreatedAt(rs.getDate("CreatedAt"));

                loan.setModifiedAt(rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null);

                loan.setModifiedBy(rs.getInt("ModifiedBy"));
                loan.setType(rs.getString("Type"));
                loan.setPaymentResponsibility(rs.getString("PaymentResponsibility"));

                loans.add(loan);
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "❌ ERROR: No se pudo recuperar la lista de préstamos.");
        }
        return loans;
    }

    public List<Loan> searchLoan(String numberSoli) {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/resumen/por-soli?soliNum=" + numberSoli);
            List<Loan> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                loans.add(loanResumenDesdeJson(e.getAsJsonObject()));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return searchLoanDirecto(numberSoli);
    }

    private List<Loan> searchLoanDirecto(String numberSoli) {

        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT \n"
                + "    l.ID, l.ModifiedAt, l.SoliNum, \n"
                + "    e1.fullName AS SolicitorName, \n"
                + "    e2.fullName AS GuarantorName, \n"
                + "    (SELECT SUM(MonthlyFeeValue - IFNULL(payment, 0)) \n"
                + "     FROM loandetail \n"
                + "     WHERE LoanID = l.RefinanceParentID AND State IN ('pendiente', 'parcial') \n"
                + "     GROUP BY LoanID) AS Refinanciado,\n"
                + "    l.RequestedAmount, l.AmountWithdrawn,\n"
                + "    l.Dues,\n"
                + "    dd.TotalInterest,\n"
                + "    dd.TotalIntangibleFund,\n"
                + "    dd.MonthlyCapitalInstallment,\n"
                + "    dd.MonthlyInterestFee,\n"
                + "    dd.MonthlyIntangibleFundFee,\n"
                + "    dd.MonthlyFeeValue,\n"
                + "    l.State, l.PaymentResponsibility \n"
                + "FROM loan l \n"
                + "LEFT JOIN employees e1 ON l.EmployeeID = e1.national_id \n"
                + "LEFT JOIN employees e2 ON l.GuarantorId = e2.national_id\n"
                + "LEFT JOIN (\n"
                + "    SELECT ld.* \n"
                + "    FROM loandetail ld \n"
                + "    WHERE ld.ID = (SELECT MIN(ID) FROM loandetail WHERE LoanID = ld.LoanID) \n"
                + ") dd ON dd.LoanID = l.ID \n"
                + "WHERE l.SoliNum = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, numberSoli);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                loans.add(new Loan(
                        rs.getInt("ID"),
                        rs.getDate("ModifiedAt") == null ? "" : rs.getDate("ModifiedAt").toString(),
                        rs.getString("SoliNum"),
                        rs.getString("SolicitorName"),
                        rs.getString("GuarantorName"),
                        rs.getBigDecimal("RequestedAmount"),
                        rs.getBigDecimal("AmountWithdrawn"),
                        rs.getBigDecimal("Refinanciado"),
                        rs.getInt("Dues"),
                        rs.getBigDecimal("TotalInterest"),
                        rs.getBigDecimal("TotalIntangibleFund"),
                        rs.getBigDecimal("MonthlyCapitalInstallment"),
                        rs.getBigDecimal("MonthlyInterestFee"),
                        rs.getBigDecimal("MonthlyIntangibleFundFee"),
                        rs.getBigDecimal("MonthlyFeeValue"),
                        rs.getString("State"),
                        rs.getString("PaymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    // Metodo para obtener los ultimos N prestamos sin filtro de fecha
    public List<Loan> getLastLoans(int limit) {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/resumen/ultimos?limite=" + limit);
            List<Loan> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                loans.add(loanResumenDesdeJson(e.getAsJsonObject()));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getLastLoansDirecto(limit);
    }

    private List<Loan> getLastLoansDirecto(int limit) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT \n"
                + "    l.ID, l.ModifiedAt, l.SoliNum, \n"
                + "    e1.fullName AS SolicitorName, \n"
                + "    e2.fullName AS GuarantorName, \n"
                + "    (SELECT SUM(MonthlyFeeValue - IFNULL(payment, 0)) \n"
                + "     FROM loandetail \n"
                + "     WHERE LoanID = l.RefinanceParentID AND State IN ('pendiente', 'parcial') \n"
                + "     GROUP BY LoanID) AS Refinanciado,\n"
                + "    l.RequestedAmount, l.AmountWithdrawn,\n"
                + "    l.Dues,\n"
                + "    dd.TotalInterest,\n"
                + "    dd.TotalIntangibleFund,\n"
                + "    dd.MonthlyCapitalInstallment,\n"
                + "    dd.MonthlyInterestFee,\n"
                + "    dd.MonthlyIntangibleFundFee,\n"
                + "    dd.MonthlyFeeValue,\n"
                + "    l.State, l.PaymentResponsibility \n"
                + "FROM loan l \n"
                + "LEFT JOIN employees e1 ON l.EmployeeID = e1.national_id \n"
                + "LEFT JOIN employees e2 ON l.GuarantorId = e2.national_id\n"
                + "LEFT JOIN (\n"
                + "    SELECT ld.* \n"
                + "    FROM loandetail ld \n"
                + "    WHERE ld.ID = (SELECT MIN(ID) FROM loandetail WHERE LoanID = ld.LoanID) \n"
                + ") dd ON dd.LoanID = l.ID \n"
                + "ORDER BY l.ID DESC LIMIT ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                loans.add(new Loan(
                        rs.getInt("ID"),
                        rs.getDate("ModifiedAt") == null ? "" : rs.getDate("ModifiedAt").toString(),
                        rs.getString("SoliNum"),
                        rs.getString("SolicitorName"),
                        rs.getString("GuarantorName"),
                        rs.getBigDecimal("RequestedAmount"),
                        rs.getBigDecimal("AmountWithdrawn"),
                        rs.getBigDecimal("Refinanciado"),
                        rs.getInt("Dues"),
                        rs.getBigDecimal("TotalInterest"),
                        rs.getBigDecimal("TotalIntangibleFund"),
                        rs.getBigDecimal("MonthlyCapitalInstallment"),
                        rs.getBigDecimal("MonthlyInterestFee"),
                        rs.getBigDecimal("MonthlyIntangibleFundFee"),
                        rs.getBigDecimal("MonthlyFeeValue"),
                        rs.getString("State"),
                        rs.getString("PaymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error -< " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    public List<Loan> getAllLoanss(Date fechaInicio, Date fechaFin) {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/resumen/por-rango?inicio="
                    + new java.sql.Date(fechaInicio.getTime()) + "&fin=" + new java.sql.Date(fechaFin.getTime()));
            List<Loan> loans = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                loans.add(loanResumenDesdeJson(e.getAsJsonObject()));
            }
            return loans;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllLoanssDirecto(fechaInicio, fechaFin);
    }

    private List<Loan> getAllLoanssDirecto(Date fechaInicio, Date fechaFin) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT \n"
                + "    l.ID, l.ModifiedAt, l.SoliNum, \n"
                + "    e1.fullName AS SolicitorName, \n"
                + "    e2.fullName AS GuarantorName, \n"
                + "    (SELECT SUM(MonthlyFeeValue - IFNULL(payment, 0)) \n"
                + "     FROM loandetail \n"
                + "     WHERE LoanID = l.RefinanceParentID AND State IN ('pendiente', 'parcial') \n"
                + "     GROUP BY LoanID) AS Refinanciado,\n"
                + "    l.RequestedAmount, l.AmountWithdrawn,\n"
                + "    l.Dues,\n"
                + "    dd.TotalInterest,\n"
                + "    dd.TotalIntangibleFund,\n"
                + "    dd.MonthlyCapitalInstallment,\n"
                + "    dd.MonthlyInterestFee,\n"
                + "    dd.MonthlyIntangibleFundFee,\n"
                + "    dd.MonthlyFeeValue,\n"
                + "    l.State, l.PaymentResponsibility \n"
                + "FROM loan l \n"
                + "LEFT JOIN employees e1 ON l.EmployeeID = e1.national_id \n"
                + "LEFT JOIN employees e2 ON l.GuarantorId = e2.national_id\n"
                + "LEFT JOIN (\n"
                + "    SELECT ld.* \n"
                + "    FROM loandetail ld \n"
                + "    WHERE ld.ID = (SELECT MIN(ID) FROM loandetail WHERE LoanID = ld.LoanID) \n"
                + ") dd ON dd.LoanID = l.ID \n"
                + "WHERE DATE(l.CreatedAt) BETWEEN ? AND ? ORDER BY l.ID DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            statement.setDate(2, new java.sql.Date(fechaFin.getTime()));

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                loans.add(new Loan(
                        rs.getInt("ID"),
                        rs.getDate("ModifiedAt") == null ? "" : rs.getDate("ModifiedAt").toString(),
                        rs.getString("SoliNum"),
                        rs.getString("SolicitorName"),
                        rs.getString("GuarantorName"),
                        rs.getBigDecimal("RequestedAmount"),
                        rs.getBigDecimal("AmountWithdrawn"),
                        rs.getBigDecimal("Refinanciado"),
                        rs.getInt("Dues"),
                        rs.getBigDecimal("TotalInterest"),
                        rs.getBigDecimal("TotalIntangibleFund"),
                        rs.getBigDecimal("MonthlyCapitalInstallment"),
                        rs.getBigDecimal("MonthlyInterestFee"),
                        rs.getBigDecimal("MonthlyIntangibleFundFee"),
                        rs.getBigDecimal("MonthlyFeeValue"),
                        rs.getString("State"),
                        rs.getString("PaymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error -< " + e.getMessage());
            e.printStackTrace();
        }
        return loans;
    }

    //
    public void fillLoanTable(javax.swing.JTable jTable) {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/tabla");

            DefaultTableModel model = (DefaultTableModel) jTable.getModel();
            model.setRowCount(0); // Limpiamos la tabla antes de agregar datos

            for (JsonElement e : resp.getAsJsonArray("data")) {
                JsonObject o = e.getAsJsonObject();
                model.addRow(new Object[]{
                    //enteroDeLoan(o, "id"),
                    textoDeLoan(o, "soliNum"),
                    textoDeLoan(o, "solicitorName"),
                    textoDeLoan(o, "guarantorName") == null ? "" : textoDeLoan(o, "guarantorName"),
                    decimalDeLoan(o, "requestedAmount"),
                    decimalDeLoan(o, "amountWithdrawn"),
                    textoDeLoan(o, "state"),
                    textoDeLoan(o, "paymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
                });
            }
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        fillLoanTableDirecto(jTable);
    }

    private void fillLoanTableDirecto(javax.swing.JTable jTable) {
        String query = """
             SELECT
                 l.ID,
                 l.SoliNum,
                 e1.fullName AS SolicitorName,
                 e2.fullName AS GuarantorName,
                 l.RequestedAmount,
                 l.AmountWithdrawn,
                 l.State,
                 l.PaymentResponsibility
             FROM loan l
             LEFT JOIN employees e1 ON l.EmployeeID = e1.national_id
             LEFT JOIN employees e2 ON l.GuarantorId = e2.national_id
         """;

        DefaultTableModel model = (DefaultTableModel) jTable.getModel();
        model.setRowCount(0); // Limpiamos la tabla antes de agregar datos

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                model.addRow(new Object[]{
                    //rs.getInt("ID"),
                    rs.getString("SoliNum"),
                    rs.getString("SolicitorName"),
                    rs.getString("GuarantorName") == null ? "" : rs.getString("GuarantorName"),
                    rs.getBigDecimal("RequestedAmount"),
                    rs.getBigDecimal("AmountWithdrawn"),
                    rs.getString("State"),
                    rs.getString("PaymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //
    public Double createLoanWithStateValidation(LoanTb newLoan, Double montoo) throws SQLException {
        try {
            return createLoanConBackend(newLoan, montoo);
        } catch (SQLException e) {
            throw e; // regla de negocio del propio flujo (ej. prestamo Pendiente), no es caida del backend
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return createLoanWithStateValidationDirecto(newLoan, montoo);
    }

    /**
     * Mismo flujo que el original: los dialogos de confirmacion y el calculo
     * de montos siguen aqui; el tramo de escritura (refinanciar padre +
     * INSERT + SoliNum %08d) es UNA transaccion server-side en el backend.
     */
    private Double createLoanConBackend(LoanTb newLoan, Double montoo)
            throws SQLException, IOException, InterruptedException {
        Double monto = 0.0;

        // 1. Verificar préstamos en estado Pendiente
        JsonObject check = ApiBackend.get("/integracion/ft/prestamos/pendiente-check?employeeId="
                + newLoan.getEmployeeId() + "&state=" + LoanTb.LoanState.Pendiente.name());
        if (check.getAsJsonObject("data").get("tiene").getAsBoolean()) {
            throw new SQLException(" El empleado tiene un préstamo en proceso (Pendiente).");
        }

        // 2. Buscar préstamo Aceptado más reciente
        Optional<LoanTb> activeLoan;
        try {
            JsonObject act = ApiBackend.get("/integracion/ft/prestamos/activo?employeeId="
                    + newLoan.getEmployeeId() + "&state=" + LoanTb.LoanState.Aceptado.name());
            activeLoan = Optional.of(loanTbDesdeJson(act.getAsJsonObject("data"), false));
        } catch (ApiBackend.NoEncontradoException e) {
            activeLoan = Optional.empty();
        }

        Integer refinanciarLoanId = null;

        // 3. Manejar refinanciación si existe préstamo Aceptado
        if (activeLoan.isPresent()) {

            Double montoRefinanciado = 0.0;

            if (activeLoan.get().getRefinanceParentId() != null) {

                if (activeLoan.get().getRefinanceParentId() != 0) {

                    if (JOptionPane.showConfirmDialog(null, "¿El solicitante tiene un refinanciamiento ya puesto, desea poner otro? ") != JOptionPane.YES_OPTION) {
                        return null;
                    }
                }
            } else {
                if (JOptionPane.showConfirmDialog(null, "¿Esta seguro de refinanciar el prestamo? ") != JOptionPane.YES_OPTION) {
                    return null;
                }
            }

            montoRefinanciado = calcularMontoPendientePorLoanId(activeLoan.get().getId());

            if (montoRefinanciado > (newLoan.getRequestedAmount() - montoo)) {
                JOptionPane.showMessageDialog(null, "Error el monto adeudado del anterior prestamo es mas grande", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            // handleRefinancing: el UPDATE del padre lo hace el backend en la
            // misma transaccion del INSERT (refinanciarLoanId)
            refinanciarLoanId = activeLoan.get().getId();
            newLoan.setRefinanceParentId(refinanciarLoanId);
            newLoan.setAmountWithdrawn((newLoan.getRequestedAmount() - montoo) - montoRefinanciado);
            monto = montoRefinanciado;

            JOptionPane.showMessageDialog(null, "Ya esta refinanciando, vaya al listado y acepte el nuevo solicitó para procesar las cuotas", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);

        } else {
            if (montoo > 0.0) {
                newLoan.setAmountWithdrawn(newLoan.getRequestedAmount() - montoo);
                monto = montoo;
            }
        }

        JsonObject resp = ApiBackend.post("/integracion/ft/prestamos", cuerpoPrestamo(newLoan, refinanciarLoanId));
        soliNum = resp.getAsJsonObject("data").get("soliNum").getAsString();

        JOptionPane.showMessageDialog(null, "Se registro el prestamo", "GÉSTION PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
        return monto;
    }

    private Double createLoanWithStateValidationDirecto(LoanTb newLoan, Double montoo) throws SQLException {
        Double monto = 0.0;
        try {
            connection.setAutoCommit(false);

            // 1. Verificar préstamos en estado Pendiente
            if (hasLoanInState(newLoan.getEmployeeId(), LoanTb.LoanState.Pendiente)) {
                throw new SQLException(" El empleado tiene un préstamo en proceso (Pendiente).");
            }

//            if (hasLoanInState(newLoan.getEmployeeId(), LoanTb.LoanState.Refinanciado)) {
//                throw new SQLException(" El empleado tiene un préstamo en estado (Refinanciado).");
//            }
//            if (verifRefinan(newLoan.getEmployeeId())) {
//                throw new SQLException(" El empleado ya contiene un refinanciamiento puesto");
//            }
            // 2. Buscar préstamo Aceptado más reciente
            Optional<LoanTb> activeLoan = findLatestLoanByState(
                    newLoan.getEmployeeId(),
                    LoanTb.LoanState.Aceptado
            );

//            Optional<LoanTb> activeLoanRefi = findLatestLoanByState(
//                    newLoan.getEmployeeId(),
//                    LoanTb.LoanState.Refinanciado
//            );
            // 3. Manejar refinanciación si existe préstamo Aceptado
            if (activeLoan.isPresent()) {

                Double montoRefinanciado = 0.0;

                if (activeLoan.get().getRefinanceParentId() != null) {

                    if (activeLoan.get().getRefinanceParentId() != 0) {

                        if (JOptionPane.showConfirmDialog(null, "¿El solicitante tiene un refinanciamiento ya puesto, desea poner otro? ") != JOptionPane.YES_OPTION) {
                            return null;
                        }
                    }
                } else {
                    if (JOptionPane.showConfirmDialog(null, "¿Esta seguro de refinanciar el prestamo? ") != JOptionPane.YES_OPTION) {
                        return null;
                    }
                }

                montoRefinanciado = calcularMontoPendientePorLoanId(activeLoan.get().getId());

                if (montoRefinanciado > (newLoan.getRequestedAmount() - montoo)) {
                    JOptionPane.showMessageDialog(null, "Error el monto adeudado del anterior prestamo es mas grande", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                handleRefinancing(activeLoan.get(), newLoan);
                newLoan.setAmountWithdrawn((newLoan.getRequestedAmount() - montoo) - montoRefinanciado);
                monto = montoRefinanciado;

                JOptionPane.showMessageDialog(null, "Ya esta refinanciando, vaya al listado y acepte el nuevo solicitó para procesar las cuotas", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);

            } else {
                if (montoo > 0.0) {
                    newLoan.setAmountWithdrawn(newLoan.getRequestedAmount() - montoo);
                    monto = montoo;
                }
            }

            insertNewLoan(newLoan);

            if (newLoan == null) {
                return null;
            }

            JOptionPane.showMessageDialog(null, "Se registro el prestamo", "GÉSTION PRESTAMO", JOptionPane.INFORMATION_MESSAGE);
            connection.commit();
            return monto;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private boolean hasLoanInState(String employeeId, LoanTb.LoanState state) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loan WHERE StateLoan = 'Pendiente' AND PaymentResponsibility = 'EMPLOYEE' AND EmployeeID = ? AND State = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, state.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean verifRefinan(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loan WHERE StateLoan = 'Pendiente' AND EmployeeID = ? AND PaymentResponsibility = 'EMPLOYEE' AND  RefinanceParentID is  NOT NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Optional<LoanTb> findLatestLoanByState(String employeeId, LoanTb.LoanState state) throws SQLException {
        String sql = """
                     SELECT *
                     FROM `loan`
                     WHERE `EmployeeID` = ?
                       AND `State` = ?
                       AND `StateLoan` = 'Pendiente'
                     AND  `RefinanceParentID` IS NULL AND PaymentResponsibility = 'EMPLOYEE'
                     ORDER BY `CreatedAt` DESC LIMIT 1;""";
        System.out.println("Lola");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, state.name());
            System.out.println("Peep");
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Result -> " + rs.getRow());
                return rs.next() ? Optional.of(mapResultSetToLoan(rs)) : Optional.empty();
            }
        }
    }

    public Optional<LoanTb> findLoan(int idLoan) throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/por-id/" + idLoan);
            return Optional.of(loanTbDesdeJson(resp.getAsJsonObject("data"), false));
        } catch (ApiBackend.NoEncontradoException e) {
            return Optional.empty(); // el backend confirmo que no existe
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findLoanDirecto(idLoan);
    }

    private Optional<LoanTb> findLoanDirecto(int idLoan) throws SQLException {

        String sql = "SELECT * FROM loan WHERE ID = ?  ORDER BY CreatedAt DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idLoan);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToLoan(rs)) : Optional.empty();
            }
        }
    }

    public Optional<LoanTb> findLoan(String numSoli) throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos/por-soli/" + numSoli);
            return Optional.of(loanTbDesdeJson(resp.getAsJsonObject("data"), false));
        } catch (ApiBackend.NoEncontradoException e) {
            return Optional.empty(); // el backend confirmo que no existe
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findLoanDirecto(numSoli);
    }

    private Optional<LoanTb> findLoanDirecto(String numSoli) throws SQLException {
        String sql = "SELECT * FROM loan WHERE SoliNum = ?  ORDER BY CreatedAt DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numSoli);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToLoan(rs)) : Optional.empty();
            }
        }
    }

    private void handleRefinancing(LoanTb originalLoan, LoanTb newLoan) throws SQLException {

        // Actualizar estado del préstamo original
        String updateSql = "UPDATE loan SET State = 'Refinanciado' , StateLoan = 'Pagado' WHERE ID = ?";

        //  String updateSqlLoanDetail = "UPDATE loandetail SET State = 'Pagado' WHERE LoanID = ? ";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, originalLoan.getId());
            stmt.executeUpdate();
        }

//        try (PreparedStatement stmt = connection.prepareStatement(updateSqlLoanDetail)) {
//            stmt.setInt(1, originalLoan.getId());
//            stmt.executeUpdate();
//        }
        // Configurar nuevo préstamo como refinanciación
        newLoan.setRefinanceParentId(originalLoan.getId());
    }

    private int insertNewLoan(LoanTb loan) throws SQLException {
        String insertSql = "INSERT INTO loan ("
                + "EmployeeID, GuarantorId, RequestedAmount, AmountWithdrawn, Dues, PaymentDate, "
                + "State, StateLoan, RefinanceParentID, CreatedBy, CreatedAt, ModifiedAt, ModifiedBy, Type, PaymentResponsibility"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, loan);
            if (loan == null) {
                return 0;
            }
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Error al obtener ID generado");
                }

                int newId = generatedKeys.getInt(1);
                updateSoliNum(newId); // Generar número de solicitud

                return newId;
            }
        }
    }

    private void updateSoliNum(int loanId) throws SQLException {
        soliNum = String.format("%08d", loanId);
        String updateSql = "UPDATE loan SET SoliNum = ? WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, soliNum);
            stmt.setInt(2, loanId);
            stmt.executeUpdate();
        }
    }

    public void updateSoliNumStatus(String soliNum, String status, int userModifi) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("status", status);
            body.addProperty("userModifi", userModifi);
            ApiBackend.put("/integracion/ft/prestamos/" + soliNum + "/estado", body);
            return;
        } catch (ApiBackend.NoEncontradoException e) {
            throw new SQLException("No se encontró ningún préstamo con SoliNum = " + soliNum);
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        updateSoliNumStatusDirecto(soliNum, status, userModifi);
    }

    private void updateSoliNumStatusDirecto(String soliNum, String status, int userModifi) throws SQLException {
        String selectSql = "SELECT RefinanceParentId FROM loan WHERE SoliNum = ?";
        String updateParentSql = "UPDATE loan SET State = ?, ModifiedAt = ?, ModifiedBy = ? WHERE SoliNum = ?";
        String updateChildSql = "UPDATE loan SET State = ?, StateLoan = ?, ModifiedAt = ?, ModifiedBy = ? WHERE Id = ?";

        connection.setAutoCommit(false);

        try (
                PreparedStatement stmtSelect = connection.prepareStatement(selectSql); PreparedStatement stmtUpdateParent = connection.prepareStatement(updateParentSql); PreparedStatement stmtUpdateChild = connection.prepareStatement(updateChildSql)) {
            // 🔹 1️⃣ Buscar el préstamo principal
            stmtSelect.setString(1, soliNum);
            ResultSet rs = stmtSelect.executeQuery();

            if (!rs.next()) {
                throw new SQLException("No se encontró ningún préstamo con SoliNum = " + soliNum);
            }

            int parentId = rs.getInt("RefinanceParentId");

            // 🔹 2️⃣ Actualizar préstamo principal
            stmtUpdateParent.setString(1, status);
            stmtUpdateParent.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmtUpdateParent.setInt(3, userModifi);
            stmtUpdateParent.setString(4, soliNum);
            stmtUpdateParent.executeUpdate();

            // 🔹 3️⃣ Si el estado es "Cancelado", buscar préstamo refinanciado
            if (status.equalsIgnoreCase("Denegado")) {

                //  StateLoan   tiene que estar en pendiente
                // como arreglar en caso

                stmtUpdateChild.setString(1, "Aceptado");
                stmtUpdateChild.setString(2, "Pendiente");
                stmtUpdateChild.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmtUpdateChild.setInt(4, userModifi);
                stmtUpdateChild.setInt(5, parentId);

                int updatedChild = stmtUpdateChild.executeUpdate();

                if (updatedChild > 0) {
                    System.out.println("✔ El préstamo refinanciado fue actualizado a 'Aceptado'.");
                }

            }

            connection.commit();

        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void setInsertParameters(PreparedStatement stmt, LoanTb loan) throws SQLException {

        stmt.setString(1, loan.getEmployeeId());
        stmt.setString(2, loan.getGuarantorIds() == null ? null : loan.getGuarantorIds());

        // Manejar RequestedAmount - usar 0.0 si es null
        if (loan.getRequestedAmount() != null) {
            stmt.setDouble(3, loan.getRequestedAmount());
        } else {
            stmt.setDouble(3, 0.0);
        }

        // Manejar AmountWithdrawn - usar 0.0 si es null
        if (loan.getAmountWithdrawn() != null) {
            stmt.setDouble(4, loan.getAmountWithdrawn());
        } else {
            stmt.setDouble(4, 0.0);
        }

        stmt.setInt(5, loan.getDues());
        stmt.setDate(6, Date.valueOf(loan.getPaymentDate()));
        stmt.setString(7, loan.getState());
        stmt.setString(8, "Pendiente");
        stmt.setObject(9, loan.getRefinanceParentId(), Types.INTEGER);
        stmt.setInt(10, loan.getCreatedBy());
        stmt.setDate(11, Date.valueOf(loan.getCreatedAt().toLocalDate()));
        stmt.setTimestamp(12, loan.getModifiedAt() != null ? Timestamp.valueOf(loan.getModifiedAt()) : null);
        stmt.setObject(13, loan.getModifiedBy(), Types.INTEGER);
        stmt.setString(14, loan.getType());
        stmt.setString(15, loan.getPaymentResponsibility() != null ? loan.getPaymentResponsibility() : "EMPLOYEE");
    }

    private LoanTb mapResultSetToLoan(ResultSet rs) throws SQLException {
        LoanTb loan = new LoanTb(
                rs.getString("EmployeeID"),
                rs.getString("GuarantorId"),
                rs.getDouble("AmountWithdrawn"),
                rs.getDouble("RequestedAmount"),
                rs.getInt("Dues"),
                rs.getDate("PaymentDate").toLocalDate(),
                rs.getString("State"),
                rs.getInt("RefinanceParentID"),
                rs.getInt("CreatedBy"),
                rs.getDate("CreatedAt"),
                rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null,
                rs.getInt("ModifiedBy"),
                rs.getString("Type"),
                rs.getString("PaymentResponsibility")
        );
        loan.setId(rs.getInt("ID"));
        loan.setSoliNum(rs.getString("SoliNum"));
        loan.setStateLoan(rs.getString("StateLoan"));
        return loan;
    }

    // ─── Helpers JSON → entidad (mismos tipos que devolvia el ResultSet) ──

    private static String textoDeLoan(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    /** rs.getInt: 0 cuando la columna es NULL. */
    private static int enteroDeLoan(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0 : v.getAsInt();
    }

    /** rs.getDouble: 0.0 cuando la columna es NULL. */
    private static double dobleDeLoan(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0.0 : v.getAsDouble();
    }

    /** rs.getBigDecimal: null cuando la columna es NULL. */
    private static BigDecimal decimalDeLoan(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsBigDecimal();
    }

    /** rs.getDate: java.sql.Date o null ("yyyy-MM-dd..." desde el backend). */
    private static Date fechaSqlDeLoan(JsonObject o, String campo) {
        String v = textoDeLoan(o, campo);
        if (v == null) {
            return null;
        }
        return Date.valueOf(v.substring(0, Math.min(10, v.length())));
    }

    /** rs.getTimestamp(...).toLocalDateTime() o null. */
    private static LocalDateTime fechaHoraDeLoan(JsonObject o, String campo) {
        String v = textoDeLoan(o, campo);
        if (v == null) {
            return null;
        }
        if (v.length() == 10) {
            return LocalDate.parse(v).atStartOfDay();
        }
        return LocalDateTime.parse(v, FT_FECHA_HORA_API);
    }

    /**
     * Replica el mapeo por ResultSet de LoanTb. refinanceCeroComoNull=true
     * replica el `rs.getInt(...) == 0 ? null : ...` de getAllLoans y
     * getLoansByDateRange; con false replica el rs.getInt directo (NULL -> 0)
     * de findLoansByEmployeeId y mapResultSetToLoan.
     */
    private static LoanTb loanTbDesdeJson(JsonObject o, boolean refinanceCeroComoNull) {
        LoanTb loan = new LoanTb();
        loan.setId(enteroDeLoan(o, "id"));
        loan.setSoliNum(textoDeLoan(o, "soliNum"));
        loan.setEmployeeId(textoDeLoan(o, "employeeId"));
        loan.setGuarantorIds(textoDeLoan(o, "guarantorId"));
        loan.setRequestedAmount(dobleDeLoan(o, "requestedAmount"));
        loan.setAmountWithdrawn(dobleDeLoan(o, "amountWithdrawn"));
        loan.setDues(enteroDeLoan(o, "dues"));
        // sin null-check, igual que rs.getDate("PaymentDate").toLocalDate()
        loan.setPaymentDate(LocalDate.parse(textoDeLoan(o, "paymentDate")));
        loan.setState(textoDeLoan(o, "state"));
        loan.setStateLoan(textoDeLoan(o, "stateLoan"));
        int refinance = enteroDeLoan(o, "refinanceParentId");
        loan.setRefinanceParentId(refinanceCeroComoNull && refinance == 0 ? null : refinance);
        loan.setCreatedBy(enteroDeLoan(o, "createdBy"));
        loan.setCreatedAt(fechaSqlDeLoan(o, "createdAt"));
        loan.setModifiedAt(fechaHoraDeLoan(o, "modifiedAt"));
        loan.setModifiedBy(enteroDeLoan(o, "modifiedBy"));
        loan.setType(textoDeLoan(o, "type"));
        loan.setPaymentResponsibility(textoDeLoan(o, "paymentResponsibility"));
        return loan;
    }

    /** Replica el mapeo del "resumen" (searchLoan/getLastLoans/getAllLoanss). */
    private static Loan loanResumenDesdeJson(JsonObject o) {
        String modificado = textoDeLoan(o, "modifiedAt");
        return new Loan(
                enteroDeLoan(o, "id"),
                // rs.getDate("ModifiedAt").toString() -> solo "yyyy-MM-dd"
                modificado == null ? "" : modificado.substring(0, Math.min(10, modificado.length())),
                textoDeLoan(o, "soliNum"),
                textoDeLoan(o, "solicitorName"),
                textoDeLoan(o, "guarantorName"),
                decimalDeLoan(o, "requestedAmount"),
                decimalDeLoan(o, "amountWithdrawn"),
                decimalDeLoan(o, "refinanciado"),
                enteroDeLoan(o, "dues"),
                decimalDeLoan(o, "totalInterest"),
                decimalDeLoan(o, "totalIntangibleFund"),
                decimalDeLoan(o, "monthlyCapitalInstallment"),
                decimalDeLoan(o, "monthlyInterestFee"),
                decimalDeLoan(o, "monthlyIntangibleFundFee"),
                decimalDeLoan(o, "monthlyFeeValue"),
                textoDeLoan(o, "state"),
                textoDeLoan(o, "paymentResponsibility").equalsIgnoreCase("EMPLOYEE") ? "SOLICITANTE" : "AVAL"
        );
    }

    /** Body del POST /integracion/ft/prestamos (mismos valores que setInsertParameters). */
    private static JsonObject cuerpoPrestamo(LoanTb loan, Integer refinanciarLoanId) {
        JsonObject body = new JsonObject();
        body.addProperty("employeeId", loan.getEmployeeId());
        body.addProperty("guarantorId", loan.getGuarantorIds());
        body.addProperty("requestedAmount", loan.getRequestedAmount());
        body.addProperty("amountWithdrawn", loan.getAmountWithdrawn());
        body.addProperty("dues", loan.getDues());
        // sin null-check, igual que Date.valueOf(loan.getPaymentDate())
        body.addProperty("paymentDate", loan.getPaymentDate().toString());
        body.addProperty("state", loan.getState());
        body.addProperty("refinanceParentId", loan.getRefinanceParentId());
        body.addProperty("createdBy", loan.getCreatedBy());
        // sin null-check, igual que Date.valueOf(loan.getCreatedAt().toLocalDate())
        body.addProperty("createdAt", loan.getCreatedAt().toLocalDate().toString());
        body.addProperty("modifiedAt", loan.getModifiedAt() != null
                ? loan.getModifiedAt().format(FT_FECHA_HORA_API) : null);
        body.addProperty("modifiedBy", loan.getModifiedBy());
        body.addProperty("type", loan.getType());
        body.addProperty("paymentResponsibility", loan.getPaymentResponsibility());
        body.addProperty("refinanciarLoanId", refinanciarLoanId);
        return body;
    }
}
