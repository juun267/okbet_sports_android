package org.cxct.sportlottery.view.dialog.promotion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youth.banner.adapter.BannerAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemPromotionBinding

class PromotionAdapter(private val promotionList: List<PromotionData>) :
    BannerAdapter<PromotionData, RecyclerView.ViewHolder>(promotionList) {


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
            binding.ivImage.load(itemData.imgUrl, R.drawable.ic_image_load, R.drawable.ic_image_broken)
        }
    }
}
