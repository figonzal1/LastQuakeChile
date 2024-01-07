package cl.figonzal.lastquakechile.core.utils.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
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
import cl.figonzal.lastquakechile.core.utils.views.*
import cl.figonzal.lastquakechile.databinding.FragmentMapsBinding
import cl.figonzal.lastquakechile.databinding.ShareQuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
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

/**
 * Inflater any view
 */
fun ViewGroup.layoutInflater(layout: Int): View =
    LayoutInflater.from(context).inflate(layout, this, false)

fun ImageView.configSensitive(quake: Quake) {
    visibility = when {
        quake.isSensitive -> View.VISIBLE
        else -> View.GONE
    }
}

fun ImageView.configVerified(quake: Quake) {
    visibility = when {
        quake.isVerified -> View.VISIBLE
        else -> View.GONE
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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
        }
    }
    return getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
}

fun View.viewToBitmap(): Bitmap {

    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888, true
        )
    } else {
        Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

fun ShareQuakeBottomSheetBinding.configGoogleMapShareQuakeBorders(drawable: Drawable?) {
    val shapeAppearanceModel = ShapeAppearanceModel()
        .toBuilder()
        .setTopLeftCorner(CornerFamily.ROUNDED, 32f)
        .setTopRightCorner(CornerFamily.ROUNDED, 32f)
        .build()
    includeShareQuake.ivGoogleMaps.setImageDrawable(drawable)
    includeShareQuake.ivGoogleMaps.shapeAppearanceModel = shapeAppearanceModel
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

/**
 * Load image to imageView from URL
 *
 * @param url
 * @param imageView
 * @return
 */
fun Context.loadImage(url: Int, imageView: ImageView) = Glide.with(this)
    .load(url)
    .centerCrop()
    .error(R.drawable.not_found)
    .placeholder(R.drawable.placeholder)
    .transition(
        DrawableTransitionOptions.withCrossFade(
            DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        )
    )
    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    .into(imageView)


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

/**
 * Get color resources depending on magnitude value
 */
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

/**
 * Get color resources depending on magnitude value
 */
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

