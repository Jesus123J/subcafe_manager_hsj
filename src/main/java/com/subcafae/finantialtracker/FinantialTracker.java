/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.subcafae.finantialtracker;

import com.subcafae.finantialtracker.config.ConfigApp;
import com.subcafae.finantialtracker.controller.ControllerMain;
import java.net.ServerSocket;
import javax.swing.UIManager;

/**
 *
 * @author Jesus Gutierrez<
 */
public class FinantialTracker {

    private static final int PORT = 9999;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            // Configurar Look and Feel moderno
            configurarLookAndFeel();

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

    private static void configurarLookAndFeel() {
        try {
            // Intentar usar FlatLaf si está disponible
            Class<?> flatRobotoClass = Class.forName("com.formdev.flatlaf.fonts.roboto.FlatRobotoFont");
            Class<?> flatMacDarkClass = Class.forName("com.formdev.flatlaf.themes.FlatMacDarkLaf");

            // Instalar fuente Roboto
            flatRobotoClass.getMethod("install").invoke(null);

            // Configurar tema FlatMacDarkLaf
            flatMacDarkClass.getMethod("setup").invoke(null);

            // Obtener el nombre de la familia de fuentes
            String fontFamily = (String) flatRobotoClass.getField("FAMILY").get(null);
            UIManager.put("defaultFont", new java.awt.Font(fontFamily, java.awt.Font.PLAIN, 13));

            System.out.println("FlatLaf configurado correctamente.");
        } catch (Exception e) {
            // Si FlatLaf no está disponible, usar el Look and Feel del sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("Usando Look and Feel del sistema.");
            } catch (Exception ex) {
                System.out.println("No se pudo configurar el Look and Feel: " + ex.getMessage());
            }
        }
    }
}
