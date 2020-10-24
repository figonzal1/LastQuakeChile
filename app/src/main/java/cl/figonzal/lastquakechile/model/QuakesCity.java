package cl.figonzal.lastquakechile.model;

import androidx.annotation.NonNull;

public class QuakesCity {

    private String ciudad;
    private int n_sismos;

    public QuakesCity() {
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
                ", ciudad='" + ciudad + '\'' +
                ", n_sismos=" + n_sismos +
                '}';
    }
}
