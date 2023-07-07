package org.cxct.sportlottery.ui.betList.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import timber.log.Timber

class BetBasketballListAdapter(val onItemClickListener: OnItemClickListener) :
    BaseQuickAdapter<BetInfoListData, BaseViewHolder>(R.layout.item_bet_basketball_ending_cart) {

    override fun convert(holder: BaseViewHolder, item: BetInfoListData) {

        val tvMatchOdds = holder.getView<TextView>(R.id.tvMatchOdds)
        tvMatchOdds.background = DrawableCreatorUtils.getBasketballBetListButton()
        holder.setText(R.id.tvMatchOdds, item.matchOdd.playName)
        val tvHide = holder.getView<TextView>(R.id.tvHide)
        tvHide.background = DrawableCreatorUtils.getBasketballDeleteButton()

        if (item.isClickForBasketball == true) {
            tvHide.visible()
        } else {
            tvHide.gone()
        }

        //设置+More
        if (holder.layoutPosition == data.size - 1) {
            holder.setGone(R.id.tvMatchOdds, true).setVisible(R.id.tvBsMore, true)
                .setText(R.id.tvBsMore, R.string.N920)
            val tvBsMore = holder.getView<TextView>(R.id.tvBsMore)
            tvBsMore.background = DrawableCreatorUtils.getBasketballPlusMore()
            tvBsMore.setOnClickListener {
                onItemClickListener.addMore()
            }
        } else {
            holder.setVisible(R.id.tvMatchOdds, true).setGone(R.id.tvBsMore, true)
        }

        //点击赔率
        tvMatchOdds.setOnClickListener {
            //刷新上一次点击的区域
            data.forEachIndexed { index, betInfoListData ->
                if (betInfoListData.isClickForBasketball == true) {
                    betInfoListData.isClickForBasketball = false
                }
                notifyItemChanged(index)
            }
            val currentPosition = holder.layoutPosition
            //记录本次点击的区域
            if (data.size > currentPosition) {
                data[currentPosition].isClickForBasketball = true
                notifyItemChanged(currentPosition)
                Timber.d("currentSelectPo:${currentPosition}")
            }
        }

        //蒙版点击事件
        tvHide.setOnClickListener {
            data[holder.layoutPosition].isClickForBasketball = false
            onItemClickListener.onDeleteClick(
                data[holder.layoutPosition].matchOdd.oddsId, itemCount
            )
        }
    }
}