package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemHomeBettingstationBinding
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load

class HomeBettingStationAdapter : BindingAdapter<BettingStation, ItemHomeBettingstationBinding>() {

    init {
        addChildClickViewIds(R.id.linView)
    }

    override fun onBinding(
        position: Int,
        vb: ItemHomeBettingstationBinding,
        item: BettingStation,
    ) = vb.run {
        tvName.text = item.name
        tvMobileTitle.text = "${context.getString(R.string.mobile)}:"
        tvMobile.text = item.telephone
        tvTimeTitle.text = "${context.getString(R.string.mobile)}:"
        tvTime.text = "(${item.officeStartTime}~${item.officeEndTime})"
        tvLocationTitle.text = "${context.getString(R.string.P458)}:"
        tvLocation.text = item.addr
    }
}
