package cl.figonzal.lastquakechile.quake_feature.ui.dialog

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cl.figonzal.lastquakechile.core.utils.shareIgFeedIntent
import cl.figonzal.lastquakechile.core.utils.shareIgStoriesIntent
import cl.figonzal.lastquakechile.core.utils.shareQuake
import cl.figonzal.lastquakechile.core.utils.views.configGoogleMapShareQuakeBorders
import cl.figonzal.lastquakechile.core.utils.views.configSensitive
import cl.figonzal.lastquakechile.core.utils.views.configVerified
import cl.figonzal.lastquakechile.core.utils.views.formatFilterColor
import cl.figonzal.lastquakechile.core.utils.views.formatMagnitude
import cl.figonzal.lastquakechile.core.utils.views.formatQuakeTime
import cl.figonzal.lastquakechile.core.utils.views.getLocalBitmapUri
import cl.figonzal.lastquakechile.core.utils.views.viewToBitmap
import cl.figonzal.lastquakechile.databinding.ShareQuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


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
        val screenshotDrawable = Drawable.createFromStream(inputStream, quakeBitmapUri.toString())

        with(binding) {

            configGoogleMapShareQuakeBorders(screenshotDrawable)

            with(includeShareQuake.sheetContent) {
                tvCity.text = quake.city
                tvMagnitude.formatMagnitude(quake)
                tvDate.formatQuakeTime(quake, true)
                tvReference.text = quake.reference
                ivMagColor.formatFilterColor(requireContext(), quake)

                ivVerified.configVerified(quake)
                ivSensitive.configSensitive(quake)
            }



            btnShareIgStory.setOnClickListener {
                val bitmap = includeShareQuake.cvShareQuake.viewToBitmap()
                val bitMapUriView = requireContext().getLocalBitmapUri(bitmap)

                requireContext().shareIgStoriesIntent(bitMapUriView)
            }

            btnShareIgFeed.setOnClickListener {
                requireContext().shareIgFeedIntent(quakeBitmapUri)
            }

            btnShareLink.setOnClickListener {

            }

            btnShareMore.setOnClickListener {
                requireContext().shareQuake(quake, quakeBitmapUri)
            }
            return root
        }
    }
}