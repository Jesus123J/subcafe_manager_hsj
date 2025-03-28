/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.report.HistoryPayment.AbonoDetailResult;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class AbonoDetailsDao {

    private Connection connection;

    public AbonoDetailsDao() {
        this.connection = Conexion.getConnection();
    }

   

    public List<AbonoDetailsTb> getAllAbonoDetails() throws SQLException {
        String sql = "SELECT id, AbonoID, dues, monthly, payment, paymentDate, state, createdBy, createdAt, modifiedBy, modifiedAt FROM abonodetail";
        List<AbonoDetailsTb> abonoDetails = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AbonoDetailsTb abonoDetail = new AbonoDetailsTb();
                abonoDetail.setId(rs.getInt("id"));
                abonoDetail.setAbonoID(rs.getInt("AbonoID"));
                abonoDetail.setDues(rs.getInt("dues"));
                abonoDetail.setMonthly(rs.getDouble("monthly"));
                abonoDetail.setPayment(rs.getDouble("payment"));
                abonoDetail.setPaymentDate(rs.getString("paymentDate"));
                abonoDetail.setState(rs.getString("state"));
                abonoDetail.setCreatedBy(rs.getString("createdBy"));
                abonoDetail.setCreatedAt(rs.getString("createdAt"));
                abonoDetail.setModifiedBy(rs.getString("modifiedBy"));
                abonoDetail.setModifiedAt(rs.getString("modifiedAt"));

                abonoDetails.add(abonoDetail);
            }
        }

        return abonoDetails;
    }

    public void insertAbonoDetail(AbonoTb abono, int user) throws SQLException {
        String sql = "INSERT INTO abonodetail (AbonoID, dues, monthly, payment,paymentDate, state, createdBy, createdAt, modifiedBy, modifiedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate curremtDate = LocalDate.parse(abono.getPaymentDate(), formatter);

            for (int i = 0; i < abono.getDues(); i++) {
                stmt.setInt(1, abono.getId());
                stmt.setInt(2, i + 1);
                stmt.setDouble(3, abono.getMonthly());

                stmt.setDouble(4, 0.0);

                LocalDate lastDayOfMonth = curremtDate.withDayOfMonth(curremtDate.lengthOfMonth());

// Convertir LocalDate a java.util.Date
                java.util.Date utilDate = Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

// Convertir java.util.Date a java.sql.Date
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

// Usar el objeto java.sql.Date en el PreparedStatement
                stmt.setString(5, sqlDate.toString());
                stmt.setString(6, "Pendiente");

                stmt.setString(7, String.valueOf(user));
                stmt.setString(8, LocalDate.now().toString());
                stmt.setString(9, "");
                stmt.setString(10, "");

                stmt.addBatch();

                curremtDate = curremtDate.plusMonths(1);
            }

            stmt.executeBatch();

        }
    }

    public List<AbonoDetailResult> getAbonoDetailById(Integer id) throws SQLException {
        String sql = "SELECT serv.description AS description, ab.dues AS abonoDues, abDet.dues AS abonodetailDues, abDet.monthly ,  abDet.paymentDate"
                + "FROM financialtracker1.abonodetail abDet "
                + "LEFT JOIN financialtracker1.abono ab ON ab.ID = abDet.AbonoID "
                + "LEFT JOIN financialtracker1.service_concept serv ON serv.ID = ab.service_concept_id "
                + "WHERE abDet.id = ?";

        List<AbonoDetailResult> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id); // Asignar el parámetro id

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AbonoDetailResult result = new AbonoDetailResult();
                    result.setPaymentDate(rs.getString("paymentDate"));
                    result.setDescription(rs.getString("description"));
                    result.setAbonoDues(rs.getInt("abonoDues"));
                    result.setAbonodetailDues(rs.getInt("abonodetailDues"));
                    result.setMonthly(rs.getDouble("monthly"));

                    results.add(result);
                }
            }
        }

        return results;
    }
}
