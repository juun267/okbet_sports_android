package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetBetinfoItemBinding
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.KeyBoardUtil


/**
 * @author Kevin
 * @create 2021/7/8
 * @description
 */
class BetInfoCarDialog : BaseBottomSheetFragment<GameViewModel>(GameViewModel::class) {


    private lateinit var binding: DialogBottomSheetBetinfoItemBinding


    private var betInfoListData: BetInfoListData? = null


    private var addFlag = false


    init {
        setStyle(STYLE_NORMAL, R.style.LightBackgroundBottomSheet)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBottomSheetBetinfoItemBinding.inflate(inflater, container, false)
        binding.apply {
            gameViewModel = this@BetInfoCarDialog.viewModel
            lifecycleOwner = this@BetInfoCarDialog.viewLifecycleOwner
            dialog = this@BetInfoCarDialog
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyBoard()
        initObserve()
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


    private fun initObserve() {
        viewModel.betInfoSingle.observe(this.viewLifecycleOwner, {
            betInfoListData = it
        })
    }


    fun addToBetInfoList() {
        addFlag = true
        dismiss()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!addFlag) viewModel.removeBetInfoAll()
    }
}