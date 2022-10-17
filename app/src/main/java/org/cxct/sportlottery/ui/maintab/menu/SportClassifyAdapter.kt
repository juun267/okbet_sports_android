package org.cxct.sportlottery.ui.maintab.menu

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.StatusSheetData

class SportClassifyAdapter(data: MutableList<StatusSheetData>) :
    BaseQuickAdapter<StatusSheetData, BaseViewHolder>(
        R.layout.item_sport_classify, data) {

    override fun convert(helper: BaseViewHolder, item: StatusSheetData) {
        helper.setText(R.id.tv_name, item.showName)
    }
}