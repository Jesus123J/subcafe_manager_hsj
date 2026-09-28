package com.subcafae.finantialtracker.data.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Cliente HTTP hacia el backend de la tienda Sub Cafe (Spring Boot).
 *
 * FinantialTracker NO lee las tablas de la tienda directamente: todo pasa por
 * la API REST (http://localhost:8080/api por defecto) con login JWT.
 *
 * Configuracion (en este orden): propiedades de sistema -Dtienda.api.url /
 * -Dtienda.api.user / -Dtienda.api.pass, archivo tienda-api.properties junto
 * al JAR (o en el classpath) y, si no hay nada, los valores por defecto del
 * backend en desarrollo (admin / admin123).
 */
public class TiendaApiClient {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private final String baseUrl;
    private final String usuario;
    private final String clave;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private volatile String token;

    public TiendaApiClient() {
        Properties p = cargarConfig();
        this.baseUrl = quitarSlash(System.getProperty("tienda.api.url",
                p.getProperty("tienda.api.url", "http://localhost:8080/api")));
        this.usuario = System.getProperty("tienda.api.user", p.getProperty("tienda.api.user", "admin"));
        this.clave = System.getProperty("tienda.api.pass", p.getProperty("tienda.api.pass", "admin123"));
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /** GET autenticado; devuelve el campo "data" del ApiResponse del backend. */
    public JsonNode get(String path) throws IOException {
        return get(path, true);
    }

    private JsonNode get(String path, boolean reintentarSi401) throws IOException {
        asegurarToken();
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> res = enviar(req);
        if (res.statusCode() == 401 && reintentarSi401) {
            token = null;                       // token vencido: volver a loguear
            return get(path, false);
        }
        return data(res, path);
    }

    /** Codifica un parametro de query. */
    public static String enc(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    // ───────────────────────── internos ─────────────────────────

    private synchronized void asegurarToken() throws IOException {
        if (token != null) return;
        String body = JSON.writeValueAsString(JSON.createObjectNode()
                .put("username", usuario).put("password", clave));
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/auth/login"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
        HttpResponse<String> res = enviar(req);
        JsonNode data = data(res, "/auth/login");
        token = data.path("token").asText(null);
        if (token == null || token.isBlank()) {
            throw new IOException("El backend no devolvio token para el usuario " + usuario);
        }
    }

    private HttpResponse<String> enviar(HttpRequest req) throws IOException {
        try {
            return http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Peticion interrumpida", e);
        } catch (IOException e) {
            throw new IOException("No se pudo conectar al backend de la tienda en " + baseUrl
                    + " (" + e.getMessage() + "). ¿Esta levantado (run.sh / run.cmd)?", e);
        }
    }

    private static JsonNode data(HttpResponse<String> res, String path) throws IOException {
        JsonNode root;
        try {
            root = res.body() == null || res.body().isBlank() ? JSON.nullNode() : JSON.readTree(res.body());
        } catch (IOException e) {
            throw new IOException("Respuesta no valida de " + path + " (HTTP " + res.statusCode() + ")");
        }
        if (res.statusCode() >= 400 || (root.has("success") && !root.path("success").asBoolean(true))) {
            String msg = root.path("message").asText("HTTP " + res.statusCode());
            throw new IOException(msg + " [" + path + "]");
        }
        return root.path("data");
    }

    private static Properties cargarConfig() {
        Properties p = new Properties();
        try {
            File externo = new File("tienda-api.properties");
            if (externo.exists()) {
                try (FileInputStream in = new FileInputStream(externo)) {
                    p.load(in);
                }
            } else {
                InputStream in = TiendaApiClient.class.getClassLoader().getResourceAsStream("tienda-api.properties");
                if (in != null) {
                    try (in) {
                        p.load(in);
                    }
                }
            }
        } catch (IOException ignored) {
            // sin archivo: se usan los valores por defecto
        }
        return p;
    }

    private static String quitarSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
