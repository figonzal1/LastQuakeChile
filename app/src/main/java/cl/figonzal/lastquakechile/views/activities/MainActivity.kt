package cl.figonzal.lastquakechile.views.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.ui.MainFragmentStateAdapter
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.databinding.ActivityMainBinding
import cl.figonzal.lastquakechile.services.FirebaseService
import cl.figonzal.lastquakechile.services.GooglePlayService
import cl.figonzal.lastquakechile.services.NightModeService
import cl.figonzal.lastquakechile.services.UpdaterService
import cl.figonzal.lastquakechile.services.notifications.QuakesNotification
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var mAppBarLayout: AppBarLayout? = null
    private var mIvFoto: ImageView? = null
    private var updaterService: UpdaterService? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val sharedPrefService = SharedPrefUtil(applicationContext)

        //Night mode
        NightModeService(this, this.lifecycle, window)

        //GP services
        GooglePlayService(this, this.lifecycle)

        //Firebase services
        val firebaseService = FirebaseService(FirebaseMessaging.getInstance())
        firebaseService.getFirebaseToken()

        //Updater service
        //updaterService = UpdaterService(this, AppUpdateManagerFactory.create(this))
        //updaterService!!.checkAvailability()

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        val quakeNotification = QuakesNotification(this, sharedPrefService)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            quakeNotification.createChannel()
        }
        quakeNotification.suscribedToQuakes(true)

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs()

        //Setear imagen de toolbar
        loadImageToolbar()
    }

    /**
     * Setear elementos de UI necesarios para el funcionamiento de la APP
     */
    private fun setToolbarViewPagerTabs() {

        //Buscar toolbar en resources
        val mToolbar = binding.toolbarLayout.toolbarMain.toolBar
        setSupportActionBar(mToolbar)

        //Appbar layout para minimizar el collapse toolbar cuando se presiona el tab de mapa
        mAppBarLayout = binding.toolbarLayout.appBar

        //View pager para los fragments (Solo 1 fragment en esta app)
        val viewPager2 = binding.toolbarLayout.viewPager
        viewPager2.adapter = MainFragmentStateAdapter(this, applicationContext)

        //Seteo de tabs.
        val tabLayout = binding.toolbarLayout.tabs
        TabLayoutMediator(tabLayout, viewPager2) { tab: TabLayout.Tab, position: Int ->
            tab.text = MainFragmentStateAdapter.tabs[position]
        }.attach()
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab != null) {
                when (i) {
                    0 -> tab.setIcon(R.drawable.ic_quakes_24dp)
                    1 -> tab.setIcon(R.drawable.ic_report_24dp)
                    2 -> tab.setIcon(R.drawable.ic_map_24dp)
                }
            }
        }

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mAppBarLayout!!.setExpanded(tab.position != 2)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        //Setear collapsing toolbar con titulo estatico superior y animacion de colores al recoger
        // toolbar
        val mCollapsingToolbarLayout = binding.toolbarLayout.collapsingToolbar
        mCollapsingToolbarLayout.isTitleEnabled = true
        val modeNightType = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        //Detecta modo noche automatico como YES
        if (modeNightType == Configuration.UI_MODE_NIGHT_YES) {
            mCollapsingToolbarLayout.setContentScrimColor(
                resources.getColor(
                    R.color.colorPrimary,
                    theme
                )
            )
        } else if (modeNightType == Configuration.UI_MODE_NIGHT_NO) {
            mCollapsingToolbarLayout.setContentScrimColor(
                resources.getColor(
                    R.color.colorPrimary, theme
                )
            )
        }
    }

    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private fun loadImageToolbar() {

        mIvFoto = binding.toolbarLayout.toolbarImage
        Glide.with(this)
            .load(R.drawable.foto)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.not_found)
            )
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    mIvFoto!!.setImageDrawable(
                        ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.not_found
                        )
                    )
                    return false
                }

                //No es necesario usarlo (If u want)
                override fun onResourceReady(
                    resource: Drawable?, model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(mIvFoto!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.settings_menu) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UpdaterService.UPDATE_CODE) {
            if (resultCode == RESULT_OK) {
                Timber.i(getString(R.string.UPDATE_OK))
            } else {
                Timber.e(getString(R.string.UPDATE_FAILED), resultCode)
            }
        }
    }
}