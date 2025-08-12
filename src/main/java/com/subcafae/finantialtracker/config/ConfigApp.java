/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.config;

import com.subcafae.finantialtracker.FinantialTracker;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jesus Gutierrez
 */
public class ConfigApp {

    public static final ConfigApp INSTANCE = new ConfigApp();

    public static final String APP_NAME = "Financial Tracker";
    public static final String VERSION = "1.0.0";

    private static String entorno;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    public static ConfigApp get() {
        return INSTANCE;
    }

    private static void getPropertiesConfigConnection(String entorno) {
        try {
            String path = "/properties/config-" + entorno + ".properties";
            Properties props = new Properties();
            props.load(get().getClass().getResourceAsStream(path));
            System.out.println("Lectura de data");
            ConfigApp.cargar(props);
        } catch (IOException ex) {
           
        }
    }

    public static void setEntorno(String env) {
        getPropertiesConfigConnection(env);
    }

    public static String getEntorno() {
        return entorno;
    }

    private static void cargar(Properties props) {
        dbUrl = props.getProperty("db.url");
        dbUser = props.getProperty("db.user");
        dbPass = props.getProperty("db.pass");
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbPass() {
        return dbPass;
    }
}
