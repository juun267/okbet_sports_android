package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Environment
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.item_favorite.view.*
import kotlinx.android.synthetic.main.view_account_balance_2.*
import kotlinx.coroutines.flow.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.common.extentions.screenHeight
import org.cxct.sportlottery.common.extentions.translationXAnimation
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.CateDetailData
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.network.service.close_play_cate.ClosePlayCateEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.common.dialog.ServiceDialog
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.live.HomeLiveAdapter
import org.cxct.sportlottery.ui.maintab.live.ItemHomeLiveHolder
import org.cxct.sportlottery.ui.sport.favorite.FavoriteAdapter
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.DisplayUtil.dpToPx
import org.cxct.sportlottery.util.SvgUtil.setSvgIcon
import org.cxct.sportlottery.view.boundsEditText.TextFieldBoxes
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.statusSelector.StatusSpinnerAdapter
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


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
            Timber.d("onScrollStateChanged")
            when (newState) {
                //停止
                RecyclerView.SCROLL_STATE_IDLE -> {
                    Timber.d("SCROLL_STATE_IDLE")
                    val visibleRangePair = mutableListOf<Pair<Int, Int>>()

                    when (currentAdapter) {

                        is FavoriteAdapter -> {
                            getVisibleRangePosition().forEach { leaguePosition ->
                                val viewByPosition =
                                    layoutManager?.findViewByPosition(leaguePosition)
                                viewByPosition?.let {
                                    if (getChildViewHolder(it) is FavoriteAdapter.ItemViewHolder) {
                                        val viewHolder =
                                            getChildViewHolder(it) as FavoriteAdapter.ItemViewHolder
                                        viewHolder.itemView.rv_league.getVisibleRangePosition()
                                            .forEach { matchPosition ->
                                                visibleRangePair.add(
                                                    Pair(
                                                        leaguePosition,
                                                        matchPosition
                                                    )
                                                )
                                            }
                                    }
                                }
                            }
                        }

                        is HomeLiveAdapter -> {
                            getVisibleRangePosition().forEach { leaguePosition ->
                                visibleRangePair.add(Pair(leaguePosition, -1))
                            }
                        }
                    }

                    onVisible(visibleRangePair)
                }

                //手指滾動
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    Timber.d("SCROLL_STATE_DRAGGING")
                    onScrolling()
                }
            }
        }
    })
}

fun RecyclerView.setupBackTop(targetView: View, offset: Int) {

    var targetWidth = 0f
    var animaIdle = true
    val animEndCall: () -> Unit = { animaIdle = true }
    val hideRunnable = {
        animaIdle = false
        targetView.translationXAnimation(targetWidth, animEndCall)
    }

    targetView.setOnClickListener {
        hideRunnable.invoke()
        smoothScrollToPosition(0)
    }

    targetView.post {
        targetWidth = targetView.measuredWidth.toFloat()
        if (targetView.translationX != targetWidth) { targetView.translationX = targetWidth }
    }

    addOnScrollListener(object : OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (!animaIdle) {
                return
            }

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if (targetView.translationX != targetWidth) {
                    hideRunnable.invoke()
                }
                return
            }

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (computeVerticalScrollOffset() > offset) {
                    if (targetView.translationX != 0f) {
                        animaIdle = false
                        targetView.translationXAnimation(0f, animEndCall)
                    }
                }
            }
        }

    })
}


