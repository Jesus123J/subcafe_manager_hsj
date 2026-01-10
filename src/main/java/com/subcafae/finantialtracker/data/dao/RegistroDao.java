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
     * Corrige pagos duplicados en abonos (ajusta payment = monthly)
     * @param usuario Usuario que realiza la corrección
     * @param motivo Motivo de la corrección
     * @return Cantidad de cuotas corregidas
     */
    public int corregirPagosDuplicadosAbonos(String usuario, String motivo) {
        int corregidos = 0;
        String sqlSelect = "SELECT ad.id, a.SoliNum, ad.dues, ad.payment, ad.monthly " +
                "FROM abonodetail ad " +
                "INNER JOIN abono a ON ad.AbonoID = a.ID " +
                "WHERE ad.payment > ad.monthly " +
                "AND ad.state = 'Pagado'";

        String sqlUpdate = "UPDATE abonodetail SET payment = monthly WHERE id = ?";

        String sqlHistorial = "INSERT INTO historial_correcciones " +
                "(tipo, solicitud, cuota, pago_anterior, pago_nuevo, usuario, motivo) " +
                "VALUES ('ABONO', ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
                 ResultSet rs = stmtSelect.executeQuery()) {

                while (rs.next()) {
                    long id = rs.getLong("id");
                    String soliNum = rs.getString("SoliNum");
                    int cuota = rs.getInt("dues");
                    double pagoAnterior = rs.getDouble("payment");
                    double pagoNuevo = rs.getDouble("monthly");

                    // Actualizar el pago
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                        stmtUpdate.setLong(1, id);
                        stmtUpdate.executeUpdate();
                    }

                    // Guardar en historial
                    try (PreparedStatement stmtHist = conn.prepareStatement(sqlHistorial)) {
                        stmtHist.setString(1, soliNum);
                        stmtHist.setInt(2, cuota);
                        stmtHist.setDouble(3, pagoAnterior);
                        stmtHist.setDouble(4, pagoNuevo);
                        stmtHist.setString(5, usuario);
                        stmtHist.setString(6, motivo);
                        stmtHist.executeUpdate();
                    }

                    corregidos++;
                    System.out.println("✓ Corregido abono " + soliNum + " cuota " + cuota +
                            ": " + pagoAnterior + " -> " + pagoNuevo);
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
}
