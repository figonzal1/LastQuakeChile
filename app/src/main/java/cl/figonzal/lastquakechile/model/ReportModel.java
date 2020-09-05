package cl.figonzal.lastquakechile.model;

import androidx.annotation.NonNull;

import java.util.List;

public class ReportModel {

    private int id;
    private String mes_reporte;
    private int n_sensibles;
    private int n_sismos;
    private double prom_magnitud;
    private double prom_profundidad;
    private double max_magnitud;
    private double min_profundidad;

    private List<QuakesCity> quakesCities;

    public ReportModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMes_reporte() {
        return mes_reporte;
    }

    public void setMes_reporte(String mes_reporte) {
        this.mes_reporte = mes_reporte;
    }

    public int getN_sensibles() {
        return n_sensibles;
    }

    public void setN_sensibles(int n_sensibles) {
        this.n_sensibles = n_sensibles;
    }

    public int getN_sismos() {
        return n_sismos;
    }

    public void setN_sismos(int n_sismos) {
        this.n_sismos = n_sismos;
    }

    public double getProm_magnitud() {
        return prom_magnitud;
    }

    public void setProm_magnitud(double prom_magnitud) {
        this.prom_magnitud = prom_magnitud;
    }

    public double getProm_profundidad() {
        return prom_profundidad;
    }

    public void setProm_profundidad(double prom_profundidad) {
        this.prom_profundidad = prom_profundidad;
    }

    public double getMax_magnitud() {
        return max_magnitud;
    }

    public void setMax_magnitud(double max_magnitud) {
        this.max_magnitud = max_magnitud;
    }

    public double getMin_profundidad() {
        return min_profundidad;
    }

    public void setMin_profundidad(double min_profundidad) {
        this.min_profundidad = min_profundidad;
    }

    public List<QuakesCity> getQuakesCities() {
        return quakesCities;
    }

    public void setQuakesCities(List<QuakesCity> quakesCities) {
        this.quakesCities = quakesCities;
    }

    @NonNull
    @Override
    public String toString() {

        return "ReportModel{" +
                "id=" + id +
                ", fecha_reporte=" + mes_reporte +
                ", n_sensibles=" + n_sensibles +
                ", n_sismos=" + n_sismos +
                ", prom_magnitud=" + prom_magnitud +
                ", prom_profundidad=" + prom_profundidad +
                ", max_magnitud=" + max_magnitud +
                ", min_profundidad=" + min_profundidad +
                ", quakesCities=" + quakesCities +
                '}';
    }
}
