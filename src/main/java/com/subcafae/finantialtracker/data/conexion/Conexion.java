/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.conexion;

import com.subcafae.finantialtracker.config.ConfigApp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */

public class Conexion {
    private static Connection connection = null;

    static {
        try {
            String url = ConfigApp.getDbUrl();
            String user = ConfigApp.getDbUser();
            String pass = ConfigApp.getDbPass();

            connection = DriverManager.getConnection(url, user, pass);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error de conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}