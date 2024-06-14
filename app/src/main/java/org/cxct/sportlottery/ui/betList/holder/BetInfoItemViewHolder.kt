package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ContentBetInfoItemV32Binding as ItemBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.view.BetListPopupWindow
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getNameMap
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

class BetInfoItemViewHolder(
    parent: ViewGroup,
    private val contentView: ItemBinding = ItemBinding.inflate(LayoutInflater.from(parent.context))
) : BetInfoChangeViewHolder(contentView.root) {
    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var inputWinMaxMoney: Double = 0.0
    private var inputWinMinMoney: Double = 0.0
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private val isLogin: Boolean
        get() = LoginRepository.isLogin.value == true

    private var isTouched: Boolean = false
    fun bind(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        itemCount: Int,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        position: Int,
        userMoney: Double,
        userLogin: Boolean,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        //設置投注限額
        setupInputLimit(itemData)
        val odds = getOddsAndSaveRealAmount(itemData, currentOddsType)
        //設置可贏限額
        inputWinMaxMoney = inputMaxMoney * odds
        inputWinMinMoney = inputMinMoney * odds
//            Timber.e("inputMaxMoney: $inputMaxMoney")
//            Timber.e("inputWinMaxMoney: $inputWinMaxMoney")

        contentView.apply {
            //region 20220714 投注單版面調整
//            GameType.getGameType(itemData.matchOdd.gameType)?.let {
//                ivSportLogo.setImageResource(GameType.getBetListGameTypeIcon(it))
//            }
            rlLeftContent.setOnClickListener {
//                slideLayout.open()
                slideLayout.smoothExpand()
            }

            //不支援串關
            //僅有串關的單注才會出現此提示
            //补充场景，当串关购物车里面只有一个冠军注单当时候，也要显示不可用
            val cannotParlay =
                adapterBetType == BetListRefactorAdapter.BetRvType.PARLAY_SINGLE && itemData.pointMarked && (betListSize > 1 || (betListSize==1 && itemData.matchType==MatchType.OUTRIGHT))
            if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                when (adapterBetType) {
                    BetListRefactorAdapter.BetRvType.SINGLE -> {
                        setupContainerUI(isVisible = true, isLock = false, cannotParlay = false)
                        viewDivider.gone()
                        llViewContent.gone()
                        tvClose.gone()
                    }

                    else -> {
                        tvClose.visible()
                        setupContainerUI(isVisible = false, isLock = false, cannotParlay)
                    }
                }
            } else {
                setupContainerUI(isVisible = false, isLock = true, cannotParlay)
            }
            Timber.d("isOnlyEUType:${itemData.matchOdd.isOnlyEUType}")
            setupBetAmountInput(
                itemData,
                if (itemData.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType,
                onItemClickListener,
                betListSize,
                position,
                adapterBetType
            )
            setupDeleteButton(itemData, itemCount, onItemClickListener)
        }
    }


    private fun setupContainerUI(isVisible: Boolean, isLock: Boolean, cannotParlay: Boolean) {
        contentView.apply {
            //串关隐藏键盘
            llKeyboardContainer.isVisible = isVisible
            llContainer.isVisible = isVisible

            //侧滑删除提示区域
            slideLayout.isVisible = cannotParlay || isLock
            llViewContent.isVisible = slideLayout.isVisible
            viewDivider.isVisible = !slideLayout.isVisible

            //不支持串关
            if (cannotParlay) {
                tvTipsContent.text = LocalUtils.getString(R.string.bet_list_cant_parlay_warn)
            }

            //盤口關閉的提示
            if (isLock) {
                tvTipsContent.text = LocalUtils.getString(R.string.bet_info_bet_lock_hint)
//                if (cannotParlay) LocalUtils.getString(R.string.bet_info_no_parlay_hint)
            }
        }
    }

    private fun setupInputLimit(itemData: BetInfoListData) {
        val maxBet = itemData.parlayOdds?.max ?: 0
        //未登录的情况下，最大限额为7个9
        inputMaxMoney = if (mUserLogin) maxBet.toDouble() else 9999999.toDouble()
        val minBet = itemData.parlayOdds?.min ?: 0
        inputMinMoney = minBet.toDouble()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBetAmountInput(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        position: Int,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {

        val update = {
            if (itemData.input==null){
                tvCanWin.text = "${root.context.getString(R.string.bet_win)}: --"
            }else {
                //更新可贏額
                var win = itemData.betAmount * getOddsAndSaveRealAmount(
                    itemData, currentOddsType
                )

                val strTvCanWin =
                    "${root.context.getString(R.string.bet_win)}：${sConfigData?.systemCurrencySign} ${
                        TextUtil.formatInputMoney(win)
                    }"
                val canWinSpannable = SpannableString(strTvCanWin)
                canWinSpannable.setSpan(
                    ForegroundColorSpan(root.context.getColor(R.color.color_E23434)),
                    "${LocalUtils.getString(R.string.bet_win)}：".length,
                    strTvCanWin.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                canWinSpannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    "${LocalUtils.getString(R.string.bet_win)}：".length,
                    strTvCanWin.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tvCanWin.text = canWinSpannable
            }
        }


        //移除TextChangedListener
        etBet.etBetParlay.apply {
            if (tag is TextWatcher) {
                removeTextChangedListener(tag as TextWatcher)
            }
            onFocusChangeListener = null
            filters = arrayOf(MoneyInputFilter())
        }


        //設定editText內容
        etBet.etBetParlay.apply {
            //金额只默认填充一次，输入之后就不在默认填充
            if (isTouched) {
                return@apply
            }
//            if (itemData.input == null) {
//                val minBet = itemData.parlayOdds?.min ?: 0
//                if (isLogin) {
//                    if (minBet > mUserMoney) {
//                        itemData.input = mUserMoney.toString()
//                    } else {
//                        itemData.input = minBet.toString()
//                    }
//                } else {
//                    itemData.input = minBet.toString()
//                }
//            }
            itemData.inputBetAmountStr = itemData.input
            itemData.betAmount = itemData.input?.toDouble()?:0.0
            setText(if(itemData.input.isNullOrEmpty()) "" else itemData.inputBetAmountStr) //OKF-1558 [安卓]最低投注金额默认不要显示，包括单关，串关
            update()
            setSelection(text.length)
        }
        update()
        checkBetLimit(itemData)
        setupOddInfo(
            itemData, currentOddsType, betListSize, onItemClickListener, adapterBetType
        )

        layoutKeyBoard.setUserMoney(mUserMoney)
        if (itemData.isInputWin) {
            layoutKeyBoard.setupMaxBetMoney(inputWinMaxMoney)
        } else {
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
        }

        val tw: TextWatcher?
        tw = object : TextWatcher {
            override fun afterTextChanged(it: Editable?) {
                isTouched = true
                Timber.d("textChange:${it.toString()}")
                if (it.isNullOrEmpty()) {
                    itemData.betAmount = 0.00
                    itemData.inputBetAmountStr = ""
                    itemData.input = null
                    itemData.realAmount = 0.00
                    //更新可贏額
//                    if (itemData.isInputBet) {
                    tvCanWin.text = "${root.context.getString(R.string.bet_win)}: --"
//                    }
                } else {
                    val quota = it.toString().trim().toDoubleS()
                    itemData.betAmount = quota
                    itemData.inputBetAmountStr = it.toString()
                    itemData.input = it.toString()
                    val max = MAX_BET_VALUE
//                    val max = inputMaxMoney.coerceAtMost(quota.coerceAtLeast(userBalance()))
                    Timber.d("quota:$quota max:$max")
                    if (quota > max) {
                        etBet.etBetParlay.apply {
                            setText(TextUtil.formatInputMoney(max))
                            setSelection(text.length)
                        }
                        return
                    }
                    update()
                }
                checkBetLimit(itemData)
                onItemClickListener.refreshBetInfoTotal()
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
            }
        }

        etBet.etBetParlay.addTextChangedListener(tw)
        etBet.tag = tw
        etBet.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    etBet.isFocusable = true
                    onItemClickListener.onHideKeyBoard()
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    layoutKeyBoard.showKeyboard(
                        etBet.etBetParlay, position
                    )
                    onItemClickListener.onShowKeyboard(position)
                }
            }
            false
        }
        //單筆注單展開時，預設開啟輸入本金的鍵盤
        if (betListSize == 1) {
            etBet.requestFocus()
            itemData.isInputBet = false
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
            if (adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE) {
                layoutKeyBoard.showKeyboard(
                    etBet.etBetParlay, position
                )
            }
        }
        etBet.setOnFocusChangeListener { _, hasFocus ->
//                    if (!hasFocus) layoutKeyBoard?.hideKeyboard() //兩個輸入匡會互搶focus不能這樣關閉鍵盤
            itemData.isInputBet = hasFocus
            if (hasFocus) {
                etBet.etBetParlay.setSelection(etBet.etBetParlay.text.length)
            }
            setEtBackground(itemData)
        }
        clItemBackground.setOnClickListener {
//            onItemClickListener.onHideKeyBoard()
            clItemBackground.clearFocus()
        }
    }

    var oddsId = ""
    var oldOdds = ""
    var handler = Handler(Looper.getMainLooper())
    private val totalAnimationDuration = 3000L //動畫總共呈現時間
    private val totalAnimationTipsDur = 5000L //
    private val animationDuration = 750L //單次動畫持續時間
    private val delayResetTime = totalAnimationDuration - animationDuration * 2

    var repeatCount = 0
    private fun setAnimation(
        ivArrow: ImageView,
        tvOdds: TextView,
        isUp: Boolean,
        onItemClickListener: OnItemClickListener
    ) {
        if (repeatCount == 0) {
            handler.removeCallbacksAndMessages(null)
            resetOddsUI()
        }
        val anim = if (isUp) R.anim.arrow_up else R.anim.arrow_down
        val animation = AnimationUtils.loadAnimation(ivArrow.context, anim)
        animation.duration = animationDuration
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                onItemClickListener.onOddChangeStartListener(isUp)
                if (isUp) {
                    ivArrow.setImageResource(R.drawable.icon_cart_odds_up)
                    tvOdds.setTextColor(
                        ContextCompat.getColor(
                            tvOdds.context, R.color.color_1CD219
                        )
                    )
                } else {
                    ivArrow.setImageResource(R.drawable.icon_cart_odds_down)
                    tvOdds.setTextColor(
                        ContextCompat.getColor(
                            tvOdds.context, R.color.color_FF2E00
                        )
                    )
                }
                ivArrow.isVisible = true
                repeatCount += 1
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (repeatCount == 2) {
                    repeatCount = 0
                    handler.postDelayed({
                        onItemClickListener.onOddChangeEndListener()
                        resetOddsUI()
                    }, delayResetTime)
                } else {
                    setAnimation(ivArrow, tvOdds, isUp, onItemClickListener)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        ivArrow.startAnimation(animation)
    }

    private fun resetOddsUI() {
        contentView.apply {
            ivOddsArrow.clearAnimation()
            ivOddsArrow.isVisible = false
            tvOdds.setTextColor(ContextCompat.getColor(root.context, R.color.color_025BE8))
        }
    }

    private fun setupOddInfo(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        betListSize: Int,
        onItemClickListener: OnItemClickListener,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {

        isHandicapShowSetup()

        if (oddsId == itemData.matchOdd.oddsId && itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != "" && oldOdds != TextUtil.formatForOdd(
                getOdds(itemData.matchOdd, currentOddsType)
            )
        ) {
            //賠率變動更新箭頭和文字色碼
            repeatCount = 0
            if (itemData.matchOdd.oddState == OddState.LARGER.state) {
                setAnimation(ivOddsArrow, tvOdds, true, onItemClickListener)
            } else if (itemData.matchOdd.oddState == OddState.SMALLER.state) {
                setAnimation(ivOddsArrow, tvOdds, false, onItemClickListener)
            }

//            handler.postDelayed({
////                oddsChangeLayout.flIncludeBetTipsSingle?.visibility = View.GONE
//            }, totalAnimationDuration)
        }
        val spread: String =
            if (itemData.matchOdd.spread.isEmpty() || !PlayCate.needShowSpread(itemData.matchOdd.playCode) || itemData.matchType == MatchType.OUTRIGHT) {
                ""
            } else {
                itemData.matchOdd.spread
            }

        tvOddsContent.text = itemData.matchOdd.playName
        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != TextUtil.formatForOdd(
                getOdds(itemData.matchOdd, currentOddsType)
            )
        ) {
            oddsId = itemData.matchOdd.oddsId
            oldOdds = TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
        }
        val tvOdd = when (itemData.matchOdd.playCode) {
            PlayCate.LCS.value -> {
                "@" + TextUtil.formatForOddPercentage(
                    getOdds(
                        itemData.matchOdd,
                        currentOddsType,
                        adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
                    ) - 1
                )
            }
            else -> {
                "@" + TextUtil.formatForOdd(
                    getOdds(
                        itemData.matchOdd,
                        currentOddsType,
                        adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
                    )
                )
            }
        }

        tvOdds.text = if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) tvOdd else {
            resetOddsUI()
            tvOdd
        }

        //特別處理playCode為SCO時, 此處不顯示
        if (itemData.matchOdd.extInfo != null && itemData.matchOdd.playCode != PlayCate.SCO.value) {
            tvContent.text = itemData.matchOdd.extInfo + spread
        } else {
            tvContent.text = spread
        }

        oddsContentContainer.setBackgroundResource(R.color.transparent)
        tvOddsContent.setOUStyle(false)
        tvContent.setOUStyle(false)

        if (tvContent.text.isNullOrEmpty()) {
            tvPlaceHolderLine.gone()
//            Timber.d("===> 线隐藏")
        } else {
            tvPlaceHolderLine.visible()
//            Timber.d("===> 线显示")
        }

        //玩法名稱 目前詳細玩法裡面是沒有給betPlayCateNameMap，所以顯示邏輯沿用舊版
        val nameOneLine = { inputStr: String ->
            inputStr.replace("\n", "-")
        }

        val inPlay = System.currentTimeMillis() > (itemData.matchOdd.startTime ?: 0)

        if (inPlay) {
            tvMatchType.visible()
            tvMatchType.text = LocalUtils.getString(R.string.home_tab_in_play) //滚球
            tvMatchType.background =
                AppCompatResources.getDrawable(root.context, R.drawable.bg_match_type_red_circle)
        } else {
            tvMatchType.visible()
            tvMatchType.text = LocalUtils.getString(R.string.home_tab_early) //早盘
            tvMatchType.background =
                AppCompatResources.getDrawable(root.context, R.drawable.bg_match_type_green_circle)
        }

        if (itemData.matchType == MatchType.OUTRIGHT) {
            tvMatchType.gone()
        }
//        tv_match_type.tranByPlayCode(playCode, playCateCode, playCateName, rtScore)
        Timber.d("itemData:${itemData.matchOdd}")
//        Timber.d("betPlayCateNameMap:${itemData.matchOdd.}")

        val tvNameText: String
        if (itemData.betPlayCateNameMap.isNullOrEmpty()) {
            tvNameText = if (inPlay && itemData.matchType != MatchType.OUTRIGHT) {
                if (itemData.matchOdd.playCode.isCornerPlayCate()) {
                    root.context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeCornerKicks.toStringS("0"),
                        itemData.matchOdd.awayCornerKicks.toStringS("0")
                    )
                } else {
                    root.context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeScore.toString(),
                        itemData.matchOdd.awayScore.toString()
                    )
                }

                } else  {
                    if (itemData.matchOdd.playCateName.isEmpty()) {
                        tvName.gone()
                    } else {
                        tvName.visible()
                    }
                    itemData.matchOdd.playCateName
            }
        } else {
            val playCateName = itemData.betPlayCateNameMap?.getNameMap(
                itemData.matchOdd.gameType, itemData.matchOdd.playCode
            )?.get(LanguageManager.getSelectLanguage(root.context).key) ?: ""


            tvNameText = if (inPlay && itemData.matchType != MatchType.OUTRIGHT) {
                if (itemData.matchOdd.playCode.isCornerPlayCate()) {
                    root.context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeCornerKicks.toStringS("0"),
                        itemData.matchOdd.awayCornerKicks.toStringS("0")
                    )
                } else {
                    root.context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeScore.toString(),
                        itemData.matchOdd.awayScore.toString()
                    )
                }

            } else  {
                nameOneLine(playCateName)
            }
        }

        tvName.text = tvNameText

        //設定隊伍名稱, 聯賽名稱, 開賽時間
        when (itemData.matchType) {
            MatchType.OUTRIGHT -> {
//                tvMatch.text = itemData.outrightMatchInfo?.name
                tvMatchHome.text = itemData.outrightMatchInfo?.name
                tvMatchHome.maxWidth = 330.dp
                setViewGone(tvVs, tvMatchAway, tvLeagueName)
//                tvStartTime.isVisible = false
            }

            else -> {
                tvMatchHome.text = itemData.matchOdd.homeName
                tvMatchHome.maxWidth = 160.dp
                tvMatchAway.text = itemData.matchOdd.awayName
                setViewVisible(tvVs, tvMatchAway, tvLeagueName)
                tvLeagueName.text = itemData.matchOdd.leagueName
            }
        }

        val popupWindow = BetListPopupWindow(context = tvMatchAway.context)
        popupWindow.initOperation(tvLeagueName, tvMatchHome, tvMatchAway, tvOddsContent,tvName,tvNameText, itemData)

        tvNameType.text =
            root.context.getString(if (adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE) currentOddsType.res else OddsType.EU.res)

    }

    private fun TextView.setOUStyle(isOUType: Boolean) {
        if (isOUType) {
            setTextColor(ContextCompat.getColor(context, R.color.color_141931_F9F9F9))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        }
    }

    private fun getOddsAndSaveRealAmount(
        itemData: BetInfoListData, currentOddsType: OddsType
    ): Double {
        var odds = 0.0
        var realAmount = itemData.betAmount
        val tempOdds = getOdds(itemData.matchOdd, currentOddsType)
        when (currentOddsType) {
            OddsType.MYS -> {
                if (tempOdds < 0) {
//                    odds = ArithUtil.div(1.0, Math.abs(tempOdds), 2)
                    realAmount = itemData.betAmount * Math.abs(tempOdds)
//                        win = itemData.betAmount
                    odds = 1.0
                } else {
//                        win = itemData.betAmount * getOdds(
//                            itemData.matchOdd,
//                            currentOddsType
//                        )
                    odds = tempOdds
                }

            }

            OddsType.IDN -> {
                if (tempOdds < 0) {
//                    odds = ArithUtil.div(1.0, Math.abs(tempOdds), 2)
                    realAmount = itemData.betAmount * Math.abs(tempOdds)
//                        win = itemData.betAmount
                    odds = 1.0
                } else {
//                        win = itemData.betAmount * getOdds(
//                            itemData.matchOdd,
//                            currentOddsType
//                        )
                    odds = tempOdds
                }
            }

            OddsType.EU -> {
//                    win = itemData.betAmount * (getOdds(
//                        itemData.matchOdd,
//                        currentOddsType
//                    ) - 1)
                odds = tempOdds
            }


            else -> {
//                    win = itemData.betAmount * getOdds(
//                        itemData.matchOdd,
//                        currentOddsType
//                    )
                odds = tempOdds + 1
            }
        }
        itemData.realAmount = realAmount
        odds = ArithUtil.toOddFormat(odds, 2).toDoubleS()
//            Timber.e("odds: $odds")
        return odds
    }

    private fun setEtBackground(itemData: BetInfoListData) {
        contentView.etBet.setBackgroundAndColor(itemData, inputMinMoney, inputMaxMoney)
    }

    private fun checkBetLimit(
        itemData: BetInfoListData
    ) {
//       contentView.apply {
//            val betAmount = itemData.betAmount
//
//            var amountError = if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
//                !itemData.input.isNullOrEmpty()
//            } else {
//                if (betAmount > inputMaxMoney) {
//                    //超過最大限額
//                    true
//                } else {
//                    betAmount != 0.0 && betAmount < inputMinMoney
//                }
//            }
//            if (itemData.input.isNullOrEmpty()) {
//                amountError = true
//            }
//
//            Timber.d("用户余额:$mUserMoney")
//            val balanceError = betAmount != 0.0 && betAmount > mUserMoney
//            itemData.amountError = balanceError || amountError
//        }
        itemData.amountError = itemData.input.isNullOrEmpty()&&itemData.betAmount == 0.0
        setEtBackground(itemData)
    }

    private fun setupDeleteButton(
        itemData: BetInfoListData, itemCount: Int, onItemClickListener: OnItemClickListener
    ) {
        contentView.tvClose.setOnClickListener {
            onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
        }
        contentView.btnDelete.setOnClickListener {
            contentView.slideLayout.quickClose()
            onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
        }
    }
}






