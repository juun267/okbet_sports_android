package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.rotationAnimation
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.setLeagueLogo

class EndScoreFirstProvider(val adapter: EndScoreAdapter,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 1,
                            override val layoutId: Int = R.layout.item_endscore_group): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val league = item as LeagueOdd
        helper.setText(R.id.tv_league_name, league.league?.name)
        val ivArrow = helper.getView<ImageView>(R.id.iv_league_arrow)
        helper.getView<ImageView>(R.id.iv_league_logo).setLeagueLogo(league.league?.categoryIcon)
        setArrowSpin(ivArrow, league, false)
        ivArrow.setOnClickListener {
            val position = adapter.getItemPosition(item)
            adapter.expandOrCollapse(item)
            setArrowSpin(ivArrow, league, true)
            onItemClick.invoke(position, it, league)
        }
    }

    private fun setArrowSpin(ivArrow: ImageView, data: LeagueOdd, isAnimate: Boolean) {

        var rotation = 180f
        if (data.isExpanded) {
            rotation = 0f
        }

        if (isAnimate) {
            ivArrow.rotationAnimation(rotation)
        } else {
            ivArrow.rotation = rotation
        }
    }

}