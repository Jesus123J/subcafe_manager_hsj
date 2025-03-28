/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.descuento;

/**
 *
 * @author Jesus Gutierrez
 */

public class DatosPersona {

    private String dni; // Agregar campo DNI
    private String nombre;
    private double monto;
    private double prestamo;

    public DatosPersona(String nombre, String dni, double monto, double prestamo) {
        this.dni = dni;
        this.nombre = nombre;
        this.monto = monto;
        this.prestamo = prestamo;
    }

    public double getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(double prestamo) {
        this.prestamo = prestamo;
    }

    // Getters y Setters
    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public double getMonto() {
        return monto;
    }

    public void sumarMonto(double monto, double prestamo) {
        if (prestamo == 0.0) {
            this.monto += monto;
        } else {
            this.monto += monto + prestamo;
        }

    }
}
