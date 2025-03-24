/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

/**
 *
 * @author Jesus Gutierrez
 */
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonoDao {

    private final Connection connection;

    public AbonoDao(Connection connection) {
        this.connection = connection;
    }

    // Método para insertar un nuevo abono
    public boolean insertAbono(AbonoTb abono) throws SQLException {
        String sql = "INSERT INTO abono (SoliNum, service_concept_id, Employee_id, dues, monthly, paymentDate, " +
                "status, discount_from, createdBy, createdAt, modifiedBy, modifiedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, abono.getSoliNum());
            stmt.setString(2, abono.getServiceConceptId());
            stmt.setString(3, abono.getEmployeeId());
            stmt.setInt(4, abono.getDues());
            stmt.setBigDecimal(5, abono.getMonthly());
            stmt.setString(6, abono.getPaymentDate());
            stmt.setString(7, abono.getStatus());
            stmt.setString(8, abono.getDiscountFrom());
            stmt.setInt(9, abono.getCreatedBy());
            stmt.setString(10, abono.getCreatedAt());
            stmt.setInt(11, abono.getModifiedBy());
            stmt.setString(12, abono.getModifiedAt());
            return stmt.executeUpdate() > 0;
        }
    }

    // Método para actualizar un abono existente
    public boolean updateAbono(AbonoTb abono) throws SQLException {
        String sql = "UPDATE abono SET SoliNum = ?, service_concept_id = ?, Employee_id = ?, dues = ?, monthly = ?, " +
                "paymentDate = ?, status = ?, discount_from = ?, createdBy = ?, createdAt = ?, modifiedBy = ?, modifiedAt = ? " +
                "WHERE ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, abono.getSoliNum());
            stmt.setString(2, abono.getServiceConceptId());
            stmt.setString(3, abono.getEmployeeId());
            stmt.setInt(4, abono.getDues());
            stmt.setBigDecimal(5, abono.getMonthly());
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
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                abonos.add(mapResultSetToAbono(rs));
            }
        }
        return abonos;
    }

    // Método auxiliar para mapear un ResultSet a un objeto Abono
    private AbonoTb mapResultSetToAbono(ResultSet rs) throws SQLException {
        AbonoTb abono = new AbonoTb();
        abono.setId(rs.getInt("ID"));
        abono.setSoliNum(rs.getString("SoliNum"));
        abono.setServiceConceptId(rs.getString("service_concept_id"));
        abono.setEmployeeId(rs.getString("Employee_id"));
        abono.setDues(rs.getInt("dues"));
        abono.setMonthly(rs.getBigDecimal("monthly"));
        abono.setPaymentDate(rs.getString("paymentDate"));
        abono.setStatus(rs.getString("status"));
        abono.setDiscountFrom(rs.getString("discount_from"));
        abono.setCreatedBy(rs.getInt("createdBy"));
        abono.setCreatedAt(rs.getString("createdAt"));
        abono.setModifiedBy(rs.getInt("modifiedBy"));
        abono.setModifiedAt(rs.getString("modifiedAt"));
        return abono;
    }
}

