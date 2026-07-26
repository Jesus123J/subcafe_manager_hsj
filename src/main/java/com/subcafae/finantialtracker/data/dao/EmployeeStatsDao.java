package com.subcafae.finantialtracker.data.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.subcafae.finantialtracker.data.conexion.ApiBackend;
import com.subcafae.finantialtracker.data.entity.AbonoTb;
import com.subcafae.finantialtracker.data.entity.EmployeeTb;
import com.subcafae.finantialtracker.data.entity.LoanTb;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Estadisticas agregadas para un empleado dado su DNI.
 *
 * Fuente primaria: el backend REST (GET /integracion/ft/empleados/{dni}/
 * estadisticas), que centraliza la conexion a las dos bases de datos.
 * Fallback: los DAOs con JDBC directo, para que la app siga funcionando
 * cuando el backend no esta corriendo.
 */
public class EmployeeStatsDao {

    /**
     * Devuelve null si no existe empleado con ese DNI.
     */
    public EmployeeStatsDto getStats(String dni) throws SQLException {
        try {
            return statsDesdeBackend(dni);
        } catch (ApiBackend.NoEncontradoException e) {
            return null; // el backend confirmo que el empleado no existe
        } catch (Exception e) {
            System.out.println("Backend no disponible, usando conexion directa: " + e.getMessage());
        }
        return statsDesdeDao(dni);
    }

    // ─── Via backend ───────────────────────────────────────────────────

    private EmployeeStatsDto statsDesdeBackend(String dni) throws IOException, InterruptedException {
        JsonObject resp = ApiBackend.get("/integracion/ft/empleados/" + dni + "/estadisticas");
        JsonObject data = resp.getAsJsonObject("data");
        JsonObject emp = data.getAsJsonObject("empleado");

        EmployeeTb empleado = new EmployeeTb();
        JsonElement id = emp.get("id");
        if (id != null && !id.isJsonNull()) {
            empleado.setEmployeeId(id.getAsInt());
        }
        empleado.setNationalId(textoDe(emp, "dni"));
        empleado.setFullName(textoDe(emp, "nombreCompleto"));
        empleado.setEmploymentStatus(textoDe(emp, "estadoEmpleo"));

        EmployeeStatsDto dto = new EmployeeStatsDto();
        dto.empleado = empleado;
        dto.totalLoans = data.get("totalPrestamos").getAsInt();
        dto.totalRefinancings = data.get("totalRefinanciamientos").getAsInt();
        dto.totalAbonos = data.get("totalAbonos").getAsInt();
        dto.totalAbonosMonto = data.get("montoTotalAbonos").getAsDouble();
        dto.loansByState = aMapaEntero(data.getAsJsonObject("prestamosPorEstado"));
        dto.abonosByState = aMapaEntero(data.getAsJsonObject("abonosPorEstado"));
        dto.loansByMonth = aMapaEntero(data.getAsJsonObject("prestamosPorMes"));
        dto.abonosByMonth = aMapaEntero(data.getAsJsonObject("abonosPorMes"));
        return dto;
    }

    private static String textoDe(JsonObject obj, String campo) {
        JsonElement v = obj.get(campo);
        return v == null || v.isJsonNull() ? null : v.getAsString();
    }

