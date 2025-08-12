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
public class ReportPayementData {
    private String dni_parameter;
    private String lastName_parameter;
    private List<ListDataReport> detalles;

    public String getDni_parameter() {
        return dni_parameter;
    }

    public void setDni_parameter(String dni_parameter) {
        this.dni_parameter = dni_parameter;
    }

    public String getLastName_parameter() {
        return lastName_parameter;
    }

    public void setLastName_parameter(String lastName_parameter) {
        this.lastName_parameter = lastName_parameter;
    }

    public List<ListDataReport> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ListDataReport> detalles) {
        this.detalles = detalles;
    }

}
