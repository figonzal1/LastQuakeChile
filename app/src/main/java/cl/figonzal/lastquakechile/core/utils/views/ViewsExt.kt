package cl.figonzal.lastquakechile.core.utils.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.data.remote.ApiError
import cl.figonzal.lastquakechile.core.data.remote.ApiError.*
import cl.figonzal.lastquakechile.core.ui.SettingsActivity
import cl.figonzal.lastquakechile.core.utils.latLongToDMS
import cl.figonzal.lastquakechile.core.utils.localDateToDHMS
import cl.figonzal.lastquakechile.core.utils.stringToLocalDateTime
import cl.figonzal.lastquakechile.core.utils.views.*
import cl.figonzal.lastquakechile.databinding.FragmentMapsBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Coordinate
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import coil.load
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.floor


fun Fragment.configOptionsMenu(
    @MenuRes menuId: Int = R.menu.menu_main,
    fragmentIndex: Int = 0,
    callback: (MenuItem) -> Unit
) {
    val menuHost: MenuHost = this.requireActivity()

    menuHost.addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuId, menu)

            val refreshItem = menu.findItem(R.id.refresh_menu)
            val layerItem = menu.findItem(R.id.layers_menu)

            when (fragmentIndex) {

                //QuakeList
                1, 3 -> {
                    refreshItem.isVisible = true
                    layerItem.isVisible = false
                }

                //Map List
                2 -> {
                    refreshItem.isVisible = false
                    layerItem.isVisible = true
                }

                else -> {
                    refreshItem.isVisible = false
                    layerItem.isVisible = false
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

            //Settings called in all fragments
            when (menuItem.itemId) {
                R.id.status_menu -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.CRONITOR_STATUS))
                    ).apply {
                        startActivity(this)
                    }

                }

                R.id.settings_menu -> {
                    Intent(requireActivity(), SettingsActivity::class.java).apply {
                        startActivity(this)
                    }
                }
            }

            callback(menuItem)
            return true
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
}

/**
 * Function that sets background colors depending on the magnitude of the earthquake
 *
 * @param magnitude Quake magnitude
 * @return id color resource
 */
fun getMagnitudeColor(magnitude: Double, forMapa: Boolean) = when {
    forMapa -> getColorResourceMap(floor(magnitude).toInt())
    else -> getColorResource(floor(magnitude).toInt())
}

private fun getColorResource(mMagFloor: Int) = when {
    mMagFloor == 1 -> R.color.magnitude1
    mMagFloor == 2 -> R.color.magnitude2
    mMagFloor == 3 -> R.color.magnitude3
    mMagFloor == 4 -> R.color.magnitude4
    mMagFloor == 5 -> R.color.magnitude5
    mMagFloor == 6 -> R.color.magnitude6
    mMagFloor == 7 -> R.color.magnitude7
    mMagFloor >= 8 -> R.color.magnitude8
    else -> R.color.colorPrimary
}

private fun getColorResourceMap(mMagFloor: Int) = when {
    mMagFloor == 1 -> R.color.magnitude1_alpha
    mMagFloor == 2 -> R.color.magnitude2_alpha
    mMagFloor == 3 -> R.color.magnitude3_alpha
    mMagFloor == 4 -> R.color.magnitude4_alpha
    mMagFloor == 5 -> R.color.magnitude5_alpha
    mMagFloor == 6 -> R.color.magnitude6_alpha
    mMagFloor == 7 -> R.color.magnitude7_alpha
    mMagFloor >= 8 -> R.color.magnitude8_alpha
    else -> R.color.colorPrimary
}

/**
 * Inflater any view
 */
fun ViewGroup.layoutInflater(layout: Int): View =
    LayoutInflater.from(context).inflate(layout, this, false)

/**
 * Setting scale textview depending on the value of string
 *
 * @param scale Quake scale
 */
fun TextView.setScale(scale: String) {
    text = when {
        scale.contains("Mw") -> String.format(
            QUAKE_DETAILS_SCALE_FORMAT,
            context.getString(R.string.moment_magnitude)
        )

        else -> String.format(
            QUAKE_DETAILS_SCALE_FORMAT,
            context.getString(R.string.local_magnitude)
        )
    }
}

