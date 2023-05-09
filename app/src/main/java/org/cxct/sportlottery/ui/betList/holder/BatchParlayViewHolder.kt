package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_v3_2.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.ui.betRecord.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getMultipleOdds
import timber.log.Timber

abstract class BatchParlayViewHolder(
    itemView: View, private val keyboardView: KeyboardView
) : RecyclerView.ViewHolder(itemView) {
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var mHasBetClosed: Boolean = false
//    private var isTouched = false

    protected fun setupParlayItem(
        itemData: ParlayOdd?,
        currentOddsType: OddsType,
        hasBetClosed: Boolean,
        firstItem: Boolean = false,
        onItemClickListener: OnItemClickListener,
        mSelectedPosition: Int,
        mBetView: BetListRefactorAdapter.BetViewType,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int,
        userMoney: Double,
        userLogin: Boolean,
        betList: MutableList<BetInfoListData>?
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        mHasBetClosed = hasBetClosed
        //設置投注輸入上限額
        setupInputMoney(itemData)
        setupItemEnable(hasBetClosed)
        if (itemData != null) {
            val multipleOdds = betList?.let { getMultipleOdds(it) }

            itemView.tv_parlay_type?.text = if (position == 0) {
                getParlayName(itemData.parlayType).plus("*")
                    .plus(itemData.num.toString()) + multipleOdds
            } else {
                getParlayName(itemData.parlayType).plus("*").plus(itemData.num.toString())
            }
            setupBetAmountInput(
                itemData,
                OddsType.EU,
                onItemClickListener,
                mSelectedPosition,
                mBetView,
                onSelectedPositionListener,
                position
            )
        }

    }

    private fun setupInputMoney(itemData: ParlayOdd?) {
        val parlayMaxBet = itemData?.max ?: 0
        //未登录的情况下，最大限额为7个9
        inputMaxMoney = if (mUserLogin) parlayMaxBet.toDouble() else 9999999.toDouble()
        val parlayMinBet = itemData?.min ?: 0
        inputMinMoney = parlayMinBet.toDouble()
    }

    private fun getParlayName(parlayType: String): String {
        return getParlayStringRes(parlayType)?.let {
            itemView.context.getString(it)
        } ?: ""
    }

    private fun setupItemEnable(hasBetClosed: Boolean) {
//        itemView.apply {
//            ll_hint_container.isVisible = !hasBetClosed
//        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBetAmountInput(
        data: ParlayOdd,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        mSelectedPosition: Int,
        mBetView: BetListRefactorAdapter.BetViewType,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int
    ) {
        itemView.apply {

            if (position==0){
                if (mUserMoney < inputMinMoney) {
                    et_bet_parlay.setText(mUserMoney.toString())
                } else {
                    et_bet_parlay.setText(inputMinMoney.toString())
                }
            }


            et_bet_parlay.apply {
                //第1步：為了避免TextWatcher在第2步被調用，提前移除
                if (tag is TextWatcher) {
                    removeTextChangedListener(tag as TextWatcher)
                }
                //第2步：移除TextWatcher之後，設置EditText的value
                if (data.input != null) {
                    setText(data.inputBetAmountStr)
                } else {
                    text.clear()
                }
                setSelection(text.length)
            }

            if (!BetInfoRepository.isTouched && position == 0) {
                Timber.d("1 进来了- - - -- - - - - - - :isTouched:${false}")
                et_bet_parlay.requestFocus()
                data.isInputBet = true
                keyboardView.showKeyboard(et_bet_parlay,0)
            }

            onFocusChangeListener = null
            refreshSingleWinAmount(data)
            checkBetLimitParlay(data)
            et_bet_parlay.apply {
                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            data.betAmount = 0.000
                            data.inputBetAmountStr = ""
                            data.input = null
                            refreshSingleWinAmount(null)
                        } else {
                            val quota = it.toString().toDoubleS()
                            data.betAmount = quota
                            data.inputBetAmountStr = it.toString()
                            data.input = it.toString()

                            inputMaxMoney.let { max ->
                                if (quota > max) {
                                    et_bet_parlay.apply {
                                        setText(TextUtil.formatInputMoney(max))
//                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }
                        }
                        checkBetLimitParlay(data)
                        onItemClickListener.refreshBetInfoTotal()
                        refreshSingleWinAmount(data)
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
                removeTextChangedListener(tw)
                addTextChangedListener(tw)
                tag = tw
            }

            et_bet_parlay.setOnTouchListener { view, event ->
                BetInfoRepository.isTouched = true
                Timber.d("2 进来了- - - -- - - - - - - :isTouched:${BetInfoRepository.isTouched}")
                if (event.action == MotionEvent.ACTION_UP) {
//                    et_bet_parlay.isFocusable = true
                    et_bet_parlay.requestFocus()
                    keyboardView.setupMaxBetMoney(inputMaxMoney)
                    keyboardView.showKeyboard(
                        et_bet_parlay, position, isParlay = true
                    )
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition, BetListRefactorAdapter.BetViewType.PARLAY
                    )
                    onItemClickListener.onShowParlayKeyboard(position)
                }
                false
            }

            Timber.d("position:${position}")

            et_bet_parlay.setOnFocusChangeListener { _, hasFocus ->
//                if (!hasFocus) keyboardView.hideKeyboard()
                data.isInputBet = hasFocus
                if (hasFocus) {
                    et_bet_parlay.setSelection(et_bet_parlay.text.length)
                }
                setEtBetParlayBackground(data)
            }

            ll_control_connect.setOnClickListener {
                clearFocus()
            }
        }
    }

    private fun setEtBetParlayBackground(itemData: ParlayOdd) {
        itemView.apply {
            if (mHasBetClosed) {
                et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                et_bet_parlay.isEnabled = false
                tv_hint_parlay_default.text = LocalUtils.getString(R.string.str_market_is_closed)
            } else {
                et_bet_parlay.isEnabled = true
                if (itemData.amountError) {
                    et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_error)
                } else if (itemData.isInputBet) {
                    et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                } else {
                    et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                }
            }

            val betHint = context.getString(
                R.string.hint_bet_limit_range,
                inputMinMoney.toLong().toString(),
                inputMaxMoney.toLong().toString()
            )
            if (LoginRepository.isLogin.value == true) {
                val etBetHasInput = !et_bet_parlay.text.isNullOrEmpty()
                tv_hint_parlay_default.isVisible = !etBetHasInput //僅輸入金額以後隱藏
                if (mHasBetClosed) {
                    tv_hint_parlay_default.text =
                        LocalUtils.getString(R.string.str_market_is_closed)
                    et_bet_parlay.setText("")
                } else {
                    //限額用整數提示
                    tv_hint_parlay_default.text = betHint
                }
            } else {
                tv_hint_parlay_default.isVisible = false
            }
        }
    }

    private fun checkBetLimitParlay(
        itemData: ParlayOdd, betAmount: Double = itemData.betAmount
    ) {
        itemView.apply {
            val amountError: Boolean = if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                //請輸入正確投注額
                !itemData.input.isNullOrEmpty()
            } else {
                if (betAmount > inputMaxMoney) {
                    //超過最大限額
                    true
                } else {
                    betAmount != 0.0 && betAmount < inputMinMoney
                }
            }
            val balanceError: Boolean = betAmount != 0.0 && betAmount > mUserMoney
            itemData.amountError = if (balanceError) true else amountError
        }
        setEtBetParlayBackground(itemData)
    }

    private fun refreshSingleWinAmount(itemData: ParlayOdd?) {
        if (itemData == null) {
            itemView.tvCanWinAmount.text = "${sConfigData?.systemCurrencySign} --"
        } else {
            val w = itemData.betAmount.toBigDecimal().multiply(itemData.odds.toBigDecimal())
//            val winnable =
//                w.subtract(itemData.betAmount.toBigDecimal().multiply(itemData.num.toBigDecimal()))
//                    .toDouble()
//            Timber.d("w:${w} winnable:${winnable} item.betAmount:${itemData.betAmount} itemData.num:${itemData.num}")

            itemView.tvCanWinAmount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(w, 2)}"
        }
    }


}