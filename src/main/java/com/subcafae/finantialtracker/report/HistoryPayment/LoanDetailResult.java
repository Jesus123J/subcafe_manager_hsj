/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.HistoryPayment;

/**
 *
 * @author Jesus Gutierrez
 */
public class LoanDetailResult {
    private int loanDues;
    private int loandetailDues;
    private double monthlyFeeValue;
    private String paymentDate;

    public int getLoanDues() {
        return loanDues;
    }

    public void setLoanDues(int loanDues) {
        this.loanDues = loanDues;
    }

    public int getLoandetailDues() {
        return loandetailDues;
    }

    public void setLoandetailDues(int loandetailDues) {
        this.loandetailDues = loandetailDues;
    }

    public double getMonthlyFeeValue() {
        return monthlyFeeValue;
    }

    public void setMonthlyFeeValue(double monthlyFeeValue) {
        this.monthlyFeeValue = monthlyFeeValue;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    
    
    
}

