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
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoanTb {
    private Integer id;
    private String soliNum;
    private String employeeId;
    private String guarantorIds;
    private double RequestedAmount;
    private String stateLoan;
    private double AmountWithdrawn;
    private int dues;
    private LocalDate paymentDate;
    private String state;
    private Integer refinanceParentId;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer modifiedBy;
    private String type;
    private String paymentResponsibility;
    
    public LoanTb() {
    }

    public String getPaymentResponsibility() {
        return paymentResponsibility;
    }

    public void setPaymentResponsibility(String paymentResponsibility) {
        this.paymentResponsibility = paymentResponsibility;
    }

    
    
    public String getStateLoan() {
        return stateLoan;
    }

    public void setStateLoan(String stateLoan) {
        this.stateLoan = stateLoan;
    }

    public enum LoanState {
        Pendiente, Aceptado, Denegado, Refinanciado
    }

    // Constructor sin ID y sin SoliNum
    public LoanTb(String employeeId, String guarantorIds, double AmountWithdrawn, double RequestedAmount, 
                int dues, LocalDate paymentDate, String state, Integer refinanceParentId,
                int createdBy, LocalDateTime createdAt, LocalDateTime modifiedAt, 
                Integer modifiedBy, String type , String PaymentResponsibility) {
        this.employeeId = employeeId;
        this.guarantorIds = guarantorIds;
        this.AmountWithdrawn = AmountWithdrawn;
        this.RequestedAmount = RequestedAmount;
        this.dues = dues;
        this.paymentDate = paymentDate;
        this.state = state;
        this.refinanceParentId = refinanceParentId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.type = type;
        this.paymentResponsibility = PaymentResponsibility;
    }

    // Getters y Setters (implementar todos)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSoliNum() { return soliNum; }
    public void setSoliNum(String soliNum) { this.soliNum = soliNum; }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }



    public int getDues() {
        return dues;
    }

    public void setDues(int dues) {
        this.dues = dues;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getRefinanceParentId() {
        return refinanceParentId;
    }

    public void setRefinanceParentId(Integer refinanceParentId) {
        this.refinanceParentId = refinanceParentId;
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

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Integer getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Integer modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGuarantorIds() {
        return guarantorIds;
    }

    public void setGuarantorIds(String guarantorIds) {
        this.guarantorIds = guarantorIds;
    }

    public double getRequestedAmount() {
        return RequestedAmount;
    }

    public void setRequestedAmount(double RequestedAmount) {
        this.RequestedAmount = RequestedAmount;
    }

    public double getAmountWithdrawn() {
        return AmountWithdrawn;
    }

    public void setAmountWithdrawn(double AmountWithdrawn) {
        this.AmountWithdrawn = AmountWithdrawn;
    }

    @Override
    public String toString() {
        return "LoanTb{" + "id=" + id + ", soliNum=" + soliNum + ", employeeId=" + employeeId + ", guarantorIds=" + guarantorIds + ", RequestedAmount=" + RequestedAmount + ", stateLoan=" + stateLoan + ", AmountWithdrawn=" + AmountWithdrawn + ", dues=" + dues + ", paymentDate=" + paymentDate + ", state=" + state + ", refinanceParentId=" + refinanceParentId + ", createdBy=" + createdBy + ", createdAt=" + createdAt + ", modifiedAt=" + modifiedAt + ", modifiedBy=" + modifiedBy + ", type=" + type + '}';
    }

  
   
}