package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.sport.SportMenu

class MainHomeMenuAdapter(data: MutableList<SportMenu>) :
    BaseQuickAdapter<SportMenu, BaseViewHolder>(
        R.layout.item_main_home_menu, data
    ) {
    override fun convert(helper: BaseViewHolder, item: SportMenu) {
        helper.setText(R.id.tv_name, item.sportName)
        helper.setText(R.id.tv_amount, item.gameCount.toString())
    }
}