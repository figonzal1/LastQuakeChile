package cl.figonzal.lastquakechile.quake_feature.ui.share

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import cl.figonzal.lastquakechile.R
import cl.figonzal.lastquakechile.core.utils.views.layoutInflater
import cl.figonzal.lastquakechile.databinding.ItemShareStickerPageBinding
import coil3.load

/**
 * Backs the design carousel in [ShareQuakeBottomSheet]. The URI list is fixed once the sheet
 * opens - one already-rendered sticker per [StickerDesign] - so no DiffUtil is needed. The
 * [background] is a separate, mutable axis shared by every page: changing it repaints every
 * canvas without touching the sticker bitmaps themselves.
 */
class ShareStickerPagerAdapter(
    private val stickerUris: List<Uri>,
    @ColorInt private val magnitudeColor: Int
) : RecyclerView.Adapter<ShareStickerPagerAdapter.StickerPageViewHolder>() {

    var background: StickerBackground = StickerBackground.SOLID
        set(value) {
            if (field == value) return
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) =
        StickerPageViewHolder(viewGroup.layoutInflater(R.layout.item_share_sticker_page))

    override fun onBindViewHolder(holder: StickerPageViewHolder, position: Int) =
        holder.bind(stickerUris[position], StickerDesign.entries[position])

    override fun getItemCount(): Int = stickerUris.size

    inner class StickerPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemShareStickerPageBinding.bind(itemView)

        fun bind(uri: Uri, design: StickerDesign) {
            binding.ivStickerPage.load(uri)
            binding.ivStickerPage.contentDescription = itemView.context.getString(design.labelRes)
            binding.canvasStickerPage.background = background.previewDrawable(
                magnitudeColor,
                GradientDrawable.RECTANGLE,
                itemView.resources.getDimension(R.dimen.share_story_canvas_corner)
            )
        }
    }
}
