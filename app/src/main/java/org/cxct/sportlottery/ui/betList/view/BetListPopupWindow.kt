package org.cxct.sportlottery.ui.betList.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.PopupwindowTipsBinding
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.util.DisplayUtil.dp

class BetListPopupWindow @JvmOverloads constructor(
    val context: Context, set: AttributeSet? = null, defTheme: Int = 0
) : PopupWindow(context, set, defTheme) {

    init {
        initView()
    }

    lateinit var binding: PopupwindowTipsBinding

    private fun initView() {
        binding=PopupwindowTipsBinding.inflate(LayoutInflater.from(context))
        contentView = binding.root
        setBackgroundDrawable(null)
        isOutsideTouchable = true
    }

    fun initOperation(
        tvLeagueName: TextView? = null,
        tvMatchHome: TextView? = null,
        tvMatchAway: TextView? = null,
        tvOddsContent: TextView? = null,
        tvName: TextView? = null,
        tvNameText: String? = null,
        itemData: BetInfoListData
    ) {

        val showPopAsTop: (TextView, String?) -> Unit = { it, it2 ->
            if (isShowing) {
                dismiss()
            }

            it.setTextColor(it.context.getColor(R.color.color_025BE8))
            binding.tvContent.text = it2
            val xOff: Int
            val yOff = (-50).dp
            if (it == tvMatchAway) {
                xOff = (-20).dp
                binding.ivPopupWindowTipsBg.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_right)
            } else {
                xOff = (-5).dp
                binding.ivPopupWindowTipsBg.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_left)
            }
            showAsDropDown(it, xOff, yOff)
        }
        setOnClickListeners(tvLeagueName, tvMatchHome, tvMatchAway, tvName, tvOddsContent) {
            when (it) {
                tvName -> {
                    showPopAsTop(tvName, tvNameText)
                }

                tvLeagueName -> {
                    showPopAsTop(tvLeagueName, itemData.matchOdd.leagueName)
                }

                tvMatchHome -> {
                    showPopAsTop(tvMatchHome, itemData.matchOdd.homeName)
                }

                tvMatchAway -> {
                    showPopAsTop(tvMatchAway, itemData.matchOdd.awayName)
                }

                tvOddsContent -> {
                    showPopAsTop(tvOddsContent, itemData.matchOdd.playName)
                }
            }
        }

        setOnDismissListener {
            tvName?.setTextColor(context.getColor(R.color.color_000000))
            tvLeagueName?.setTextColor(context.getColor(R.color.color_6D7693))
            tvMatchHome?.setTextColor(context.getColor(R.color.color_6D7693))
            tvMatchAway?.setTextColor(context.getColor(R.color.color_6D7693))
            tvOddsContent?.setTextColor(context.getColor(R.color.color_000000))
        }
    }


}