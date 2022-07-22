package org.cxct.sportlottery.ui.game.publicity

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.PublicityPromotionItemBinding

class PublicityPromotionItemViewHolder(
    val binding: PublicityPromotionItemBinding,
    private val publicityAdapterListener: GamePublicityNewAdapter.PublicityAdapterNewListener
) :
    RecyclerView.ViewHolder(binding.root) {

    val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    fun bind(data: PublicityPromotionItemData) {
        binding.tvPromotionTitle.text = data.title
        binding.tvPromotionContent.text = data.content
        binding.tvMore.setOnClickListener {
            publicityAdapterListener.onClickPromotionListener()
        }

        Glide.with(binding.ivPromotionImage)
            .load(data.imageUrl)
            .apply(requestOptions)
            .into(binding.ivPromotionImage)
    }
}