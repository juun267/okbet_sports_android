package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betRecord.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.util.MoneyInputFilter
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getMultipleOdds

abstract class BatchParlayViewHolder(
    itemView: View, private val keyboardView: KeyboardView
) : RecyclerView.ViewHolder(itemView) {
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var mHasBetClosed: Boolean = false

    //判断用户是否进行了操作，如果操作之后就默认设置为最小金额
    private var isTouched = false

    protected fun setupParlayItem(
        itemData: ParlayOdd?,
        hasBetClosed: Boolean,
        onItemClickListener: OnItemClickListener,
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
        if (itemData != null) {
            val multipleOdds = betList?.let { getMultipleOdds(it) }
            itemView.tv_parlay_type?.text = if (position == 0) {
                getParlayName(itemData.parlayType).plus("*")
                    .plus(itemData.num.toString()) + multipleOdds
            } else {
                getParlayName(itemData.parlayType).plus("*").plus(itemData.num.toString())
            }
            setupBetAmountInput(
                itemData, onItemClickListener, position
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBetAmountInput(
        data: ParlayOdd, onItemClickListener: OnItemClickListener, position: Int
    ) {
        itemView.apply {

            if (position == 0 && etBet.etBetParlay.text.isNullOrEmpty() && !isTouched) {
                if (mUserMoney < inputMinMoney) {
                    etBet.etBetParlay.setText(TextUtil.formatInputMoney(mUserMoney))
                } else {
                    etBet.etBetParlay.setText(TextUtil.formatInputMoney(inputMinMoney))
                }
            }

            etBet.etBetParlay.apply {
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
//                Timber.d("1 进来了- - - -- - - - - - - :isTouched:${false}")
                etBet.etBetParlay.requestFocus()
                data.isInputBet = true
                keyboardView.setupMaxBetMoney(inputMaxMoney)
                keyboardView.setUserMoney(mUserMoney)
                keyboardView.showKeyboard(etBet.etBetParlay, 0)
            }

            onFocusChangeListener = null
            refreshSingleWinAmount(data)
            setEtBetParlayBackground(data)
            etBet.etBetParlay.apply {
                filters = arrayOf(MoneyInputFilter())
                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        isTouched = true
                        if (it.isNullOrEmpty()) {
                            data.betAmount = 0.00
                            data.inputBetAmountStr = ""
                            data.input = null
                            refreshSingleWinAmount(null)
                        } else {
                            val quota = it.toString().toDoubleS()
                            data.betAmount = quota
                            data.inputBetAmountStr = it.toString()
                            data.input = it.toString()

                            MAX_BET_VALUE.let { max ->
                                if (quota > max) {
                                    etBet.etBetParlay.apply {
                                        setText(TextUtil.formatInputMoney(max))
//                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }
                        }
                        setEtBetParlayBackground(data)
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


            etBet.etBetParlay.setOnTouchListener { view, event ->
                BetInfoRepository.isTouched = true
                if (event.action == MotionEvent.ACTION_UP) {
                    etBet.etBetParlay.requestFocus()
                    etBet.tvSymbol.setTextColor(context.getColor(R.color.color_025BE8))
                    keyboardView.setUserMoney(mUserMoney)
                    keyboardView.setupMaxBetMoney(inputMaxMoney)
                    keyboardView.showKeyboard(
                        etBet.etBetParlay, position, isParlay = true
                    )
                }
                false
            }


            etBet.etBetParlay.setOnFocusChangeListener { _, hasFocus ->
                data.isInputBet = hasFocus
                if (hasFocus) {
                    etBet.etBetParlay.setSelection(etBet.etBetParlay.text.length)
                }
                setEtBetParlayBackground(data)
            }

            ll_control_connect.setOnClickListener {
                clearFocus()
            }
        }
    }

    private fun setEtBetParlayBackground(itemData: ParlayOdd) {
        itemView.etBet.setBackgroundAndColor(
            null, inputMinMoney, inputMaxMoney, itemData2 = itemData
        )
    }


    private fun refreshSingleWinAmount(itemData: ParlayOdd?) {
        if (itemData == null) {
            itemView.tvCanWinAmount.text = "${sConfigData?.systemCurrencySign} --"
        } else {
            val w = itemData.betAmount.toBigDecimal().multiply(itemData.odds.toBigDecimal())
            itemView.tvCanWinAmount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(w, 2)}"
        }
    }


}