    /** Gson preserva el orden de insercion — los buckets por mes llegan cronologicos. */
    private static Map<String, Integer> aMapaEntero(JsonObject obj) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        if (obj != null) {
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                mapa.put(e.getKey(), e.getValue().getAsInt());
            }
        }
        return mapa;
    }

    // ─── Fallback: JDBC directo (logica original) ──────────────────────

    private EmployeeStatsDto statsDesdeDao(String dni) throws SQLException {
        Optional<EmployeeTb> empOpt = new EmployeeDao().findAll().stream()
                .filter(e -> dni.equalsIgnoreCase(e.getNationalId()))
                .findFirst();
        if (empOpt.isEmpty()) {
            return null;
        }
        EmployeeTb emp = empOpt.get();
        String empIdStr = String.valueOf(emp.getEmployeeId());
        String dniNorm = dni.trim();

        EmployeeStatsDto dto = new EmployeeStatsDto();
        dto.empleado = emp;

        // getAllLoans (no findLoansByEmployeeId): esa query filtra
        // State='Aceptado' y StateLoan='Pendiente', aqui interesan todos.
        // OJO: loan.EmployeeID guarda el DNI (ver ModelManageLoan
        // insertDataLoan -> getNationalId()), no el id numerico.
        List<LoanTb> loans = new LoanDao().getAllLoans().stream()
                .filter(l -> {
                    String eid = l.getEmployeeId() == null ? "" : l.getEmployeeId().trim();
                    return eid.equalsIgnoreCase(dniNorm) || eid.equalsIgnoreCase(empIdStr);
                })
                .collect(Collectors.toList());
        dto.totalLoans = loans.size();
        dto.totalRefinancings = (int) loans.stream()
                .filter(l -> l.getRefinanceParentId() != null && l.getRefinanceParentId() > 0)
                .count();
        dto.loansByState = loans.stream()
                .filter(l -> l.getState() != null)
                .collect(Collectors.groupingBy(LoanTb::getState,
                        Collectors.summingInt(l -> 1)));
        // CreatedAt es java.sql.Date: toInstant() lanza
        // UnsupportedOperationException, usar toLocalDate() directo.
        dto.loansByMonth = agrupacionMensual(
                loans.stream()
                        .map(l -> l.getCreatedAt() == null ? null : l.getCreatedAt().toLocalDate())
                        .collect(Collectors.toList()));

        // El abono puede tener Employee_id numerico o (data legacy) el DNI.
        List<AbonoTb> abonos = new AbonoDao().findAllAbonos().stream()
                .filter(a -> {
                    String eid = a.getEmployeeId() == null ? "" : a.getEmployeeId().trim();
                    return eid.equalsIgnoreCase(empIdStr) || eid.equalsIgnoreCase(dniNorm);
                })
                .collect(Collectors.toList());
        dto.totalAbonos = abonos.size();
        dto.totalAbonosMonto = abonos.stream()
                .mapToDouble(a -> a.getMonthly() == null ? 0.0 : a.getMonthly())
                .sum();
        dto.abonosByState = abonos.stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(AbonoTb::getStatus,
                        Collectors.summingInt(a -> 1)));
        dto.abonosByMonth = agrupacionMensual(
                abonos.stream()
                        .map(a -> parseDate(a.getCreatedAt()))
                        .collect(Collectors.toList()));

        return dto;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            // formato comun "yyyy-MM-dd"
            return LocalDate.parse(s.substring(0, Math.min(10, s.length())));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Agrupa fechas en los ultimos 12 meses calendario, devolviendo un
     * LinkedHashMap (mantiene orden insertado) con keys "MMM yyyy" (es).
     */
    private static Map<String, Integer> agrupacionMensual(List<LocalDate> fechas) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es"));
        Map<String, Integer> resultado = new LinkedHashMap<>();
        YearMonth ahora = YearMonth.now();
        // Inicializar 12 meses en 0 para que el grafico muestre todos los buckets.
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = ahora.minusMonths(i);
            resultado.put(ym.atDay(1).format(fmt), 0);
        }

        for (LocalDate f : fechas) {
            if (f == null) continue;
            YearMonth ym = YearMonth.from(f);
            // solo cuenta si esta dentro de los ultimos 12 meses
            if (ym.isBefore(ahora.minusMonths(11)) || ym.isAfter(ahora)) continue;
            String key = ym.atDay(1).format(fmt);
            resultado.merge(key, 1, Integer::sum);
        }
        return resultado;
    }

    /**
     * Holder de datos agregados para un empleado. Los charts y cards
     * de UI consumen este objeto directamente.
     */
    public static class EmployeeStatsDto {
        public EmployeeTb empleado;
        public int totalLoans;
        public int totalRefinancings;
        public int totalAbonos;
        public double totalAbonosMonto;
        public Map<String, Integer> loansByState = new LinkedHashMap<>();
        public Map<String, Integer> abonosByState = new LinkedHashMap<>();
        public Map<String, Integer> loansByMonth = new LinkedHashMap<>();
        public Map<String, Integer> abonosByMonth = new LinkedHashMap<>();
    }
}
