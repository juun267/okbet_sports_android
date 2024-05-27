package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ContentBetInfoItemV3BaseketballEndingCardBinding as ItemBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetBasketballListAdapter
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.view.BetListPopupWindow
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScoreNameByTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

class BasketballEndingCardViewHolder(
    parent: ViewGroup,
    private val binding: ItemBinding = ItemBinding.inflate(LayoutInflater.from(parent.context)),
) : RecyclerView.ViewHolder(binding.root) {

    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var inputWinMaxMoney: Double = 0.0
    private var inputWinMinMoney: Double = 0.0
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false

    var oddsId = ""
    var oldOdds = ""

    private val isLogin: Boolean
        get() = LoginRepository.isLogin.value == true

    fun bind(
        betList: MutableList<BetInfoListData>?,
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        position: Int,
        userMoney: Double,
        userLogin: Boolean,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        if (betList.isNullOrEmpty()) {
            binding.tvBasketBallEndingScore.setText(R.string.N903)
        } else {
            val matchOdd = betList.first().matchOdd
            binding.tvBasketBallEndingScore.run {
                text = "${context.getString(R.string.N903)}-${matchOdd.playCode.getEndScoreNameByTab(context)}"
            }
        }

        //設置投注限額
        setupInputLimit(itemData)
        binding.layoutKeyBoard.setUserMoney(mUserMoney)
        binding.layoutKeyBoard.setGameType(itemData.matchOdd.playCode)
        if (betList != null) {
            binding.layoutKeyBoard.setBetItemCount(betList.size)
        }
        setupBetAmountInput(
            betList,
            itemData,
            if (itemData.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType,
            onItemClickListener,
            betListSize,
            position,
            adapterBetType
        )

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
        betList: MutableList<BetInfoListData>?,
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        position: Int,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = binding.run {
        fun showTotalStakeWinAmount(bet: Double) {
            val totalBet = ArithUtil.toMoneyFormatFloor(bet * betListSize)
            val totalCanWin = ArithUtil.toMoneyFormatFloor(bet * itemData.matchOdd.odds)
            includeControl.tvCanWinAmount.text = "${totalCanWin}"
        }
        //移除TextChangedListener
        includeControl.etBet.etBetParlay.apply {
            if (tag is TextWatcher) {
                removeTextChangedListener(tag as TextWatcher)
            }
            onFocusChangeListener = null
            filters = arrayOf(MoneyInputFilter())
        }

        val rcvBasketballAdapter = BetBasketballListAdapter(onItemClickListener)
        rcvBasketballScore.adapter = rcvBasketballAdapter
        rcvBasketballScore.setupBackTop(topShadow,20.dp)
        val newList = mutableListOf<BetInfoListData>()
        if (betList != null) {
            newList.addAll(betList)
        }
        newList.sortBy { it.matchOdd.playName.split("-")[1].toInt() }
        newList.sortBy { it.matchOdd.playName.split("-")[0].toInt() }
        newList.add(newList[0].clone())
        rcvBasketballAdapter.setNewInstance(newList)
        rcvBasketballScore.layoutManager = GridLayoutManager(root.context, 5)
        includeControl.tvParlayType.text = Spanny("${root.context.getString(R.string.P204)}*${betList?.size} ")
            .append(tvOdds.text,
                ForegroundColorSpan(ContextCompat.getColor(itemView.context,R.color.color_000000)),
                StyleSpan(Typeface.BOLD))
        setOnClickListeners(rcvBasketballScore, clItemBackground) {
            rcvBasketballAdapter.data.forEach { itemD ->
                itemD.isClickForBasketball = false
            }
            it.clearFocus()
            rcvBasketballAdapter.notifyDataSetChanged()
        }
        //設定editText內容
        includeControl.etBet.etBetParlay.apply {
            if (itemData.input == null) {
                val minBet = itemData.parlayOdds?.min ?: 0
                if (isLogin) {
                    if (minBet > mUserMoney) {
                        itemData.input = mUserMoney.toString()
                    } else {
                        itemData.input = minBet.toString()
                    }
                } else {
                    itemData.input = minBet.toString()
                }
            }
            itemData.inputBetAmountStr = itemData.input
            itemData.betAmount = itemData.input!!.toDouble()
//            setText(itemData.inputBetAmountStr) //OKF-1558 [安卓]最低投注金额默认不要显示，包括单关，串关
            setSelection(text.length)

            //显示总投注
            val bet = itemData.inputBetAmountStr!!.toDouble()
            showTotalStakeWinAmount(bet)
        }
        setEtBackground(itemData)

        setupOddInfo(
            itemData, currentOddsType, onItemClickListener, adapterBetType
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
                    includeControl.tvCanWinAmount.text = ""
                } else {
                    val quota = it.toString().toDoubleS()
                    itemData.betAmount = quota
                    itemData.inputBetAmountStr = it.toString()
                    itemData.input = it.toString()
                    val max = MAX_BET_VALUE
                    if (quota > max) {
                        includeControl.etBet.etBetParlay.apply {
                            setText(TextUtil.formatInputMoney(max))
                            setSelection(text.length)
                        }
                        return
                    }
                    //总投注
                    val bet = it.toString().toDoubleS()
                    showTotalStakeWinAmount(bet)
                }
                setEtBackground(itemData)
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

        includeControl.etBet.etBetParlay.addTextChangedListener(tw)
        includeControl.etBet.etBetParlay.tag = tw
        includeControl.etBet.etBetParlay.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    includeControl.etBet.etBetParlay.isFocusable = true
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    layoutKeyBoard.showKeyboard(
                        includeControl.etBet.etBetParlay, position
                    )
                    onItemClickListener.onShowKeyboard(position)
                }
            }
            false
        }
        //單筆注單展開時，預設開啟輸入本金的鍵盤
        if (betListSize == 1) {
            includeControl.etBet.etBetParlay.requestFocus()
            itemData.isInputBet = true
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
            layoutKeyBoard.showKeyboard(
                includeControl.etBet.etBetParlay, position
            )

        }
        includeControl.etBet.etBetParlay.setOnFocusChangeListener { _, hasFocus ->
            itemData.isInputBet = hasFocus
            if (hasFocus) {
                includeControl.etBet.etBetParlay.setSelection(includeControl.etBet.etBetParlay.text.length)
            }
            setEtBackground(itemData)
        }
    }

    private fun setupOddInfo(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = binding.run {

        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != TextUtil.formatForOdd(
                getOdds(itemData.matchOdd, currentOddsType)
            )
        ) {
            oddsId = itemData.matchOdd.oddsId
            oldOdds = TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
        }
        tvOdds.text  = "@" + TextUtil.formatForOdd(
            getOdds(
                itemData.matchOdd,
                currentOddsType,
                adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
            )
        )
        oddsContentContainer.setBackgroundResource(R.color.transparent)
        //設定隊伍名稱, 聯賽名稱, 開賽時間
        tvMatchHome.text = itemData.matchOdd.homeName
        tvMatchHome.maxWidth = 160.dp
        tvMatchAway.text = itemData.matchOdd.awayName
        setViewVisible(tvVs, tvMatchAway, tvLeagueName)
        tvLeagueName.text = itemData.matchOdd.leagueName?.trim()

        val popupWindow = BetListPopupWindow(context = tvMatchAway.context)
        popupWindow.initOperation(
            tvLeagueName = tvLeagueName,
            tvMatchHome, tvMatchAway, itemData = itemData
        )

    }

    private fun setEtBackground(itemData: BetInfoListData) {
        binding.includeControl.etBet.setBackgroundAndColor(itemData, inputMinMoney, inputMaxMoney, isEndScore = true)
    }

}