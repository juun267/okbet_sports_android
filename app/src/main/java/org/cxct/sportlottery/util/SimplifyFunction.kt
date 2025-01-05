package org.cxct.sportlottery.util

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.HideReturnsTransformationMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ViewPaymentMaintenanceBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.money.config.MoneyRechCfg
import org.cxct.sportlottery.network.money.config.RechCfg
import org.cxct.sportlottery.network.myfavorite.MyFavoriteNotify
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.detail.CateDetailData
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.common.dialog.ServiceDialog
import org.cxct.sportlottery.ui.login.CaptchaDialog
import org.cxct.sportlottery.ui.login.VerifyCodeDialog
import org.cxct.sportlottery.ui.login.VerifyCallback
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.money.withdraw.WithdrawActivity
import org.cxct.sportlottery.ui.profileCenter.identity.*
import org.cxct.sportlottery.ui.profileCenter.identity.handheld.VerifyNotFullyActivity
import org.cxct.sportlottery.ui.profileCenter.identity.liveness.LivenessStartActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SvgUtil.setSvgIcon
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.view.boundsEditText.AsteriskPasswordTransformationMethod
import org.cxct.sportlottery.view.boundsEditText.LoginFormFieldView
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.cxct.sportlottery.view.dialog.ToMayaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun AppBarLayout.expand(animate: Boolean) {
    val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior
    if (behavior is AppBarLayout.Behavior) {
        val topAndBottomOffset = behavior.topAndBottomOffset
        if (topAndBottomOffset != 0) {
            behavior.topAndBottomOffset = 0
            setExpanded(true, animate)
        }
    }
}

fun RecyclerView.setupBackTop(targetView: View,
                              offset: Int,
                              tabCode: String? = null,
                              onBackTop: (() -> Boolean)? = null ) {

    val b = tabCode == MatchType.END_SCORE.postValue
    var targetWidth = 0f
    var animaIdle = true
    val animEndCall: () -> Unit = { animaIdle = true }
    val hideRunnable = {
        animaIdle = false
        targetView.translationXAnimation(targetWidth, animEndCall)
    }

    targetView.setOnClickListener {
        hideRunnable.invoke()
        if (onBackTop?.invoke() != true) {
            smoothScrollToPosition(0)
        }
    }

    targetView.post {
        targetWidth = targetView.measuredWidth.toFloat()
        if (targetView.translationX != targetWidth) {
            targetView.translationX = targetWidth
        }
    }

    addOnScrollListener(object : OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (!animaIdle) {
                return
            }
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                //轻轻滑动只会触发dragging 不会触发idle
                if (getScrollYDistance()) {
                    //在最顶端
                    if (targetView.translationX != targetWidth) {
                        hideRunnable.invoke()
                    }
                    return
                }
            }

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //如果是篮球末位比分,需要特殊处理
                if (b) {
                    if (getScrollYDistance()) {
                        if (targetView.translationX != targetWidth) {
                            hideRunnable.invoke()
                            return
                        }
                    } else {
                        animaIdle = false
                        targetView.translationXAnimation(0f, animEndCall)
                        return
                    }
                } else {
                    val isBigger = computeVerticalScrollOffset().dp > offset
                    if (isBigger) {
                        if (targetView.translationX != 0f) {
                            animaIdle = false
                            targetView.translationXAnimation(0f, animEndCall)
                        }
                    }else{
                        if (targetView.translationX!=targetWidth){
                            hideRunnable.invoke()
                        }
                    }
                }
            }
        }
    })
}

fun RecyclerView.getScrollYDistance(): Boolean {
    val layoutManager = if (layoutManager is GridLayoutManager) {
        layoutManager as GridLayoutManager
    } else {
        layoutManager as LinearLayoutManager
    }

    val position = layoutManager.findFirstVisibleItemPosition()
    val firstVisibleChildView = layoutManager.findViewByPosition(position)
    return firstVisibleChildView?.top == 0
}


