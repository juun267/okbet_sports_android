package org.cxct.sportlottery.ui.maintab.home

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.databinding.ItemHomeBettingstationBinding
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil

class HomeBettingStationAdapter : BindingAdapter<BettingStation, ItemHomeBettingstationBinding>() {

    init {
        addChildClickViewIds(R.id.ivLocation)
    }
    private val itemWidth by lazy { (ScreenUtil.getScreenWidth(context)-12.dp*2-8.dp)/2 }

    override fun onBinding(
        position: Int,
        vb: ItemHomeBettingstationBinding,
        item: BettingStation,
    ) = vb.run {
        root.layoutParams.apply {
            width = itemWidth
        }
        ivCover.load(sConfigData?.resServerHost + item.stationImage,R.drawable.img_banner01)
        tvName.text = item.name
        tvAddress.text = item.addr
        tvMobile.text = item.telephone

    }
}
