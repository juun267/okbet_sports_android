package org.cxct.sportlottery.ui.finance

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ViewItemRedEnvelopeLogBinding
import org.cxct.sportlottery.network.money.list.RedEnvelopeRow
import org.cxct.sportlottery.repository.sConfigData

class RedEnvelopeLogAdapter : BindingAdapter<RedEnvelopeRow,ViewItemRedEnvelopeLogBinding>() {

    override fun onBinding(
        position: Int,
        binding: ViewItemRedEnvelopeLogBinding,
        item: RedEnvelopeRow,
    ) {
        binding.apply {
            rechLogDate.text = item.rechDateStr
            rechLogTime.text = item.rechTimeStr
            rechLogOrderNo.text = item.orderNo
            rechLogAmount.text = "${sConfigData?.systemCurrencySign} ${item.money}"
            rechLogType.text = item.tranTypeDisplay
        }
    }
}