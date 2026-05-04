package com.subcafae.finantialtracker.data.conexion;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JOptionPane;

public class Conexion {

    private static Connection connection = null;

    // Configuracion del servidor remoto (primario)
    private static final String URL_REMOTO = "jdbc:mariadb://192.168.97.10:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&connectTimeout=3000&socketTimeout=30000&autoReconnect=true";
    private static final String USER_REMOTO = "root";
    private static final String PASSWORD_REMOTO = "123456789";

    // Configuracion localhost (fallback)
    private static final String URL_LOCAL = "jdbc:mariadb://localhost:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&connectTimeout=3000&socketTimeout=30000&autoReconnect=true";
    private static final String USER_LOCAL = "root";
    private static final String PASSWORD_LOCAL = "123456";

    // Configuracion localhost 2 (fallback)
    private static final String URL_LOCAL_2 = "jdbc:mariadb://localhost:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&useSSL=false&connectTimeout=3000&socketTimeout=30000&autoReconnect=true";
    private static final String USER_LOCAL_2 = "root";
    private static final String PASSWORD_LOCAL_2 = "123456789";

    private static final int MAX_REINTENTOS = 3;
    private static final int ESPERA_ENTRE_REINTENTOS_MS = 2000;

    /**
     * Obtiene la conexion activa. Si la conexion murio o no existe, reconecta automaticamente.
     */
    public static synchronized Connection getConnection() {
        // Si la conexion existe y sigue viva, reutilizarla
        if (estaViva(connection)) {
            return connection;
        }

        // Conexion muerta o null -> reconectar con reintentos
        System.out.println("Conexion perdida o inexistente. Reconectando...");
        connection = null;

        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            System.out.println("Intento de reconexion " + intento + "/" + MAX_REINTENTOS);
            connection = crearNuevaConexion();

            if (connection != null) {
                System.out.println("Reconexion exitosa en intento " + intento);
                return connection;
            }

            if (intento < MAX_REINTENTOS) {
                try {
                    Thread.sleep(ESPERA_ENTRE_REINTENTOS_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Todos los reintentos fallaron
        JOptionPane.showMessageDialog(null,
                "No se pudo conectar a la base de datos.\nVerifique que el servidor este activo.",
                "Error de Conexion", JOptionPane.ERROR_MESSAGE);

        return null;
    }

    /**
     * Verifica si una conexion sigue viva y funcional.
     */
    private static boolean estaViva(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Intenta crear una nueva conexion probando todos los origenes disponibles.
     */
    private static Connection crearNuevaConexion() {
        Connection conn;

        // 1. Servidor remoto
        conn = intentarConexion(URL_REMOTO, USER_REMOTO, PASSWORD_REMOTO, "servidor remoto (192.168.97.10)");
        if (conn != null) return conn;

        // 2. Localhost
        conn = intentarConexion(URL_LOCAL, USER_LOCAL, PASSWORD_LOCAL, "localhost");
        if (conn != null) return conn;

        // 3. Localhost 2
        conn = intentarConexion(URL_LOCAL_2, USER_LOCAL_2, PASSWORD_LOCAL_2, "localhost_2");
        if (conn != null) return conn;

        // 4. Archivo de propiedades externo
        conn = intentarConexionDesdeProperties();
        if (conn != null) return conn;

        return null;
    }

    private static Connection intentarConexion(String url, String user, String password, String descripcion) {
        try {
            System.out.println("Conectando a " + descripcion + "...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexion exitosa a " + descripcion);
            return conn;
        } catch (SQLException e) {
            System.out.println("Error conectando a " + descripcion + ": " + e.getMessage());
            return null;
        }
    }

    private static Connection intentarConexionDesdeProperties() {
        try {
            Properties props = new Properties();

            File externalFile = new File("database.properties");
            if (externalFile.exists()) {
                try (FileInputStream fis = new FileInputStream(externalFile)) {
                    props.load(fis);
                }
            } else {
                InputStream input = Conexion.class.getClassLoader().getResourceAsStream("database.properties");
                if (input != null) {
                    try (input) {
                        props.load(input);
                    }
                } else {
                    return null;
                }
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            return intentarConexion(url, user, password, "archivo de propiedades");
        } catch (Exception e) {
            System.out.println("Error cargando configuracion: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("Conexion -> " + Conexion.getConnection());
    }
}
