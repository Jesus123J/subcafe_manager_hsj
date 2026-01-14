-- ============================================================
-- SCRIPT COMPLETO: DIAGNÓSTICO Y CORRECCIÓN DE PAGOS MAL DISTRIBUIDOS
-- ============================================================
-- Problema: Registros con monto de 300 pero solo se aplicaron 0.75 a las cuotas
-- Autor: Sistema FinantialTracker
-- Fecha: 2026-01-13
-- ============================================================

-- ************************************************************
-- PARTE 1: DIAGNÓSTICO (EJECUTAR PRIMERO PARA VER EL PROBLEMA)
-- ************************************************************

-- 1.1 Ver registros donde el monto total NO coincide con la suma de detalles aplicados
SELECT
    r.id AS registro_id,
    r.codigo,
    r.empleado_id,
    e.fullName AS empleado,
    r.amount AS monto_registro,
    COALESCE(SUM(rd.amountPar), 0) AS monto_aplicado,
    r.amount - COALESCE(SUM(rd.amountPar), 0) AS diferencia_perdida,
    r.fecha_registro
FROM registro r
LEFT JOIN employees e ON e.employee_id = r.empleado_id
LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
GROUP BY r.id, r.codigo, r.empleado_id, e.fullName, r.amount, r.fecha_registro
HAVING ABS(r.amount - COALESCE(SUM(rd.amountPar), 0)) > 0.01
ORDER BY diferencia_perdida DESC;

-- 1.2 Contar cuántos registros tienen este problema
SELECT
    COUNT(*) AS total_registros_con_problema,
    SUM(diferencia) AS monto_total_perdido
FROM (
    SELECT
        r.id,
        r.amount - COALESCE(SUM(rd.amountPar), 0) AS diferencia
    FROM registro r
    LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
    GROUP BY r.id, r.amount
    HAVING ABS(r.amount - COALESCE(SUM(rd.amountPar), 0)) > 0.01
) AS problemas;

-- 1.3 Ver detalle de pagos donde se aplicó muy poco comparado con el monto original
SELECT
    r.id AS registro_id,
    r.codigo,
    e.fullName AS empleado,
    r.amount AS monto_total_registro,
    rd.amountPar AS monto_aplicado_cuota,
    CASE
        WHEN rd.idLoanDetails IS NOT NULL THEN 'PRESTAMO'
        WHEN rd.idBondDetails IS NOT NULL THEN 'ABONO'
        ELSE 'SIN ASIGNAR'
    END AS tipo,
    COALESCE(l.SoliNum, a.SoliNum) AS num_solicitud,
    COALESCE(ld.Dues, ad.dues) AS num_cuota,
    COALESCE(ld.MonthlyFeeValue, ad.monthly) AS cuota_mensual,
    COALESCE(ld.payment, ad.payment) AS total_pagado_cuota,
    r.fecha_registro
FROM registro r
LEFT JOIN employees e ON e.employee_id = r.empleado_id
LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
LEFT JOIN loandetail ld ON ld.ID = rd.idLoanDetails
LEFT JOIN loan l ON l.ID = ld.LoanID
LEFT JOIN abonodetail ad ON ad.id = rd.idBondDetails
LEFT JOIN abono a ON a.id = ad.AbonoID
WHERE r.amount > 50
AND rd.amountPar IS NOT NULL
AND rd.amountPar < (r.amount * 0.5)  -- Aplicó menos del 50% del monto
ORDER BY r.fecha_registro DESC, r.id;

-- 1.4 Ver registros huérfanos (sin ningún detalle asociado)
SELECT
    r.id AS registro_id,
    r.codigo,
    e.fullName AS empleado,
    e.national_id AS dni,
    r.amount AS monto_perdido,
    r.fecha_registro
FROM registro r
LEFT JOIN employees e ON e.employee_id = r.empleado_id
LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
WHERE rd.id IS NULL
ORDER BY r.amount DESC;

