/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.report.HistoryPayment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesus Gutierrez
 */
public class ModelPayment {

    String fecha;
    String codeDocument;
    String amountDocument;
    Map<String,String> mapAndPaymeny = new HashMap<>();
    Map<String , String > amountPaymentAndLoan;

    public ModelPayment() {
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getCodeDocument() {
        return codeDocument;
    }

    public void setCodeDocument(String codeDocument) {
        this.codeDocument = codeDocument;
    }

    public String getAmountDocument() {
        return amountDocument;
    }

    public void setAmountDocument(String amountDocument) {
        this.amountDocument = amountDocument;
    }

    public Map<String , String > getAmountPaymentAndLoan() {
        return amountPaymentAndLoan;
    }

    public void setAmountPaymentAndLoan(Map<String , String > amountPaymentAndLoan) {
        this.amountPaymentAndLoan = amountPaymentAndLoan;
    }

    public Map<String, String> getMapAndPaymeny() {
        return mapAndPaymeny;
    }

    public void setMapAndPaymeny(Map<String, String> mapAndPaymeny) {
        this.mapAndPaymeny = mapAndPaymeny;
    }

    @Override
    public String toString() {
        return "modelPayment{" + "fecha=" + fecha + ", codeDocument=" + codeDocument + ", amountDocument=" + amountDocument + ", mapAndPaymeny=" + mapAndPaymeny + ", amountPaymentAndLoan=" + amountPaymentAndLoan + '}';
    }

}
