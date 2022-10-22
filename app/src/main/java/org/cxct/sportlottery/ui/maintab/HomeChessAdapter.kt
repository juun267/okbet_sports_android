package org.cxct.sportlottery.ui.maintab

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R

class HomeChessAdapter(data: MutableList<HomeChessItem>):
    BaseQuickAdapter<HomeChessItem, BaseViewHolder>(R.layout.item_poker_game) {

    override fun convert(helper: BaseViewHolder, item: HomeChessItem) {
            helper.setImageResource(R.id.iv_poker_bg,item.icon)
            helper.setText(R.id.tv_poker_name,item.name)
            helper.setText(R.id.tv_sub_poker_name,item.subName)
    }

}
data class HomeChessItem(val icon: Int, val name: String,val subName: String)