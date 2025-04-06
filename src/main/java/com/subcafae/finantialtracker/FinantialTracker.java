/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.subcafae.finantialtracker;

import com.subcafae.finantialtracker.controller.ControllerMain;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.JOptionPane;

/**
 *
 * @author Jesus Gutierrez<
 */
public class FinantialTracker {

    private static final int PORT = 9999;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            new ControllerMain();
            System.out.println("Aplicación iniciada correctamente.");
        } catch (IOException e) {
           // JOptionPane.showMessageDialog(null, "Error unico (Port:9999) usado");
            System.out.println("Otra instancia ya está corriendo.");
            System.exit(0);
        }

    }
}
