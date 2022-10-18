package org.cxct.sportlottery.ui.maintab.menu

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R

class OddsPriceAdapter(data: List<String?>?) :
    BaseQuickAdapter<String?, BaseViewHolder>(R.layout.item_odds_type, data) {
    private var selectPos = -1
    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.setChecked(R.id.rbtn_odds, helper.layoutPosition == selectPos)
        helper.setText(R.id.rbtn_odds, item)
    }

    fun setSelectPos(selectPos: Int) {
        this.selectPos = selectPos
        notifyDataSetChanged()
    }
}