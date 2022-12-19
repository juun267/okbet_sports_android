package org.cxct.sportlottery.ui.sport.filter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.setLeagueLogo

class LeagueSelectAdapter
/**
 * 构造方法里， super()必须设置 header layout
 * data可有可无
 */
    (data: MutableList<LeagueSection>?) :
    BaseSectionQuickAdapter<LeagueSection, BaseViewHolder>(R.layout.item_league_select_head,
        R.layout.item_league_select,
        data) {
    override fun convertHeader(helper: BaseViewHolder, item: LeagueSection) {
        helper.setText(R.id.tv_name, item.header)
    }

    override fun convert(helper: BaseViewHolder, item: LeagueSection) {
        item.t.let {
            helper.getView<ImageView>(R.id.iv_select).isSelected = it.isSelected
            (helper.getView(R.id.iv_logo) as ImageView).setLeagueLogo(item.t.icon)
            helper.setText(R.id.tv_name, it.name)
            helper.setText(R.id.tv_num, it.num.toString())
        }

    }

}