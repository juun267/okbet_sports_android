package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemBetListBatchControlV3Binding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.util.MoneyInputFilter
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import java.lang.Math.abs

//填充單注
class BatchSingleInMoreOptionViewHolder(
    private val contentView: ItemBetListBatchControlV3Binding,
    private val keyboardView: KeyboardView
) : BatchParlayViewHolder(contentView.root, keyboardView) {
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private var mHasBetClosedForSingle: Boolean = false
    private var maxBet: Double = 0.0
    private var minBet: Double = 0.0

    fun bind(
        itemData: ParlayOdd?,
        betList: MutableList<BetInfoListData>,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        notifyAllBet: () -> Unit,
        mSelectedPosition: Int,
        mBetView: BetListRefactorAdapter.BetViewType,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int,
        hasBetClosedForSingle: Boolean,
        userMoney: Double,
        userLogin: Boolean,
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        mHasBetClosedForSingle = hasBetClosedForSingle
        maxBet = getMaxOrMinAmount(true, betList)
        minBet = getMaxOrMinAmount(false, betList)

        if (itemData != null) {
            setupSingleItem(
                betList,
                itemData,
                currentOddsType,
                onItemClickListener,
                notifyAllBet,
                mSelectedPosition,
                mBetView,
                onSelectedPositionListener,
                position
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSingleItem(
        betList: MutableList<BetInfoListData>,
        itemData: ParlayOdd,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        notifyAllBet: () -> Unit,
        mSelectedPosition: Int,
        mBetView: BetListRefactorAdapter.BetViewType,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int
    ) {
        contentView.apply {

            etBetSingle.apply {
                if (tag is TextWatcher) {
                    removeTextChangedListener(tag as TextWatcher)
                }
                onFocusChangeListener = null
                filters = arrayOf(MoneyInputFilter())
            }
            val singleType =
                "${contentView.root.context.getString(R.string.bet_list_single)}*${betList.size}"
            tvSingleType.text = singleType

            val initValue = if (itemData.singleInput != null) itemData.allSingleInput else ""



            etBetSingle.apply {
                if (!initValue.isNullOrEmpty()) setText(initValue) else text.clear()
//                setSelection(text.length)
                setEtBetSingleBackground(itemData)

                /* set listener */
                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        val inputValue: Double
                        if (it.isNullOrEmpty()) {
                            inputValue = 0.0
                            itemData.allSingleInput = null
                            itemData.singleInput = null
                            //更新可贏額
//                                if (itemData.isInputBet) itemView.et_win_single.text.clear()
                        } else {
                            inputValue = it.toString().toDouble()
                            itemData.allSingleInput = it.toString()
                            itemData.singleInput = it.toString()

                            val allWinnableAmount =
                                getAllSingleWinnableAmount(inputValue, currentOddsType, betList)

                            val maxAmount = getMaxOrMinAmount(isGetMax = true, betList)

                            maxAmount.let { max ->
                                if (inputValue > max) {
                                    etBetSingle.apply {
                                        setText(TextUtil.formatInputMoney(max))
                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }

                            //更新可贏額
//                                if (allWinnableAmount > 0) {
//                                    itemView.et_win_single.setText(TextUtil.formatInputMoney(allWinnableAmount))
//                                }
                        }
                        setEtBetSingleBackground(itemData)

                        betList.forEachIndexed { _, data ->
                            if (data.matchOdd.status != BetStatus.ACTIVATED.code) return@forEachIndexed

                            checkBetLimitSingle(data, inputValue)

                            if (data.parlayOdds?.max == null || inputValue < (data.parlayOdds?.max
                                    ?: 0)
                            ) {
                                data.betAmount = inputValue
                                data.inputBetAmountStr = it.toString()
                                data.input = it.toString()
                            } else {
                                data.betAmount = (data.parlayOdds?.max ?: 0).toDouble()
                                data.inputBetAmountStr = (data.parlayOdds?.max ?: 0).toString()
                                data.input = (data.parlayOdds?.max ?: 0).toString()
                            }

                            if (itemData.allSingleInput == null) data.input = null

                            var dataOddsType = currentOddsType
                            data.apply {
                                if (matchOdd.isOnlyEUType || matchOdd.odds == matchOdd.malayOdds || matchType == MatchType.OUTRIGHT || matchType == MatchType.OTHER_OUTRIGHT) {
                                    dataOddsType = OddsType.EU
                                }
                            }

                            //region 設置realAmount
                            var realAmount = data.betAmount
                            when (dataOddsType) {
                                OddsType.MYS -> {
                                    if (getOdds(data.matchOdd, dataOddsType) < 0) {
                                        realAmount = data.betAmount * abs(
                                            getOdds(
                                                data.matchOdd, dataOddsType
                                            )
                                        )
                                    }
                                }

                                OddsType.IDN -> {
                                    if (getOdds(data.matchOdd, dataOddsType) < 0) {
                                        realAmount = data.betAmount * abs(
                                            getOdds(
                                                data.matchOdd, dataOddsType
                                            )
                                        )
                                    }
                                }

                                else -> {
                                    //do nothing
                                }
                            }

                            data.realAmount = realAmount
                            //endregion
                        }
                        notifyAllBet()
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

                //keyListener = null
                removeTextChangedListener(tw)
                addTextChangedListener(tw)
                tag = tw
            }
            etBetSingle.isSelected =
                mSelectedPosition == bindingAdapterPosition && mBetView == BetListRefactorAdapter.BetViewType.SINGLE

            etBetSingle.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    etBetSingle.isFocusable = true
                    onItemClickListener.onHideKeyBoard()
//                    layoutKeyBoard.setupMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))
//                    layoutKeyBoard.showKeyboard(
//                        etBetSingle, position
//                    )
                    //onItemClickListener.onShowParlayKeyboard(etBetSingle, itemData, position, getMaxOrMinAmount(isGetMax = true, betList))
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition, BetListRefactorAdapter.BetViewType.SINGLE
                    )
                    onItemClickListener.onShowKeyboard(position)
                }
                false
            }

//            etBetSingle.setOnFocusChangeListener { _, hasFocus ->
//                if (!hasFocus) layoutKeyBoard?.hideKeyboard()
//                itemData.isInputBet = hasFocus
//                if (hasFocus) etBetSingle.setSelection(etBetSingle.text.length)
//                setEtBetSingleBackground(itemData)
//            }

            clItemBackgroundSingle.setOnClickListener {
                onItemClickListener.onHideKeyBoard()
                itemView.clearFocus()
            }

        }
    }


    /**
     * 填充所有單注後獲取總可贏額
     */
    private fun getAllSingleWinnableAmount(
        betAmount: Double, currentOddsType: OddsType, betList: MutableList<BetInfoListData>
    ): Double {
        var allWinnableAmount = 0.0
        betList.forEach {
            var win = 0.0
            when (val currentOddsTypeChecked =
                if (it.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType) {
                OddsType.MYS -> {
                    win = if (getOdds(it.matchOdd, currentOddsTypeChecked) < 0) {
                        betAmount
                    } else {
                        betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                    }

                }

                OddsType.IDN -> {
                    win = if (getOdds(it.matchOdd, currentOddsTypeChecked) < 0) {
                        betAmount
                    } else {
                        betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                    }
                }

                OddsType.EU -> {
                    win = betAmount * (getOdds(it.matchOdd, currentOddsTypeChecked) - 1)
                }

                else -> {
                    win = betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                }
            }
            allWinnableAmount += win
        }
        return allWinnableAmount
    }

    private fun getMaxOrMinAmount(
        isGetMax: Boolean, betList: MutableList<BetInfoListData>
    ): Double {

        val defaultMax = 9999999L
        var min = betList.firstOrNull()?.parlayOdds?.min ?: 0
        var max = betList.firstOrNull()?.parlayOdds?.max ?: defaultMax
        betList.forEach {
            if (it.parlayOdds?.min ?: 0 > min) {
                min = it.parlayOdds?.min ?: 0
            }
            if (it.parlayOdds?.max ?: 0 < max) {
                max = it.parlayOdds?.max ?: defaultMax
            }
        }

        return when (isGetMax) {
            true -> if (mUserLogin) max.toDouble() else defaultMax.toDouble() //未登入最大值9位數
            else -> min.toDouble()
        }
    }

    private fun checkBetLimitSingle(
        itemData: BetInfoListData, betAmount: Double
    ) {
        itemView.apply {
            var amountError = false
            val maxBet = (itemData.parlayOdds?.max ?: 0).toDouble()
            val minBet = (itemData.parlayOdds?.min ?: 0).toDouble()
            amountError = if (betAmount > maxBet) {
                //最大限額
                true
            } else {
                //最小限額
                betAmount != 0.0 && betAmount < minBet
            }
            val balanceError = betAmount != 0.0 && betAmount > mUserMoney
            itemData.amountError = if (balanceError) true else amountError
        }
    }

    private fun setEtBetSingleBackground(itemData: ParlayOdd) {
        contentView.apply {
            if (mHasBetClosedForSingle) {
                etBetSingle.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                etBetSingle.isEnabled = false
            } else {
                etBetSingle.isEnabled = true
                if (itemData.isInputBet) {
                    etBetSingle.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                } else {
                    etBetSingle.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                }
            }

            if (LoginRepository.isLogin.value == true) {
                //更新bet single editText hint
                val hint = etBetSingle.context.getString(
                    R.string.hint_bet_limit_range,
                    minBet.toLong().toString(),
                    maxBet.toLong().toString()
                )
                //限額用整數提示
                tvHintSingleDefault.text = hint
                val etBetHasInput = !etBetSingle.text.isNullOrEmpty()
//                et_bet_single
//                etBetSingle.setText("")
//                if (etBetHasInput) {
                tvHintSingleDefault.isVisible = !etBetHasInput //僅輸入金額以後隱藏
//                } else {
//                    tv_hint_single_default.isVisible = !itemData.isInputBet
//                }
            } else {
                tvHintSingleDefault.isVisible = false
            }
        }
    }
}