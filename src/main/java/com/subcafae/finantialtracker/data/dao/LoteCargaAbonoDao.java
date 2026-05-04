package com.subcafae.finantialtracker.data.dao;

import com.subcafae.finantialtracker.data.conexion.Conexion;
import com.subcafae.finantialtracker.data.entity.LoteCargaAbonoTb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla lote_carga_abono. Permite registrar lotes de cargas
 * masivas (Excel) y revertirlos.
 */
public class LoteCargaAbonoDao {

    private final Connection connection;

    public LoteCargaAbonoDao() {
        this.connection = Conexion.getConnection();
    }

    /**
     * Crea un nuevo lote ACTIVO. Devuelve el id generado.
     */
    public Integer crearLote(LoteCargaAbonoTb lote) throws SQLException {
        String sql = "INSERT INTO lote_carga_abono "
                + "(fecha_creacion, usuario_id, nombre_archivo, cantidad_abonos, state) "
                + "VALUES (?, ?, ?, ?, 'ACTIVO')";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String fecha = lote.getFechaCreacion();
            if (fecha == null || fecha.isBlank()) {
                fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            stmt.setString(1, fecha);
            if (lote.getUsuarioId() != null) {
                stmt.setInt(2, lote.getUsuarioId());
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setString(3, lote.getNombreArchivo());
            stmt.setInt(4, lote.getCantidadAbonos());

            if (stmt.executeUpdate() == 0) return null;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /**
     * Actualiza el contador de abonos efectivamente insertados.
     */
    public void actualizarCantidad(int loteId, int cantidad) throws SQLException {
        String sql = "UPDATE lote_carga_abono SET cantidad_abonos = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, loteId);
            stmt.executeUpdate();
        }
    }

    /**
     * Lista los lotes ACTIVOS ordenados por fecha desc, mostrando los mas
     * recientes primero. Limita a 50 para no abrumar la UI.
     */
    public List<LoteCargaAbonoTb> findLotesActivos() throws SQLException {
        String sql = "SELECT id, fecha_creacion, usuario_id, nombre_archivo, cantidad_abonos, "
                + "state, motivo_reversion, fecha_reversion, revertido_por "
                + "FROM lote_carga_abono "
                + "WHERE state = 'ACTIVO' "
                + "ORDER BY fecha_creacion DESC "
                + "LIMIT 50";
        List<LoteCargaAbonoTb> result = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    /**
     * Cuenta cuantos abonos del lote tienen pagos parciales o pagados
     * (no se pueden borrar limpiamente).
     */
    public int countAbonosUsados(int loteId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT a.id) "
                + "FROM abono a "
                + "JOIN abonodetail ad ON ad.Abono_id = a.id "
                + "WHERE a.lote_id = ? "
                + "AND (ad.state = 'Pagado' OR ad.state = 'Parcial')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, loteId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Revierte un lote: borra los abonodetail y abono asociados, marca el
     * lote como REVERTIDO con motivo. NO borra abonos que ya tengan pagos
     * (parcial o pagado) — esos quedan huerfanos sin lote_id (NULL).
     *
     * Devuelve cuantos abonos efectivamente borrados.
     */
    public int revertirLote(int loteId, int usuarioId, String motivo) throws SQLException {
        boolean prevAuto = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            // 1. Liberar abonos del lote que ya tengan pagos: poner lote_id = NULL
            String sqlSepararUsados =
                    "UPDATE abono SET lote_id = NULL "
                    + "WHERE lote_id = ? AND id IN ("
                    + "  SELECT * FROM (SELECT a.id FROM abono a "
                    + "    JOIN abonodetail ad ON ad.Abono_id = a.id "
                    + "    WHERE a.lote_id = ? AND (ad.state = 'Pagado' OR ad.state = 'Parcial')"
                    + "  ) t)";
            try (PreparedStatement stmt = connection.prepareStatement(sqlSepararUsados)) {
                stmt.setInt(1, loteId);
                stmt.setInt(2, loteId);
                stmt.executeUpdate();
            }

            // 2. Borrar abonodetails de los abonos que aun pertenecen al lote
            String sqlDeleteDetails =
                    "DELETE ad FROM abonodetail ad "
                    + "JOIN abono a ON ad.Abono_id = a.id "
                    + "WHERE a.lote_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteDetails)) {
                stmt.setInt(1, loteId);
                stmt.executeUpdate();
            }

            // 3. Borrar los abonos del lote
            int abonosBorrados;
            String sqlDeleteAbonos = "DELETE FROM abono WHERE lote_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlDeleteAbonos)) {
                stmt.setInt(1, loteId);
                abonosBorrados = stmt.executeUpdate();
            }

            // 4. Marcar el lote como REVERTIDO
            String sqlUpdateLote =
                    "UPDATE lote_carga_abono SET state = 'REVERTIDO', "
                    + "motivo_reversion = ?, fecha_reversion = ?, revertido_por = ? "
                    + "WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sqlUpdateLote)) {
                stmt.setString(1, motivo);
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(3, usuarioId);
                stmt.setInt(4, loteId);
                stmt.executeUpdate();
            }

            connection.commit();
            return abonosBorrados;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(prevAuto);
        }
    }

    private LoteCargaAbonoTb map(ResultSet rs) throws SQLException {
        LoteCargaAbonoTb lote = new LoteCargaAbonoTb();
        lote.setId(rs.getInt("id"));
        lote.setFechaCreacion(rs.getString("fecha_creacion"));
        int usrId = rs.getInt("usuario_id");
        lote.setUsuarioId(rs.wasNull() ? null : usrId);
        lote.setNombreArchivo(rs.getString("nombre_archivo"));
        lote.setCantidadAbonos(rs.getInt("cantidad_abonos"));
        lote.setState(rs.getString("state"));
        lote.setMotivoReversion(rs.getString("motivo_reversion"));
        lote.setFechaReversion(rs.getString("fecha_reversion"));
        int revBy = rs.getInt("revertido_por");
        lote.setRevertidoPor(rs.wasNull() ? null : revBy);
        return lote;
    }
}
