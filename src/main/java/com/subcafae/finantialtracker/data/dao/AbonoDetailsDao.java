/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
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

    // ═══ Backend primero, fallback a JDBC directo ══════════════════════

    // Método para actualizar pagos parciales y validar si el LoanDetail debe cambiar a "Pagado"
    public void updateLoanStateByLoandetailId(Long loandetailId, double monthlyFeeValue, double newPayment) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("monthly", monthlyFeeValue);
            body.addProperty("payment", newPayment);
            ApiBackend.put("/integracion/ft/abonos-detalle/" + loandetailId + "/pago", body);
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        updateLoanStateByLoandetailIdDirecto(loandetailId, monthlyFeeValue, newPayment);
    }

    public List<AbonoDetailsTb> getAllAbonoDetails() throws SQLException {
        try {
            JsonArray data = ApiBackend.get("/integracion/ft/abonos-detalle").getAsJsonArray("data");
            List<AbonoDetailsTb> abonoDetails = new ArrayList<>();
            for (JsonElement el : data) {
                abonoDetails.add(jsonToDetalle(el.getAsJsonObject()));
            }
            return abonoDetails;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllAbonoDetailsDirecto();
    }

    public void insertAbonoDetail(AbonoTb abono, int user) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("abonoId", abono.getId());
            body.addProperty("dues", abono.getDues());
            body.addProperty("monthly", abono.getMonthly());
            body.addProperty("paymentDate", abono.getPaymentDate());
            body.addProperty("usuario", user);
            ApiBackend.post("/integracion/ft/abonos-detalle", body);
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        insertAbonoDetailDirecto(abono, user);
    }

    public List<AbonoDetailResult> getAbonoDetailById(Integer id) throws SQLException {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/abonos-detalle/" + id + "/historial")
                    .getAsJsonArray("data");
            List<AbonoDetailResult> results = new ArrayList<>();
            for (JsonElement el : data) {
                JsonObject fila = el.getAsJsonObject();
                AbonoDetailResult result = new AbonoDetailResult();
                result.setPayment(decimal(fila, "payment"));
                result.setPaymentDate(texto(fila, "paymentDate"));
                result.setDescription(texto(fila, "description"));
                result.setAbonoDues(entero(fila, "abonoDues"));
                result.setAbonodetailDues(entero(fila, "abonodetailDues"));
                result.setMonthly(decimal(fila, "monthly"));
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAbonoDetailByIdDirecto(id);
    }

    // Método para buscar abonodetail por AbonoID
    public List<AbonoDetailsTb> findAbonoDetailsByAbonoId(int abonoId) throws SQLException {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/abonos-detalle/por-abono/" + abonoId)
                    .getAsJsonArray("data");
            List<AbonoDetailsTb> abonoDetails = new ArrayList<>();
            for (JsonElement el : data) {
                abonoDetails.add(jsonToDetalle(el.getAsJsonObject()));
            }
            return abonoDetails;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findAbonoDetailsByAbonoIdDirecto(abonoId);
    }

    // ─── Mapeo del JSON del backend a entidades ────────────────────────

    private static AbonoDetailsTb jsonToDetalle(JsonObject o) {
        AbonoDetailsTb abonoDetail = new AbonoDetailsTb();
        JsonElement id = o.get("id");
        abonoDetail.setId(id == null || id.isJsonNull() ? 0L : id.getAsLong());
        abonoDetail.setAbonoID(entero(o, "abonoId"));
        abonoDetail.setDues(entero(o, "dues"));
        abonoDetail.setMonthly(decimal(o, "monthly"));
        abonoDetail.setPayment(decimal(o, "payment"));
        abonoDetail.setPaymentDate(texto(o, "paymentDate"));
        abonoDetail.setState(texto(o, "state"));
        abonoDetail.setCreatedBy(texto(o, "createdBy"));
        abonoDetail.setCreatedAt(texto(o, "createdAt"));
        abonoDetail.setModifiedBy(texto(o, "modifiedBy"));
        abonoDetail.setModifiedAt(texto(o, "modifiedAt"));
        return abonoDetail;
    }

    private static String texto(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    private static int entero(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0 : v.getAsInt();
    }

    private static double decimal(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0.0 : v.getAsDouble();
    }

    // ═══ Fallback: JDBC directo (logica original, NO borrar) ═══════════

    private void updateLoanStateByLoandetailIdDirecto(Long loandetailId, double monthlyFeeValue, double newPayment) throws SQLException {

        String findLoanIdQuery = "SELECT AbonoID , payment FROM abonodetail WHERE id = ?";
        String updateLoandetailStateQuery = "UPDATE abonodetail SET payment = ?, state = ? WHERE id = ?";
        String findLoandetailsStateQuery = "SELECT state FROM abonodetail WHERE AbonoID = ?";
        String updateLoanStateQuery = "UPDATE abono SET status = ? WHERE ID = ?";

        try (PreparedStatement stmtFindLoanId = connection.prepareStatement(findLoanIdQuery); PreparedStatement stmtUpdateLoandetail = connection.prepareStatement(updateLoandetailStateQuery); PreparedStatement stmtFindLoandetailsState = connection.prepareStatement(findLoandetailsStateQuery); PreparedStatement stmtUpdateLoan = connection.prepareStatement(updateLoanStateQuery)) {

            // Paso 1: Obtener LoanID y monto actual de pago en loandetail
            stmtFindLoanId.setLong(1, loandetailId);
            ResultSet rsLoanId = stmtFindLoanId.executeQuery();

            if (rsLoanId.next()) {

                System.out.println("Entrandi");
                int loanId = rsLoanId.getInt("AbonoID");
                double currentPayment = rsLoanId.getDouble("payment");

                // Paso 2: Sumar el nuevo pago al total de pagos acumulados
                double totalPayment = currentPayment + newPayment;

                // Determinar el nuevo estado según el pago total
                String loandetailState = totalPayment == monthlyFeeValue ? "Pagado" : "Parcial";

                // Actualizar loandetail con el nuevo monto acumulado y estado
                stmtUpdateLoandetail.setDouble(1, totalPayment);
                stmtUpdateLoandetail.setString(2, loandetailState);
                stmtUpdateLoandetail.setLong(3, loandetailId);
                stmtUpdateLoandetail.executeUpdate();

                // Paso 3: Verificar si **todas** las cuotas (`loandetail`) están pagadas
                stmtFindLoandetailsState.setInt(1, loanId);
                ResultSet rsLoandetailsState = stmtFindLoandetailsState.executeQuery();

                boolean allPaid = true;

                while (rsLoandetailsState.next()) {
                    if (!"Pagado".equals(rsLoandetailsState.getString("state"))) {
                        allPaid = false;
                        break;
                    }
                }

                // Paso 4: Si todas las cuotas están pagadas, cambiar `StateLoan` a "Pagado"
                if (allPaid) {
                    stmtUpdateLoan.setString(1, "Pagado");
                    stmtUpdateLoan.setInt(2, loanId);
                    stmtUpdateLoan.executeUpdate();
                }
            }
        }
    }

    private List<AbonoDetailsTb> getAllAbonoDetailsDirecto() throws SQLException {
        String sql = "SELECT id, AbonoID, dues, monthly, payment, paymentDate, state, createdBy, createdAt, modifiedBy, modifiedAt FROM abonodetail";
        List<AbonoDetailsTb> abonoDetails = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AbonoDetailsTb abonoDetail = new AbonoDetailsTb();
                abonoDetail.setId(rs.getLong("id"));
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

    private void insertAbonoDetailDirecto(AbonoTb abono, int user) throws SQLException {
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

    private List<AbonoDetailResult> getAbonoDetailByIdDirecto(Integer id) throws SQLException {
        String sql = "SELECT serv.description AS description, abDet.payment ,  ab.dues AS abonoDues, abDet.dues AS abonodetailDues, "
                + "abDet.monthly AS monthly, abDet.paymentDate "
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
                    result.setPayment(rs.getDouble("payment"));
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

    private List<AbonoDetailsTb> findAbonoDetailsByAbonoIdDirecto(int abonoId) throws SQLException {
        String sql = "SELECT * FROM abonodetail WHERE AbonoID = ?";

        List<AbonoDetailsTb> abonoDetails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, abonoId); // Asigna el AbonoID al parámetro

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear el resultado al objeto AbonoDetailTb
                    AbonoDetailsTb detail = new AbonoDetailsTb();
                    detail.setId(rs.getLong("id"));
                    detail.setAbonoID(rs.getInt("AbonoID"));
                    detail.setDues(rs.getInt("dues"));
                    detail.setMonthly(rs.getDouble("monthly"));
                    detail.setPayment(rs.getDouble("payment"));
                    detail.setPaymentDate(rs.getString("paymentDate"));
                    detail.setState(rs.getString("state"));
                    detail.setCreatedBy(rs.getString("createdBy"));
                    detail.setCreatedAt(rs.getString("createdAt"));
                    detail.setModifiedBy(rs.getString("modifiedBy"));
                    detail.setModifiedAt(rs.getString("modifiedAt"));

                    // Añadir el detalle del abono a la lista
                    abonoDetails.add(detail);
                }
            }
        }

        return abonoDetails;
    }

}
