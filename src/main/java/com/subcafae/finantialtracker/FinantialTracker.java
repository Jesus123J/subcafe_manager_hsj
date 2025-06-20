/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.subcafae.finantialtracker;

import com.subcafae.finantialtracker.config.ConfigApp;
import com.subcafae.finantialtracker.controller.ControllerMain;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Jesus Gutierrez<
 */
public class FinantialTracker {

    private static final int PORT = 9999;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        String entorno = System.getProperty("env", "dev");
        ConfigApp.setEntorno(entorno);

        try {
            String path = "config-" + entorno + ".properties";
            Properties props = new Properties();
            props.load(new FileInputStream(path));
            ConfigApp.cargar(props);

            serverSocket = new ServerSocket(PORT);
            new ControllerMain();
            
            System.out.println("Aplicación iniciada correctamente.");
        } catch (Exception e) {
            System.out.println("Error -> " + e.getMessage());
            // JOptionPane.showMessageDialog(null, "Error unico (Port:9999) usado");
            System.out.println("Otra instancia ya está corriendo.");
            System.exit(0);
        }

    }
}
