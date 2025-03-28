/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.HistoryPayment;

import java.sql.Timestamp;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelPaymentAndLoan {
    private int id;
    private String codigo;
    private Timestamp fechaRegistro;
    private double amount;
    private String prestamos;
    private String bonos;

    public ModelPaymentAndLoan() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(String prestamo) {
        this.prestamos = prestamo;
    }

    public String getBonos() {
        return bonos;
    }

    public void setBonos(String bonos) {
        this.bonos = bonos;
    }

  
}
