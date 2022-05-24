package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.layout_loading.view.*
import kotlinx.coroutines.*
import me.jessyan.autosize.AutoSizeCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.commonCheckDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.lang.Runnable
import kotlin.reflect.KClass

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    private var mLayoutHandler = Handler(Looper.getMainLooper())
    private var mPromptDialog: CustomAlertDialog? = null
    private var mTokenPromptDialog: CustomAlertDialog? = null
    private var mOnNetworkExceptionListener: View.OnClickListener? = null
    private var mPickerView: OptionsPickerView<String>? = null
    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private val mHandler = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable? = null


    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }
    private val bottomSheetView: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.dialog_bottom_sheet_custom, null)
    }

    override fun onStart() {
        super.onStart()
        startCheckToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()
    }

    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this) {
            if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName) return@observe
            if (it.code != HttpError.KICK_OUT_USER.code)
                toMaintenanceOrShowDialog(it)
        }
    }

    private fun toMaintenanceOrShowDialog(result: BaseResult) {
        when (result.code) {
            HttpError.DO_NOT_HANDLE.code -> {
            }
            HttpError.MAINTENANCE.code -> {
                startActivity(Intent(this, MaintenanceActivity::class.java))
                finish()
            }
            else -> {
                showTokenPromptDialog(result.msg) {
                    viewModel.doLogoutCleanUser {
                        if (sConfigData?.thirdOpen == FLAG_OPEN)
                            MainActivity.reStart(this)
                        else
                            GamePublicityActivity.reStart(this)
                    }
                }
            }
        }
    }

    private fun onNetworkException() {
        viewModel.networkExceptionUnavailable.observe(this) {
            hideLoading()
            showErrorPromptDialog(it) { mOnNetworkExceptionListener?.onClick(null) }
        }

        viewModel.networkExceptionTimeout.observe(this) {
            hideLoading()
            showErrorPromptDialog(it) { mOnNetworkExceptionListener?.onClick(null) }
        }

        viewModel.networkExceptionUnknown.observe(this) {
            hideLoading()
            showErrorPromptDialog(it) { mOnNetworkExceptionListener?.onClick(null) }
        }

        viewModel.isKickedOut.observe(this) {
            hideLoading()
            it.getContentIfNotHandled()?.let { msg ->
                showTokenPromptDialog(msg) {
                    viewModel.loginRepository._isLogin.postValue(false)
                    val intent = Intent(this@BaseActivity, GamePublicityActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    //20210526 紀錄：call webAPI 的 exception 錯誤提示統一在 BackActivity 處理，若有需要 callback 再使用此 fun
    fun setOnNetworkExceptionListener(listener: View.OnClickListener?) {
        mOnNetworkExceptionListener = listener
    }

    /*弹出加载界面*/
    open fun loading() {
        loading(null)
    }

    @SuppressLint("InflateParams")
    open fun loading(message: String?) {
        if (loadingView == null) {
            loadingView = layoutInflater.inflate(R.layout.layout_loading, null)
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
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

    private fun showTokenPromptDialog(errorMessage: String, positiveClickListener: () -> Unit?) {
        safelyUpdateLayout(Runnable {
            try {
                //防止跳出多個 error dialog
                if (mTokenPromptDialog?.isShowing == true)
                    return@Runnable

                mTokenPromptDialog = CustomAlertDialog(this@BaseActivity).apply {
                    setTextColor(R.color.color_E44438_e44438)
                    setTitle(this@BaseActivity.getString(R.string.prompt))
                    setMessage(errorMessage)
                    setPositiveButtonText(this@BaseActivity.getString(R.string.btn_determine))
                    setNegativeButtonText(null)
                    setPositiveClickListener(View.OnClickListener {
                        positiveClickListener()
                        mTokenPromptDialog?.dismiss()
                        mTokenPromptDialog = null
                    })

                    setCanceledOnTouchOutside(false)
                    isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
                }
                mTokenPromptDialog?.show(supportFragmentManager, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: String,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, null, positiveClickListener, false)
    }

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: Spanned,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, null, positiveClickListener, false)
    }

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: Spanned,
        isOutsideCancelable: Boolean,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, null, positiveClickListener, false, isOutsideCancelable = isOutsideCancelable)
    }

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: String,
        buttonText: String?,
        isShowDivider: Boolean,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, buttonText, positiveClickListener, false, isShowDivider)
    }

    fun showErrorPromptDialog(message: String, positiveClickListener: () -> Unit?) {
        showErrorPromptDialog(getString(R.string.error), message, positiveClickListener)
    }

    fun showErrorPromptDialog(title: String, message: String, positiveClickListener: () -> Unit?) {
        showPromptDialog(title, message, null, positiveClickListener, true)
    }

    fun showPromptDialog(
        title: String?,
        errorMessageSpan: Spanned,
        buttonText: String?,
        positiveClickListener: () -> Unit?,
        isError: Boolean,
        isShowDivider: Boolean? = false,
        isOutsideCancelable: Boolean = false
    ) {
        commonCheckDialog(
            context = this,
            fm = supportFragmentManager,
            isError = isError,
            isShowDivider = isShowDivider,
            title = title,
            errorMessageSpan = errorMessageSpan,
            buttonText = buttonText,
            positiveClickListener = positiveClickListener,
            isOutsideCancelable = isOutsideCancelable
        )
    }

    fun showPromptDialog(
        title: String?,
        errorMessage: String?,
        buttonText: String?,
        positiveClickListener: () -> Unit?,
        isError: Boolean,
        isShowDivider: Boolean? = false
    ) {
        commonCheckDialog(
            context = this,
            fm = supportFragmentManager,
            isError = isError,
            isShowDivider = isShowDivider,
            title = title,
            errorMessage = errorMessage,
            buttonText = buttonText,
            positiveClickListener = positiveClickListener
        )
    }

    fun showBottomSheetDialog(
        title: String?,
        dataList: List<StatusSheetData>,
        defaultData: StatusSheetData,
        itemClickListener: StatusSheetAdapter.ItemCheckedListener,
    ) {
        val strList: ArrayList<String?> = arrayListOf()
        dataList.forEach { strList.add(it.showName) }

        val defaultPosition = checkDefaultPosition(dataList, defaultData)

        mPickerView = OptionsPickerBuilder(this) { options1, _, _, _ ->
            itemClickListener.onChecked(true, dataList[options1])
        }
            .setItemVisibleCount(4)
            .setBgColor(resources.getColor(R.color.color_666666_bcbcbc))
            .setCancelColor(resources.getColor(R.color.color_e5e5e5_333333))//取消按钮文字颜色
            .setSubmitText(resources.getString(R.string.complete))
            .setCancelText(resources.getString(R.string.btn_cancel))
            .build()
        mPickerView?.setPicker(strList)
        mPickerView?.setSelectOptions(defaultPosition)
        mPickerView?.show()
    }

    private fun checkDefaultPosition(
        dataList: List<StatusSheetData>,
        defaultData: StatusSheetData
    ): Int {
        var defaultPosition = 0
        dataList.forEachIndexed { position, statusSheetData ->
            if (statusSheetData.code == defaultData.code) {
                defaultPosition = position
            }
        }
        return defaultPosition
    }

    private fun safelyUpdateLayout(runnable: Runnable) {
        try {
            mLayoutHandler.post(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun avoidFastDoubleClick() {
        mIsEnabled = false
        Handler().postDelayed({ mIsEnabled = true }, 500)
    }

    private fun startCheckToken() {
        try {
            if (viewModel.loginRepository.isLogin.value == true) {
                if (mRunnable == null) {
                    mRunnable = getRunnable()
                    mRunnable?.let {
                        mHandler.post(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRunnable(): Runnable {
        return Runnable {
            viewModel.viewModelScope.launch {
                viewModel.checkIsUserAlive()
            }
            mRunnable?.let {
                mHandler.postDelayed(it, 30000)
            }
        }
    }

    private fun stopRunnable() {
        mRunnable = null
    }

    /**
     * 修正 auto size 偶發失效問題
     * */
    override fun getResources(): Resources {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        }
        return super.getResources()
    }

}
