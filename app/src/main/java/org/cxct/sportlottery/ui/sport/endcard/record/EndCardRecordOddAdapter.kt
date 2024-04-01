package org.cxct.sportlottery.ui.sport.endcard.record

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemEndcardRecordOddBinding

class EndCardRecordOddAdapter:BindingAdapter<String,ItemEndcardRecordOddBinding>() {

    override fun onBinding(position: Int, binding: ItemEndcardRecordOddBinding, item: String) {
         binding.tvOddName.text = item
    }
}