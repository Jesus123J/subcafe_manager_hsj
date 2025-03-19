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
    private double amount;
    private double originalAmount;
    private int dues;
    private LocalDate paymentDate;
    private LoanState state;
    private Integer refinanceParentId;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer modifiedBy;
    private String type;

    public enum LoanState {
        Pendiente, Aceptado, Denegado, Refinanciado
    }

    // Constructor sin ID y sin SoliNum
    public LoanTb(String employeeId, String guarantorIds, double amount, double originalAmount, 
                int dues, LocalDate paymentDate, LoanState state, Integer refinanceParentId,
                int createdBy, LocalDateTime createdAt, LocalDateTime modifiedAt, 
                Integer modifiedBy, String type) {
        this.employeeId = employeeId;
        this.guarantorIds = guarantorIds;
        this.amount = amount;
        this.originalAmount = originalAmount;
        this.dues = dues;
        this.paymentDate = paymentDate;
        this.state = state;
        this.refinanceParentId = refinanceParentId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
        this.type = type;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
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

    public LoanState getState() {
        return state;
    }

    public void setState(LoanState state) {
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
   
}