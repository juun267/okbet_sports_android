package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.layout_bet_info_list_float_button.*
import kotlinx.android.synthetic.main.layout_bet_info_list_float_button.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.bet.list.BetInfoListDialog
import org.cxct.sportlottery.ui.bet.list.BetInfoListParlayDialog
import org.cxct.sportlottery.ui.common.DragFloatActionButton
import kotlin.reflect.KClass


abstract class BaseOddButtonActivity<T : BaseOddButtonViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    private var oddListDialog: DialogFragment? = null
    private var floatButtonView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.betInfoRepository.isParlayPage.observe(this, {
            oddListDialog = when (it) {
                true -> {
                    BetInfoListParlayDialog()
                }
                false -> {
                    BetInfoListDialog()
                }
            }

            viewModel.betInfoRepository.getCurrentBetInfoList()
        })

        viewModel.betInfoRepository.betInfoList.observe(this, {
            when {
                it.isNullOrEmpty() -> {
                    updateOddButton(false, null)
                }
                oddListDialog is BetInfoListParlayDialog -> {
                    updateOddButton(true, 1)
                }
                oddListDialog is BetInfoListDialog -> {
                    updateOddButton(true, it.size)
                }
            }
        })

        viewModel.betInfoRepository.updateButtonPosition.observe(this, { map ->
            ll_bet_float_button?.x = map?.get("x")?.toFloat()?:0f
            ll_bet_float_button?.y = map?.get("y")?.toFloat()?:0f
        })
    }

    private fun updateOddButton(visible: Boolean, count: Int?) {

        ll_bet_float_button?.x = viewModel.betInfoRepository.buttonPositionX.toFloat()
        ll_bet_float_button?.y = viewModel.betInfoRepository.buttonPositionY.toFloat()

        ll_bet_float_button?.visibility = if (visible) View.VISIBLE else View.GONE

        count?.let {
            tv_bet_count?.text = it.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        setupOddButton()
        viewModel.betInfoRepository.getCurrentBetInfoList()
    }

    private fun setupOddButton() {
        if (floatButtonView != null) return

        val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)
        floatButtonView = LayoutInflater.from(this)
            .inflate(R.layout.layout_bet_info_list_float_button, contentView, false).apply {
                ll_bet_float_button.apply {
                    visibility = View.GONE
                    setOnClickListener {
                        oddListDialog?.show(
                            supportFragmentManager,
                            BaseOddButtonActivity::class.java.simpleName
                        )
                    }
                    actionUpListener = DragFloatActionButton.ActionUpListener {
                        viewModel.betInfoRepository.updateButtonPosition(x.toInt(), y.toInt())
                    }
                    post {
                        x = viewModel.betInfoRepository.buttonPositionX.toFloat()
                        y = viewModel.betInfoRepository.buttonPositionY.toFloat()
                        viewModel.betInfoRepository.updateButtonPosition(x.toInt(), y.toInt())
                    }
                }
            }

        contentView.addView(floatButtonView)
    }
}