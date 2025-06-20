/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.config;

import java.util.Properties;

/**
 *
 * @author Jesus Gutierrez
 */
public class ConfigApp {

    public static final String APP_NAME = "Financial Tracker";
    public static final String VERSION = "1.0.0";

    private static String entorno;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    public static void setEntorno(String env) {
        entorno = env;
    }

    public static String getEntorno() {
        return entorno;
    }

    public static void cargar(Properties props) {
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
