package org.cxct.sportlottery.ui.thirdGame

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.view.View
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityThirdGameBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.common.WebActivity.Companion.FIRM_CODE
import org.cxct.sportlottery.ui.common.WebActivity.Companion.GAME_CATEGORY_CODE
import org.cxct.sportlottery.ui.common.WebActivityImp
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.isThirdTransferOpen
import org.cxct.sportlottery.util.jumpToDeposit
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.dialog.ToGcashDialog

open class ThirdGameActivity : BaseActivity<MainViewModel, ActivityThirdGameBinding>() {

    private val Int.dp: Int
        get() = (this * density).toInt()
    private var density: Float = 0f

    private var mUserInfo: UserInfo? = null

    private val firmCode by lazy { intent.getStringExtra(FIRM_CODE) }
    private val gameType by lazy { intent.getStringExtra(GAME_CATEGORY_CODE) }
    private val mUrl: String by lazy { intent?.getStringExtra(WebActivity.KEY_URL) ?: "about:blank" }

    private val webActivityImp by lazy { WebActivityImp(this,this::overrideUrlLoading) }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setStatuEnable(ORIENTATION_PORTRAIT == newConfig.orientation)
    }

    private fun setStatuEnable(enable: Boolean) {

        if (enable) {

            10.dp.let { binding.toolBar.setPadding(it, 0, it, 0) }
            val dp28 = 28.dp
            changeViewWH(binding.toolBar, -1, 40.dp)
            changeViewWH(binding.ivBack, dp28, dp28)
            changeViewWH(binding.ivDeposit, dp28, dp28)
            changeViewWH(binding.ivLogo, 68.dp, dp28)

            ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_SHOW_BAR)
                .statusBarColor(R.color.white)
                .statusBarDarkFont(true)
                .fitsSystemWindows(true)
                .init()
        } else {

            33.dp.let { binding.toolBar.setPadding(it, 0, it, 0) }
            val dp20 = 20.dp
            changeViewWH(binding.toolBar, -1, 28.dp)
            changeViewWH(binding.ivBack, dp20, dp20)
            changeViewWH(binding.ivDeposit, dp20, dp20)
            changeViewWH(binding.ivLogo, 48.dp, dp20)

            ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .fitsSystemWindows(false)
                .init()
        }
    }

    private fun changeViewWH(view: View, width: Int, height: Int) {
        val lp = view.layoutParams
        lp.width = width
        lp.height = height
        view.layoutParams = lp
    }

    override fun onInitView()=binding.run {
        density = resources.displayMetrics.density
        setStatuEnable(ORIENTATION_PORTRAIT ==resources.configuration.orientation)
        ivBack.setOnClickListener { finish() }
        webActivityImp.setCookie(mUrl)
        webActivityImp.setupWebView(webView)
        webView.loadUrl(mUrl)
        setupMenu()
        initObserve()
        postRefreshToken() // 避免用户长期在三方游戏中导致token过期
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleScope) {
            if (it.maintain == 1 && firmCode == it.firmType /*&& gameType == it.gameType*/) {
//                motionMenu.gone()
                showErrorPromptDialog(getString(R.string.error), getString(R.string.hint_game_maintenance)) {
                    finish()
                }
            }
        }
    }
     fun overrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.isEmptyStr()) {
            view.loadUrl(url)
            return false
        }

        val requestUrl = url.replace("https", "http", true)
        val host = Constants.getBaseUrl().replace("https", "http", true)
        if (requestUrl.startsWith(host, true)) {
            finish()
            return true
        }
         view.loadUrl(url)
        return false
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            super.onBackPressed()
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseRefreshToken()
        binding.webView.destroy()
        if (isThirdTransferOpen()) {
            LoginRepository.allTransferOut()
        }
    }

    private fun setupMenu() {
        binding.ivDeposit.setOnClickListener {
            ToGcashDialog.showByClick { jumpToDeposit() }
        }
//        binding.motionMenu.setOnMenuListener(object : MotionFloatingMenu.OnMenuListener {
//            override fun onHome() {
//                finish()
//            }
//
//            override fun onCashSave() {
//                if (checkLogin()) {
//                    ToGcashDialog.showByClick {
//                        viewModel.checkRechargeKYCVerify()
//                    }
//                }
//            }
//
//            override fun onCashGet() {
//                if (checkLogin()) {
//                    viewModel.checkWithdrawKYCVerify()
//                }
//            }
//        })
    }


    private fun checkLogin(): Boolean {
        return when (mUserInfo?.testFlag) {
            TestFlag.NORMAL.index -> true
            TestFlag.TEST.index -> true // TODO 20221208 增加了內部測試選項
            TestFlag.GUEST.index -> {
                ToastUtil.showToastInCenter(this, resources.getString(R.string.message_guest_no_permission))
                false
            }
            else -> {
                startLogin()
                false
            }
        }
    }

    private fun initObserve() {
        viewModel.userInfo.observe(this) {
            mUserInfo = it
        }
    }

    private val refreshTokenDuring = 5 * 60_000L
    private val refreshTokenRunnable = Runnable {
        lifecycleScope.launch { runWithCatch { UserInfoRepository.getUserInfo() } }
        postRefreshToken()
    }

    private fun postRefreshToken() {
        if (LoginRepository.isLogined() && !isDestroyed) {
            binding.root.postDelayed(refreshTokenRunnable, refreshTokenDuring)
        }
    }

    private fun releaseRefreshToken() {
        binding.root.removeCallbacks(refreshTokenRunnable)
    }

}
