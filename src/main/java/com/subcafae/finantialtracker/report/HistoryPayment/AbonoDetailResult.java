/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.HistoryPayment;

/**
 *
 * @author Jesus Gutierrez
 */
public class AbonoDetailResult {
    
    private String paymentDate;
    private String description;
    private int abonoDues;
    private int abonodetailDues;
    private double monthly;
    private double payment;

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    // Getters y Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAbonoDues() {
        return abonoDues;
    }

    public void setAbonoDues(int abonoDues) {
        this.abonoDues = abonoDues;
    }

    public int getAbonodetailDues() {
        return abonodetailDues;
    }

    public void setAbonodetailDues(int abonodetailDues) {
        this.abonodetailDues = abonodetailDues;
    }

    public double getMonthly() {
        return monthly;
    }

    public void setMonthly(double monthly) {
        this.monthly = monthly;
    }

}
