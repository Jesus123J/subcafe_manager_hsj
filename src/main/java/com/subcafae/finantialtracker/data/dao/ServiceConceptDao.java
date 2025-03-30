/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.ServiceConceptTb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class ServiceConceptDao {

    private Connection connection;

    public ServiceConceptDao() {
        this.connection = Conexion.getConnection();
    }

    public void insert(ServiceConceptTb entity) throws SQLException {
        String query = "INSERT INTO service_concept (ID, description, sale_price, cost_price, priority, unid ,"
                + "priority_concept, createdBy, createdAt, modifiedBy, modifiedAt) "
                + "VALUES (?, ?, ?, ?, ? , ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, entity.getId());
            stmt.setString(2, entity.getDescription());
            stmt.setDouble(3, entity.getSalePrice());
            stmt.setDouble(4, entity.getCostPrice());
            stmt.setInt(5, entity.getPriority());
            stmt.setInt(6, entity.getUnid());
            stmt.setString(7, entity.getPriorityConcept());
            stmt.setInt(8, entity.getCreatedBy());
            stmt.setString(9, entity.getCreatedAt());
            stmt.setInt(10, entity.getModifiedBy());
            stmt.setString(11, entity.getModifiedAt());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Se registro el Concepto", "GÉSTION DE BONO", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public ServiceConceptTb findServiceConceptByDescription(String description) {
        String sql = "SELECT * FROM service_concept WHERE description = ? ORDER BY ID ASC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, description); // Asigna el parámetro de búsqueda

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    ServiceConceptTb serviceConcept = new ServiceConceptTb();
                    serviceConcept.setId(rs.getInt("ID"));
                    serviceConcept.setDescription(rs.getString("description"));
                    serviceConcept.setSalePrice(rs.getDouble("sale_price"));
                    serviceConcept.setCostPrice(rs.getDouble("cost_price"));
                    serviceConcept.setPriority(rs.getInt("priority"));
                    serviceConcept.setUnid(rs.getInt("unid"));
                    serviceConcept.setPriorityConcept(rs.getString("priority_concept"));
                    serviceConcept.setCreatedBy(rs.getInt("createdBy"));
                    serviceConcept.setCreatedAt(rs.getString("createdAt"));
                    serviceConcept.setModifiedBy(rs.getInt("modifiedBy"));
                    serviceConcept.setModifiedAt(rs.getString("modifiedAt"));
                    return serviceConcept;

                } else {
                    // Si no se encuentra, imprime el mensaje
                    JOptionPane.showMessageDialog(null, "No se encontro nombre del concepto", "GENERAL", JOptionPane.INFORMATION_MESSAGE);

                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
        }
        return null;
    }

    public List<ServiceConceptTb> getAllServiceConcepts() throws SQLException {
        List<ServiceConceptTb> serviceConceptList = new ArrayList<>();
        String query = "SELECT ID, description, sale_price, cost_price, priority, unid, priority_concept, "
                + "createdBy, createdAt, modifiedBy, modifiedAt FROM service_concept";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ServiceConceptTb serviceConcept = new ServiceConceptTb();
                serviceConcept.setId(rs.getInt("ID"));
                serviceConcept.setDescription(rs.getString("description"));
                serviceConcept.setSalePrice(rs.getDouble("sale_price"));
                serviceConcept.setCostPrice(rs.getDouble("cost_price"));
                serviceConcept.setPriority(rs.getInt("priority"));
                serviceConcept.setUnid(rs.getInt("unid"));
                serviceConcept.setPriorityConcept(rs.getString("priority_concept"));
                serviceConcept.setCreatedBy(rs.getInt("createdBy"));
                serviceConcept.setCreatedAt(rs.getString("createdAt"));
                serviceConcept.setModifiedBy(rs.getInt("modifiedBy"));
                serviceConcept.setModifiedAt(rs.getString("modifiedAt"));

                serviceConceptList.add(serviceConcept);
            }
        }
        return serviceConceptList;
    }

}
