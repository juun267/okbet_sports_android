package org.cxct.sportlottery.ui.base

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_bet_info_list_float_button.*
import kotlinx.android.synthetic.main.layout_loading.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.home.MainActivity
import kotlin.reflect.KClass
import org.cxct.sportlottery.ui.bet.list.BetInfoListDialog
import org.cxct.sportlottery.ui.home.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    private var floatButtonView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()

    }

    override fun onResume() {
        super.onResume()
        createOddButton()
    }

    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this, Observer {
            showDialogLogout(it.msg)
        })
    }

    private fun showDialogLogout(message: String) {
        val dialog = CustomAlertDialog(this)
        dialog.setMessage(message)
        dialog.setPositiveClickListener {
            MainActivity.reStart(this)
            dialog.dismiss()
        }
        dialog.setNegativeButtonText(null)
        dialog.show()
    }

    private fun onNetworkException() {
        viewModel.networkExceptionUnknown.observe(this, Observer {
            //TODO show network exception message
        })
    }

    /*弹出加载界面*/
    open fun loading() {
        loading(null)
    }

    open fun loading(message: String?) {
        if (loadingView == null) {
            loadingView = layoutInflater.inflate(R.layout.layout_loading, null)
            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            addContentView(loadingView, params)
        } else {
            loadingView?.rl_loading?.visibility = View.VISIBLE
            loadingView?.rl_loading?.isClickable = true
        }

        loadingView?.pb_message?.text = message ?: getString(R.string.loading)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        if (loadingView == null) {
            Timber.d("loadingView不存在")
        } else {
            loadingView?.rl_loading?.visibility = View.GONE
        }
    }

    //隱藏鍵盤
    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus?.windowToken, 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onNetworkUnavailable() {
        Toast.makeText(applicationContext, R.string.connect_first, Toast.LENGTH_SHORT).show()
    }

    private fun createOddButton() {
        if (floatButtonView != null) return
        val contentView: ViewGroup = window.decorView.findViewById(android.R.id.content)
        floatButtonView = LayoutInflater.from(this).inflate(R.layout.layout_bet_info_list_float_button, contentView, false)
        contentView.addView(floatButtonView)

        if (viewModel is MainViewModel) {
            (viewModel as MainViewModel).betInfoList.observe(this, Observer {
                if (it != null) checkBetInfoList(it.size)
            })
        }

        betFloatButtonVisible(false)//default
        rl_bet_float_button.setOnClickListener { BetInfoListDialog().show(supportFragmentManager, BetInfoListDialog.TAG) }
    }

    private fun checkBetInfoList(count: Int) {
        if (count > 0) {
            betFloatButtonVisible(true)
            setBetCount(count)
        } else {
            betFloatButtonVisible(false)
        }
    }

    open fun betFloatButtonVisible(visible: Boolean) {
        rl_bet_float_button.visibility = if (visible) View.VISIBLE else View.GONE
    }

    open fun setBetCount(count: Int) {
        tv_bet_count.text = count.toString()
    }


}
