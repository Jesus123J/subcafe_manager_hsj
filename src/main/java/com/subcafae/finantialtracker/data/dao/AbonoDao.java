/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

/**
 *
 * @author Jesus Gutierrez
 */
import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
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

    public boolean renounceAbono(String soliNum, Integer id, String fecha) throws SQLException {
        String updateQuery = "UPDATE abono SET status = 'REN' , modifiedBy  = ? ,modifiedAt = ?  WHERE SoliNum = ? AND state = 'Pendiente'";

        try (PreparedStatement stmtUpdate = connection.prepareStatement(updateQuery)) {
            stmtUpdate.setString(1, soliNum);
            stmtUpdate.setInt(2, id);
            stmtUpdate.setString(3, fecha);

            return stmtUpdate.executeUpdate() > 0;
        }
    }

    public boolean deleteAbonoIfNotUsed(String soliNum) throws SQLException {
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

    public boolean hasPendingAbono(String id, String idConcep) {
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

    // Método para insertar un nuevo abono
    // Método para insertar un nuevo abono
    public Integer insertAbono(AbonoTb abono) throws SQLException {

        if (hasPendingAbono(abono.getEmployeeId() , abono.getServiceConceptId())) {  
            return -1; // No se permite la inserción
        }

        String sql = "INSERT INTO abono (SoliNum, service_concept_id, Employee_id, dues, monthly, paymentDate, "
                + "status, discount_from, createdBy, createdAt, modifiedBy, modifiedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

    // Método para actualizar un abono existente
    public boolean updateAbono(AbonoTb abono) throws SQLException {
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

    // Método para eliminar un abono por su ID
    public boolean deleteAbono(int id) throws SQLException {
        String sql = "DELETE FROM abono WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Método para buscar un abono por su ID
    public AbonoTb findAbonoById(int id) throws SQLException {
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

    // Método para listar todos los abonos
    public List<AbonoTb> findAllAbonos() throws SQLException {
        String sql = "SELECT * FROM abono";
        List<AbonoTb> abonos = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                abonos.add(mapResultSetToAbono(rs));
            }
        }
        return abonos;
    }

    public List<AbonoTb> getListAbonoTByConcepAndCodeEm(Integer idConcept, String codeEm) throws SQLException {
        String sql = "SELECT  ab.* FROM financialtracker1.abono ab "
                + "LEFT JOIN financialtracker1.employees em ON  em.employee_id = ab.Employee_id "
                + "  WHERE ab.service_concept_id = ?  AND  em.employment_status_code = ?";

        List<AbonoTb> abonos = new ArrayList<>();

        PreparedStatement stmt = connection.prepareStatement(sql);

        stmt.setInt(1, idConcept);
        stmt.setString(2, codeEm);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            abonos.add(mapResultSetToAbono(rs));
        }
        return abonos;
    }

    public List<AbonoDetailsTb> getListAbonoBySoli(String soli) throws SQLException {
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

    public List<AbonoTb> findAbonosByEmployeeAndCurrentYear(String employeeId) throws SQLException {

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
}
