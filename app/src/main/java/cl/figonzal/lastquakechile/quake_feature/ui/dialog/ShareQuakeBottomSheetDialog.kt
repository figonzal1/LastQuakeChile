package cl.figonzal.lastquakechile.quake_feature.ui.dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cl.figonzal.lastquakechile.core.utils.views.formatFilterColor
import cl.figonzal.lastquakechile.core.utils.views.formatMagnitude
import cl.figonzal.lastquakechile.core.utils.views.formatQuakeTime
import cl.figonzal.lastquakechile.core.utils.views.getLocalBitmapUri
import cl.figonzal.lastquakechile.databinding.ShareQuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel


class ShareQuakeBottomSheetDialog(private val quakeBitmapUri: Uri, private val quake: Quake) :
    BottomSheetDialogFragment() {

    private var _binding: ShareQuakeBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ShareQuakeBottomSheetBinding.inflate(layoutInflater, container, false)

        val inputStream = requireContext().contentResolver.openInputStream(quakeBitmapUri)
        val drawable = Drawable.createFromStream(inputStream, quakeBitmapUri.toString())

        with(binding.includeShareQuake) {

            val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 32f)
                .setTopRightCorner(CornerFamily.ROUNDED, 32f)
                .build()
            ivGoogleMaps.setImageDrawable(drawable)
            ivGoogleMaps.shapeAppearanceModel = shapeAppearanceModel

            with(sheetContent) {
                tvCity.text = quake.city
                tvMagnitude.formatMagnitude(quake)
                tvDate.formatQuakeTime(quake, true)
                tvReference.text = quake.reference
                ivMagColor.formatFilterColor(requireContext(), quake)
            }
        }

        binding.btnShareIgStory.setOnClickListener {
            instagramShareIntent()
        }

        return binding.root
    }

    private fun instagramShareIntent() {

        val bitmap = viewToBitmap(binding.includeShareQuake.cvShareQuake)
        val bitMapUriView = requireContext().getLocalBitmapUri(bitmap)

        requireContext().grantUriPermission(
            "com.instagram.android",
            bitMapUriView,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {

            type = "image/*"
            putExtra("source_application", "740961793640508")
            putExtra("interactive_asset_uri", bitMapUriView)
            putExtra("top_background_color", "#006994");
            putExtra("bottom_background_color", "#006994");
        }
        startActivity(intent)
    }

    private fun viewToBitmap(view: View): Bitmap {
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val totalHeight = view.height
        val totalWidth = view.width
        val percent = 1.0f //use this value to scale bitmap to specific size


        val canvasBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        canvasBitmap.eraseColor(Color.RED)
        val canvas = Canvas(canvasBitmap)
        canvas.scale(percent, percent)
        view.draw(canvas)

        return canvasBitmap
    }
}