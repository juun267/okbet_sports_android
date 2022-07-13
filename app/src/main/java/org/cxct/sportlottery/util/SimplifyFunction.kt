package org.cxct.sportlottery.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.*
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
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.component.StatusSpinnerAdapter
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.outright.OutrightLeagueOddAdapter
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.widget.FakeBoldSpan
import org.cxct.sportlottery.widget.boundsEditText.TextFieldBoxes
import org.json.JSONArray
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
    visibility = if (isCreditSystem()) View.GONE else View.VISIBLE
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
@SuppressLint("ClickableViewAccessibility")
fun View.setSpinnerView(
    editText: EditText,
    textFieldBoxes: TextFieldBoxes,
    spinnerList: List<StatusSheetData>,
    touchListener: () -> Unit,
    itemSelectedListener: (data: StatusSheetData?) -> Unit,
    popupWindowDismissListener: () -> Unit
) {
    val spinnerAdapter: StatusSpinnerAdapter?

    var selectItem: StatusSheetData?
    var mListPop = ListPopupWindow(context)

    setOnClickListener {

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
                    when (sConfigData?.handicapShow?.split(",")?.first { type -> type.isNotEmpty() }) {
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
            OddsType.values().firstOrNull { oddsType -> oddsType.code == getDefaultHandicapType().name }
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
        OddsType.values().firstOrNull { oddsType -> oddsType.code == getDefaultHandicapType().name }
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

    val path = MultiLanguagesApplication.appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
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