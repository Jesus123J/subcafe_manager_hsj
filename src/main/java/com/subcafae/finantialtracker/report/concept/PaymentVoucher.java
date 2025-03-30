/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.concept;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class PaymentVoucher {

    private final Connection conn = Conexion.getConnection();

    // Payment has to be made up front
    // Data
    private String numVoucher; // autocomplete the client
    private String numAccount; // the business
    private String numCheck;   // It depends on the client
    private String bank;  //  insert bank depends on the client
    private LocalDate dateEntry; // autoc
    private Double amount;
    private String details;

    //
    private String documentDni;  // the name client
    private String nameLastName; // lastName client

    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public PaymentVoucher() {

    }

    public PaymentVoucher(String numVoucher, String numAccount, String numCheck,
            String bank, Double amount, String details,
            String documentDni, String nameLastName, LocalDate dateEntry , Integer user) {
        this.numVoucher = numVoucher;
        this.numAccount = numAccount;
        this.numCheck = numCheck;
        this.bank = bank;
        this.amount = amount;
        this.details = details;
        this.documentDni = documentDni;
        this.nameLastName = nameLastName;
        this.dateEntry = dateEntry;
        this.userId = user;
    }

    public void imprintVoucher() {
        ReporteConcepto conceptReport = new ReporteConcepto();
        conceptReport.reporteConcepto(
                numVoucher,
                bank,
                numAccount,
                numCheck,
                nameLastName,
                amount,
                details,
                dateEntry
        );

    }
    private static final String PREFIX = "001";
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String INSERT_SQL
            = "INSERT INTO voucher (num_voucher, num_account, num_check, bank, "
            + "date_entry, amount, details, document_dni, name_lastname , userId) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?)";

    private final String UPDATE_SQL
            = "UPDATE voucher SET "
            + "num_account = ?, "
            + "num_check = ?, "
            + "bank = ?, "
            + "date_entry = ?, "
            + "amount = ?, "
            + "details = ?, "
            + "document_dni = ?, "
            + "name_lastname = ? "
            + "WHERE num_voucher = ?";

    public void generateVoucher() {

        try {
            String updateSQL = "UPDATE voucher_temp SET status = 'CONFIRMED' WHERE num_voucher = ?";
            PreparedStatement pstmtUpdate = conn.prepareStatement(updateSQL);
            pstmtUpdate.setString(1, getNumVoucher());
            pstmtUpdate.executeUpdate();

            conn.setAutoCommit(false);

            // Insertar en base de datos
            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            pstmt.setString(1, getNumVoucher());
            pstmt.setString(2, getNumAccount());
            pstmt.setString(3, getNumCheck());
            pstmt.setString(4, getBank());
            pstmt.setString(5, getDateEntry().toString());
            pstmt.setDouble(6, getAmount());
            pstmt.setString(7, getDetails());
            pstmt.setString(8, getDocumentDni());
            pstmt.setString(9, getNameLastName());
            pstmt.setString(10, getUserId().toString());
            int rowsAffected = pstmt.executeUpdate();
            conn.commit();

            if (rowsAffected > 0) {

                JOptionPane.showMessageDialog(null, "Registro de voucher guardado", "Inserción", JOptionPane.INFORMATION_MESSAGE);

                imprintVoucher();

            } else {
                JOptionPane.showMessageDialog(null, "Error de guardado", "Inserción", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {

        }
    }

    public static void cleanUnusedVouchers() {
        String deleteSQL = "DELETE FROM voucher_temp WHERE status = 'PENDING'";
        Connection conn = Conexion.getConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(deleteSQL);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Se eliminaron " + rowsDeleted + " vouchers no usados.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateAndReserveVoucher() {
        String getMaxSQL = "SELECT MAX(CAST(SUBSTRING(num_voucher, 5) AS UNSIGNED)) FROM voucher_temp";
        String insertSQL = "INSERT INTO voucher_temp (num_voucher) VALUES (?)";
        Connection conn = Conexion.getConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(getMaxSQL);
            int lastSequence = rs.next() ? rs.getInt(1) : 0;
            int newSequence = lastSequence + 1;

            String newVoucher = String.format("001-%06d", newSequence);

            PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, newVoucher);
            pstmt.executeUpdate();

            return newVoucher;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean updateVoucher() {
        try {
            PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL);
            
            pstmt.setString(1, getNumAccount());
            pstmt.setString(2, getNumCheck());
            pstmt.setString(3, getBank());
            pstmt.setString(4, getDateEntry().toString());
            pstmt.setDouble(5, getAmount());
            pstmt.setString(6, getDetails());
            pstmt.setString(7, getDocumentDni());
            pstmt.setString(8, getNameLastName());
            pstmt.setString(9, getNumVoucher());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<PaymentVoucher> list() {
        List<PaymentVoucher> listaVouchers = new ArrayList<>();
        String SELECT_SQL = "SELECT * FROM voucher";

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_SQL)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                PaymentVoucher voucher = new PaymentVoucher();

                voucher.setNumVoucher(rs.getString("num_voucher"));
                voucher.setNumAccount(rs.getString("num_account"));
                voucher.setNumCheck(rs.getString("num_check"));
                voucher.setBank(rs.getString("bank"));
                voucher.setDateEntry(LocalDate.parse(rs.getString("date_entry")));
                voucher.setAmount(rs.getDouble("amount"));
                voucher.setDetails(rs.getString("details"));
                voucher.setDocumentDni(rs.getString("document_dni"));
                voucher.setNameLastName(rs.getString("name_lastname"));

                listaVouchers.add(voucher);
            }
        } catch (SQLException e) {
            System.out.println("Error " + e.getMessage());
        }

        return listaVouchers;
    }

    public String getNumVoucher() {
        return numVoucher;
    }

    public void setNumVoucher(String numVoucher) {
        this.numVoucher = numVoucher;
    }

    public String getNumAccount() {
        return numAccount;
    }

    public void setNumAccount(String numAccount) {
        this.numAccount = numAccount;
    }

    public String getNumCheck() {
        return numCheck;
    }

    public void setNumCheck(String numCheck) {
        this.numCheck = numCheck;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public LocalDate getDateEntry() {
        return dateEntry;
    }

    public void setDateEntry(LocalDate dateEntry) {
        this.dateEntry = dateEntry;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDocumentDni() {
        return documentDni;
    }

    public void setDocumentDni(String documentDni) {
        this.documentDni = documentDni;
    }

    public String getNameLastName() {
        return nameLastName;
    }

    public void setNameLastName(String nameLastName) {
        this.nameLastName = nameLastName;
    }
}
