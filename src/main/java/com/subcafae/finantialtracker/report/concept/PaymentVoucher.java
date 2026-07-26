/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.concept;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
import com.subcafae.finantialtracker.data.conexion.Conexion;
import java.io.IOException;
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

    // Conexion perezosa: solo se abre si algun metodo JDBC la necesita.
    // Asi construir PaymentVoucher con datos que vienen del backend REST
    // no dispara una conexion directa a la BD.
    private Connection conn;

    private Connection conn() {
        if (conn == null) {
            conn = Conexion.getConnection();
        }
        return conn;
    }

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

    /**
     * Guarda el voucher: primero via backend (POST /integracion/ft/vouchers,
     * que confirma voucher_temp e inserta voucher en una transaccion
     * server-side); si el backend no responde, fallback al JDBC original.
     * La UI (JOptionPane + impresion) se comporta igual en ambos caminos.
     */
    public void generateVoucher() {
        Integer rowsAffected = null;
        try {
            rowsAffected = generarVoucherBackend();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }

        if (rowsAffected == null) {
            generateVoucherDirecto();
            return;
        }

        if (rowsAffected > 0) {
            JOptionPane.showMessageDialog(null, "Registro de voucher guardado", "Inserción", JOptionPane.INFORMATION_MESSAGE);
            imprintVoucher();
        } else {
            JOptionPane.showMessageDialog(null, "Error de guardado", "Inserción", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** POST al backend; devuelve las filas afectadas por el INSERT. */
    private Integer generarVoucherBackend() throws IOException, InterruptedException {
        JsonObject resp = ApiBackend.post("/integracion/ft/vouchers", jsonVoucher());
        return resp.getAsJsonObject("data").get("filasAfectadas").getAsInt();
    }

    /** Body JSON compartido por crear (POST) y actualizar (PUT). */
    private JsonObject jsonVoucher() {
        JsonObject body = new JsonObject();
        body.addProperty("numVoucher", getNumVoucher());
        body.addProperty("numAccount", getNumAccount());
        body.addProperty("numCheck", getNumCheck());
        body.addProperty("bank", getBank());
        body.addProperty("dateEntry", getDateEntry() == null ? null : getDateEntry().toString());
        body.addProperty("amount", getAmount());
        body.addProperty("details", getDetails());
        body.addProperty("documentDni", getDocumentDni());
        body.addProperty("nameLastname", getNameLastName());
        body.addProperty("userId", getUserId());
        return body;
    }

    // Fallback: logica JDBC original, sin cambios.
    private void generateVoucherDirecto() {

        try {
            String updateSQL = "UPDATE voucher_temp SET status = 'CONFIRMED' WHERE num_voucher = ?";
            PreparedStatement pstmtUpdate = conn().prepareStatement(updateSQL);
            pstmtUpdate.setString(1, getNumVoucher());
            pstmtUpdate.executeUpdate();

            conn().setAutoCommit(false);

            // Insertar en base de datos
            PreparedStatement pstmt = conn().prepareStatement(INSERT_SQL);
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
            conn().commit();

            if (rowsAffected > 0) {

                JOptionPane.showMessageDialog(null, "Registro de voucher guardado", "Inserción", JOptionPane.INFORMATION_MESSAGE);

                imprintVoucher();

            } else {
                JOptionPane.showMessageDialog(null, "Error de guardado", "Inserción", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {

        }
    }

    /**
     * Limpia reservas PENDING: primero via backend
     * (DELETE /integracion/ft/vouchers/temp-pendientes), fallback JDBC.
     */
    public static void cleanUnusedVouchers() {
        try {
            JsonObject resp = ApiBackend.delete("/integracion/ft/vouchers/temp-pendientes");
            int rowsDeleted = resp.getAsJsonObject("data").get("eliminados").getAsInt();
            System.out.println("Se eliminaron " + rowsDeleted + " vouchers no usados.");
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        cleanUnusedVouchersDirecto();
    }

    // Fallback: logica JDBC original, sin cambios.
    private static void cleanUnusedVouchersDirecto() {
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

    /**
     * Reserva el siguiente correlativo: primero via backend
     * (POST /integracion/ft/vouchers/reservar), fallback JDBC.
     */
    public static String generateAndReserveVoucher() {
        try {
            JsonObject resp = ApiBackend.post("/integracion/ft/vouchers/reservar", null);
            return resp.getAsJsonObject("data").get("numVoucher").getAsString();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return generateAndReserveVoucherDirecto();
    }

    // Fallback: logica JDBC original, sin cambios.
    private static String generateAndReserveVoucherDirecto() {
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

    /**
     * Actualiza el voucher: primero via backend
     * (PUT /integracion/ft/vouchers/{numVoucher}), fallback JDBC.
     */
    public boolean updateVoucher() {
        try {
            JsonObject resp = ApiBackend.put(
                    "/integracion/ft/vouchers/" + getNumVoucher(), jsonVoucher());
            return resp.get("success").getAsBoolean();
        } catch (ApiBackend.NoEncontradoException e) {
            return false; // el backend confirmo que el voucher no existe
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updateVoucherDirecto();
    }

    // Fallback: logica JDBC original, sin cambios.
    private boolean updateVoucherDirecto() {
        try {
            PreparedStatement pstmt = conn().prepareStatement(UPDATE_SQL);
            
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

    /**
     * Lista vouchers: primero via backend (GET /integracion/ft/vouchers,
     * mismo endpoint que usa ModelMain.cargarVouchersBackendODao), fallback
     * al SELECT directo original.
     */
    public List<PaymentVoucher> list() {
        try {
            return listBackend();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return listDirecto();
    }

    private List<PaymentVoucher> listBackend() throws IOException, InterruptedException {
        JsonObject resp = ApiBackend.get("/integracion/ft/vouchers?limite=500");
        List<PaymentVoucher> lista = new ArrayList<>();
        for (JsonElement el : resp.getAsJsonArray("data")) {
            JsonObject o = el.getAsJsonObject();
            PaymentVoucher voucher = new PaymentVoucher();
            voucher.setNumVoucher(textoJson(o, "numVoucher"));
            voucher.setNumAccount(textoJson(o, "numCuenta"));
            voucher.setNumCheck(textoJson(o, "numCheque"));
            voucher.setBank(textoJson(o, "banco"));
            String fecha = textoJson(o, "fecha");
            if (fecha != null && fecha.length() >= 10) {
                try {
                    voucher.setDateEntry(LocalDate.parse(fecha.substring(0, 10)));
                } catch (Exception ignored) {
                }
            }
            JsonElement monto = o.get("monto");
            if (monto != null && !monto.isJsonNull()) {
                voucher.setAmount(monto.getAsDouble());
            }
            voucher.setDetails(textoJson(o, "detalle"));
            voucher.setDocumentDni(textoJson(o, "dni"));
            voucher.setNameLastName(textoJson(o, "beneficiario"));
            lista.add(voucher);
        }
        return lista;
    }

    private static String textoJson(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    // Fallback: logica JDBC original, sin cambios.
    private List<PaymentVoucher> listDirecto() {
        List<PaymentVoucher> listaVouchers = new ArrayList<>();
        String SELECT_SQL = "SELECT * FROM voucher";

        try (PreparedStatement pstmt = conn().prepareStatement(SELECT_SQL)) {
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
