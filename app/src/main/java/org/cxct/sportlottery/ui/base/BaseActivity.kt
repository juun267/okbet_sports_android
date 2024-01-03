package org.cxct.sportlottery.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.layout_loading.view.*
import kotlinx.android.synthetic.main.view_status_bar.*
import me.jessyan.autosize.AutoSizeCompat
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.getKClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.ui.common.adapter.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.common.dialog.CustomAlertDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.floatingbtn.LotteryManager
import org.cxct.sportlottery.view.floatingbtn.LuckyWheelManager
import org.cxct.sportlottery.view.floatingbtn.PromotionManager
import org.cxct.sportlottery.view.floatingbtn.RedEnvelopeManager
//import org.cxct.sportlottery.util.language.MultiLanguages
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>? = null) : AppCompatActivity() {

    private var mLayoutHandler = Handler(Looper.getMainLooper())
    private var mPromptDialog: CustomAlertDialog? = null
    private var mTokenPromptDialog: CustomAlertDialog? = null
    private var mOnNetworkExceptionListener: View.OnClickListener? = null
    private var mPickerView: OptionsPickerView<String>? = null
    public var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一
    private var mRunnable: Runnable? = null
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    val viewModel: T by viewModel(clazz = clazz ?: getKClass(0) as KClass<T>)

    private var loadingView: View? = null

    val bottomSheet: BottomSheetDialog by lazy { BottomSheetDialog(this) }
    private val bottomSheetView: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.dialog_bottom_sheet_custom, null)
    }

    override fun onStart() {
        super.onStart()
        RedEnvelopeManager.instance.bind(this as BaseActivity<BaseViewModel>)
        LotteryManager.instance.bind(this as BaseActivity<BaseViewModel>)
        LuckyWheelManager.instance.bind(this as BaseActivity<BaseViewModel>)
        PromotionManager.instance.bind(this as BaseActivity<BaseViewModel>)
    }

    private var firstCrate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        firstCrate = true
        super.onCreate(savedInstanceState)

        onTokenStateChanged()
        onNetworkException()
//        ServiceFloatingButton.targetActivity(this)
        if (BuildConfig.DEBUG) {
            Log.e("For Test", "======>>> Activity ${this::class.java.name}")
        }
    }

    private fun onTokenStateChanged() {
        viewModel.errorResultToken.observe(this) {

            if (this is MaintenanceActivity) return@observe
            val result = it.getContentIfNotHandled() ?: return@observe
            if (result.code == HttpError.BALANCE_IS_LOW.code) {
                ToastUtil.showToast(this, result.msg)
            } else {
                toMaintenanceOrShowDialog(result)
            }
        }
    }

    private fun toMaintenanceOrShowDialog(result: BaseResult) {
        when (result.code) {
//            HttpError.KICK_OUT_USER.code,
//            HttpError.UNAUTHORIZED.code,
//            HttpError.DO_NOT_HANDLE.code -> { // 鉴权失败、token过期
//
//            }

            HttpError.MAINTENANCE.code -> {
                startActivity(Intent(this, MaintenanceActivity::class.java))
                finish()
            }

            else -> {
                if (this is MaintenanceActivity
                    || this is SplashActivity) {
                    return
                }
                viewModel.doCleanToken()
                showTokenPromptDialog(result.msg) {
                    viewModel.doLogoutCleanUser {
                        if (isErrorTokenToMainActivity()) {
                            MainTabActivity.reStart(this)
                        }
                    }
                }
            }
        }
    }


    private fun isErrorTokenToMainActivity(): Boolean {
        return this !is MaintenanceActivity && this !is SplashActivity && this !is LaunchActivity
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
                if (this.javaClass.simpleName == MaintenanceActivity::class.java.simpleName ||
                    this.javaClass.simpleName == ThirdGameActivity::class.java.simpleName
                ) return@observe
                viewModel.doCleanToken()
                showTokenPromptDialog(msg) {
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

        val animationDrawable = loadingView?.ivLoading?.background as AnimationDrawable
        animationDrawable.start()
//        loadingView?.pb_message?.text = message ?: getString(R.string.loading)
    }

    /*关闭加载界面*/
    open fun hideLoading() {
        if (loadingView == null) {
            Timber.d("loadingView不存在")
        } else {
            loadingView?.rl_loading?.visibility = View.GONE
            (loadingView?.ivLoading?.background as AnimationDrawable).stop()
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
        try {
            //防止跳出多個 error dialog
            if (mTokenPromptDialog != null)
                return

            mTokenPromptDialog = CustomAlertDialog(this@BaseActivity).apply {
                setTextColor(R.color.color_E44438_e44438)
                setTitle(this@BaseActivity.getString(R.string.prompt))
                setMessage(errorMessage)
                setPositiveButtonText(this@BaseActivity.getString(R.string.btn_confirm))
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                isCancelable = false //不能用系統 BACK 按鈕關閉 dialog
                setPositiveClickListener {
                    positiveClickListener()
                    mTokenPromptDialog?.dismiss()
                    mTokenPromptDialog = null
                }
            }

            if (!supportFragmentManager.isDestroyed) mTokenPromptDialog?.show(
                supportFragmentManager,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    fun showErrorPromptDialog(title: String, message: Spanned,hasCancel: Boolean, positiveClickListener: () -> Unit?) {
        showPromptDialog(title, message, null, positiveClickListener, true,hasCancel)
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
        }
            .setItemVisibleCount(4)
            .setTitleBgColor(resources.getColor(R.color.color_2B2B2B_e2e2e2))
            .setBgColor(resources.getColor(R.color.color_191919_FCFCFC))
            .setCancelColor(resources.getColor(R.color.color_7F7F7F_999999))//取消按钮文字颜色
            .setSubmitColor(resources.getColor(R.color.color_0760D4))//取消按钮文字颜色
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

    fun avoidFastDoubleClick() {
        mIsEnabled = false
        mHandler.postDelayed({ mIsEnabled = true }, 100)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        firstCrate = true
        super.onConfigurationChanged(newConfig)
    }

    private val localeResources by lazy { ResourceWrapper(this@BaseActivity, super.getResources()) }

    /**
     * 修正 auto size 偶發失效問題
     * */
    override fun getResources(): Resources {
        val res = localeResources
        if (firstCrate && Looper.myLooper() == Looper.getMainLooper()) {
            firstCrate = false
            AutoSizeCompat.autoConvertDensityOfGlobal(res)
        }
        return res
    }

//    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(MultiLanguages.attach(newBase))
//    }

    open fun setStatusbar(bgColor: Int = R.color.color_232C4F_FFFFFF, darkFont: Boolean = true) {
        ImmersionBar.with(this).statusBarColor(bgColor)
            .statusBarDarkFont(darkFont)
            .fitsSystemWindows(true)
            .init()
    }

    override fun onDestroy() {
        super.onDestroy()
        LotteryManager.instance.onDestroy(this)
        mHandler.removeCallbacksAndMessages(null)
    }

    fun setStatusBarDarkFont(view: View? = null) {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(view)
            .fitsSystemWindows(false)
            .init()
    }


    fun replaceFragment(container: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction();
        transaction.replace(container, fragment)
        transaction.commit()
    }

    protected open fun fadeStyle() = true
    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        if (fadeStyle()) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
