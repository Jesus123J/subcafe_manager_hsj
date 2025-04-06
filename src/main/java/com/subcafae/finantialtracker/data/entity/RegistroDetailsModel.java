/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

import java.math.BigDecimal;

 public class RegistroDetailsModel {
    private String fechaRegistro;
    private String codigo;
    private BigDecimal amount;
    private String conceptLoan;
    private String conceptBond;
    private String fechaVLoan;
    private String fechaVBond;
    private BigDecimal amountPar;
    private BigDecimal montoLoan;
    private BigDecimal montoBond;

    // Constructor vacío
    public RegistroDetailsModel() {}

    // Constructor con parámetros
    public RegistroDetailsModel(String fechaRegistro, String codigo, BigDecimal amount, String conceptLoan, 
                                String conceptBond, String fechaVLoan, String fechaVBond, 
                                BigDecimal amountPar, BigDecimal montoLoan, BigDecimal montoBond) {
        this.fechaRegistro = fechaRegistro;
        this.codigo = codigo;
        this.amount = amount;
        this.conceptLoan = conceptLoan;
        this.conceptBond = conceptBond;
        this.fechaVLoan = fechaVLoan;
        this.fechaVBond = fechaVBond;
        this.amountPar = amountPar;
        this.montoLoan = montoLoan;
        this.montoBond = montoBond;
    }

    // Getters y Setters
    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getConceptLoan() {
        return conceptLoan;
    }

    public void setConceptLoan(String conceptLoan) {
        this.conceptLoan = conceptLoan;
    }

    public String getConceptBond() {
        return conceptBond;
    }

    public void setConceptBond(String conceptBond) {
        this.conceptBond = conceptBond;
    }

    public String getFechaVLoan() {
        return fechaVLoan;
    }

    public void setFechaVLoan(String fechaVLoan) {
        this.fechaVLoan = fechaVLoan;
    }

    public String getFechaVBond() {
        return fechaVBond;
    }

    public void setFechaVBond(String fechaVBond) {
        this.fechaVBond = fechaVBond;
    }

    public BigDecimal getAmountPar() {
        return amountPar;
    }

    public void setAmountPar(BigDecimal amountPar) {
        this.amountPar = amountPar;
    }

    public BigDecimal getMontoLoan() {
        return montoLoan;
    }

    public void setMontoLoan(BigDecimal montoLoan) {
        this.montoLoan = montoLoan;
    }

    public BigDecimal getMontoBond() {
        return montoBond;
    }

    public void setMontoBond(BigDecimal montoBond) {
        this.montoBond = montoBond;
    }

    @Override
    public String toString() {
        return "RegistroDetailsModel{" +
                "fechaRegistro='" + fechaRegistro + '\'' +
                ", codigo='" + codigo + '\'' +
                ", amount=" + amount +
                ", conceptLoan='" + conceptLoan + '\'' +
                ", conceptBond='" + conceptBond + '\'' +
                ", fechaVLoan='" + fechaVLoan + '\'' +
                ", fechaVBond='" + fechaVBond + '\'' +
                ", amountPar=" + amountPar +
                ", montoLoan=" + montoLoan +
                ", montoBond=" + montoBond +
                '}';
    }
}