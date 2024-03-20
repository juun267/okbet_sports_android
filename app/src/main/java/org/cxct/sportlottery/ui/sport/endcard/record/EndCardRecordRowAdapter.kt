package org.cxct.sportlottery.ui.sport.endcard.record

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.ItemEndcardDetailResultRowContentBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.setColors

class EndCardRecordRowAdapter:BindingAdapter<String,ItemEndcardDetailResultRowContentBinding>() {

    override fun onBinding(position: Int, binding: ItemEndcardDetailResultRowContentBinding, item: String) {
        binding.tvWinAmount.apply {
            if (item.toIntS(0)>0){
                setColors(R.color.color_00FF81)
                text = "$showCurrencySign +${TextUtil.formatMoney(item,2)}"
            }else{
                setColors(R.color.color_FFFFFF)
                text = "-"
            }
        }
    }
}