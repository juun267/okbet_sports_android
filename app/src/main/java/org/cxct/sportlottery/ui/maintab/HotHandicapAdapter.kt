package org.cxct.sportlottery.ui.maintab


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.material.internal.ContextUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData


class HotHandicapAdapter(data:List<HandicapData>):
    BaseQuickAdapter<HandicapData,BaseViewHolder>(R.layout.item_hot_handicap,data) {
    override fun convert(helper: BaseViewHolder, item: HandicapData) {

    
    val recycleView:RecyclerView = helper.getView(R.id.rv_handicap_item)
       val itemAdapter: ItemHandicapAdapter = ItemHandicapAdapter(
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
        recycleView.adapter = itemAdapter
        recycleView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)

    }

}
