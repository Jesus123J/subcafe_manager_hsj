/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.User;
import com.subcafae.finantialtracker.data.entity.UserTb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Jesus Gutierrez
 */
public class UserDao {

    private final Connection connection;

    public UserDao() {
        this.connection = Conexion.getConnection();
    }

    public boolean toggleUserState(String username) {
        String sqlSelect = "SELECT state FROM user WHERE username = ?";
        String sqlUpdate = "UPDATE user SET state = ? WHERE username = ?";

        try (PreparedStatement selectStmt = connection.prepareStatement(sqlSelect)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int currentState = rs.getInt("state");
                if (currentState == 9) {
                    JOptionPane.showMessageDialog(null, "No se puede bloquiar a una cuenta super administrador");
                    return false;
                }
                int newState = (currentState == 1) ? 0 : 1; // Cambia automático: 1 → 0, 0 → 1

                try (PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate)) {
                    updateStmt.setInt(1, newState);
                    updateStmt.setString(2, username);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return true;
                    } else {
                        JOptionPane.showMessageDialog(null, "No se logro cambiar de estado");
                        return false;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró usuario");
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un error", "MENSAGE", JOptionPane.WARNING_MESSAGE);
            // e.printStackTrace();
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UserTb getUserByUsername(String username, String password) {
        String sql = "SELECT u.iduser, u.username, u.password, u.idEmployee,u.rol ,u.state,"
                + "e.fullName, e.national_id, "
                + "e.gender, e.employment_status, e.employment_status_code, "
                + "e.start_date, e.created_at, e.updated_at "
                + "FROM user u "
                + "JOIN employees e ON u.idEmployee = e.employee_id "
                + "WHERE u.username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedPassword = rs.getString("password");

                // Retornar el objeto User si la contraseña coincide
                if (BCrypt.checkpw(password, storedPassword)) {

                    return new UserTb(
                            rs.getInt("iduser"),
                            rs.getString("username"),
                            rs.getInt("idEmployee"),
                            rs.getString("fullName"),
                            rs.getString("rol"),
                            rs.getString("state"),
                            rs.getString("national_id"),
                            rs.getString("gender"),
                            rs.getString("employment_status"),
                            rs.getString("employment_status_code"),
                            rs.getDate("start_date"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un error", "MENSAGE", JOptionPane.WARNING_MESSAGE);
            // e.printStackTrace();
        }
        return null;
    }

    public boolean createUser(String username, String password, int idEmployee, String state) {

        if (isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(null, "El nombre de usuario ya está en uso.", "MENSAGE", JOptionPane.WARNING_MESSAGE);
            //System.out.println("El nombre de usuario ya está en uso.");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO user (username, password, idEmployee , rol , state) VALUES (?, ?, ? , ? ,? )";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setInt(3, idEmployee);
            stmt.setString(4, state);
            stmt.setString(5, "1");

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un error", "MENSAGE", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();
        String sql = "SELECT u.iduser, u.username, u.password, "
                + "e.fullName AS employee_name, "
                + "u.rol, u.state "
                + "FROM user u "
                + "JOIN employees e ON u.idEmployee = e.employee_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("iduser"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("employee_name"), // Nombre del empleado en vez de ID
                        rs.getString("rol"),
                        rs.getString("state")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un error", "MENSAGE", JOptionPane.WARNING_MESSAGE);
        }

        System.out.println("Imprimir -> " + users.size());
        return users;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        // Verificar que no sea el super administrador
        String sqlCheckState = "SELECT state FROM user WHERE username = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(sqlCheckState)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("state") == 9) {
                JOptionPane.showMessageDialog(null, "No se puede modificar la contraseña del super administrador", "GESTIÓN DE USUARIOS", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            return false;
        }

        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String sql = "UPDATE user SET password = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al actualizar la contraseña", "GESTIÓN DE USUARIOS", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateUserRole(String username, String newRole) {
        // Verificar que no sea el super administrador
        String sqlCheckState = "SELECT state FROM user WHERE username = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(sqlCheckState)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("state") == 9) {
                JOptionPane.showMessageDialog(null, "No se puede modificar el rol del super administrador", "GESTIÓN DE USUARIOS", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            return false;
        }

        String sql = "UPDATE user SET rol = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al actualizar el rol", "GESTIÓN DE USUARIOS", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

}
