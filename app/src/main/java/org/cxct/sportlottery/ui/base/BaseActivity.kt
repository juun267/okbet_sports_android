package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.layout_loading.ivLoading
import kotlinx.android.synthetic.main.layout_loading.view.*
import kotlinx.android.synthetic.main.view_status_bar.*
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeCompat
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.ui.common.adapter.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.LotteryManager
import org.cxct.sportlottery.util.RedEnvelopeManager
import org.cxct.sportlottery.util.ResourceWrapper
import org.cxct.sportlottery.util.commonCheckDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    private var mLayoutHandler = Handler(Looper.getMainLooper())
    private var mPromptDialog: CustomAlertDialog? = null
    private var mTokenPromptDialog: CustomAlertDialog? = null
    private var mOnNetworkExceptionListener: View.OnClickListener? = null
    private var mPickerView: OptionsPickerView<String>? = null
    public var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一
    private var mRunnable: Runnable? = null
    var hasHandler = false
    private val mHandler by lazy {
        hasHandler = true
        Handler(Looper.getMainLooper())
    }

    val viewModel: T by viewModel(clazz = clazz)

    private var loadingView: View? = null

    val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }
    private val bottomSheetView: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.dialog_bottom_sheet_custom, null)
    }

    override fun onStart() {
        super.onStart()
        startCheckToken()
        RedEnvelopeManager.instance.bind(this as BaseActivity<BaseViewModel>)
        LotteryManager.instance.bind(this as BaseActivity<BaseViewModel>)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()

        if (BuildConfig.DEBUG) {
            Log.e("For Test", "======>>> Activity ${this::class.java.name}")
        }
    }

    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this) {
            if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName) return@observe
            if (it.code != HttpError.KICK_OUT_USER.code) toMaintenanceOrShowDialog(it)
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
                if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName) return
                showTokenPromptDialog(result.msg) {
                    viewModel.doLogoutCleanUser {
//                        if (sConfigData?.thirdOpen == FLAG_OPEN)
//                            MainActivity.reStart(this)
//                        else
                        MainTabActivity.reStart(this)
                    }
                }
            }
        }
    }

    private fun netError(errorMessage: String) {
        hideLoading()
        showPromptDialog(
            getString(R.string.prompt),
            errorMessage,
            buttonText = null,
            { mOnNetworkExceptionListener?.onClick(null) },
            isError = true,
            hasCancle = false
        )
    }

    private fun onNetworkException() {
        viewModel.networkExceptionUnavailable.observe(this) { netError(it) }

//        viewModel.networkExceptionTimeout.observe(this) { netError(it) }
//
//        viewModel.networkExceptionUnknown.observe(this)  { netError(it) }

        viewModel.isKickedOut.observe(this) {
            hideLoading()
            it.getContentIfNotHandled()?.let { msg ->
                if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName || this.javaClass.simpleName == ThirdGameActivity::class.java.simpleName) return@observe
                showTokenPromptDialog(msg) {
                    viewModel.loginRepository._isLogin.postValue(false)
                    viewModel.doLogoutCleanUser {
                        run {
                            MainTabActivity.reStart(this)
                        }
                    }
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
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
            )
            addContentView(loadingView, params)
        } else {
            loadingView?.rl_loading?.visibility = View.VISIBLE
            loadingView?.rl_loading?.isClickable = true
        }
        loadingView?.ivLoading?.setBackgroundResource(R.drawable.anim_loading)
        val animationDrawable = loadingView?.ivLoading?.background as AnimationDrawable
        animationDrawable.start()
        loadingView?.pb_message?.text = message ?: getString(R.string.loading)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
//        if (loadingView == null) {
//            Timber.d("loadingView不存在")
//        } else {
//            loadingView?.rl_loading?.visibility = View.GONE
//            (loadingView?.ivLoading?.background as AnimationDrawable).stop()
//        }
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
                if (mTokenPromptDialog?.isShowing == true) return@Runnable

                mTokenPromptDialog = CustomAlertDialog(this@BaseActivity).apply {
                    setTextColor(R.color.color_E44438_e44438)
                    setTitle(this@BaseActivity.getString(R.string.prompt))
                    setMessage(errorMessage)
                    setPositiveButtonText(this@BaseActivity.getString(R.string.btn_confirm))
                    setNegativeButtonText(null)
                    setPositiveClickListener(View.OnClickListener {
                        positiveClickListener()
                        mTokenPromptDialog?.dismiss()
                        mTokenPromptDialog = null
                    })

                    setCanceledOnTouchOutside(false)
                    isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
                }
                if (!supportFragmentManager.isDestroyed) mTokenPromptDialog?.show(
                    supportFragmentManager, null
                )
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

    fun showPromptDialogNoCancel(
        title: String? = getString(R.string.prompt),
        message: String,
        positiveClickListener: () -> Unit?
    ) {
        showPromptDialogNoCancel(title, message, null, positiveClickListener, false)
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
        showPromptDialog(
            title,
            message,
            null,
            positiveClickListener,
            false,
            isOutsideCancelable = isOutsideCancelable
        )
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
        showErrorPromptDialog(getString(R.string.prompt), message, positiveClickListener)
    }

    fun showErrorPromptDialog(title: String, message: String, positiveClickListener: () -> Unit?) {
        showPromptDialog(title, message, null, positiveClickListener, true)
    }

    fun showErrorPromptDialog(
        title: String, message: Spanned, hasCancel: Boolean, positiveClickListener: () -> Unit?
    ) {
        showPromptDialog(title, message, null, positiveClickListener, true, hasCancel)
    }

    fun showPromptDialog(
        title: String?,
        errorMessageSpan: Spanned,
        buttonText: String?,
        positiveClickListener: () -> Unit?,
        isError: Boolean,
        hasCancle: Boolean = true,
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
            negativeText = null,
            isOutsideCancelable = isOutsideCancelable
        )
    }

    fun showPromptDialog(
        title: String?,
        errorMessage: String?,
        buttonText: String?,
        positiveClickListener: () -> Unit?,
        isError: Boolean,
        isShowDivider: Boolean? = false,
        hasCancle: Boolean = false,
    ) {
        commonCheckDialog(
            context = this,
            fm = supportFragmentManager,
            isError = isError,
            isShowDivider = isShowDivider,
            title = title,
            errorMessage = errorMessage,
            buttonText = buttonText,
            positiveClickListener = positiveClickListener,
            negativeText = if (hasCancle) getString(R.string.btn_cancel) else null,
        )
    }

    fun showPromptDialogNoCancel(
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
            positiveClickListener = positiveClickListener,
            negativeText = null
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
        }.setItemVisibleCount(4).setTitleBgColor(resources.getColor(R.color.color_2B2B2B_e2e2e2))
            .setBgColor(resources.getColor(R.color.color_191919_FCFCFC))
            .setCancelColor(resources.getColor(R.color.color_7F7F7F_999999))//取消按钮文字颜色
            .setSubmitColor(resources.getColor(R.color.color_0760D4))//取消按钮文字颜色
            .setSubmitText(resources.getString(R.string.complete))
            .setCancelText(resources.getString(R.string.btn_cancel)).build()
        mPickerView?.setPicker(strList)
        mPickerView?.setSelectOptions(defaultPosition)
        mPickerView?.show()
    }

    private fun checkDefaultPosition(
        dataList: List<StatusSheetData>, defaultData: StatusSheetData
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
        mHandler.postDelayed({ mIsEnabled = true }, 500)
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

    private val localeResources by lazy { ResourceWrapper(this@BaseActivity, super.getResources()) }

    /**
     * 修正 auto size 偶發失效問題
     * */
    override fun getResources(): Resources {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfGlobal(localeResources)
        }
        return localeResources
    }

    open fun setStatusbar(bgColor: Int, darkFont: Boolean) {
        ImmersionBar.with(this).statusBarColor(bgColor).statusBarDarkFont(darkFont)
            .fitsSystemWindows(true).init()
    }

    override fun onDestroy() {
        super.onDestroy()
        LotteryManager.instance.onDestroy(this)
        if (hasHandler) {
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    fun setStatusBarDarkFont(view: View? = null) {
        ImmersionBar.with(this).statusBarDarkFont(true).transparentStatusBar().statusBarView(view)
            .fitsSystemWindows(false).init()
    }


    fun replaceFragment(container: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction();
        transaction.replace(container, fragment)
        transaction.commit()
    }
}
