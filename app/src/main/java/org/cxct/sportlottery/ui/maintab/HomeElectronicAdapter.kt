package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R

class HomeElectronicAdapter(data: MutableList<HomeTabItem1>):
    BaseQuickAdapter<HomeTabItem1, BaseViewHolder>(R.layout.item_electronics_game) {

    override fun convert(helper: BaseViewHolder, item: HomeTabItem1) {
            helper.setImageResource(R.id.iv_electronics,item.icon)

    }

}
data class HomeTabItem1(val icon: Int, val name: Int)