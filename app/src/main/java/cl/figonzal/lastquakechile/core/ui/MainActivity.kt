package cl.figonzal.lastquakechile.core.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.services.UpdaterService
import cl.figonzal.lastquakechile.core.services.notifications.utils.getFirebaseToken
import cl.figonzal.lastquakechile.core.services.notifications.utils.setUpNotificationService
import cl.figonzal.lastquakechile.core.utils.SharedPrefUtil
import cl.figonzal.lastquakechile.core.utils.checkEULAConsentAds
import cl.figonzal.lastquakechile.core.utils.initLifecycleObservers
import cl.figonzal.lastquakechile.core.utils.startAds
import cl.figonzal.lastquakechile.core.utils.views.handleShortcuts
import cl.figonzal.lastquakechile.core.utils.views.loadImage
import cl.figonzal.lastquakechile.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var adView: AdView? = null
    private var updaterService: UpdaterService? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setupKoinFragmentFactory()
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Default preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        val sharedPrefUtil = SharedPrefUtil(applicationContext)

        initServices(sharedPrefUtil)
        setToolbarViewPagerTabs()
        binding.toolbarLayout.toolbarImage.loadImage(R.drawable.foto)
    }

    private fun initServices(sharedPrefUtil: SharedPrefUtil) {
        initLifecycleObservers(sharedPrefUtil)

        getFirebaseToken()

        checkEULAConsentAds {
            MobileAds.initialize(this)

            //Ads
            adView = startAds(binding.adViewContainer)
        }

        //Ads
        adView = startAds(binding.adViewContainer)

        setUpNotificationService(sharedPrefUtil)

        //Updater service
        updaterService = UpdaterService(this, AppUpdateManagerFactory.create(this))
        updaterService?.checkAvailability()
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

                handleShortcuts(intent.action, applicationContext.packageName)
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
                        tab.setIcon(R.drawable.round_campaign_24)
                        hideAdBanner(true)
                        tab.contentDescription = getString(R.string.cd_ad_pager)
                    }

                    1 -> {
                        tab.setIcon(R.drawable.quakes_24dp)
                        tab.contentDescription =
                            getString(R.string.cd_quakes_pager)
                    }

                    2 -> {
                        tab.setIcon(R.drawable.round_place_24)
                        hideAdBanner(true)
                        tab.contentDescription = getString(R.string.cd_map_pager)
                    }

                    3 -> {
                        tab.setIcon(R.drawable.round_analytics_24)
                        tab.contentDescription =
                            getString(R.string.cd_reports_pager)
                    }
                }
            }.attach()

            wrapFirstTab(tabLayout)

            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {

                    when (tab.position) {
                        0, 2 -> hideAdBanner(true)
                        else -> hideAdBanner(false)
                    }

                    when {
                        tab.position != 0 || tab.position != 2 -> appBar.setExpanded(false)
                        else -> appBar.setExpanded(true)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) = Unit
                override fun onTabReselected(tab: TabLayout.Tab) = Unit
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
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UpdaterService.UPDATE_CODE) {
            when (resultCode) {
                RESULT_OK -> Timber.d("Lqch-apk updated successfully")
                else -> Timber.e("Lqch-apk update flow failed! Result code: %s", resultCode)
            }
        }
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()
        adView?.resume()
        updaterService?.resumeUpdater()
    }

    /** Called when leaving the activity  */
    public override fun onPause() {
        super.onPause()
        adView?.pause()
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
    }
}