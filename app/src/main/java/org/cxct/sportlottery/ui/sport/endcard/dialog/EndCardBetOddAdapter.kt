package org.cxct.sportlottery.ui.sport.endcard.dialog

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardBetOddBinding
import org.cxct.sportlottery.view.setColors

class EndCardBetOddAdapter:BindingAdapter<String, ItemEndcardBetOddBinding>() {

    var deleteOddsId: String?=null

    override fun onBinding(position: Int, binding: ItemEndcardBetOddBinding, item: String)=binding.run {
        if (deleteOddsId == item){
            tvOddName.text = context.getString(R.string.delete_bet)
            tvOddName.setColors(R.color.color_FFD600)
            tvOddName.textSize = 14f
            tvOddName.setBackgroundResource(R.drawable.bg_yellow_radius_6)
        }else{
            tvOddName.text = item
            tvOddName.setColors(R.color.color_FFFFFF)
            tvOddName.textSize = 16f
            tvOddName.setBackgroundResource(R.drawable.bg_darkblue_radius_6)
        }
    }
    fun setDeleteId(oddsId: String){
        deleteOddsId = oddsId
        notifyDataSetChanged()
    }
}