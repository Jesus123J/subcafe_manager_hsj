package com.subcafae.finantialtracker.data.entity;

/**
 * Representa un lote de carga masiva de abonos (tipicamente desde Excel).
 * Permite rastrear todos los abonos importados juntos y revertir el lote
 * completo si se subio el archivo equivocado o duplicado.
 */
public class LoteCargaAbonoTb {

    private int id;
    private String fechaCreacion;
    private Integer usuarioId;
    private String nombreArchivo;
    private int cantidadAbonos;
    private String state;
    private String motivoReversion;
    private String fechaReversion;
    private Integer revertidoPor;

    public LoteCargaAbonoTb() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public int getCantidadAbonos() { return cantidadAbonos; }
    public void setCantidadAbonos(int cantidadAbonos) { this.cantidadAbonos = cantidadAbonos; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getMotivoReversion() { return motivoReversion; }
    public void setMotivoReversion(String motivoReversion) { this.motivoReversion = motivoReversion; }

    public String getFechaReversion() { return fechaReversion; }
    public void setFechaReversion(String fechaReversion) { this.fechaReversion = fechaReversion; }

    public Integer getRevertidoPor() { return revertidoPor; }
    public void setRevertidoPor(Integer revertidoPor) { this.revertidoPor = revertidoPor; }
}
