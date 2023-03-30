package org.cxct.sportlottery.ui.maintab.home

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.SportMenu

class MainHomeMenuAdapter(data: MutableList<SportMenu>) :
    BaseQuickAdapter<SportMenu, BaseViewHolder>(
        R.layout.item_main_home_menu, data) {
    override fun convert(helper: BaseViewHolder, item: SportMenu) {
        helper.setImageResource(R.id.iv_people, GameType.getGameTypeBannerBg(item.gameType))
        helper.setText(R.id.tv_name, item.sportName)
        helper.setText(R.id.tv_amount, item.gameCount.toString())
    }
}