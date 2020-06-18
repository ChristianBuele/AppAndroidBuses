package org.example.proyectofinal;

import java.io.Serializable;

public class Configuracion implements Serializable {
    private String tiempoGps="1500";
    public Configuracion(){

    }

    public String getTiempoGps() {
        return tiempoGps;
    }

    public void setTiempoGps(String tiempoGps) {
        this.tiempoGps = tiempoGps;
    }
}
