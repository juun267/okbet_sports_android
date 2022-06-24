package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.repository.FLAG_CREDIT_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.outright.OutrightLeagueOddAdapter
import org.cxct.sportlottery.widget.FakeBoldSpan
import org.json.JSONArray

/**
 * @author kevin
 * @create 2022/3/31
 * @description
 */
fun RecyclerView.addScrollWithItemVisibility(
    onScrolling: () -> Unit,
    onVisible: (visibleList: List<Pair<Int, Int>>) -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val currentAdapter = adapter
            when (newState) {
                //停止
                RecyclerView.SCROLL_STATE_IDLE -> {

                    val visibleRangePair = mutableListOf<Pair<Int, Int>>()

                    when (currentAdapter) {
                        is LeagueAdapter -> {
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
                        }
                        //冠軍
                        is OutrightLeagueOddAdapter -> {
                            getVisibleRangePosition().forEach { leaguePosition ->
                                visibleRangePair.add(Pair(leaguePosition, -1))
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

fun RecyclerView.addScrollListenerForBottomNavBar(
    onScrollDown: (isScrollDown: Boolean) -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        var needChangeBottomBar = true
        var directionIsDown = true
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (needChangeBottomBar) {
                needChangeBottomBar = false
                //更新記錄的方向
                if (dy > 0) {
                    directionIsDown = true
                    onScrollDown(true)
                } else if (dy < 0) {
                    directionIsDown = false
                    onScrollDown(false)
                }
            }
            //Y軸移動的值和記錄的方向不同時, 重設狀態
            if (dy > 0 != directionIsDown) {
                needChangeBottomBar = true
            }

            //滑到最底部時顯示
            if (!recyclerView.canScrollVertically(1)) {
                onScrollDown(false)
            }
        }
    })
}

/**
 * 僅處理CoordinatorLayout內包含 [AppBarLayout, RecyclerView] 的上半部UI收合行為
 */
fun AppBarLayout.addOffsetListenerForBottomNavBar(
    onScrollDown: (isScrollDown: Boolean) -> Unit
) {
    addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
        var oldOffset = 0
        var needChangeBottomBar = true
        var directionIsDown = true
        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
            if (needChangeBottomBar) {
                needChangeBottomBar = false
                //更新記錄的方向
                if (oldOffset > verticalOffset) {
                    directionIsDown = true
                    onScrollDown(true)
                } else if (oldOffset < verticalOffset) {
                    directionIsDown = false
                    onScrollDown(false)
                }
            }
            //移動的值和記錄的方向不同時, 重設狀態
            if (oldOffset > verticalOffset != directionIsDown) {
                needChangeBottomBar = true
            }
            oldOffset = verticalOffset
        }
    })
}

