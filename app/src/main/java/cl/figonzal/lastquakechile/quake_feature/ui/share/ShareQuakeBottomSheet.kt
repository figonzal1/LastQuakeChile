package cl.figonzal.lastquakechile.quake_feature.ui.share

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.BundleCompat
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.copyQuakeText
import cl.figonzal.lastquakechile.core.utils.isInstagramStoriesAvailable
import cl.figonzal.lastquakechile.core.utils.isWhatsAppAvailable
import cl.figonzal.lastquakechile.core.utils.shareQuakeGeneric
import cl.figonzal.lastquakechile.core.utils.shareQuakeToInstagramStory
import cl.figonzal.lastquakechile.core.utils.shareQuakeToWhatsApp
import cl.figonzal.lastquakechile.core.utils.views.toast
import cl.figonzal.lastquakechile.databinding.ItemShareBackgroundBinding
import cl.figonzal.lastquakechile.databinding.ItemShareDestinationBinding
import cl.figonzal.lastquakechile.databinding.ShareQuakeBottomSheetBinding
import cl.figonzal.lastquakechile.quake_feature.domain.model.Quake
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private const val ARG_QUAKE = "arg_quake"
private const val ARG_STICKER_URIS = "arg_sticker_uris"
private const val ARG_MAGNITUDE_COLOR = "arg_magnitude_color"
private const val ARG_SELECTED_BACKGROUND = "arg_selected_background"

private const val INSTAGRAM_PACKAGE = "com.instagram.android"
private const val WHATSAPP_PACKAGE = "com.whatsapp"

class ShareQuakeBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "share_quake_bottom_sheet"

        /** [stickerUris] must have one entry per [StickerDesign], in [StickerDesign.entries] order. */
        fun newInstance(
            quake: Quake,
            stickerUris: List<Uri>,
            @ColorInt magnitudeColor: Int
        ) = ShareQuakeBottomSheet().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_QUAKE, quake)
                putParcelableArrayList(ARG_STICKER_URIS, ArrayList(stickerUris))
                putInt(ARG_MAGNITUDE_COLOR, magnitudeColor)
            }
        }
    }

    private var _binding: ShareQuakeBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val quake: Quake by lazy {
        BundleCompat.getParcelable(requireArguments(), ARG_QUAKE, Quake::class.java)!!
    }
    private val stickerUris: List<Uri> by lazy {
        BundleCompat.getParcelableArrayList(requireArguments(), ARG_STICKER_URIS, Uri::class.java)!!
    }
    @get:ColorInt
    private val magnitudeColor: Int by lazy { requireArguments().getInt(ARG_MAGNITUDE_COLOR) }

    private lateinit var stickerAdapter: ShareStickerPagerAdapter

    /** The design/background pickers are independent axes - the background survives a swipe. */
    private var selectedBackground: StickerBackground = StickerBackground.SOLID

    private val designButtonIds by lazy {
        listOf(binding.btnDesignCard.id, binding.btnDesignMagnitude.id)
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.toggleShareDesigns.check(designButtonIds[position])
        }
    }

    /** The sticker design currently visible in the carousel - that's the one that gets shared. */
    private val selectedStickerUri: Uri
        get() = stickerUris[binding.pagerShareDesigns.currentItem]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ShareQuakeBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState
            ?.getString(ARG_SELECTED_BACKGROUND)
            ?.let { selectedBackground = StickerBackground.valueOf(it) }

        stickerAdapter = ShareStickerPagerAdapter(stickerUris, magnitudeColor).apply {
            background = selectedBackground
        }
        with(binding.pagerShareDesigns) {
            adapter = stickerAdapter
            // Keep the neighbouring designs laid out so they actually render in the peek area.
            offscreenPageLimit = 1
            setPageTransformer(
                MarginPageTransformer(resources.getDimensionPixelSize(R.dimen.share_design_page_margin))
            )
            registerOnPageChangeCallback(pageChangeCallback)
        }

        with(binding.toggleShareDesigns) {
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) binding.pagerShareDesigns.currentItem = designButtonIds.indexOf(checkedId)
            }
            check(designButtonIds[binding.pagerShareDesigns.currentItem])
        }

        configBackground(binding.bgSolid, StickerBackground.SOLID)
        configBackground(binding.bgGradient, StickerBackground.GRADIENT)
        refreshBackgroundSelection()

        configDestination(
            item = binding.destInstagram,
            icon = resolveAppIcon(INSTAGRAM_PACKAGE),
            labelRes = R.string.SHARE_DEST_INSTAGRAM_STORIES,
            isAvailable = requireContext().isInstagramStoriesAvailable()
        ) {
            val (topColor, bottomColor) = selectedBackground.storyColors(magnitudeColor)
            val sent = requireContext().shareQuakeToInstagramStory(selectedStickerUri, topColor, bottomColor)
            if (!sent) requireContext().toast(R.string.SHARE_IG_NOT_AVAILABLE)
            dismiss()
        }

        configDestination(
            item = binding.destWhatsapp,
            icon = resolveAppIcon(WHATSAPP_PACKAGE),
            labelRes = R.string.SHARE_DEST_WHATSAPP,
            isAvailable = requireContext().isWhatsAppAvailable()
        ) {
            requireContext().shareQuakeToWhatsApp(quake, selectedStickerUri)
            dismiss()
        }

        configDestination(
            item = binding.destCopy,
            iconRes = R.drawable.round_content_copy_24,
            labelRes = R.string.SHARE_DEST_COPY,
            isAvailable = true
        ) {
            requireContext().copyQuakeText(quake)
            requireContext().toast(R.string.SHARE_COPIED_TOAST)
            dismiss()
        }

        configDestination(
            item = binding.destMore,
            iconRes = R.drawable.round_share_24,
            labelRes = R.string.SHARE_DEST_MORE,
            isAvailable = true
        ) {
            requireContext().shareQuakeGeneric(quake, selectedStickerUri)
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_SELECTED_BACKGROUND, selectedBackground.name)
    }

    private fun configDestination(
        item: ItemShareDestinationBinding,
        @StringRes labelRes: Int,
        isAvailable: Boolean,
        icon: Drawable? = null,
        @DrawableRes iconRes: Int? = null,
        onClick: () -> Unit
    ) {
        if (!isAvailable) {
            item.root.visibility = View.GONE
            return
        }

        item.tvDestLabel.setText(labelRes)
        when {
            icon != null -> item.ivDestIcon.setImageDrawable(icon)
            iconRes != null -> item.ivDestIcon.setImageResource(iconRes)
        }
        item.root.setOnClickListener { onClick() }
    }

    private fun configBackground(item: ItemShareBackgroundBinding, background: StickerBackground) {
        item.ivBackgroundDot.setImageDrawable(
            background.previewDrawable(magnitudeColor, GradientDrawable.OVAL)
        )
        item.root.contentDescription = getString(background.labelRes)
        item.root.setOnClickListener {
            selectedBackground = background
            stickerAdapter.background = background
            refreshBackgroundSelection()
        }
    }

    private fun refreshBackgroundSelection() {
        binding.bgSolid.root.isSelected = selectedBackground == StickerBackground.SOLID
        binding.bgGradient.root.isSelected = selectedBackground == StickerBackground.GRADIENT
    }

    private fun resolveAppIcon(packageName: String): Drawable? = try {
        requireContext().packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    override fun onDestroyView() {
        binding.pagerShareDesigns.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
        _binding = null
    }
}