// 监听NestedScrollView滑出屏幕距离(offset)显示返回顶部按钮
fun NestedScrollView.setupBackTop(targetView: View, offset: Int) {

    var targetWidth = 0f
    targetView.setOnClickListener { smoothScrollTo(0, 0) }
    targetView.post {
        targetWidth = targetView.measuredWidth.toFloat()
        if (targetView.translationX != targetWidth) { targetView.translationX = targetWidth }
    }

    var lastY = 0
    var animaIdle = true
    val animEndCall: () -> Unit = { animaIdle = true }
    val runnable = Runnable {
        if (!animaIdle) {
            return@Runnable
        }

        if (lastY > offset) {
            if (targetView.translationX != 0f) {
                animaIdle = false
                targetView.translationXAnimation(0f, animEndCall)
            }
        }
    }

    setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
        if (!animaIdle) {
            return@setOnScrollChangeListener
        }

        if (scrollY < oldScrollY) { // 往上回滚，根据条件立即隐藏返回顶部按钮
            lastY = scrollY
            if (targetView.translationX != targetWidth) {
                animaIdle = false
                targetView.translationXAnimation(targetWidth, animEndCall)
            }

            if (offset > scrollY) {
                return@setOnScrollChangeListener
            }
        }

        lastY = scrollY
        targetView.removeCallbacks(runnable)
        targetView.postDelayed(runnable, 80)
    }

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
@Deprecated("不建议使用这种没有复用逻辑的集合式")
@SuppressLint("LogNotTimber")
fun RecyclerView.firstVisibleRange(
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
    activity: Activity,
) {
    post {
        getVisibleRangePosition().forEach { leaguePosition ->
            val viewByPosition = layoutManager?.findViewByPosition(leaguePosition)
            when (adapter) {

                is FavoriteAdapter -> {
                    viewByPosition?.let { view ->
                        if (getChildViewHolder(view) is FavoriteAdapter.ItemViewHolder) {
                            val viewHolder =
                                getChildViewHolder(view) as FavoriteAdapter.ItemViewHolder
                            viewHolder.itemView.rv_league.getVisibleRangePosition()
                                .forEach { matchPosition ->
                                    if (adapter.data.isNotEmpty()) {
                                        Log.d(
                                            "[subscribe]",
                                            "訂閱 ${adapter.data.getOrNull(leaguePosition)?.league?.name} -> " +
                                                    "${
                                                        adapter.data.getOrNull(leaguePosition)?.matchOdds?.getOrNull(
                                                            matchPosition
                                                        )?.matchInfo?.homeName
                                                    } vs " +
                                                    "${
                                                        adapter.data.getOrNull(leaguePosition)?.matchOdds?.getOrNull(
                                                            matchPosition
                                                        )?.matchInfo?.awayName
                                                    }"
                                        )
                                        (activity as BaseSocketActivity<*>).subscribeChannelHall(
                                            adapter.data.getOrNull(leaguePosition)?.gameType?.key,
                                            adapter.data.getOrNull(leaguePosition)?.matchOdds?.getOrNull(
                                                matchPosition
                                            )?.matchInfo?.id
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }

    }
}

@SuppressLint("LogNotTimber")
fun RecyclerView.firstVisibleRange(adapter: HomeLiveAdapter, activity: Activity) {
    post {
        getVisibleRangePosition().forEach { leaguePosition ->
            val viewByPosition = layoutManager?.findViewByPosition(leaguePosition)
            when (adapter) {
                is HomeLiveAdapter -> {
                    viewByPosition?.let { view ->
                        if (getChildViewHolder(view) is ItemHomeLiveHolder) {
                            val itemdata = adapter.data[leaguePosition]
                            if (itemdata is MatchLiveData) {
                                Log.d(
                                    "[subscribe]",
                                    "訂閱 ${itemdata.matchInfo?.name} -> " +
                                            "${itemdata.matchInfo?.homeName} vs " +
                                            "${itemdata.matchInfo?.awayName}"
                                )
                                (activity as BaseSocketActivity<*>).subscribeChannelHall(
                                    itemdata.matchInfo?.gameType,
                                    itemdata.matchInfo?.id
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
 * 调整中文文字间距
 * 中文之外無間距
 */
fun TextView.setTitleLetterSpacing2F() {
    this.letterSpacing =
        when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> 0.2F
            else -> 0F
        }
}

/**
 * 设置textview文字渐变
 */
fun TextView.setGradientSpan(startColor: Int, endColor: Int, isLeftToRight: Boolean) {
    var spannableStringBuilder = SpannableStringBuilder(text)
    var span = LinearGradientFontSpan(startColor, endColor, isLeftToRight)
    spannableStringBuilder.setSpan(
        span,
        0,
        spannableStringBuilder.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    setText(spannableStringBuilder, TextView.BufferType.SPANNABLE)
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

fun loginedRun(context: Context, block: ()-> Unit): Boolean {
    if (LoginRepository.isLogined()) {
        block.invoke()
        return true
    }

    if (context is Activity) {
        Snackbar.make(
            context.findViewById(android.R.id.content),
            context.getString(R.string.login_notify),
            Snackbar.LENGTH_LONG
        ).apply {
            val snackView: View = context.layoutInflater.inflate(
                R.layout.snackbar_login_notify,
                context.findViewById(android.R.id.content),
                false
            )
            (this.view as Snackbar.SnackbarLayout).apply {
                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    visibility = View.INVISIBLE
                }
                background.alpha = 0
                addView(snackView, 0)
                setPadding(0, 0, 0, 0)
            }

            if (context is MainTabActivity) {
                setAnchorView(R.id.cLBottom)
            }
            show()
        }
        return false
    }

    context.startActivity(Intent(context, LoginOKActivity::class.java))
    return false
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
//        if ((homeName?.length ?: 0) > countCheck) "$homeName  v$awayName"
//        else
        "$homeName  VS  $awayName"
}

/**
 * 依 config -> creditSystem 參數顯示
 */
fun View.setVisibilityByCreditSystem() {
    visibility = if (isCreditSystem()) View.GONE else View.VISIBLE
}

fun View.setVisibilityByMarketSwitch() {
    visibility = if (SPUtil.getMarketSwitch()) View.GONE else View.VISIBLE
}

/**
 * sConfigData?.realNameWithdrawVerified -> 判斷提現有沒有開啟KYC認證
 * enableKYCVerify的判斷已經棄用
 */
fun isKYCVerifyWithdrawOpen(): Boolean {
    return sConfigData?.realNameWithdrawVerified.isStatusOpen()
}

/**
 * sConfigData?.realNameRechargeVerified -> 判斷充值有沒有開啟KYC認證
 * enableKYCVerify的判斷已經棄用
 */
fun isKYCVerifyRechargeOpen(): Boolean {
    return sConfigData?.realNameRechargeVerified.isStatusOpen()
}

/**
 * 判斷當前是否為信用系統
 * @return true: 是, false: 否
 */
fun isCreditSystem(): Boolean {
    return sConfigData?.creditSystem == FLAG_CREDIT_OPEN
//    return true // for test
}

/**
 * 第三方自動轉換功能是否開啟
 * @return true: 是, false: 否
 */
fun isThirdTransferOpen(): Boolean {
    return sConfigData?.thirdTransferOpen == FLAG_OPEN
//    return true // for test
}

inline fun String?.isStatusOpen(): Boolean {
    return this == FLAG_OPEN
}


/**
 * 設置WebView的日、夜間模式背景色, 避免還在讀取時出現與日夜模式不符的顏色區塊
 * @since 夜間模式時, WebView尚未讀取完成時會顯示其預設背景(白色)
 */
fun WebView.setWebViewCommonBackgroundColor() {
    setBackgroundColor(
        if (MultiLanguagesApplication.isNightMode) Color.BLACK else Color.WHITE
    )
}

/**
 * 展開下拉選單
 * ##點擊覆蓋一個View在註冊頁的TextFieldBoxes上避免觸發TextFieldBoxes的行為
 * @param editText: 註冊頁中的ExtendedEditText
 * @param textFieldBoxes: 註冊頁中的TextFieldBoxes
 * @see org.cxct.sportlottery.widget.boundsEditText.ExtendedEditText
 * @see org.cxct.sportlottery.widget.boundsEditText.TextFieldBoxes
 *
 * 取自
 * @see org.cxct.sportlottery.ui.component.StatusSpinnerView
 */
//针对TextFormFieldBoxes和TextFieldBoxes控件类型写的重载方法
@SuppressLint("ClickableViewAccessibility")
fun View.setSpinnerView(
    editText: EditText,
    textFieldBoxes: TextFormFieldBoxes,
    spinnerList: List<StatusSheetData>,
    touchListener: () -> Unit,
    itemSelectedListener: (data: StatusSheetData?) -> Unit,
    popupWindowDismissListener: () -> Unit
) {
    val spinnerAdapter: StatusSpinnerAdapter?

    var selectItem: StatusSheetData
    var mListPop = ListPopupWindow(context)

    var rawY = 0F
    setOnTouchListener { _, event ->
        rawY = event?.rawY ?: 0F
        false
    }

    setOnClickListener {
        var totalHeight = 0F

        for (i in 0..spinnerList.size) {
            totalHeight += 40F.dpToPx
        }
        val currentHeight = context.screenHeight - rawY

        if (totalHeight > currentHeight) {
            mListPop.height = currentHeight.toInt() - 200
        } else {
            mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (mListPop.isShowing) {
            mListPop.dismiss()
        } else {
            mListPop.show()
        }

        if (!editText.isFocused) {
            //設置TextFieldBoxes為選中狀態, 但EditText不給予focus(不給予focus以不觸發系統鍵盤出現)
            textFieldBoxes.setHasFocus(true, false)
        }
        //隱藏光標
        editText.isCursorVisible = false

        touchListener()
    }

    if (spinnerList.isNotEmpty()) {
        val first = spinnerList[0]
        first.isChecked = true
        selectItem = first
    }
    spinnerAdapter = StatusSpinnerAdapter(spinnerList.toMutableList())
    spinnerAdapter.setItmeColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
    mListPop = ListPopupWindow(context)
    mListPop.width = textFieldBoxes.width
    mListPop.setAdapter(spinnerAdapter)

    mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
    mListPop.setBackgroundDrawable(
        ContextCompat.getDrawable(
            context,
            R.drawable.bg_play_category_pop
        )
    )

    mListPop.anchorView = textFieldBoxes //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
    mListPop.isModal = true //设置是否是模式
    mListPop.setOnItemClickListener { _, _, position, _ ->
        //隱藏EditText的光標
        editText.isCursorVisible = false
        mListPop.dismiss()
        selectItem = spinnerList[position]
        selectItem?.isChecked = true
        spinnerList.find { it != selectItem && it.isChecked }?.isChecked = false
        itemSelectedListener.invoke(selectItem)
    }
    //PopupWindow關閉時
    mListPop.setOnDismissListener {
        textFieldBoxes.hasFocus = false
        editText.clearFocus()
        popupWindowDismissListener()
    }
}

//针对TextFormFieldBoxes和TextFieldBoxes控件类型写的重载方法
@SuppressLint("ClickableViewAccessibility")
fun View.setSpinnerView(
    editText: EditText,
    textFieldBoxes: TextFieldBoxes,
    spinnerList: List<StatusSheetData>,
    touchListener: () -> Unit,
    itemSelectedListener: (data: StatusSheetData?) -> Unit,
    popupWindowDismissListener: () -> Unit,
) {
    val spinnerAdapter: StatusSpinnerAdapter?

    var selectItem: StatusSheetData?
    var mListPop = ListPopupWindow(context)

    var rawY = 0F
    setOnTouchListener { _, event ->
        rawY = event?.rawY ?: 0F
        false
    }

    setOnClickListener {
        var totalHeight = 0F

        for (i in 0..spinnerList.size) {
            totalHeight += 40F.dpToPx
        }
        val currentHeight = context.screenHeight - rawY

        if (totalHeight > currentHeight) {
            mListPop.height = currentHeight.toInt() - 200
        } else {
            mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        }
        if (mListPop.isShowing) {
            mListPop.dismiss()
        } else {
            mListPop.show()
        }

        if (!editText.isFocused) {
            //設置TextFieldBoxes為選中狀態, 但EditText不給予focus(不給予focus以不觸發系統鍵盤出現)
            textFieldBoxes.setHasFocus(true, false)
        }
        //隱藏光標
        editText.isCursorVisible = false

        touchListener()
    }

    if (spinnerList.isNotEmpty()) {
        val first = spinnerList[0]
        first.isChecked = true
        selectItem = first
    }
    spinnerAdapter = StatusSpinnerAdapter(spinnerList.toMutableList())
    spinnerAdapter.setItmeColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
    mListPop = ListPopupWindow(context)
    mListPop.width = textFieldBoxes.width
    mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT

    mListPop.setBackgroundDrawable(
        ContextCompat.getDrawable(
            context,
            R.drawable.bg_play_category_pop
        )
    )
    mListPop.setAdapter(spinnerAdapter)
    mListPop.anchorView = textFieldBoxes //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
    mListPop.isModal = true //设置是否是模式
    mListPop.setOnItemClickListener { _, _, position, _ ->
        //隱藏EditText的光標
        editText.isCursorVisible = false
        mListPop.dismiss()
        selectItem = spinnerList[position]
        selectItem?.isChecked = true
        spinnerList.find { it != selectItem && it.isChecked }?.isChecked = false
        itemSelectedListener.invoke(selectItem)
    }
    //PopupWindow關閉時
    mListPop.setOnDismissListener {
        textFieldBoxes.hasFocus = false
        editText.clearFocus()
        popupWindowDismissListener()
    }
}

/**
 * 判斷盤口啟用參數是否有配置
 * @return true: 有配置, false: 沒有配置(為空或null)
 */
fun isHandicapShowSetup(): Boolean {
    return sConfigData?.handicapShow?.isEmpty() == false
}

/**
 * 判斷盤口類型是否有開放
 * 若sConfigData?.handicapShow為空或null則開放預設的四項(EU,HK,MY,ID)
 */
fun isOddsTypeEnable(handicapType: HandicapType): Boolean {
    return isOddsTypeEnable(handicapType.name)
}

fun isOddsTypeEnable(handicapTypeCode: String): Boolean {
    return !isHandicapShowSetup() || sConfigData?.handicapShow?.contains(handicapTypeCode) == true
}

/**
 * 根據盤口類型是否有開放顯示或隱藏View
 */
fun View.setupOddsTypeVisibility(handicapType: HandicapType) {
    visibility = if (isOddsTypeEnable(handicapType)) View.VISIBLE else View.GONE
}

/**
 * 獲取盤口類型預設盤口, 若未配置預設為原先的HK
 */
fun getDefaultHandicapType(): HandicapType {
    return when (sConfigData) {
        //config尚未取得
        null -> HandicapType.NULL
        else -> {
            when {
                //region 若sConfigData?.handicapShow為空或null則開放預設為HK
                !isHandicapShowSetup() -> {
                    HandicapType.HK
                }
                //endregion
                //region 第一個盤口作為預設盤口
                else -> {
                    when (sConfigData?.handicapShow?.split(",")
                        ?.first { type -> type.isNotEmpty() }) {
                        HandicapType.EU.name -> HandicapType.EU
                        HandicapType.HK.name -> HandicapType.HK
                        HandicapType.MY.name -> HandicapType.MY
                        HandicapType.ID.name -> HandicapType.ID
                        else -> HandicapType.HK
                    }
                }
                //endregion
            }
        }
    }
}

var updatingDefaultHandicapType = false

/**
 * 僅作為獲取config後更新預設盤口使用
 */
fun setupDefaultHandicapType() {
    //若處於更新中則不再更新
    if (!updatingDefaultHandicapType) {
        updatingDefaultHandicapType = true
        //若當前盤口尚未配置預設盤口
        if (MultiLanguagesApplication.mInstance.sOddsType == HandicapType.NULL.name) {
            OddsType.values()
                .firstOrNull { oddsType -> oddsType.code == getDefaultHandicapType().name }
                ?.let { defaultOddsType ->
                    MultiLanguagesApplication.saveOddsType(defaultOddsType)
                }
        } else {
            MultiLanguagesApplication.mInstance.getOddsType()
        }
        updatingDefaultHandicapType = false
    }
}

/**
 * @since 原先儲存的盤口配置檢查發現當前不可用時, 需要重新配置預設盤口
 */
fun updateDefaultHandicapType() {
    //若config尚未取得或處於更新中則不再更新
    if (!updatingDefaultHandicapType) {
        updatingDefaultHandicapType = true
        OddsType.values()
            .firstOrNull { oddsType -> oddsType.code == getDefaultHandicapType().name }
            ?.let { defaultOddsType ->
                MultiLanguagesApplication.saveOddsType(defaultOddsType)
            }
        updatingDefaultHandicapType = false
    }
}

/**
 * 對Bitmap進行壓縮並回傳File型態
 * @param image 壓縮對象
 * @param sizeLimit 欲壓縮後大小(kb)
 */
private fun compressImageToFile(image: Bitmap, sizeLimit: Int): File? {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    var options = 90
    //循环判断如果压缩后图片是否大于sizeLimit,大于继续压缩
    while ((baos.toByteArray().size / 1024) > sizeLimit && options >= 0) {
        //重置baos即清空baos
        baos.reset()
        //这里压缩options%，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, options, baos)
        //每次都减少10
        options -= 10
    }

    val path =
        MultiLanguagesApplication.appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val file = File.createTempFile(Math.random().toString(), ".png", path)
    val os = FileOutputStream(file)
    os.write(baos.toByteArray())
    os.close()

    return file
}

/**
 * 對Bitmap尺寸進行等比縮放
 * @param bitmap 縮放對象
 * @param sizeLimit 長或寬的最大尺寸
 * @return 縮放後Bitmap
 */
private fun resizeBitmap(bitmap: Bitmap, sizeLimit: Int): Bitmap? { //拍照的圖片太大，設定格式大小
    //獲取原來圖片的寬高
    val width = bitmap.width
    val height = bitmap.height
    //計算原來圖片的高寬之比
    val temp = height.toFloat() / width.toFloat()

    //以較大的寬或高進行計算至sizeLimit
    val scaleWidth: Float
    val scaleHeight: Float
    when {
        width > sizeLimit || height > sizeLimit -> {
            when {
                height > width -> {
                    //根據傳入的新圖片的高度計算新圖片的寬度
                    val newWidth = (sizeLimit / temp).toInt()
                    scaleWidth = newWidth.toFloat() / width
                    scaleHeight = sizeLimit.toFloat() / height
                }

                else -> {
                    //根據傳入的新圖片的寬度計算新圖片的高度
                    val newHeight = (sizeLimit * temp).toInt()
                    scaleWidth = sizeLimit.toFloat() / width
                    scaleHeight = newHeight.toFloat() / height
                }
            }
        }

        else -> {
            return bitmap
        }
    }
    //Bitmap 通過matrix 矩陣變換生成新的Bitmap
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    val resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    bitmap.recycle()
    return resizedBitmap
}

/**
 * 進行尺寸及質量的壓縮
 * @param path 圖片的路徑
 */
fun getCompressFile(path: String?): File? {
    if (path != null) {

        val image = File(path)
        val bmOptions: BitmapFactory.Options = BitmapFactory.Options()
        var bitmap: Bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

        //對寬高進行等比縮小 任一邊不超過1024
        resizeBitmap(bitmap, 1024)?.let { resizeImage ->
            //對質量進行一次壓縮
            compressImageToFile(resizeImage, 1024)?.let { compressFile ->
                if (compressFile.exists())
                    return compressFile
            }
        }
    }
    return null
}

fun MutableList<LeagueOdd>.closePlayCate(closePlayCateEvent: ClosePlayCateEvent) {
    forEach { leagueOdd ->
        leagueOdd.matchOdds.forEach { matchOdd ->
            matchOdd.oddsMap?.forEach { map ->
                if (map.key == closePlayCateEvent.playCateCode) {
                    map.value?.forEach { odd ->
                        odd?.status = BetStatus.DEACTIVATED.code
                    }
                }
            }
        }
    }
}

/**
 * 判斷當前是否為多站點平台
 */
fun isMultipleSitePlat(): Boolean {
    val appName = MultiLanguagesApplication.stringOf(R.string.app_name)
    return appName == "ONbet" || appName == "BET88" || appName == "OKbet9"
}

fun isForQA(): Boolean = BuildConfig.FLAVOR == "forqa"

/**
 * 判斷當前是否為OKBET平台
 */
fun isOKPlat(): Boolean = MultiLanguagesApplication.stringOf(R.string.app_name).equals("OKBET", true)

fun isUAT(): Boolean = BuildConfig.FLAVOR == "phuat"

/**
 * 解析以下报错，不能用lambda
 *  Cannot add the same observer with different lifecycles
 */
//fun <T> LiveData<T>.observe(@NonNull owner: LifecycleOwner, callback: (T) -> Unit) {
//    this.observe(owner, object : Observer<T> {
//        override fun onChanged(t: T) {
//            callback.invoke(t)
//        }
//    })
//}

fun ImageView.setTeamLogo(icon: String?) {
    if (icon.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_team_default)
    } else {
        if (icon.startsWith("http")) {
            load(icon, R.drawable.ic_team_default)
        } else {
            setSvgIcon(icon, R.drawable.ic_team_default)
        }
    }
}

fun ImageView.setLeagueLogo(icon: String?) {
    if (icon.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_league_default)
    } else if (icon.startsWith("<defs>")) { //經測試 <defs> 標籤下 起始 path d 套件無法解析
        setImageResource(R.drawable.ic_league_default)
    } else {
        if (icon.startsWith("http")) {
            load(icon, R.drawable.ic_league_default)
        } else {
            setSvgIcon(icon, R.drawable.ic_league_default)
        }
    }
}

fun MutableMap<String, MutableList<Odd>?>.sortOddsMap(sizeCheck: Int = 3) {
    forEach { (_, value) ->
        when (sizeCheck) {
            3 -> {
                if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                    value?.sortBy { it?.marketSort }
                }
            }

            // 目前僅作用於推薦賽事 (OddData)
            2 -> {
                if (value?.size ?: 0 > 2 && value?.first()?.marketSort != 0) {
                    value?.sortBy { it?.marketSort }
                }
            }
        }
    }
}

fun MutableMap<String, CateDetailData>.sortOddsMapByDetail() {
    forEach { (_, value) ->
        if (value.odds.size > 3 && value.odds.first()?.marketSort != 0 && (value.odds.first()?.odds != value.odds.first()?.malayOdds)) {
            value.odds.sortBy { it?.marketSort }
        }
    }
}

fun Context.copyToClipboard(copyText: String) {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clipData = ClipData.newPlainText(null, copyText)
    clipboard?.setPrimaryClip(clipData)
    ToastUtil.showToastInCenter(this, this.getString(R.string.bet_slip_id_is_copied))
}

fun Activity.startRegister() {
    if (isUAT()) {
        return
    }
    startLogin()
//    this.startActivity(Intent(this,
//        if (isOKPlat())
//            RegisterOkActivity::class.java
//        else
//            RegisterActivity::class.java)
//    )
}

fun Activity.startLogin() {
    this.startActivity(Intent(this, LoginOKActivity::class.java))
}

fun View.refreshMoneyLoading() {
    this.startAnimation(RotateAnimation(0f,
        720f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f).apply {
        duration = 1000
    })
}

// 绑定'联系客服'点击事件
fun View.setServiceClick(fragmentManager: FragmentManager, block: (() -> Unit)? = null) = setOnClickListener {

    block?.invoke()

    val serviceUrl = sConfigData?.customerServiceUrl
    val serviceUrl2 = sConfigData?.customerServiceUrl2
    when {
        !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
            ServiceDialog().show(fragmentManager, null)
        }
        serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                JumpUtil.toExternalWeb(context, serviceUrl2)
        }
        !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                JumpUtil.toExternalWeb(context, serviceUrl)
        }
    }
}


fun View.setBtnEnable(enable: Boolean) {
    this.isEnabled = enable
    this.alpha = if(enable) { 1.0f } else { 0.5f }
}

fun BaseFragment<SportListViewModel>.showErrorMsgDialog(msg: String) {
    val dialog = CustomAlertDialog(requireContext())
    dialog.setTitle(resources.getString(R.string.prompt))
    dialog.setMessage(msg)
    dialog.setTextColor(R.color.color_E44438_e44438)
    dialog.setNegativeButtonText(null)
    dialog.setPositiveClickListener {
        viewModel.resetErrorDialogMsg()
        dialog.dismiss()
        back()
    }
    dialog.setCanceledOnTouchOutside(false)
    dialog.isCancelable = false
    dialog.show(childFragmentManager, null)
}

fun <T> BaseQuickAdapter<T, *>.doOnVisiableRange(block: (Int, T) -> Unit) {
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    val first = layoutManager.findFirstVisibleItemPosition()
    val count = data.size
    if (first < 0 || first >= count) {
        return
    }

    val last = layoutManager.findLastVisibleItemPosition()
    if (last < 0 || last >= count) {
        return
    }

    for (i in first..last) {
        block.invoke(i, getItem(i))
    }
}

fun View.bindExpanedAdapter(adapter: ExpanableOddsAdapter, block: ((Boolean) -> Unit)? = null) {
    setOnClickListener {
        block?.invoke(isSelected)
        val selected = !isSelected
        isSelected = selected
        if (selected) {
            adapter.collapseAll()
            rotationAnimation(180f)
        } else {
            adapter.expandAll()
            rotationAnimation(0f)
        }
    }
}

fun BaseFragment<out MainHomeViewModel>.enterThirdGame(
    result: EnterThirdGameResult,
    firmType: String,
) {
    hideLoading()
    when (result.resultType) {
        EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
            JumpUtil.toThirdGameWeb(this, result.url ?: "", firmType)
        }
        EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
            getString(R.string.prompt),
            result.errorMsg ?: ""
        ) {}
        EnterThirdGameResult.ResultType.NEED_REGISTER -> requireActivity().startRegister()

        EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
            getString(R.string.error),
            result.errorMsg ?: ""
        ) {}
        EnterThirdGameResult.ResultType.NONE -> {
        }
    }
    if (result.resultType != EnterThirdGameResult.ResultType.NONE)
        viewModel.clearThirdGame()
}

// 设置优惠活动点击事件
fun View.bindPromoClick(click: (() -> Unit)? = null) = setOnClickListener {

    when (UserInfoRepository.userInfo.value?.testFlag) {
        TestFlag.NORMAL.index, TestFlag.TEST.index -> {
            JumpUtil.toInternalWeb(
                context,
                Constants.getPromotionUrl(
                    LoginRepository.token,
                    LanguageManager.getSelectLanguage(context)
                ),
                context.getString(R.string.promotion)
            )
        }
        else -> { // TODO 20220108 沒有遊客的話，要確認一下文案是否正確 by Hewie
            ToastUtil.showToastInCenter(context, context.getString(R.string.message_guest_no_permission))
        }
    }
}

