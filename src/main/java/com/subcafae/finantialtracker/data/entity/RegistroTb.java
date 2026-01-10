/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

import java.sql.Timestamp;

public class RegistroTb {

    private int id;
    private String codigo;
    private int empleadoId;
    private Timestamp fechaRegistro;
    public Double amount;

    public RegistroTb() {
    }

    public RegistroTb(int empleadoId, Double amount) {
        this.empleadoId = empleadoId;
        this.amount = amount;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public int getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(int empleadoId) {
        this.empleadoId = empleadoId;
    }

    @Override
    public String toString() {
        return "RegistroTb{" + "id=" + id + ", codigo=" + codigo + ", empleadoId=" + empleadoId + ", fechaRegistro=" + fechaRegistro + ", amount=" + amount + '}';
    }
    
    
}