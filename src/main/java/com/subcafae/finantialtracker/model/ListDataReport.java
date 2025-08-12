/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesus Gutierrez
 */
//      detalles.add(Map.of(
//                    "date_field",  payment.getFecha(),
//                    "document_field",   payment.getCodeDocument(),
//                    "amount_field", payment.getAmountDocument(),
//                    "concept_payment_field", "",
//                    "vencimiento_field", "",
//                    "amount_payment_field", "",
//                    "amount_pri_field", ""
//            ));
public class ListDataReport {

    private String date_field;
    private String document_field;
    private String amount_field;
    private List<Map<String, ?>> detalles_sub;

    public String getDate_field() {
        return date_field;
    }

    public void setDate_field(String date_field) {
        this.date_field = date_field;
    }

    public String getDocument_field() {
        return document_field;
    }

    public void setDocument_field(String document_field) {
        this.document_field = document_field;
    }

    public String getAmount_field() {
        return amount_field;
    }

    public void setAmount_field(String amount_field) {
        this.amount_field = amount_field;
    }

    public List<Map<String, ?>> getDetalles_sub() {
        return detalles_sub;
    }

    public void setDetalles_sub(List<Map<String, ?>> detalles_sub) {
        this.detalles_sub = detalles_sub;
    }
    
    
}
