package org.cxct.sportlottery.ui.maintab.menu

import android.widget.CheckBox
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.HandicapType

class OddsTypeAdapter(data: List<String?>?) :
    BaseQuickAdapter<String?, BaseViewHolder>(R.layout.item_odds_type, data?.toMutableList()) {
    private var selectPos = -1
    override fun convert(helper: BaseViewHolder, item: String?) {
        helper.getView<CheckBox>(R.id.rbtn_odds).isChecked = helper.layoutPosition == selectPos
        helper.setText(R.id.rbtn_odds, item)

        helper.getView<TextView>(R.id.rbtn_odds).apply {
            when (item) {
                HandicapType.EU.name -> {
                    text = context.getString(R.string.odd_type_eu)
                }
                HandicapType.HK.name -> {
                    text = context.getString(R.string.odd_type_hk)
                }
                HandicapType.MY.name -> {
                    text = context.getString(R.string.odd_type_mys)
                }
                HandicapType.ID.name -> {
                    text = context.getString(R.string.odd_type_idn)
                }
            }
        }
    }

    fun setSelectPos(selectPos: Int) {
        this.selectPos = selectPos
        notifyDataSetChanged()
    }
}