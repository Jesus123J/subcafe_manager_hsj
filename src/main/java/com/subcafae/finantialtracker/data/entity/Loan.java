/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

/**
 *
 * @author Jesus Gutierrez
 */
import java.math.BigDecimal;

public class Loan {
    private int id;
    private String soliNum;
    private String solicitorName;
    private String guarantorName;
    private BigDecimal requestedAmount;
    private BigDecimal amountWithdrawn;
    private String state;
    private String paymentResponsibility;

    public Loan(int id, String soliNum, String solicitorName, String guarantorName,
                BigDecimal requestedAmount, BigDecimal amountWithdrawn, String state,
                String paymentResponsibility) {
        this.id = id;
        this.soliNum = soliNum;
        this.solicitorName = solicitorName;
        this.guarantorName = guarantorName;
        this.requestedAmount = requestedAmount;
        this.amountWithdrawn = amountWithdrawn;
        this.state = state;
        this.paymentResponsibility = paymentResponsibility;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSoliNum() {
        return soliNum;
    }

    public void setSoliNum(String soliNum) {
        this.soliNum = soliNum;
    }

    public String getSolicitorName() {
        return solicitorName;
    }

    public void setSolicitorName(String solicitorName) {
        this.solicitorName = solicitorName;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAmountWithdrawn() {
        return amountWithdrawn;
    }

    public void setAmountWithdrawn(BigDecimal amountWithdrawn) {
        this.amountWithdrawn = amountWithdrawn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPaymentResponsibility() {
        return paymentResponsibility;
    }

    public void setPaymentResponsibility(String paymentResponsibility) {
        this.paymentResponsibility = paymentResponsibility;
    }

    @Override
    public String toString() {
        return "Loan{" + "id=" + id + ", soliNum=" + soliNum + ", solicitorName=" + solicitorName + ", guarantorName=" + guarantorName + ", requestedAmount=" + requestedAmount + ", amountWithdrawn=" + amountWithdrawn + ", state=" + state + ", paymentResponsibility=" + paymentResponsibility + '}';
    }
    
}