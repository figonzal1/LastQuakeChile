package cl.figonzal.lastquakechile.model;

import androidx.annotation.NonNull;

public class QuakesCity {

    private int id;
    private int id_reports;
    private String ciudad;
    private int n_sismos;

    public QuakesCity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_reports() {
        return id_reports;
    }

    public void setId_reports(int id_reports) {
        this.id_reports = id_reports;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public int getN_sismos() {
        return n_sismos;
    }

    public void setN_sismos(int n_sismos) {
        this.n_sismos = n_sismos;
    }

    @NonNull
    @Override
    public String toString() {

        return "QuakesCity{" +
                "id=" + id +
                ", id_reports=" + id_reports +
                ", ciudad='" + ciudad + '\'' +
                ", n_sismos=" + n_sismos +
                '}';
    }
}
