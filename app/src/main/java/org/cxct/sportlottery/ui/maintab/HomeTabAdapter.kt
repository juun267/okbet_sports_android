package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R

class HomeTabAdapter(data: List<HomeTabItem>?, val selectPos: Int) :
    BaseQuickAdapter<HomeTabItem, BaseViewHolder>(R.layout.item_tab_home, data) {
    companion object {
        fun getItems(): List<HomeTabItem> {
            return mutableListOf<HomeTabItem>(
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
                HomeTabItem(R.drawable.ic_home_football, R.string.home_tan_main),
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
