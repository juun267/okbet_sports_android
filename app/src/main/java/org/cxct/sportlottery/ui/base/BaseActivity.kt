package org.cxct.sportlottery.ui.base

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import me.jessyan.autosize.AutoSizeCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.createVBinding
import org.cxct.sportlottery.common.extentions.getKClass
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.view.floatingbtn.LotteryManager
import org.cxct.sportlottery.view.floatingbtn.LuckyWheelManager
import org.cxct.sportlottery.view.floatingbtn.PromotionManager
import org.cxct.sportlottery.view.floatingbtn.RedEnvelopeManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.reflect.KClass

/**
 * 框架封装，共用方法调用，这里不放业务相关的代码
 */
abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding>(clazz: KClass<VM>? = null) : AppCompatActivity()  {

    val viewModel: VM by viewModel(clazz = clazz ?: getKClass(0) as KClass<VM>)
    protected val binding: VB by lazy { createVBinding(layoutInflater, 1) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.setLocal(newBase))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        firstCrate = true
        super.onConfigurationChanged(newConfig)
        LanguageManager.onConfigurationChanged(this)
    }

    override fun onStart() {
        super.onStart()
        RedEnvelopeManager.instance.bind(this)
        LotteryManager.instance.bind(this)
        LuckyWheelManager.instance.bind(this)
        PromotionManager.instance.bind(this)
    }

    private var firstCrate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        firstCrate = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onInitView()
        onInitData()
//        ServiceFloatingButton.targetActivity(this)
    }
    protected abstract fun onInitView()

    protected open fun onInitData() { }

    /**
     * 修正 auto size 偶發失效問題
     * */
    override fun getResources(): Resources {
        val res = super.getResources()
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

    fun setStatusBarDarkFont(isDarkFont: Boolean = true, view: View? = null) {
        ImmersionBar.with(this)
            .statusBarDarkFont(isDarkFont)
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
