/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.entity.LoanTb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDao  extends  LoanDetailsDao{

    private final Connection connection;

    public LoanDao(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    //
    public void fillLoanTable(javax.swing.JTable jTable) {
        String query = """
             SELECT 
                 l.ID, 
                 l.SoliNum, 
                 CONCAT(e1.first_name, ' ', e1.last_name) AS SolicitorName,
                 CONCAT(e2.first_name, ' ', e2.last_name) AS GuarantorName,
                 l.OriginalAmount, 
                 l.Amount, 
                 l.State
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
                    rs.getString("GuarantorName"),
                    rs.getBigDecimal("OriginalAmount"),
                    rs.getBigDecimal("Amount"),
                    rs.getString("State")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //
    public Double createLoanWithStateValidation(LoanTb newLoan) throws SQLException {
        Double monto = 0.0;
        try {
            connection.setAutoCommit(false);

            // 1. Verificar préstamos en estado Pendiente
            if (hasLoanInState(newLoan.getEmployeeId(), LoanTb.LoanState.Pendiente)) {
                throw new SQLException(" El empleado tiene un préstamo en proceso (Pendiente).");
            }

            if (hasLoanInState(newLoan.getEmployeeId(), LoanTb.LoanState.Refinanciado)) {
                throw new SQLException(" El empleado tiene un préstamo en estado (Refinanciado).");
            }

            // 2. Buscar préstamo Aceptado más reciente
            Optional<LoanTb> activeLoan = findLatestLoanByState(
                    newLoan.getEmployeeId(),
                    LoanTb.LoanState.Aceptado
            );
            // 3. Manejar refinanciación si existe préstamo Aceptado
            if (activeLoan.isPresent()) {
                int option = JOptionPane.showConfirmDialog(null, "Desea refinanciar el prestamo anterior ? ", "GESTIÓN PRESTAMO", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.NO_OPTION) {
                    return null;
                }
                handleRefinancing(activeLoan.get(), newLoan);
                monto = activeLoan.get().getOriginalAmount();
            }

            // 4. Insertar nuevo préstamo
            insertNewLoan(newLoan);
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
        String sql = "SELECT COUNT(*) FROM loan WHERE StateLoan = 'Pendiente' AND EmployeeID = ? AND State = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, state.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Optional<LoanTb> findLatestLoanByState(String employeeId, LoanTb.LoanState state) throws SQLException {
        String sql = "SELECT * FROM loan WHERE EmployeeID = ? AND State = ?  AND StateLoan = 'Pendiente' ORDER BY CreatedAt DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, state.name());
            try (ResultSet rs = stmt.executeQuery()) {
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
        String updateSql = "UPDATE loan SET State = 'Refinanciado' WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, originalLoan.getId());
            stmt.executeUpdate();
        }
        double origin = newLoan.getOriginalAmount();

        newLoan.setOriginalAmount(originalLoan.getOriginalAmount());
        // Configurar nuevo préstamo como refinanciación
        newLoan.setRefinanceParentId(originalLoan.getId());

        newLoan.setAmount(origin + originalLoan.getOriginalAmount());
    }

    private int insertNewLoan(LoanTb loan) throws SQLException {
        String insertSql = "INSERT INTO loan ("
                + "EmployeeID, GuarantorId, Amount, OriginalAmount, Dues, PaymentDate, "
                + "State,StateLoan, RefinanceParentID, CreatedBy, CreatedAt, ModifiedAt, ModifiedBy, Type"
                + ") VALUES (?, ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, loan);
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
        String soliNum = String.format("%08d", loanId);
        String updateSql = "UPDATE loan SET SoliNum = ? WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, soliNum);
            stmt.setInt(2, loanId);
            stmt.executeUpdate();
        }
    }

    public void updateSoliNumStatus(String soliNum, String status, int userModifi) throws SQLException {

        String updateSql = "UPDATE loan SET State = ? , ModifiedAt = ? ,ModifiedBy = ?  WHERE SoliNum = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, userModifi);
            stmt.setString(4, soliNum);

            stmt.executeUpdate();
        }
    }

    private void setInsertParameters(PreparedStatement stmt, LoanTb loan) throws SQLException {
        stmt.setString(1, loan.getEmployeeId());
        stmt.setString(2, loan.getGuarantorIds() == null ? null : loan.getGuarantorIds());
        stmt.setDouble(3, loan.getAmount());
        stmt.setDouble(4, loan.getOriginalAmount());
        stmt.setInt(5, loan.getDues());
        stmt.setDate(6, Date.valueOf(loan.getPaymentDate()));
        stmt.setString(7, loan.getState().name());
        stmt.setString(8, "Pendiente");
        stmt.setObject(9, loan.getRefinanceParentId(), Types.INTEGER);
        stmt.setInt(10, loan.getCreatedBy());
        stmt.setTimestamp(11, Timestamp.valueOf(loan.getCreatedAt()));
        stmt.setTimestamp(12, loan.getModifiedAt() != null ? Timestamp.valueOf(loan.getModifiedAt()) : null);
        stmt.setObject(13, loan.getModifiedBy(), Types.INTEGER);
        stmt.setString(14, loan.getType());
    }

    private LoanTb mapResultSetToLoan(ResultSet rs) throws SQLException {
        LoanTb loan = new LoanTb(
                rs.getString("EmployeeID"),
                rs.getString("GuarantorId"),
                rs.getDouble("Amount"),
                rs.getDouble("OriginalAmount"),
                rs.getInt("Dues"),
                rs.getDate("PaymentDate").toLocalDate(),
                LoanTb.LoanState.valueOf(rs.getString("State")),
                rs.getInt("RefinanceParentID"),
                rs.getInt("CreatedBy"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
                rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null,
                rs.getInt("ModifiedBy"),
                rs.getString("Type")
        );
        loan.setId(rs.getInt("ID"));
        loan.setSoliNum(rs.getString("SoliNum"));
        loan.setStateLoan(rs.getString("StateLoan"));
        return loan;
    }
}
