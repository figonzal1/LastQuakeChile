package cl.figonzal.lastquakechile.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.*
import cl.figonzal.lastquakechile.databinding.ActivityQuakeDetailsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.util.*

class QuakeDetailsActivity : AppCompatActivity() {
    /*
     * Atributos Mapa
     */
    private var mBitmapUri: Uri? = null
    private lateinit var quake: Quake

    private lateinit var binding: ActivityQuakeDetailsBinding


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindingResources()
    }

    private fun bindingResources() {

        setSupportActionBar(binding.includeToolbar.materialToolbar)

        //Muestra la flecha en toolbar para volver atras
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        handleBundles()
    }

    private fun handleBundles() {

        //Obtener datos desde intent
        quake = intent.extras?.get("quake") as Quake

        //Seteo de textview
        setTextViews()

        //Seteo de floating buttons
        //binding.fabShare.setOnClickListener {
        //  Timber.i(getString(R.string.TAG_FAB_SHARE_STATUS) + ": " + getString(R.string.TAG_FAB_SHARE_STATUS_CLICKED))
        //makeSnapshot()
        //}

    }

    /*private fun makeSnapshot() {
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
    }*/

    private fun shareQuake() {
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
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
                    quake.city,
                    quake.localDate,
                    quake.magnitude,
                    quake.scale,
                    quake.depth,
                    quake.reference,
                    getString(R.string.DEEP_LINK)
                )
            )
            putExtra(Intent.EXTRA_STREAM, mBitmapUri)
            type = "image/*"
            startActivity(Intent.createChooser(this, getString(R.string.INTENT_CHOOSER)))
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


        supportActionBar?.title = quake.city

        with(binding.includeCvQuakeDetail) {

            //Set city name
            tvCiudadDetail.text = quake.city

            //Setear mReferencia
            tvReferenciaDetail.text = quake.reference

            //Setear mMagnitud en en circulo de color
            tvMagnitudDetail.text =
                String.format(getString(R.string.magnitud), quake.magnitude)

            //Setear el color de background dependiendo de mMagnitud del sismo
            ivMagColorDetail.setColorFilter(getColor(getMagnitudeColor(quake.magnitude, false)))

            //Setear mProfundidad
            tvEpicentro.text =
                String.format(getString(R.string.quake_details_profundidad), quake.depth)

            //Setear fecha
            tvFecha.text =
                quake.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            //Calculo de Grados,Minutos y segundos
            tvGms.formatDMS(quake.coordinates)

            //Calcular hora
            tvHoraDetail.setTimeToTextView(dateToDHMS(quake.localDate))

            //Scale
            tvEscala.text = quake.scale

            //sensitive
            ivSensibleDetail.visibility = when {
                quake.isSensitive -> View.VISIBLE
                else -> View.GONE
            }
        }

        //Calculo de estado
        binding.ivEstado.setStatusImage(quake.isVerified, binding.tvEstado)
    }
/*
    override fun onMapReady(googleMap: GoogleMap) {
        val mIdColor = getMagnitudeColor(quake!!.magnitude, true)


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
}*/

}