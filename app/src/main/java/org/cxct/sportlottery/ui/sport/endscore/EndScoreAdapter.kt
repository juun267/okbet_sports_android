package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus
import org.cxct.sportlottery.view.stickyheader.StickyAdapter

// 篮球末位比分
class EndScoreAdapter(val playCates: List<String>, val onItemClick:(Int, View, BaseNode) -> Unit)
    : ExpanableOddsAdapter<MatchOdd>(), StickyAdapter<BaseViewHolder, BaseViewHolder> {

    private val recyclerPool by lazy { RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(3, 100) } }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    private val matchItemProvider = EndScoreSecondProvider(this, onItemClick)

    init {
        footerWithEmptyEnable = true
        addFullSpanNodeProvider(EndScoreFirstProvider(this, onItemClick)) // 联赛
        addFullSpanNodeProvider(matchItemProvider) // 比赛球队
        addNodeProvider(EndScoreThirdProvider(this, onItemClick)) //赔率
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.setRecycledViewPool(recyclerPool)
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            is MatchOdd -> 2
            else -> 3
        }
    }

    override fun dataCount() = getDefItemCount()

    override fun getAdapter(): RecyclerView.Adapter<BaseViewHolder> {
        return this
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        return if(getItem(itemPosition) is MatchOdd) itemPosition else -1
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): BaseViewHolder {
        return matchItemProvider.onCreateViewHolder(parent, 2)
    }

    override fun onBindHeaderViewHolder(holder: BaseViewHolder, headerPosition: Int) {
        matchItemProvider.convert(holder, getItem(headerPosition))
    }


    // scoket推送的赔率变化，更新列表
    fun onMatchOdds(oddsChangeEvent: OddsChangeEvent): Boolean {
        if (oddsChangeEvent.eventId.isEmptyStr() || oddsChangeEvent.odds.isNullOrEmpty()) {
            return false
        }

        val matchOdd = currentVisiableMatchOdds[oddsChangeEvent.eventId] ?: return false
        LogUtil.toJson(oddsChangeEvent.oddsList.map { it.playCateCode })
        if (oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }
        var needNotifyAllChanged = false
        playCates.forEach {
            // 过滤玩法赔率
            val newOdds = oddsChangeEvent.odds[it]
            if (!newOdds.isNullOrEmpty()) {
                if (updateMatchOdds(matchOdd, newOdds, oddsChangeEvent)) {
                    needNotifyAllChanged = true
                }
            }
        }
        if (needNotifyAllChanged) {
            notifyDataSetChanged()
        }
        return needNotifyAllChanged
    }

    private fun resetOddIdsMap(matchOdd: MatchOdd, playCates: List<String>) {
        val oddList = matchOdd.childNode as MutableList<Odd>
        val idsMap = mutableMapOf<String, Odd>()
        oddList.forEach { odd->
            odd.parentNode = matchOdd
            odd?.id?.let { idsMap[it] = odd }
            playCates.forEach {
                matchOdd.oddIdsMap[it] = idsMap
            }
        }
    }

    // 赔率变化更新列表赔率
    private fun updateMatchOdds(matchOdd: MatchOdd, newOdds: MutableList<Odd>, oddsChangeEvent: OddsChangeEvent): Boolean {

        var isNeedRefresh = false
        var oddsMap = matchOdd.childNode as MutableList<Odd>
        if (oddsMap.isNullOrEmpty()) {
            nodeAddData(matchOdd, newOdds)
            resetOddIdsMap(matchOdd, playCates)
        } else {
            val oddList = matchOdd.childNode as MutableList<Odd>
            isNeedRefresh = refreshMatchOdds(playCates, matchOdd, oddList, newOdds)
        }

        //更新翻譯
        if(matchOdd.betPlayCateNameMap == null){
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }
        oddsChangeEvent.betPlayCateNameMap?.let { matchOdd.betPlayCateNameMap!!.putAll(it) }

        if(matchOdd.playCateNameMap == null){
            matchOdd.playCateNameMap = mutableMapOf()
        }

        oddsChangeEvent.playCateNameMap?.let { matchOdd.playCateNameMap!!.putAll(it) }

        if (isNeedRefresh) {
//            SocketUpdateUtil.sortOdds(matchOdd)
            matchOdd.updateOddStatus()
        }

        matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum

        if (isNeedRefresh) {
            notifyDataSetChanged()
        }

        return isNeedRefresh
    }

    private fun refreshMatchOdds(playCates: List<String>, matchOdd: MatchOdd, oddsMap: MutableList<Odd>, oddsMapSocket: List<Odd>): Boolean {

        if (oddsMap.all { it == null }) { // 如果oddsMap里面全是空对象(之前的逻辑)
            nodeReplaceChildData(matchOdd, oddsMapSocket)
            resetOddIdsMap(matchOdd, playCates)
            return false
        }
        var isNeedRefresh = false
        playCates.forEach { playCate->
            val oddIdsMap: MutableMap<String, Odd> = matchOdd.oddIdsMap[playCate] ?: return false

            for (socketOdd in oddsMapSocket) {
                if (socketOdd == null) {
                    continue
                }
                val odd = oddIdsMap[socketOdd.id]
                if (odd == null) {
                    socketOdd.parentNode = matchOdd
                    oddIdsMap["${socketOdd.id}"] = socketOdd
                    oddsMap.add(socketOdd)
                    continue
                }
                isNeedRefresh = true
                refreshOdds(odd, socketOdd)
            }
        }
        return isNeedRefresh
    }


    fun updateFavorite(favorMatchIds: List<String>) {
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