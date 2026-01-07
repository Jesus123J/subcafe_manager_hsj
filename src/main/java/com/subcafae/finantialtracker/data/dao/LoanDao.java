/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.Loan;
import com.subcafae.finantialtracker.data.entity.LoanTb;
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
 *
 * @author Jesus Gutierrez
 */
public class LoanDao extends LoanDetailsDao {

    private final Connection connection;
    public String soliNum;

    public LoanDao() {
        this.connection = Conexion.getConnection();
    }

    // Método para obtener todos los números de solicitud para autocompletado
    public List<String> getAllSoliNums() {
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
        List<LoanTb> loans = new ArrayList<>();
        System.out.println("Fecha inicial -> " + fechaInicio);
        System.out.println("Fecha fin -> " + fechaFin);
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

    public List<Loan> getAllLoanss(Date fechaInicio, Date fechaFin) {
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
                + "WHERE DATE(l.CreatedAt) BETWEEN ? AND ? ORDER BY l.CreatedAt ASC";

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

        String sql = "SELECT * FROM loan WHERE ID = ?  ORDER BY CreatedAt DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idLoan);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapResultSetToLoan(rs)) : Optional.empty();
            }
        }
    }

    public Optional<LoanTb> findLoan(String numSoli) throws SQLException {
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
                + "State,StateLoan, RefinanceParentID, CreatedBy, CreatedAt, ModifiedAt, ModifiedBy, Type"
                + ") VALUES (?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        String selectSql = "SELECT RefinanceParentId FROM loan WHERE SoliNum = ?";
        String updateParentSql = "UPDATE loan SET State = ?, ModifiedAt = ?, ModifiedBy = ? WHERE SoliNum = ?";
        String updateChildSql = "UPDATE loan SET State = ?, ModifiedAt = ?, ModifiedBy = ? WHERE Id = ?";

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

                stmtUpdateChild.setString(1, "Aceptado");
                stmtUpdateChild.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmtUpdateChild.setInt(3, userModifi);
                stmtUpdateChild.setInt(4, parentId);

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
        stmt.setDouble(3, loan.getRequestedAmount());
        stmt.setDouble(4, loan.getAmountWithdrawn());
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
}
