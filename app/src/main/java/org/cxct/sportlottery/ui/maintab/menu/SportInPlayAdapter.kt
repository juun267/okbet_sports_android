package org.cxct.sportlottery.ui.maintab.menu

import androidx.appcompat.content.res.AppCompatResources
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.Item

class SportInPlayAdapter(data: MutableList<Item>) :
    BaseQuickAdapter<Item, BaseViewHolder>(
        R.layout.item_sport_inplay, data
    ) {
    var gameType: GameType? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun convert(helper: BaseViewHolder, item: Item) {
//        helper.setImageResource(R.id.iv_sport_logo, GameType.getInplayIcon(item.code))
//        helper.setImageDrawable()
        helper.setImageDrawable(
            R.id.iv_sport_logo,
            AppCompatResources.getDrawable(context, GameType.getInplayIcon(item.code))
        )
        helper.setText(R.id.tv_name, item.name)
        helper.setText(R.id.tv_num, item.num.toString())
        helper.itemView.isSelected = (gameType?.key == item.code)
    }

}