/**
 * Extension for toast
 */
fun Activity.toast(stringId: Int) {
    Toast.makeText(
        this,
        getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.toast(stringId: Int) {
    Toast.makeText(
        requireContext(),
        getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}

fun Context.toast(stringId: Int) {
    Toast.makeText(
        this,
        getString(stringId),
        Toast.LENGTH_LONG
    ).show()
}


/**
 * Transform localDateTime to a string text in (days or hours or minutes)
 */
fun TextView.timeToText(quake: Quake, isShortVersion: Boolean = false) {

    val timeMap = quake.localDate.stringToLocalDateTime().localDateToDHMS()
    val days = timeMap[DAYS]

    text = when {
        days != null && days == 0L -> {
            calculateTextViewBelowDay(this.context, timeMap, isShortVersion)
        }

        days != null && days > 0 -> {
            calculateTextViewAboveDay(this.context, timeMap, isShortVersion)
        }

        else -> ""
    }
}

private fun calculateTextViewAboveDay(
    context: Context,
    timeMap: Map<String, Long>,
    isShortVersion: Boolean
): String {

    val days = timeMap[DAYS]
    val hour = timeMap[HOURS]

    return when {
        hour != null && hour == 0L -> when {
            isShortVersion -> String.format(
                QUAKETIME_D_FORMAT,
                days
            )

            else -> String.format(
                context.getString(R.string.quake_time_day_info_windows),
                days
            )
        }

        hour != null && hour >= 1 -> when {
            isShortVersion -> String.format(
                QUAKETIME_DH_FORMAT,
                days,
                hour / 24
            )

            else -> String.format(
                context.getString(R.string.quake_time_day_hour_info_windows),
                days,
                hour / 24
            )
        }

        else -> ""
    }
}

private fun calculateTextViewBelowDay(
    context: Context,
    timeMap: Map<String, Long>,
    isShortVersion: Boolean
): String {

    val hour = timeMap[HOURS]
    val min = timeMap[MINUTES]
    val seg = timeMap[SECONDS]

    return when {
        hour != null && hour >= 1 -> when {
            isShortVersion -> String.format(QUAKETIME_H_FORMAT, hour)
            else -> String.format(context.getString(R.string.quake_time_hour_info_windows), hour)
        }

        else -> when {
            min != null && min < 1 -> when {
                isShortVersion -> String.format(
                    QUAKETIME_S_FORMAT, seg
                )

                else -> String.format(
                    context.getString(R.string.quake_time_second_info_windows), seg
                )
            }

            else -> when {
                isShortVersion -> String.format(
                    QUAKETIME_M_FORMAT,
                    min
                )

                else -> String.format(
                    context.getString(R.string.quake_time_minute_info_windows),
                    min
                )
            }
        }
    }
}

/**
 * Coordinates to DMS
 */
fun TextView.formatDMS(coordinates: Coordinate) {

    val latDMS = coordinates.latitude.latLongToDMS()
    val degreeLat = latDMS["grados"]
    val minLat = latDMS["minutos"]
    val segLat = latDMS["segundos"]

    val dmsLat = String.format(
        Locale.US,
        "%.1f° %.1f' %.1f'' %s",
        degreeLat,
        minLat,
        segLat,
        when {
            coordinates.latitude < 0 -> this.context.getString(R.string.south_cords)
            else -> this.context.getString(R.string.north_cords)
        }
    )

    val longDMS = coordinates.longitude.latLongToDMS()
    val degreeLong = longDMS["grados"]
    val minLong = longDMS["minutos"]
    val segLong = longDMS["segundos"]

    val dmsLong = String.format(
        Locale.US,
        "%.1f° %.1f' %.1f'' %s",
        degreeLong,
        minLong,
        segLong,
        when {
            coordinates.longitude < 0 -> this.context.getString(R.string.west_cords)
            else -> this.context.getString(R.string.east_cords)
        }
    )

    text = String.format(QUAKE_CORDS_FORMAT, dmsLat, dmsLong)
}

/**
 * Save snapshot from google map in cache directory
 */
@Throws(IOException::class)
fun Context.getLocalBitmapUri(bitmap: Bitmap): Uri {

    val c = Calendar.getInstance()
    val date = c.timeInMillis.toInt()
    val file = File(cacheDir, "share$date.jpeg")

    when {
        file.exists() -> Timber.d("Share image exist")
        else -> {
            Timber.d("Share image not exist")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
        }
    }
    return getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
}

/**
 * Function to print change log features
 */
fun List<String>.printChangeLogList(): CharSequence {
    var changes = ""

    this.indices.forEach { i ->

        changes = when {
            i > 0 -> changes.plus("\n" + this[i])
            else -> changes.plus(this[i])
        }
    }
    return changes
}

fun ImageView.loadImage(url: Int) {
    load(url) {
        crossfade(true)
        placeholder(R.drawable.placeholder)
        error(R.drawable.not_found)
    }
}

fun Context.getMonth(month: Int) = arrayOf(
    getString(R.string.JAN),
    getString(R.string.FEB),
    getString(R.string.MAR),
    getString(R.string.APR),
    getString(R.string.MAY),
    getString(R.string.JUN),
    getString(R.string.JUL),
    getString(R.string.AUG),
    getString(R.string.SEP),
    getString(R.string.OCT),
    getString(R.string.NOV),
    getString(R.string.DEC)
)[month - 1]

fun Fragment.showServerApiError(apiError: ApiError, callback: (Int, String) -> Unit) =
    when (apiError) {
        IoError, NoWifiError -> {
            toast(R.string.io_error)
            callback(R.drawable.round_wifi_off_24, getString(R.string.io_error))
        }

        ServerError, TimeoutError -> {
            toast(R.string.service_error)
            callback(R.drawable.round_router_24, getString(R.string.service_error))
        }

        NoMoreData -> toast(R.string.no_more_data)
        EmptyList -> callback(R.drawable.round_outlined_flag_24, getString(R.string.empty_list))
        else -> {
            toast(R.string.http_error)
            callback(R.drawable.round_report_24, getString(R.string.http_error))
        }
    }

fun ViewPager2.handleShortcuts(action: String?, packageName: String) {
    when {
        action.equals("${packageName}.LIST") -> setCurrentItem(1, true)
        action.equals("${packageName}.MAP") -> setCurrentItem(2, true)
        action.equals("${packageName}.REPORT") -> setCurrentItem(3, true)
    }
}

fun BottomSheetBehavior<MaterialCardView>.handleBottomSheetState() {
    state = when (state) {
        BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_EXPANDED
        else -> BottomSheetBehavior.STATE_COLLAPSED
    }
}

fun BottomSheetBehavior<MaterialCardView>.configBottomSheetCallback(
    p0: GoogleMap,
    binding: FragmentMapsBinding
) = object : BottomSheetBehavior.BottomSheetCallback() {

    override fun onSlide(bottomSheet: View, slideOffset: Float) {

        if (state == BottomSheetBehavior.STATE_DRAGGING ||
            state == BottomSheetBehavior.STATE_SETTLING
        ) {
            p0.adjustMapPadding(binding)
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        // Needed only in case you manually change the bottomsheet's state in code somewhere.
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                // Nothing to do here
            }

            else -> p0.adjustMapPadding(binding)
        }
    }
}

private fun GoogleMap.adjustMapPadding(binding: FragmentMapsBinding) {
    val bottomSheetContainerHeight = binding.include.root.height
    val currentBottomSheetTop = binding.include.cvBottomSheet.top

    this.setPadding(
        0, // left
        0, // top
        0, // right
        bottomSheetContainerHeight - currentBottomSheetTop // bottom
    )
}

fun Float.toDips(resources: Resources) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

fun ViewGroup.getViewBottomHeight(
    targetViewId: Int,
    behavior: BottomSheetBehavior<MaterialCardView>?
) {

    val callback = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            behavior?.peekHeight =
                findViewById<View>(targetViewId).bottom + 20f.toDips(resources).toInt()
        }
    }

    viewTreeObserver.addOnGlobalLayoutListener(callback)
}

