package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
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
import org.cxct.sportlottery.view.stickyheader.StickyAdapter

// 篮球末位比分
class EndScoreAdapter(val onItemClick:(Int, View, BaseNode) -> Unit)
    : ExpanableOddsAdapter<MatchOdd>(), StickyAdapter<BaseViewHolder, BaseViewHolder> {

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
         if (oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }
        playCates.forEach { playCate->
            val newOdds = oddsChangeEvent.odds[playCate]
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
        LogUtil.d(matchOdd.matchInfo?.id+","+matchOdd.oddIdsMap.keys)
        notifyDataSetChanged()
        return true
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