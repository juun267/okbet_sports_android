package org.cxct.sportlottery.ui.maintab


import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.setLeagueLogo


class HotHandicapAdapter(data:List<HandicapData>):
    BaseQuickAdapter<HandicapData,BaseViewHolder>(R.layout.item_hot_handicap,data) {

    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var homeRecommendListener: HomeRecommendListener? = null
    var clickOdd: Odd? = null
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

    override fun convert(helper: BaseViewHolder, item: HandicapData) {
        helper.setText(R.id.tv_league_name, item.league.name)
        helper.getView<ImageView>(R.id.iv_league_logo).setLeagueLogo(item.league.categoryIcon)

        var recycleView = helper.getView<RecyclerView>(R.id.rv_handicap_item)
        recycleView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                homeRecommendListener?.let {
                    var itemAdapter = ItemHandicapAdapter(it)
                    itemAdapter.oddsType = oddsType
                    itemAdapter.data = item.matchInfos
                    adapter = itemAdapter
                }

            } else {
                (adapter as ItemHandicapAdapter).data = item.matchInfos
            }
        }

    }

}
