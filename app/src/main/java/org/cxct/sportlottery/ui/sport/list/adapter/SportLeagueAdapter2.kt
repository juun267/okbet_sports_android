package org.cxct.sportlottery.ui.sport.list.adapter

import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SocketUpdateUtil.toMutableFormat_1
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus
import org.cxct.sportlottery.util.closePlayCate

class SportLeagueAdapter2(
    var matchType: MatchType,
    val lifecycleOwner: LifecycleOwner,
    private val onNodeExpand: (BaseNode) -> Unit,
    onOddClick: OnOddClickListener,
    onFavorite:(String) -> Unit,
    val esportTheme: Boolean = false,
    val onAttachMatch: (org.cxct.sportlottery.network.odds.MatchInfo) -> Unit,
    val onDetachMatch: (org.cxct.sportlottery.network.odds.MatchInfo) -> Unit,
): ExpanableOddsAdapter<MatchOdd>() {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }


    init {
        footerWithEmptyEnable = true
        addFullSpanNodeProvider(SportLeagueProvider(this,esportTheme)) // 联赛名
        addFullSpanNodeProvider(SportMatchProvider(this,esportTheme, onOddClick, onFavorite)) // 赛事
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            is MatchOdd -> 2
            else -> 3
        }
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder.itemView.tag as MatchOdd?)?.matchInfo?.let {
            recodeRangeMatchOdd()
            onAttachMatch(it)
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        (holder.itemView.tag as MatchOdd?)?.matchInfo?.let {
            recodeRangeMatchOdd()
            onDetachMatch(it)
        }
    }

    fun nodeExpandOrCollapse(node: BaseNode, parentPayload: Any?): Int {
        val num = expandOrCollapse(node, parentPayload = parentPayload)
        onNodeExpand.invoke(node)
        return num
    }

    fun dataCount() = getDefItemCount()

    fun notifyMatchOddChanged(matchOdd: MatchOdd) {
        val position = getItemPosition(matchOdd)
        if (position >= dataCount() || position < 0) {
            return
        }

        notifyMatchItemChanged(position, matchOdd)
    }

    // 对外保留的方法刷新item（添加headerview后刷新item时position计算需要加上headerview）
    fun notifyMatchItemChanged(position: Int, any: Any) {
        notifyItemChanged(position, any)
//        notifyItemChanged(position + headerLayoutCount, any)
    }

    fun onOddsChangeEvent(oddsChangeEvent: OddsChangeEvent): Int {
        val eventId = oddsChangeEvent.eventId ?: return -1
        if (oddsChangeEvent.oddsList.isNullOrEmpty() || currentVisiableMatchOdds.isEmpty()) {
            return -2
        }

        val matchOdd = findVisiableRangeMatchOdd(eventId) ?: return -3
        val position = getItemPosition(matchOdd)
        if (position >= dataCount() || position < 0) {
            return -4
        }

        if (updateMatchOdds(matchOdd, oddsChangeEvent)) {
            notifyMatchItemChanged(position, SportMatchEvent.OddsChanged)
            return position
//            updateMatch(leagueIndex, matchOdd)
//            updateBetInfo(leagueOdd, oddsChangeEvent)
        }
        return -100 * position
    }

    private fun updateMatchOdds(matchOdd: org.cxct.sportlottery.network.common.MatchOdd,
                                oddsChangeEvent: OddsChangeEvent): Boolean {

        var isNeedRefresh = false
        var isNeedRefreshPlayCate = false

        val cateMenuCode = oddsChangeEvent.channel?.split(context.getString(R.string.splash_no_trans))?.getOrNull(6)
        isNeedRefresh = when {

            (cateMenuCode == PlayCate.EPS.value) -> {
                SocketUpdateUtil.updateMatchOdds(
                    mutableMapOf(Pair(PlayCate.EPS.value, matchOdd.oddsEps?.eps?.toMutableList() ?: mutableListOf())),
                    oddsChangeEvent.odds,oddsChangeEvent.updateMode)
            }


            (QuickPlayCate.values().map { it.value }.contains(cateMenuCode)) -> {
                SocketUpdateUtil.updateMatchOdds(
                    matchOdd.quickPlayCateList?.find { it.isSelected }?.quickOdds?.toMutableFormat_1()
                        ?: mutableMapOf(), oddsChangeEvent.odds,oddsChangeEvent.updateMode)
            }

            else -> {
                if (matchOdd.oddsMap == null) {
                    matchOdd.oddsMap = mutableMapOf()
                }
                SocketUpdateUtil.updateMatchOdds(
                    matchOdd.oddsMap as MutableMap<String, MutableList<Odd?>?>? ?: mutableMapOf(),
                    oddsChangeEvent.odds,oddsChangeEvent.updateMode
                )
            }
        }

        //更新翻譯
        if (matchOdd.betPlayCateNameMap == null) {
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }

        SocketUpdateUtil.updateBetPlayCateNameMap(
            matchOdd.betPlayCateNameMap,
            oddsChangeEvent.betPlayCateNameMap
        )

        if (matchOdd.playCateNameMap == null) {
            matchOdd.playCateNameMap = mutableMapOf()
        }

        SocketUpdateUtil.updatePlayCateNameMap(
            matchOdd.playCateNameMap,
            oddsChangeEvent.playCateNameMap
        )

        isNeedRefreshPlayCate = when (matchOdd.quickPlayCateList.isNullOrEmpty()) {
            true -> {
                SocketUpdateUtil.insertPlayCate(matchOdd, oddsChangeEvent, matchType)
            }

            false -> {
                SocketUpdateUtil.refreshPlayCate(matchOdd, oddsChangeEvent, matchType)
            }
        }

        isNeedRefreshPlayCate =
            isNeedRefreshPlayCate || (matchOdd.matchInfo?.playCateNum != oddsChangeEvent.playCateNum)

        if (isNeedRefresh) {
            SocketUpdateUtil.sortOdds(matchOdd)
            matchOdd.updateOddStatus()
        }

        if (isNeedRefreshPlayCate) {
            matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum
        }

        return isNeedRefresh || isNeedRefreshPlayCate

    }

    private fun removeMatchOdd(matchOdd: MatchOdd) {
        val leagueOdd = matchOdd.parentNode as LeagueOdd
        if (leagueOdd.childNode.isEmpty() || leagueOdd.childNode.size  == 1) {
            remove(leagueOdd)
        } else {
            nodeRemoveData(leagueOdd, matchOdd)
        }
    }

    fun removeMatchOdd(matchId: String) {
        data.forEach { baseNode ->
            if (baseNode is MatchOdd && baseNode.matchInfo?.id == matchId) {
                removeMatchOdd(baseNode)
                return
            }
        }
    }

    fun matchStatuChanged(matchOdd: MatchOdd) {
        val position = getItemPosition(matchOdd)
        if (position >= 0) {
            notifyMatchItemChanged(position, SportMatchEvent.MatchStatuChanged)
        }
    }

    fun updateOddsSelectStatus() {
        currentVisiableMatchOdds.values.forEach { matchOdd ->
            val index = getItemPosition(matchOdd)
            if (index > 0) {
                matchOdd.oddsMap?.values?.forEachIndexed { _, oddsList ->
                    oddsList?.forEach { odd ->
                        val isSelected = odd.id?.let { QuickListManager.containOdd(it) } == true
                        if (odd.isSelected != isSelected) {
                            odd.isSelected = isSelected
                            notifyMatchItemChanged(index, SportMatchEvent.OddSelected)
                            return@forEachIndexed
                        }
                    }
                }
            }
        }
    }

    fun closePlayCate(closeEvent: FrontWsEvent.ClosePlayCateEvent) {
        if (getCount() < 1 || rootNodes.isNullOrEmpty()) {
            return
        }
        (rootNodes!!.toMutableList() as MutableList<LeagueOdd>).closePlayCate(closeEvent)
        notifyDataSetChanged()
    }


    override fun toString(): String {
        return " ==SportLeagueAdapter2 ${lifecycleOwner::class.java.simpleName} $matchType==  "
    }
   fun setESportTheme(){

   }
}