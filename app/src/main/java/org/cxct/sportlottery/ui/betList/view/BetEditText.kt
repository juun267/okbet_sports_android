package org.cxct.sportlottery.ui.betList.view

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewBetEditTextBinding
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.util.LocalUtils
import splitties.systemservices.layoutInflater

class BetEditText @JvmOverloads constructor(
    context: Context,
    attribute: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attribute, defStyle) {

    init {
        initView()
    }

    lateinit var binding: ViewBetEditTextBinding
    lateinit var etBetParlay: EditText
    lateinit var tvHintParlayDefault: TextView
    lateinit var tvSymbol: TextView

    private fun initView() {
        binding = ViewBetEditTextBinding.inflate(layoutInflater, this, true)
        etBetParlay = binding.etBetParlay
        tvHintParlayDefault = binding.tvHintParlayDefault
        tvSymbol = binding.tvSymbol
    }

    fun setBackgroundAndColor(
        itemData: BetInfoListData? = null,
        inputMinMoney: Double,
        inputMaxMoney: Double,
        isParlay: Boolean = false,
        mHasBetClosed: Boolean = false,
        itemData2: ParlayOdd? = null,
        isEndScore: Boolean = false
    ) {
        val etParlayBg: (Int) -> Unit = {
            etBetParlay.setBackgroundResource(it)
        }

        val isInputData: Boolean = itemData?.isInputBet ?: (itemData2?.isInputBet == true)

        if (isInputData) {
            tvSymbol.setTextColor(context.getColor(R.color.color_025BE8))
            etParlayBg(R.drawable.bg_radius_2_edittext_focus)
            etBetParlay.setTextColor(context.getColor(R.color.color_025BE8))
        } else {
            tvSymbol.setTextColor(context.getColor(R.color.color_000000))
            etParlayBg(if (isEndScore) R.drawable.bg_radius_2_edittext_unfocus_black else R.drawable.bg_radius_2_edittext_unfocus)
            etBetParlay.setTextColor(if (isEndScore) context.getColor(R.color.color_000000) else context.getColor(R.color.color_025BE8))
        }
        val betHint = tvSymbol.context.getString(
            R.string.hint_bet_limit_range,
            inputMinMoney.toLong().toString(),
            inputMaxMoney.toLong().toString()
        )

        if (isParlay) {
            if (LoginRepository.isLogin.value == true) {
                val etBetHasInput = !etBetParlay.text.isNullOrEmpty()
                tvHintParlayDefault.isVisible = !etBetHasInput //僅輸入金額以後隱藏
                if (mHasBetClosed) {
                    tvHintParlayDefault.text =
                        LocalUtils.getString(R.string.str_market_is_closed)
                    if (etBetHasInput) {
                        etBetParlay.setText("")
                    }
                } else {
                    //限額用整數提示
                    tvHintParlayDefault.text = betHint
                }
            } else {
                tvHintParlayDefault.isVisible = false
            }
        } else {
            etBetParlay.hint = betHint
        }
    }

}