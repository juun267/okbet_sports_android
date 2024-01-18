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
import org.cxct.sportlottery.common.extentions.*
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

    val viewModel: T by viewModel(clazz = clazz ?: getKClass(0) as KClass<T>)

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
            {  },
            isError = true,
            hasCancle = false
        )
    }

    private fun onNetworkException() {
        viewModel.networkExceptionUnavailable.observe(this) { netError(it) }

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

    fun onNetworkUnavailable() {
        Toast.makeText(applicationContext, R.string.connect_first, Toast.LENGTH_SHORT).show()
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

    open fun setStatusbar(bgColor: Int = R.color.color_232C4F_FFFFFF, darkFont: Boolean = true) {
        ImmersionBar.with(this).statusBarColor(bgColor)
            .statusBarDarkFont(darkFont)
            .fitsSystemWindows(true)
            .init()
    }

    override fun onDestroy() {
        super.onDestroy()
        LotteryManager.instance.onDestroy(this)
    }

    fun setStatusBarDarkFont(view: View? = null) {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(view)
            .fitsSystemWindows(false)
            .init()
    }

    protected open fun fadeStyle() = true
    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        if (fadeStyle()) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
