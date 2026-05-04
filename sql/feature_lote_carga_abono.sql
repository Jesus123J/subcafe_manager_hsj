-- ============================================================
-- Migration: lote_carga_abono
-- Habilita rastrear y revertir cargas masivas de abonos por Excel.
--
-- Aplicar antes de levantar Subcafae-HSJv0.0.20+
-- ============================================================

CREATE TABLE IF NOT EXISTS lote_carga_abono (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fecha_creacion DATETIME NOT NULL,
    usuario_id INT NULL,
    nombre_archivo VARCHAR(255),
    cantidad_abonos INT DEFAULT 0,
    state VARCHAR(20) DEFAULT 'ACTIVO',
    motivo_reversion VARCHAR(500) NULL,
    fecha_reversion DATETIME NULL,
    revertido_por INT NULL,
    INDEX idx_lote_state_fecha (state, fecha_creacion)
);

ALTER TABLE abono ADD COLUMN lote_id INT NULL;
ALTER TABLE abono ADD INDEX idx_abono_lote (lote_id);
