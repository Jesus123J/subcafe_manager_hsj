/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.User;
import com.subcafae.finantialtracker.data.entity.UserTb;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Gestion de usuarios (login incluido).
 *
 * Fuente primaria: el backend REST (rutas /integracion/ft/usuarios/**,
 * espejo exacto de este DAO; la contrasena se verifica/hashea alla con
 * BCrypt de Spring Security, compatible con los hashes de jbcrypt).
 * Fallback: JDBC directo (metodos xxxDirecto()), para que la app siga
 * funcionando cuando el backend no esta corriendo.
 *
 * @author Jesus Gutierrez
 */
public class UserDao {

    private static final String BASE = "/integracion/ft/usuarios";

    private final Connection connection;

    public UserDao() {
        this.connection = Conexion.getConnection();
    }

    public boolean toggleUserState(String username) {
        try {
            JsonObject data = ApiBackend
                    .put(BASE + "/" + encodar(username) + "/toggle-estado", null)
                    .getAsJsonObject("data");
            boolean cambiado = data.get("cambiado").getAsBoolean();
            if (!cambiado) {
                // Mismos dialogos que la version JDBC original
                String motivo = textoDe(data, "motivo");
                if ("SUPER_ADMIN".equals(motivo)) {
                    JOptionPane.showMessageDialog(null, "No se puede bloquiar a una cuenta super administrador");
                } else if ("ADMINISTRADOR".equals(motivo)) {
                    JOptionPane.showMessageDialog(null, "NO SE PUEDE BLOQUEAR NI EDITAR A UN ADMINISTRADOR");
                } else if ("NO_ENCONTRADO".equals(motivo)) {
                    JOptionPane.showMessageDialog(null, "No se encontró usuario");
                } else {
                    JOptionPane.showMessageDialog(null, "No se logro cambiar de estado");
                }
            }
            return cambiado;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return toggleUserStateDirecto(username);
    }

    private boolean toggleUserStateDirecto(String username) {
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
        try {
            JsonObject data = ApiBackend.get(BASE + "/" + encodar(username) + "/existe")
                    .getAsJsonObject("data");
            return data.get("existe").getAsBoolean();
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return isUsernameTakenDirecto(username);
    }

    private boolean isUsernameTakenDirecto(String username) {
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
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", username);
            body.addProperty("password", password);
            JsonObject data = ApiBackend.post(BASE + "/login", body).getAsJsonObject("data");
            if (!data.get("autenticado").getAsBoolean()) {
                // Igual que el original: null tanto si el usuario no existe
                // como si la contrasena no coincide.
                return null;
            }
            JsonObject u = data.getAsJsonObject("usuario");
            return new UserTb(
                    u.get("iduser").getAsInt(),
                    textoDe(u, "username"),
                    u.get("idEmployee").getAsInt(),
                    textoDe(u, "fullName"),
                    textoDe(u, "rol"),
                    textoDe(u, "state"),
                    textoDe(u, "national_id"),
                    textoDe(u, "gender"),
                    textoDe(u, "employment_status"),
                    textoDe(u, "employment_status_code"),
                    fechaSql(textoDe(u, "start_date")),
                    timestampSql(textoDe(u, "created_at")),
                    timestampSql(textoDe(u, "updated_at"))
            );
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getUserByUsernameDirecto(username, password);
    }

    private UserTb getUserByUsernameDirecto(String username, String password) {
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
        try {
            JsonObject body = new JsonObject();
            body.addProperty("username", username);
            body.addProperty("password", password);
            body.addProperty("idEmployee", idEmployee);
            // OJO: el parametro "state" de este metodo siempre fue el ROL
            // (el INSERT original lo mete en la columna rol y deja state='1').
            body.addProperty("rol", state);
            JsonObject data = ApiBackend.post(BASE, body).getAsJsonObject("data");
            boolean creado = data.get("creado").getAsBoolean();
            if (!creado && "USERNAME_EN_USO".equals(textoDe(data, "motivo"))) {
                JOptionPane.showMessageDialog(null, "El nombre de usuario ya está en uso.", "MENSAGE", JOptionPane.WARNING_MESSAGE);
            }
            return creado;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return createUserDirecto(username, password, idEmployee, state);
    }

    private boolean createUserDirecto(String username, String password, int idEmployee, String state) {

        if (isUsernameTakenDirecto(username)) {
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
        try {
            List<User> users = new ArrayList<>();
            JsonObject resp = ApiBackend.get(BASE);
            for (JsonElement elem : resp.get("data").getAsJsonArray()) {
                JsonObject fila = elem.getAsJsonObject();
                users.add(new User(
                        fila.get("iduser").getAsInt(),
                        textoDe(fila, "username"),
                        textoDe(fila, "password"),
                        textoDe(fila, "employee_name"), // Nombre del empleado en vez de ID
                        textoDe(fila, "rol"),
                        textoDe(fila, "state")
                ));
            }
            System.out.println("Imprimir -> " + users.size());
            return users;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return getAllUsersDirecto();
    }

    private List<User> getAllUsersDirecto() {

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
        try {
            JsonObject body = new JsonObject();
            body.addProperty("password", newPassword);
            JsonObject data = ApiBackend
                    .put(BASE + "/" + encodar(username) + "/password", body)
                    .getAsJsonObject("data");
            boolean actualizado = data.get("actualizado").getAsBoolean();
            if (!actualizado && "SUPER_ADMIN".equals(textoDe(data, "motivo"))) {
                JOptionPane.showMessageDialog(null, "No se puede modificar la contraseña del super administrador", "GESTIÓN DE USUARIOS", JOptionPane.WARNING_MESSAGE);
            }
            return actualizado;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updateUserPasswordDirecto(username, newPassword);
    }

    private boolean updateUserPasswordDirecto(String username, String newPassword) {
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
        try {
            JsonObject body = new JsonObject();
            body.addProperty("rol", newRole);
            JsonObject data = ApiBackend
                    .put(BASE + "/" + encodar(username) + "/rol", body)
                    .getAsJsonObject("data");
            boolean actualizado = data.get("actualizado").getAsBoolean();
            if (!actualizado && "SUPER_ADMIN".equals(textoDe(data, "motivo"))) {
                JOptionPane.showMessageDialog(null, "No se puede modificar el rol del super administrador", "GESTIÓN DE USUARIOS", JOptionPane.WARNING_MESSAGE);
            }
            return actualizado;
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return updateUserRoleDirecto(username, newRole);
    }

    private boolean updateUserRoleDirecto(String username, String newRole) {
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

    // ─── Helpers para el camino backend ────────────────────────────────

    /** Codifica un valor para usarlo en el path de la API. */
    private static String encodar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String textoDe(JsonObject obj, String campo) {
        JsonElement v = obj.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    /** "yyyy-MM-dd" del backend a java.sql.Date (null-safe). */
    private static Date fechaSql(String valor) {
        return valor == null ? null : Date.valueOf(valor);
    }

    /** "yyyy-MM-dd HH:mm:ss" del backend a java.sql.Timestamp (null-safe). */
    private static Timestamp timestampSql(String valor) {
        return valor == null ? null : Timestamp.valueOf(valor);
    }
}
