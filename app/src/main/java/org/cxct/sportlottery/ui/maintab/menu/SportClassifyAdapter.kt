package org.cxct.sportlottery.ui.maintab.menu

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData

class SportClassifyAdapter(data: MutableList<StatusSheetData>) :
    BaseQuickAdapter<StatusSheetData, BaseViewHolder>(
        R.layout.item_sport_classify, data) {
    var gameType: GameType? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun convert(helper: BaseViewHolder, item: StatusSheetData) {
        helper.setText(R.id.tv_name, item.showName)
        helper.itemView.isSelected = (gameType?.key == item.code)
    }
}