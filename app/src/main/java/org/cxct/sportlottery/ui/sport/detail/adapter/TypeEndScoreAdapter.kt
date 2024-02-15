package org.cxct.sportlottery.ui.sport.detail.adapter


import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.databinding.ItemEndscoreButtonBinding
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener


class TypeEndScoreAdapter (
    var oddsDetail: OddsDetailListData,
    var onOddClickListener: OnOddClickListener,
    var oddsType: OddsType
) : BindingAdapter<Odd?, ItemEndscoreButtonBinding>() {

    init {
        setList(oddsDetail.oddArrayList)
    }

    fun setOddsDetailData(oddsDetail: OddsDetailListData){
        this.oddsDetail = oddsDetail
        setList(oddsDetail.oddArrayList)
    }
    override fun onBinding(position: Int, binding: ItemEndscoreButtonBinding, item: Odd?) {
        binding.endingBtn.apply {
            setupOdd(item, oddsType)
            oddStatus = item?.oddState
            clickDelay {
                item?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
            }
        }
    }

}