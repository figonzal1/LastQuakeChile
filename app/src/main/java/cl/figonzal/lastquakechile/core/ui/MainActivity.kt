package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.GooglePlayService
import cl.figonzal.lastquakechile.core.services.NightModeService
import cl.figonzal.lastquakechile.core.services.UpdaterService
import cl.figonzal.lastquakechile.core.services.notifications.QuakesNotification
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.getFirebaseToken
import cl.figonzal.lastquakechile.databinding.ActivityMainBinding
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
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var updaterService: UpdaterService? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val sharedPrefUtil = SharedPrefUtil(applicationContext)

        //Night mode
        lifecycle.addObserver(NightModeService(this))

        //GP services
        lifecycle.addObserver(GooglePlayService(this))

        //Firebase services
        getFirebaseToken()

        //Updater service
        //updaterService = UpdaterService(this, AppUpdateManagerFactory.create(this))
        //updaterService!!.checkAvailability()

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        setUpNotificationService(sharedPrefUtil)

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs()

        //Setear imagen de toolbar
        loadImageToolbar()
    }

    private fun setUpNotificationService(sharedPrefUtil: SharedPrefUtil) {

        QuakesNotification(this, sharedPrefUtil).apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> createChannel()
            }

            suscribedToQuakes(true)
        }
    }

    /**
     * Setear elementos de UI necesarios para el funcionamiento de la APP
     */
    private fun setToolbarViewPagerTabs() {

        with(binding.toolbarLayout) {

            //Buscar toolbar en resources
            setSupportActionBar(toolbarMain.toolBar)

            //View pager para los fragments (Solo 1 fragment en esta app)
            viewPager.apply {
                adapter = MainFragmentStateAdapter(this@MainActivity, applicationContext)
                setTabs(tabs, appBar)
            }
        }

    }

    private fun setTabs(tabLayout: TabLayout, appBar: AppBarLayout) {

        with(binding.toolbarLayout) {

            //Seteo de tabs.
            TabLayoutMediator(tabs, viewPager) { tab: TabLayout.Tab, position: Int ->
                tab.text = MainFragmentStateAdapter.tabs[position]
            }.attach()

            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.apply {

                    when (i) {
                        0 -> setIcon(R.drawable.ic_quakes_24dp)
                        1 -> setIcon(R.drawable.ic_report_24dp)
                        2 -> setIcon(R.drawable.ic_map_24dp)
                    }
                }
            }

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    appBar.setExpanded(tab.position != 2)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        }
    }


    /**
     * Funcion encargada de cargar la imagen de fondo en el toolbar
     */
    private fun loadImageToolbar() {

        with(binding.toolbarLayout.toolbarImage) {

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

                        setImageDrawable(
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
                .into(this@with)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.settings_menu) {

            Intent(this@MainActivity, SettingsActivity::class.java).apply {
                startActivity(this)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UpdaterService.UPDATE_CODE) {
            when (resultCode) {
                RESULT_OK -> Timber.i(getString(R.string.UPDATE_OK))
                else -> Timber.e(getString(R.string.UPDATE_FAILED), resultCode)
            }
        }
    }
}