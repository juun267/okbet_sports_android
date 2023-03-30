package org.cxct.sportlottery.ui.maintab.home


import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHotHandicapBinding
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.recyclerview.adapter.BindingAdapter
import org.cxct.sportlottery.ui.recyclerview.adapter.BindingVH
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.setLeagueLogo

class HotHandicapAdapter(val lifecycleOwner: LifecycleOwner, private val homeRecommendListener: HomeRecommendListener):
    BindingAdapter<HandicapData, ItemHotHandicapBinding>() {

    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var playType: String = "1"
    set(value) {
        if (value != field) {
            field = value
            notifyDataSetChanged()
        }
    }

    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            var needUpdate = false
            data.forEach { handicapData ->
                handicapData.matchInfos.forEach {

                    it.oddsMap?.values?.forEach { oddList ->
                        oddList?.forEach { odd ->
                            val newSelectStatus = field.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                            if (odd?.isSelected != newSelectStatus) {
                                odd?.isSelected = newSelectStatus
                                needUpdate = true
                            }
                        }
                    }
                }
            }

            if (needUpdate) {
                notifyDataSetChanged()
            }
        }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemHotHandicapBinding> {
        val vh = super.onCreateDefViewHolder(parent, viewType)
//        val vh = BindingVH(ItemHotHandicapBinding.inflate(LayoutInflater.from(context), parent, false))
        vh.vb.rvHandicapItem.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.VERTICAL, false)
        vh.vb.rvHandicapItem.adapter = ItemHandicapAdapter(lifecycleOwner, homeRecommendListener)
        vh.vb.tvTitle2.text = LocalUtils.getString(R.string.text_x)
        return vh
    }

    override fun onBinding(position: Int, binding: ItemHotHandicapBinding, item: HandicapData) = binding.run {
        tvLeagueName.text = item.league.name
        ivLeagueLogo.setLeagueLogo(item.league.categoryIcon)
        (rvHandicapItem.adapter as ItemHandicapAdapter).run {
            oddsType = oddsType
            data = item.matchInfos
        }

        when (playType){
            "1"-> {
                if (item.sportName == LocalUtils.getString(R.string.soccer)){
                    setTitles(R.string.text_1, R.string.text_2, true, binding)
                } else {
                    setTitles(R.string.odds_button_name_home, R.string.odds_button_name_away, false, binding)
                }
            }
            "2"-> {
                setTitles(R.string.odds_button_name_home, R.string.odds_button_name_away, false, binding)
            }
            "3"-> {
                setTitles(R.string.more_than_the, R.string.less_than_the, false, binding)
            }
        }
    }

    private fun setTitles(t1: Int, t3: Int, t2Visiable: Boolean, binding: ItemHotHandicapBinding) = binding.run {
        tvTitle2.isVisible = t2Visiable
        tvTitle1.text = LocalUtils.getString(t1)
        tvTitle3.text = LocalUtils.getString(t3)
    }

}
