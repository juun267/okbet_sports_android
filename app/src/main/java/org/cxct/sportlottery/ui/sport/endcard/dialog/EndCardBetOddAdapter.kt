package org.cxct.sportlottery.ui.sport.endcard.dialog

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardBetOddBinding

class EndCardBetOddAdapter:BindingAdapter<String, ItemEndcardBetOddBinding>() {

    var deleteOddsId: String?=null

    override fun onBinding(position: Int, binding: ItemEndcardBetOddBinding, item: String) {
        if (deleteOddsId == item){
            binding.tvOddName.text = context.getString(R.string.delete_bet)
            binding.tvOddName.setBackgroundResource(R.drawable.bg_yellow_radius_6)
        }else{
            binding.tvOddName.text = item
            binding.tvOddName.setBackgroundResource(R.drawable.bg_darkblue_radius_6)
        }
    }
    fun setDeleteId(oddsId: String){
        deleteOddsId = oddsId
        notifyDataSetChanged()
    }
}