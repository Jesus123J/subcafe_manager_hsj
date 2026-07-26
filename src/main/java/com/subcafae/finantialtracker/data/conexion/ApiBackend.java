package com.subcafae.finantialtracker.data.conexion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

/**
 * Cliente HTTP hacia el backend Spring Boot (Proyeto_2026/backend), que es
 * quien se conecta a las bases de datos. La app de escritorio consume la
 * API REST en vez de ir directo con los DAOs; los DAOs quedan como fallback
 * cuando el backend no esta corriendo (ver cada caller).
 *
 * Configuracion via archivo "backend.properties" al lado del JAR (o en
 * resources), con defaults de desarrollo:
 *   backend.url=http://localhost:8080/api
 *   backend.user=admin
 *   backend.pass=admin123
 *
 * Autentica con POST /auth/login (JWT) y reintenta el login una vez si el
 * token expira (401). Timeouts cortos para que el fallback a DAO sea rapido
 * si el backend esta caido.
 */
public final class ApiBackend {

    /** 404 del backend: el recurso no existe (distinto de "backend caido"). */
    public static class NoEncontradoException extends IOException {
        public NoEncontradoException(String msg) {
            super(msg);
        }
    }

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static volatile String baseUrl;
    private static volatile String usuario;
    private static volatile String password;
    private static volatile String token;

    // ─── Circuit breaker ───────────────────────────────────────────────
    // Si el backend no responde (apagado/red caida), no tiene sentido que
    // CADA llamada de DAO espere su timeout antes de caer al JDBC directo:
    // se marca la caida, se avisa al usuario UNA sola vez con un dialogo,
    // y durante REINTENTO_MS los DAOs fallan instantaneo al fallback.
    // Pasado ese tiempo se vuelve a probar; si revive, se rearma el aviso.
    private static final long REINTENTO_MS = 30_000;
    private static volatile long caidoDesde = 0;
    private static volatile boolean avisoMostrado = false;

    private ApiBackend() {
    }

    private static void verificarCircuito() throws IOException {
        long desde = caidoDesde;
        if (desde > 0 && System.currentTimeMillis() - desde < REINTENTO_MS) {
            throw new IOException("servicio backend apagado (en espera de reintento)");
        }
    }

    private static void registrarCaida(Exception causa) {
        caidoDesde = System.currentTimeMillis();
        if (!avisoMostrado) {
            avisoMostrado = true;
            javax.swing.SwingUtilities.invokeLater(() ->
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "EL SERVICIO DEL SISTEMA (BACKEND) ESTA APAGADO O NO RESPONDE.\n\n"
                            + "La aplicacion seguira funcionando con conexion directa\n"
                            + "a la base de datos mientras tanto.\n\n"
                            + "Avise a soporte para encender el servicio (" + baseUrl + ").",
                            "SERVICIO APAGADO",
                            javax.swing.JOptionPane.WARNING_MESSAGE));
        }
    }

    private static void registrarVivo() {
        caidoDesde = 0;
        // rearmar el aviso: si vuelve a caerse mas tarde, se notifica de nuevo
        avisoMostrado = false;
    }

    private static synchronized void cargarConfig() {
        if (baseUrl != null) return;
        Properties props = new Properties();
        try {
            File externo = new File("backend.properties");
            if (externo.exists()) {
                try (FileInputStream fis = new FileInputStream(externo)) {
                    props.load(fis);
                }
            } else {
                InputStream input = ApiBackend.class.getClassLoader()
                        .getResourceAsStream("backend.properties");
                if (input != null) {
                    try (input) {
                        props.load(input);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer backend.properties: " + e.getMessage());
        }
        baseUrl = props.getProperty("backend.url", "http://localhost:8080/api");
        usuario = props.getProperty("backend.user", "admin");
        password = props.getProperty("backend.pass", "admin123");
    }

    private static synchronized String obtenerToken() throws IOException, InterruptedException {
        if (token != null) return token;
        cargarConfig();
        verificarCircuito();
        JsonObject cred = new JsonObject();
        cred.addProperty("username", usuario);
        cred.addProperty("password", password);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .timeout(Duration.ofSeconds(4))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(cred)))
                .build();
        HttpResponse<String> resp;
        try {
            resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // no hubo respuesta: el servicio esta apagado o inalcanzable
            registrarCaida(e);
            throw e;
        }
        registrarVivo(); // respondio (aunque sea con error HTTP): esta encendido
        if (resp.statusCode() != 200) {
            throw new IOException("Login al backend fallo (HTTP " + resp.statusCode() + ")");
        }
        JsonObject body = GSON.fromJson(resp.body(), JsonObject.class);
        token = body.getAsJsonObject("data").get("token").getAsString();
        return token;
    }

    /**
     * Roundtrip liviano y sin autenticacion (swagger docs) para saber si el
     * backend responde. NO dispara el aviso generico de servicio apagado —
     * el caller decide que mostrar (lo usa el login en modo estricto).
     */
    public static boolean servicioDisponible() {
        try {
            cargarConfig();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v3/api-docs"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GET autenticado. Devuelve el JSON completo del ApiResponse del backend.
     * Lanza NoEncontradoException en 404; IOException en cualquier otro fallo
     * (backend caido, 5xx, etc) para que el caller haga fallback a DAO.
     */
    public static JsonObject get(String path) throws IOException, InterruptedException {
        return enviar("GET", path, null);
    }

    /** POST autenticado con body JSON (puede ser null). */
    public static JsonObject post(String path, JsonObject body) throws IOException, InterruptedException {
        return enviar("POST", path, body);
    }

    /** PUT autenticado con body JSON (puede ser null). */
    public static JsonObject put(String path, JsonObject body) throws IOException, InterruptedException {
        return enviar("PUT", path, body);
    }

    /** DELETE autenticado. */
    public static JsonObject delete(String path) throws IOException, InterruptedException {
        return enviar("DELETE", path, null);
    }

    private static JsonObject enviar(String metodo, String path, JsonObject body)
            throws IOException, InterruptedException {
        cargarConfig();
        verificarCircuito();
        long inicio = System.currentTimeMillis();
        HttpResponse<String> resp = enviarRequest(metodo, path, body, obtenerToken());
        if (resp.statusCode() == 401) {
            // token vencido: relogin una vez
            synchronized (ApiBackend.class) {
                token = null;
            }
            resp = enviarRequest(metodo, path, body, obtenerToken());
        }
        // Log de consumo: cada llamada del escritorio a la API queda visible
        // en consola con su estado y duracion.
        System.out.println("[API] " + metodo + " " + path + " -> HTTP "
                + resp.statusCode() + " (" + (System.currentTimeMillis() - inicio) + " ms)");
        if (resp.statusCode() == 404) {
            throw new NoEncontradoException("No encontrado: " + path);
        }
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Backend respondio HTTP " + resp.statusCode()
                    + " en " + metodo + " " + path);
        }
        return GSON.fromJson(resp.body(), JsonObject.class);
    }

    private static HttpResponse<String> enviarRequest(String metodo, String path,
            JsonObject body, String jwt) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher pub = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(GSON.toJson(body));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + jwt)
                .header("Content-Type", "application/json")
                .method(metodo, pub)
                .build();
        HttpResponse<String> resp;
        try {
            resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            // no hubo respuesta: el servicio esta apagado o inalcanzable
            registrarCaida(e);
            throw e;
        }
        registrarVivo(); // respondio: el servicio esta encendido
        return resp;
    }
}
