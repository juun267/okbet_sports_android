package org.cxct.sportlottery.ui.maintab.games.adapter

import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHotGameViewBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class RecyclerHotGameAdapter: BindingAdapter<Recommend, ItemHotGameViewBinding>() {

    init {
        addChildClickViewIds(R.id.llayout_win)
        addChildClickViewIds(R.id.llayout_draw)
        addChildClickViewIds(R.id.llayout_lose)
    }
    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onBinding(position: Int, binding: ItemHotGameViewBinding, item: Recommend) {
        binding.tvMatchTitle.text="${item.leagueName}"
        binding.tvHomeName.text="${item.homeName}"
        binding.tvAwayName.text="${item.awayName}"
        binding.tvHomeScore.text="${item.matchInfo?.homeScore}"
        binding.tvAwayScore.text="${item.matchInfo?.awayScore}"
    }

}