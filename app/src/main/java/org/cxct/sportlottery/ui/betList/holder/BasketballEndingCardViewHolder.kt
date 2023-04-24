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
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.common.extentions.setViewGone
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.databinding.ContentBetInfoItemV3BaseketballEndingCardBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.MoneyInputFilter
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import timber.log.Timber

class BasketballEndingCardViewHolder(
    val contentView: ContentBetInfoItemV3BaseketballEndingCardBinding,
    val userBalance: () -> Double,
) : RecyclerView.ViewHolder(contentView.root) {

    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var inputWinMaxMoney: Double = 0.0
    private var inputWinMinMoney: Double = 0.0
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false

    fun bind(
        betList: MutableList<BetInfoListData>?,
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        itemCount: Int,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        mSelectedPosition: Int,
        onSelectedPositionListener: OnSelectedPositionListener,
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

        contentView.apply {
            setupBetAmountInput(
                betList,
                itemData,
                if (itemData.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType,
                onItemClickListener,
                betListSize,
                mSelectedPosition,
                onSelectedPositionListener,
                position,
                adapterBetType
            )
//            setupDeleteButton(itemData, itemCount, onItemClickListener)
        }
    }


    private fun setupInputLimit(itemData: BetInfoListData) {
        val maxBet = itemData.parlayOdds?.max ?: 0
        //未登录的情况下，最大限额为7个9
        inputMaxMoney = if (mUserLogin) maxBet.toDouble() else 9999999.toDouble()
        val minBet = itemData.parlayOdds?.min ?: 0
        inputMinMoney = minBet.toDouble()
    }

    var isSingleBetFirstOpenKeyboard = true

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBetAmountInput(
        betList: MutableList<BetInfoListData>?,
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        mSelectedPosition: Int,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {

        //移除TextChangedListener
        etBet.apply {
            if (tag is TextWatcher) {
                removeTextChangedListener(tag as TextWatcher)
            }
            onFocusChangeListener = null
            filters = arrayOf(MoneyInputFilter())
        }

        Timber.d("itemData:${itemData}")


        val rcvBasketballAdapter = object :
            BaseQuickAdapter<BetInfoListData, BaseViewHolder>(R.layout.item_bet_basketball_ending_cart) {
            override fun convert(holder: BaseViewHolder, item: BetInfoListData) {
                holder.setText(R.id.btnMatchOdds, item.matchOdd.playName)
            }
        }
        rcvBasketballScore.adapter = rcvBasketballAdapter
        rcvBasketballAdapter.setNewInstance(betList)
        rcvBasketballScore.layoutManager = GridLayoutManager(root.context, 5)


        //設定editText內容
        etBet.apply {
            if (itemData.input != null) setText(itemData.inputBetAmountStr) else text.clear()
            setSelection(text.length)
        }
        checkBetLimit(itemData)

        setupOddInfo(
            itemData, currentOddsType, betListSize, onItemClickListener, adapterBetType
        )

        if (itemData.isInputWin) {
            layoutKeyBoard.setupMaxBetMoney(inputWinMaxMoney)
        } else {
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
        }

        val tw: TextWatcher?
        tw = object : TextWatcher {
            override fun afterTextChanged(it: Editable?) {
                Timber.d("textChange:${it.toString()}")
                if (it.isNullOrEmpty()) {
                    itemData.betAmount = 0.000
                    itemData.inputBetAmountStr = ""
                    itemData.input = null

                    itemData.realAmount = 0.0
                    //更新可贏額
                    tvCanWin.text = "${root.context.getString(R.string.bet_win)}: --"
                } else {
                    val quota = it.toString().toDouble()
                    itemData.betAmount = quota
                    itemData.inputBetAmountStr = it.toString()
                    itemData.input = it.toString()
                    val max = inputMaxMoney.coerceAtMost(0.0.coerceAtLeast(userBalance()))
                    if (quota > max) {
                        etBet.apply {
                            setText(TextUtil.formatInputMoney(max))
                            setSelection(text.length)
                        }
                        return
                    }
                    val win = itemData.betAmount * getOddsAndSaveRealAmount(
                        itemData, currentOddsType
                    )
                    //更新可贏額
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

        etBet.addTextChangedListener(tw)
        etBet.tag = tw
        etBet.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    etBet.isFocusable = true
                    onItemClickListener.onHideKeyBoard()
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    layoutKeyBoard.showKeyboard(
                        etBet, position
                    )
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition, BetListRefactorAdapter.BetViewType.SINGLE
                    )
                    onItemClickListener.onShowKeyboard(position)
                }
            }
            false
        }
        //單筆注單展開時，預設開啟輸入本金的鍵盤
        if (betListSize == 1) {
            etBet.requestFocus()
            itemData.isInputBet = true
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
            if (adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE) {
                layoutKeyBoard.showKeyboard(
                    etBet, position
                )
            }
        }
        etBet.setOnFocusChangeListener { _, hasFocus ->
            itemData.isInputBet = hasFocus
            if (hasFocus) {
                etBet.setSelection(etBet.text.length)
            }
            setEtBackground(itemData)
        }
        clItemBackground.setOnClickListener {
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


    private fun setupOddInfo(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        betListSize: Int,
        onItemClickListener: OnItemClickListener,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {

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
        //反波膽顯示 %
        var tvOdd = "@ " + TextUtil.formatForOdd(
            getOdds(
                itemData.matchOdd,
                currentOddsType,
                adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
            )
        )
        if (itemData.matchOdd.playCode == PlayCate.LCS.value) tvOdd =
            "@ " + TextUtil.formatForOddPercentage(
                getOdds(
                    itemData.matchOdd,
                    currentOddsType,
                    adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
                ) - 1
            )

        tvOdds.text = if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) tvOdd else {
            tvOdd
        }

        oddsContentContainer.setBackgroundResource(R.color.transparent)
        tvOddsContent.setOUStyle(false)

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
        val view = View.inflate(tvMatchHome.context, R.layout.popupwindow_tips, null)
        val pop = PopupWindow(tvMatchHome.context).apply {
            contentView = view
            setBackgroundDrawable(null)
            isOutsideTouchable = true
        }
        val textView = view.findViewById<TextView>(R.id.tvContent)
        val imageView = view.findViewById<ImageView>(R.id.ivPopupWindowTipsBg)
        val showPopAsTop: (TextView, String?) -> Unit = { it, it2 ->
            if (pop.isShowing) {
                pop.dismiss()
            }

            it.setTextColor(it.context.getColor(R.color.color_025BE8))
            textView.text = it2
            val xOff: Int
            val yOff = (-50).dp
            if (it == tvMatchAway) {
                xOff = (-20).dp
                imageView.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_right)
            } else {
                xOff = (-5).dp
                imageView.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_left)
            }
            pop.showAsDropDown(it, xOff, yOff)
        }

        setOnClickListener(tvLeagueName, tvMatchHome, tvMatchAway) {
            when (it) {

                tvLeagueName -> {
                    showPopAsTop(tvLeagueName, itemData.matchOdd.leagueName)
                }

                tvMatchHome -> {
                    showPopAsTop(tvMatchHome, itemData.matchOdd.homeName)
                }

                tvMatchAway -> {
                    showPopAsTop(tvMatchAway, itemData.matchOdd.awayName)
                }
            }
        }

        pop.setOnDismissListener {
            tvLeagueName.setTextColor(tvLeagueName.context.getColor(R.color.color_9BB3D9_535D76))
            tvMatchHome.setTextColor(tvLeagueName.context.getColor(R.color.color_A7B2C4))
            tvMatchAway.setTextColor(tvLeagueName.context.getColor(R.color.color_A7B2C4))
        }

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
                odds = (tempOdds - 1)
            }

            else -> {
//                    win = itemData.betAmount * getOdds(
//                        itemData.matchOdd,
//                        currentOddsType
//                    )
                odds = tempOdds
            }
        }
        itemData.realAmount = realAmount
        odds = ArithUtil.toOddFormat(odds, 2).toDouble()
//            Timber.e("odds: $odds")
        return odds
    }

    private fun setEtBackground(itemData: BetInfoListData) {
        contentView.apply {
            if (itemData.amountError) {
                Timber.d("setEtBackground error")
                etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_error)
            } else {
                if (itemData.isInputBet) {
                    Timber.d("setEtBackground itemData.isInputBet true")
                    etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                } else {
                    Timber.d("setEtBackground itemData.isInputBet false")
                    etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                }
            }

            //更新bet editText hint
            val betHint = root.context.getString(
                R.string.hint_bet_limit_range,
                inputMinMoney.toLong().toString(),
                inputMaxMoney.toLong().toString()
            )

            //更新win editText hint
            val winHint = root.context.getString(
                R.string.hint_bet_limit_range,
                inputWinMinMoney.toLong().toString(),
                inputWinMaxMoney.toLong().toString()
            )

            if (LoginRepository.isLogin.value == true) {
                etBet.hint = betHint
            } else {
                etBet.hint = ""
            }
        }
    }

    private fun checkBetLimit(
        itemData: BetInfoListData
    ) {
        contentView.apply {
            val betAmount = itemData.betAmount
            val balanceError: Boolean
            val amountError: Boolean = if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                !itemData.input.isNullOrEmpty()
            } else {
                if (betAmount > inputMaxMoney) {
                    //超過最大限額
                    true
                } else {
                    betAmount != 0.0 && betAmount < inputMinMoney
                }
            }

            Timber.d("用户余额:$mUserMoney")
            if (betAmount != 0.0 && betAmount > mUserMoney) {
                balanceError = true
                View.VISIBLE
            } else {
                balanceError = false
                View.GONE
            }
            Timber.d("balanceError1:${balanceError} amountError:$amountError")
            itemData.amountError = balanceError || amountError
            Timber.d("balanceError2:${itemData.amountError} ")
        }
        setEtBackground(itemData)
    }

//    private fun setupDeleteButton(
//        itemData: BetInfoListData, itemCount: Int, onItemClickListener: OnItemClickListener
//    ) {
////        contentView.tvClose.setOnClickListener {
////            onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
////        }
////        contentView.btnDelete.setOnClickListener {
////            contentView.slideLayout.quickClose()
////            onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
////        }
//    }

}