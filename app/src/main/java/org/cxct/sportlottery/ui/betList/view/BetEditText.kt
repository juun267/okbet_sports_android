package org.cxct.sportlottery.ui.betList.view

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.etBet
import kotlinx.android.synthetic.main.view_bet_edit_text.view.etBetParlay
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
        itemData2: ParlayOdd? = null
    ) {
        val etParlayBg: (Int) -> Unit = {
            etBetParlay.setBackgroundResource(it)
        }

        val isInputData: Boolean = itemData?.isInputBet ?: (itemData2?.isInputBet == true)

        if (isInputData) {
            tvSymbol.setTextColor(context.getColor(R.color.color_025BE8))
            etParlayBg(R.drawable.bg_radius_2_edittext_focus)
        } else {
            etParlayBg(R.drawable.bg_radius_2_edittext_unfocus)
            tvSymbol.setTextColor(context.getColor(R.color.color_000000))
        }
        val betHint = tvSymbol.context.getString(
            R.string.hint_bet_limit_range,
            inputMinMoney.toLong().toString(),
            inputMaxMoney.toLong().toString()
        )

        if (isParlay) {
            if (LoginRepository.isLogin.value == true) {
                val etBetHasInput = !etBet.etBetParlay.text.isNullOrEmpty()
                etBet.tvHintParlayDefault.isVisible = !etBetHasInput //僅輸入金額以後隱藏
                if (mHasBetClosed) {
                    etBet.tvHintParlayDefault.text =
                        LocalUtils.getString(R.string.str_market_is_closed)
                    if (etBetHasInput) {
                        etBet.etBetParlay.setText("")
                    }
                } else {
                    //限額用整數提示
                    etBet.tvHintParlayDefault.text = betHint
                }
            } else {
                etBet.tvHintParlayDefault.isVisible = false
            }
        } else {
            if (LoginRepository.isLogin.value == true) {
                etBetParlay.hint = betHint
            } else {
                etBetParlay.hint = ""
            }

        }
    }

}