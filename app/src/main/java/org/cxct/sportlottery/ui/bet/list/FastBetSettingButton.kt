package org.cxct.sportlottery.ui.bet.list


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.button_bet.view.*
import org.cxct.sportlottery.R


/**
 * @author Kevin
 * @create 2021/7/8
 * @description 包含登入按鈕、投注按鈕(兩種狀態)、賠率變動接受按鈕
 */
class FastBetSettingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        init()
    }


    private fun init() {
        inflate(context, R.layout.button_fast_bet_setting, this)

    }
}