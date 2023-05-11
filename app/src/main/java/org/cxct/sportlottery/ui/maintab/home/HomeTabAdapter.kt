package org.cxct.sportlottery.ui.maintab.home

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.SPUtil

class HomeTabAdapter(data: List<HomeTabItem>, val selectPos: Int, homeFragment: HomeFragment) :
    BaseQuickAdapter<HomeTabItem, BaseViewHolder>(R.layout.item_tab_home, data?.toMutableList()) {

    companion object {
        fun getItems(): List<HomeTabItem> {
            return mutableListOf<HomeTabItem>().apply {
                add(HomeTabItem(R.drawable.selector_home_tab_recommend, R.string.home_recommend))
                add(HomeTabItem(R.drawable.selector_home_tab_live, R.string.home_live))
                add(HomeTabItem(R.drawable.selector_home_tab_inplay, R.string.home_in_play))
                add(HomeTabItem(R.drawable.selector_home_tab_sport, R.string.home_sports))
                if (!SPUtil.getMarketSwitch()) {
                    add(HomeTabItem(R.drawable.selector_home_tab_okgame, R.string.home_on_game))
                }
            }.toList()
        }
    }

    init {
        setOnItemClickListener { _, _, position ->
            when(data[position].name) {
                R.string.home_recommend -> homeFragment.backMainHome()
                R.string.home_live -> homeFragment.jumpToLive()
                R.string.home_in_play -> homeFragment.jumpToInplaySport()
                R.string.home_sports -> homeFragment.jumpToEarlySport()
                R.string.home_on_game -> homeFragment.jumpToOKGames()
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: HomeTabItem) {
        helper.setImageResource(R.id.iv_logo, item.icon)
        helper.setText(R.id.tv_name, item.name)
        helper.itemView.isSelected = selectPos == helper.layoutPosition
    }

}

data class HomeTabItem(val icon: Int, val name: Int)
