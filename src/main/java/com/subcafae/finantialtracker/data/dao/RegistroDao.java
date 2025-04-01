package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.RegistroTb;
import com.subcafae.finantialtracker.report.HistoryPayment.ModelPaymentAndLoan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class RegistroDao {

    private final Connection conn;

    public RegistroDao() {
        this.conn = Conexion.getConnection();
    }

    public boolean insertarRegistroCompleto(RegistroTb registro, LoanDetailsTb prestamo, AbonoDetailsTb bonos) {
        String sqlRegistro = "CALL InsertarRegistro(?,?);";
        String sqlObtenerID = "SELECT LAST_INSERT_ID()"; // Obtener el último ID insertado
        String sqlPrestamo = "INSERT INTO soli_prestamo (registro_id,soli_prestamo,id_prestamoDetails) VALUES (?, ?,?)";
        String sqlBono = "INSERT INTO soli_bonus (registro_id,soli_abono,id_abonoDetails) VALUES (?, ?,?)";

        try (PreparedStatement stmtRegistro = conn.prepareStatement(sqlRegistro, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false); // Desactivar auto-commit para manejar transacción

            // 1. Insertar en la tabla registro
            System.out.println("Cantidad / " + registro.getAmount());
            stmtRegistro.setInt(1, registro.getEmpleadoId());
            stmtRegistro.setDouble(2, registro.getAmount());
            stmtRegistro.executeUpdate();

            int idRegistro = 0;
            try (PreparedStatement stmtID = conn.prepareStatement(sqlObtenerID); ResultSet rs = stmtID.executeQuery()) {
                if (rs.next()) {
                    idRegistro = rs.getInt(1);
                }
            }

            System.out.println("ID de registro generado: " + idRegistro);
            // 3. Insertar en la tabla prestamo si existen préstamos
            if (prestamo != null) {
                System.out.println("Id de registro / " + idRegistro);
                try (PreparedStatement stmtPrestamo = conn.prepareStatement(sqlPrestamo)) {

                    stmtPrestamo.setInt(1, idRegistro);
                    stmtPrestamo.setString(2, "");
                    stmtPrestamo.setInt(3, prestamo.getId());
                    stmtPrestamo.executeUpdate();

                }
            }

            // 4. Insertar en la tabla bono si existen bonos
            if (bonos != null) {
                System.out.println("Id de registro / " + idRegistro);
                try (PreparedStatement stmtBono = conn.prepareStatement(sqlBono)) {
                    stmtBono.setInt(1, idRegistro);
                    stmtBono.setString(2, "");
                    stmtBono.setInt(3, bonos.getId());
                    stmtBono.executeUpdate();

                }
            }

            // 5. Confirmar la transacción
            conn.commit();
            //JOptionPane.showMessageDialog(null, "Se registro correctamente");
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback(); // Deshacer cambios si hay error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("error  -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al registrar pago: ");
            return false;
        } finally {
            try {
                conn.setAutoCommit(true); // Reactivar auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    // Método para insertar un nuevo registro y sus detalles de préstamo y bono
    public boolean insertarRegistroCompleto(RegistroTb registro, List<LoanDetailsTb> prestamos, List<AbonoDetailsTb> bonos) {

        String sqlRegistro = "CALL InsertarRegistro(?,?);";
        String sqlObtenerID = "SELECT LAST_INSERT_ID()"; // Obtener el último ID insertado
        String sqlPrestamo = "INSERT INTO soli_prestamo (registro_id,soli_prestamo,id_prestamoDetails) VALUES (?, ?,?)";
        String sqlBono = "INSERT INTO soli_bonus (registro_id,soli_abono,id_abonoDetails) VALUES (?, ?,?)";

        try (PreparedStatement stmtRegistro = conn.prepareStatement(sqlRegistro, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false); // Desactivar auto-commit para manejar transacción

            // 1. Insertar en la tabla registro
            System.out.println("Cantidad / " + registro.getAmount());
            stmtRegistro.setInt(1, registro.getEmpleadoId());
            stmtRegistro.setDouble(2, registro.getAmount());
            stmtRegistro.executeUpdate();

            int idRegistro = 0;
            try (PreparedStatement stmtID = conn.prepareStatement(sqlObtenerID); ResultSet rs = stmtID.executeQuery()) {
                if (rs.next()) {
                    idRegistro = rs.getInt(1);
                }
            }

            System.out.println("ID de registro generado: " + idRegistro);
            // 3. Insertar en la tabla prestamo si existen préstamos
            if (!prestamos.isEmpty()) {
                System.out.println("Id de registro / " + idRegistro);
                try (PreparedStatement stmtPrestamo = conn.prepareStatement(sqlPrestamo)) {
                    for (LoanDetailsTb p : prestamos) {
                        stmtPrestamo.setInt(1, idRegistro);
                        stmtPrestamo.setString(2, "");
                        stmtPrestamo.setInt(3, p.getId());
                        stmtPrestamo.executeUpdate();
                    }
                }
            }

            // 4. Insertar en la tabla bono si existen bonos
            if (!bonos.isEmpty()) {
                System.out.println("Id de registro / " + idRegistro);
                try (PreparedStatement stmtBono = conn.prepareStatement(sqlBono)) {
                    for (AbonoDetailsTb b : bonos) {
                        stmtBono.setInt(1, idRegistro);
                        stmtBono.setString(2, "");
                        stmtBono.setInt(3, b.getId());
                        stmtBono.executeUpdate();
                    }
                }
            }

            // 5. Confirmar la transacción
            conn.commit();
            //JOptionPane.showMessageDialog(null, "Se registro correctamente");
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback(); // Deshacer cambios si hay error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("error  -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al registrar pago: ");
            return false;
        } finally {
            try {
                conn.setAutoCommit(true); // Reactivar auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para obtener registros completos por ID de empleado, agrupados por código
    public List<ModelPaymentAndLoan> obtenerRegistrosPorEmpleado(int empleadoId) {
        List<ModelPaymentAndLoan> registros = new ArrayList<>();
        String sql = "SELECT r.id, r.codigo, r.fecha_registro, r.amount, "
                + " GROUP_CONCAT(DISTINCT p.id_prestamoDetails ORDER BY p.id_prestamoDetails SEPARATOR ', ') AS prestamos, "
                + " GROUP_CONCAT(DISTINCT b.id_abonoDetails ORDER BY b.id_abonoDetails SEPARATOR ', ') AS bonos "
                + "FROM registro r "
                + "LEFT JOIN soli_prestamo p ON r.id = p.registro_id "
                + "LEFT JOIN soli_bonus b ON r.id = b.registro_id "
                + "WHERE r.empleado_id = ? "
                + "GROUP BY r.codigo "
                + "ORDER BY r.fecha_registro DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Código: " + rs.getString("codigo"));
                System.out.println("Fecha Registro: " + rs.getTimestamp("fecha_registro"));
                System.out.println("Monto: " + rs.getDouble("amount"));
                System.out.println("Prestamos (crudo): " + rs.getString("prestamos"));
                System.out.println("Bonos (crudo): " + rs.getString("bonos"));

                ModelPaymentAndLoan registro = new ModelPaymentAndLoan();

                registro.setId(rs.getInt("id"));
                registro.setCodigo(rs.getString("codigo"));
                registro.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                registro.setAmount(rs.getDouble("amount"));
                registro.setBonos(rs.getString("bonos"));
                registro.setPrestamos(rs.getString("prestamos"));

                registros.add(registro);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al obtener registros: " + e.getMessage());
        }
        return registros;
    }
}
