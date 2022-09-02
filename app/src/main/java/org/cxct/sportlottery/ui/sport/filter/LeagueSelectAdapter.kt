package org.cxct.sportlottery.ui.sport.filter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.setSvgDrawable

class LeagueSelectAdapter
/**
 * 构造方法里， super()必须设置 header layout
 * data可有可无
 */
    (data: List<LeagueSection?>?) :
    BaseSectionQuickAdapter<LeagueSection, BaseViewHolder>(R.layout.item_league_select,
        R.layout.item_league_select_head,
        data) {
    override fun convertHead(helper: BaseViewHolder, item: LeagueSection) {
        helper.setText(R.id.tv_name, item.header)
    }

    override fun convert(helper: BaseViewHolder, item: LeagueSection) {
        item.t.let {
            helper.getView<ImageView>(R.id.iv_select).isSelected = it.isSelected
            (helper.getView(R.id.iv_logo) as ImageView).setSvgDrawable(item.t.icon)
            helper.setText(R.id.tv_name, it.name)
            helper.setText(R.id.tv_num, it.num.toString())
        }

    }
}