package cl.figonzal.lastquakechile.model;

import androidx.annotation.NonNull;

import java.util.Date;

public class QuakeModel {

    private Date fecha_local;
    private String ciudad;
    private String referencia;
    private Double magnitud;
    private String escala;
    private Boolean sensible;
    private String latitud;
    private String longitud;
    private Double profundidad;
    private String agencia;
    private String imagen_url;
    private String estado;

    public QuakeModel() {
    }

    public Date getFecha_local() {
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

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Double getMagnitud() {
        return magnitud;
    }

    public void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
    }

    public String getEscala() {
        return escala;
    }

    public void setEscala(String escala) {
        this.escala = escala;
    }

    public Boolean getSensible() {
        return sensible;
    }

    public void setSensible(Boolean sensible) {
        this.sensible = sensible;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public Double getProfundidad() {
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

    public String getImagen_url() {
        return imagen_url;
    }

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @NonNull
    @Override
    public String toString() {
        return "QuakeModel{" +
                "fecha_local=" + fecha_local +
                ", ciudad='" + ciudad + '\'' +
                ", referencia='" + referencia + '\'' +
                ", magnitud=" + magnitud +
                ", escala='" + escala + '\'' +
                ", sensible=" + sensible +
                ", latitud='" + latitud + '\'' +
                ", longitud='" + longitud + '\'' +
                ", profundidad=" + profundidad +
                ", agencia='" + agencia + '\'' +
                ", imagen_url='" + imagen_url + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
