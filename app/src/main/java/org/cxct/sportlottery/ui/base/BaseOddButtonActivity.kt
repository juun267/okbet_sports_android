package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_bet_info_list_float_button.*
import kotlinx.android.synthetic.main.layout_bet_info_list_float_button.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.bet.list.BetInfoListDialog
import org.cxct.sportlottery.ui.bet.list.BetInfoListParlayDialog
import kotlin.reflect.KClass


abstract class BaseOddButtonActivity<T : BaseViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    private var oddListDialog: DialogFragment? = null
    private var floatButtonView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.betInfoRepository?.isParlayPage?.observe(this, Observer {
            oddListDialog = when (it) {
                true -> {
                    BetInfoListParlayDialog()
                }
                false -> {
                    BetInfoListDialog()
                }
            }

            viewModel.betInfoRepository?.getCurrentBetInfoList()
        })

        viewModel.betInfoRepository?.betInfoList?.observe(this, Observer {
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
    }

    private fun updateOddButton(visible: Boolean, count: Int?) {
        if (visible) {
            rl_bet_float_button.visibility = View.VISIBLE
        } else {
            rl_bet_float_button.visibility = View.GONE
        }

        count?.let {
            tv_bet_count.text = it.toString()
        }
    }

    override fun onResume() {
        super.onResume()

        setupOddButton()

        viewModel.betInfoRepository?.getCurrentBetInfoList()
    }

    private fun setupOddButton() {
        if (floatButtonView != null) return
        val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)
        floatButtonView = LayoutInflater.from(this)
            .inflate(R.layout.layout_bet_info_list_float_button, contentView, false).apply {
                this.rl_bet_float_button.visibility = View.GONE
                this.rl_bet_float_button.setOnClickListener {
                    oddListDialog?.show(
                        supportFragmentManager,
                        BaseOddButtonActivity::class.java.simpleName
                    )
                }
            }
        contentView.addView(floatButtonView)
    }
}