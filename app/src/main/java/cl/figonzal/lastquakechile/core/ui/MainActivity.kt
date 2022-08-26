@file:Suppress("unused")

package cl.figonzal.lastquakechile.core.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
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
import cl.figonzal.lastquakechile.core.utils.loadImage
import cl.figonzal.lastquakechile.core.utils.startAds
import cl.figonzal.lastquakechile.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private var updaterService: UpdaterService? = null
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory()
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Default preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val sharedPrefUtil = SharedPrefUtil(applicationContext)

        //Night mode
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                Timber.d("ANDROID_VERSION < Q: ${Build.VERSION.SDK_INT}")
                lifecycle.addObserver(NightModeService(this))
            }
            else -> {
                Timber.d("ANDROID_VERSION > Q: ${Build.VERSION.SDK_INT}")
            }
        }

        //GP services
        lifecycle.addObserver(GooglePlayService(this))

        //ChangeLog Service
        lifecycle.addObserver(ChangeLogService(this, SharedPrefUtil(this)))

        //Firebase services
        getFirebaseToken()

        //Ads service
        adView = startAds(binding.adViewContainer)

        //Updater service
        updaterService = UpdaterService(this, AppUpdateManagerFactory.create(this))
        updaterService!!.checkAvailability()

        setUpNotificationService(sharedPrefUtil)

        setToolbarViewPagerTabs()

        loadImage(R.drawable.foto, binding.toolbarLayout.toolbarImage)
    }

    private fun setUpNotificationService(sharedPrefUtil: SharedPrefUtil) {

        QuakesNotification(this, sharedPrefUtil).apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> createChannel()
            }

            subscribedToQuakes(true)
        }
    }

    private fun setToolbarViewPagerTabs() {

        with(binding.toolbarLayout) {

            setSupportActionBar(toolbarMain.toolBar)

            collapsingToolbar.isTitleEnabled = true
            collapsingToolbar.setContentScrimColor(
                getColor(R.color.colorPrimary)
            )

            //View pager for fragments
            viewPager.apply {
                adapter = MainFragmentStateAdapter(this@MainActivity, context)
                setTabs(tabs, appBar)
            }
        }

    }

    private fun setTabs(tabLayout: TabLayout, appBar: AppBarLayout) {

        with(binding.toolbarLayout) {

            //Setting tabs
            TabLayoutMediator(tabs, viewPager) { tab: TabLayout.Tab, position: Int ->
                tab.text = MainFragmentStateAdapter.tabs[position]

                when (position) {
                    0 -> {
                        tab.setIcon(R.drawable.ic_round_campaign_24)
                        hideAdBanner(true)
                    }
                    1 -> tab.setIcon(R.drawable.ic_quakes_24dp)
                    2 -> tab.setIcon(R.drawable.ic_round_place_24)
                    3 -> tab.setIcon(R.drawable.ic_round_analytics_24)
                }
            }.attach()

            wrapFirstTab(tabLayout)

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {

                    when (tab.position) {
                        0 -> hideAdBanner(true)
                        else -> hideAdBanner(false)
                    }

                    when {
                        tab.position != 0 || tab.position != 2 -> appBar.setExpanded(false)
                        else -> appBar.setExpanded(true)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        }
    }

    private fun wrapFirstTab(tabLayout: TabLayout) {
        val tabStrip = tabLayout.getChildAt(0)
        if (tabStrip is ViewGroup) {
            val tabView = tabStrip.getChildAt(0) // 0th position tab i.e 1st tab
            tabView.minimumWidth = 0
            tabView.setPadding(16, tabView.paddingTop, 16, tabView.paddingBottom)
            tabView.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            tabLayout.requestLayout()
        }
    }

    private fun hideAdBanner(hide: Boolean) {
        binding.adViewContainer.visibility = when (hide) {
            true -> View.GONE
            false -> View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UpdaterService.UPDATE_CODE) {
            when (resultCode) {
                RESULT_OK -> Timber.d(getString(R.string.UPDATE_OK))
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
        updaterService?.resumeUpdater()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}