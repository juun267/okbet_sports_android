package org.cxct.sportlottery.util

import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.ui.game.common.LeagueAdapter

/**
 * @author kevin
 * @create 2022/3/31
 * @description
 */
fun RecyclerView.addScrollWithItemVisibility(onScrolling: () -> Unit, onVisible: (visibleList: List<Pair<Int, Int>>) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when(newState){
                //停止
                RecyclerView.SCROLL_STATE_IDLE -> {

                    val visibleRangePair = mutableListOf<Pair<Int, Int>>()

                    getVisibleRangePosition().forEach { leaguePosition ->
                        val viewByPosition = layoutManager?.findViewByPosition(leaguePosition)
                        viewByPosition?.let {
                            if (getChildViewHolder(it) is LeagueAdapter.ItemViewHolder) {
                                val viewHolder = getChildViewHolder(it) as LeagueAdapter.ItemViewHolder
                                viewHolder.itemView.league_odd_list.getVisibleRangePosition().forEach { matchPosition ->
                                    visibleRangePair.add(Pair(leaguePosition, matchPosition))
                                }
                            }
                        }
                    }

                    onVisible(visibleRangePair)


//                    val manager = layoutManager
//                    if (manager is LinearLayoutManager) {
//                        val firstPosition = manager.findFirstVisibleItemPosition()
//                        val lastPosition = manager.findLastVisibleItemPosition()
//                        val visibleRangePair = mutableListOf<Pair<Int, Int>>()
//                        for (leaguePosition in firstPosition..lastPosition) {
//                            val view = manager.findViewByPosition(leaguePosition) ?: continue
//                            val rect = Rect()
//                            val isVisible = view.getGlobalVisibleRect(rect)
//                            if (isVisible) {
//                                val viewByPosition = manager.findViewByPosition(leaguePosition)
//                                viewByPosition?.let {
//                                    if (getChildViewHolder(it) is LeagueAdapter.ItemViewHolder) {
//                                        val viewHolder = getChildViewHolder(it) as LeagueAdapter.ItemViewHolder
//                                        val m = viewHolder.itemView.league_odd_list.layoutManager
//                                        if (m is LinearLayoutManager) {
//                                            val first: Int = m.findFirstVisibleItemPosition()
//                                            val last: Int = m.findLastVisibleItemPosition()
//                                            for (matchPosition in first..last) {
//                                                val mView = m.findViewByPosition(matchPosition) ?: continue
//                                                val mRect = Rect()
//                                                val mIsVisible = mView.getGlobalVisibleRect(mRect)
//                                                if (mIsVisible) {
//                                                    visibleRangePair.add(Pair(leaguePosition, matchPosition))
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        onVisible(visibleRangePair)
//                    }
                }

                //手指滾動
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    onScrolling()
                }
            }
        }
    })
}

fun RecyclerView.getVisibleRangePosition(): List<Int>{
    return mutableListOf<Int>().apply {
        val manager = layoutManager
        if (manager is LinearLayoutManager) {
            val first: Int = manager.findFirstVisibleItemPosition()
            val last: Int = manager.findLastVisibleItemPosition()
            for (i in first..last) {
                val v = manager.findViewByPosition(i) ?: continue
                val r = Rect()
                val isVisible = v.getGlobalVisibleRect(r)
                if (isVisible) {
                    this.add(i)
                }
            }
        }
    }
}