package org.cxct.sportlottery.ui.maintab.home

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.isCreditSystem

class HomeTabAdapter(data: List<HomeTabItem>?, val selectPos: Int, homeFragment: HomeFragment) :
    BaseQuickAdapter<HomeTabItem, BaseViewHolder>(R.layout.item_tab_home, data?.toMutableList()) {

    companion object {
        fun getItems(): List<HomeTabItem> {

            if (isCreditSystem()) {  //OK9
                return mutableListOf<HomeTabItem>(
                    HomeTabItem(R.drawable.selector_home_tab_recommend, R.string.home_recommend),
                    HomeTabItem(R.drawable.selector_home_tab_live, R.string.home_live),
                    HomeTabItem(R.drawable.selector_home_tab_inplay, R.string.home_in_play),
                    HomeTabItem(R.drawable.selector_home_tab_sport, R.string.home_sports),
                    HomeTabItem(R.drawable.selector_home_tab_reallive, R.string.live),
                    HomeTabItem(R.drawable.selector_home_tab_okgame, R.string.home_on_game),
                    HomeTabItem(R.drawable.selector_home_tab_lottery, R.string.lottery),
                ).toList()
            }

            return mutableListOf<HomeTabItem>(
                HomeTabItem(R.drawable.selector_home_tab_recommend, R.string.home_recommend),
                HomeTabItem(R.drawable.selector_home_tab_live, R.string.home_live),
                HomeTabItem(R.drawable.selector_home_tab_inplay, R.string.home_in_play),
                HomeTabItem(R.drawable.selector_home_tab_sport, R.string.home_sports),
                HomeTabItem(R.drawable.selector_home_tab_slot, R.string.home_slot),
                HomeTabItem(R.drawable.selector_home_tab_okgame, R.string.home_on_game),
            ).toList()
        }
    }

    init {
        setOnItemClickListener { _, _, position ->
            homeFragment.onTabClickByPosition(position)
        }
    }

    override fun convert(helper: BaseViewHolder, item: HomeTabItem) {
        helper.setImageResource(R.id.iv_logo, item.icon)
        helper.setText(R.id.tv_name, item.name)
        helper.itemView.isSelected = selectPos == helper.layoutPosition
    }

}

data class HomeTabItem(val icon: Int, val name: Int)
