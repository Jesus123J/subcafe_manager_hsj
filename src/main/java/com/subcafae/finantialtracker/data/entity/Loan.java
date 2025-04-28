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
    private BigDecimal refinanciado;
    private int cantCuota;
    private BigDecimal interTo;
    private BigDecimal fondoTo;
    private BigDecimal cuotaMenSin;
    private BigDecimal cuotaInter;
    private BigDecimal cuotaFond;
    private BigDecimal valor;
   private String modificado;
    private String state;
    private String paymentResponsibility;

    public Loan(int id,
            String modificado,
            String soliNum,
            String solicitorName,
            String guarantorName, 
            BigDecimal requestedAmount, 
            BigDecimal amountWithdrawn, 
            BigDecimal refinanciado, 
            int cantCuota, 
            BigDecimal interTo, 
            BigDecimal fondoTo, 
            BigDecimal cuotaMenSin,
            BigDecimal cuotaInter,
            BigDecimal cuotaFond,
            BigDecimal valor,
            String state,
            String paymentResponsibility) {
        this.id = id;
        this.modificado = modificado;
        this.soliNum = soliNum;
        this.solicitorName = solicitorName;
        this.guarantorName = guarantorName;
        this.requestedAmount = requestedAmount;
        this.amountWithdrawn = amountWithdrawn;
        this.refinanciado = refinanciado;
        this.cantCuota = cantCuota;
        this.interTo = interTo;
        this.fondoTo = fondoTo;
        this.cuotaMenSin = cuotaMenSin;
        this.cuotaInter = cuotaInter;
        this.cuotaFond = cuotaFond;
        this.valor = valor;
        this.state = state;
        this.paymentResponsibility = paymentResponsibility;
    }

    public String getModificado() {
        return modificado;
    }

    public void setModificado(String modificado) {
        this.modificado = modificado;
    }

    public BigDecimal getRefinanciado() {
        return refinanciado;
    }

    public void setRefinanciado(BigDecimal refinanciado) {
        this.refinanciado = refinanciado;
    }

    public int getCantCuota() {
        return cantCuota;
    }

    public void setCantCuota(int cantCuota) {
        this.cantCuota = cantCuota;
    }

    public BigDecimal getInterTo() {
        return interTo;
    }

    public void setInterTo(BigDecimal interTo) {
        this.interTo = interTo;
    }

    public BigDecimal getFondoTo() {
        return fondoTo;
    }

    public void setFondoTo(BigDecimal fondoTo) {
        this.fondoTo = fondoTo;
    }

    public BigDecimal getCuotaMenSin() {
        return cuotaMenSin;
    }

    public void setCuotaMenSin(BigDecimal cuotaMenSin) {
        this.cuotaMenSin = cuotaMenSin;
    }

    public BigDecimal getCuotaInter() {
        return cuotaInter;
    }

    public void setCuotaInter(BigDecimal cuotaInter) {
        this.cuotaInter = cuotaInter;
    }

    public BigDecimal getCuotaFond() {
        return cuotaFond;
    }

    public void setCuotaFond(BigDecimal cuotaFond) {
        this.cuotaFond = cuotaFond;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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
        return "Loan{" + "id=" + id + ", soliNum=" + soliNum + ", solicitorName=" + solicitorName + ", guarantorName=" + guarantorName + ", requestedAmount=" + requestedAmount + ", amountWithdrawn=" + amountWithdrawn + ", refinanciado=" + refinanciado + ", cantCuota=" + cantCuota + ", interTo=" + interTo + ", fondoTo=" + fondoTo + ", cuotaMenSin=" + cuotaMenSin + ", cuotaInter=" + cuotaInter + ", cuotaFond=" + cuotaFond + ", valor=" + valor + ", modificado=" + modificado + ", state=" + state + ", paymentResponsibility=" + paymentResponsibility + '}';
    }


}
