/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.data.entity;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDetailsTb {
    private int id;
    private int loanId;
    private int dues;
    private double totalInterest;
    private double totalIntangibleFund;
    private double monthlyCapitalInstallment;
    private double monthlyInterestFee;
    private double monthlyIntangibleFundFee;
    private double monthlyFeeValue;
    private double payment;
    private Date paymentDate;
    private String state;
    private int createdBy;
    private LocalDateTime createdAt;
    private int modifiedBy;
    private LocalDateTime modifiedAt;

    // Constructor vacío
    public LoanDetailsTb() {}

    // Constructor completo
    public LoanDetailsTb(int id, int loanId, int dues, double totalInterest, double totalIntangibleFund,
                      double monthlyCapitalInstallment, double monthlyInterestFee, double monthlyIntangibleFundFee,
                      double monthlyFeeValue, double payment, Date paymentDate, String state, int createdBy,
                      LocalDateTime createdAt, int modifiedBy, LocalDateTime modifiedAt) {
        this.id = id;
        this.loanId = loanId;
        this.dues = dues;
        this.totalInterest = totalInterest;
        this.totalIntangibleFund = totalIntangibleFund;
        this.monthlyCapitalInstallment = monthlyCapitalInstallment;
        this.monthlyInterestFee = monthlyInterestFee;
        this.monthlyIntangibleFundFee = monthlyIntangibleFundFee;
        this.monthlyFeeValue = monthlyFeeValue;
        this.payment = payment;
        this.paymentDate = paymentDate;
        this.state = state;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getDues() {
        return dues;
    }

    public void setDues(int dues) {
        this.dues = dues;
    }

    public double getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(double totalInterest) {
        this.totalInterest = totalInterest;
    }

    public double getTotalIntangibleFund() {
        return totalIntangibleFund;
    }

    public void setTotalIntangibleFund(double totalIntangibleFund) {
        this.totalIntangibleFund = totalIntangibleFund;
    }

    public double getMonthlyCapitalInstallment() {
        return monthlyCapitalInstallment;
    }

    public void setMonthlyCapitalInstallment(double monthlyCapitalInstallment) {
        this.monthlyCapitalInstallment = monthlyCapitalInstallment;
    }

    public double getMonthlyInterestFee() {
        return monthlyInterestFee;
    }

    public void setMonthlyInterestFee(double monthlyInterestFee) {
        this.monthlyInterestFee = monthlyInterestFee;
    }

    public double getMonthlyIntangibleFundFee() {
        return monthlyIntangibleFundFee;
    }

    public void setMonthlyIntangibleFundFee(double monthlyIntangibleFundFee) {
        this.monthlyIntangibleFundFee = monthlyIntangibleFundFee;
    }

    public double getMonthlyFeeValue() {
        return monthlyFeeValue;
    }

    public void setMonthlyFeeValue(double monthlyFeeValue) {
        this.monthlyFeeValue = monthlyFeeValue;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String toString() {
        return "LoanDetail{" +
               "id=" + id +
               ", loanId=" + loanId +
               ", dues=" + dues +
               ", totalInterest=" + totalInterest +
               ", totalIntangibleFund=" + totalIntangibleFund +
               ", monthlyCapitalInstallment=" + monthlyCapitalInstallment +
               ", monthlyInterestFee=" + monthlyInterestFee +
               ", monthlyIntangibleFundFee=" + monthlyIntangibleFundFee +
               ", monthlyFeeValue=" + monthlyFeeValue +
               ", payment=" + payment +
               ", paymentDate=" + paymentDate +
               ", state='" + state + '\'' +
               ", createdBy=" + createdBy +
               ", createdAt=" + createdAt +
               ", modifiedBy=" + modifiedBy +
               ", modifiedAt=" + modifiedAt +
               '}';
    }
}
