/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

/**
 *
 * @author Jesus Gutierrez
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import java.io.IOException;
import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class AbonoDao {

    private final Connection connection;

    public AbonoDao() {
        this.connection = Conexion.getConnection();
    }

    // ═══ Backend primero, fallback a JDBC directo ══════════════════════

    public void renounceAbono(String soliNum, Integer id, String fecha) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("modifiedBy", id);
            body.addProperty("modifiedAt", fecha);
            JsonObject data = ApiBackend
                    .put("/integracion/ft/abonos/soli/" + soliNum + "/renuncia", body)
                    .getAsJsonObject("data");
            if (data.get("renunciado").getAsBoolean()) {
                JOptionPane.showMessageDialog(null, "Se cambió el estado a Renunciado (REN)");
            } else {
                JOptionPane.showMessageDialog(null, "El número de solicitud ya se encuentra pagado");
            }
            return;
        } catch (ApiBackend.NoEncontradoException e) {
            JOptionPane.showMessageDialog(null, "No se encontró el número de solicitud buscado");
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        renounceAbonoDirecto(soliNum, id, fecha);
    }

    public boolean deleteAbonoIfNotUsed(String soliNum) throws SQLException {
        try {
            JsonObject data = ApiBackend
                    .delete("/integracion/ft/abonos/soli/" + soliNum)
                    .getAsJsonObject("data");
            boolean borrado = data.get("borrado").getAsBoolean();
            if (!borrado) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar, el abono tiene cuotas con estado 'Pagado' o 'Parcial'", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
            }
            return borrado;
        } catch (ApiBackend.NoEncontradoException e) {
            JOptionPane.showMessageDialog(null, "No se encontró el bono con el SoliNum proporcionado.", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
            return false;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return deleteAbonoIfNotUsedDirecto(soliNum);
    }

    public boolean hasPendingAbono(String id, String idConcep) {
        try {
            JsonObject data = ApiBackend
                    .get("/integracion/ft/abonos/pendiente?employeeId=" + id + "&conceptoId=" + idConcep)
                    .getAsJsonObject("data");
            return data.get("pendiente").getAsBoolean();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return hasPendingAbonoDirecto(id, idConcep);
    }

    // Método para insertar un nuevo abono
    public Integer insertAbono(AbonoTb abono) throws SQLException {
        try {
            return insertAbonoBackend(abono);
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return insertAbonoDirecto(abono);
    }

    private Integer insertAbonoBackend(AbonoTb abono) throws IOException, InterruptedException {
        if (hasPendingAbono(abono.getEmployeeId(), abono.getServiceConceptId())) {
            return -1; // No se permite la inserción
        }
        JsonObject body = new JsonObject();
        body.addProperty("soliNum", abono.getSoliNum());
        body.addProperty("serviceConceptId", abono.getServiceConceptId());
        body.addProperty("employeeId", abono.getEmployeeId());
        body.addProperty("dues", abono.getDues());
        body.addProperty("monthly", abono.getMonthly());
        body.addProperty("paymentDate", abono.getPaymentDate());
        body.addProperty("status", abono.getStatus());
        body.addProperty("discountFrom", abono.getDiscountFrom());
        body.addProperty("createdBy", abono.getCreatedBy());
        body.addProperty("createdAt", abono.getCreatedAt());
        body.addProperty("modifiedBy", abono.getModifiedBy());
        body.addProperty("modifiedAt", abono.getModifiedAt());
        if (abono.getLoteId() != null) {
            body.addProperty("loteId", abono.getLoteId());
        }
        JsonObject data = ApiBackend.post("/integracion/ft/abonos", body).getAsJsonObject("data");
        return data.get("id").getAsInt();
    }

    // Método para actualizar un abono existente
    public boolean updateAbono(AbonoTb abono) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("soliNum", abono.getSoliNum());
            body.addProperty("serviceConceptId", abono.getServiceConceptId());
            body.addProperty("employeeId", abono.getEmployeeId());
            body.addProperty("dues", abono.getDues());
            body.addProperty("monthly", abono.getMonthly());
            body.addProperty("paymentDate", abono.getPaymentDate());
            body.addProperty("status", abono.getStatus());
            body.addProperty("discountFrom", abono.getDiscountFrom());
            body.addProperty("createdBy", abono.getCreatedBy());
            body.addProperty("createdAt", abono.getCreatedAt());
            body.addProperty("modifiedBy", abono.getModifiedBy());
            body.addProperty("modifiedAt", abono.getModifiedAt());
            JsonObject data = ApiBackend
                    .put("/integracion/ft/abonos/" + abono.getId(), body)
                    .getAsJsonObject("data");
            return data.get("actualizado").getAsBoolean();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updateAbonoDirecto(abono);
    }

    // Método para eliminar un abono por su ID
    public boolean deleteAbono(int id) throws SQLException {
        try {
            JsonObject data = ApiBackend
                    .delete("/integracion/ft/abonos/" + id)
                    .getAsJsonObject("data");
            return data.get("borrado").getAsBoolean();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return deleteAbonoDirecto(id);
    }

    // Método para buscar un abono por su ID
    public AbonoTb findAbonoById(int id) throws SQLException {
        try {
            JsonObject data = ApiBackend
                    .get("/integracion/ft/abonos/" + id)
                    .getAsJsonObject("data");
            return jsonToAbono(data);
        } catch (ApiBackend.NoEncontradoException e) {
            return null; // el backend confirmo que el abono no existe
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findAbonoByIdDirecto(id);
    }

    // Método para listar todos los abonos
    public List<AbonoTb> findAllAbonos() throws SQLException {
        try {
            return jsonToAbonos(ApiBackend.get("/integracion/ft/abonos").getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findAllAbonosDirecto();
    }

    /**
     * Obtiene la lista de fechas (año/mes) con abonos por concepto y código
     * de empleado, en formato "Mes yyyy (n abonos)".
     */
    public List<String> getAvailableDates(Integer idConcept, String codeEm) throws SQLException {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/abonos/fechas-disponibles?conceptoId=" + idConcept
                            + "&codigoEmpleado=" + codeEm)
                    .getAsJsonArray("data");
            String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            List<String> fechas = new ArrayList<>();
            for (JsonElement el : data) {
                JsonObject fila = el.getAsJsonObject();
                int anio = entero(fila, "anio");
                int mes = entero(fila, "mes");
                int total = entero(fila, "total");
                fechas.add(meses[mes - 1] + " " + anio + " (" + total + " abonos)");
            }
            return fechas;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAvailableDatesDirecto(idConcept, codeEm);
    }

    public List<AbonoTb> getListAbonoTByConcepAndCodeEmRange(Integer idConcept, String codeEm, Date start, Date end) throws SQLException {
        try {
            return jsonToAbonos(ApiBackend
                    .get("/integracion/ft/abonos/por-concepto/rango?conceptoId=" + idConcept
                            + "&codigoEmpleado=" + codeEm
                            + "&inicio=" + start + "&fin=" + end)
                    .getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getListAbonoTByConcepAndCodeEmRangeDirecto(idConcept, codeEm, start, end);
    }

    public List<AbonoTb> getListAbonoTByConcepAndCodeEm(Integer idConcept, String codeEm, Date start) throws SQLException {
        try {
            String path = "/integracion/ft/abonos/por-concepto?conceptoId=" + idConcept
                    + "&codigoEmpleado=" + codeEm;
            if (start != null) {
                path += "&fecha=" + start;
            }
            return jsonToAbonos(ApiBackend.get(path).getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getListAbonoTByConcepAndCodeEmDirecto(idConcept, codeEm, start);
    }

    public List<AbonoTb> findAllAbonos(Date start, Date finaly) throws SQLException {
        try {
            return jsonToAbonos(ApiBackend
                    .get("/integracion/ft/abonos/rango?inicio=" + start + "&fin=" + finaly)
                    .getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findAllAbonosDirecto(start, finaly);
    }

    public List<AbonoDetailsTb> getListAbonoBySoli(String soli) throws SQLException {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/abonos/soli/" + soli.trim() + "/detalles")
                    .getAsJsonArray("data");
            List<AbonoDetailsTb> abonoDetails = new ArrayList<>();
            for (JsonElement el : data) {
                abonoDetails.add(jsonToDetalle(el.getAsJsonObject()));
            }
            return abonoDetails;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getListAbonoBySoliDirecto(soli);
    }

    public List<AbonoTb> findAbonosByEmployeeAndCurrentYear(String employeeId) throws SQLException {
        try {
            return jsonToAbonos(ApiBackend
                    .get("/integracion/ft/abonos/pendientes/" + employeeId)
                    .getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findAbonosByEmployeeAndCurrentYearDirecto(employeeId);
    }

    // Metodo para obtener los ultimos N abonos sin filtro de fecha
    public List<AbonoTb> getLastAbonos(int limit) throws SQLException {
        try {
            return jsonToAbonos(ApiBackend
                    .get("/integracion/ft/abonos/ultimos?limite=" + limit)
                    .getAsJsonArray("data"));
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getLastAbonosDirecto(limit);
    }

    // Metodo para obtener todos los numeros de solicitud de abonos
    public List<String> getAllSoliNums() {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/abonos/solinums")
                    .getAsJsonArray("data");
            List<String> soliNums = new ArrayList<>();
            for (JsonElement el : data) {
                if (!el.isJsonNull()) {
                    soliNums.add(el.getAsString());
                }
            }
            return soliNums;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllSoliNumsDirecto();
    }

    // ─── Mapeo del JSON del backend a entidades ────────────────────────

    private static List<AbonoTb> jsonToAbonos(JsonArray data) {
        List<AbonoTb> abonos = new ArrayList<>();
        for (JsonElement el : data) {
            abonos.add(jsonToAbono(el.getAsJsonObject()));
        }
        return abonos;
    }

    private static AbonoTb jsonToAbono(JsonObject o) {
        AbonoTb abono = new AbonoTb();
        abono.setId(entero(o, "id"));
        abono.setSoliNum(texto(o, "soliNum"));
        abono.setServiceConceptId(texto(o, "serviceConceptId"));
        abono.setEmployeeId(texto(o, "employeeId"));
        abono.setDues(entero(o, "dues"));
        abono.setMonthly(decimal(o, "monthly"));
        abono.setPaymentDate(texto(o, "paymentDate"));
        abono.setStatus(texto(o, "status"));
        abono.setDiscountFrom(texto(o, "discountFrom"));
        abono.setCreatedBy(entero(o, "createdBy"));
        abono.setCreatedAt(texto(o, "createdAt"));
        abono.setModifiedBy(entero(o, "modifiedBy"));
        abono.setModifiedAt(texto(o, "modifiedAt"));
        // lote_id no se mapea, igual que mapResultSetToAbono original
        return abono;
    }

    private static AbonoDetailsTb jsonToDetalle(JsonObject o) {
        AbonoDetailsTb detalle = new AbonoDetailsTb();
        JsonElement id = o.get("id");
        detalle.setId(id == null || id.isJsonNull() ? 0L : id.getAsLong());
        detalle.setAbonoID(entero(o, "abonoId"));
        detalle.setDues(entero(o, "dues"));
        detalle.setMonthly(decimal(o, "monthly"));
        detalle.setPayment(decimal(o, "payment"));
        detalle.setPaymentDate(texto(o, "paymentDate"));
        detalle.setState(texto(o, "state"));
        detalle.setCreatedBy(texto(o, "createdBy"));
        detalle.setCreatedAt(texto(o, "createdAt"));
        detalle.setModifiedBy(texto(o, "modifiedBy"));
        detalle.setModifiedAt(texto(o, "modifiedAt"));
        return detalle;
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

    private void renounceAbonoDirecto(String soliNum, Integer id, String fecha) throws SQLException {
        String updateQuery = "UPDATE abono SET status = 'REN', modifiedBy = ?, modifiedAt = ? WHERE SoliNum = ? AND status = 'Pendiente'";
        String statusQuery = "SELECT * FROM abono WHERE SoliNum = ?";

        try (PreparedStatement stmtUpdate = connection.prepareStatement(updateQuery); PreparedStatement rsQuery = connection.prepareStatement(statusQuery)) {

            // Verificar si existe la solicitud
            rsQuery.setString(1, soliNum);
            ResultSet rs = rsQuery.executeQuery();

            if (rs.next()) {  // Si hay resultados
                stmtUpdate.setInt(1, id);
                stmtUpdate.setString(2, fecha);
                stmtUpdate.setString(3, soliNum);

                if (stmtUpdate.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Se cambió el estado a Renunciado (REN)");
                } else {
                    JOptionPane.showMessageDialog(null, "El número de solicitud ya se encuentra pagado");
                }
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el número de solicitud buscado");
            }
        }
    }

    private boolean deleteAbonoIfNotUsedDirecto(String soliNum) throws SQLException {
        String findAbonoIdQuery = "SELECT ID FROM abono WHERE SoliNum = ?";
        String checkUsageQuery = "SELECT COUNT(*) FROM abonodetail WHERE AbonoID = ? AND state IN ('Pagado', 'Parcial')";
        String deleteQuery = "DELETE FROM abono WHERE ID = ?";

        try (PreparedStatement stmtFindAbonoId = connection.prepareStatement(findAbonoIdQuery); PreparedStatement stmtCheckUsage = connection.prepareStatement(checkUsageQuery); PreparedStatement stmtDelete = connection.prepareStatement(deleteQuery)) {

            // Paso 1: Obtener el ID del abono basado en el SoliNum
            stmtFindAbonoId.setString(1, soliNum);
            ResultSet rsAbonoId = stmtFindAbonoId.executeQuery();

            if (!rsAbonoId.next()) {
                JOptionPane.showMessageDialog(null, "No se encontró el bono con el SoliNum proporcionado.", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            int abonoId = rsAbonoId.getInt("ID");

            // Paso 2: Verificar si hay cuotas con estado "Pagado" o "Parcial"
            stmtCheckUsage.setInt(1, abonoId);
            ResultSet rsUsage = stmtCheckUsage.executeQuery();

            if (rsUsage.next() && rsUsage.getInt(1) == 0) {
                // Paso 3: Si no hay cuotas con "Pagado" o "Parcial", eliminar el abono
                stmtDelete.setInt(1, abonoId);
                return stmtDelete.executeUpdate() > 0;
            } else {
                JOptionPane.showMessageDialog(null, "No se puede eliminar, el abono tiene cuotas con estado 'Pagado' o 'Parcial'", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
    }

    private void updateSoliNum(int loanId) throws SQLException {
        String soliNum = String.format("%08d", loanId);
        String updateSql = "UPDATE abono SET SoliNum = ? WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, soliNum);
            stmt.setInt(2, loanId);
            stmt.executeUpdate();
        }
    }

    private boolean hasPendingAbonoDirecto(String id, String idConcep) {
        String sql = "SELECT COUNT(*) FROM abono WHERE Employee_id = ? AND status = 'Pendiente' AND service_concept_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, idConcep);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Si hay registros con estado "Pendiente", retorna true
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Si no hay registros "Pendiente" o hay un error, retorna false
    }

    private Integer insertAbonoDirecto(AbonoTb abono) throws SQLException {

        if (hasPendingAbonoDirecto(abono.getEmployeeId(), abono.getServiceConceptId())) {
            return -1; // No se permite la inserción
        }

        String sql = "INSERT INTO abono (SoliNum, service_concept_id, Employee_id, dues, monthly, paymentDate, "
                + "status, discount_from, createdBy, createdAt, modifiedBy, modifiedAt, lote_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, abono.getSoliNum());
            stmt.setString(2, abono.getServiceConceptId());
            stmt.setString(3, abono.getEmployeeId());
            stmt.setInt(4, abono.getDues());
            stmt.setDouble(5, abono.getMonthly());
            stmt.setString(6, abono.getPaymentDate());
            stmt.setString(7, abono.getStatus());
            stmt.setString(8, abono.getDiscountFrom());
            stmt.setInt(9, abono.getCreatedBy());
            stmt.setString(10, abono.getCreatedAt());
            stmt.setInt(11, abono.getModifiedBy());
            stmt.setString(12, abono.getModifiedAt());
            if (abono.getLoteId() != null) {
                stmt.setInt(13, abono.getLoteId());
            } else {
                stmt.setNull(13, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                Integer newId = null;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1);
                        updateSoliNum(newId);
                    } else {
                        throw new SQLException("No se pudo obtener la clave primaria generada.");
                    }
                }

                return newId;
            } else {
                return null;
            }
        }
    }

    private boolean updateAbonoDirecto(AbonoTb abono) throws SQLException {
        String sql = "UPDATE abono SET SoliNum = ?, service_concept_id = ?, Employee_id = ?, dues = ?, monthly = ?, "
                + "paymentDate = ?, status = ?, discount_from = ?, createdBy = ?, createdAt = ?, modifiedBy = ?, modifiedAt = ? "
                + "WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, abono.getSoliNum());
            stmt.setString(2, abono.getServiceConceptId());
            stmt.setString(3, abono.getEmployeeId());
            stmt.setInt(4, abono.getDues());
            stmt.setDouble(5, abono.getMonthly());
            stmt.setString(6, abono.getPaymentDate());
            stmt.setString(7, abono.getStatus());
            stmt.setString(8, abono.getDiscountFrom());
            stmt.setInt(9, abono.getCreatedBy());
            stmt.setString(10, abono.getCreatedAt());
            stmt.setInt(11, abono.getModifiedBy());
            stmt.setString(12, abono.getModifiedAt());
            stmt.setInt(13, abono.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean deleteAbonoDirecto(int id) throws SQLException {
        String sql = "DELETE FROM abono WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private AbonoTb findAbonoByIdDirecto(int id) throws SQLException {
        String sql = "SELECT * FROM abono WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAbono(rs);
            }
        }
        return null;
    }

    private List<AbonoTb> findAllAbonosDirecto() throws SQLException {
        String sql = "SELECT * FROM abono";
        List<AbonoTb> abonos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                abonos.add(mapResultSetToAbono(rs));
            }
        }
        return abonos;
    }

    private List<String> getAvailableDatesDirecto(Integer idConcept, String codeEm) throws SQLException {
        List<String> fechas = new ArrayList<>();
        String sql = "SELECT YEAR(ab.CreatedAt) AS anio, MONTH(ab.CreatedAt) AS mes, COUNT(*) AS total "
                + "FROM financialtracker1.abono ab "
                + "LEFT JOIN financialtracker1.employees em ON em.employee_id = ab.Employee_id "
                + "WHERE ab.service_concept_id = ? "
                + "AND em.employment_status_code = ? "
                + "GROUP BY YEAR(ab.CreatedAt), MONTH(ab.CreatedAt) "
                + "ORDER BY anio DESC, mes DESC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, idConcept);
        stmt.setString(2, codeEm);
        ResultSet rs = stmt.executeQuery();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        while (rs.next()) {
            int anio = rs.getInt("anio");
            int mes = rs.getInt("mes");
            int total = rs.getInt("total");
            fechas.add(meses[mes - 1] + " " + anio + " (" + total + " abonos)");
        }
        return fechas;
    }

    private List<AbonoTb> getListAbonoTByConcepAndCodeEmRangeDirecto(Integer idConcept, String codeEm, Date start, Date end) throws SQLException {
        List<AbonoTb> abonos = new ArrayList<>();
        String sql = "SELECT ab.* FROM financialtracker1.abono ab "
                + "LEFT JOIN financialtracker1.employees em ON em.employee_id = ab.Employee_id "
                + "WHERE ab.service_concept_id = ? "
                + "AND em.employment_status_code = ? "
                + "AND DATE(ab.CreatedAt) BETWEEN ? AND ? "
                + "ORDER BY ab.CreatedAt ASC";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, idConcept);
        stmt.setString(2, codeEm);
        stmt.setDate(3, start);
        stmt.setDate(4, end);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            abonos.add(mapResultSetToAbono(rs));
        }
        return abonos;
    }

    private List<AbonoTb> getListAbonoTByConcepAndCodeEmDirecto(Integer idConcept, String codeEm, Date start) throws SQLException {
        List<AbonoTb> abonos = new ArrayList<>();
        String sql;
        PreparedStatement stmt;

        if (start != null) {
            // Con filtro de fecha: filtra por año y mes de creación
            sql = "SELECT ab.* FROM financialtracker1.abono ab "
                    + "LEFT JOIN financialtracker1.employees em ON em.employee_id = ab.Employee_id "
                    + "WHERE ab.service_concept_id = ? "
                    + "AND em.employment_status_code = ? "
                    + "AND YEAR(ab.CreatedAt) = YEAR(?) "
                    + "AND MONTH(ab.CreatedAt) = MONTH(?) "
                    + "ORDER BY ab.CreatedAt ASC";

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idConcept);
            stmt.setString(2, codeEm);
            stmt.setDate(3, start);
            stmt.setDate(4, start);
        } else {
            // Sin filtro de fecha: trae todos los abonos del concepto y tipo de empleado
            sql = "SELECT ab.* FROM financialtracker1.abono ab "
                    + "LEFT JOIN financialtracker1.employees em ON em.employee_id = ab.Employee_id "
                    + "WHERE ab.service_concept_id = ? "
                    + "AND em.employment_status_code = ? "
                    + "ORDER BY ab.CreatedAt ASC";

            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idConcept);
            stmt.setString(2, codeEm);
        }

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            abonos.add(mapResultSetToAbono(rs));
        }
        return abonos;
    }

    private List<AbonoTb> findAllAbonosDirecto(Date start, Date finaly) throws SQLException {
        String sql = "SELECT * FROM abono WHERE DATE(CreatedAt) BETWEEN ? AND ? ORDER BY CreatedAt ASC";

        List<AbonoTb> abonos = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setDate(1, start);
            stmt.setDate(2, finaly);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                abonos.add(mapResultSetToAbono(rs));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "GÉSTION ABONOS", JOptionPane.OK_OPTION);
            return abonos;
        }
        return abonos;
    }

    private List<AbonoDetailsTb> getListAbonoBySoliDirecto(String soli) throws SQLException {
        String sql = "SELECT abDe.* FROM financialtracker1.abonodetail abDe "
                + "LEFT JOIN financialtracker1.abono bon ON abDe.AbonoID = bon.ID "
                + "WHERE bon.SoliNum = ?;";

        List<AbonoDetailsTb> abonoDetails = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, soli.trim()); // Asigna el parámetro correctamente

            try (ResultSet rs = stmt.executeQuery()) { // Llamada correcta sin pasar el SQL nuevamente
                while (rs.next()) {

                    // Mapeo de los datos obtenidos
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
        }

        return abonoDetails;
    }

    // Método auxiliar para mapear un ResultSet a un objeto Abono
    private AbonoTb mapResultSetToAbono(ResultSet rs) throws SQLException {
        AbonoTb abono = new AbonoTb();
        abono.setId(rs.getInt("ID"));
        abono.setSoliNum(rs.getString("SoliNum"));
        abono.setServiceConceptId(rs.getString("service_concept_id"));
        abono.setEmployeeId(rs.getString("Employee_id"));
        abono.setDues(rs.getInt("dues"));
        abono.setMonthly(rs.getDouble("monthly"));
        abono.setPaymentDate(rs.getString("paymentDate"));
        abono.setStatus(rs.getString("status"));
        abono.setDiscountFrom(rs.getString("discount_from"));
        abono.setCreatedBy(rs.getInt("createdBy"));
        abono.setCreatedAt(rs.getString("createdAt"));
        abono.setModifiedBy(rs.getInt("modifiedBy"));
        abono.setModifiedAt(rs.getString("modifiedAt"));
        return abono;
    }

    private List<AbonoTb> findAbonosByEmployeeAndCurrentYearDirecto(String employeeId) throws SQLException {

        String sql = "SELECT * FROM abono WHERE Employee_id = ? AND status = 'Pendiente' ";

        List<AbonoTb> abonos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employeeId); // Asigna el ID del empleado

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear el resultado al objeto AbonoTb
                    AbonoTb abono = new AbonoTb();
                    abono.setId(rs.getInt("ID"));
                    abono.setSoliNum(rs.getString("SoliNum"));
                    abono.setServiceConceptId(rs.getString("service_concept_id"));
                    abono.setEmployeeId(rs.getString("Employee_id"));
                    abono.setDues(rs.getInt("dues"));
                    abono.setMonthly(rs.getDouble("monthly"));
                    abono.setPaymentDate(rs.getString("paymentDate"));
                    abono.setStatus(rs.getString("status"));
                    abono.setDiscountFrom(rs.getString("discount_from"));
                    abono.setCreatedBy(rs.getInt("createdBy"));
                    abono.setCreatedAt(rs.getString("createdAt"));
                    abono.setModifiedBy(rs.getInt("modifiedBy"));
                    abono.setModifiedAt(rs.getString("modifiedAt"));

                    // Añadir el abono a la lista
                    abonos.add(abono);
                }
            }
        }

        return abonos;
    }

    private List<AbonoTb> getLastAbonosDirecto(int limit) throws SQLException {
        String sql = "SELECT * FROM abono ORDER BY ID DESC LIMIT ?";
        List<AbonoTb> abonos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                abonos.add(mapResultSetToAbono(rs));
            }
        }
        return abonos;
    }

    private List<String> getAllSoliNumsDirecto() {
        List<String> soliNums = new ArrayList<>();
        String sql = "SELECT SoliNum FROM abono WHERE SoliNum IS NOT NULL ORDER BY ID DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String soliNum = rs.getString("SoliNum");
                if (soliNum != null && !soliNum.isBlank()) {
                    soliNums.add(soliNum);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soliNums;
    }
}
