package org.cxct.sportlottery.ui.sport.endcard.record

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.databinding.ItemEndcardDetailResultRowContentBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.setColors

class EndCardRecordRowAdapter:BindingAdapter<Item,ItemEndcardDetailResultRowContentBinding>() {

    override fun onBinding(position: Int, binding: ItemEndcardDetailResultRowContentBinding, item: Item) {
        binding.tvQuarter.text = item.name
        binding.tvResult.text = if(item.score.isNullOrEmpty()) "-" else item.score
        binding.tvWinAmount.apply {
            if (item.winnable?:0>0){
                setColors(R.color.color_00FF81)
                text = "$showCurrencySign +${TextUtil.formatMoney(item.winnable?:0,2)}"
            }else{
                setColors(R.color.color_FFFFFF)
                text = "-"
            }
        }
    }

}
@KeepMembers
data class Item(
    val name:String?,
    val score:String?,
    val winnable:Int?
)