// 监听NestedScrollView滑出屏幕距离(offset)显示返回顶部按钮
fun NestedScrollView.setupBackTop(targetView: View, offset: Int, onStopRunnable: Runnable? = null) {

    var targetWidth = 0f
    targetView.setOnClickListener { smoothScrollTo(0, 0) }
    targetView.post {
        targetWidth = targetView.measuredWidth.toFloat()
        if (targetView.translationX != targetWidth) {
            targetView.translationX = targetWidth
        }
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
        onStopRunnable?.let {
            targetView.removeCallbacks(it)
            targetView.postDelayed(it, 80)
        }
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
 * 調整標題文字間距
 * 中文之外無間距
 */
fun TextView.setTitleLetterSpacing() {
    this.letterSpacing = when (LanguageManager.getSelectLanguage(context)) {
        LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> 0.1F
        else -> 0F
    }
}

/**
 * 调整中文文字间距
 * 中文之外無間距
 */
fun TextView.setTitleLetterSpacing2F() {
    this.letterSpacing = when (LanguageManager.getSelectLanguage(context)) {
        LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> 0.2F
        else -> 0F
    }
}


fun loginedRun(context: Context, jumpToLoginPage: Boolean = false, block: () -> Unit): Boolean {
    if (LoginRepository.isLogined()) {
        block.invoke()
        return true
    }
    if (jumpToLoginPage){
        context.startActivity(Intent(context, LoginOKActivity::class.java))
    }else{
        if (context is FragmentActivity){
            context.showLoginSnackbar()
            return false
        }
        context.startActivity(Intent(context, LoginOKActivity::class.java))
    }
    return false
}

private fun snackText(context: Context, text: String): TextView {

    val textView = TextView(context)
    textView.gravity = Gravity.CENTER
    textView.textSize = 14f
    textView.setTextColor(context.getColor(R.color.color_FCFCFC))
    textView.setBackgroundResource(R.color.color_317FFF_0760D4)
    val lp = FrameLayout.LayoutParams(-1, 48.dp)
    val dp8 = 8.dp
    lp.leftMargin = dp8
    lp.rightMargin = dp8
    lp.topMargin = dp8
    lp.bottomMargin = 60.dp
    textView.text = text
    textView.layoutParams = lp

    return textView
}

fun FragmentActivity.showLoginSnackbar(@StringRes titleResId: Int = R.string.login_notify, @IdRes anchorViewId: Int?=null){
    Snackbar.make(
        findViewById(android.R.id.content),
        getString(R.string.login_notify),
        Snackbar.LENGTH_LONG
    ).apply {
        (this.view as Snackbar.SnackbarLayout).apply {
            findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                visibility = View.INVISIBLE
            }
            background.alpha = 0
            addView(snackText(context, getString(titleResId)), 0)
            setPadding(0, 0, 0, 0)
        }
        anchorViewId?.let {
            setAnchorView(it)
        }
        show()
    }
}
fun FragmentActivity.showFavoriteSnackbar(favoriteText: String){

    val snackbar = Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_LONG)
    (snackbar.view as Snackbar.SnackbarLayout).apply {
        findViewById<TextView>(com.google.android.material.R.id.snackbar_text).visibility = View.INVISIBLE
        background.alpha = 0
        addView(snackText(context, favoriteText), 0)
        setPadding(0, 0, 0, 0)
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

fun View.setVisibilityByMarketSwitch() {
    visibility = if (getMarketSwitch()) View.GONE else View.VISIBLE
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

fun String.isCornerPlayCate(): Boolean = contains("CORNER-")

/**
 * 判斷盤口啟用參數是否有配置
 * @return true: 有配置, false: 沒有配置(為空或null)
 */
fun isHandicapShowSetup(): Boolean {
    return sConfigData?.handicapShow?.isEmpty() == false
}


fun isOddsTypeEnable(handicapTypeCode: String): Boolean {
    return !isHandicapShowSetup() || sConfigData?.handicapShow?.contains(handicapTypeCode) == true
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
 fun compressImageToFile(image: Bitmap, sizeLimit: Int): File? {
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
            compressImageToFile(resizeImage, 500)?.let { compressFile ->
                if (compressFile.exists()) return compressFile
            }
        }
    }
    return null
}

fun MutableList<LeagueOdd>.closePlayCate(closePlayCateEvent: FrontWsEvent.ClosePlayCateEvent) {
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
 * 判斷是否為遊客(試玩帳號)
 */
fun isGuest(): Boolean {
    return MultiLanguagesApplication.mInstance.userInfo()?.testFlag == TestFlag.GUEST.index
}

fun isOpenChatRoom(): Boolean = sConfigData?.chatOpen.isStatusOpen()

fun isGooglePlayVersion() = BuildConfig.FLAVOR == "google"

fun getMarketSwitch() = KvUtils.decodeBoolean(KvUtils.MARKET_SWITCH)

fun ImageView.setTeamLogo(icon: String?,@DrawableRes defaultResId: Int=R.drawable.ic_team_default) {
    if (icon.isNullOrEmpty()) {
        setImageResource(defaultResId)
    } else if (icon.startsWith("<defs><path d=")) { //經測試 <defs> 標籤下 起始 path d 套件無法解析
        setImageResource(defaultResId)
    } else if (icon.startsWith("http")) {
        load(icon, defaultResId)
    } else {
        setSvgIcon(icon, defaultResId)
    }
}

fun ImageView.setLeagueLogo(icon: String?,@DrawableRes defaultResId: Int=R.drawable.ic_team_default) {
    if (icon.isNullOrEmpty()) {
        setImageResource(defaultResId)
    } else if (icon.startsWith("<defs><path d=")) { //經測試 <defs> 標籤下 起始 path d 套件無法解析
        setImageResource(defaultResId)
    } else if (icon.startsWith("http")) {
        load(icon, defaultResId)
    } else {
        setSvgIcon(icon,defaultResId)
    }
}

fun MutableMap<String, MutableList<Odd>?>.sortOddsMap(sizeCheck: Int = 0) {
    forEach { (key, value) ->
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
            //維持原邏輯後去擴充
            else -> {
                when {
                    key.contains(PlayCate.HDP.value) || key.contains(PlayCate.OU.value) -> {
                        value?.sortBy { it?.marketSort }
                    }
                }
            }
        }
    }
}

fun MutableMap<String, CateDetailData>.sortOddsMapByDetail() {
    forEach { (_, value) ->
        value.odds.sortWith(compareBy({ it?.marketSort }, { it?.rowSort }))
    }
}

fun Context.copyToClipboard(copyText: String) {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clipData = ClipData.newPlainText(null, copyText)
    clipboard?.setPrimaryClip(clipData)
    ToastUtil.showToastInCenter(this, this.getString(R.string.bet_slip_id_is_copied))
}

fun Context.copyText(copyText: String) {
    val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clipData = ClipData.newPlainText(null, copyText)
    clipboard?.setPrimaryClip(clipData)
}

fun DialogFragment.showAllowingStateLoss(fragmentManager: FragmentManager, tag: String? = null) {
    fragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss()
}

fun Context.startLogin() {
    this.startActivity(Intent(this, LoginOKActivity::class.java))
}

fun View.refreshMoneyLoading() {
    this.startAnimation(RotateAnimation(
        0f, 720f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 1000
    })
}

// 绑定'联系客服'点击事件
fun View.setServiceClick(fragmentManager: FragmentManager, block: (() -> Unit)? = null) {
    setOnClickListener (serviceClickListener(fragmentManager, block))
}

fun serviceClickListener(fragmentManager: FragmentManager, block: (() -> Unit)? = null) : View.OnClickListener {
    return View.OnClickListener {
        block?.invoke()
        serviceEvent(it.context, fragmentManager)
    }
}

fun serviceEvent(context: Context, fragmentManager: FragmentManager) {
    val serviceUrl = sConfigData?.customerServiceUrl
    val serviceUrl2 = sConfigData?.customerServiceUrl2
    when {
        !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
            ServiceDialog().show(fragmentManager, ServiceDialog::class.java.simpleName)
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
    this.alpha = if (enable) {
        1.0f
    } else {
        0.5f
    }
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

fun View.bindExpanedAdapter(adapter: ExpanableOddsAdapter<*>, block: ((Boolean) -> Unit)? = null) {
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


fun String.formatHTML(): String {
    val head =
        "<head>" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " + "<style>img{max-width: 100%; width:auto; height:auto!important;}</style>" + "</head>"
    return "<html>$head<body>$this</body></html>"
}
fun setExpandArrow(ivArrow: ImageView, isExpanded: Boolean,esportTheme: Boolean = false) {
    if (isExpanded) {
        ivArrow.rotation = 0f
        ivArrow.setImageResource(if(esportTheme) R.drawable.ic_filter_arrow_up_es else R.drawable.ic_filter_arrow_up)
    } else {
        ivArrow.rotation = 180f
        ivArrow.setImageResource(R.drawable.ic_filter_arrow_up2)
    }
}

fun View.setArrowSpin(isExpanded: Boolean, isAnimate: Boolean, angle: Float = 180f, onRotateEnd: (() -> Unit)?= null) {
    var rotation = angle
    if (isExpanded) {
        rotation = 0f
    }

    if (isAnimate) {
        rotationAnimation(rotation, onEnd = onRotateEnd)
    } else {
        this.rotation = rotation
    }
}

fun Context.dividerView(
    @ColorRes color: Int, width: Int = 0.5f.dp, isVertical: Boolean = true, margins: Int = 0,
): View {
    return View(this).apply {
        setBackgroundResource(color)
        if (isVertical) {
            layoutParams = ViewGroup.MarginLayoutParams(-1, width).apply {
                leftMargin = margins
                rightMargin = margins
            }
        } else {
            layoutParams = ViewGroup.MarginLayoutParams(width, -1).apply {
                leftMargin = margins
                rightMargin = margins
            }
        }
    }
}
 fun setupSummary(tvSummary: TextView) {
    sConfigData?.imageList?.filter {
                it.imageType == ImageType.LOGIN_SUMMARY
                && it.lang == LanguageManager.getSelectLanguage(tvSummary.context).key
                && !it.imageText1.isNullOrEmpty()
                && !getMarketSwitch() }?.sortedByDescending { it.imageSort }?.firstOrNull()?.imageText1.let {
        tvSummary.apply {
            isVisible = !it.isNullOrEmpty()
            text = Html.fromHtml(it?:"")
        }
    }
}

/**
 * 设置充值提款渠道的维护状态
 */
fun setupMoneyCfgMaintanince(rechfg: RechCfg, submitBtn: Button, binding: ViewPaymentMaintenanceBinding) {
    if (rechfg.open== MoneyRechCfg.Switch.OPEN.code){
        submitBtn.visible()
        binding.root.gone()
    }else if(rechfg.open== MoneyRechCfg.Switch.MAINTAINCE.code){
        submitBtn.gone()
        binding.root.visible()
        binding.linMaintenanceTip.isVisible = !rechfg.frontDeskRemark.isNullOrEmpty()
        binding.tvTipsContent.text = rechfg.frontDeskRemark
    }
}

fun resetInputTransformationMethod(fieldBox: LoginFormFieldView, editText: EditText) {
    if (fieldBox.endIconResourceId == R.drawable.ic_eye_open) {
        editText.transformationMethod = AsteriskPasswordTransformationMethod()
        fieldBox.setEndIcon(R.drawable.ic_eye_close)
    } else {
        fieldBox.setEndIcon(R.drawable.ic_eye_open)
        editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
    }

    fieldBox.hasFocus = true
    editText.setSelection(editText.text.toString().length)
}

fun TextFormFieldBoxes.setTransformationMethodEvent(editText: EditText) {
    endIconImageButton.setOnClickListener {
        if (endIconResourceId == R.drawable.ic_eye_open) {
            editText.transformationMethod = AsteriskPasswordTransformationMethod()
            setEndIcon(R.drawable.ic_eye_close)
        } else {
            setEndIcon(R.drawable.ic_eye_open)
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        setHasFocus(true)
        editText.setSelection(editText.text.toString().length)
    }
}

/**
 * 获取内存权限，兼容android33 部分手机
 */
fun RxPermissions.requestWriteStorageWithApi33(grantFun: ()->Unit,unGrantFun: ()->Unit){
    this.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .subscribe { aBoolean ->
            if (aBoolean) {
                grantFun()
            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    this.request(Manifest.permission.READ_MEDIA_IMAGES).subscribe {
                        if (it){
                            grantFun()
                        }else{
                            unGrantFun
                        }
                    }
                }else{
                    unGrantFun
                }
            }
        }
}

// 发送邮件
fun toSendEmail(context: Context, emailAddress: String) {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))    // 收件人地址
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject")     // 邮件主题
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "message")        // 邮件内容
        context.startActivity(emailIntent)
    } catch (e: Exception) {
        context.copyText(emailAddress)
        toast("${context.getString(R.string.email_address)}, ${context.getString(R.string.text_money_copy_success)}")
    }

}

fun FragmentActivity.showCaptchaDialog(tag: String? = null) {
    if (this !is VerifyCallback) {
        throw RuntimeException(" ${this.javaClass.name} 为实现接口: VerifyCallback")
    }
    showCaptchaDialog(supportFragmentManager, tag)
}

fun Fragment.showCaptchaDialog(tag: String? = null) {
    if (this !is VerifyCallback) {
        throw RuntimeException(" ${this.javaClass.name} 为实现接口: VerifyCallback")
    }
    showCaptchaDialog(childFragmentManager, tag)
}

private fun showCaptchaDialog(fragmentManager: FragmentManager, tag: String? = null) {
    if (sConfigData?.captchaType == 1){
        CaptchaDialog().show(fragmentManager, tag)
    }else{
        VerifyCodeDialog().show(fragmentManager, tag)
    }
}


fun Context.getIconSelector(selected: Int, unSelected: Int): Drawable {
    val selectDrawable = ContextCompat.getDrawable(this,selected)
    val unSelecteDrawable = ContextCompat.getDrawable(this,unSelected)
    return DrawableCreator.Builder()
        .setSelectedDrawable(selectDrawable)
        .setUnSelectedDrawable(unSelecteDrawable)
        .setPressedDrawable(selectDrawable)
        .setUnPressedDrawable(unSelecteDrawable)
        .build()
}
fun AppCompatActivity.showBetReceiptDialog(
    betResultData: Receipt?,
    betParlayList: List<ParlayOdd>,
    isMultiBet: Boolean,
    containerId: Int,
) {
    replaceFragment(containerId, BetReceiptFragment.newInstance(betResultData, betParlayList))
}
fun AppCompatActivity.showFavoriteNotify(result: MyFavoriteNotify) {

    val isFavorite = result.isFavorite ?: return

    if (result.type == FavoriteType.LEAGUE) {
        if (isFavorite) {
            showFavoriteSnackbar(getString(R.string.myfavorite_notify_league_add))
        } else {
            showFavoriteSnackbar(getString(R.string.myfavorite_notify_league_remove))
        }
        return
    }

    if (result.type == FavoriteType.MATCH) {
        if (isFavorite) {
            showFavoriteSnackbar(getString(R.string.myfavorite_notify_match_add))
        } else {
            showFavoriteSnackbar(getString(R.string.myfavorite_notify_match_remove))
        }
        return
    }

    if (result.type == FavoriteType.PLAY_CATE) {
        if (isFavorite) {
            showFavoriteSnackbar(getString(R.string.Pinned))
        } else {
            showFavoriteSnackbar(getString(R.string.Unpin))
        }
        return
    }
}
 fun RadioGroup.setSelectorTypeFace(normal: Typeface, checked: Typeface){
    children.forEach {
        (it as? RadioButton)?.apply {
            typeface = if(isChecked) checked else normal
        }
    }
}
fun setupOKPlay(showFunc: (OKGameBean?)->Unit){
    if (StaticData.sbSportOpened()){
        val okPlayBean = OKGamesRepository.okPlayEvent.value
        if (okPlayBean!=null){
            showFunc.invoke(okPlayBean)
            return
        }
    }
    showFunc.invoke(null)
}
fun RadioGroup.setTextBold(){
    children.forEach {
        (it as? RadioButton)?.apply {
            paint.isFakeBoldText = isChecked
        }
    }
}
fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    val lParams = layoutParams as ViewGroup.MarginLayoutParams
    lParams.leftMargin = left
    lParams.topMargin = top
    lParams.rightMargin = right
    lParams.bottomMargin = bottom
}

