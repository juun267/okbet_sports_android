package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.widget.FakeBoldSpan

/**
 * @author kevin
 * @create 2022/3/31
 * @description
 */
fun RecyclerView.addScrollWithItemVisibility(onScrolling: () -> Unit, onVisible: (visibleList: List<Pair<Int, Int>>) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
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
                }

                //手指滾動
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    onScrolling()
                }
            }
        }
    })
}

fun RecyclerView.getVisibleRangePosition(): List<Int> {
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

/**
 * 初次獲取資料訂閱可視範圍內賽事(GameV3Fragment、GameLeagueFragment、MyFavoriteFragment)
 */
@SuppressLint("LogNotTimber")
fun RecyclerView.firstVisibleRange(leagueAdapter: LeagueAdapter, activity: Activity) {
    post {
        getVisibleRangePosition().forEach { leaguePosition ->
            val viewByPosition = layoutManager?.findViewByPosition(leaguePosition)
            viewByPosition?.let { view ->
                if (getChildViewHolder(view) is LeagueAdapter.ItemViewHolder) {
                    val viewHolder = getChildViewHolder(view) as LeagueAdapter.ItemViewHolder
                    viewHolder.itemView.league_odd_list.getVisibleRangePosition().forEach { matchPosition ->
                        if (leagueAdapter.data.isNotEmpty()) {
                            Log.d(
                                "[subscribe]",
                                "訂閱 ${leagueAdapter.data[leaguePosition].league.name} -> " +
                                        "${leagueAdapter.data[leaguePosition].matchOdds[matchPosition].matchInfo?.homeName} vs " +
                                        "${leagueAdapter.data[leaguePosition].matchOdds[matchPosition].matchInfo?.awayName}"
                            )
                            (activity as BaseSocketActivity<*>).subscribeChannelHall(
                                leagueAdapter.data[leaguePosition].gameType?.key,
                                leagueAdapter.data[leaguePosition].matchOdds[matchPosition].matchInfo?.id
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 設置大廳所需顯示的快捷玩法 (api未回傳的玩法需以“—”表示)
 * 2021.10.25 發現可能會回傳但是是傳null, 故新增邏輯, 該玩法odd為null時也做處理
 */
fun MutableMap<String, List<Odd?>?>.setupQuickPlayCate(playCate: String) {
    val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")

    playCateSort?.forEach {
        if (!this.keys.contains(it) || this[it] == null)
            this[it] = mutableListOf(null, null, null)
    }
}

/**
 * 根據QuickPlayCate的rowSort將盤口重新排序
 */
fun MutableMap<String, List<Odd?>?>.sortQuickPlayCate(playCate: String) {
    val playCateSort = QuickPlayCate.values().find { it.value == playCate }?.rowSort?.split(",")
    val sortedList = this.toSortedMap(compareBy<String> {
        val oddsIndex = playCateSort?.indexOf(it)
        oddsIndex
    }.thenBy { it })

    this.clear()
    this.putAll(sortedList)
}

/**
 * 調整標題文字間距
 * 中文之外無間距
 */
fun TextView.setTitleLetterSpacing() {
    this.letterSpacing =
        when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> 0.1F
            else -> 0F
        }
}

/**
 * 目前需求有font weight 500 約等於0.7f
 */
fun TextView.setTextWithStrokeWidth(str: String, width: Float) {
    val span = SpannableString(str)
    span.setSpan(FakeBoldSpan(width), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    text = span
}

/**
 * 特殊狀況需手動設定res(黑白模式)
 */
fun View.setBackColorWithColorMode(lightModeColor: Int, darkModeColor: Int) {
    setBackgroundColor(
        ContextCompat.getColor(
            context,
            if (MultiLanguagesApplication.isNightMode) darkModeColor else lightModeColor
        )
    )
}

/**
 * 移除所有ItemDecorations
 */
fun <T : RecyclerView> T.removeItemDecorations() {
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}

/**
 * 隊伍名稱過長處理 teamA v teamB
 * @param countCheck 數量超過時，後面隊伍換行
 */
fun TextView.setTeamNames(countCheck: Int, homeName: String?, awayName: String?) {
    text =
        if (homeName?.length ?: 0 > countCheck) "$homeName  v\n$awayName"
        else "$homeName  v  $awayName"
}