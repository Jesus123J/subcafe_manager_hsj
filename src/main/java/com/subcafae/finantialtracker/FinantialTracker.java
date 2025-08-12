/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.subcafae.finantialtracker;

import com.subcafae.finantialtracker.config.ConfigApp;
import com.subcafae.finantialtracker.controller.ControllerMain;
import com.subcafae.finantialtracker.model.ModelMain;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.Properties;

/**
 *
 * @author Jesus Gutierrez<
 */
public class FinantialTracker {

    private static final int PORT = 9999;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            String entorno = System.getProperty("env", "dev");
            ConfigApp.setEntorno(entorno);
            System.out.println("Component");
            serverSocket = new ServerSocket(PORT);
            new ControllerMain();
            System.out.println("Aplicación iniciada correctamente.");
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            System.exit(0);
        }

    }
}
