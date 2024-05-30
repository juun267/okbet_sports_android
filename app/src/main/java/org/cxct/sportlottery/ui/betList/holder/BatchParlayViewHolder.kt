package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toDoubleS
import org.cxct.sportlottery.databinding.ItemBetListBatchControlConnectV3Binding as ItemBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betRecord.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.util.MoneyInputFilter
import org.cxct.sportlottery.util.Spanny
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getMultipleOdds
import org.cxct.sportlottery.view.boundsEditText.EditTextWatcher

class BatchParlayViewHolder(
    parent: ViewGroup,
    private val keyboardView: KeyboardView,
    private val binding: ItemBinding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root) {
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false
    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var mHasBetClosed: Boolean = false

    //判断用户是否进行了操作，如果操作之后就默认设置为最小金额
    private var isTouched = false

    fun setupParlayItem(
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
        if (itemData == null) {
            return
        }

        val multipleOdds = betList?.let { getMultipleOdds(it) }
        binding.tvParlayType.text = if (position == 0) {
//            setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.color_0D2245)),startIndex,endIndex,Spanny.SPAN_EXCLUSIVE_EXCLUSIVE)
//            setSpan(StyleSpan(Typeface.BOLD),startIndex,endIndex,Spanny.SPAN_EXCLUSIVE_EXCLUSIVE)
            Spanny(getParlayName(itemData.parlayType).plus("* ").plus(itemData.num.toString()))
                .append(multipleOdds,ForegroundColorSpan(ContextCompat.getColor(itemView.context,R.color.color_000000)),StyleSpan(Typeface.BOLD))
        } else {
            getParlayName(itemData.parlayType).plus("*").plus(itemData.num.toString())
        }
        setupBetAmountInput(itemData, onItemClickListener, position)
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

        val etBetParlay = binding.etBet.etBetParlay

        if (position == 0 && etBetParlay.text.isNullOrEmpty() && !isTouched) {
            if (mUserMoney < inputMinMoney) {
                etBetParlay.setText(TextUtil.formatInputMoney(mUserMoney))
            } else {
                etBetParlay.setText(TextUtil.formatInputMoney(inputMinMoney))
            }
        }

        etBetParlay.apply {
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
            etBetParlay.requestFocus()
            data.isInputBet = true
            keyboardView.setupMaxBetMoney(inputMaxMoney)
            keyboardView.setUserMoney(mUserMoney)
            keyboardView.showKeyboard(etBetParlay, 0)
        }

        itemView.onFocusChangeListener = null
        refreshSingleWinAmount(data)
        setEtBetParlayBackground(data)
        etBetParlay.apply {
            filters = arrayOf(MoneyInputFilter())
            val tw: TextWatcher = EditTextWatcher {
                isTouched = true
                if (it.isNullOrEmpty()) {
                    data.betAmount = 0.00
                    data.inputBetAmountStr = ""
                    data.input = null
                    refreshSingleWinAmount(null)
                } else {
                    val quota = it.toDoubleS()
                    data.betAmount = quota
                    data.inputBetAmountStr = it
                    data.input = it

                    MAX_BET_VALUE.let { max ->
                        if (quota > max) {
                            etBetParlay.setText(TextUtil.formatInputMoney(max))
//                          etBetParlay.setSelection(text.length)
                            return@EditTextWatcher
                        }
                    }
                }
                data.amountError = data.input.isNullOrEmpty()&&data.betAmount == 0.0
                setEtBetParlayBackground(data)
                onItemClickListener.refreshBetInfoTotal()
                refreshSingleWinAmount(data)
            }

            removeTextChangedListener(tw)
            addTextChangedListener(tw)
            tag = tw
        }

        val tvSymbol = binding.etBet.tvSymbol
        etBetParlay.setOnTouchListener { _, event ->
            BetInfoRepository.isTouched = true
            if (event.action == MotionEvent.ACTION_UP) {
                etBetParlay.requestFocus()
                tvSymbol.setTextColor(keyboardView.context.getColor(R.color.color_025BE8))
                keyboardView.setUserMoney(mUserMoney)
                keyboardView.setupMaxBetMoney(inputMaxMoney)
                keyboardView.showKeyboard(
                    etBetParlay, position, isParlay = true
                )
            }
            false
        }


        etBetParlay.setOnFocusChangeListener { _, hasFocus ->
            data.isInputBet = hasFocus
            if (hasFocus) {
                etBetParlay.setSelection(etBetParlay.text.length)
            }
            setEtBetParlayBackground(data)
        }

        binding.llControlConnect.setOnClickListener { itemView.clearFocus() }
    }

    private fun setEtBetParlayBackground(itemData: ParlayOdd) {
        binding.etBet.setBackgroundAndColor(
            null, inputMinMoney, inputMaxMoney, itemData2 = itemData
        )
    }


    private fun refreshSingleWinAmount(itemData: ParlayOdd?) {
        if (itemData == null) {
            binding.tvCanWinAmount.text = "${sConfigData?.systemCurrencySign} --"
        } else {
            val w = itemData.betAmount.toBigDecimal().multiply(itemData.odds.toBigDecimal())
            binding.tvCanWinAmount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(w, 2)}"
        }
    }


}