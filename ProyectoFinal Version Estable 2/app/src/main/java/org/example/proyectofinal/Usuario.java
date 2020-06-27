package org.example.proyectofinal;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String usuario;
    private Double latitud,longitud;
    Double distanciaRecorrida=0.0;
    Float pasosDados=(float)0;
    String idRegistro="";
    String apellido="";
    Boolean isConductor=false;
    private boolean band=true;
    private String unidades="m";
    public Usuario(String usuario, Double latitud, Double longitud) {
        this.usuario = usuario;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Boolean getConductor() { return isConductor; }

    public void setConductor(Boolean isConductor) { this.isConductor = isConductor;}

    public String getIdRegistro() { return idRegistro; }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public boolean getBand() {
        return this.band;
    }

    public void setBand(boolean band) {
        this.band = band;
    }

    public String getUnidades() {
        return unidades;
    }

    public void setUnidades(String unidades) {
        this.unidades = unidades;
    }
}



