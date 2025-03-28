/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import com.subcafae.finantialtracker.report.HistoryPayment.LoanDetailResult;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDetailsDao extends EmployeeDao {

    private final Connection connection;

    // Constructor para inicializar la conexión
    public LoanDetailsDao() {
        this.connection = Conexion.getConnection();
    }

    public List<LoanDetailsTb> getAllLoanDetails() throws SQLException {
        String sql = "SELECT ID, LoanID, Dues, TotalInterest, TotalIntangibleFund, MonthlyCapitalInstallment, "
                + "MonthlyInterestFee, MonthlyIntangibleFundFee, MonthlyFeeValue, payment, PaymentDate, State, "
                + "CreatedBy, CreatedAt, ModifiedBy, ModifiedAt FROM loandetail";

        List<LoanDetailsTb> loanDetails = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoanDetailsTb loanDetail = new LoanDetailsTb();
                loanDetail.setId(rs.getInt("ID"));
                loanDetail.setLoanId(rs.getInt("LoanID"));
                loanDetail.setDues(rs.getInt("Dues"));
                loanDetail.setTotalInterest(rs.getDouble("TotalInterest"));
                loanDetail.setTotalIntangibleFund(rs.getDouble("TotalIntangibleFund"));
                loanDetail.setMonthlyCapitalInstallment(rs.getDouble("MonthlyCapitalInstallment"));
                loanDetail.setMonthlyInterestFee(rs.getDouble("MonthlyInterestFee"));
                loanDetail.setMonthlyIntangibleFundFee(rs.getDouble("MonthlyIntangibleFundFee"));
                loanDetail.setMonthlyFeeValue(rs.getDouble("MonthlyFeeValue"));
                loanDetail.setPayment(rs.getDouble("payment"));

                // Convertir PaymentDate a LocalDate
                loanDetail.setPaymentDate(rs.getDate("PaymentDate"));

                loanDetail.setState(rs.getString("State"));
                loanDetail.setCreatedBy(rs.getInt("CreatedBy"));

                // Convertir CreatedAt y ModifiedAt a LocalDateTime
                loanDetail.setCreatedAt(rs.getTimestamp("CreatedAt") == null ? null : rs.getTimestamp("CreatedAt").toLocalDateTime());
                loanDetail.setModifiedBy(rs.getInt("ModifiedBy"));

                loanDetail.setModifiedAt(rs.getTimestamp("ModifiedAt") == null ? null : rs.getTimestamp("ModifiedAt").toLocalDateTime());

                loanDetails.add(loanDetail);
            }
        }

        return loanDetails;
    }

    // Método para insertar múltiples LoanDetails
    public void insertMultipleLoanDetails(LoanDetailsTb loanDetails, LoanTb loan, int user) throws SQLException {
        String insertSql = "INSERT INTO loandetail (LoanID, Dues, TotalInterest, TotalIntangibleFund, "
                + "MonthlyCapitalInstallment, MonthlyInterestFee, MonthlyIntangibleFundFee, "
                + " PaymentDate, State, CreatedBy, CreatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {

            LocalDate currentDate = loan.getPaymentDate();

            for (int i = 0; i < loan.getDues(); i++) {

                stmt.setInt(1, loan.getId());
                stmt.setInt(2, i + 1);
                stmt.setDouble(3, loanDetails.getTotalInterest());
                stmt.setDouble(4, loanDetails.getTotalIntangibleFund());
                stmt.setDouble(5, loanDetails.getMonthlyCapitalInstallment());
                stmt.setDouble(6, loanDetails.getMonthlyInterestFee());
                stmt.setDouble(7, loanDetails.getMonthlyIntangibleFundFee());
//                stmt.setDouble(8, loanDetails.getMonthlyFeeValue());

                LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

// Convertir LocalDate a java.util.Date
                java.util.Date utilDate = Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

// Convertir java.util.Date a java.sql.Date
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

// Usar el objeto java.sql.Date en el PreparedStatement
                stmt.setDate(8, sqlDate);

                stmt.setString(9, "Pendiente");
                stmt.setInt(10, user);
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                stmt.addBatch(); // Agregar cada registro al batch

                currentDate = currentDate.plusMonths(1);

            }

            stmt.executeBatch(); // Ejecutar el batch
        }
    }

    public double calcularMontoPendientePorLoanId(Integer loanId) {
        double totalPendiente = 0.0;
        if (loanId == null) {
            return 0.0;
        }
        // Consulta SQL
        String query = "SELECT SUM(MonthlyFeeValue - IFNULL(payment, 0)) AS TotalPendingAmount "
                + "FROM loandetail "
                + "WHERE LoanID = ? AND State IN ('pendiente', 'parcial')";

        try {
            PreparedStatement stmt = connection.prepareStatement(query);

            // Configurar el parámetro LoanID
            stmt.setInt(1, loanId);

            // Ejecutar la consulta
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                totalPendiente = rs.getDouble("TotalPendingAmount");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalPendiente;
    }

    public LoanDetailResult getLoanDetailById(Integer id) throws SQLException {
        String sql = "SELECT loa.dues AS loanDues, loaDet.dues AS loandetailDues, loaDet.MonthlyFeeValue, loaDet.PaymentDate "
                + "FROM financialtracker1.loandetail loaDet "
                + "LEFT JOIN financialtracker1.loan loa ON loaDet.LoanID = loa.ID "
                + "WHERE loaDet.ID = ?";

        LoanDetailResult result = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id); // Asignar el parámetro ID

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = new LoanDetailResult();
                    result.setLoanDues(rs.getInt("loanDues"));
                    result.setLoandetailDues(rs.getInt("loandetailDues"));
                    result.setMonthlyFeeValue(rs.getDouble("MonthlyFeeValue"));
                    result.setPaymentDate(rs.getString("PaymentDate"));
                }
            }
        }

        return result;
    }

}
