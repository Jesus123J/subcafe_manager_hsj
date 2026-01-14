package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.AbonoDetailsTb;
import com.subcafae.finantialtracker.data.entity.LoanDetailsTb;
import com.subcafae.finantialtracker.data.entity.RegistroDetailsModel;
import com.subcafae.finantialtracker.data.entity.RegistroTb;
import com.subcafae.finantialtracker.report.HistoryPayment.ModelPaymentAndLoan;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Método para insertar un registro asociado a un lote de carga masiva
     */
    public int insertarRegistroCompletoConLote(RegistroTb registro, List<LoanDetailsTb> prestamos, List<AbonoDetailsTb> bonos, int loteId) {

        if (prestamos.isEmpty() && bonos.isEmpty()) {
            return 4;
        }

        String sqlRegistro = "INSERT INTO registro (empleado_id, amount, lote_id) VALUES (?, ?, ?)";
        String sqlObtenerID = "SELECT LAST_INSERT_ID()";
        String sqlActualizarCodigo = "UPDATE registro SET codigo = CONCAT('P/', LPAD(?, 4, '0'), '-', LPAD(id, 8, '0')) WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // 1. Insertar en la tabla registro con lote_id
            int idRegistro = 0;
            try (PreparedStatement stmtRegistro = conn.prepareStatement(sqlRegistro)) {
                stmtRegistro.setInt(1, registro.getEmpleadoId());
                stmtRegistro.setDouble(2, registro.getAmount());
                stmtRegistro.setInt(3, loteId);
                stmtRegistro.executeUpdate();
            }

            // Obtener el ID generado
            try (PreparedStatement stmtID = conn.prepareStatement(sqlObtenerID);
                 ResultSet rs = stmtID.executeQuery()) {
                if (rs.next()) {
                    idRegistro = rs.getInt(1);
                }
            }

            // 2. Actualizar el código del registro
            try (PreparedStatement stmtCodigo = conn.prepareStatement(sqlActualizarCodigo)) {
                stmtCodigo.setInt(1, loteId);
                stmtCodigo.setInt(2, idRegistro);
                stmtCodigo.executeUpdate();
            }

            // 3. Insertar detalles de préstamos
            if (!prestamos.isEmpty()) {
                for (LoanDetailsTb p : prestamos) {
                    insertRegisterDetail(idRegistro, null, p.getId(), p.getMonto());
                }
            }

            // 4. Insertar detalles de bonos
            if (!bonos.isEmpty()) {
                for (AbonoDetailsTb b : bonos) {
                    insertRegisterDetail(idRegistro, b.getId(), null, b.getMonto());
                }
            }

            // 5. Confirmar la transacción
            conn.commit();
            return 1;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            System.out.println("Error insertando registro con lote: " + e.getMessage());
            return 3;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
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

    // ============================================
    // MÉTODOS PARA REVERTIR PAGO
    // ============================================

    /**
     * Busca un registro por su código (ej: P/0001-00001234)
     * @param codigo El código del recibo a buscar
     * @return Optional con el RegistroTb si existe, vacío si no
     */
    public java.util.Optional<RegistroTb> findByCodigo(String codigo) {
        String sql = "SELECT id, codigo, empleado_id, fecha_registro, amount FROM registro WHERE codigo = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RegistroTb registro = new RegistroTb();
                    registro.setId(rs.getInt("id"));
                    registro.setCodigo(rs.getString("codigo"));
                    registro.setEmpleadoId(rs.getInt("empleado_id"));
                    registro.setFechaRegistro(rs.getTimestamp("fecha_registro"));
                    registro.setAmount(rs.getDouble("amount"));
                    return java.util.Optional.of(registro);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando registro por código: " + e.getMessage());
        }

        return java.util.Optional.empty();
    }

    /**
     * Obtiene los detalles del pago para mostrar antes de revertir
     * @param registroId ID del registro
     * @return String con los detalles formateados en HTML
     */
    public String obtenerDetallesPagoParaRevertir(int registroId) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("<html><body style='width: 350px;'>");

        // Obtener información del registro
        String sqlRegistro = "SELECT r.codigo, r.fecha_registro, r.amount, " +
                "e.fullName as empleado, " +
                "e.national_id as dni " +
                "FROM registro r " +
                "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                "WHERE r.id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sqlRegistro)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalles.append("<h3 style='color:#c0392b;'>INFORMACIÓN DEL PAGO</h3>");
                    detalles.append("<b>Código:</b> ").append(rs.getString("codigo")).append("<br>");
                    detalles.append("<b>Fecha:</b> ").append(rs.getTimestamp("fecha_registro")).append("<br>");
                    detalles.append("<b>Monto Total:</b> S/ ").append(String.format("%.2f", rs.getDouble("amount"))).append("<br>");
                    detalles.append("<b>Empleado:</b> ").append(rs.getString("empleado")).append("<br>");
                    detalles.append("<b>DNI:</b> ").append(rs.getString("dni")).append("<br><br>");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo info del registro: " + e.getMessage());
        }

        // Obtener detalles de préstamos pagados
        String sqlPrestamos = "SELECT rd.id, rd.amountPar, ld.ID as cuotaId, ld.Dues, " +
                "l.SoliNum, ld.MonthlyFeeValue, ld.payment " +
                "FROM registerdetails rd " +
                "INNER JOIN loandetail ld ON rd.idLoanDetails = ld.ID " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE rd.idRegistro = ?";

        boolean hayPrestamos = false;
        try (PreparedStatement stmt = conn.prepareStatement(sqlPrestamos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!hayPrestamos) {
                        detalles.append("<h4 style='color:#2980b9;'>PRÉSTAMOS AFECTADOS:</h4>");
                        detalles.append("<table border='1' cellpadding='3'>");
                        detalles.append("<tr><th>Solicitud</th><th>Cuota</th><th>Pagado</th></tr>");
                        hayPrestamos = true;
                    }
                    detalles.append("<tr>");
                    detalles.append("<td>").append(rs.getString("SoliNum")).append("</td>");
                    detalles.append("<td>").append(rs.getInt("Dues")).append("</td>");
                    detalles.append("<td>S/ ").append(String.format("%.2f", rs.getDouble("amountPar"))).append("</td>");
                    detalles.append("</tr>");
                }
                if (hayPrestamos) {
                    detalles.append("</table><br>");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo préstamos: " + e.getMessage());
        }

        // Obtener detalles de abonos pagados
        String sqlAbonos = "SELECT rd.id, rd.amountPar, ad.id as cuotaId, ad.dues, " +
                "a.SoliNum, sc.description, ad.monthly, ad.payment " +
                "FROM registerdetails rd " +
                "INNER JOIN abonodetail ad ON rd.idBondDetails = ad.id " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "INNER JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE rd.idRegistro = ?";

        boolean hayAbonos = false;
        try (PreparedStatement stmt = conn.prepareStatement(sqlAbonos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (!hayAbonos) {
                        detalles.append("<h4 style='color:#27ae60;'>ABONOS AFECTADOS:</h4>");
                        detalles.append("<table border='1' cellpadding='3'>");
                        detalles.append("<tr><th>Concepto</th><th>Cuota</th><th>Pagado</th></tr>");
                        hayAbonos = true;
                    }
                    detalles.append("<tr>");
                    detalles.append("<td>").append(rs.getString("description")).append("</td>");
                    detalles.append("<td>").append(rs.getInt("dues")).append("</td>");
                    detalles.append("<td>S/ ").append(String.format("%.2f", rs.getDouble("amountPar"))).append("</td>");
                    detalles.append("</tr>");
                }
                if (hayAbonos) {
                    detalles.append("</table><br>");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo abonos: " + e.getMessage());
        }

        if (!hayPrestamos && !hayAbonos) {
            detalles.append("<p style='color:orange;'>⚠ No se encontraron detalles de pago asociados.</p>");
        }

        detalles.append("</body></html>");
        return detalles.toString();
    }

    /**
     * Revierte un pago completo: elimina registerdetails, resta los montos y guarda historial
     * @param registroId ID del registro a revertir
     * @param usuarioReversion Usuario que realiza la reversión
     * @param motivoReversion Motivo de la reversión (puede ser null)
     * @return true si se revirtió correctamente, false si hubo error
     */
    public boolean revertirPago(int registroId, String usuarioReversion, String motivoReversion) {
        try {
            conn.setAutoCommit(false);

            // 0. Obtener información del registro antes de eliminarlo para el historial
            String codigoRegistro = "";
            java.sql.Timestamp fechaRegistroOriginal = null;
            double montoTotal = 0.0;
            int empleadoIdVal = 0;
            String empleadoDni = "";
            String empleadoNombre = "";

            String sqlInfoRegistro = "SELECT r.codigo, r.fecha_registro, r.amount, r.empleado_id, " +
                    "e.national_id, e.fullName " +
                    "FROM registro r " +
                    "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                    "WHERE r.id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlInfoRegistro)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        codigoRegistro = rs.getString("codigo");
                        fechaRegistroOriginal = rs.getTimestamp("fecha_registro");
                        montoTotal = rs.getDouble("amount");
                        empleadoIdVal = rs.getInt("empleado_id");
                        empleadoDni = rs.getString("national_id");
                        empleadoNombre = rs.getString("fullName");
                    }
                }
            }

            // Obtener detalles de préstamos para el historial (JSON)
            StringBuilder detallesPrestamosJson = new StringBuilder("[");
            boolean primerPrestamo = true;

            // 1. Obtener y procesar préstamos
            String sqlGetPrestamos = "SELECT rd.id, rd.idLoanDetails, rd.amountPar, " +
                    "ld.Dues, l.SoliNum, ld.MonthlyFeeValue " +
                    "FROM registerdetails rd " +
                    "INNER JOIN loandetail ld ON rd.idLoanDetails = ld.ID " +
                    "INNER JOIN loan l ON ld.LoanID = l.ID " +
                    "WHERE rd.idRegistro = ? AND rd.idLoanDetails IS NOT NULL";

            try (PreparedStatement stmt = conn.prepareStatement(sqlGetPrestamos)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        long loanDetailId = rs.getLong("idLoanDetails");
                        double amountPar = rs.getDouble("amountPar");
                        String soliNum = rs.getString("SoliNum");
                        int dues = rs.getInt("Dues");

                        // Agregar al JSON del historial
                        if (!primerPrestamo) detallesPrestamosJson.append(",");
                        detallesPrestamosJson.append("{")
                            .append("\"solicitud\":\"").append(soliNum).append("\",")
                            .append("\"cuota\":").append(dues).append(",")
                            .append("\"monto\":").append(amountPar)
                            .append("}");
                        primerPrestamo = false;

                        // Restar el monto pagado de loandetail
                        String sqlUpdateLoan = "UPDATE loandetail SET payment = payment - ?, " +
                                "State = CASE " +
                                "  WHEN (payment - ?) <= 0 THEN 'Pendiente' " +
                                "  WHEN (payment - ?) < MonthlyFeeValue THEN 'Parcial' " +
                                "  ELSE State " +
                                "END " +
                                "WHERE ID = ?";

                        try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdateLoan)) {
                            updateStmt.setDouble(1, amountPar);
                            updateStmt.setDouble(2, amountPar);
                            updateStmt.setDouble(3, amountPar);
                            updateStmt.setLong(4, loanDetailId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }
            detallesPrestamosJson.append("]");

            // Obtener detalles de abonos para el historial (JSON)
            StringBuilder detallesAbonosJson = new StringBuilder("[");
            boolean primerAbono = true;

            // 2. Obtener y procesar abonos
            String sqlGetAbonos = "SELECT rd.id, rd.idBondDetails, rd.amountPar, " +
                    "ad.dues, a.SoliNum, sc.description " +
                    "FROM registerdetails rd " +
                    "INNER JOIN abonodetail ad ON rd.idBondDetails = ad.id " +
                    "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                    "INNER JOIN service_concept sc ON a.service_concept_id = sc.id " +
                    "WHERE rd.idRegistro = ? AND rd.idBondDetails IS NOT NULL";

            try (PreparedStatement stmt = conn.prepareStatement(sqlGetAbonos)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        long bondDetailId = rs.getLong("idBondDetails");
                        double amountPar = rs.getDouble("amountPar");
                        String soliNum = rs.getString("SoliNum");
                        int dues = rs.getInt("dues");
                        String concepto = rs.getString("description");

                        // Agregar al JSON del historial
                        if (!primerAbono) detallesAbonosJson.append(",");
                        detallesAbonosJson.append("{")
                            .append("\"concepto\":\"").append(concepto != null ? concepto.replace("\"", "'") : "").append("\",")
                            .append("\"solicitud\":\"").append(soliNum).append("\",")
                            .append("\"cuota\":").append(dues).append(",")
                            .append("\"monto\":").append(amountPar)
                            .append("}");
                        primerAbono = false;

                        // Restar el monto pagado de abonodetail
                        String sqlUpdateAbono = "UPDATE abonodetail SET payment = payment - ?, " +
                                "state = CASE " +
                                "  WHEN (payment - ?) <= 0 THEN 'Pendiente' " +
                                "  WHEN (payment - ?) < monthly THEN 'Parcial' " +
                                "  ELSE state " +
                                "END " +
                                "WHERE id = ?";

                        try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdateAbono)) {
                            updateStmt.setDouble(1, amountPar);
                            updateStmt.setDouble(2, amountPar);
                            updateStmt.setDouble(3, amountPar);
                            updateStmt.setLong(4, bondDetailId);
                            updateStmt.executeUpdate();
                        }
                    }
                }
            }
            detallesAbonosJson.append("]");

            // 3. Guardar en historial de reversiones
            String sqlInsertHistorial = "INSERT INTO historial_reversiones " +
                    "(registro_id, codigo_registro, fecha_registro_original, monto_total, " +
                    "empleado_id, empleado_dni, empleado_nombre, " +
                    "detalles_prestamos, detalles_abonos, usuario_reversion, motivo_reversion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertHistorial)) {
                stmt.setInt(1, registroId);
                stmt.setString(2, codigoRegistro);
                stmt.setTimestamp(3, fechaRegistroOriginal);
                stmt.setDouble(4, montoTotal);
                stmt.setInt(5, empleadoIdVal);
                stmt.setString(6, empleadoDni);
                stmt.setString(7, empleadoNombre);
                stmt.setString(8, detallesPrestamosJson.toString());
                stmt.setString(9, detallesAbonosJson.toString());
                stmt.setString(10, usuarioReversion != null ? usuarioReversion : "SISTEMA");
                stmt.setString(11, motivoReversion);
                stmt.executeUpdate();
            }

            // 4. Eliminar los registros de registerdetails
            String sqlDeleteDetails = "DELETE FROM registerdetails WHERE idRegistro = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteDetails)) {
                stmt.setInt(1, registroId);
                stmt.executeUpdate();
            }

            // 5. Eliminar el registro principal
            String sqlDeleteRegistro = "DELETE FROM registro WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteRegistro)) {
                stmt.setInt(1, registroId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✓ Pago revertido exitosamente. Registro ID: " + registroId);
            System.out.println("✓ Historial guardado para código: " + codigoRegistro);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Error en rollback: " + rollbackEx.getMessage());
            }
            System.out.println("✗ Error al revertir pago: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Método de compatibilidad - llama al nuevo método con valores por defecto
     */
    public boolean revertirPago(int registroId) {
        return revertirPago(registroId, null, null);
    }

    // ============================================
    // MÉTODOS PARA CARGA MASIVA (LOTES)
    // ============================================

    /**
     * Crea un nuevo lote de carga masiva
     * @param nombreArchivo Nombre del archivo procesado
     * @param mes Mes del proceso
     * @param anio Año del proceso
     * @param usuarioCarga Usuario que realiza la carga
     * @return ID del lote creado, o -1 si hubo error
     */
    public int crearLoteCarga(String nombreArchivo, String mes, String anio, String usuarioCarga) {
        String sql = "INSERT INTO lote_carga (nombre_archivo, mes_proceso, anio_proceso, usuario_carga) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombreArchivo);
            stmt.setString(2, mes);
            stmt.setString(3, anio);
            stmt.setString(4, usuarioCarga);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creando lote de carga: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza las estadísticas del lote después de procesar
     * @param loteId ID del lote
     * @param cantidadRegistros Cantidad de registros procesados
     * @param montoTotal Monto total del lote
     */
    public void actualizarEstadisticasLote(int loteId, int cantidadRegistros, double montoTotal) {
        String sql = "UPDATE lote_carga SET cantidad_registros = ?, monto_total = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidadRegistros);
            stmt.setDouble(2, montoTotal);
            stmt.setInt(3, loteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error actualizando estadísticas del lote: " + e.getMessage());
        }
    }

    /**
     * Inserta un registro asociado a un lote
     */
    public int insertarRegistroConLote(int empleadoId, double amount, int loteId) {
        String sql = "INSERT INTO registro (empleado_id, amount, lote_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, empleadoId);
            stmt.setDouble(2, amount);
            stmt.setInt(3, loteId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error insertando registro con lote: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Obtiene la lista de lotes activos para mostrar en el diálogo de reversión
     * @return Lista de lotes activos formateados
     */
    public List<String[]> obtenerLotesActivos() {
        List<String[]> lotes = new ArrayList<>();
        String sql = "SELECT id, nombre_archivo, mes_proceso, anio_proceso, cantidad_registros, " +
                "monto_total, fecha_carga, usuario_carga " +
                "FROM lote_carga WHERE estado = 'ACTIVO' ORDER BY fecha_carga DESC LIMIT 50";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String[] lote = new String[8];
                lote[0] = String.valueOf(rs.getInt("id"));
                lote[1] = rs.getString("nombre_archivo");
                lote[2] = rs.getString("mes_proceso");
                lote[3] = rs.getString("anio_proceso");
                lote[4] = String.valueOf(rs.getInt("cantidad_registros"));
                lote[5] = String.format("%.2f", rs.getDouble("monto_total"));
                lote[6] = rs.getTimestamp("fecha_carga").toString();
                lote[7] = rs.getString("usuario_carga");
                lotes.add(lote);
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo lotes activos: " + e.getMessage());
        }
        return lotes;
    }

    /**
     * Obtiene los detalles de un lote para mostrar antes de revertir
     * @param loteId ID del lote
     * @return String con los detalles formateados en HTML
     */
    public String obtenerDetalleLoteParaRevertir(int loteId) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("<html><body style='width: 450px;'>");

        // Información del lote
        String sqlLote = "SELECT * FROM lote_carga WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlLote)) {
            stmt.setInt(1, loteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalles.append("<h3 style='color:#c0392b;'>INFORMACIÓN DEL LOTE</h3>");
                    detalles.append("<b>ID Lote:</b> ").append(rs.getInt("id")).append("<br>");
                    detalles.append("<b>Archivo:</b> ").append(rs.getString("nombre_archivo")).append("<br>");
                    detalles.append("<b>Período:</b> ").append(rs.getString("mes_proceso")).append("/").append(rs.getString("anio_proceso")).append("<br>");
                    detalles.append("<b>Fecha Carga:</b> ").append(rs.getTimestamp("fecha_carga")).append("<br>");
                    detalles.append("<b>Usuario:</b> ").append(rs.getString("usuario_carga")).append("<br>");
                    detalles.append("<b>Registros:</b> ").append(rs.getInt("cantidad_registros")).append("<br>");
                    detalles.append("<b>Monto Total:</b> S/ ").append(String.format("%.2f", rs.getDouble("monto_total"))).append("<br><br>");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo info del lote: " + e.getMessage());
        }

        // Resumen de empleados afectados
        String sqlEmpleados = "SELECT e.national_id, e.fullName, r.amount, r.codigo " +
                "FROM registro r " +
                "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                "WHERE r.lote_id = ? ORDER BY e.fullName LIMIT 20";

        try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleados)) {
            stmt.setInt(1, loteId);
            try (ResultSet rs = stmt.executeQuery()) {
                detalles.append("<h4 style='color:#2980b9;'>EMPLEADOS AFECTADOS (máx. 20):</h4>");
                detalles.append("<table border='1' cellpadding='3'>");
                detalles.append("<tr><th>DNI</th><th>Nombre</th><th>Monto</th></tr>");

                while (rs.next()) {
                    detalles.append("<tr>");
                    detalles.append("<td>").append(rs.getString("national_id")).append("</td>");
                    detalles.append("<td>").append(rs.getString("fullName")).append("</td>");
                    detalles.append("<td>S/ ").append(String.format("%.2f", rs.getDouble("amount"))).append("</td>");
                    detalles.append("</tr>");
                }
                detalles.append("</table>");
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo empleados del lote: " + e.getMessage());
        }

        detalles.append("</body></html>");
        return detalles.toString();
    }

    /**
     * Revierte un lote completo de carga masiva
     * @param loteId ID del lote a revertir
     * @param usuarioReversion Usuario que realiza la reversión
     * @param motivoReversion Motivo de la reversión
     * @return true si se revirtió correctamente
     */
    public boolean revertirLoteCarga(int loteId, String usuarioReversion, String motivoReversion) {
        try {
            conn.setAutoCommit(false);

            // 1. Obtener todos los registros del lote con información completa
            String sqlRegistros = "SELECT r.id, r.codigo, r.fecha_registro, r.amount, r.empleado_id, " +
                    "e.national_id, e.fullName " +
                    "FROM registro r " +
                    "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                    "WHERE r.lote_id = ?";

            List<Object[]> registrosInfo = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sqlRegistros)) {
                stmt.setInt(1, loteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Object[] info = new Object[7];
                        info[0] = rs.getInt("id");
                        info[1] = rs.getString("codigo");
                        info[2] = rs.getTimestamp("fecha_registro");
                        info[3] = rs.getDouble("amount");
                        info[4] = rs.getInt("empleado_id");
                        info[5] = rs.getString("national_id");
                        info[6] = rs.getString("fullName");
                        registrosInfo.add(info);
                    }
                }
            }

            if (registrosInfo.isEmpty()) {
                System.out.println("No se encontraron registros para el lote: " + loteId);
                conn.rollback();
                return false;
            }

            System.out.println("Revirtiendo " + registrosInfo.size() + " registros del lote " + loteId);

            // 2. Revertir cada registro individualmente Y guardar en historial
            for (Object[] regInfo : registrosInfo) {
                int registroId = (int) regInfo[0];
                String codigoRegistro = (String) regInfo[1];
                java.sql.Timestamp fechaRegistroOriginal = (java.sql.Timestamp) regInfo[2];
                double montoTotal = (double) regInfo[3];
                int empleadoIdVal = (int) regInfo[4];
                String empleadoDni = (String) regInfo[5];
                String empleadoNombre = (String) regInfo[6];

                // Obtener detalles de préstamos para el historial (JSON)
                StringBuilder detallesPrestamosJson = new StringBuilder("[");
                boolean primerPrestamo = true;

                // Revertir préstamos
                String sqlGetPrestamos = "SELECT rd.idLoanDetails, rd.amountPar, " +
                        "ld.Dues, l.SoliNum " +
                        "FROM registerdetails rd " +
                        "LEFT JOIN loandetail ld ON rd.idLoanDetails = ld.ID " +
                        "LEFT JOIN loan l ON ld.LoanID = l.ID " +
                        "WHERE rd.idRegistro = ? AND rd.idLoanDetails IS NOT NULL";

                try (PreparedStatement stmt = conn.prepareStatement(sqlGetPrestamos)) {
                    stmt.setInt(1, registroId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            long loanDetailId = rs.getLong("idLoanDetails");
                            double amountPar = rs.getDouble("amountPar");
                            String soliNum = rs.getString("SoliNum");
                            int dues = rs.getInt("Dues");

                            // Agregar al JSON del historial
                            if (!primerPrestamo) detallesPrestamosJson.append(",");
                            detallesPrestamosJson.append("{")
                                .append("\"solicitud\":\"").append(soliNum != null ? soliNum : "").append("\",")
                                .append("\"cuota\":").append(dues).append(",")
                                .append("\"monto\":").append(amountPar)
                                .append("}");
                            primerPrestamo = false;

                            String sqlUpdateLoan = "UPDATE loandetail SET payment = payment - ?, " +
                                    "State = CASE " +
                                    "  WHEN (payment - ?) <= 0 THEN 'Pendiente' " +
                                    "  WHEN (payment - ?) < MonthlyFeeValue THEN 'Parcial' " +
                                    "  ELSE State " +
                                    "END " +
                                    "WHERE ID = ?";

                            try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdateLoan)) {
                                updateStmt.setDouble(1, amountPar);
                                updateStmt.setDouble(2, amountPar);
                                updateStmt.setDouble(3, amountPar);
                                updateStmt.setLong(4, loanDetailId);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
                detallesPrestamosJson.append("]");

                // Obtener detalles de abonos para el historial (JSON)
                StringBuilder detallesAbonosJson = new StringBuilder("[");
                boolean primerAbono = true;

                // Revertir abonos
                String sqlGetAbonos = "SELECT rd.idBondDetails, rd.amountPar, " +
                        "ad.dues, a.SoliNum, sc.description " +
                        "FROM registerdetails rd " +
                        "LEFT JOIN abonodetail ad ON rd.idBondDetails = ad.id " +
                        "LEFT JOIN abono a ON ad.AbonoID = a.ID " +
                        "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                        "WHERE rd.idRegistro = ? AND rd.idBondDetails IS NOT NULL";

                try (PreparedStatement stmt = conn.prepareStatement(sqlGetAbonos)) {
                    stmt.setInt(1, registroId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            long bondDetailId = rs.getLong("idBondDetails");
                            double amountPar = rs.getDouble("amountPar");
                            String soliNum = rs.getString("SoliNum");
                            int dues = rs.getInt("dues");
                            String concepto = rs.getString("description");

                            // Agregar al JSON del historial
                            if (!primerAbono) detallesAbonosJson.append(",");
                            detallesAbonosJson.append("{")
                                .append("\"concepto\":\"").append(concepto != null ? concepto.replace("\"", "'") : "").append("\",")
                                .append("\"solicitud\":\"").append(soliNum != null ? soliNum : "").append("\",")
                                .append("\"cuota\":").append(dues).append(",")
                                .append("\"monto\":").append(amountPar)
                                .append("}");
                            primerAbono = false;

                            String sqlUpdateAbono = "UPDATE abonodetail SET payment = payment - ?, " +
                                    "state = CASE " +
                                    "  WHEN (payment - ?) <= 0 THEN 'Pendiente' " +
                                    "  WHEN (payment - ?) < monthly THEN 'Parcial' " +
                                    "  ELSE state " +
                                    "END " +
                                    "WHERE id = ?";

                            try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdateAbono)) {
                                updateStmt.setDouble(1, amountPar);
                                updateStmt.setDouble(2, amountPar);
                                updateStmt.setDouble(3, amountPar);
                                updateStmt.setLong(4, bondDetailId);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }
                detallesAbonosJson.append("]");

                // Guardar en historial de reversiones
                String sqlHistorial = "INSERT INTO historial_reversiones " +
                        "(registro_id, codigo_registro, fecha_registro_original, monto_total, " +
                        "empleado_id, empleado_dni, empleado_nombre, " +
                        "detalles_prestamos, detalles_abonos, " +
                        "usuario_reversion, motivo_reversion) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sqlHistorial)) {
                    stmt.setInt(1, registroId);
                    stmt.setString(2, codigoRegistro);
                    stmt.setTimestamp(3, fechaRegistroOriginal);
                    stmt.setDouble(4, montoTotal);
                    stmt.setInt(5, empleadoIdVal);
                    stmt.setString(6, empleadoDni);
                    stmt.setString(7, empleadoNombre);
                    stmt.setString(8, detallesPrestamosJson.toString());
                    stmt.setString(9, detallesAbonosJson.toString());
                    stmt.setString(10, usuarioReversion != null ? usuarioReversion : "SISTEMA");
                    stmt.setString(11, motivoReversion + " [LOTE #" + loteId + "]");
                    stmt.executeUpdate();
                }

                // Eliminar registerdetails
                String sqlDeleteDetails = "DELETE FROM registerdetails WHERE idRegistro = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteDetails)) {
                    stmt.setInt(1, registroId);
                    stmt.executeUpdate();
                }

                // Eliminar registro
                String sqlDeleteRegistro = "DELETE FROM registro WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteRegistro)) {
                    stmt.setInt(1, registroId);
                    stmt.executeUpdate();
                }
            }

            // 3. Marcar el lote como revertido
            String sqlUpdateLote = "UPDATE lote_carga SET estado = 'REVERTIDO', " +
                    "fecha_reversion = CURRENT_TIMESTAMP, " +
                    "usuario_reversion = ?, " +
                    "motivo_reversion = ? " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLote)) {
                stmt.setString(1, usuarioReversion != null ? usuarioReversion : "SISTEMA");
                stmt.setString(2, motivoReversion);
                stmt.setInt(3, loteId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✓ Lote " + loteId + " revertido exitosamente. " + registrosInfo.size() + " registros eliminados.");
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Error en rollback: " + rollbackEx.getMessage());
            }
            System.out.println("✗ Error al revertir lote: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    // ============================================
    // MÉTODOS PARA CORRECCIÓN DE PAGOS DUPLICADOS
    // ============================================

    /**
     * Busca cuotas de préstamos con pagos duplicados:
     * 1. Cuotas con payment > MonthlyFeeValue (exceso de pago)
     * 2. Cuotas con múltiples registros en registerdetails (pagos duplicados)
     * @return Lista de Object[] con: SoliNum, Dues, Payment, MonthlyFeeValue, ID, TipoDuplicado, CantidadRegistros
     */
    public List<Object[]> buscarPagosDuplicadosPrestamos() {
        List<Object[]> duplicados = new ArrayList<>();

        // 1. Buscar cuotas con payment > MonthlyFeeValue
        String sql1 = "SELECT l.SoliNum, ld.Dues, ld.payment, ld.MonthlyFeeValue, ld.ID, " +
                "'EXCESO' as TipoDuplicado, 0 as CantRegistros " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE ld.payment > ld.MonthlyFeeValue " +
                "AND ld.State = 'Pagado' " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql1);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getString("SoliNum");
                row[1] = rs.getInt("Dues");
                row[2] = rs.getDouble("payment");
                row[3] = rs.getDouble("MonthlyFeeValue");
                row[4] = rs.getLong("ID");
                row[5] = rs.getString("TipoDuplicado");
                row[6] = rs.getInt("CantRegistros");
                duplicados.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error buscando excesos de préstamos: " + e.getMessage());
        }

        // 2. Buscar cuotas con múltiples registros de pago
        String sql2 = "SELECT l.SoliNum, ld.Dues, ld.payment, ld.MonthlyFeeValue, ld.ID, " +
                "'REGISTRO_MULTIPLE' as TipoDuplicado, COUNT(rd.id) as CantRegistros, " +
                "SUM(rd.amountPar) as TotalPagado " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "INNER JOIN registerdetails rd ON rd.idLoanDetails = ld.ID " +
                "GROUP BY ld.ID, l.SoliNum, ld.Dues, ld.payment, ld.MonthlyFeeValue " +
                "HAVING COUNT(rd.id) > 1 " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql2);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getString("SoliNum");
                row[1] = rs.getInt("Dues");
                row[2] = rs.getDouble("TotalPagado"); // Total pagado (suma de todos los registros)
                row[3] = rs.getDouble("MonthlyFeeValue");
                row[4] = rs.getLong("ID");
                row[5] = rs.getString("TipoDuplicado");
                row[6] = rs.getInt("CantRegistros");
                duplicados.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error buscando registros múltiples de préstamos: " + e.getMessage());
        }

        return duplicados;
    }

    /**
     * Busca cuotas de abonos con pagos duplicados:
     * 1. Cuotas con payment > monthly (exceso de pago)
     * 2. Cuotas con múltiples registros en registerdetails (pagos duplicados)
     * @return Lista de Object[] con: SoliNum, Dues, Payment, Monthly, ID, TipoDuplicado, CantidadRegistros
     */
    public List<Object[]> buscarPagosDuplicadosAbonos() {
        List<Object[]> duplicados = new ArrayList<>();

        // 1. Buscar cuotas con payment > monthly
        String sql1 = "SELECT a.SoliNum, ad.dues, ad.payment, ad.monthly, ad.id, " +
                "'EXCESO' as TipoDuplicado, 0 as CantRegistros " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "WHERE ad.payment > ad.monthly " +
                "AND ad.state = 'Pagado' " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql1);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getString("SoliNum");
                row[1] = rs.getInt("dues");
                row[2] = rs.getDouble("payment");
                row[3] = rs.getDouble("monthly");
                row[4] = rs.getLong("id");
                row[5] = rs.getString("TipoDuplicado");
                row[6] = rs.getInt("CantRegistros");
                duplicados.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error buscando excesos de abonos: " + e.getMessage());
        }

        // 2. Buscar cuotas con múltiples registros de pago
        String sql2 = "SELECT a.SoliNum, ad.dues, ad.payment, ad.monthly, ad.id, " +
                "'REGISTRO_MULTIPLE' as TipoDuplicado, COUNT(rd.id) as CantRegistros, " +
                "SUM(rd.amountPar) as TotalPagado " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "INNER JOIN registerdetails rd ON rd.idBondDetails = ad.id " +
                "GROUP BY ad.id, a.SoliNum, ad.dues, ad.payment, ad.monthly " +
                "HAVING COUNT(rd.id) > 1 " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql2);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[7];
                row[0] = rs.getString("SoliNum");
                row[1] = rs.getInt("dues");
                row[2] = rs.getDouble("TotalPagado"); // Total pagado
                row[3] = rs.getDouble("monthly");
                row[4] = rs.getLong("id");
                row[5] = rs.getString("TipoDuplicado");
                row[6] = rs.getInt("CantRegistros");
                duplicados.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error buscando registros múltiples de abonos: " + e.getMessage());
        }

        return duplicados;
    }

    /**
     * Corrige pagos duplicados en préstamos:
     * 1. Ajusta payment = MonthlyFeeValue donde hay exceso
     * 2. Elimina registros duplicados en registerdetails (mantiene el primero)
     * @param usuario Usuario que realiza la corrección
     * @param motivo Motivo de la corrección
     * @return Cantidad de cuotas corregidas
     */
    public int corregirPagosDuplicadosPrestamos(String usuario, String motivo) {
        int corregidos = 0;

        try {
            conn.setAutoCommit(false);

            // Crear tabla historial si no existe
            crearTablaHistorialCorrecciones();

            // 1. Corregir excesos de pago
            String sqlSelectExceso = "SELECT ld.ID, l.SoliNum, ld.Dues, ld.payment, ld.MonthlyFeeValue " +
                    "FROM loandetail ld " +
                    "INNER JOIN loan l ON ld.LoanID = l.ID " +
                    "WHERE ld.payment > ld.MonthlyFeeValue " +
                    "AND ld.State = 'Pagado'";

            String sqlUpdateExceso = "UPDATE loandetail SET payment = MonthlyFeeValue WHERE ID = ?";

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectExceso);
                 ResultSet rs = stmtSelect.executeQuery()) {

                while (rs.next()) {
                    long id = rs.getLong("ID");
                    String soliNum = rs.getString("SoliNum");
                    int cuota = rs.getInt("Dues");
                    double pagoAnterior = rs.getDouble("payment");
                    double pagoNuevo = rs.getDouble("MonthlyFeeValue");

                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateExceso)) {
                        stmtUpdate.setLong(1, id);
                        stmtUpdate.executeUpdate();
                    }

                    guardarHistorialCorreccion("PRESTAMO_EXCESO", soliNum, cuota, pagoAnterior, pagoNuevo, usuario, motivo);
                    corregidos++;
                    System.out.println("✓ Corregido exceso préstamo " + soliNum + " cuota " + cuota);
                }
            }

            // 2. Eliminar registros duplicados en registerdetails
            String sqlSelectDuplicados = "SELECT ld.ID, l.SoliNum, ld.Dues, GROUP_CONCAT(rd.id ORDER BY rd.id) as registros " +
                    "FROM loandetail ld " +
                    "INNER JOIN loan l ON ld.LoanID = l.ID " +
                    "INNER JOIN registerdetails rd ON rd.idLoanDetails = ld.ID " +
                    "GROUP BY ld.ID, l.SoliNum, ld.Dues " +
                    "HAVING COUNT(rd.id) > 1";

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectDuplicados);
                 ResultSet rs = stmtSelect.executeQuery()) {

                while (rs.next()) {
                    String soliNum = rs.getString("SoliNum");
                    int cuota = rs.getInt("Dues");
                    String registrosStr = rs.getString("registros");

                    // Obtener IDs de registros, mantener solo el primero
                    String[] ids = registrosStr.split(",");
                    if (ids.length > 1) {
                        for (int i = 1; i < ids.length; i++) {
                            String sqlDelete = "DELETE FROM registerdetails WHERE id = ?";
                            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                                stmtDelete.setInt(1, Integer.parseInt(ids[i].trim()));
                                stmtDelete.executeUpdate();
                            }
                        }
                        guardarHistorialCorreccion("PRESTAMO_REG_DUP", soliNum, cuota, ids.length, 1, usuario,
                                motivo + " - Eliminados " + (ids.length - 1) + " registros duplicados");
                        corregidos++;
                        System.out.println("✓ Eliminados " + (ids.length - 1) + " registros duplicados de préstamo " + soliNum + " cuota " + cuota);
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error corrigiendo duplicados de préstamos: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
        return corregidos;
    }

    /**
     * Corrige pagos duplicados en abonos:
     * 1. Ajusta payment = monthly donde hay exceso
     * 2. Elimina registros duplicados en registerdetails (mantiene el primero)
     * @param usuario Usuario que realiza la corrección
     * @param motivo Motivo de la corrección
     * @return Cantidad de cuotas corregidas
     */
    public int corregirPagosDuplicadosAbonos(String usuario, String motivo) {
        int corregidos = 0;

        try {
            conn.setAutoCommit(false);

            // 1. Corregir excesos de pago
            String sqlSelectExceso = "SELECT ad.id, a.SoliNum, ad.dues, ad.payment, ad.monthly " +
                    "FROM abonodetail ad " +
                    "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                    "WHERE ad.payment > ad.monthly " +
                    "AND ad.state = 'Pagado'";

            String sqlUpdateExceso = "UPDATE abonodetail SET payment = monthly WHERE id = ?";

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectExceso);
                 ResultSet rs = stmtSelect.executeQuery()) {

                while (rs.next()) {
                    long id = rs.getLong("id");
                    String soliNum = rs.getString("SoliNum");
                    int cuota = rs.getInt("dues");
                    double pagoAnterior = rs.getDouble("payment");
                    double pagoNuevo = rs.getDouble("monthly");

                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateExceso)) {
                        stmtUpdate.setLong(1, id);
                        stmtUpdate.executeUpdate();
                    }

                    guardarHistorialCorreccion("ABONO_EXCESO", soliNum, cuota, pagoAnterior, pagoNuevo, usuario, motivo);
                    corregidos++;
                    System.out.println("✓ Corregido exceso abono " + soliNum + " cuota " + cuota);
                }
            }

            // 2. Eliminar registros duplicados en registerdetails
            String sqlSelectDuplicados = "SELECT ad.id, a.SoliNum, ad.dues, GROUP_CONCAT(rd.id ORDER BY rd.id) as registros " +
                    "FROM abonodetail ad " +
                    "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                    "INNER JOIN registerdetails rd ON rd.idBondDetails = ad.id " +
                    "GROUP BY ad.id, a.SoliNum, ad.dues " +
                    "HAVING COUNT(rd.id) > 1";

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectDuplicados);
                 ResultSet rs = stmtSelect.executeQuery()) {

                while (rs.next()) {
                    String soliNum = rs.getString("SoliNum");
                    int cuota = rs.getInt("dues");
                    String registrosStr = rs.getString("registros");

                    String[] ids = registrosStr.split(",");
                    if (ids.length > 1) {
                        for (int i = 1; i < ids.length; i++) {
                            String sqlDelete = "DELETE FROM registerdetails WHERE id = ?";
                            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                                stmtDelete.setInt(1, Integer.parseInt(ids[i].trim()));
                                stmtDelete.executeUpdate();
                            }
                        }
                        guardarHistorialCorreccion("ABONO_REG_DUP", soliNum, cuota, ids.length, 1, usuario,
                                motivo + " - Eliminados " + (ids.length - 1) + " registros duplicados");
                        corregidos++;
                        System.out.println("✓ Eliminados " + (ids.length - 1) + " registros duplicados de abono " + soliNum + " cuota " + cuota);
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error corrigiendo duplicados de abonos: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
        return corregidos;
    }

    /**
     * Crea la tabla historial_correcciones si no existe
     */
    private void crearTablaHistorialCorrecciones() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS historial_correcciones (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "tipo VARCHAR(30), " +
                    "solicitud VARCHAR(50), " +
                    "cuota INT, " +
                    "pago_anterior DECIMAL(10,2), " +
                    "pago_nuevo DECIMAL(10,2), " +
                    "usuario VARCHAR(100), " +
                    "motivo TEXT, " +
                    "fecha_correccion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(createTable)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Tabla ya existe, continuar
        }
    }

    /**
     * Guarda un registro en el historial de correcciones
     */
    private void guardarHistorialCorreccion(String tipo, String solicitud, int cuota,
                                            double pagoAnterior, double pagoNuevo,
                                            String usuario, String motivo) {
        String sql = "INSERT INTO historial_correcciones " +
                "(tipo, solicitud, cuota, pago_anterior, pago_nuevo, usuario, motivo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            stmt.setString(2, solicitud);
            stmt.setInt(3, cuota);
            stmt.setDouble(4, pagoAnterior);
            stmt.setDouble(5, pagoNuevo);
            stmt.setString(6, usuario);
            stmt.setString(7, motivo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error guardando historial: " + e.getMessage());
        }
    }

    // ============================================
    // MÉTODOS PARA REORGANIZAR PAGOS HUÉRFANOS
    // ============================================

    /**
     * Busca registros huérfanos (sin detalles en registerdetails) por empleado
     * @return Lista de Object[] con: empleado_id, dni, nombre, cantidad_registros_huerfanos, monto_total_huerfano
     */
    public List<Object[]> buscarEmpleadosConRegistrosHuerfanos() {
        List<Object[]> empleados = new ArrayList<>();

        String sql = "SELECT r.empleado_id, e.national_id, e.fullName, " +
                "COUNT(r.id) as cantidad_huerfanos, SUM(r.amount) as monto_huerfano " +
                "FROM registro r " +
                "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                "LEFT JOIN registerdetails rd ON rd.idRegistro = r.id " +
                "WHERE rd.id IS NULL " +
                "GROUP BY r.empleado_id, e.national_id, e.fullName " +
                "ORDER BY e.fullName";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("empleado_id");
                row[1] = rs.getString("national_id");
                row[2] = rs.getString("fullName");
                row[3] = rs.getInt("cantidad_huerfanos");
                row[4] = rs.getDouble("monto_huerfano");
                empleados.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error buscando empleados con registros huérfanos: " + e.getMessage());
        }

        return empleados;
    }

    /**
     * Obtiene detalles de un empleado específico para reorganizar sus pagos
     * @param empleadoId ID del empleado
     * @return Object[] con info del empleado y sus registros/cuotas pendientes
     */
    public Map<String, Object> obtenerDetalleEmpleadoParaReorganizar(int empleadoId) {
        Map<String, Object> detalle = new HashMap<>();

        // Info del empleado
        String sqlEmpleado = "SELECT national_id, fullName FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detalle.put("dni", rs.getString("national_id"));
                    detalle.put("nombre", rs.getString("fullName"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo empleado: " + e.getMessage());
        }

        // Registros huérfanos (sin detalles)
        List<Object[]> registrosHuerfanos = new ArrayList<>();
        String sqlHuerfanos = "SELECT r.id, r.codigo, r.fecha_registro, r.amount " +
                "FROM registro r " +
                "LEFT JOIN registerdetails rd ON rd.idRegistro = r.id " +
                "WHERE r.empleado_id = ? AND rd.id IS NULL " +
                "ORDER BY r.fecha_registro ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sqlHuerfanos)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] reg = new Object[4];
                    reg[0] = rs.getInt("id");
                    reg[1] = rs.getString("codigo");
                    reg[2] = rs.getTimestamp("fecha_registro");
                    reg[3] = rs.getDouble("amount");
                    registrosHuerfanos.add(reg);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo registros huérfanos: " + e.getMessage());
        }
        detalle.put("registrosHuerfanos", registrosHuerfanos);

        // Cuotas de préstamos pendientes Y pagadas (ordenadas por fecha de vencimiento)
        // Incluimos las pagadas para poder reasignar pagos que ya se hicieron
        List<Object[]> cuotasPrestamos = new ArrayList<>();
        String sqlPrestamos = "SELECT ld.ID, l.SoliNum, ld.Dues, l.Dues as TotalDues, " +
                "ld.MonthlyFeeValue, ld.payment, (ld.MonthlyFeeValue - ld.payment) as pendiente, " +
                "ld.PaymentDate, ld.State " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE l.EmployeeID = ? " +
                "ORDER BY ld.PaymentDate ASC, ld.Dues ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sqlPrestamos)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] cuota = new Object[9];
                    cuota[0] = rs.getLong("ID");
                    cuota[1] = rs.getString("SoliNum");
                    cuota[2] = rs.getInt("Dues");
                    cuota[3] = rs.getInt("TotalDues");
                    cuota[4] = rs.getDouble("MonthlyFeeValue");
                    cuota[5] = rs.getDouble("payment");
                    cuota[6] = rs.getDouble("pendiente");
                    cuota[7] = rs.getDate("PaymentDate");
                    cuota[8] = rs.getString("State");
                    cuotasPrestamos.add(cuota);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo cuotas de préstamos: " + e.getMessage());
        }
        detalle.put("cuotasPrestamos", cuotasPrestamos);

        // Cuotas de abonos pendientes Y pagadas
        List<Object[]> cuotasAbonos = new ArrayList<>();
        String sqlAbonos = "SELECT ad.id, a.SoliNum, ad.dues, a.dues as TotalDues, " +
                "ad.monthly, ad.payment, (ad.monthly - ad.payment) as pendiente, " +
                "ad.paymentDate, ad.state, sc.description as concepto " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "INNER JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE a.EmployeeID = ? " +
                "ORDER BY ad.paymentDate ASC, ad.dues ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sqlAbonos)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] cuota = new Object[10];
                    cuota[0] = rs.getLong("id");
                    cuota[1] = rs.getString("SoliNum");
                    cuota[2] = rs.getInt("dues");
                    cuota[3] = rs.getInt("TotalDues");
                    cuota[4] = rs.getDouble("monthly");
                    cuota[5] = rs.getDouble("payment");
                    cuota[6] = rs.getDouble("pendiente");
                    cuota[7] = rs.getDate("paymentDate");
                    cuota[8] = rs.getString("state");
                    cuota[9] = rs.getString("concepto");
                    cuotasAbonos.add(cuota);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo cuotas de abonos: " + e.getMessage());
        }
        detalle.put("cuotasAbonos", cuotasAbonos);

        return detalle;
    }

    /**
     * Reorganiza los pagos de un empleado: toma los registros huérfanos y los asigna
     * a las cuotas en orden de vencimiento. Primero busca cuotas pendientes/parciales,
     * luego cuotas pagadas sin registro en registerdetails.
     * @param empleadoId ID del empleado
     * @param usuario Usuario que realiza la reorganización
     * @param motivo Motivo de la reorganización
     * @return Cantidad de registros reorganizados
     */
    @SuppressWarnings("unchecked")
    public int reorganizarPagosEmpleado(int empleadoId, String usuario, String motivo) {
        int reorganizados = 0;

        try {
            conn.setAutoCommit(false);

            Map<String, Object> detalle = obtenerDetalleEmpleadoParaReorganizar(empleadoId);
            List<Object[]> registrosHuerfanos = (List<Object[]>) detalle.get("registrosHuerfanos");
            List<Object[]> cuotasPrestamos = (List<Object[]>) detalle.get("cuotasPrestamos");
            List<Object[]> cuotasAbonos = (List<Object[]>) detalle.get("cuotasAbonos");

            if (registrosHuerfanos == null || registrosHuerfanos.isEmpty()) {
                System.out.println("No hay registros huérfanos para el empleado: " + empleadoId);
                return 0;
            }

            // Obtener cuotas de préstamos SIN registro en registerdetails (pagadas pero sin vincular)
            List<Long> prestamosConRegistro = obtenerCuotasConRegistro("idLoanDetails");
            List<Long> abonosConRegistro = obtenerCuotasConRegistro("idBondDetails");

            System.out.println("Reorganizando " + registrosHuerfanos.size() + " registros huérfanos del empleado " + empleadoId);

            // Procesar cada registro huérfano
            for (Object[] regHuerfano : registrosHuerfanos) {
                int registroId = (int) regHuerfano[0];
                String codigo = (String) regHuerfano[1];
                double montoDisponible = (double) regHuerfano[3];
                boolean asignoAlgo = false;

                System.out.println("Procesando registro huérfano: " + codigo + " con monto: " + montoDisponible);

                // Primero intentar asignar a préstamos pendientes/parciales
                for (Object[] cuotaPrestamo : cuotasPrestamos) {
                    if (montoDisponible <= 0) break;

                    long loanDetailId = (long) cuotaPrestamo[0];
                    String soliNum = (String) cuotaPrestamo[1];
                    int numCuota = (int) cuotaPrestamo[2];
                    double mensual = (double) cuotaPrestamo[4];
                    double pagado = (double) cuotaPrestamo[5];
                    double pendiente = (double) cuotaPrestamo[6];
                    String estado = (String) cuotaPrestamo[8];

                    // Si la cuota está pendiente o parcial, asignar el pago
                    if (pendiente > 0) {
                        double pagoAplicar = Math.min(montoDisponible, pendiente);

                        // Insertar en registerdetails
                        String sqlInsert = "INSERT INTO registerdetails (idRegistro, idLoanDetails, amountPar) VALUES (?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                            stmt.setInt(1, registroId);
                            stmt.setLong(2, loanDetailId);
                            stmt.setDouble(3, pagoAplicar);
                            stmt.executeUpdate();
                        }

                        // Actualizar loandetail
                        double nuevoPago = pagado + pagoAplicar;
                        String nuevoEstado = nuevoPago >= mensual ? "Pagado" : "Parcial";

                        String sqlUpdate = "UPDATE loandetail SET payment = ?, State = ? WHERE ID = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                            stmt.setDouble(1, nuevoPago);
                            stmt.setString(2, nuevoEstado);
                            stmt.setLong(3, loanDetailId);
                            stmt.executeUpdate();
                        }

                        // Actualizar valores locales
                        cuotaPrestamo[5] = nuevoPago;
                        cuotaPrestamo[6] = mensual - nuevoPago;
                        cuotaPrestamo[8] = nuevoEstado;

                        montoDisponible -= pagoAplicar;
                        asignoAlgo = true;

                        System.out.println("  -> Asignado S/ " + String.format("%.2f", pagoAplicar) +
                                " a préstamo " + soliNum + " cuota " + numCuota);
                    }
                    // Si está pagada pero no tiene registro en registerdetails, vincular
                    else if ("Pagado".equals(estado) && !prestamosConRegistro.contains(loanDetailId)) {
                        double pagoAplicar = Math.min(montoDisponible, mensual);

                        // Solo vincular si el monto del registro coincide aproximadamente
                        if (Math.abs(pagoAplicar - mensual) < 0.02 || montoDisponible >= mensual) {
                            String sqlInsert = "INSERT INTO registerdetails (idRegistro, idLoanDetails, amountPar) VALUES (?, ?, ?)";
                            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                                stmt.setInt(1, registroId);
                                stmt.setLong(2, loanDetailId);
                                stmt.setDouble(3, pagoAplicar);
                                stmt.executeUpdate();
                            }

                            prestamosConRegistro.add(loanDetailId); // Marcar como vinculado
                            montoDisponible -= pagoAplicar;
                            asignoAlgo = true;

                            System.out.println("  -> Vinculado S/ " + String.format("%.2f", pagoAplicar) +
                                    " a préstamo pagado " + soliNum + " cuota " + numCuota);
                        }
                    }
                }

                // Luego asignar a abonos pendientes/parciales
                for (Object[] cuotaAbono : cuotasAbonos) {
                    if (montoDisponible <= 0) break;

                    long abonoDetailId = (long) cuotaAbono[0];
                    String soliNum = (String) cuotaAbono[1];
                    int numCuota = (int) cuotaAbono[2];
                    double mensual = (double) cuotaAbono[4];
                    double pagado = (double) cuotaAbono[5];
                    double pendiente = (double) cuotaAbono[6];
                    String estado = (String) cuotaAbono[8];
                    String concepto = (String) cuotaAbono[9];

                    // Si la cuota está pendiente o parcial
                    if (pendiente > 0) {
                        double pagoAplicar = Math.min(montoDisponible, pendiente);

                        String sqlInsert = "INSERT INTO registerdetails (idRegistro, idBondDetails, amountPar) VALUES (?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                            stmt.setInt(1, registroId);
                            stmt.setLong(2, abonoDetailId);
                            stmt.setDouble(3, pagoAplicar);
                            stmt.executeUpdate();
                        }

                        double nuevoPago = pagado + pagoAplicar;
                        String nuevoEstado = nuevoPago >= mensual ? "Pagado" : "Parcial";

                        String sqlUpdate = "UPDATE abonodetail SET payment = ?, state = ? WHERE id = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                            stmt.setDouble(1, nuevoPago);
                            stmt.setString(2, nuevoEstado);
                            stmt.setLong(3, abonoDetailId);
                            stmt.executeUpdate();
                        }

                        cuotaAbono[5] = nuevoPago;
                        cuotaAbono[6] = mensual - nuevoPago;
                        cuotaAbono[8] = nuevoEstado;

                        montoDisponible -= pagoAplicar;
                        asignoAlgo = true;

                        System.out.println("  -> Asignado S/ " + String.format("%.2f", pagoAplicar) +
                                " a abono " + concepto + " " + soliNum + " cuota " + numCuota);
                    }
                    // Si está pagada pero no tiene registro
                    else if ("Pagado".equals(estado) && !abonosConRegistro.contains(abonoDetailId)) {
                        double pagoAplicar = Math.min(montoDisponible, mensual);

                        if (Math.abs(pagoAplicar - mensual) < 0.02 || montoDisponible >= mensual) {
                            String sqlInsert = "INSERT INTO registerdetails (idRegistro, idBondDetails, amountPar) VALUES (?, ?, ?)";
                            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                                stmt.setInt(1, registroId);
                                stmt.setLong(2, abonoDetailId);
                                stmt.setDouble(3, pagoAplicar);
                                stmt.executeUpdate();
                            }

                            abonosConRegistro.add(abonoDetailId);
                            montoDisponible -= pagoAplicar;
                            asignoAlgo = true;

                            System.out.println("  -> Vinculado S/ " + String.format("%.2f", pagoAplicar) +
                                    " a abono pagado " + concepto + " " + soliNum + " cuota " + numCuota);
                        }
                    }
                }

                // Si no se asignó nada y hay monto, es un registro duplicado que quedó huérfano
                if (!asignoAlgo) {
                    System.out.println("  -> REGISTRO SIN CUOTAS DISPONIBLES: " + codigo +
                            " con monto S/ " + String.format("%.2f", montoDisponible) +
                            " - será eliminado como duplicado");

                    // Eliminar el registro huérfano que no tiene a donde asignarse
                    String sqlDelete = "DELETE FROM registro WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                        stmt.setInt(1, registroId);
                        stmt.executeUpdate();
                    }

                    guardarHistorialCorreccion("HUERFANO_ELIMINADO", codigo, 0,
                            montoDisponible, 0, usuario, motivo + " - Registro sin cuotas disponibles");
                } else if (montoDisponible > 0.01) {
                    System.out.println("  -> Sobró S/ " + String.format("%.2f", montoDisponible) + " (saldo a favor)");
                }

                reorganizados++;
            }

            // Guardar en historial
            guardarHistorialCorreccion("REORGANIZACION", String.valueOf(empleadoId), 0,
                    registrosHuerfanos.size(), reorganizados, usuario, motivo);

            conn.commit();
            System.out.println("✓ Reorganizados " + reorganizados + " registros del empleado " + empleadoId);

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error reorganizando pagos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }

        return reorganizados;
    }

    /**
     * Obtiene la lista de IDs de cuotas que ya tienen un registro en registerdetails
     * @param campo Campo a buscar: "idLoanDetails" o "idBondDetails"
     * @return Lista de IDs de cuotas con registro
     */
    private List<Long> obtenerCuotasConRegistro(String campo) {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT " + campo + " FROM registerdetails WHERE " + campo + " IS NOT NULL";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo cuotas con registro: " + e.getMessage());
        }

        return ids;
    }

    /**
     * Elimina registros que quedaron completamente vacíos (sin detalles y monto 0)
     * después de la reorganización
     * @return Cantidad de registros eliminados
     */
    public int eliminarRegistrosVacios() {
        int eliminados = 0;

        String sqlSelect = "SELECT r.id, r.codigo FROM registro r " +
                "LEFT JOIN registerdetails rd ON rd.idRegistro = r.id " +
                "WHERE rd.id IS NULL";

        String sqlDelete = "DELETE FROM registro WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            List<Object[]> registrosVacios = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlSelect);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    registrosVacios.add(new Object[]{rs.getInt("id"), rs.getString("codigo")});
                }
            }

            for (Object[] reg : registrosVacios) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                    stmt.setInt(1, (int) reg[0]);
                    stmt.executeUpdate();
                    eliminados++;
                    System.out.println("Eliminado registro vacío: " + reg[1]);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error eliminando registros vacíos: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }

        return eliminados;
    }

    // ============================================
    // MÉTODOS PARA EDITAR PAGOS
    // ============================================

    /**
     * Obtiene los detalles de un pago para edición
     * @param registroId ID del registro
     * @return Map con prestamos, abonos, empleadoNombre, empleadoDni
     */
    public Map<String, Object> obtenerDetallesPagoParaEdicion(int registroId) {
        Map<String, Object> resultado = new HashMap<>();
        List<Object[]> prestamos = new ArrayList<>();
        List<Object[]> abonos = new ArrayList<>();
        String empleadoNombre = "";
        String empleadoDni = "";

        // Obtener información del empleado
        String sqlEmpleado = "SELECT e.fullName, e.national_id " +
                "FROM registro r " +
                "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                "WHERE r.id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    empleadoNombre = rs.getString("fullName");
                    empleadoDni = rs.getString("national_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo empleado: " + e.getMessage());
        }

        // Obtener detalles de préstamos
        String sqlPrestamos = "SELECT l.SoliNum, ld.Dues, ld.MonthlyFeeValue, rd.amountPar, rd.id as rdId " +
                "FROM registerdetails rd " +
                "INNER JOIN loandetail ld ON rd.idLoanDetails = ld.ID " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE rd.idRegistro = ? AND rd.idLoanDetails IS NOT NULL " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sqlPrestamos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("SoliNum");
                    row[1] = rs.getInt("Dues");
                    row[2] = rs.getDouble("MonthlyFeeValue");
                    row[3] = rs.getDouble("amountPar");
                    row[4] = rs.getLong("rdId");
                    prestamos.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo préstamos: " + e.getMessage());
        }

        // Obtener detalles de abonos
        String sqlAbonos = "SELECT a.SoliNum, ad.dues, ad.monthly, rd.amountPar, rd.id as rdId " +
                "FROM registerdetails rd " +
                "INNER JOIN abonodetail ad ON rd.idBondDetails = ad.id " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "WHERE rd.idRegistro = ? AND rd.idBondDetails IS NOT NULL " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sqlAbonos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("SoliNum");
                    row[1] = rs.getInt("dues");
                    row[2] = rs.getDouble("monthly");
                    row[3] = rs.getDouble("amountPar");
                    row[4] = rs.getLong("rdId");
                    abonos.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo abonos: " + e.getMessage());
        }

        resultado.put("prestamos", prestamos);
        resultado.put("abonos", abonos);
        resultado.put("empleadoNombre", empleadoNombre);
        resultado.put("empleadoDni", empleadoDni);

        return resultado;
    }

    /**
     * Actualiza el monto de un detalle de pago
     * @param idRegisterDetail ID del detalle en registerdetails
     * @param esPrestamo true si es préstamo, false si es abono
     * @param montoAnterior Monto anterior
     * @param montoNuevo Nuevo monto
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo del cambio
     * @return true si se actualizó correctamente
     */
    public boolean actualizarMontoDetallePago(long idRegisterDetail, boolean esPrestamo,
                                               double montoAnterior, double montoNuevo,
                                               String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener el ID de la cuota (loandetail o abonodetail)
            String sqlGetDetail = "SELECT idLoanDetails, idBondDetails FROM registerdetails WHERE id = ?";
            Long idLoanDetails = null;
            Long idBondDetails = null;

            try (PreparedStatement stmt = conn.prepareStatement(sqlGetDetail)) {
                stmt.setLong(1, idRegisterDetail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idLoanDetails = rs.getObject("idLoanDetails") != null ? rs.getLong("idLoanDetails") : null;
                        idBondDetails = rs.getObject("idBondDetails") != null ? rs.getLong("idBondDetails") : null;
                    }
                }
            }

            double diferencia = montoNuevo - montoAnterior;

            // Actualizar registerdetails
            String sqlUpdateRd = "UPDATE registerdetails SET amountPar = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateRd)) {
                stmt.setDouble(1, montoNuevo);
                stmt.setLong(2, idRegisterDetail);
                stmt.executeUpdate();
            }

            // Actualizar la cuota correspondiente
            if (esPrestamo && idLoanDetails != null) {
                String sqlUpdateLd = "UPDATE loandetail SET payment = payment + ?, " +
                        "State = CASE " +
                        "  WHEN payment + ? >= MonthlyFeeValue THEN 'Pagado' " +
                        "  WHEN payment + ? > 0 THEN 'Parcial' " +
                        "  ELSE 'Pendiente' " +
                        "END " +
                        "WHERE ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLd)) {
                    stmt.setDouble(1, diferencia);
                    stmt.setDouble(2, diferencia);
                    stmt.setDouble(3, diferencia);
                    stmt.setLong(4, idLoanDetails);
                    stmt.executeUpdate();
                }
            } else if (!esPrestamo && idBondDetails != null) {
                String sqlUpdateAd = "UPDATE abonodetail SET payment = payment + ?, " +
                        "state = CASE " +
                        "  WHEN payment + ? >= monthly THEN 'Pagado' " +
                        "  WHEN payment + ? > 0 THEN 'Parcial' " +
                        "  ELSE 'Pendiente' " +
                        "END " +
                        "WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateAd)) {
                    stmt.setDouble(1, diferencia);
                    stmt.setDouble(2, diferencia);
                    stmt.setDouble(3, diferencia);
                    stmt.setLong(4, idBondDetails);
                    stmt.executeUpdate();
                }
            }

            // Guardar en historial
            guardarHistorialCorreccion(
                esPrestamo ? "EDICION_PRESTAMO" : "EDICION_ABONO",
                String.valueOf(idRegisterDetail),
                0,
                montoAnterior,
                montoNuevo,
                usuario,
                motivo
            );

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error actualizando detalle de pago: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Elimina un detalle de pago y revierte el monto en la cuota
     * @param idRegisterDetail ID del detalle en registerdetails
     * @param esPrestamo true si es préstamo, false si es abono
     * @param monto Monto que se va a revertir
     * @param usuario Usuario que realiza la acción
     * @param motivo Motivo de la eliminación
     * @return true si se eliminó correctamente
     */
    public boolean eliminarDetallePago(long idRegisterDetail, boolean esPrestamo,
                                        double monto, String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener el ID de la cuota y del préstamo/abono padre
            String sqlGetDetail = "SELECT idLoanDetails, idBondDetails FROM registerdetails WHERE id = ?";
            Long idLoanDetails = null;
            Long idBondDetails = null;
            Long loanId = null;
            Long abonoId = null;

            try (PreparedStatement stmt = conn.prepareStatement(sqlGetDetail)) {
                stmt.setLong(1, idRegisterDetail);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idLoanDetails = rs.getObject("idLoanDetails") != null ? rs.getLong("idLoanDetails") : null;
                        idBondDetails = rs.getObject("idBondDetails") != null ? rs.getLong("idBondDetails") : null;
                    }
                }
            }

            // Revertir el pago en la cuota
            if (esPrestamo && idLoanDetails != null) {
                // Obtener LoanID para actualizar estado general
                String sqlGetLoanId = "SELECT LoanID FROM loandetail WHERE ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlGetLoanId)) {
                    stmt.setLong(1, idLoanDetails);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            loanId = rs.getLong("LoanID");
                        }
                    }
                }

                // Actualizar loandetail
                String sqlUpdateLd = "UPDATE loandetail SET payment = GREATEST(0, payment - ?), " +
                        "State = CASE " +
                        "  WHEN GREATEST(0, payment - ?) = 0 THEN 'Pendiente' " +
                        "  WHEN GREATEST(0, payment - ?) < MonthlyFeeValue THEN 'Parcial' " +
                        "  ELSE 'Pagado' " +
                        "END " +
                        "WHERE ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLd)) {
                    stmt.setDouble(1, monto);
                    stmt.setDouble(2, monto);
                    stmt.setDouble(3, monto);
                    stmt.setLong(4, idLoanDetails);
                    stmt.executeUpdate();
                }

                // Actualizar estado general del préstamo (loan)
                if (loanId != null) {
                    actualizarEstadoGeneralLoan(loanId);
                }

            } else if (!esPrestamo && idBondDetails != null) {
                // Obtener AbonoID para actualizar estado general
                String sqlGetAbonoId = "SELECT AbonoID FROM abonodetail WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlGetAbonoId)) {
                    stmt.setLong(1, idBondDetails);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            abonoId = rs.getLong("AbonoID");
                        }
                    }
                }

                // Actualizar abonodetail
                String sqlUpdateAd = "UPDATE abonodetail SET payment = GREATEST(0, payment - ?), " +
                        "state = CASE " +
                        "  WHEN GREATEST(0, payment - ?) = 0 THEN 'Pendiente' " +
                        "  WHEN GREATEST(0, payment - ?) < monthly THEN 'Parcial' " +
                        "  ELSE 'Pagado' " +
                        "END " +
                        "WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateAd)) {
                    stmt.setDouble(1, monto);
                    stmt.setDouble(2, monto);
                    stmt.setDouble(3, monto);
                    stmt.setLong(4, idBondDetails);
                    stmt.executeUpdate();
                }

                // Actualizar estado general del abono
                if (abonoId != null) {
                    actualizarEstadoGeneralAbono(abonoId);
                }
            }

            // Eliminar el detalle de registerdetails
            String sqlDelete = "DELETE FROM registerdetails WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                stmt.setLong(1, idRegisterDetail);
                stmt.executeUpdate();
            }

            // Guardar en historial con más información
            String solicitud = esPrestamo ?
                (loanId != null ? "LOAN-" + loanId + "/LD-" + idLoanDetails : String.valueOf(idRegisterDetail)) :
                (abonoId != null ? "ABONO-" + abonoId + "/AD-" + idBondDetails : String.valueOf(idRegisterDetail));

            guardarHistorialCorreccion(
                esPrestamo ? "ELIMINACION_PRESTAMO" : "ELIMINACION_ABONO",
                solicitud,
                0,
                monto,
                0,
                usuario,
                motivo
            );

            conn.commit();
            System.out.println("✓ Detalle de pago eliminado: " + idRegisterDetail);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error eliminando detalle de pago: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Actualiza el estado general del préstamo (loan) basándose en el estado de sus cuotas
     */
    private void actualizarEstadoGeneralLoan(long loanId) throws SQLException {
        // Contar cuotas por estado
        String sqlContarEstados = "SELECT " +
                "SUM(CASE WHEN State = 'Pagado' THEN 1 ELSE 0 END) AS pagadas, " +
                "SUM(CASE WHEN State = 'Parcial' THEN 1 ELSE 0 END) AS parciales, " +
                "SUM(CASE WHEN State = 'Pendiente' THEN 1 ELSE 0 END) AS pendientes, " +
                "COUNT(*) AS total " +
                "FROM loandetail WHERE LoanID = ?";

        int pagadas = 0, parciales = 0, pendientes = 0, total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sqlContarEstados)) {
            stmt.setLong(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pagadas = rs.getInt("pagadas");
                    parciales = rs.getInt("parciales");
                    pendientes = rs.getInt("pendientes");
                    total = rs.getInt("total");
                }
            }
        }

        // Determinar estado general
        String nuevoEstado;
        if (pagadas == total && total > 0) {
            nuevoEstado = "Completado";
        } else if (pendientes == total) {
            nuevoEstado = "En curso";
        } else if (parciales > 0 || (pagadas > 0 && pagadas < total)) {
            nuevoEstado = "En curso";
        } else {
            nuevoEstado = "En curso";
        }

        // Actualizar loan
        String sqlUpdateLoan = "UPDATE loan SET State = ? WHERE ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateLoan)) {
            stmt.setString(1, nuevoEstado);
            stmt.setLong(2, loanId);
            stmt.executeUpdate();
        }
    }

    /**
     * Actualiza el estado general del abono basándose en el estado de sus cuotas
     */
    private void actualizarEstadoGeneralAbono(long abonoId) throws SQLException {
        // Contar cuotas por estado
        String sqlContarEstados = "SELECT " +
                "SUM(CASE WHEN state = 'Pagado' THEN 1 ELSE 0 END) AS pagadas, " +
                "SUM(CASE WHEN state = 'Parcial' THEN 1 ELSE 0 END) AS parciales, " +
                "SUM(CASE WHEN state = 'Pendiente' THEN 1 ELSE 0 END) AS pendientes, " +
                "COUNT(*) AS total " +
                "FROM abonodetail WHERE AbonoID = ?";

        int pagadas = 0, parciales = 0, pendientes = 0, total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sqlContarEstados)) {
            stmt.setLong(1, abonoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pagadas = rs.getInt("pagadas");
                    parciales = rs.getInt("parciales");
                    pendientes = rs.getInt("pendientes");
                    total = rs.getInt("total");
                }
            }
        }

        // Determinar estado general
        String nuevoEstado;
        if (pagadas == total && total > 0) {
            nuevoEstado = "Completado";
        } else if (pendientes == total) {
            nuevoEstado = "En curso";
        } else {
            nuevoEstado = "En curso";
        }

        // Actualizar abono
        String sqlUpdateAbono = "UPDATE abono SET state = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateAbono)) {
            stmt.setString(1, nuevoEstado);
            stmt.setLong(2, abonoId);
            stmt.executeUpdate();
        }
    }

    /**
     * Busca cuotas de préstamos pendientes o parciales de un empleado
     * @param empleadoId ID del empleado
     * @return Lista de cuotas pendientes [loanDetailId, SoliNum, Dues, MonthlyFeeValue, payment, pendiente, State]
     */
    public List<Object[]> buscarCuotasPrestamosPendientesEmpleado(int empleadoId) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ld.ID, l.SoliNum, ld.Dues, ld.MonthlyFeeValue, ld.payment, " +
                "(ld.MonthlyFeeValue - COALESCE(ld.payment, 0)) AS pendiente, ld.State " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE l.EmployeeID = ? " +
                "AND (ld.State = 'Pendiente' OR ld.State = 'Parcial') " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[7];
                    row[0] = rs.getLong("ID");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getInt("Dues");
                    row[3] = rs.getDouble("MonthlyFeeValue");
                    row[4] = rs.getDouble("payment");
                    row[5] = rs.getDouble("pendiente");
                    row[6] = rs.getString("State");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas préstamos pendientes: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca cuotas de abonos pendientes o parciales de un empleado
     * @param empleadoId ID del empleado
     * @return Lista de cuotas pendientes [abonoDetailId, SoliNum, concepto, dues, monthly, payment, pendiente, State]
     */
    public List<Object[]> buscarCuotasAbonosPendientesEmpleado(int empleadoId) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ad.id, a.SoliNum, COALESCE(sc.description, 'Sin concepto') AS concepto, " +
                "ad.dues, ad.monthly, ad.payment, " +
                "(ad.monthly - COALESCE(ad.payment, 0)) AS pendiente, ad.state " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE a.EmployeeID = ? " +
                "AND (ad.state = 'Pendiente' OR ad.state = 'Parcial') " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getLong("id");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getString("concepto");
                    row[3] = rs.getInt("dues");
                    row[4] = rs.getDouble("monthly");
                    row[5] = rs.getDouble("payment");
                    row[6] = rs.getDouble("pendiente");
                    row[7] = rs.getString("state");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas abonos pendientes: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca TODAS las cuotas de un préstamo por código de solicitud (incluyendo pagadas)
     * @param soliNum Número de solicitud del préstamo
     * @return Lista de cuotas [loanDetailId, SoliNum, Dues, MonthlyFeeValue, payment, pendiente, State, totalCuotas]
     */
    public List<Object[]> buscarCuotasPrestamoPorSolicitud(String soliNum) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ld.ID, l.SoliNum, ld.Dues, ld.MonthlyFeeValue, COALESCE(ld.payment, 0) AS payment, " +
                "(ld.MonthlyFeeValue - COALESCE(ld.payment, 0)) AS pendiente, ld.State, l.Dues AS totalCuotas " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE l.SoliNum = ? " +
                "ORDER BY ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soliNum);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getLong("ID");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getInt("Dues");
                    row[3] = rs.getDouble("MonthlyFeeValue");
                    row[4] = rs.getDouble("payment");
                    row[5] = rs.getDouble("pendiente");
                    row[6] = rs.getString("State");
                    row[7] = rs.getInt("totalCuotas");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas préstamo por solicitud: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca TODAS las cuotas de un abono por código de solicitud (incluyendo pagadas)
     * @param soliNum Número de solicitud del abono
     * @return Lista de cuotas [abonoDetailId, SoliNum, concepto, dues, monthly, payment, pendiente, State, totalCuotas]
     */
    public List<Object[]> buscarCuotasAbonoPorSolicitud(String soliNum) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ad.id, a.SoliNum, COALESCE(sc.description, 'Sin concepto') AS concepto, " +
                "ad.dues, ad.monthly, COALESCE(ad.payment, 0) AS payment, " +
                "(ad.monthly - COALESCE(ad.payment, 0)) AS pendiente, ad.state, a.dues AS totalCuotas " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE a.SoliNum = ? " +
                "ORDER BY ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soliNum);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[9];
                    row[0] = rs.getLong("id");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getString("concepto");
                    row[3] = rs.getInt("dues");
                    row[4] = rs.getDouble("monthly");
                    row[5] = rs.getDouble("payment");
                    row[6] = rs.getDouble("pendiente");
                    row[7] = rs.getString("state");
                    row[8] = rs.getInt("totalCuotas");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas abono por solicitud: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca TODAS las cuotas de préstamos de un empleado (incluyendo pagadas) para edición
     * @param empleadoId ID del empleado
     * @return Lista de cuotas [loanDetailId, SoliNum, Dues, MonthlyFeeValue, payment, pendiente, State]
     */
    public List<Object[]> buscarTodasCuotasPrestamosEmpleado(int empleadoId) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ld.ID, l.SoliNum, ld.Dues, ld.MonthlyFeeValue, COALESCE(ld.payment, 0) AS payment, " +
                "(ld.MonthlyFeeValue - COALESCE(ld.payment, 0)) AS pendiente, ld.State " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE l.EmployeeID = ? " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[7];
                    row[0] = rs.getLong("ID");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getInt("Dues");
                    row[3] = rs.getDouble("MonthlyFeeValue");
                    row[4] = rs.getDouble("payment");
                    row[5] = rs.getDouble("pendiente");
                    row[6] = rs.getString("State");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando todas las cuotas préstamos: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca TODAS las cuotas de abonos de un empleado (incluyendo pagadas) para edición
     * @param empleadoId ID del empleado
     * @return Lista de cuotas [abonoDetailId, SoliNum, concepto, dues, monthly, payment, pendiente, State]
     */
    public List<Object[]> buscarTodasCuotasAbonosEmpleado(int empleadoId) {
        List<Object[]> cuotas = new ArrayList<>();
        String sql = "SELECT ad.id, a.SoliNum, COALESCE(sc.description, 'Sin concepto') AS concepto, " +
                "ad.dues, ad.monthly, COALESCE(ad.payment, 0) AS payment, " +
                "(ad.monthly - COALESCE(ad.payment, 0)) AS pendiente, ad.state " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE a.EmployeeID = ? " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getLong("id");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getString("concepto");
                    row[3] = rs.getInt("dues");
                    row[4] = rs.getDouble("monthly");
                    row[5] = rs.getDouble("payment");
                    row[6] = rs.getDouble("pendiente");
                    row[7] = rs.getString("state");
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando todas las cuotas abonos: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Agrega un nuevo detalle de préstamo a un registro existente
     * @param registroId ID del registro
     * @param loanDetailId ID del detalle de préstamo
     * @param monto Monto a aplicar
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo del cambio
     * @return true si se agregó correctamente
     */
    public boolean agregarDetallePrestamoARegistro(int registroId, long loanDetailId, double monto,
                                                    String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener LoanID para actualizar estado general
            Long loanId = null;
            String sqlGetLoanId = "SELECT LoanID FROM loandetail WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetLoanId)) {
                stmt.setLong(1, loanDetailId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        loanId = rs.getLong("LoanID");
                    }
                }
            }

            // Insertar el nuevo detalle
            String sqlInsert = "INSERT INTO registerdetails (idRegistro, idLoanDetails, amountPar) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, registroId);
                stmt.setLong(2, loanDetailId);
                stmt.setDouble(3, monto);
                stmt.executeUpdate();
            }

            // Actualizar el pago en loandetail
            String sqlUpdate = "UPDATE loandetail SET payment = COALESCE(payment, 0) + ?, " +
                    "State = CASE " +
                    "  WHEN COALESCE(payment, 0) + ? >= MonthlyFeeValue THEN 'Pagado' " +
                    "  WHEN COALESCE(payment, 0) + ? > 0 THEN 'Parcial' " +
                    "  ELSE 'Pendiente' " +
                    "END " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDouble(1, monto);
                stmt.setDouble(2, monto);
                stmt.setDouble(3, monto);
                stmt.setLong(4, loanDetailId);
                stmt.executeUpdate();
            }

            // Actualizar estado general del préstamo
            if (loanId != null) {
                actualizarEstadoGeneralLoan(loanId);
            }

            // Registrar en historial
            guardarHistorialCorreccion("AGREGAR_PRESTAMO_REGISTRO",
                    "REG-" + registroId + "/LOAN-" + loanId + "/LD-" + loanDetailId, 0,
                    0, monto, usuario, motivo);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error agregando detalle préstamo: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Agrega un nuevo detalle de abono a un registro existente
     * @param registroId ID del registro
     * @param abonoDetailId ID del detalle de abono
     * @param monto Monto a aplicar
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo del cambio
     * @return true si se agregó correctamente
     */
    public boolean agregarDetalleAbonoARegistro(int registroId, long abonoDetailId, double monto,
                                                 String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener AbonoID para actualizar estado general
            Long abonoId = null;
            String sqlGetAbonoId = "SELECT AbonoID FROM abonodetail WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetAbonoId)) {
                stmt.setLong(1, abonoDetailId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        abonoId = rs.getLong("AbonoID");
                    }
                }
            }

            // Insertar el nuevo detalle
            String sqlInsert = "INSERT INTO registerdetails (idRegistro, idBondDetails, amountPar) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setInt(1, registroId);
                stmt.setLong(2, abonoDetailId);
                stmt.setDouble(3, monto);
                stmt.executeUpdate();
            }

            // Actualizar el pago en abonodetail
            String sqlUpdate = "UPDATE abonodetail SET payment = COALESCE(payment, 0) + ?, " +
                    "state = CASE " +
                    "  WHEN COALESCE(payment, 0) + ? >= monthly THEN 'Pagado' " +
                    "  WHEN COALESCE(payment, 0) + ? > 0 THEN 'Parcial' " +
                    "  ELSE 'Pendiente' " +
                    "END " +
                    "WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setDouble(1, monto);
                stmt.setDouble(2, monto);
                stmt.setDouble(3, monto);
                stmt.setLong(4, abonoDetailId);
                stmt.executeUpdate();
            }

            // Actualizar estado general del abono
            if (abonoId != null) {
                actualizarEstadoGeneralAbono(abonoId);
            }

            // Registrar en historial
            guardarHistorialCorreccion("AGREGAR_ABONO_REGISTRO",
                    "REG-" + registroId + "/ABONO-" + abonoId + "/AD-" + abonoDetailId, 0,
                    0, monto, usuario, motivo);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error agregando detalle abono: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene detalles completos de un pago para edición, incluyendo información de cuotas
     * @param registroId ID del registro
     * @return Map con toda la información del pago
     */
    public Map<String, Object> obtenerDetallesCompletoPago(int registroId) {
        Map<String, Object> resultado = new HashMap<>();
        List<Object[]> prestamos = new ArrayList<>();
        List<Object[]> abonos = new ArrayList<>();
        String empleadoNombre = "";
        String empleadoDni = "";
        int empleadoId = 0;

        // Obtener información del empleado
        String sqlEmpleado = "SELECT e.employee_id, e.fullName, e.national_id " +
                "FROM registro r " +
                "INNER JOIN employees e ON r.empleado_id = e.employee_id " +
                "WHERE r.id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sqlEmpleado)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    empleadoId = rs.getInt("employee_id");
                    empleadoNombre = rs.getString("fullName");
                    empleadoDni = rs.getString("national_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo empleado: " + e.getMessage());
        }

        // Obtener detalles de préstamos con información completa
        String sqlPrestamos = "SELECT l.SoliNum, ld.Dues, ld.MonthlyFeeValue, ld.payment AS totalPagadoCuota, " +
                "rd.amountPar, rd.id as rdId, ld.ID as ldId, ld.State " +
                "FROM registerdetails rd " +
                "INNER JOIN loandetail ld ON rd.idLoanDetails = ld.ID " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "WHERE rd.idRegistro = ? AND rd.idLoanDetails IS NOT NULL " +
                "ORDER BY l.SoliNum, ld.Dues";

        try (PreparedStatement stmt = conn.prepareStatement(sqlPrestamos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getString("SoliNum");           // Solicitud
                    row[1] = rs.getInt("Dues");                  // Cuota
                    row[2] = rs.getDouble("MonthlyFeeValue");    // Monto mensual
                    row[3] = rs.getDouble("totalPagadoCuota");   // Total pagado en la cuota
                    row[4] = rs.getDouble("amountPar");          // Monto aplicado en este registro
                    row[5] = rs.getLong("rdId");                 // ID registerdetails
                    row[6] = rs.getLong("ldId");                 // ID loandetail
                    row[7] = rs.getString("State");              // Estado
                    prestamos.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo préstamos: " + e.getMessage());
        }

        // Obtener detalles de abonos con información completa
        String sqlAbonos = "SELECT a.SoliNum, COALESCE(sc.description, 'Sin concepto') AS concepto, " +
                "ad.dues, ad.monthly, ad.payment AS totalPagadoCuota, " +
                "rd.amountPar, rd.id as rdId, ad.id as adId, ad.state " +
                "FROM registerdetails rd " +
                "INNER JOIN abonodetail ad ON rd.idBondDetails = ad.id " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "WHERE rd.idRegistro = ? AND rd.idBondDetails IS NOT NULL " +
                "ORDER BY a.SoliNum, ad.dues";

        try (PreparedStatement stmt = conn.prepareStatement(sqlAbonos)) {
            stmt.setInt(1, registroId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[9];
                    row[0] = rs.getString("SoliNum");           // Solicitud
                    row[1] = rs.getString("concepto");          // Concepto
                    row[2] = rs.getInt("dues");                  // Cuota
                    row[3] = rs.getDouble("monthly");            // Monto mensual
                    row[4] = rs.getDouble("totalPagadoCuota");   // Total pagado en la cuota
                    row[5] = rs.getDouble("amountPar");          // Monto aplicado en este registro
                    row[6] = rs.getLong("rdId");                 // ID registerdetails
                    row[7] = rs.getLong("adId");                 // ID abonodetail
                    row[8] = rs.getString("state");              // Estado
                    abonos.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo abonos: " + e.getMessage());
        }

        resultado.put("prestamos", prestamos);
        resultado.put("abonos", abonos);
        resultado.put("empleadoNombre", empleadoNombre);
        resultado.put("empleadoDni", empleadoDni);
        resultado.put("empleadoId", empleadoId);

        return resultado;
    }

    /**
     * Busca cuotas de préstamos con información del voucher donde están pagadas
     * @param soliNum Código de solicitud (opcional, si es vacío busca todas)
     * @param empleadoId ID del empleado
     * @param mostrarTodas Si es true incluye las pagadas
     * @return Lista [loanDetailId, SoliNum, Dues, MonthlyFeeValue, payment, pendiente, State, voucherCodigo, voucherId, rdId, amountParVoucher]
     */
    public List<Object[]> buscarCuotasPrestamosConVoucher(String soliNum, int empleadoId, boolean mostrarTodas) {
        List<Object[]> cuotas = new ArrayList<>();

        String sql = "SELECT ld.ID, l.SoliNum, ld.Dues, ld.MonthlyFeeValue, COALESCE(ld.payment, 0) AS payment, " +
                "(ld.MonthlyFeeValue - COALESCE(ld.payment, 0)) AS pendiente, ld.State, " +
                "r.codigo AS voucherCodigo, r.id AS voucherId, rd.id AS rdId, rd.amountPar " +
                "FROM loandetail ld " +
                "INNER JOIN loan l ON ld.LoanID = l.ID " +
                "LEFT JOIN registerdetails rd ON rd.idLoanDetails = ld.ID " +
                "LEFT JOIN registro r ON rd.idRegistro = r.id " +
                "WHERE 1=1 ";

        // Si hay código de solicitud, buscar por código (ignora empleadoId)
        // Si no hay código, buscar por empleado
        if (soliNum != null && !soliNum.trim().isEmpty()) {
            sql += "AND l.SoliNum LIKE ? ";
        } else {
            sql += "AND l.EmployeeID = ? ";
        }
        if (!mostrarTodas) {
            sql += "AND (ld.State != 'Pagado' OR ld.State IS NULL) ";
        }
        sql += "ORDER BY l.SoliNum, ld.Dues, r.fecha_registro DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (soliNum != null && !soliNum.trim().isEmpty()) {
                stmt.setString(1, "%" + soliNum + "%");
            } else {
                stmt.setInt(1, empleadoId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[11];
                    row[0] = rs.getLong("ID");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getInt("Dues");
                    row[3] = rs.getDouble("MonthlyFeeValue");
                    row[4] = rs.getDouble("payment");
                    row[5] = rs.getDouble("pendiente");
                    row[6] = rs.getString("State");
                    row[7] = rs.getString("voucherCodigo");   // Código del voucher donde está pagado
                    row[8] = rs.getObject("voucherId") != null ? rs.getInt("voucherId") : null;
                    row[9] = rs.getObject("rdId") != null ? rs.getLong("rdId") : null;
                    row[10] = rs.getObject("amountPar") != null ? rs.getDouble("amountPar") : 0.0;
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas préstamos con voucher: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Busca cuotas de abonos con información del voucher donde están pagadas
     * @param soliNum Código de solicitud (opcional)
     * @param empleadoId ID del empleado
     * @param mostrarTodas Si es true incluye las pagadas
     * @return Lista [abonoDetailId, SoliNum, concepto, dues, monthly, payment, pendiente, State, voucherCodigo, voucherId, rdId, amountParVoucher]
     */
    public List<Object[]> buscarCuotasAbonosConVoucher(String soliNum, int empleadoId, boolean mostrarTodas) {
        List<Object[]> cuotas = new ArrayList<>();

        String sql = "SELECT ad.id, a.SoliNum, COALESCE(sc.description, 'Sin concepto') AS concepto, " +
                "ad.dues, ad.monthly, COALESCE(ad.payment, 0) AS payment, " +
                "(ad.monthly - COALESCE(ad.payment, 0)) AS pendiente, ad.state, " +
                "r.codigo AS voucherCodigo, r.id AS voucherId, rd.id AS rdId, rd.amountPar " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "LEFT JOIN service_concept sc ON a.service_concept_id = sc.id " +
                "LEFT JOIN registerdetails rd ON rd.idBondDetails = ad.id " +
                "LEFT JOIN registro r ON rd.idRegistro = r.id " +
                "WHERE 1=1 ";

        // Si hay código de solicitud, buscar por código (ignora empleadoId)
        // Si no hay código, buscar por empleado
        if (soliNum != null && !soliNum.trim().isEmpty()) {
            sql += "AND a.SoliNum LIKE ? ";
        } else {
            sql += "AND a.EmployeeID = ? ";
        }
        if (!mostrarTodas) {
            sql += "AND (ad.state != 'Pagado' OR ad.state IS NULL) ";
        }
        sql += "ORDER BY a.SoliNum, ad.dues, r.fecha_registro DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (soliNum != null && !soliNum.trim().isEmpty()) {
                stmt.setString(1, "%" + soliNum + "%");
            } else {
                stmt.setInt(1, empleadoId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[12];
                    row[0] = rs.getLong("id");
                    row[1] = rs.getString("SoliNum");
                    row[2] = rs.getString("concepto");
                    row[3] = rs.getInt("dues");
                    row[4] = rs.getDouble("monthly");
                    row[5] = rs.getDouble("payment");
                    row[6] = rs.getDouble("pendiente");
                    row[7] = rs.getString("state");
                    row[8] = rs.getString("voucherCodigo");
                    row[9] = rs.getObject("voucherId") != null ? rs.getInt("voucherId") : null;
                    row[10] = rs.getObject("rdId") != null ? rs.getLong("rdId") : null;
                    row[11] = rs.getObject("amountPar") != null ? rs.getDouble("amountPar") : 0.0;
                    cuotas.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error buscando cuotas abonos con voucher: " + e.getMessage());
        }
        return cuotas;
    }

    /**
     * Transfiere un pago de préstamo de un voucher a otro
     * @param rdIdOrigen ID del registerdetails original (a eliminar del voucher origen)
     * @param loanDetailId ID del loandetail
     * @param registroIdDestino ID del registro destino
     * @param montoTransferir Monto a transferir
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo del cambio
     * @return true si se transfirió correctamente
     */
    public boolean transferirPagoPrestamoAVoucher(long rdIdOrigen, long loanDetailId, int registroIdDestino,
                                                   double montoTransferir, String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener información del voucher origen
            int voucherOrigenId = 0;
            double montoOriginal = 0;
            String sqlGetOrigen = "SELECT idRegistro, amountPar FROM registerdetails WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetOrigen)) {
                stmt.setLong(1, rdIdOrigen);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        voucherOrigenId = rs.getInt("idRegistro");
                        montoOriginal = rs.getDouble("amountPar");
                    }
                }
            }

            if (voucherOrigenId == 0) {
                throw new SQLException("No se encontró el pago original");
            }

            // Eliminar el detalle del voucher origen
            String sqlDeleteOrigen = "DELETE FROM registerdetails WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteOrigen)) {
                stmt.setLong(1, rdIdOrigen);
                stmt.executeUpdate();
            }

            // Revertir el pago en loandetail (quitar lo que tenía)
            String sqlRevertLoan = "UPDATE loandetail SET payment = GREATEST(0, COALESCE(payment, 0) - ?), " +
                    "State = CASE " +
                    "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) = 0 THEN 'Pendiente' " +
                    "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) < MonthlyFeeValue THEN 'Parcial' " +
                    "  ELSE 'Pagado' " +
                    "END " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRevertLoan)) {
                stmt.setDouble(1, montoOriginal);
                stmt.setDouble(2, montoOriginal);
                stmt.setDouble(3, montoOriginal);
                stmt.setLong(4, loanDetailId);
                stmt.executeUpdate();
            }

            // Insertar en el nuevo voucher destino
            String sqlInsertDestino = "INSERT INTO registerdetails (idRegistro, idLoanDetails, amountPar) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertDestino)) {
                stmt.setInt(1, registroIdDestino);
                stmt.setLong(2, loanDetailId);
                stmt.setDouble(3, montoTransferir);
                stmt.executeUpdate();
            }

            // Aplicar el nuevo pago en loandetail
            String sqlApplyLoan = "UPDATE loandetail SET payment = COALESCE(payment, 0) + ?, " +
                    "State = CASE " +
                    "  WHEN COALESCE(payment, 0) + ? >= MonthlyFeeValue THEN 'Pagado' " +
                    "  WHEN COALESCE(payment, 0) + ? > 0 THEN 'Parcial' " +
                    "  ELSE 'Pendiente' " +
                    "END " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlApplyLoan)) {
                stmt.setDouble(1, montoTransferir);
                stmt.setDouble(2, montoTransferir);
                stmt.setDouble(3, montoTransferir);
                stmt.setLong(4, loanDetailId);
                stmt.executeUpdate();
            }

            // Actualizar estado general del préstamo
            Long loanId = null;
            String sqlGetLoanId = "SELECT LoanID FROM loandetail WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetLoanId)) {
                stmt.setLong(1, loanDetailId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        loanId = rs.getLong("LoanID");
                    }
                }
            }
            if (loanId != null) {
                actualizarEstadoGeneralLoan(loanId);
            }

            // Verificar si el voucher origen quedó vacío y eliminarlo
            eliminarVoucherSiVacio(voucherOrigenId, usuario);

            // Registrar en historial
            guardarHistorialCorreccion("TRANSFERIR_PRESTAMO_VOUCHER",
                    "ORIGEN-REG-" + voucherOrigenId + "/DESTINO-REG-" + registroIdDestino + "/LD-" + loanDetailId,
                    0, montoOriginal, montoTransferir, usuario, motivo);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error transfiriendo pago préstamo: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Transfiere un pago de abono de un voucher a otro
     * @param rdIdOrigen ID del registerdetails original
     * @param abonoDetailId ID del abonodetail
     * @param registroIdDestino ID del registro destino
     * @param montoTransferir Monto a transferir
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo del cambio
     * @return true si se transfirió correctamente
     */
    public boolean transferirPagoAbonoAVoucher(long rdIdOrigen, long abonoDetailId, int registroIdDestino,
                                                double montoTransferir, String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener información del voucher origen
            int voucherOrigenId = 0;
            double montoOriginal = 0;
            String sqlGetOrigen = "SELECT idRegistro, amountPar FROM registerdetails WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetOrigen)) {
                stmt.setLong(1, rdIdOrigen);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        voucherOrigenId = rs.getInt("idRegistro");
                        montoOriginal = rs.getDouble("amountPar");
                    }
                }
            }

            if (voucherOrigenId == 0) {
                throw new SQLException("No se encontró el pago original");
            }

            // Eliminar el detalle del voucher origen
            String sqlDeleteOrigen = "DELETE FROM registerdetails WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteOrigen)) {
                stmt.setLong(1, rdIdOrigen);
                stmt.executeUpdate();
            }

            // Revertir el pago en abonodetail
            String sqlRevertAbono = "UPDATE abonodetail SET payment = GREATEST(0, COALESCE(payment, 0) - ?), " +
                    "state = CASE " +
                    "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) = 0 THEN 'Pendiente' " +
                    "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) < monthly THEN 'Parcial' " +
                    "  ELSE 'Pagado' " +
                    "END " +
                    "WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRevertAbono)) {
                stmt.setDouble(1, montoOriginal);
                stmt.setDouble(2, montoOriginal);
                stmt.setDouble(3, montoOriginal);
                stmt.setLong(4, abonoDetailId);
                stmt.executeUpdate();
            }

            // Insertar en el nuevo voucher destino
            String sqlInsertDestino = "INSERT INTO registerdetails (idRegistro, idBondDetails, amountPar) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertDestino)) {
                stmt.setInt(1, registroIdDestino);
                stmt.setLong(2, abonoDetailId);
                stmt.setDouble(3, montoTransferir);
                stmt.executeUpdate();
            }

            // Aplicar el nuevo pago en abonodetail
            String sqlApplyAbono = "UPDATE abonodetail SET payment = COALESCE(payment, 0) + ?, " +
                    "state = CASE " +
                    "  WHEN COALESCE(payment, 0) + ? >= monthly THEN 'Pagado' " +
                    "  WHEN COALESCE(payment, 0) + ? > 0 THEN 'Parcial' " +
                    "  ELSE 'Pendiente' " +
                    "END " +
                    "WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlApplyAbono)) {
                stmt.setDouble(1, montoTransferir);
                stmt.setDouble(2, montoTransferir);
                stmt.setDouble(3, montoTransferir);
                stmt.setLong(4, abonoDetailId);
                stmt.executeUpdate();
            }

            // Actualizar estado general del abono
            Long abonoId = null;
            String sqlGetAbonoId = "SELECT AbonoID FROM abonodetail WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetAbonoId)) {
                stmt.setLong(1, abonoDetailId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        abonoId = rs.getLong("AbonoID");
                    }
                }
            }
            if (abonoId != null) {
                actualizarEstadoGeneralAbono(abonoId);
            }

            // Verificar si el voucher origen quedó vacío y eliminarlo
            eliminarVoucherSiVacio(voucherOrigenId, usuario);

            // Registrar en historial
            guardarHistorialCorreccion("TRANSFERIR_ABONO_VOUCHER",
                    "ORIGEN-REG-" + voucherOrigenId + "/DESTINO-REG-" + registroIdDestino + "/AD-" + abonoDetailId,
                    0, montoOriginal, montoTransferir, usuario, motivo);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error transfiriendo pago abono: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica si un voucher (registro) quedó vacío y lo elimina si es así
     * @param registroId ID del registro a verificar
     * @param usuario Usuario que realiza el cambio
     * @return true si se eliminó el voucher, false si no estaba vacío
     */
    public boolean eliminarVoucherSiVacio(int registroId, String usuario) {
        try {
            // Verificar si tiene detalles
            String sqlContarDetalles = "SELECT COUNT(*) AS total FROM registerdetails WHERE idRegistro = ?";
            int totalDetalles = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlContarDetalles)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalDetalles = rs.getInt("total");
                    }
                }
            }

            if (totalDetalles == 0) {
                // Obtener información del voucher antes de eliminarlo
                String codigoVoucher = "";
                double montoVoucher = 0;
                String sqlGetVoucher = "SELECT codigo, amount FROM registro WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlGetVoucher)) {
                    stmt.setInt(1, registroId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            codigoVoucher = rs.getString("codigo");
                            montoVoucher = rs.getDouble("amount");
                        }
                    }
                }

                // Eliminar el registro
                String sqlDelete = "DELETE FROM registro WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                    stmt.setInt(1, registroId);
                    stmt.executeUpdate();
                }

                // Registrar en historial
                guardarHistorialCorreccion("ELIMINAR_VOUCHER_VACIO",
                        "REG-" + registroId + "/COD-" + codigoVoucher,
                        0, montoVoucher, 0, usuario, "Voucher eliminado por quedar vacío después de transferencia");

                System.out.println("Voucher " + codigoVoucher + " (ID: " + registroId + ") eliminado por quedar vacío");
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println("Error verificando/eliminando voucher vacío: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un voucher completo, revirtiendo todos sus pagos
     * @param registroId ID del registro a eliminar
     * @param usuario Usuario que realiza el cambio
     * @param motivo Motivo de la eliminación
     * @return true si se eliminó correctamente
     */
    public boolean eliminarVoucherCompleto(int registroId, String usuario, String motivo) {
        try {
            conn.setAutoCommit(false);

            // Obtener info del voucher
            String codigoVoucher = "";
            double montoVoucher = 0;
            String sqlGetVoucher = "SELECT codigo, amount FROM registro WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetVoucher)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        codigoVoucher = rs.getString("codigo");
                        montoVoucher = rs.getDouble("amount");
                    }
                }
            }

            // Obtener todos los detalles para revertir
            List<Object[]> detalles = new ArrayList<>();
            String sqlDetalles = "SELECT id, idBondDetails, idLoanDetails, amountPar FROM registerdetails WHERE idRegistro = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDetalles)) {
                stmt.setInt(1, registroId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Object[] det = new Object[4];
                        det[0] = rs.getLong("id");
                        det[1] = rs.getObject("idBondDetails");
                        det[2] = rs.getObject("idLoanDetails");
                        det[3] = rs.getDouble("amountPar");
                        detalles.add(det);
                    }
                }
            }

            // Revertir cada detalle
            for (Object[] det : detalles) {
                Long bondId = det[1] != null ? ((Number)det[1]).longValue() : null;
                Long loanId = det[2] != null ? ((Number)det[2]).longValue() : null;
                double monto = (Double) det[3];

                if (loanId != null) {
                    // Revertir préstamo
                    String sqlRevert = "UPDATE loandetail SET payment = GREATEST(0, COALESCE(payment, 0) - ?), " +
                            "State = CASE " +
                            "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) = 0 THEN 'Pendiente' " +
                            "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) < MonthlyFeeValue THEN 'Parcial' " +
                            "  ELSE 'Pagado' " +
                            "END WHERE ID = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlRevert)) {
                        stmt.setDouble(1, monto);
                        stmt.setDouble(2, monto);
                        stmt.setDouble(3, monto);
                        stmt.setLong(4, loanId);
                        stmt.executeUpdate();
                    }

                    // Actualizar estado general del loan
                    Long parentLoanId = null;
                    String sqlGetParent = "SELECT LoanID FROM loandetail WHERE ID = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlGetParent)) {
                        stmt.setLong(1, loanId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) parentLoanId = rs.getLong("LoanID");
                        }
                    }
                    if (parentLoanId != null) actualizarEstadoGeneralLoan(parentLoanId);
                }

                if (bondId != null) {
                    // Revertir abono
                    String sqlRevert = "UPDATE abonodetail SET payment = GREATEST(0, COALESCE(payment, 0) - ?), " +
                            "state = CASE " +
                            "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) = 0 THEN 'Pendiente' " +
                            "  WHEN GREATEST(0, COALESCE(payment, 0) - ?) < monthly THEN 'Parcial' " +
                            "  ELSE 'Pagado' " +
                            "END WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlRevert)) {
                        stmt.setDouble(1, monto);
                        stmt.setDouble(2, monto);
                        stmt.setDouble(3, monto);
                        stmt.setLong(4, bondId);
                        stmt.executeUpdate();
                    }

                    // Actualizar estado general del abono
                    Long parentAbonoId = null;
                    String sqlGetParent = "SELECT AbonoID FROM abonodetail WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlGetParent)) {
                        stmt.setLong(1, bondId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) parentAbonoId = rs.getLong("AbonoID");
                        }
                    }
                    if (parentAbonoId != null) actualizarEstadoGeneralAbono(parentAbonoId);
                }
            }

            // Eliminar todos los detalles
            String sqlDeleteDetalles = "DELETE FROM registerdetails WHERE idRegistro = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteDetalles)) {
                stmt.setInt(1, registroId);
                stmt.executeUpdate();
            }

            // Eliminar el registro
            String sqlDeleteRegistro = "DELETE FROM registro WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteRegistro)) {
                stmt.setInt(1, registroId);
                stmt.executeUpdate();
            }

            // Registrar en historial
            guardarHistorialCorreccion("ELIMINAR_VOUCHER_COMPLETO",
                    "REG-" + registroId + "/COD-" + codigoVoucher,
                    0, montoVoucher, 0, usuario, motivo);

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error en rollback: " + ex.getMessage());
            }
            System.out.println("Error eliminando voucher completo: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restaurando auto-commit: " + e.getMessage());
            }
        }
    }

    // ==================== MÉTODOS PARA AUTOCOMPLETADO ====================

    /**
     * Obtiene lista de códigos de solicitudes de préstamos para autocompletado
     */
    public List<String> obtenerCodigosSolicitudesPrestamos() {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT DISTINCT l.SoliNum FROM loan l " +
                     "WHERE l.SoliNum IS NOT NULL AND l.SoliNum != '' " +
                     "ORDER BY l.SoliNum";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                codigos.add(rs.getString("SoliNum"));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo códigos de préstamos: " + e.getMessage());
        }
        return codigos;
    }

    /**
     * Obtiene lista de códigos de solicitudes de abonos para autocompletado
     */
    public List<String> obtenerCodigosSolicitudesAbonos() {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT DISTINCT a.SoliNum FROM abono a " +
                     "WHERE a.SoliNum IS NOT NULL AND a.SoliNum != '' " +
                     "ORDER BY a.SoliNum";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                codigos.add(rs.getString("SoliNum"));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo códigos de abonos: " + e.getMessage());
        }
        return codigos;
    }

    /**
     * Obtiene lista de códigos de tickets/vouchers para autocompletado
     */
    public List<String> obtenerCodigosTickets() {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT DISTINCT r.codigo FROM registro r " +
                     "WHERE r.codigo IS NOT NULL AND r.codigo != '' " +
                     "ORDER BY r.codigo DESC " +
                     "LIMIT 500";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                codigos.add(rs.getString("codigo"));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo códigos de tickets: " + e.getMessage());
        }
        return codigos;
    }

    /**
     * Obtiene lista de códigos de préstamos para un empleado específico
     */
    public List<String> obtenerCodigosSolicitudesPrestamosPorEmpleado(int empleadoId) {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT DISTINCT l.SoliNum FROM loan l " +
                     "WHERE l.EmployeeID = ? AND l.SoliNum IS NOT NULL AND l.SoliNum != '' " +
                     "ORDER BY l.SoliNum";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codigos.add(rs.getString("SoliNum"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo códigos de préstamos por empleado: " + e.getMessage());
        }
        return codigos;
    }

    /**
     * Obtiene lista de códigos de abonos para un empleado específico
     */
    public List<String> obtenerCodigosSolicitudesAbonosPorEmpleado(int empleadoId) {
        List<String> codigos = new ArrayList<>();
        String sql = "SELECT DISTINCT a.SoliNum FROM abono a " +
                     "WHERE a.EmployeeID = ? AND a.SoliNum IS NOT NULL AND a.SoliNum != '' " +
                     "ORDER BY a.SoliNum";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, empleadoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codigos.add(rs.getString("SoliNum"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo códigos de abonos por empleado: " + e.getMessage());
        }
        return codigos;
    }
}
