package org.cxct.sportlottery.ui.maintab.menu

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R

class BetWayAdapter(data: List<String?>?) :
    BaseQuickAdapter<String?, BaseViewHolder>(R.layout.item_odds_type, data?.toMutableList()) {
    private var selectPos = -1
    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.getView<CheckBox>(R.id.rbtn_odds).isChecked = helper.layoutPosition == selectPos
        helper.setText(R.id.rbtn_odds, item)
    }

    fun setSelectPos(selectPos: Int) {
        this.selectPos = selectPos
        notifyDataSetChanged()
    }
}