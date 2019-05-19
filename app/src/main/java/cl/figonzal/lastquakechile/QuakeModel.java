package cl.figonzal.lastquakechile;

import java.util.Date;

public class QuakeModel {

	private Date mFechaLocal;
	private String mCiudad;
	private String mLatitud;
	private String mLongitud;
	private Double mMagnitud;
	private String mEscala;
	private Double mProfundidad;
	private Boolean mSensible;
	private String mAgencia;
	private String mReferencia;
	private String mImagenUrl;
	private String mEstado;

	public QuakeModel() {
	}

	public Date getFechaLocal() {
		return mFechaLocal;
	}

	public void setFechaLocal(Date fechaLocal) {
		this.mFechaLocal = fechaLocal;
	}

	public String getCiudad() {
		return mCiudad;
	}

	public void setCiudad(String ciudad) {
		this.mCiudad = ciudad;
	}

	public String getLatitud() {
		return mLatitud;
	}

	public void setLatitud(String latitud) {
		this.mLatitud = latitud;
	}

	public String getLongitud() {
		return mLongitud;
	}

	public void setLongitud(String longitud) {
		this.mLongitud = longitud;
	}

	public Double getMagnitud() {
		return mMagnitud;
	}

	public void setMagnitud(Double magnitud) {
		this.mMagnitud = magnitud;
	}

	public String getEscala() {
		return mEscala;
	}

	public void setEscala(String escala) {
		this.mEscala = escala;
	}

	public Double getProfundidad() {
		return mProfundidad;
	}

	public void setProfundidad(Double profundidad) {
		this.mProfundidad = profundidad;
	}

	public String getAgencia() {
		return mAgencia;
	}

	public void setAgencia(String agencia) {
		this.mAgencia = agencia;
	}

	public String getReferencia() {
		return mReferencia;
	}

	public void setReferencia(String referencia) {
		this.mReferencia = referencia;
	}

	public String getImagenUrl() {
		return mImagenUrl;
	}

	public void setImagenUrl(String imagen_url) {
		this.mImagenUrl = imagen_url;
	}

	public Boolean getSensible() {
		return mSensible;
	}

	public void setSensible(Boolean sensible) {
		this.mSensible = sensible;
	}

	public String getEstado() {
		return mEstado;
	}

	public void setEstado(String estado) {
		this.mEstado = estado;
	}
}
