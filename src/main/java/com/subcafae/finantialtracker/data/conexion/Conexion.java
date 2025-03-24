/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez
 */
public class Conexion {

    private static final String URL = "jdbc:mariadb://localhost:3306/FinancialTracker1";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection = null;

    static {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "No hay conexion , comuniquese con el administrador");
            System.exit(0);
            //  throw new RuntimeException("Error al conectar a la base de datos", e);
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            JOptionPane.showMessageDialog(null, "Error de conexión" , "" , JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
        return connection;
    }

    public static void main(String[] args) {
        System.out.println("Conexion -> " + Conexion.getConnection());
    }
}
