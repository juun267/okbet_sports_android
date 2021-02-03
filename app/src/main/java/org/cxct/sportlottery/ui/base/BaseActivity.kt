package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
import org.cxct.sportlottery.ui.home.broadcast.ServiceBroadcastReceiver
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    private var mLayoutHandler = Handler(Looper.getMainLooper())
    private var mPromptDialog: CustomAlertDialog? = null

    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    private var floatButtonView: View? = null

    private val mReceiver by lazy {
        ServiceBroadcastReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()
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

    @SuppressLint("InflateParams")
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

    fun showPromptDialog(title: String, message: String, positiveClickListener: () -> Unit?) {
        showPromptDialog(title, message, null, positiveClickListener)
    }

    fun showPromptDialog(title: String?, errorMessage: String?, buttonText: String?, positiveClickListener: () -> Unit?) {
        safelyUpdateLayout(Runnable {
            try {
                //防止跳出多個 error dialog
                if (mPromptDialog?.isShowing == true)
                    mPromptDialog?.dismiss()

                mPromptDialog = CustomAlertDialog(this@BaseActivity).apply {
                    setTitle(title)
                    setMessage(errorMessage)
                    setPositiveButtonText(buttonText ?: getString(R.string.btn_confirm))
                    setNegativeButtonText(null)
                    setPositiveClickListener(View.OnClickListener {
                        positiveClickListener()
                        mPromptDialog?.dismiss()
                    })

                    setCanceledOnTouchOutside(false)
                    setCancelable(false) //不能用系統 BACK 按鈕關閉 dialog
                    show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    protected fun safelyUpdateLayout(runnable: Runnable) {
        try {
            mLayoutHandler.post(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
