package org.cxct.sportlottery.ui.base

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_loading.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.error.TokenError
import kotlin.reflect.KClass
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {
    companion object {
        private const val TAG = "BaseActivity"
    }

    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()
    }

    private fun onTokenStateChanged() {
        viewModel.code.observe(this, Observer {
            when (it) {
                TokenError.EXPIRED.code -> {
                    //TODO deal response code 2014
                }
                TokenError.FAILURE.code -> {
                    //TODO deal response code 2015
                }
                TokenError.REPEAT_LOGIN.code -> {
                    //TODO deal response code 2018
                }
            }
        })
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

}
