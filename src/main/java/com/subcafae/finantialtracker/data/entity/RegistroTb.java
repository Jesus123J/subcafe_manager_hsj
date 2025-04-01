/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

public class RegistroTb {
   
    private int empleadoId;
    public Double amount;

    public RegistroTb(int empleadoId, Double amount) {
        this.empleadoId = empleadoId;
        this.amount = amount;
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
        return "RegistroTb{" + "empleadoId=" + empleadoId + ", amount=" + amount + '}';
    }
    
    
}