package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura de las DEUDAS DE LA TIENDA (Sub Cafe) que viven en la misma base
 * de datos (tablas clientes, creditos_trabajadores, cierre_creditos_detalle
 * y vistas v_deudores / v_creditos_del_mes creadas por el backend de la tienda).
 *
 * Solo lectura: FinantialTracker no modifica las tablas de la tienda.
 * Las fechas se convierten a hora de Lima (-05:00) para mostrarlas.
 */
public class DeudaTiendaDao {

    /** Fila del resumen general. */
    public static class Resumen {
        public long deudores;
        public BigDecimal pendienteMes = BigDecimal.ZERO;
        public BigDecimal deudaAcumulada = BigDecimal.ZERO;
        public BigDecimal deudaTotal = BigDecimal.ZERO;
        public long consumosMes;
    }

    private Connection cn() {
        return Conexion.getConnection();
    }

    /** true si la tienda ya creo sus tablas en esta base de datos. */
    public boolean tiendaInstalada() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_name = 'v_deudores'";
        try (PreparedStatement st = cn().prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public Resumen resumen() throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(pendiente_mes),0), COALESCE(SUM(deuda_acumulada),0), "
                + "COALESCE(SUM(deuda_total),0), COALESCE(SUM(consumos_mes),0) FROM v_deudores";
        Resumen r = new Resumen();
        try (PreparedStatement st = cn().prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                r.deudores = rs.getLong(1);
                r.pendienteMes = rs.getBigDecimal(2);
                r.deudaAcumulada = rs.getBigDecimal(3);
                r.deudaTotal = rs.getBigDecimal(4);
                r.consumosMes = rs.getLong(5);
            }
        }
        return r;
    }

    /**
     * Trabajadores con deuda viva.
     * Columnas: cliente_id, dni, nombre, condicion, empleado_id, pendiente_mes,
     *           consumos_mes, ultimo_consumo, deuda_acumulada, deuda_total
     */
    public List<Object[]> deudores(String filtro) throws SQLException {
        String sql = "SELECT cliente_id, dni, nombre_completo, condicion_laboral, empleado_id, "
                + "pendiente_mes, consumos_mes, CONVERT_TZ(ultimo_consumo, @@session.time_zone, '-05:00'), "
                + "deuda_acumulada, deuda_total "
                + "FROM v_deudores WHERE (? = '' OR dni LIKE ? OR nombre_completo LIKE ?) "
                + "ORDER BY deuda_total DESC";
        String f = filtro == null ? "" : filtro.trim();
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement st = cn().prepareStatement(sql)) {
            st.setString(1, f);
            st.setString(2, "%" + f + "%");
            st.setString(3, "%" + f + "%");
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getObject(5), rs.getBigDecimal(6), rs.getInt(7), rs.getTimestamp(8),
                        rs.getBigDecimal(9), rs.getBigDecimal(10)
                    });
                }
            }
        }
        return out;
    }

    /**
     * Compras / consumos a credito de un trabajador (mas reciente primero).
     * Columnas: fecha, descripcion, monto, origen (POS/MANUAL), estado, registrado_por, periodo
     */
    public List<Object[]> movimientos(String clienteId) throws SQLException {
        String sql = "SELECT CONVERT_TZ(cr.fecha, @@session.time_zone, '-05:00'), cr.descripcion, cr.monto, "
                + "cr.venta_id IS NOT NULL, cr.cerrado, u.nombre_completo, "
                + "CONCAT(LPAD(cr.periodo_mes, 2, '0'), '/', cr.periodo_anio) "
                + "FROM creditos_trabajadores cr LEFT JOIN usuarios u ON u.id = cr.registrado_por "
                + "WHERE cr.cliente_id = ? ORDER BY cr.fecha DESC";
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement st = cn().prepareStatement(sql)) {
            st.setString(1, clienteId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{
                        rs.getTimestamp(1), rs.getString(2), rs.getBigDecimal(3),
                        rs.getBoolean(4) ? "Venta POS" : "Manual",
                        rs.getBoolean(5) ? "Cerrado" : "Pendiente",
                        rs.getString(6), rs.getString(7)
                    });
                }
            }
        }
        return out;
    }

    /**
     * Ultimas compras a credito de toda la tienda (tiempo real).
     * Columnas: fecha, dni, trabajador, descripcion, monto, origen, estado
     */
    public List<Object[]> recientes(int limite) throws SQLException {
        String sql = "SELECT CONVERT_TZ(cr.fecha, @@session.time_zone, '-05:00'), c.dni, "
                + "CONCAT(c.apellidos, ' ', c.nombres), cr.descripcion, cr.monto, "
                + "cr.venta_id IS NOT NULL, cr.cerrado "
                + "FROM creditos_trabajadores cr JOIN clientes c ON c.id = cr.cliente_id "
                + "ORDER BY cr.fecha DESC LIMIT ?";
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement st = cn().prepareStatement(sql)) {
            st.setInt(1, limite);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    out.add(new Object[]{
                        rs.getTimestamp(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getBigDecimal(5),
                        rs.getBoolean(6) ? "Venta POS" : "Manual",
                        rs.getBoolean(7) ? "Cerrado" : "Pendiente"
                    });
                }
            }
        }
        return out;
    }

    /**
     * Cierres mensuales de la tienda: lo que ira a planilla por trabajador.
     * Columnas: periodo, fecha_cierre, dni, trabajador, empleado_id, consumos, monto, estado FT
     */
    public List<Object[]> cierres() throws SQLException {
        String sql = "SELECT CONCAT(LPAD(cm.mes, 2, '0'), '/', cm.anio), "
                + "CONVERT_TZ(cm.fecha_cierre, @@session.time_zone, '-05:00'), c.dni, "
                + "CONCAT(c.apellidos, ' ', c.nombres), c.empleado_id, cd.cantidad_consumos, cd.monto, "
                + "cd.ft_abono_id, cd.ft_error, a.SoliNum, a.status "
                + "FROM cierre_creditos_detalle cd "
                + "JOIN cierres_mensuales_creditos cm ON cm.id = cd.cierre_id "
                + "JOIN clientes c ON c.id = cd.cliente_id "
                + "LEFT JOIN abono a ON a.ID = cd.ft_abono_id "
                + "ORDER BY cm.anio DESC, cm.mes DESC, cd.monto DESC";
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement st = cn().prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Object abonoId = rs.getObject(8);
                String error = rs.getString(9);
                String estado;
                if (abonoId != null) {
                    estado = "Abono " + rs.getString(10) + " (" + rs.getString(11) + ")";
                } else if (error != null) {
                    estado = "Error: " + error;
                } else {
                    estado = "Solo en tienda (union desactivada)";
                }
                out.add(new Object[]{
                    rs.getString(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4),
                    rs.getObject(5), rs.getInt(6), rs.getBigDecimal(7), estado
                });
            }
        }
        return out;
    }
}
