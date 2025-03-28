/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDao {

    private final Connection connection;

    public EmployeeDao() {
        this.connection = Conexion.getConnection();
    }

    // Crear empleado
    protected int create(EmployeeTb employee) throws SQLException {
        String sql = "INSERT INTO employees (first_name, last_name, national_id, phone_number, "
                + "gender, employment_status, employment_status_code, start_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getNationalId());
            stmt.setString(4, employee.getPhoneNumber());
            stmt.setString(5, employee.getGender());
            stmt.setString(6, employee.getEmploymentStatus());
            stmt.setString(7, employee.getEmploymentStatusCode());
            stmt.setDate(8, Date.valueOf(employee.getStartDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
        }
    }

    // Actualizar empleado
    protected boolean update(EmployeeTb employee) throws SQLException {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, phone_number = ?, "
                + "employment_status = ?, employment_status_code = ? WHERE employee_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getPhoneNumber());
            stmt.setString(4, employee.getEmploymentStatus());
            stmt.setString(5, employee.getEmploymentStatusCode());
            stmt.setInt(6, employee.getEmployeeId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Eliminar empleado
    protected boolean delete(int employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Buscar por ID
    public Optional<EmployeeTb> findById(String dni) throws SQLException {
        String sql = "SELECT * FROM employees WHERE national_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEmployee(rs));
            }
            return Optional.empty();
        }
    }

    // Obtener todos
    public List<EmployeeTb> findAll() throws SQLException {
        List<EmployeeTb> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }
        return employees;
    }

    private EmployeeTb mapResultSetToEmployee(ResultSet rs) throws SQLException {
        EmployeeTb employee = new EmployeeTb();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setNationalId(rs.getString("national_id"));
        employee.setPhoneNumber(rs.getString("phone_number"));
        employee.setGender(rs.getString("gender"));
        employee.setEmploymentStatus(rs.getString("employment_status"));
        employee.setEmploymentStatusCode(rs.getString("employment_status_code"));
        employee.setStartDate(rs.getDate("start_date").toLocalDate());
        employee.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        employee.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return employee;
    }
}
