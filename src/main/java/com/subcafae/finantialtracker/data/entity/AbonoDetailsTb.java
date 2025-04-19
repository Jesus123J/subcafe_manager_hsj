/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

/**
 *
 * @author Jesus Gutierrez
 */
public class AbonoDetailsTb {

    private Long id;
    private int abonoID;
    private int dues;
    private double monthly;
    private double payment;
    private String paymentDate;
    private String state;
    private String createdBy;
    private String createdAt;
    private String modifiedBy;
    private String modifiedAt;
    private Double monto;

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAbonoID() {
        return abonoID;
    }

    public void setAbonoID(int abonoID) {
        this.abonoID = abonoID;
    }

    public int getDues() {
        return dues;
    }

    public void setDues(int dues) {
        this.dues = dues;
    }

    public double getMonthly() {
        return monthly;
    }

    public void setMonthly(double monthly) {
        this.monthly = monthly;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String toString() {
        return "AbonoDetailsTb{" + "id=" + id + ", abonoID=" + abonoID + ", dues=" + dues + ", monthly=" + monthly + ", payment=" + payment + ", paymentDate=" + paymentDate + ", state=" + state + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", modifiedBy=" + modifiedBy + ", modifiedAt=" + modifiedAt + '}';
    }
    
    
    
    
    
}
