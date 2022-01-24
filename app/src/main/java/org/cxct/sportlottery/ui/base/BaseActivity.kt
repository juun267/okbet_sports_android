package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_custom.*
import kotlinx.android.synthetic.main.layout_loading.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.reflect.KClass
import org.cxct.sportlottery.ui.main.MainActivity


abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    private var mLayoutHandler = Handler(Looper.getMainLooper())
    private var mPromptDialog: CustomAlertDialog? = null
    private var mTokenPromptDialog: CustomAlertDialog? = null
    private var mOnNetworkExceptionListener: View.OnClickListener? = null
    private var mPickerView: OptionsPickerView<String>? = null

    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }
    private val bottomSheetView: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.dialog_bottom_sheet_custom, null)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()
    }

    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this) {
            showDialogLogout(it)
        }
    }

    enum class FailType(val code: Int) {
        MAINTENANCE(2611)
    }

    private fun showDialogLogout(result: BaseResult) {
        showTokenPromptDialog(result.msg) {
            viewModel.doLogoutCleanUser {
                if (result.code != FailType.MAINTENANCE.code) {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else
                        GameActivity.reStart(this)
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
                    setTextColor(R.color.colorRed)
                    setTitle(getString(R.string.prompt))
                    setMessage(errorMessage)
                    setPositiveButtonText(getString(R.string.btn_determine))
                    setNegativeButtonText(null)
                    setPositiveClickListener(View.OnClickListener {
                        positiveClickListener()
                        mTokenPromptDialog?.dismiss()
                        mTokenPromptDialog = null
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

    fun showPromptDialog(
        title: String? = getString(R.string.prompt),
        message: String,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, null, positiveClickListener, false)
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
        errorMessage: String?,
        buttonText: String?,
        positiveClickListener: () -> Unit?,
        isError: Boolean,
        isShowDivider: Boolean? = false
    ) {
        safelyUpdateLayout(Runnable {
            try {
                //防止跳出多個 error dialog
                if (mPromptDialog?.isShowing == true)
                    mPromptDialog?.dismiss()
                if (mTokenPromptDialog?.isShowing == true) {
                    mPromptDialog?.dismiss()
                    return@Runnable
                }

                mPromptDialog = CustomAlertDialog(this@BaseActivity).apply {
                    if (isError) {
                        setTextColor(R.color.colorRed)
                    }
                    setShowDivider(isShowDivider)
                    setTitle(title)
                    setMessage(errorMessage)
                    setPositiveButtonText(buttonText ?: getString(R.string.btn_determine))
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
            .setBgColor(resources.getColor(R.color.colorSilver3))
            .setCancelColor(resources.getColor(R.color.colorBlackLight))//取消按钮文字颜色
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

    protected fun safelyUpdateLayout(runnable: Runnable) {
        try {
            mLayoutHandler.post(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
