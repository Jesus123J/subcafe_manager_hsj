-- ============================================================
-- SCRIPT: DIAGNÓSTICO Y CORRECCIÓN DE COLUMNA STATE
-- ============================================================
-- Problema: "Data truncated for column 'State'"
-- Causa: La columna State puede ser ENUM o VARCHAR muy corto
-- Fecha: 2026-01-13
-- ============================================================

-- ************************************************************
-- PARTE 1: DIAGNÓSTICO - Ver estructura actual de las columnas
-- ************************************************************

-- 1.1 Ver estructura de la columna State en loandetail
SHOW COLUMNS FROM loandetail WHERE Field = 'State';

-- 1.2 Ver estructura de la columna State en abonodetail
SHOW COLUMNS FROM abonodetail WHERE Field = 'State';

-- 1.3 Ver valores actuales únicos en loandetail
SELECT DISTINCT State, COUNT(*) as cantidad FROM loandetail GROUP BY State;

-- 1.4 Ver valores actuales únicos en abonodetail
SELECT DISTINCT State, COUNT(*) as cantidad FROM abonodetail GROUP BY State;

-- ************************************************************
-- PARTE 2: CORRECCIÓN - Modificar columnas para aceptar todos los valores
-- ************************************************************

-- OPCIÓN A: Si la columna es ENUM, cambiar a VARCHAR(20)
-- Ejecutar según lo que muestre el diagnóstico

-- 2.1 Modificar loandetail.State a VARCHAR(20)
ALTER TABLE loandetail MODIFY COLUMN State VARCHAR(20) DEFAULT 'Pendiente';

-- 2.2 Modificar abonodetail.State a VARCHAR(20)
ALTER TABLE abonodetail MODIFY COLUMN State VARCHAR(20) DEFAULT 'Pendiente';

-- ************************************************************
-- PARTE 3: VERIFICACIÓN
-- ************************************************************

-- 3.1 Verificar que la columna ahora es VARCHAR
SHOW COLUMNS FROM loandetail WHERE Field = 'State';
SHOW COLUMNS FROM abonodetail WHERE Field = 'State';

-- 3.2 Probar actualización con valor 'Pendiente'
-- UPDATE loandetail SET State = 'Pendiente' WHERE ID = 1; -- Descomentar para probar
