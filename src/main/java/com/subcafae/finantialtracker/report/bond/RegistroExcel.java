package com.subcafae.finantialtracker.report.bond;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Jesus Gutierrez
 */
public class RegistroExcel {
    private String dni;
    private String datos;
    private double monto;
    private int cuotas;

    // Constructor
    public RegistroExcel(String dni, String datos, double monto, int cuotas) {
        this.dni = dni;
        this.datos = datos;
        this.monto = monto;
        this.cuotas = cuotas;
    }

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getDatos() { return datos; }
    public void setDatos(String datos) { this.datos = datos; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public int getCuotas() { return cuotas; }
    public void setCuotas(int cuotas) { this.cuotas = cuotas; }

    @Override
    public String toString() {
        return "DNI: " + dni + " | Datos: " + datos + " | Monto: " + monto + " | Cuotas: " + cuotas;
    }
}