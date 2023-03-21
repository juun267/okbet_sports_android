package org.cxct.sportlottery.ui.sport.outright

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.rotationAnimation
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.util.setLeagueLogo

// 冠军列表-联赛名称
class OutrightFirstProvider(val adapter: SportOutrightAdapter2,
                            val lifecycle: LifecycleOwner,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 1,
                            override val layoutId: Int = R.layout.item_outright_group): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val matchOdd = item as MatchOdd
        helper.setText(R.id.tv_league_name, matchOdd.matchInfo?.name)
        helper.getView<ImageView>(R.id.iv_league_logo).setLeagueLogo(matchOdd.matchInfo?.categoryIcon)
        val ivArrow = helper.getView<ImageView>(R.id.iv_league_arrow)
        setArrowSpin(ivArrow, matchOdd, false)
        ivArrow.setOnClickListener {
            val position = adapter.getItemPosition(item)
            adapter.expandOrCollapse(item)
            setArrowSpin(ivArrow, matchOdd, true)
            onItemClick.invoke(position, it, matchOdd)
        }
    }

    private fun setArrowSpin(ivArrow: ImageView, data: MatchOdd, isAnimate: Boolean) {

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