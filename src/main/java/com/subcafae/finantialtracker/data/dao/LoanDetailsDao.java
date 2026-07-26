/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Cuotas de prestamo (loandetail). Fuente primaria: el backend REST
 * (/integracion/ft/prestamos-detalle). Fallback: el JDBC directo original
 * (metodos xxxDirecto) cuando el backend no esta corriendo.
 *
 * @author Jesus Gutierrez
 */
public class LoanDetailsDao extends EmployeeDao {

    private final Connection connection;

    private static final DateTimeFormatter FECHA_HORA_API =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor para inicializar la conexión
    public LoanDetailsDao() {
        this.connection = Conexion.getConnection();
    }

    public Boolean updatePaymentResponsibilityToGuarantor(String soli) {
        try {
            ApiBackend.put("/integracion/ft/prestamos/" + soli + "/responsabilidad-aval", null);
            JOptionPane.showMessageDialog(null, "Se cambio de estado, se paso todos las deudas para el aval");
            return true;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updatePaymentResponsibilityToGuarantorDirecto(soli);
    }

    private Boolean updatePaymentResponsibilityToGuarantorDirecto(String soli) {
        String sql = "UPDATE loan SET PaymentResponsibility = 'GUARANTOR', ModifiedAt = NOW() WHERE SoliNum = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, soli);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Se cambio de estado, se paso todos las deudas para el aval");
            return true;
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un error");
            return false;

        }
    }

    // Método para actualizar pagos parciales y validar si el LoanDetail debe cambiar a "Pagado"
    public void updateLoanStateByLoandetailId(Long loandetailId, double monthlyFeeValue, double newPayment) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("monthlyFeeValue", monthlyFeeValue);
            body.addProperty("newPayment", newPayment);
            ApiBackend.put("/integracion/ft/prestamos-detalle/" + loandetailId + "/pago", body);
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        updateLoanStateByLoandetailIdDirecto(loandetailId, monthlyFeeValue, newPayment);
    }

    private void updateLoanStateByLoandetailIdDirecto(Long loandetailId, double monthlyFeeValue, double newPayment) throws SQLException {
        String findLoanIdQuery = "SELECT LoanID, payment FROM loandetail WHERE ID = ?";
        String updateLoandetailStateQuery = "UPDATE loandetail SET payment = ?, State = ? WHERE ID = ?";
        String findLoandetailsStateQuery = "SELECT State FROM loandetail WHERE LoanID = ?";
        String updateLoanStateQuery = "UPDATE loan SET StateLoan = ? WHERE ID = ?";

        try (PreparedStatement stmtFindLoanId = connection.prepareStatement(findLoanIdQuery); PreparedStatement stmtUpdateLoandetail = connection.prepareStatement(updateLoandetailStateQuery); PreparedStatement stmtFindLoandetailsState = connection.prepareStatement(findLoandetailsStateQuery); PreparedStatement stmtUpdateLoan = connection.prepareStatement(updateLoanStateQuery)) {

            // Paso 1: Obtener LoanID y monto actual de pago en loandetail
            stmtFindLoanId.setLong(1, loandetailId);
            ResultSet rsLoanId = stmtFindLoanId.executeQuery();

            if (rsLoanId.next()) {

                int loanId = rsLoanId.getInt("LoanID");
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
                    if (!"Pagado".equals(rsLoandetailsState.getString("State"))) {
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

    // Método para buscar detalles de préstamos por LoanID
    public List<LoanDetailsTb> findLoanDetailsByLoanId(int loanId) throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos-detalle/por-prestamo?loanId=" + loanId);
            List<LoanDetailsTb> loanDetails = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                // el original no mapea TotalIntangibleFund en este metodo
                loanDetails.add(detalleDesdeJson(e.getAsJsonObject(), false));
            }
            return loanDetails;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findLoanDetailsByLoanIdDirecto(loanId);
    }

    private List<LoanDetailsTb> findLoanDetailsByLoanIdDirecto(int loanId) throws SQLException {

        String sql = "SELECT * FROM loandetail WHERE LoanID = ?;";

        List<LoanDetailsTb> loanDetails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, loanId); // Asigna el LoanID al parámetro

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear el resultado al objeto LoanDetailsTb
                    LoanDetailsTb detail = new LoanDetailsTb();
                    detail.setId(rs.getLong("ID"));
                    detail.setLoanId(rs.getInt("LoanID"));
                    detail.setDues(rs.getInt("Dues"));
                    detail.setTotalInterest(rs.getDouble("TotalInterest"));
                    detail.setMonthlyCapitalInstallment(rs.getDouble("MonthlyCapitalInstallment"));
                    detail.setMonthlyInterestFee(rs.getDouble("MonthlyInterestFee"));
                    detail.setMonthlyIntangibleFundFee(rs.getDouble("MonthlyIntangibleFundFee"));
                    detail.setMonthlyFeeValue(rs.getDouble("MonthlyFeeValue"));
                    detail.setPayment(rs.getDouble("payment"));
                    detail.setPaymentDate(rs.getDate("PaymentDate"));
                    detail.setState(rs.getString("State"));
                    detail.setCreatedBy(rs.getInt("CreatedBy"));
                    detail.setCreatedAt(rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null);
                    detail.setModifiedBy(rs.getInt("ModifiedBy"));
                    detail.setModifiedAt(rs.getTimestamp("ModifiedAt") != null ? rs.getTimestamp("ModifiedAt").toLocalDateTime() : null);

                    // Añadir el detalle del préstamo a la lista
                    loanDetails.add(detail);
                }
            }
        }

        return loanDetails;
    }

    //
    public List<LoanDetailsTb> getAllLoanDetails() throws SQLException {
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos-detalle");
            List<LoanDetailsTb> loanDetails = new ArrayList<>();
            for (JsonElement e : resp.getAsJsonArray("data")) {
                loanDetails.add(detalleDesdeJson(e.getAsJsonObject(), true));
            }
            return loanDetails;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllLoanDetailsDirecto();
    }

    private List<LoanDetailsTb> getAllLoanDetailsDirecto() throws SQLException {

        String sql = "SELECT ID, LoanID, Dues, TotalInterest, TotalIntangibleFund, MonthlyCapitalInstallment, "
                + "MonthlyInterestFee, MonthlyIntangibleFundFee, MonthlyFeeValue, payment, PaymentDate, State, "
                + "CreatedBy, CreatedAt, ModifiedBy, ModifiedAt FROM loandetail";

        List<LoanDetailsTb> loanDetails = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LoanDetailsTb loanDetail = new LoanDetailsTb();
                loanDetail.setId(rs.getLong("ID"));
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
        try {
            JsonObject body = new JsonObject();
            body.addProperty("loanId", loan.getId());
            body.addProperty("dues", loan.getDues());
            body.addProperty("paymentDate", loan.getPaymentDate().toString());
            body.addProperty("totalInterest", loanDetails.getTotalInterest());
            body.addProperty("totalIntangibleFund", loanDetails.getTotalIntangibleFund());
            body.addProperty("monthlyCapitalInstallment", loanDetails.getMonthlyCapitalInstallment());
            body.addProperty("monthlyInterestFee", loanDetails.getMonthlyInterestFee());
            body.addProperty("monthlyIntangibleFundFee", loanDetails.getMonthlyIntangibleFundFee());
            body.addProperty("monthlyFeeValue", loanDetails.getMonthlyFeeValue());
            body.addProperty("usuario", user);
            ApiBackend.post("/integracion/ft/prestamos-detalle", body);
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        insertMultipleLoanDetailsDirecto(loanDetails, loan, user);
    }

    private void insertMultipleLoanDetailsDirecto(LoanDetailsTb loanDetails, LoanTb loan, int user) throws SQLException {
        String insertSql = "INSERT INTO loandetail (LoanID, Dues, TotalInterest, TotalIntangibleFund, "
                + "MonthlyCapitalInstallment, MonthlyInterestFee, MonthlyIntangibleFundFee, MonthlyFeeValue,"
                + " PaymentDate, State, CreatedBy, CreatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ? , ?, ?, ?, ?, ?)";

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
                stmt.setDouble(8, loanDetails.getMonthlyFeeValue());
                stmt.setDate(9, sqlDate);

                stmt.setString(10, "Pendiente");
                stmt.setInt(11, user);
                stmt.setTimestamp(12, java.sql.Timestamp.valueOf(LocalDateTime.now()));

                stmt.addBatch(); // Agregar cada registro al batch

                currentDate = currentDate.plusMonths(1);

            }

            stmt.executeBatch(); // Ejecutar el batch
        }
    }

    public double calcularMontoPendientePorLoanId(Integer loanId) {
        if (loanId == null) {
            return 0.0;
        }
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos-detalle/monto-pendiente?loanId=" + loanId);
            return resp.getAsJsonObject("data").get("monto").getAsDouble();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return calcularMontoPendientePorLoanIdDirecto(loanId);
    }

    private double calcularMontoPendientePorLoanIdDirecto(Integer loanId) {
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
        try {
            JsonObject resp = ApiBackend.get("/integracion/ft/prestamos-detalle/" + id + "/historial");
            JsonObject o = resp.getAsJsonObject("data");
            LoanDetailResult result = new LoanDetailResult();
            result.setPayment(dobleDe(o, "payment"));
            result.setSoli(textoDe(o, "soliNum"));
            result.setLoanDues(enteroDe(o, "loanDues"));
            result.setLoandetailDues(enteroDe(o, "loandetailDues"));
            result.setMonthlyFeeValue(dobleDe(o, "monthlyFeeValue"));
            result.setPaymentDate(textoDe(o, "paymentDate"));
            return result;
        } catch (ApiBackend.NoEncontradoException e) {
            return null; // el backend confirmo que no existe el detalle
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getLoanDetailByIdDirecto(id);
    }

    private LoanDetailResult getLoanDetailByIdDirecto(Integer id) throws SQLException {
        String sql = "SELECT loa.SoliNum ,loaDet.payment ,  loa.dues AS loanDues, loaDet.dues AS loandetailDues, loaDet.MonthlyFeeValue AS MonthlyFeeValue , loaDet.PaymentDate "
                + "FROM financialtracker1.loandetail loaDet "
                + "LEFT JOIN financialtracker1.loan loa ON loaDet.LoanID = loa.ID "
                + "WHERE loaDet.ID = ?";

        LoanDetailResult result = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id); // Asignar el parámetro ID

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = new LoanDetailResult();
                    result.setPayment(rs.getDouble("payment"));
                    result.setSoli(rs.getString("SoliNum"));
                    result.setLoanDues(rs.getInt("loanDues"));
                    result.setLoandetailDues(rs.getInt("loandetailDues"));
                    result.setMonthlyFeeValue(rs.getDouble("MonthlyFeeValue"));
                    result.setPaymentDate(rs.getString("PaymentDate"));
                }
            }
        }

        return result;
    }

    // ─── Helpers JSON → entidad (mismos tipos que devolvia el ResultSet) ──

    private static String textoDe(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    /** rs.getInt: 0 cuando la columna es NULL. */
    private static int enteroDe(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0 : v.getAsInt();
    }

    /** rs.getLong: 0 cuando la columna es NULL. */
    private static long largoDe(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0L : v.getAsLong();
    }

    /** rs.getDouble: 0.0 cuando la columna es NULL. */
    private static double dobleDe(JsonObject o, String campo) {
        JsonElement v = o.get(campo);
        return v == null || v.isJsonNull() ? 0.0 : v.getAsDouble();
    }

    /** rs.getDate: java.sql.Date o null ("yyyy-MM-dd" desde el backend). */
    private static Date fechaSqlDe(JsonObject o, String campo) {
        String v = textoDe(o, campo);
        if (v == null) {
            return null;
        }
        return Date.valueOf(v.substring(0, Math.min(10, v.length())));
    }

    /** rs.getTimestamp(...).toLocalDateTime() o null. */
    private static LocalDateTime fechaHoraDe(JsonObject o, String campo) {
        String v = textoDe(o, campo);
        if (v == null) {
            return null;
        }
        if (v.length() == 10) {
            return LocalDate.parse(v).atStartOfDay();
        }
        return LocalDateTime.parse(v, FECHA_HORA_API);
    }

    /**
     * Replica el mapeo por ResultSet de los metodos originales. Con
     * conFondoIntangible=false NO setea TotalIntangibleFund, igual que
     * findLoanDetailsByLoanId.
     */
    private static LoanDetailsTb detalleDesdeJson(JsonObject o, boolean conFondoIntangible) {
        LoanDetailsTb detail = new LoanDetailsTb();
        detail.setId(largoDe(o, "id"));
        detail.setLoanId(enteroDe(o, "loanId"));
        detail.setDues(enteroDe(o, "dues"));
        detail.setTotalInterest(dobleDe(o, "totalInterest"));
        if (conFondoIntangible) {
            detail.setTotalIntangibleFund(dobleDe(o, "totalIntangibleFund"));
        }
        detail.setMonthlyCapitalInstallment(dobleDe(o, "monthlyCapitalInstallment"));
        detail.setMonthlyInterestFee(dobleDe(o, "monthlyInterestFee"));
        detail.setMonthlyIntangibleFundFee(dobleDe(o, "monthlyIntangibleFundFee"));
        detail.setMonthlyFeeValue(dobleDe(o, "monthlyFeeValue"));
        detail.setPayment(dobleDe(o, "payment"));
        detail.setPaymentDate(fechaSqlDe(o, "paymentDate"));
        detail.setState(textoDe(o, "state"));
        detail.setCreatedBy(enteroDe(o, "createdBy"));
        detail.setCreatedAt(fechaHoraDe(o, "createdAt"));
        detail.setModifiedBy(enteroDe(o, "modifiedBy"));
        detail.setModifiedAt(fechaHoraDe(o, "modifiedAt"));
        return detail;
    }

}
