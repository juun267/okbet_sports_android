package org.cxct.sportlottery.ui.maintab


import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import org.cxct.sportlottery.util.LocalUtils
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
    var playType: String = "1"
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
    //    LogUtil.d("playType"+playType+","+"name"+"${item.league.name}"+"类型"+"${item.sportName}")
        when (playType){
            "1"-> {
                if (item.sportName == LocalUtils.getString(R.string.soccer)){
                    helper.getView<TextView>(R.id.tv_title2).visibility = View.VISIBLE
                    helper.setText(R.id.tv_title1,LocalUtils.getString(R.string.text_1))
                    helper.setText(R.id.tv_title2,LocalUtils.getString(R.string.text_x))
                    helper.setText(R.id.tv_title3,LocalUtils.getString(R.string.text_2))
                }else{
                    helper.getView<TextView>(R.id.tv_title2).visibility = View.GONE
                    helper.setText(R.id.tv_title1,LocalUtils.getString(R.string.text_1))
                    helper.setText(R.id.tv_title3,LocalUtils.getString(R.string.text_2))
                }
            }
            "2"-> {
                helper.getView<TextView>(R.id.tv_title2).visibility = View.GONE
                helper.setText(R.id.tv_title1,LocalUtils.getString(R.string.odds_button_name_home))
                helper.setText(R.id.tv_title3,LocalUtils.getString(R.string.odds_button_name_away))
            }
            "3"-> {
                helper.getView<TextView>(R.id.tv_title2).visibility = View.GONE
                helper.setText(R.id.tv_title1,LocalUtils.getString(R.string.less_than_the))
                helper.setText(R.id.tv_title3,LocalUtils.getString(R.string.more_than_the))
            }
        }

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
                (adapter as ItemHandicapAdapter).oddsType = oddsType
                (adapter as ItemHandicapAdapter).data = item.matchInfos
            }
        }

    }

}
