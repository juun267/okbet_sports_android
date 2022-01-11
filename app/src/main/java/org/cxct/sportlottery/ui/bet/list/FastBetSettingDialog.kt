package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetFastBetSettingBinding
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel


/**
 * @author Kevin
 * @create 2021/7/8
 * @description
 */
//const val INPLAY: Int = 1

@SuppressLint("SetTextI18n", "ClickableViewAccessibility")
class FastBetSettingDialog : BaseSocketBottomSheetFragment<GameViewModel>(GameViewModel::class) {


    private lateinit var binding: DialogBottomSheetFastBetSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomSheetFastBetSettingBinding.inflate(inflater, container, false)
        binding.apply {

        }.executePendingBindings()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.apply {

            viewClose.setOnClickListener {
                dismiss()
            }

            switchButton.apply {
                isChecked = viewModel.getIsFastBetOpened()
                setOnCheckedChangeListener { _, isChecked ->
                    when(isChecked){
                        true -> viewModel.setFastBetOpened(true)
                        else -> viewModel.setFastBetOpened(false)
                    }
                }
            }
        }
    }
}