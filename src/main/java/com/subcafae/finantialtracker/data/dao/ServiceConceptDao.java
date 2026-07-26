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

    // ═══ Backend primero, fallback a JDBC directo ══════════════════════

    public void insert(ServiceConceptTb entity) throws SQLException {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("id", entity.getId());
            body.addProperty("description", entity.getDescription());
            body.addProperty("salePrice", entity.getSalePrice());
            body.addProperty("costPrice", entity.getCostPrice());
            body.addProperty("priority", entity.getPriority());
            body.addProperty("unid", entity.getUnid());
            body.addProperty("priorityConcept", entity.getPriorityConcept());
            body.addProperty("createdBy", entity.getCreatedBy());
            body.addProperty("createdAt", entity.getCreatedAt());
            body.addProperty("modifiedBy", entity.getModifiedBy());
            body.addProperty("modifiedAt", entity.getModifiedAt());
            ApiBackend.post("/integracion/ft/conceptos", body);
            JOptionPane.showMessageDialog(null, "Se registro el Concepto", "GÉSTION DE BONO", JOptionPane.INFORMATION_MESSAGE);
            return;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        insertDirecto(entity);
    }

    public boolean deleteServiceConceptIfNotUsed(String codigo) throws SQLException {
        try {
            JsonObject data = ApiBackend
                    .delete("/integracion/ft/conceptos/codigo/" + codigo)
                    .getAsJsonObject("data");
            boolean borrado = data.get("borrado").getAsBoolean();
            if (!borrado) {
                JOptionPane.showMessageDialog(null, "No se puede eliminar, el servicio está referenciado en abono.", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
            }
            return borrado;
        } catch (ApiBackend.NoEncontradoException e) {
            JOptionPane.showMessageDialog(null, "No se encontró el código en service_concept.", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
            return false;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return deleteServiceConceptIfNotUsedDirecto(codigo);
    }

    public ServiceConceptTb findServiceConceptByCodigo(String codigo) {
        try {
            JsonObject data = ApiBackend
                    .get("/integracion/ft/conceptos/codigo/" + codigo)
                    .getAsJsonObject("data");
            return jsonToConcepto(data);
        } catch (ApiBackend.NoEncontradoException e) {
            // Si no se encuentra, imprime el mensaje (mismo aviso que el DAO original)
            JOptionPane.showMessageDialog(null, "No se encontró el concepto con el código proporcionado", "GENERAL", JOptionPane.INFORMATION_MESSAGE);
            return null;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return findServiceConceptByCodigoDirecto(codigo);
    }

    public List<ServiceConceptTb> getAllServiceConcepts() throws SQLException {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/conceptos/completos")
                    .getAsJsonArray("data");
            List<ServiceConceptTb> serviceConceptList = new ArrayList<>();
            for (JsonElement el : data) {
                serviceConceptList.add(jsonToConcepto(el.getAsJsonObject()));
            }
            return serviceConceptList;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllServiceConceptsDirecto();
    }

    // Metodo para obtener codigo y descripcion de conceptos para autocompletado
    // Formato: "codigo - descripcion"
    public List<String> getAllConceptDescriptions() {
        try {
            JsonArray data = ApiBackend
                    .get("/integracion/ft/conceptos/descripciones")
                    .getAsJsonArray("data");
            List<String> descriptions = new ArrayList<>();
            for (JsonElement el : data) {
                JsonObject fila = el.getAsJsonObject();
                String codigo = texto(fila, "codigo");
                String desc = texto(fila, "description");
                if (codigo != null && !codigo.isBlank() && desc != null && !desc.isBlank()) {
                    descriptions.add(codigo + " - " + desc);
                }
            }
            return descriptions;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllConceptDescriptionsDirecto();
    }

    // ─── Mapeo del JSON del backend a entidades ────────────────────────

    private static ServiceConceptTb jsonToConcepto(JsonObject o) {
        ServiceConceptTb serviceConcept = new ServiceConceptTb();
        serviceConcept.setId(entero(o, "id"));
        serviceConcept.setCodigo(texto(o, "codigo"));
        serviceConcept.setDescription(texto(o, "description"));
        serviceConcept.setSalePrice(decimal(o, "salePrice"));
        serviceConcept.setCostPrice(decimal(o, "costPrice"));
        serviceConcept.setPriority(entero(o, "priority"));
        serviceConcept.setUnid(entero(o, "unid"));
        serviceConcept.setPriorityConcept(texto(o, "priorityConcept"));
        serviceConcept.setCreatedBy(entero(o, "createdBy"));
        serviceConcept.setCreatedAt(texto(o, "createdAt"));
        serviceConcept.setModifiedBy(entero(o, "modifiedBy"));
        serviceConcept.setModifiedAt(texto(o, "modifiedAt"));
        return serviceConcept;
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

    private void insertDirecto(ServiceConceptTb entity) throws SQLException {
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

    private boolean deleteServiceConceptIfNotUsedDirecto(String codigo) throws SQLException {
        String findServiceConceptIdQuery = "SELECT ID FROM service_concept WHERE codigo = ?";
        String checkUsageQuery = "SELECT COUNT(*) FROM abono WHERE service_concept_id = ?";
        String deleteQuery = "DELETE FROM service_concept WHERE ID = ?";

        try (PreparedStatement stmtFindServiceId = connection.prepareStatement(findServiceConceptIdQuery); PreparedStatement stmtCheckUsage = connection.prepareStatement(checkUsageQuery); PreparedStatement stmtDelete = connection.prepareStatement(deleteQuery)) {

            // Paso 1: Obtener el ID del service_concept basado en el código
            stmtFindServiceId.setString(1, codigo);
            ResultSet rsServiceId = stmtFindServiceId.executeQuery();

            if (!rsServiceId.next()) {
                JOptionPane.showMessageDialog(null,"No se encontró el código en service_concept.", "REPORTE DE ABONO", JOptionPane.INFORMATION_MESSAGE);
                return false; // Retorna falso si el código no existe
            }

            int serviceConceptId = rsServiceId.getInt("ID");

            // Paso 2: Verificar si está siendo usado en la tabla abono
            stmtCheckUsage.setInt(1, serviceConceptId);
            ResultSet rsUsage = stmtCheckUsage.executeQuery();

            if (rsUsage.next() && rsUsage.getInt(1) == 0) {
                // Paso 3: Si no está referenciado, eliminar el registro
                stmtDelete.setInt(1, serviceConceptId);
                return stmtDelete.executeUpdate() > 0;
            } else {
                 JOptionPane.showMessageDialog(null, "No se puede eliminar, el servicio está referenciado en abono.", "REPORTE DE ABONO" , JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
    }

    private ServiceConceptTb findServiceConceptByCodigoDirecto(String codigo) {
        String sql = "SELECT * FROM service_concept WHERE codigo = ? ORDER BY ID ASC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo); // Asigna el parámetro de búsqueda

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ServiceConceptTb serviceConcept = new ServiceConceptTb();
                    serviceConcept.setId(rs.getInt("ID"));
                    serviceConcept.setCodigo(rs.getString("codigo")); // Ahora busca por código
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
                    JOptionPane.showMessageDialog(null, "No se encontró el concepto con el código proporcionado", "GENERAL", JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrió un problema al buscar el concepto", "GESTIÓN DE CONCEPTO", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private List<ServiceConceptTb> getAllServiceConceptsDirecto() throws SQLException {
        List<ServiceConceptTb> serviceConceptList = new ArrayList<>();
        String query = "SELECT * FROM service_concept";

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
                serviceConcept.setCodigo(rs.getString("codigo"));
                serviceConceptList.add(serviceConcept);
            }
        }
        return serviceConceptList;
    }

    private List<String> getAllConceptDescriptionsDirecto() {
        List<String> descriptions = new ArrayList<>();
        String sql = "SELECT codigo, description FROM service_concept ORDER BY codigo ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String codigo = rs.getString("codigo");
                String desc = rs.getString("description");
                if (codigo != null && !codigo.isBlank() && desc != null && !desc.isBlank()) {
                    descriptions.add(codigo + " - " + desc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return descriptions;
    }

}