//是否正在请求充值开关
private var checkRecharge = false
// 首冲活动跳转充值页面
var chargeFromActivity = false

fun BaseActivity<*, *>.jumpToDeposit(action: String, activityCharge: Boolean = false){
    if (LoginRepository.isLogined() && UserInfoRepository.isGlifeAccount() && !glifeUserWithdrawEnable()) {
        ToGcashDialog.newInstance(false).show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToGcashDialog.javaClass.name)
        return
    }
    if (LoginRepository.isLogined() && UserInfoRepository.isMayaAccount() && !mayaUserWithdrawEnable()) {
        ToMayaDialog.newInstance(false).show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToMayaDialog.javaClass.name)
        return
    }
    chargeFromActivity = activityCharge
    val isNeedVerify = UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value && isKYCVerifyRechargeOpen()
    if (isNeedVerify){
        VerifyIdentityDialog().show(supportFragmentManager, null)
    }else {
        if (!checkRecharge) {
            loading()
            viewModel.launch {
                checkRecharge = true
                val result = viewModel.doNetwork(applicationContext) {
                    MoneyRepository.checkRechargeSystem()
                }

                if (result == null || !result.success) {
                    hideLoading()
                    return@launch
                }
                val rechTypesList = result.rechCfg?.rechTypes //玩家層級擁有的充值方式
                val rechCfgsList = result.rechCfg?.rechCfgs  //後台有開的充值方式
                val operation = (rechTypesList?.size ?: 0 > 0) && (rechCfgsList?.size ?: 0 > 0)
                withContext(Dispatchers.Main) {
                    hideLoading()
                    if (operation) {
                        MoneyRechargeActivity.startFrom(this@jumpToDeposit, action)
                    } else {
                        showPromptDialog(
                            getString(R.string.prompt),
                            getString(R.string.message_recharge_maintain)
                        ) {}
                    }
                }
                checkRecharge = false
            }
        }
    }
}
fun FragmentActivity.jumpToWithdraw(){
    if (LoginRepository.isLogined() && UserInfoRepository.isGlifeAccount() && !glifeUserWithdrawEnable()) {
        ToGcashDialog.newInstance(false).show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToGcashDialog.javaClass.name)
        return
    }
    if (LoginRepository.isLogined() && UserInfoRepository.isMayaAccount() && !mayaUserWithdrawEnable()) {
        ToMayaDialog.newInstance(false).show((AppManager.currentActivity() as BaseActivity<*,*>).supportFragmentManager,ToMayaDialog.javaClass.name)
        return
    }
    val userInfo = UserInfoRepository.userInfo.value!!
    //不允许半认证用户冲提款
    if (userInfo.fullVerified !=1
        && userInfo.verified == VerifiedType.PASSED.value
        && (sConfigData?.halfVerifiedCharge==0 || sConfigData?.needFullVerifyToWithdraw==1)){
        startActivity(VerifyNotFullyActivity::class.java) {
             it.putExtra(VerifyNotFullyActivity.TYPE, 1)
        }
       return
    }
    startActivity(WithdrawActivity::class.java)
}
fun FragmentActivity.jumpToKYC(){
    loginedRun(this,true){
        val userInfo =  UserInfoRepository.userInfo.value!!
        if (userInfo.fullVerified==1){
            startActivity<VerifyIdentityActivity>()
        }else{
            when (userInfo.verified) {
                VerifiedType.VERIFYING.value,
                VerifiedType.VERIFIED_WAIT.value,
                VerifiedType.REVERIFYING.value -> {
                    startActivity<VerifyIdentityActivity>()
                }
                VerifiedType.REVERIFIED_NEED.value -> {
                    startActivity<VerifyIdentityActivity>()
                }
                VerifiedType.REJECT.value -> {
                    startActivity<VerifyIdentityActivity>()
                }
                VerifiedType.PASSED.value->{
                    if(sConfigData?.needFullVerifyToWithdraw==1){
                       startActivity<VerifyNotFullyActivity>()
                    }else{
                        startActivity<VerifyIdentityActivity>()
                    }
                }
                else -> {
                    startActivity<VerifyIdentityActivity>()
                }
            }
        }
    }
}

fun FragmentActivity.showDataSourceChangedDialog(event: Event<Boolean>) {
    if (AppManager.currentActivity() != this || event.getContentIfNotHandled() == null) {
        return
    }
    showErrorPromptDialog(
        title = getString(R.string.prompt),
        message = SpannableStringBuilder().append(getString(R.string.message_source_change)),
        hasCancel = false
    ) { }
}
fun JsonObject.appendCaptchaParams(identity: String, validCode: String){
    if (sConfigData?.captchaType ==1){
        addProperty("ticket", identity)
        addProperty("randstr", validCode)
        addProperty("checkValidCodeType", true)
    }else{
        addProperty("validCodeIdentity", identity)
        addProperty("validCode", validCode)
        addProperty("checkValidCodeType", false)
    }
}

fun isHalloweenStyle(): Boolean {
    return true
}
fun String.replaceSpecialChar(char: String):String{
    return replace(char,"")
}

fun Long.isTimeOut(): Boolean {
    return this <= System.currentTimeMillis()
}

fun String.encodeUserName(): String {
    if (this.isEmpty()) return ""
    return this.first() + "***" + this.last()
}
