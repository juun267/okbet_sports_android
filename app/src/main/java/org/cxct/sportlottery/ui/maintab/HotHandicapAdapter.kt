package org.cxct.sportlottery.ui.maintab


import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
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

    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            var needUpdate = false
            data.forEach {handicapData ->
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
         helper.setText(R.id.tv_league_name,item.league.name)
         helper.getView<ImageView>(R.id.iv_league_logo).setLeagueLogo(item.league.categoryIcon)


    val recycleView:RecyclerView = helper.getView(R.id.rv_handicap_item)
       val itemAdapter = ItemHandicapAdapter(
           HomeRecommendListener(
               onItemClickListener = {

               },
               onGoHomePageListener = {

               },
               onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->

               },
               onClickFavoriteListener = {

               },
               onClickStatisticsListener = { matchId ->

               }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->

               },onClickLiveIconListener = {gameType, matchType, matchId, matchInfoList ->

               }
       ){ gameType, matchType, matchId, matchInfoList ->

           })

         data.forEach {  handicapData ->
            handicapData.matchInfos
         }
        recycleView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
            }
            if (adapter == null) {
                adapter = itemAdapter
            }
        }
        itemAdapter.data = item.matchInfos
        itemAdapter.oddsType = oddsType
    }

}
