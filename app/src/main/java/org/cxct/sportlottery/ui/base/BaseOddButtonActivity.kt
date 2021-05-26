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
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.OnForbidClickListener
import kotlin.reflect.KClass

const val SP_NAME = "button_position"
const val POSITION_X = "position_x"
const val POSITION_Y = "position_y"
const val FIRST_BET = "first_bet"
const val FIRST_BET_FOR_PARLAY = "first_bet_for_parlay"

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
        return mSharedPreferences?.getFloat(POSITION_X, 6.dp.toFloat())
    }

    //14 bottom margin
    //48 button size
    private fun getPositionY(): Float? {
        return mSharedPreferences?.getFloat(POSITION_Y, (MetricsUtil.getScreenHeight() - 14.dp - 48.dp - MetricsUtil.getStatusBarHeight()).toFloat())
    }

    private fun saveFirstBetFlag(boolean: Boolean) {
        mSharedPreferences?.edit()
            ?.putBoolean(FIRST_BET, boolean)
            ?.apply()
    }

    private fun saveFirstBetFlagForParlay(boolean: Boolean) {
        mSharedPreferences?.edit()
            ?.putBoolean(FIRST_BET_FOR_PARLAY, boolean)
            ?.apply()
    }

    private fun getFirstBetFlag(): Boolean? {
        return mSharedPreferences?.getBoolean(FIRST_BET, true)
    }

    private fun getFirstBetFlagForParlay(): Boolean? {
        return mSharedPreferences?.getBoolean(FIRST_BET_FOR_PARLAY, true)
    }

    private var oddListDialog: DialogFragment? = null
    private var floatButtonView: View? = null
    private var openFlag = true

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

            openFlag = false
            viewModel.betInfoRepository.getCurrentBetInfoList()
        })

        viewModel.betInfoRepository.betInfoList.observe(this, {
            it.peekContent().let {
                if (it.size == 0) saveFirstBetFlag(true)
                if (it.size == 0 || it.size == 1) saveFirstBetFlagForParlay(true)

                when {
                    it.isNullOrEmpty() -> {
                        updateOddButton(false, null)
                    }
                    oddListDialog is BetInfoListParlayDialog -> {
                        updateOddButton(true, 1)
                        if (!openFlag) return@observe
                        if (getFirstBetFlagForParlay() == true && it.size == 2) {
                            saveFirstBetFlagForParlay(false)
                            showBetListDialog()
                        }
                    }
                    oddListDialog is BetInfoListDialog -> {
                        updateOddButton(true, it.size)
                        if (!openFlag) return@observe
                        if (getFirstBetFlag() == true && it.size == 1 && openFlag) {
                            saveFirstBetFlag(false)
                            showBetListDialog()
                        }
                    }
                }
                openFlag = true
            }
        })
    }

    private fun showBetListDialog() {
        if (oddListDialog?.isAdded == false) {
            oddListDialog?.show(
                supportFragmentManager,
                BaseOddButtonActivity::class.java.simpleName
            )
        }
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
        checkVisibleView()
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
                    setOnClickListener(object : OnForbidClickListener() {
                        override fun forbidClick(view: View?) {
                            oddListDialog?.show(
                                supportFragmentManager,
                                BaseOddButtonActivity::class.java.simpleName
                            )
                        }
                    })
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

    private fun checkVisibleView() {
        floatButtonView?.visibility = if (javaClass.simpleName == GameActivity::class.java.simpleName) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun resetButton() {
        savePositionXY(ll_bet_float_button.defaultPositionX, ll_bet_float_button.defaultPositionY)
    }

}