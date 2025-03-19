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
import java.sql.Time;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDao {

    private final Connection connection;

    public LoanDao(Connection connection) {
        this.connection = connection;
    }

    public int insert(LoanTb loan) throws SQLException {
        String insertSql = "INSERT INTO loan ("
                + "EmployeeID, GuarantorId,  Amount, OriginalAmount, Dues, PaymentDate, State, "
                + "RefinanceParentID, CreatedBy, CreatedAt, ModifiedAt, ModifiedBy, Type"
                + ") VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false); // Iniciar transacción

            int newId;
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                setInsertParameters(insertStmt, loan);
                insertStmt.executeUpdate();

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("No se obtuvo el ID generado");
                    }
                    newId = generatedKeys.getInt(1);
                    loan.setId(newId);
                }
            }

            // Paso 2: Generar y actualizar SoliNum
            String soliNum = String.format("%08d", newId); // 8 dígitos con ceros a la izquierda
            String updateSql = "UPDATE loan SET SoliNum = ? WHERE ID = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setString(1, soliNum);
                updateStmt.setInt(2, newId);
                updateStmt.executeUpdate();
            }

            connection.commit(); // Confirmar transacción
            return newId;

        } catch (SQLException e) {
            connection.rollback(); // Revertir en caso de error
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void setInsertParameters(PreparedStatement stmt, LoanTb loan) throws SQLException {
        stmt.setString(1, loan.getEmployeeId());
        if (loan.getGuarantorIds() != null) {
            stmt.setString(2, loan.getGuarantorIds());
        } else {
            stmt.setNull(2, Types.VARCHAR);
        }

        stmt.setDouble(3, loan.getAmount());
        stmt.setDouble(4, loan.getOriginalAmount());
        stmt.setInt(5, loan.getDues());
        stmt.setDate(6, Date.valueOf(loan.getPaymentDate()));
        stmt.setString(7, loan.getState().name());
        if (loan.getRefinanceParentId() != null) {
            stmt.setInt(8, loan.getRefinanceParentId());
        } else {
            stmt.setNull(8, Types.INTEGER);
        }
        stmt.setInt(9, loan.getCreatedBy());
        stmt.setTimestamp(10, Timestamp.valueOf(loan.getCreatedAt()));
        if (loan.getModifiedAt() != null) {
            stmt.setTimestamp(11, Timestamp.valueOf(loan.getModifiedAt()));
        } else {
            stmt.setNull(11, Types.TIMESTAMP);
        }
        if (loan.getModifiedBy() != null) {
            stmt.setInt(12, loan.getModifiedBy());
        } else {
            stmt.setNull(12, Types.INTEGER);
        }
        stmt.setString(13, loan.getType());
    }

    // Método para llenar el JTable
    
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

    // Otros métodos (findById, update, etc.)...
}
