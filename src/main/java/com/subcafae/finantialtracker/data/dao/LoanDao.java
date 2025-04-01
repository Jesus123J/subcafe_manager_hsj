/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
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

    public LoanDao() {
        this.connection = Conexion.getConnection();
    }
    // Método para buscar préstamos por EmployeeID excluyendo aquellos con PaymentResponsibility = 'GUARANTOR'

    public List<LoanTb> findLoansByEmployeeId(String employeeId) throws SQLException {
        String sql = "SELECT * FROM loan WHERE EmployeeID = ? AND PaymentResponsibility = 'EMPLOYEE'";

        List<LoanTb> loans = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId); // Asigna el ID del empleado

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
                    loan.setCreatedAt(rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null);
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
                loan.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                loan.setModifiedAt(rs.getTimestamp("ModifiedAt").toLocalDateTime());

                loan.setModifiedBy(rs.getInt("ModifiedBy"));
                loan.setType(rs.getString("Type"));
                loan.setPaymentResponsibility(rs.getString("PaymentResponsibility"));
                
                loans.add(loan);
            }
        }

        return loans;
    }

    //
    public void fillLoanTable(javax.swing.JTable jTable) {
        String query = """
             SELECT 
                 l.ID, 
                 l.SoliNum, 
                 CONCAT(e1.first_name, ' ', e1.last_name) AS SolicitorName,
                 CONCAT(e2.first_name, ' ', e2.last_name) AS GuarantorName,
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
                    rs.getString("PaymentResponsibility")
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
            if (verifRefinan(newLoan.getEmployeeId())) {
                throw new SQLException(" El empleado ya contiene un refinanciamiento puesto");
            }

            // 2. Buscar préstamo Aceptado más reciente
            Optional<LoanTb> activeLoan = findLatestLoanByState(
                    newLoan.getEmployeeId(),
                    LoanTb.LoanState.Aceptado
            );
            // 3. Manejar refinanciación si existe préstamo Aceptado
            if (activeLoan.isPresent()) {

                Double montoRefinanciado = calcularMontoPendientePorLoanId(activeLoan.get().getId());

                if (montoRefinanciado > newLoan.getRequestedAmount()) {
                    JOptionPane.showMessageDialog(null, "Error el refinanciamiento es mas grande", "GESTIÓN PRESTAMO", JOptionPane.WARNING_MESSAGE);
                    return null;
                }
                int option = JOptionPane.showConfirmDialog(null, "Desea refinanciar el prestamo anterior ? ", "GESTIÓN PRESTAMO", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.NO_OPTION) {
                    return null;
                }

                handleRefinancing(activeLoan.get(), newLoan);
                newLoan.setAmountWithdrawn(newLoan.getRequestedAmount() - montoRefinanciado);
                monto = montoRefinanciado;
            }

            // 4. Insertar nuevo préstamo
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
        String sql = "SELECT COUNT(*) FROM loan WHERE StateLoan = 'Pendiente' AND EmployeeID = ? AND State = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            stmt.setString(2, state.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean verifRefinan(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loan WHERE StateLoan = 'Pendiente' AND EmployeeID = ? AND  RefinanceParentID is  NOT NULL";
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
                     AND  `RefinanceParentID` IS NULL 
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

        String updateSqlLoanDetail = "UPDATE loandetail SET State = 'Pagado' WHERE LoanID = ? ";

        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setInt(1, originalLoan.getId());
            stmt.executeUpdate();
        }

        try (PreparedStatement stmt = connection.prepareStatement(updateSqlLoanDetail)) {
            stmt.setInt(1, originalLoan.getId());
            stmt.executeUpdate();
        }

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
        stmt.setDouble(3, loan.getRequestedAmount());
        stmt.setDouble(4, loan.getAmountWithdrawn());
        stmt.setInt(5, loan.getDues());
        stmt.setDate(6, Date.valueOf(loan.getPaymentDate()));
        stmt.setString(7, loan.getState());
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
                rs.getDouble("AmountWithdrawn"),
                rs.getDouble("RequestedAmount"),
                rs.getInt("Dues"),
                rs.getDate("PaymentDate").toLocalDate(),
                rs.getString("State"),
                rs.getInt("RefinanceParentID"),
                rs.getInt("CreatedBy"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
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
