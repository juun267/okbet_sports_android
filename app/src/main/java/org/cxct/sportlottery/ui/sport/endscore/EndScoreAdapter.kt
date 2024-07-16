package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus

// 篮球末位比分
class EndScoreAdapter(val onItemClick:(Int, View, BaseNode) -> Unit)
    : ExpanableOddsAdapter<MatchOdd>() {

    private val recyclerPool by lazy { RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(3, 100) } }
    // 篮球末尾比分组合玩法
    private val playCates = listOf(
        PlayCate.FS_LD_CS.value,
        PlayCate.FS_LD_CS_SEG1.value,
        PlayCate.FS_LD_CS_SEG2.value,
        PlayCate.FS_LD_CS_SEG3.value,
        PlayCate.FS_LD_CS_SEG4.value,
    )
    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }


    init {
        footerWithEmptyEnable = true
        addFullSpanNodeProvider(EndScoreFirstProvider(this, onItemClick)) // 联赛
        addFullSpanNodeProvider(EndScoreSecondProvider(this, onItemClick)) // 比赛球队
        addNodeProvider(EndScoreThirdProvider(this, onItemClick)) //赔率
        addNodeProvider(EndScoreViewAllProvider(this, onItemClick)) //赔率
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setRecycledViewPool(recyclerPool)
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            is MatchOdd -> 2
            is ViewAllNode -> 4
            else -> 3
        }
    }


    // scoket推送的赔率变化，更新列表
    fun onMatchOdds(oddsChangeEvent: OddsChangeEvent): Boolean {
        if (oddsChangeEvent.eventId.isEmptyStr() || oddsChangeEvent.odds.isNullOrEmpty()) {
            return false
        }
        val matchOdd = currentVisiableMatchOdds[oddsChangeEvent.eventId] ?: return false
        if (oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }
        playCates.forEach { playCate->
            val newOdds = oddsChangeEvent.odds[playCate]
            //若为全量更新模式，则要清楚本地所有玩法数据，然后再重新组装数据
            if (oddsChangeEvent.isReplaceAll()){
                matchOdd.oddIdsMap.clear()
            }
            if (!newOdds.isNullOrEmpty()) {
                val idsMap = mutableMapOf<String, Odd>()
                newOdds.forEach { odd->
                    odd.parentNode = matchOdd
                    odd?.id?.let { idsMap[it] = odd }
                    matchOdd.oddIdsMap[playCate] = idsMap
                }
            }
        }
        matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum
        //更新翻譯
        if(matchOdd.betPlayCateNameMap == null){
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }
        oddsChangeEvent.betPlayCateNameMap?.let { matchOdd.betPlayCateNameMap!!.putAll(it) }
        if(matchOdd.playCateNameMap == null){
            matchOdd.playCateNameMap = mutableMapOf()
        }
        oddsChangeEvent.playCateNameMap?.let { matchOdd.playCateNameMap!!.putAll(it) }
        SocketUpdateUtil.sortOdds(matchOdd)
        matchOdd.updateOddStatus()
        val position = getItemPosition(matchOdd)
        if (position < 0) {
            return false
        }
        notifyItemChanged(position, SportMatchEvent.OddsChanged)
//        LogUtil.d(matchOdd.matchInfo?.id+","+matchOdd.oddIdsMap.keys)
        return true
    }


    fun updateFavorite(favorMatchIds: Set<String>) {
        rootNodes?.forEach { rootNode ->
            rootNode.childNode?.forEach {
                val matchOdd = (it as MatchOdd)
                matchOdd.matchInfo?.run {
                    val favorite = favorMatchIds.contains(id)
                    if (isFavorite != favorite) {
                        isFavorite = favorite
                        val position = getItemPosition(matchOdd)
                        if (position > 0) {
                            notifyItemChanged(position, SportMatchEvent.FavoriteChanged)
                        }
                    }
                }
            }
        }
    }

}