package org.cxct.sportlottery.ui.base

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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

const val SP_NAME = "button_position"
const val POSITION_X = "position_x"
const val POSITION_Y = "position_y"

abstract class BaseOddButtonActivity<T : BaseOddButtonViewModel>(clazz: KClass<T>) :
    BaseSocketActivity<T>(clazz) {

    private var mSharedPreferences: SharedPreferences? = null

    fun getInstance(context: Context?): SharedPreferences? {
        if (mSharedPreferences == null)
            mSharedPreferences = context?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return mSharedPreferences
    }

    private fun savePositionXY(x: Float, y: Float) {
        mSharedPreferences?.edit()
            ?.putFloat(POSITION_X, x)
            ?.putFloat(POSITION_Y, y)
            ?.apply()
    }

    private fun getPositionX(): Float? {
        return mSharedPreferences?.getFloat(POSITION_X, -1f)
    }

    private fun getPositionY(): Float? {
        return mSharedPreferences?.getFloat(POSITION_Y, -1f)
    }

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
    }

    private fun updateOddButton(visible: Boolean, count: Int?) {
        ll_bet_float_button?.visibility = if (visible) View.VISIBLE else View.GONE
        count?.let {
            tv_bet_count?.text = it.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        getInstance(applicationContext)
        setupOddButton()
        viewModel.betInfoRepository.getCurrentBetInfoList()
    }


    private fun setupOddButton() {
        if (floatButtonView != null) {
            ll_bet_float_button.post {
                getPositionX()?.let { it -> ll_bet_float_button?.x = it }
                getPositionY()?.let { it -> ll_bet_float_button?.y = it }
            }
            return
        }
        val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)
        floatButtonView = LayoutInflater.from(this)
            .inflate(R.layout.layout_bet_info_list_float_button, contentView, false).apply {
                ll_bet_float_button.apply {
                    visibility = View.INVISIBLE
                    setOnClickListener {
                        oddListDialog?.show(
                            supportFragmentManager,
                            BaseOddButtonActivity::class.java.simpleName
                        )
                    }
                    actionUpListener = DragFloatActionButton.ActionUpListener {
                        savePositionXY(x, y)
                    }
                    post {
                        getPositionX()?.let { it -> x = it }
                        getPositionY()?.let { it -> y = it }
                        savePositionXY(x, y)
                    }
                }
            }
        contentView.addView(floatButtonView)
    }

    fun resetButton() {
        savePositionXY(ll_bet_float_button.defaultPositionX, ll_bet_float_button.defaultPositionY)
    }

}