@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.ChangeLogService
import cl.figonzal.lastquakechile.core.services.GooglePlayService
import cl.figonzal.lastquakechile.core.services.NightModeService
import cl.figonzal.lastquakechile.core.services.UpdaterService
import cl.figonzal.lastquakechile.core.services.notifications.QuakesNotification
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.getFirebaseToken
import cl.figonzal.lastquakechile.core.utils.setTabWidthAsWrapContent
import cl.figonzal.lastquakechile.core.utils.startAds
import cl.figonzal.lastquakechile.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private var updaterService: UpdaterService? = null
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testDeviceIds = Arrays.asList("AADCDB8868D6F854F75225420A8F220E")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        //Setear configuraciones por defecto de ConfigActivity
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val sharedPrefUtil = SharedPrefUtil(applicationContext)

        //Night mode
        lifecycle.addObserver(NightModeService(this))

        //GP services
        lifecycle.addObserver(GooglePlayService(this))

        //ChangeLog Service
        lifecycle.addObserver(ChangeLogService(this, SharedPrefUtil(this)))

        //Firebase services
        getFirebaseToken()

        //Ads service
        MobileAds.initialize(this)
        adView = startAds(binding.adViewContainer)

        //Updater service
        //updaterService = UpdaterService(this, AppUpdateManagerFactory.create(this))
        //updaterService!!.checkAvailability()

        //Creacion de canal de notificaciones para sismos y para changelogs (Requerido para API >26)
        setUpNotificationService(sharedPrefUtil)

        //Setear toolbars, viewpagers y tabs
        setToolbarViewPagerTabs()

        //Setear imagen de toolbar
        loadImageToolbar()

        MobileAds.openAdInspector(this) { }
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

            collapsingToolbar.isTitleEnabled = true
            collapsingToolbar.setContentScrimColor(
                getColor(R.color.colorPrimary)
            )

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
                        1 -> setIcon(R.drawable.ic_baseline_campaign_24)
                        2 -> setIcon(R.drawable.ic_map_24dp)
                        3 -> setIcon(R.drawable.ic_report_24dp)

                    }
                }

            }

            tabLayout.setTabWidthAsWrapContent(1)

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {

                    when (tab.position) {
                        1 -> hideAdBanner(true)
                        else -> hideAdBanner(false)
                    }

                    when {
                        tab.position != 1 || tab.position != 2 -> appBar.setExpanded(false)
                        else -> appBar.setExpanded(true)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        }
    }

    private fun hideAdBanner(hide: Boolean) {
        binding.adViewContainer.visibility = when (hide) {
            true -> View.GONE
            false -> View.VISIBLE
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
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.settings_menu -> {

                Intent(this@MainActivity, SettingsActivity::class.java).apply {
                    startActivity(this)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    /** Called when leaving the activity  */
    public override fun onPause() {
        adView.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()
        adView.resume()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}