package com.subcafae.finantialtracker.data.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.subcafae.finantialtracker.data.api.TiendaApiClient;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DEUDAS DE LA TIENDA (Sub Cafe) leidas SOLO a traves del backend REST de la
 * tienda (ver {@link TiendaApiClient}). FinantialTracker no toca las tablas
 * de la tienda: lo que se muestra es exactamente lo que expone la API.
 *
 * Endpoints usados:
 *   GET /deudores/resumen        totales
 *   GET /deudores?q=             trabajadores con deuda viva
 *   GET /deudores/{clienteId}    estado de cuenta (movimientos)
 *   GET /creditos                todos los consumos a credito (ultimas compras)
 *   GET /creditos/cierres        cierres mensuales
 *   GET /deudores/cierres/{id}   detalle de un cierre por trabajador
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

    private final TiendaApiClient api = new TiendaApiClient();
    private volatile String ultimoError;

    public String getBaseUrl() {
        return api.getBaseUrl();
    }

    /** Ultimo motivo por el que la tienda no respondio (para mostrar en pantalla). */
    public String getUltimoError() {
        return ultimoError;
    }

    /** true si el backend de la tienda responde y acepta el login. */
    public boolean tiendaInstalada() {
        try {
            api.get("/deudores/resumen");
            ultimoError = null;
            return true;
        } catch (IOException e) {
            ultimoError = e.getMessage();
            return false;
        }
    }

    public Resumen resumen() throws IOException {
        JsonNode d = api.get("/deudores/resumen");
        Resumen r = new Resumen();
        r.deudores = d.path("deudores").asLong(0);
        r.pendienteMes = dec(d.path("pendiente_mes"));
        r.deudaAcumulada = dec(d.path("deuda_acumulada"));
        r.deudaTotal = dec(d.path("deuda_total"));
        r.consumosMes = d.path("consumos_mes").asLong(0);
        return r;
    }

    /**
     * Trabajadores con deuda viva.
     * Columnas: cliente_id, dni, nombre, condicion, empleado_id, pendiente_mes,
     *           consumos_mes, ultimo_consumo, deuda_acumulada, deuda_total
     */
    public List<Object[]> deudores(String filtro) throws IOException {
        String f = filtro == null ? "" : filtro.trim();
        JsonNode lista = api.get("/deudores" + (f.isEmpty() ? "" : "?q=" + TiendaApiClient.enc(f)));
        List<Object[]> out = new ArrayList<>();
        for (JsonNode n : lista) {
            out.add(new Object[]{
                txt(n, "cliente_id"), txt(n, "dni"), txt(n, "nombre_completo"), txt(n, "condicion_laboral"),
                entero(n.path("empleado_id")), dec(n.path("pendiente_mes")), n.path("consumos_mes").asInt(0),
                fecha(n.path("ultimo_consumo")), dec(n.path("deuda_acumulada")), dec(n.path("deuda_total"))
            });
        }
        return out;
    }

    /**
     * Compras / consumos a credito de un trabajador (mas reciente primero).
     * Columnas: fecha, descripcion, monto, origen (Venta POS/Manual), estado, registrado_por, periodo
     */
    public List<Object[]> movimientos(String clienteId) throws IOException {
        JsonNode d = api.get("/deudores/" + clienteId);
        List<Object[]> out = new ArrayList<>();
        for (JsonNode m : d.path("movimientos")) {
            out.add(filaMovimiento(m));
        }
        return out;
    }

    /**
     * Ultimas compras a credito de toda la tienda (tiempo real).
     * Columnas: fecha, dni, trabajador, descripcion, monto, origen, estado
     */
    public List<Object[]> recientes(int limite) throws IOException {
        JsonNode lista = api.get("/creditos");
        List<JsonNode> nodos = new ArrayList<>();
        lista.forEach(nodos::add);
        nodos.sort(Comparator.comparing((JsonNode n) -> txt(n, "fecha")).reversed());
        List<Object[]> out = new ArrayList<>();
        for (JsonNode m : nodos) {
            if (out.size() >= limite) break;
            out.add(new Object[]{
                fecha(m.path("fecha")), txt(m, "clienteDni"), txt(m, "clienteNombre"), txt(m, "descripcion"),
                dec(m.path("monto")),
                m.hasNonNull("ventaId") ? "Venta POS" : "Manual",
                m.path("cerrado").asBoolean(false) ? "Cerrado" : "Pendiente"
            });
        }
        return out;
    }

    /**
     * Cierres mensuales de la tienda: lo que ira a planilla por trabajador.
     * Columnas: periodo, fecha_cierre, dni, trabajador, empleado_id, consumos, monto, estado FT
     */
    public List<Object[]> cierres() throws IOException {
        List<Object[]> out = new ArrayList<>();
        for (JsonNode c : api.get("/creditos/cierres")) {
            String periodo = String.format("%02d/%d", c.path("mes").asInt(), c.path("anio").asInt());
            Timestamp fechaCierre = fecha(c.path("fecha_cierre"));
            for (JsonNode d : api.get("/deudores/cierres/" + txt(c, "id"))) {
                String estado;
                if (d.hasNonNull("ft_abono_id")) {
                    estado = "Abono " + txt(d, "ft_solicitud") + " (" + txt(d, "ft_estado") + ")";
                } else if (d.hasNonNull("ft_error")) {
                    estado = "Error: " + txt(d, "ft_error");
                } else {
                    estado = "Solo en tienda (union desactivada)";
                }
                out.add(new Object[]{
                    periodo, fechaCierre, txt(d, "dni"), txt(d, "nombre_completo"), entero(d.path("empleado_id")),
                    d.path("cantidad_consumos").asInt(0), dec(d.path("monto")), estado
                });
            }
        }
        return out;
    }

    // ───────────────────────── helpers JSON ─────────────────────────

    private static Object[] filaMovimiento(JsonNode m) {
        String periodo = m.hasNonNull("periodoMes")
                ? String.format("%02d/%d", m.path("periodoMes").asInt(), m.path("periodoAnio").asInt()) : "";
        return new Object[]{
            fecha(m.path("fecha")), txt(m, "descripcion"), dec(m.path("monto")),
            m.hasNonNull("ventaId") ? "Venta POS" : "Manual",
            m.path("cerrado").asBoolean(false) ? "Cerrado" : "Pendiente",
            txt(m, "registradoPor"), periodo
        };
    }

    private static String txt(JsonNode n, String campo) {
        JsonNode v = n.path(campo);
        return v.isMissingNode() || v.isNull() ? null : v.asText();
    }

    private static Integer entero(JsonNode v) {
        return v.isMissingNode() || v.isNull() ? null : v.asInt();
    }

    private static BigDecimal dec(JsonNode v) {
        if (v.isMissingNode() || v.isNull()) return BigDecimal.ZERO;
        return v.isNumber() ? v.decimalValue() : new BigDecimal(v.asText("0"));
    }

    /** El backend manda fechas ISO-8601 con zona (America/Lima). */
    private static Timestamp fecha(JsonNode v) {
        if (v.isMissingNode() || v.isNull()) return null;
        String s = v.asText();
        try {
            return Timestamp.from(OffsetDateTime.parse(s).toInstant());
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(s));
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