-- ************************************************************
-- PARTE 2: CREAR TABLAS DE BACKUP (EJECUTAR ANTES DE CORREGIR)
-- ************************************************************

-- 2.1 Crear tablas de backup
CREATE TABLE IF NOT EXISTS backup_registro_fix_20260113 (
    id INT,
    empleado_id INT,
    amount DECIMAL(10,2),
    codigo VARCHAR(50),
    fecha_registro TIMESTAMP,
    lote_id INT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS backup_registerdetails_fix_20260113 (
    id INT,
    idRegistro INT,
    idBondDetails BIGINT,
    idLoanDetails BIGINT,
    amountPar DECIMAL(10,2),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS backup_loandetail_fix_20260113 (
    ID BIGINT,
    LoanID INT,
    Dues INT,
    payment DECIMAL(10,2),
    MonthlyFeeValue DECIMAL(10,2),
    State VARCHAR(20),
    PaymentDate DATE,
    PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS backup_abonodetail_fix_20260113 (
    id BIGINT,
    AbonoID INT,
    dues INT,
    payment DECIMAL(10,2),
    monthly DECIMAL(10,2),
    state VARCHAR(20),
    paymentDate VARCHAR(20),
    PRIMARY KEY (id)
);

-- 2.2 Guardar datos actuales en backup
INSERT IGNORE INTO backup_registro_fix_20260113
SELECT id, empleado_id, amount, codigo, fecha_registro, lote_id FROM registro;

INSERT IGNORE INTO backup_registerdetails_fix_20260113
SELECT id, idRegistro, idBondDetails, idLoanDetails, amountPar FROM registerdetails;

INSERT IGNORE INTO backup_loandetail_fix_20260113
SELECT ID, LoanID, Dues, payment, MonthlyFeeValue, State, PaymentDate FROM loandetail;

INSERT IGNORE INTO backup_abonodetail_fix_20260113
SELECT id, AbonoID, dues, payment, monthly, state, paymentDate FROM abonodetail;

-- Verificar que se guardó el backup
SELECT 'backup_registro_fix_20260113' AS tabla, COUNT(*) AS registros FROM backup_registro_fix_20260113
UNION ALL
SELECT 'backup_registerdetails_fix_20260113', COUNT(*) FROM backup_registerdetails_fix_20260113
UNION ALL
SELECT 'backup_loandetail_fix_20260113', COUNT(*) FROM backup_loandetail_fix_20260113
UNION ALL
SELECT 'backup_abonodetail_fix_20260113', COUNT(*) FROM backup_abonodetail_fix_20260113;

-- ************************************************************
-- PARTE 3: CORRECCIÓN DE DATOS
-- ************************************************************

-- Desactivar modo seguro de MySQL Workbench
SET SQL_SAFE_UPDATES = 0;

-- OPCIÓN A: Convertir registros mal aplicados en huérfanos para usar el botón "Reorganizar"
-- (Recomendado si quieres usar la interfaz de la aplicación)

-- 3A.1 Primero revertir los pagos en loandetail (préstamos)
UPDATE loandetail ld
INNER JOIN registerdetails rd ON rd.idLoanDetails = ld.ID
INNER JOIN registro r ON rd.idRegistro = r.id
SET
    ld.payment = GREATEST(0, ld.payment - rd.amountPar),
    ld.State = CASE
        WHEN GREATEST(0, ld.payment - rd.amountPar) = 0 THEN 'Pendiente'
        WHEN GREATEST(0, ld.payment - rd.amountPar) < ld.MonthlyFeeValue THEN 'Parcial'
        ELSE 'Pagado'
    END
WHERE rd.amountPar < (r.amount * 0.5)  -- Donde se aplicó menos del 50%
AND r.amount > 50;

-- 3A.2 Revertir los pagos en abonodetail (abonos)
UPDATE abonodetail ad
INNER JOIN registerdetails rd ON rd.idBondDetails = ad.id
INNER JOIN registro r ON rd.idRegistro = r.id
SET
    ad.payment = GREATEST(0, ad.payment - rd.amountPar),
    ad.state = CASE
        WHEN GREATEST(0, ad.payment - rd.amountPar) = 0 THEN 'Pendiente'
        WHEN GREATEST(0, ad.payment - rd.amountPar) < ad.monthly THEN 'Parcial'
        ELSE 'Pagado'
    END
WHERE rd.amountPar < (r.amount * 0.5)  -- Donde se aplicó menos del 50%
AND r.amount > 50;

-- 3A.3 Eliminar los detalles mal aplicados (dejan el registro huérfano)
DELETE rd FROM registerdetails rd
INNER JOIN registro r ON rd.idRegistro = r.id
WHERE rd.amountPar < (r.amount * 0.5)
AND r.amount > 50;

-- Reactivar modo seguro
SET SQL_SAFE_UPDATES = 1;

-- ************************************************************
-- PARTE 4: VERIFICACIÓN DESPUÉS DE LA CORRECCIÓN
-- ************************************************************

-- 4.1 Ver cuántos registros quedaron huérfanos (listos para reorganizar)
SELECT
    COUNT(*) AS registros_huerfanos,
    SUM(r.amount) AS monto_total_a_reorganizar
FROM registro r
LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
WHERE rd.id IS NULL;

-- 4.2 Ver detalle de registros huérfanos por empleado
SELECT
    r.empleado_id,
    e.fullName AS empleado,
    e.national_id AS dni,
    COUNT(r.id) AS cantidad_registros,
    SUM(r.amount) AS monto_total
FROM registro r
LEFT JOIN employees e ON e.employee_id = r.empleado_id
LEFT JOIN registerdetails rd ON rd.idRegistro = r.id
WHERE rd.id IS NULL
GROUP BY r.empleado_id, e.fullName, e.national_id
ORDER BY monto_total DESC;

-- 4.3 Ver cuotas que volvieron a estado Pendiente
SELECT 'PRESTAMOS' AS tipo, COUNT(*) AS cuotas_pendientes
FROM loandetail WHERE State = 'Pendiente'
UNION ALL
SELECT 'ABONOS', COUNT(*)
FROM abonodetail WHERE state = 'Pendiente';

-- ************************************************************
-- PARTE 5: DESPUÉS DE ESTO, USA EL BOTÓN "REORGANIZAR PAGOS"
--          EN LA APLICACIÓN PARA REDISTRIBUIR LOS MONTOS
-- ************************************************************

-- ************************************************************
-- PARTE 6: ROLLBACK (SI ALGO SALE MAL)
-- ************************************************************

-- Si necesitas revertir los cambios, ejecuta esto:

/*
-- 6.1 Restaurar registerdetails
DELETE FROM registerdetails;
INSERT INTO registerdetails (id, idRegistro, idBondDetails, idLoanDetails, amountPar)
SELECT id, idRegistro, idBondDetails, idLoanDetails, amountPar
FROM backup_registerdetails_fix_20260113;

-- 6.2 Restaurar loandetail
UPDATE loandetail ld
INNER JOIN backup_loandetail_fix_20260113 bk ON ld.ID = bk.ID
SET
    ld.payment = bk.payment,
    ld.State = bk.State;

-- 6.3 Restaurar abonodetail
UPDATE abonodetail ad
INNER JOIN backup_abonodetail_fix_20260113 bk ON ad.id = bk.id
SET
    ad.payment = bk.payment,
    ad.state = bk.state;
*/

-- ************************************************************
-- PARTE 7: LIMPIAR BACKUPS (DESPUÉS DE VERIFICAR QUE TODO ESTÁ BIEN)
-- ************************************************************

/*
-- Ejecutar solo cuando estés seguro de que todo funciona correctamente
DROP TABLE IF EXISTS backup_registro_fix_20260113;
DROP TABLE IF EXISTS backup_registerdetails_fix_20260113;
DROP TABLE IF EXISTS backup_loandetail_fix_20260113;
DROP TABLE IF EXISTS backup_abonodetail_fix_20260113;
*/
