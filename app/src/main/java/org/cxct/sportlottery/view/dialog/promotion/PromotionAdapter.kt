package org.cxct.sportlottery.view.dialog.promotion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemPromotionBinding

class PromotionAdapter(private val promotionList: List<PromotionData>) :
    BannerAdapter<PromotionData, RecyclerView.ViewHolder>(promotionList) {
    val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return PromotionViewHolder(
            ItemPromotionBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
        )
    }

    override fun onBindView(holder: RecyclerView.ViewHolder?, data: PromotionData?, position: Int, size: Int) {
        val itemData = promotionList[position]
        when (holder) {
            is PromotionViewHolder -> {
                holder.bind(itemData)
            }
        }
    }

    inner class PromotionViewHolder(val binding: ItemPromotionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: PromotionData) {
            binding.tvTitle.text = itemData.title
            Glide.with(binding.ivImage)
                .load(itemData.imgUrl)
                .apply(requestOptions)
                .into(binding.ivImage)
        }
    }
}
