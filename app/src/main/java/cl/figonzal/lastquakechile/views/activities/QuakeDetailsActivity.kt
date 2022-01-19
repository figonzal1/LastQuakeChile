package cl.figonzal.lastquakechile.views.activities

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.handlers.DateHandler
import cl.figonzal.lastquakechile.handlers.ViewsManager
import cl.figonzal.lastquakechile.model.QuakeModel
import cl.figonzal.lastquakechile.services.NightModeService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong

class QuakeDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
    /*
     * Atributos Mapa
     */
    private var mGoogleMap: GoogleMap? = null
    private var mBitmapUri: Uri? = null
    private var mTvCiudad: TextView? = null
    private var mTvReferencia: TextView? = null
    private var mTvEscala: TextView? = null
    private var mTvMagnitud: TextView? = null
    private var mTvProfundidad: TextView? = null
    private var mTvFecha: TextView? = null
    private var mTvHora: TextView? = null
    private var mTvGms: TextView? = null
    private var mTvEstado: TextView? = null
    private var mIvSensible: ImageView? = null
    private var mIvMagColor: ImageView? = null
    private var mIvEstado: ImageView? = null
    private var mDmsLat: String? = null
    private var mDmsLong: String? = null
    private var mFechaLocal: String? = null
    private var mTiempos: Map<String, Long>? = null
    private var mFabShare: FloatingActionButton? = null
    private var dateHandler: DateHandler? = null
    private var viewsManager: ViewsManager? = null
    private var quakeModel: QuakeModel? = null

    private lateinit var binding: ActivityQuakeDetailsBinding


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Check night mode
        NightModeService(this, this.lifecycle, window)
        val mapFragment = fragmentManager.findFragmentById(R.id.map_fragment) as MapFragment

        mapFragment.getMapAsync(this)

        initResources()
    }

    private fun initResources() {
        dateHandler = DateHandler()
        viewsManager = ViewsManager()


        //Setting toolbar
        val mToolbar = findViewById<Toolbar>(R.id.tool_bar)
        setSupportActionBar(mToolbar)

        //Muestra la flecha en toolbar para volver atras
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //TEXTVIEWS
        with(binding) {
            mTvCiudad = tvCiudadDetail
            mTvReferencia = tvReferenciaDetail
            mTvEscala = tvEscala
            mTvMagnitud = tvMagnitudDetail
            mTvProfundidad = tvEpicentro
            mTvFecha = tvFecha
            mTvGms = tvGms
            mTvHora = tvHoraDetail
            mTvEstado = tvEstado

            //IMAGE VIEWS
            mIvSensible = ivSensibleDetail
            mIvMagColor = ivMagColorDetail
            mIvEstado = ivEstado

            //SETEO DE FLOATING BUTTONS
            mFabShare = fabShare
        }

        handleBundles()
    }

    private fun handleBundles() {

        //Obtener datos desde intent
        val mBundle = intent.extras
        if (mBundle != null) {

            //OBTENCION DE INFO DESDE INTENT
            quakeModel = QuakeModel()
            quakeModel!!.ciudad = mBundle.getString(getString(R.string.INTENT_CIUDAD))
            quakeModel!!.referencia = mBundle.getString(getString(R.string.INTENT_REFERENCIA))
            quakeModel!!.magnitud = mBundle.getDouble(getString(R.string.INTENT_MAGNITUD))
            quakeModel!!.profundidad = mBundle.getDouble(getString(R.string.INTENT_PROFUNDIDAD))
            quakeModel!!.escala = mBundle.getString(getString(R.string.INTENT_ESCALA))
            quakeModel!!.sensible = mBundle.getString(getString(R.string.INTENT_SENSIBLE))
            quakeModel!!.estado = mBundle.getString(getString(R.string.INTENT_ESTADO))
            quakeModel!!.latitud = mBundle.getString(getString(R.string.INTENT_LATITUD))
            quakeModel!!.longitud = mBundle.getString(getString(R.string.INTENT_LONGITUD))

            //Calculo de Grados,Minutos y segundos de
            //mLatitud y mLongitud
            calculateGMS(quakeModel!!.latitud, quakeModel!!.longitud)

            //Configuración de fechas locales y utc
            dateConfig(mBundle)

            //Seteo de textview
            setTextViews()

            //Seteo de floating buttons
            setFloatingButtons()
        }
    }

    /**
     * Funcion encargada de la logica de los botones flotantes
     */
    private fun setFloatingButtons() {

        //Boton compartir central
        mFabShare!!.setOnClickListener { v: View? ->
            Timber.i(getString(R.string.TAG_FAB_SHARE_STATUS) + ": " + getString(R.string.TAG_FAB_SHARE_STATUS_CLICKED))
            makeSnapshot()
        }
    }

    private fun makeSnapshot() {
        if (mGoogleMap != null) {

            //Tomar screenshot del mapa para posterior funcion de compartir
            mGoogleMap!!.snapshot { bitmap: Bitmap? ->
                try {
                    Timber.i("Snapshot google play")
                    mBitmapUri = getLocalBitmapUri(bitmap!!, applicationContext)
                    shareQuake()
                } catch (e: IOException) {
                    Timber.e(e, "Error screenshot map: %s", e.message)
                }
            }
        }
    }

    private fun shareQuake() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT, String.format(
                Locale.US,
                """
                [Alerta sísmica] #sismo #chile
                
                Información sismológica
                Ciudad: %1${"$"}s
                Hora Local: %2${"$"}s
                Magnitud: %3$.1f %4${"$"}s
                Profundidad: %5$.1f Km
                Georeferencia: %6${"$"}s
                
                Descarga la app aquí -> %7${"$"}s
                """.trimIndent(),
                quakeModel!!.ciudad,
                mFechaLocal,
                quakeModel!!.magnitud,
                quakeModel!!.escala,
                quakeModel!!.profundidad,
                quakeModel!!.referencia,
                getString(R.string.DEEP_LINK)
            )
        )
        sendIntent.putExtra(Intent.EXTRA_STREAM, mBitmapUri)
        sendIntent.type = "image/*"
        startActivity(Intent.createChooser(sendIntent, getString(R.string.INTENT_CHOOSER)))
    }

    /**
     * Funcion encargada de la logica del calculo de grados,minutos y segundo, tanto de lalatitud
     * como de la mLongitud
     *
     * @param latitud  Latitud del sismo
     * @param longitud Longitud del sismo
     */
    private fun calculateGMS(latitud: String, longitud: String) {

        //Conversion de mLatitud a dms
        val mLatUbicacion = latitud.toDouble()
        val mLongUbicacion = longitud.toDouble()
        mDmsLat = if (mLatUbicacion < 0) {
            getString(R.string.coordenadas_sur)
        } else {
            getString(R.string.coordenadas_norte)
        }
        mDmsLong = if (mLongUbicacion < 0) {
            getString(R.string.coordenadas_oeste)
        } else {
            getString(R.string.coordenadas_este)
        }

        //Calculo de lat to GMS
        val mMapLatDMS = latLonToDMS(mLatUbicacion)
        val mLatGradosDMS = mMapLatDMS["grados"]
        val mLatMinutosDMS = mMapLatDMS["minutos"]
        val mLatSegundosDMS = mMapLatDMS["segundos"]
        mDmsLat = String.format(
            Locale.US,
            "%.1f° %.1f' %.1f'' %s",
            mLatGradosDMS,
            mLatMinutosDMS,
            mLatSegundosDMS,
            mDmsLat
        )

        //Calculo de long to GMS
        val mMapLongDMS = latLonToDMS(mLongUbicacion)
        val mLongGradosDMS = mMapLongDMS["grados"]
        val mLongMinutosDMS = mMapLongDMS["minutos"]
        val mLongSegundosDMS = mMapLongDMS["segundos"]
        mDmsLong = String.format(
            Locale.US,
            "%.1f° %.1f' %.1f'' %s",
            mLongGradosDMS,
            mLongMinutosDMS,
            mLongSegundosDMS,
            mDmsLong
        )
    }

    /**
     * Funcion encargada de la logica de las fechas de los sismos.
     *
     * @param b Bundle con los datos
     */
    private fun dateConfig(b: Bundle) {

        //SECCION CONVERSION DE TIEMPO UTC-LOCAL (DEPENDIENDO SI VIENE DE ADAPTER O DE
        // NOTIFICACION)
        mFechaLocal = b.getString(getString(R.string.INTENT_FECHA_LOCAL))
        val mFechaUtc = b.getString(getString(R.string.INTENT_FECHA_UTC))


        //SI INTENT VIENE DE ADAPTER
        //Convertir mFechaLocal a Date
        //Calcular DHMS de Date fecha_local
        if (mFechaLocal != null) {
            val local_date: Date?
            try {
                local_date = dateHandler!!.stringToDate(this, mFechaLocal!!)
                quakeModel!!.fecha_local = local_date
                mTiempos = dateHandler!!.dateToDHMS(quakeModel!!.fecha_local)
            } catch (e: ParseException) {
                Timber.e(e, "Parse exception error: %s", e.message)
            }
        } else {
            if (mFechaUtc != null) {
                val fecha_utc: Date?
                try {
                    fecha_utc = dateHandler!!.stringToDate(this, mFechaUtc)
                    if (fecha_utc != null) {
                        val mDateFechaLocal = dateHandler!!.utcToLocal(fecha_utc)
                        quakeModel!!.fecha_local = mDateFechaLocal
                        mTiempos = dateHandler!!.dateToDHMS(mDateFechaLocal)

                        //Setear string que será usado en textviews de detalle con la fecha transformada
                        // de utc a local desde notificacion
                        mFechaLocal = dateHandler!!.dateToString(this, mDateFechaLocal)
                    }
                } catch (e: ParseException) {
                    Timber.e(e, "Parse exception error: %s", e.message)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Respond to the action bar's Up/Home button
        if (item.itemId == android.R.id.home) {
            Timber.i(getString(R.string.TAG_INTENT_DETALLE_HOME_UP_RESPONSE))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Funcion que permite setear los textview del detalle con la información procesada
     */
    private fun setTextViews() {

        //Setear titulo de mCiudad en activity
        val actionBar = supportActionBar
        actionBar?.title = quakeModel!!.ciudad

        //Setear nombre mCiudad
        mTvCiudad!!.text = quakeModel!!.ciudad

        //Setear mReferencia
        mTvReferencia!!.text = quakeModel!!.referencia

        //Setear mMagnitud en en circulo de color
        mTvMagnitud!!.text = String.format(getString(R.string.magnitud), quakeModel!!.magnitud)

        //Setear el color de background dependiendo de mMagnitud del sismo
        mIvMagColor!!.setColorFilter(
            getColor(
                viewsManager!!.getMagnitudeColor(
                    quakeModel!!.magnitud,
                    false
                )
            )
        )

        //Setear mProfundidad
        mTvProfundidad!!.text = String.format(
            Locale.US,
            getString(R.string.quake_details_profundidad),
            quakeModel!!.profundidad
        )

        //Setear fecha
        mTvFecha!!.text = mFechaLocal

        //Setear posicionamiento
        mTvGms!!.text = String.format(getString(R.string.format_coordenadas), mDmsLat, mDmsLong)

        //SETEO DE ESTADO
        if (quakeModel!!.estado != null) {
            viewsManager!!.setStatusImage(
                applicationContext,
                quakeModel!!.estado,
                mTvEstado!!,
                mIvEstado!!
            )
        }

        //SETEO DE HORA
        if (mTiempos != null) {
            viewsManager!!.setTimeToTextView(applicationContext, mTiempos!!, mTvHora!!)
        }

        //SETEO DE ESCALA
        if (quakeModel!!.escala != null) {
            viewsManager!!.setEscala(applicationContext, quakeModel!!.escala, mTvEscala!!)
        }

        //SETEO SISMO SENSIBLE
        if (quakeModel!!.sensible == "1") {
            mIvSensible!!.visibility = View.VISIBLE
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        //NIGHT MODE MAPA
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    applicationContext,
                    R.raw.map_night_mode
                )
            )
        }
        mGoogleMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mGoogleMap!!.setMinZoomPreference(5.0f)
        mGoogleMap!!.uiSettings.isZoomGesturesEnabled = false
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
        mGoogleMap!!.uiSettings.isTiltGesturesEnabled = false
        mGoogleMap!!.uiSettings.isScrollGesturesEnabled = false
        mGoogleMap!!.uiSettings.isMapToolbarEnabled = false
        mGoogleMap!!.uiSettings.isRotateGesturesEnabled = false
        mGoogleMap!!.uiSettings.isCompassEnabled = false
        val mLatLong = LatLng(
            quakeModel!!.latitud.toDouble(), quakeModel!!.longitud.toDouble()
        )
        val mIdColor = viewsManager!!.getMagnitudeColor(quakeModel!!.magnitud, true)

        //Circulo grande con color segun magnitud
        mGoogleMap!!.addCircle(
            CircleOptions()
                .center(mLatLong)
                .radius(90000.0)
                .fillColor(getColor(mIdColor))
                .strokeColor(getColor(R.color.grey_dark_alpha))
        )
        mGoogleMap!!.addCircle(
            CircleOptions()
                .center(mLatLong)
                .radius(3000.0)
                .fillColor(getColor(R.color.grey_dark_alpha))
                .strokeColor(Color.TRANSPARENT)
        )

        //Circulo gris animado
        val circle_anim = mGoogleMap!!.addCircle(
            CircleOptions()
                .center(mLatLong)
                .radius(90000.0)
                .strokeWidth(1f)
                .strokeColor(getColor(R.color.grey_dark_alpha))
        )
        val animator = ValueAnimator.ofInt(0, 90000)
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        animator.duration = 4000
        animator.setEvaluator(IntEvaluator())
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation: ValueAnimator ->
            val animatedFraction = animation.animatedFraction
            circle_anim.radius = (animatedFraction * 140000).toDouble()
        }
        animator.start()

        //Circulo gris animado
        val circle_anim2 = mGoogleMap!!.addCircle(
            CircleOptions()
                .center(mLatLong)
                .radius(90000.0)
                .strokeWidth(1f)
                .strokeColor(getColor(R.color.grey_dark_alpha))
        )
        val animator2 = ValueAnimator.ofInt(0, 90000)
        animator2.repeatMode = ValueAnimator.RESTART
        animator2.repeatCount = ValueAnimator.INFINITE
        animator2.duration = 4000
        animator2.startDelay = 1000
        animator2.setEvaluator(IntEvaluator())
        animator2.interpolator = AccelerateDecelerateInterpolator()
        animator2.addUpdateListener { animation: ValueAnimator ->
            val animatedFraction = animation.animatedFraction
            circle_anim2.radius = (animatedFraction * 140000).toDouble()
        }
        animator2.start()
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLong, 6.0f))

        //Callback en espera de mapa completamente cargado
        mGoogleMap!!.setOnMapLoadedCallback {}
    }

    /**
     * Funcion que permite cambiaar latitud o longitud a DMS
     *
     * @param input Longitud o Latitud
     * @return grados, minutos, segundos en un Map
     */
    private fun latLonToDMS(input: Double): Map<String, Double> {
        val mDMS: MutableMap<String, Double> = HashMap()
        val abs = abs(input)
        val mLatGradosLet = floor(abs) //71
        val mMinutes = floor((abs - mLatGradosLet) * 3600 / 60) // 71.43 -71 = 0.43
        // =25.8 = 25
        //(71.43 - 71)*3600 /60 - (71.43-71)*3600/60 = 25.8 - 25 =0.8
        val mSeconds = ((abs - mLatGradosLet) * 3600 / 60 - mMinutes) * 60
        mDMS["grados"] = floor(abs(input))
        mDMS["minutos"] = mMinutes.roundToLong().toDouble()
        mDMS["segundos"] = mSeconds.roundToLong().toDouble()
        return mDMS
    }

    /**
     * Funcion encargada se guardar en directorio de celular una imagen bitmap
     *
     * @param bitmap  Bitmap de la imagen
     * @param context Contexto necesario para usar recursos
     * @return Path de la imagen
     */
    @Throws(IOException::class)
    fun getLocalBitmapUri(bitmap: Bitmap, context: Context): Uri {
        val c = Calendar.getInstance()
        c.time = quakeModel!!.fecha_local
        val date = c.timeInMillis.toInt()
        val mFile = File(context.cacheDir, "share$date.jpeg")
        if (mFile.exists()) {
            Timber.i("Share image exist")
        } else {
            Timber.i("Share image not exist")
            val out = FileOutputStream(mFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
        }
        return FileProvider.getUriForFile(context, "cl.figonzal.lastquakechile.fileprovider", mFile)
    }
}