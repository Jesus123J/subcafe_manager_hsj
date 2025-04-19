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
import javax.swing.JOptionPane;

public class EmployeeDao {

    private final Connection connection;

    public EmployeeDao() {
        this.connection = Conexion.getConnection();
    }

    public boolean updateEmploymentStatusByDNI(String dni, String newStatus) {
        String sql = "UPDATE employees SET employment_status = ?, updated_at = CURRENT_TIMESTAMP WHERE national_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newStatus);
            preparedStatement.setString(2, dni);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployeeIfNotUsed(String dni) throws SQLException {
        String findEmployeeIdQuery = "SELECT employee_id FROM employees WHERE national_id = ?";
        String checkLoanUsageQuery = "SELECT COUNT(*) FROM loan WHERE EmployeeID = ?";
        String checkUserUsageQuery = "SELECT COUNT(*) FROM user se\n"
                + "LEFT JOIN employees em ON se.idEmployee = em.employee_id\n"
                + " WHERE em.national_id = ?";
        String checkAbonoUsageQuery = "SELECT COUNT(*) FROM abono WHERE Employee_id = ?";
        String deleteQuery = "DELETE FROM employees WHERE employee_id = ?";

        try (PreparedStatement stmtFindEmployeeId = connection.prepareStatement(findEmployeeIdQuery); PreparedStatement stmUser = connection.prepareStatement(checkUserUsageQuery); PreparedStatement stmtCheckLoanUsage = connection.prepareStatement(checkLoanUsageQuery); PreparedStatement stmtCheckAbonoUsage = connection.prepareStatement(checkAbonoUsageQuery); PreparedStatement stmtDelete = connection.prepareStatement(deleteQuery)) {

            // Paso 1: Obtener el ID del empleado basado en el DNI
            stmtFindEmployeeId.setString(1, dni);
            ResultSet rsEmployeeId = stmtFindEmployeeId.executeQuery();

            if (!rsEmployeeId.next()) {
                JOptionPane.showMessageDialog(null, "No se encontró el empleado con el DNI proporcionado.", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);

                return false;
            }

            int employeeId = rsEmployeeId.getInt("employee_id");

            // Paso 2: Verificar si tiene préstamos activos
            stmtCheckLoanUsage.setString(1, dni);
            ResultSet rsLoanUsage = stmtCheckLoanUsage.executeQuery();

            stmUser.setString(1, dni);
            ResultSet rsUser = stmUser.executeQuery();
            // Paso 3: Verificar si tiene bonos activos
            stmtCheckAbonoUsage.setInt(1, employeeId);
            ResultSet rsAbonoUsage = stmtCheckAbonoUsage.executeQuery();

            if (rsLoanUsage.next() && rsLoanUsage.getInt(1) == 0
                    && rsAbonoUsage.next() && rsAbonoUsage.getInt(1) == 0
                    && rsUser.next() && rsUser.getInt(1) == 0) {
                // Paso 4: Si no tiene préstamos ni bonos, eliminar al empleado
                stmtDelete.setInt(1, employeeId);
                return stmtDelete.executeUpdate() > 0;
            } else {
                JOptionPane.showMessageDialog(null, "No se puede eliminar, el empleado tiene préstamos o bonos registrados.", "GESTIÓN TRABAJADOR", JOptionPane.WARNING_MESSAGE);

                return false;
            }
        }
    }

    public List<EmployeeTb> getEmployeesByDateRange(Date fechaInicio, Date fechaFin) {
        List<EmployeeTb> listaEmpleados = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE start_date BETWEEN ? AND ? ORDER BY start_date ASC";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, fechaInicio);
            statement.setDate(2, fechaFin);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                listaEmpleados.add(mapResultSetToEmployee(resultSet));
            }
            
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null  , "❌ ERROR: No se pudo recuperar la lista de empleados.");
        }
        return listaEmpleados;
    }

    // Crear empleado
    public int create(EmployeeTb employee) throws SQLException {
        String sql = "INSERT INTO employees (fullName, national_id,"
                + "gender, employment_status, employment_status_code, start_date) "
                + "VALUES (?,?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, employee.getFullName().toUpperCase());
            stmt.setString(2, employee.getNationalId().toUpperCase());
            stmt.setString(3, employee.getGender() == null ? "" : employee.getGender());
            stmt.setString(4, employee.getEmploymentStatus().toUpperCase());
            stmt.setString(5, employee.getEmploymentStatusCode());
            stmt.setDate(6, Date.valueOf(employee.getStartDate()));

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

    public Optional<EmployeeTb> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEmployee(rs));
            }
            return Optional.empty();
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
        employee.setFullName(rs.getString("fullName"));
        employee.setNationalId(rs.getString("national_id"));
        employee.setGender(rs.getString("gender"));
        employee.setEmploymentStatus(rs.getString("employment_status"));
        employee.setEmploymentStatusCode(rs.getString("employment_status_code"));
        employee.setStartDate(rs.getDate("start_date").toLocalDate());
        employee.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        employee.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return employee;
    }

    public List<EmployeeTb> findEmployeesByFullName(String fullName) throws SQLException {
        String sql = "SELECT * FROM employees WHERE fullName LIKE ?";

        List<EmployeeTb> employees = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Agregar el parámetro con el valor del nombre completo y comodines
            stmt.setString(1, "%" + fullName.toUpperCase().trim() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Mapear el resultado al objeto EmployeeTb
                    EmployeeTb employee = new EmployeeTb();
                    employee.setEmployeeId(rs.getInt("employee_id"));
                    employee.setFullName(rs.getString("fullName"));
                    employee.setNationalId(rs.getString("national_id"));
                    employee.setGender(rs.getString("gender"));
                    employee.setEmploymentStatus(rs.getString("employment_status"));
                    employee.setEmploymentStatusCode(rs.getString("employment_status_code"));
                    employee.setStartDate(rs.getDate("start_date").toLocalDate());
                    employee.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    employee.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                    employees.add(employee);
                }
            }
        }

        return employees;
    }
}
