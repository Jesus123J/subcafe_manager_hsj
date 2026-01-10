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

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static Connection connection = null;

    // Configuración del servidor remoto (primario)
    private static final String URL_REMOTO = "jdbc:mariadb://192.168.97.10:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&collation=utf8mb4_0900_ai_ci&useSSL=false";
    private static final String USER_REMOTO = "root";
    private static final String PASSWORD_REMOTO = "123456789";

    // Configuración localhost (fallback)
    private static final String URL_LOCAL = "jdbc:mariadb://localhost:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&collation=utf8mb4_0900_ai_ci&useSSL=false";
    private static final String USER_LOCAL = "root";
    private static final String PASSWORD_LOCAL = "123456";

    // Configuración localhost (fallback)
    private static final String URL_LOCAL_2 = "jdbc:mariadb://localhost:3306/FinancialTracker1?useUnicode=true&characterEncoding=UTF-8&collation=utf8mb4_0900_ai_ci&useSSL=false";
    private static final String USER_LOCAL_2 = "root";
    private static final String PASSWORD_LOCAL_2 = "123456789";

    static {
        // Intentar conexión con servidor remoto primero
        connection = intentarConexion(URL_REMOTO, USER_REMOTO, PASSWORD_REMOTO, "servidor remoto (192.168.97.10)");

        // Si falla, intentar con localhost
        if (connection == null) {
            System.out.println("Intentando conexión con localhost...");
            connection = intentarConexion(URL_LOCAL, USER_LOCAL, PASSWORD_LOCAL, "localhost");
        }

        if (connection == null) {
            System.out.println("Intentando conexión con localhost...");
            connection = intentarConexion(URL_LOCAL_2, USER_LOCAL_2, PASSWORD_LOCAL_2, "localhost_2");
        }
        // Si ambas fallan, intentar con archivo de propiedades externo
        if (connection == null) {
            System.out.println("Intentando conexión con archivo de propiedades...");
            connection = intentarConexionDesdeProperties();
        }

        // Si todas las conexiones fallan, mostrar error y salir
        if (connection == null) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a ninguna base de datos.\n" +
                            "Verifique que el servidor esté activo.",
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private static Connection intentarConexion(String url, String user, String password, String descripcion) {
        try {
            System.out.println("Conectando a " + descripcion + "...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✓ Conexión exitosa a " + descripcion);
            return conn;
        } catch (SQLException e) {
            System.out.println("✗ Error conectando a " + descripcion + ": " + e.getMessage());
            return null;
        }
    }

    private static Connection intentarConexionDesdeProperties() {
        try {
            Properties props = new Properties();

            File externalFile = new File("database.properties");
            if (externalFile.exists()) {
                props.load(new FileInputStream(externalFile));
            } else {
                InputStream input = Conexion.class.getClassLoader().getResourceAsStream("database.properties");
                if (input != null) {
                    props.load(input);
                } else {
                    return null;
                }
            }

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            return intentarConexion(URL, USER, PASSWORD, "archivo de propiedades");
        } catch (Exception e) {
            System.out.println("Error cargando configuración: " + e.getMessage());
            return null;
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            JOptionPane.showMessageDialog(null, "Error de conexión", "", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        return connection;
    }

    public static void main(String[] args) {
        System.out.println("Conexion -> " + Conexion.getConnection());
    }
}
