package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.KeyBoardUtil


/**
 * @author Kevin
 * @create 2021/7/8
 * @description
 */
class BetInfoCarDialog : BaseBottomSheetFragment<GameViewModel>(GameViewModel::class) {


    init {
        setStyle(STYLE_NORMAL, R.style.LightBackgroundBottomSheet)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet_betinfo_item, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyBoard()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyBoard() {
        val keyboard = KeyBoardUtil(kv_keyboard, null)
        et_bet.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                keyboard.showKeyboard(view as EditText)
            }
            false
        }
    }


}