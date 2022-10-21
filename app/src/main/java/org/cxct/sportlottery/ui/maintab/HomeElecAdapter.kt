package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R

class HomeElecAdapter(data: List<HomeElecItem>) :
    BaseQuickAdapter<HomeElecItem, BaseViewHolder>(R.layout.item_home_elec, data) {
    companion object {
        fun getItems(): List<HomeElecItem> {
            return mutableListOf<HomeElecItem>(
                HomeElecItem(R.drawable.selector_home_tab_recommend, R.string.home_recommend),
                HomeElecItem(R.drawable.selector_home_tab_live, R.string.home_live),
                HomeElecItem(R.drawable.selector_home_tab_sport, R.string.home_sports),
                HomeElecItem(R.drawable.selector_home_tab_worldcup, R.string.home_word_cup),
                HomeElecItem(R.drawable.selector_home_tab_slot, R.string.home_slot),
                HomeElecItem(R.drawable.selector_home_tab_okgame, R.string.home_on_game),
            ).toList()
        }
    }

    override fun convert(helper: BaseViewHolder, item: HomeElecItem) {

    }
}

data class HomeElecItem(val icon: Int, val name: Int)
