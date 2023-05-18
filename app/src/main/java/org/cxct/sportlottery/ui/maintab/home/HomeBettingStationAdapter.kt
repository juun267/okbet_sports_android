package org.cxct.sportlottery.ui.maintab.home

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.databinding.ItemHomeBettingstationBinding
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp

class HomeBettingStationAdapter : BindingAdapter<BettingStation, ItemHomeBettingstationBinding>() {

    init {
        addChildClickViewIds(R.id.linDirection)
    }

    override fun onBinding(
        position: Int,
        vb: ItemHomeBettingstationBinding,
        item: BettingStation,
    ) = vb.run {
        ivCover.roundOf(sConfigData?.resServerHost + item.stationImage, 12.dp, R.drawable.img_banner01)
        tvName.text = item.name
        tvAddress.text = item.addr
        tvMobile.text = item.telephone

    }
}
