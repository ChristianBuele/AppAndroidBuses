package org.example.proyectofinal;

public class DatosBaseDatos {
    private Double latitud,longitud;
    private String usuario;
     String nombre;
    private String id;
    public DatosBaseDatos(){

    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
