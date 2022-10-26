package org.cxct.sportlottery.ui.maintab.menu

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.sport.Item

class SportInPlayAdapter(data: MutableList<Item>) :
    BaseQuickAdapter<Item, BaseViewHolder>(
        R.layout.item_sport_inplay, data) {

    override fun convert(helper: BaseViewHolder, item: Item) {
        helper.setText(R.id.tv_name, item.name)
        helper.setText(R.id.tv_num, item.num.toString())
    }

}