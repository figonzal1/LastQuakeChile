package cl.figonzal.lastquakechile;

import java.util.Date;

public class QuakeModel {

    private Date fecha_local;
    private String latitud;
    private String longitud;
    private Double magnitud;
    private String escala;
    private Double profundidad;
    private Boolean sensible;
    private String agencia;
    private String referencia;
    private String imagen_url;

    public QuakeModel() {
    }

    Date getFecha_local() {
        return fecha_local;
    }

    void setFecha_local(Date fecha_local) {
        this.fecha_local = fecha_local;
    }

    String getLatitud() {
        return latitud;
    }

    void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    String getLongitud() {
        return longitud;
    }

    void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    Double getMagnitud() {
        return magnitud;
    }

    void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
    }

    String getEscala() {
        return escala;
    }

    void setEscala(String escala) {
        this.escala = escala;
    }

    Double getProfundidad() {
        return profundidad;
    }

    void setProfundidad(Double profundidad) {
        this.profundidad = profundidad;
    }

    public String getAgencia() {
        return agencia;
    }


    void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    String getReferencia() {
        return referencia;
    }

    void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    String getImagen_url() {
        return imagen_url;
    }

    void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    Boolean getSensible() {
        return sensible;
    }

    void setSensible(Boolean sensible) {
        this.sensible = sensible;
    }
}