fun ScrollView.addScrollListenerForBottomNavBar(
    onScrollDown: (isScrollDown: Boolean) -> Unit
) {
    setOnScrollChangeListener(object :View.OnScrollChangeListener {
        var needChangeBottomBar = true
        var directionIsDown = true
        override fun onScrollChange(
            v: View?,
            scrollX: Int,
            scrollY: Int,
            oldScrollX: Int,
            oldScrollY: Int
        ) {
            if (needChangeBottomBar) {
                needChangeBottomBar = false
                //更新記錄的方向
                if (scrollY > oldScrollY) {
                    directionIsDown = true
                    MultiLanguagesApplication.mInstance.setIsScrollDown(true)
                } else if (scrollY < oldScrollY) {
                    directionIsDown = false
                    MultiLanguagesApplication.mInstance.setIsScrollDown(false)
                }
            }
            //Y軸移動的值和記錄的方向不同時, 重設狀態
            if (scrollY > oldScrollY != directionIsDown) {
                needChangeBottomBar = true
            }

            //滑到最底部時顯示
            if (!canScrollVertically(1)) {
                onScrollDown(false)
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
 * 初次獲取冠軍資料訂閱可視範圍內賽事(GameV3Fragment(OutrightLeagueOddAdapter))
 *
 * @see org.cxct.sportlottery.ui.game.hall.GameV3Fragment.setOutrightLeagueAdapter
 * @see OutrightLeagueOddAdapter
 */
fun RecyclerView.firstVisibleRange(gameType: String?, outrightLeagueOddAdapter: OutrightLeagueOddAdapter, activity: Activity) {
    post {
        getVisibleRangePosition().forEach { leaguePosition ->
            val viewByPosition = layoutManager?.findViewByPosition(leaguePosition)
            viewByPosition?.let { view ->
                when (getChildViewHolder(view)) {
                    is OutrightLeagueOddAdapter.OutrightTitleViewHolder -> {
                        when (val outrightLeagueData = outrightLeagueOddAdapter.data[leaguePosition]) {
                            is MatchOdd -> {
                                Log.d(
                                    "[subscribe]",
                                    "訂閱 ${outrightLeagueData.matchInfo?.name} -> " +
                                            "${outrightLeagueData.matchInfo?.homeName} vs " +
                                            "${outrightLeagueData.matchInfo?.awayName}"
                                )
                                (activity as BaseSocketActivity<*>).subscribeChannelHall(
                                    gameType,
                                    outrightLeagueData.matchInfo?.id
                                )
                            }
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

/**
 * 依 config -> creditSystem 參數顯示
 */
fun View.setVisibilityByCreditSystem() {
    visibility = if (sConfigData?.creditSystem == FLAG_CREDIT_OPEN) View.GONE else View.VISIBLE
}

/**
 * 判斷當前是否為信用系統
 * @return true: 是, false: 否
 */
fun isCreditSystem(): Boolean {
    return sConfigData?.creditSystem == FLAG_CREDIT_OPEN
}

/**
 * 篩選玩法
 * 更新翻譯、排序
 */
fun MutableList<LeagueOdd>.updateOddsSort(
    gameType: String?,
    playCategoryAdapter: PlayCategoryAdapter
) {
    val playSelected = playCategoryAdapter.data.find { it.isSelected }
    val selectionType = playSelected?.selectionType
    val playSelectedCode = playSelected?.code
    val playCateMenuCode = when (playSelected?.selectionType) {
        SelectionType.SELECTABLE.code -> {
            playSelected.playCateList?.find { it.isSelected }?.code
        }
        SelectionType.UN_SELECTABLE.code -> {
            playSelected.code
        }
        else -> null
    }

    val mPlayCateMenuCode =
        if (selectionType == SelectionType.SELECTABLE.code) playCateMenuCode else playSelectedCode

    val oddsSortFilter =
        if (selectionType == SelectionType.SELECTABLE.code) playCateMenuCode else PlayCateMenuFilterUtils.filterOddsSort(
            gameType,
            mPlayCateMenuCode
        )
    val playCateNameMapFilter =
        if (selectionType == SelectionType.SELECTABLE.code) PlayCateMenuFilterUtils.filterSelectablePlayCateNameMap(
            gameType,
            playSelectedCode,
            mPlayCateMenuCode
        ) else PlayCateMenuFilterUtils.filterPlayCateNameMap(gameType, mPlayCateMenuCode)

    this.forEach { LeagueOdd ->
        LeagueOdd.matchOdds.forEach { MatchOdd ->
            MatchOdd.oddsSort = oddsSortFilter
            MatchOdd.playCateNameMap = playCateNameMapFilter
        }
    }
}

fun getLevelName(context: Context, level: Int): String {
    val jsonString = LocalJsonUtil.getLocalJson(MultiLanguagesApplication.appContext, "localJson/LevelName.json")
    val jsonArray = JSONArray(jsonString)
    val jsonObject = jsonArray.getJSONObject(level)
    return jsonObject.getString(LanguageManager.getSelectLanguage(context).key)
}

val playCateMappingList by lazy {
    val json = LocalJsonUtil.getLocalJson(
        MultiLanguagesApplication.appContext,
        "localJson/PlayCateMapping.json"
    )
    json.fromJson<List<PlayCateMapItem>>() ?: listOf()
}

/**
 * 設置WebView的日、夜間模式背景色, 避免還在讀取時出現與日夜模式不符的顏色區塊
 * @since 夜間模式時, WebView尚未讀取完成時會顯示其預設背景(白色)
 */
fun WebView.setWebViewCommonBackgroundColor(){
    setBackgroundColor(
        ContextCompat.getColor(
            context, if (MultiLanguagesApplication.isNightMode) {
                R.color.color_000000
            } else {
                R.color.color_FFFFFF
            }
        )
    )
}