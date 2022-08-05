package org.cxct.sportlottery.ui.game.publicity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ViewPublicityPromotionBinding

class PublicityPromotionViewHolder(
    val binding: ViewPublicityPromotionBinding,
    private val publicityAdapterListener: GamePublicityNewAdapter.PublicityAdapterNewListener
) : RecyclerView.ViewHolder(binding.root) {
    private val mPublicityPromotionItemAdapter by lazy { PublicityPromotionItemAdapter(publicityAdapterListener) }
    fun bind(promotionDataList: List<PublicityPromotionItemData>) {
        binding.tvMore.setOnClickListener { publicityAdapterListener.onClickPromotionListener() }

        with(binding.rvPromotion) {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = mPublicityPromotionItemAdapter
            }
        }

        mPublicityPromotionItemAdapter.setData(promotionDataList)
    }

}