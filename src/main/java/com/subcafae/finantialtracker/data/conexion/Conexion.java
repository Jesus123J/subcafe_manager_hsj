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

    static {
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
                    throw new RuntimeException("No se encontró database.properties");
                }
            }

            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error -> " + e.getMessage());
            JOptionPane.showMessageDialog(null, "No hay conexion , comuniquese con el administrador");
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando configuración: " + e.getMessage());
            System.exit(0);
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
