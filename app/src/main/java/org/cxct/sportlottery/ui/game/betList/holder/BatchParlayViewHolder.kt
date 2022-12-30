package org.cxct.sportlottery.ui.game.betList.holder

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.et_bet_parlay
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.layoutKeyBoard
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.ll_control_connect
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.ll_hint_container
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.tvBalanceInsufficientMessageParlay
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.tvErrorMessageParlay
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.tvPleaseEnterCorrectAmountParlay
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.tv_hint_parlay_default
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.tv_parlay_type
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.game.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.game.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.game.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.TextUtil

abstract class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var mHasBetClosed: Boolean = false
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
        userLogin: Boolean
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        mHasBetClosed = hasBetClosed
        //設置投注輸入上限額
        setupInputMoney(itemData)
        setupItemEnable(hasBetClosed)
        if (itemData != null) {
            itemView.tv_parlay_type?.text = if (itemData.num == 1) {
                getParlayName(itemData.parlayType).plus("*").plus(itemData.num.toString())
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
        itemView.apply {
            ll_hint_container.isVisible = !hasBetClosed
        }
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
            et_bet_parlay.apply {
                //第1步：為了避免TextWatcher在第2步被調用，提前移除
                if (tag is TextWatcher) {
                    removeTextChangedListener(tag as TextWatcher)
                }

                //第2步：移除TextWatcher之後，設置EditText的value
                if (data.input != null) setText(data.inputBetAmountStr) else text.clear()
                setSelection(text.length)
            }
            onFocusChangeListener = null

            if (data.isInputBet) {
                layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
            }
            checkBetLimitParlay(data)

            et_bet_parlay.apply {
                /* set listener */
                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            data.betAmount = 0.000
                            data.inputBetAmountStr = ""
                            data.input = null
                        } else {
                            val quota = it.toString().toDouble()
                            data.betAmount = quota
                            data.inputBetAmountStr = it.toString()
                            data.input = it.toString()

                            inputMaxMoney.let { max ->
                                if (quota > max) {
                                    et_bet_parlay.apply {
                                        setText(TextUtil.formatInputMoney(max))
                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }
                        }
                        checkBetLimitParlay(data)
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

                removeTextChangedListener(tw)
                addTextChangedListener(tw)
                tag = tw
            }

            et_bet_parlay.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    et_bet_parlay.isFocusable = true
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    layoutKeyBoard.showKeyboard(
                        et_bet_parlay, position, isParlay = true
                    )
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition, BetListRefactorAdapter.BetViewType.PARLAY
                    )
                    onItemClickListener.onShowParlayKeyboard(position)
                }
                false
            }

            et_bet_parlay.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                data.isInputBet = hasFocus
                if (hasFocus) et_bet_parlay.setSelection(et_bet_parlay.text.length)
                setEtBetParlayBackground(data)
            }

            ll_control_connect.setOnClickListener {
                onItemClickListener.onHideKeyBoard()
                clearFocus()
            }
        }
    }

    private fun setEtBetParlayBackground(itemData: ParlayOdd) {
        itemView.apply {
            if (mHasBetClosed) {
                et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                et_bet_parlay.isEnabled = false
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
                //限額用整數提示
                tv_hint_parlay_default.text = betHint
                val etBetHasInput = !et_bet_parlay.text.isNullOrEmpty()
                tv_hint_parlay_default.isVisible = !etBetHasInput //僅輸入金額以後隱藏

            } else {
                tv_hint_parlay_default.isVisible = false
            }
        }
    }

    private fun checkBetLimitParlay(
        itemData: ParlayOdd, betAmount: Double = itemData.betAmount
    ) {
        itemView.apply {
            var amountError: Boolean
            val balanceError: Boolean
            if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                tvErrorMessageParlay.isVisible = false
                //請輸入正確投注額
                tvPleaseEnterCorrectAmountParlay.visibility =
                    if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                        amountError = true
                        View.VISIBLE
                    } else {
                        amountError = false
                        View.GONE
                    }
            } else {
                tvPleaseEnterCorrectAmountParlay.isVisible = false
                if (betAmount > inputMaxMoney) {
                    //超過最大限額
                    amountError = true
                    tvErrorMessageParlay.apply {
                        text = context.getString(R.string.bet_info_list_maximum_limit_amount)
                        isVisible = true
                    }
                } else {
                    tvErrorMessageParlay.apply {
                        isVisible = false
                        if (betAmount != 0.0 && betAmount < inputMinMoney) {
                            //低於最小限額
                            amountError = true
                            text = context.getString(R.string.bet_info_list_minimum_limit_amount)
                            isVisible = true
                        } else {
                            amountError = false
                            isVisible = false
                        }
                    }
                }
            }
            tvBalanceInsufficientMessageParlay.visibility =
                if (betAmount != 0.0 && betAmount > mUserMoney) {
                    tvErrorMessageParlay.isVisible = false //同時滿足限額和餘額不足提示條件，優先顯示餘額不足
                    balanceError = true
                    View.VISIBLE
                } else {
                    balanceError = false
                    View.GONE
                }
            itemData.amountError = if (balanceError) true else amountError
        }
        setEtBetParlayBackground(itemData)
    }


}