package cl.figonzal.lastquakechile;

import java.util.Date;

public class QuakeModel {

    private Date fecha_local;
    private String ciudad;
    private String latitud;
    private String longitud;
    private Double magnitud;
    private String escala;
    private Double profundidad;
    private Boolean sensible;
    private String agencia;
    private String referencia;
    private String imagen_url;
    private String estado;

    public QuakeModel() {
    }

    Date getFecha_local() {
        return fecha_local;
    }

    public void setFecha_local(Date fecha_local) {
        this.fecha_local = fecha_local;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public Double getMagnitud() {
        return magnitud;
    }

    public void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
    }

    String getEscala() {
        return escala;
    }

    public void setEscala(String escala) {
        this.escala = escala;
    }

    Double getProfundidad() {
        return profundidad;
    }

    public void setProfundidad(Double profundidad) {
        this.profundidad = profundidad;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    String getImagen_url() {
        return imagen_url;
    }

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    Boolean getSensible() {
        return sensible;
    }

    public void setSensible(Boolean sensible) {
        this.sensible = sensible;
    }

    String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
