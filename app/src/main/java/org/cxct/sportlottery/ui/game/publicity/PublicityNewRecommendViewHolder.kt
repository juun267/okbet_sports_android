package org.cxct.sportlottery.ui.game.publicity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.PublicityRecommendViewBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.menu.OddsType

class PublicityNewRecommendViewHolder(
    val binding: PublicityRecommendViewBinding,
    private val publicityAdapterListener: GamePublicityAdapter.PublicityAdapterListener
) : RecyclerView.ViewHolder(binding.root) {
    private val mPublicityRecommendItemAdapter by lazy {
        PublicityNewRecommendAdapter(publicityAdapterListener)
    }

    fun bind(recommendList: List<Recommend>, oddsType: OddsType) {
        with(binding.rootRv) {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = mPublicityRecommendItemAdapter
        }
        mPublicityRecommendItemAdapter.setupRecommendItem(recommendList, oddsType)
    }
}