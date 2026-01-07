package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.RegistroDetailsModel;
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

    public boolean insertRegisterDetail(Integer idRegistro, Long idBondDetails, Long idLoanDetails, Double amountPar) {
        String sql = "INSERT INTO registerdetails (idRegistro, idBondDetails, idLoanDetails, amountPar) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRegistro); // Asigna idRegistro (Nunca debería ser NULL)

            // Si idBondDetails es null, lo asigna como NULL en SQL
            if (idBondDetails != null) {
                stmt.setLong(2, idBondDetails);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }

            // Si idLoanDetails es null, lo asigna como NULL en SQL
            if (idLoanDetails != null) {
                stmt.setLong(3, idLoanDetails);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            stmt.setDouble(4, amountPar); // Asigna amountPar (Debería tener siempre valor)

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Retorna true si la inserción fue exitosa

        } catch (SQLException e) {

            System.out.println("Error -< " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "ERROR", JOptionPane.WARNING_MESSAGE);
            return false; // Retorna false si hay error
        }
    }

    public List<RegistroDetailsModel> findRegisterDetailsByEmployeeId(String employeeId) {
        String sql = "SELECT DATE_FORMAT(rs.fecha_registro, '%Y-%m-%d') AS fecha_registro, rs.codigo, rs.amount, "
                + "CONCAT('Préstamo', '-', loan.SoliNum, ' ', loan.Dues, '/', ld.Dues) AS conceptLoan, "
                + "CONCAT(ser.description, '-', bon.SoliNum, ' ', bon.dues, '/', abDe.dues) AS conceptBond, "
                + "ld.PaymentDate AS fechaVLoan, abDe.paymentDate AS fechaVBond, "
                + "rsDe.amountPar, ld.MonthlyFeeValue AS montoLoan, abDe.monthly AS montoBond "
                + "FROM financialtracker1.registro rs "
                + "LEFT JOIN financialtracker1.registerdetails rsDe ON rsDe.idRegistro = rs.id "
                + "LEFT JOIN financialtracker1.loandetail ld ON ld.ID = rsDe.idLoanDetails "
                + "LEFT JOIN financialtracker1.loan loan ON loan.ID = ld.LoanID "
                + "LEFT JOIN financialtracker1.abonodetail abDe ON abDe.id = rsDe.idBondDetails "
                + "LEFT JOIN financialtracker1.abono bon ON bon.id = abDe.AbonoID "
                + "LEFT JOIN financialtracker1.service_concept ser ON ser.id = bon.service_concept_id "
                + "WHERE rs.empleado_id = ? ORDER BY rs.fecha_registro DESC";

        List<RegistroDetailsModel> registros = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RegistroDetailsModel registro = new RegistroDetailsModel();
                    registro.setFechaRegistro(rs.getString("fecha_registro"));
                    registro.setCodigo(rs.getString("codigo"));
                    registro.setAmount(rs.getBigDecimal("amount"));
                    registro.setConceptLoan(rs.getString("conceptLoan"));
                    registro.setConceptBond(rs.getString("conceptBond"));
                    registro.setFechaVLoan(rs.getString("fechaVLoan"));
                    registro.setFechaVBond(rs.getString("fechaVBond"));
                    registro.setAmountPar(rs.getBigDecimal("amountPar"));
                    registro.setMontoLoan(rs.getBigDecimal("montoLoan"));
                    registro.setMontoBond(rs.getBigDecimal("montoBond"));

                    registros.add(registro);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error -< " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Ocurrio un problema", "ERROR", JOptionPane.WARNING_MESSAGE);
        }

        return registros;
    }

    public boolean insertarRegistroCompleto(RegistroTb registro, LoanDetailsTb prestamo, AbonoDetailsTb bonos, Double monto) {

        String sqlRegistro = "CALL InsertarRegistro(?,?);";
        String sqlObtenerID = "SELECT LAST_INSERT_ID()"; // Obtener el último ID insertado

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
                insertRegisterDetail(idRegistro, null, prestamo.getId(), monto);
            }

            // 4. Insertar en la tabla bono si existen bonos
            if (bonos != null) {
                insertRegisterDetail(idRegistro, bonos.getId(), null, monto);
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

                System.out.println("Error -< " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un problema", "ERROR", JOptionPane.WARNING_MESSAGE);
            }
        }

    }

    //  Método para insertar un nuevo registro y sus detalles de préstamo y bono
    public int insertarRegistroCompleto(RegistroTb registro, List<LoanDetailsTb> prestamos, List<AbonoDetailsTb> bonos) {
        
        if (prestamos.isEmpty() && bonos.isEmpty()) {
            return 4;
        }
        
        System.out.println("Prestamoms " + prestamos.toString());
        System.out.println("Prestamoms " + bonos.toString());
        
        String sqlRegistro = "CALL InsertarRegistro(?,?);";
        String sqlObtenerID = "SELECT LAST_INSERT_ID()"; // Obtener el último ID insertado

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
            
            // duplicando el pedido en la tabla registro  se envia doble o triple 

            System.out.println("ID de registro generado: " + idRegistro);
            // 3. Insertar en la tabla prestamo si existen préstamos
            if (!prestamos.isEmpty()) {
                System.out.println("Id de registro / " + idRegistro);

                for (LoanDetailsTb p : prestamos) {
                    insertRegisterDetail(idRegistro, null, p.getId(), p.getMonto());
//                    stmtPrestamo.setInt(1, idRegistro);
//                    stmtPrestamo.setString(2, "");
//                    stmtPrestamo.setLong(3, p.getId());
//                    stmtPrestamo.executeUpdate();
                }

            }

            // 4. Insertar en la tabla bono si existen bonos
            if (!bonos.isEmpty()) {

                for (AbonoDetailsTb b : bonos) {
                    insertRegisterDetail(idRegistro, b.getId(), null, b.getMonto());
//                    stmtBono.setInt(1, idRegistro);
//                    stmtBono.setString(2, "");
//                    stmtBono.setLong(3, b.getId());
//                    stmtBono.executeUpdate();
                }

            }

            // 5. Confirmar la transacción
            conn.commit();
            //JOptionPane.showMessageDialog(null, "Se registro correctamente");
            return 1;

        } catch (SQLException e) {
            try {
                conn.rollback(); // Deshacer cambios si hay error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("error  -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al registrar pago: ");
            return 3;
        } finally {
            try {
                conn.setAutoCommit(true); // Reactivar auto-commit
            } catch (SQLException e) {

                System.out.println("Error -< " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Ocurrio un problema", "ERROR", JOptionPane.WARNING_MESSAGE);
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
