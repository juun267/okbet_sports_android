package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData

class HomeTabAdapter(data: List<HomeTabItem>?, val selectPos: Int) :
    BaseQuickAdapter<HomeTabItem, BaseViewHolder>(R.layout.item_tab_home, data) {
    companion object {
        fun getItems(): List<HomeTabItem> {
            return mutableListOf<HomeTabItem>(
                HomeTabItem(R.drawable.selector_home_tab_recommend, R.string.home_recommend),
                HomeTabItem(R.drawable.selector_home_tab_live, R.string.home_live),
                (if (sConfigData?.worldCupOpen == 1)
                    HomeTabItem(R.drawable.selector_home_tab_worldcup, R.string.home_word_cup)
                else
                    HomeTabItem(R.drawable.selector_home_tab_inplay, R.string.home_in_play)),
                HomeTabItem(R.drawable.selector_home_tab_sport, R.string.home_sports),
                HomeTabItem(R.drawable.selector_home_tab_slot, R.string.home_slot),
                HomeTabItem(R.drawable.selector_home_tab_okgame, R.string.home_on_game),
            ).toList()
        }
    }

    override fun convert(helper: BaseViewHolder, item: HomeTabItem) {
        helper.setImageResource(R.id.iv_logo, item.icon)
        helper.setText(R.id.tv_name, item.name)
        helper.itemView.isSelected = selectPos == helper.layoutPosition
    }

}

data class HomeTabItem(val icon: Int, val name: Int)
