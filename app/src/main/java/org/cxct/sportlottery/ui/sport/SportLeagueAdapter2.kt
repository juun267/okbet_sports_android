//package org.cxct.sportlottery.ui.sport
//
//import android.view.View
//import android.view.ViewGroup
//import androidx.lifecycle.LifecycleOwner
//import org.cxct.sportlottery.network.common.MatchType
//import org.cxct.sportlottery.network.odds.list.LeagueOdd
//import org.cxct.sportlottery.network.odds.list.MatchOdd
//import org.cxct.sportlottery.ui.game.common.LeagueOddListener
//import org.cxct.sportlottery.enum.OddsType
//import org.cxct.sportlottery.ui.sport.vh.SportLeagueVH
//import org.cxct.sportlottery.ui.sport.vh.SportMatchVH
//import org.cxct.sportlottery.widget.expandablerecyclerview.ExpandableAdapter
//
//class SportLeagueAdapter2(private val lifecycle: LifecycleOwner,
//                          private val matchType: MatchType,
//                          private val onChildVieClick:(View, item: MatchOdd)-> Unit,
//                          private val leagueOddListener: LeagueOddListener,
//                          ): ExpandableAdapter<ExpandableAdapter.ViewHolder>() {
//
//    var data = mutableListOf<LeagueOdd>()
//    var oddsType: OddsType = OddsType.EU
//        set(value) {
//            if (value != field) {
//                field = value
//                notifyDataSetChanged()
//            }
//        }
//
//    override fun onCreateGroupViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        return SportLeagueVH.of(viewGroup.context, lifecycle)
//    }
//
//    override fun onCreateChildViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//        return SportMatchVH(viewGroup.context, matchType, lifecycle, onChildVieClick, leagueOddListener)
//    }
//
//    override fun onBindChildViewHolder(
//        holder: ViewHolder,
//        groupPosition: Int,
//        childPosition: Int,
//        payloads: List<Any>) {
//        val leagueOdd = data.get(groupPosition)
//        (holder as SportMatchVH).bind(leagueOdd.matchOdds.get(childPosition), leagueOdd, oddsType, payloads)
//    }
//
//    override fun onBindGroupViewHolder(
//        holder: ViewHolder,
//        groupPosition: Int,
//        expand: Boolean,
//        payloads: List<Any>) {
//        (holder as SportLeagueVH).bind(groupPosition, data.get(groupPosition), payloads)
//    }
//
//    override fun onGroupViewHolderExpandChange(
//        holder: ViewHolder,
//        groupPosition: Int,
//        animDuration: Long,
//        expand: Boolean) {
//        (holder as SportLeagueVH).onExpandChange(expand, data.get(groupPosition))
//    }
//
//    override fun getGroupCount() = data?.size ?: 0
//
//    override fun getChildCount(groupPosition: Int): Int {
//        return data.get(groupPosition).matchOdds?.size ?: 0
//    }
//}