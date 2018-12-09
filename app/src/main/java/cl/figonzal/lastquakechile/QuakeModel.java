package cl.figonzal.lastquakechile;

import java.util.Date;

public class QuakeModel {

    private Date fecha_local;
    private Date fecha_utc;
    private String latitud;
    private String longitud;
    private Double magnitud;
    private String agencia;
    private String referencia;
    private String imagen_url;

    public QuakeModel() {
    }

    public Date getFecha_local() {
        return fecha_local;
    }

    public void setFecha_local(Date fecha_local) {
        this.fecha_local = fecha_local;
    }

    public Date getFecha_utc() {
        return fecha_utc;
    }

    public void setFecha_utc(Date fecha_utc) {
        this.fecha_utc = fecha_utc;
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

    public Double getMagnitud() {
        return magnitud;
    }

    public void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
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

    public String getImagen_url() {
        return imagen_url;
    }

    public void setImagen_url(String imagen_url) {
        this.imagen_url = imagen_url;
    }
}
