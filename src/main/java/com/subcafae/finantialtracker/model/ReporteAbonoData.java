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
public class ReporteAbonoData {
    private String concepto_var;
    private String section_var;
    private String total_var;
    private String vencimiento_information_var;
    private String num_solicitud_var;
    private String num_cuotas_pendiente_var;
    private   List<Map<String, ?>>  detalles;
    
    // Getters y setters

    public String getConcepto_var() {
        return concepto_var;
    }

    public void setConcepto_var(String concepto_var) {
        this.concepto_var = concepto_var;
    }

    public String getSection_var() {
        return section_var;
    }

    public void setSection_var(String section_var) {
        this.section_var = section_var;
    }

    public String getTotal_var() {
        return total_var;
    }

    public void setTotal_var(String total_var) {
        this.total_var = total_var;
    }

    public String getVencimiento_information_var() {
        return vencimiento_information_var;
    }

    public void setVencimiento_information_var(String vencimiento_information_var) {
        this.vencimiento_information_var = vencimiento_information_var;
    }

    public String getNum_solicitud_var() {
        return num_solicitud_var;
    }

    public void setNum_solicitud_var(String num_solicitud_var) {
        this.num_solicitud_var = num_solicitud_var;
    }

    public String getNum_cuotas_pendiente_var() {
        return num_cuotas_pendiente_var;
    }

    public void setNum_cuotas_pendiente_var(String num_cuotas_pendiente_var) {
        this.num_cuotas_pendiente_var = num_cuotas_pendiente_var;
    }

    public List<Map<String, ?>> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<Map<String, ?>> detalles) {
        this.detalles = detalles;
    }


}
