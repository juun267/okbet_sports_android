package org.cxct.sportlottery.ui.betList.adapter

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBetBasketballEndingCartBinding
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import timber.log.Timber

class BetBasketballListAdapter(val onItemClickListener: OnItemClickListener) :
    BindingAdapter<BetInfoListData, ItemBetBasketballEndingCartBinding>() {
    override fun onBinding(
        position: Int,
        binding: ItemBetBasketballEndingCartBinding,
        item: BetInfoListData,
    ) =binding.run{
        tvMatchOdds.background = DrawableCreatorUtils.getBasketballBetListButton()
        tvMatchOdds.text = item.matchOdd.playName
        tvHide.background = DrawableCreatorUtils.getBasketballDeleteButton()
        tvHide.isVisible = item.isClickForBasketball == true
        //设置+More
        if (position == data.size - 1) {
            tvMatchOdds.isVisible = false
            tvBsMore.isVisible = true
            tvBsMore.text = context.getString(R.string.N920)
            tvBsMore.background = DrawableCreatorUtils.getBasketballPlusMore()
            tvBsMore.setOnClickListener {
                onItemClickListener.addMore()
            }
        } else {
            tvMatchOdds.isVisible = true
            tvBsMore.isVisible = false
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
            //记录本次点击的区域
            if (data.size > position) {
                data[position].isClickForBasketball = true
                notifyItemChanged(position)
                Timber.d("currentSelectPo:${position}")
            }
        }

        //蒙版点击事件
        tvHide.setOnClickListener {
            data[position].isClickForBasketball = false
            onItemClickListener.onDeleteClick(
                data[position].matchOdd.oddsId, itemCount
            )
        }
    }

}