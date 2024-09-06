package org.cxct.sportlottery.ui.thirdGame

import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.view.View
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityThirdGameBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.common.WebActivity.Companion.FIRM_TYPE
import org.cxct.sportlottery.ui.common.WebActivity.Companion.GAME_BEAN
import org.cxct.sportlottery.ui.common.WebActivity.Companion.GUESTLOGIN
import org.cxct.sportlottery.ui.common.WebActivityImp
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.ToGcashDialog

class ThirdGameActivity : BaseActivity<MainViewModel, ActivityThirdGameBinding>() {
    override fun pageName() = "三方游戏页面"

    private val Int.dp: Int
        get() = (this * density).toInt()
    private var density: Float = 0f

    private var mUserInfo: UserInfo? = null

    private val firmType by lazy { intent.getStringExtra(FIRM_TYPE) }
    private val okGameBean by lazy { intent.getParcelableExtra(GAME_BEAN) as? OKGameBean }
    private val guestLogin by lazy { intent.getBooleanExtra(GUESTLOGIN, false) }
    private val mUrl: String by lazy { intent?.getStringExtra(WebActivity.KEY_URL) ?: "about:blank" }

    private val webActivityImp by lazy { WebActivityImp(this,this::overrideUrlLoading) }
    private val depositURL = sConfigData?.gameUserDepositURL
    private var startLoadTime: Long = 0

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setStatuEnable(ORIENTATION_PORTRAIT == newConfig.orientation)
    }

    private fun setStatuEnable(enable: Boolean) {

        if (enable) {

            10.dp.let { binding.toolBar.setPadding(it, 0, it, 0) }
            val childHeight = 28.dp
            val toolBarHeight = 40.dp
            changeViewWH(binding.toolBar, -1, toolBarHeight)
            changeViewWH(binding.ivBack, childHeight, childHeight)
            changeViewWH(binding.ivDeposit, childHeight, childHeight)
            changeViewWH(binding.ivLogo, -2, childHeight)
            changeViewWH(binding.tvLogin,-2, childHeight)
            changeViewWH(binding.tvRegist,-2, childHeight)

            ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_SHOW_BAR)
                .statusBarColor(R.color.white)
                .statusBarDarkFont(true)
                .fitsSystemWindows(true)
                .init()
        } else {
            33.dp.let { binding.toolBar.setPadding(it, 0, it, 0) }
            val childHeight = if(guestLogin) 28.dp else 20.dp
            val toolBarHeight = if(guestLogin) 40.dp else 28.dp
            changeViewWH(binding.toolBar, -1, toolBarHeight)
            changeViewWH(binding.ivBack, childHeight, childHeight)
            changeViewWH(binding.ivDeposit, childHeight, childHeight)
            changeViewWH(binding.ivLogo, -2, childHeight)
            changeViewWH(binding.tvLogin, -2, childHeight)
            changeViewWH(binding.tvRegist,-2, childHeight)

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
        ivDeposit.isVisible = !guestLogin
        tvLogin.isVisible = guestLogin
        tvRegist.isVisible = guestLogin
        webActivityImp.setCookie(mUrl)
        webActivityImp.setupWebView(webView)
        webActivityImp.onLoadEnd = ::onLoadEndEvent
        webView.loadUrl(mUrl)
        startLoadEvent()
        setupMenu()
        initObserve()
        postRefreshToken() // 避免用户长期在三方游戏中导致token过期
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleScope) {
            if (it.maintain == 1 && firmType == it.firmType /*&& gameType == it.gameType*/) {
//                motionMenu.gone()
                showErrorPromptDialog(getString(R.string.error), getString(R.string.hint_game_maintenance)) {
                    finish()
                }
            }
        }
    }

    private fun startLoadEvent() {
        startLoadTime = System.currentTimeMillis()
        SensorsEventUtil.startLoadingGame("${okGameBean?.firmName}", "${okGameBean?.gameType}", "${okGameBean?.gameName}", okGameBean?.id.toString())
    }

    private fun onLoadEndEvent(result: Boolean) {
        SensorsEventUtil.onGameLoadingResult(
            "${okGameBean?.firmName}",
            "${okGameBean?.gameType}",
            "${okGameBean?.gameName}",
            okGameBean?.id.toString(),
            loadingDuration = ((System.currentTimeMillis() - startLoadTime) / 1000).toInt(),
            loadingResult = result
        )
    }

     private fun overrideUrlLoading(view: WebView, url: String): Boolean {
        if (url.isEmptyStr()) {
            view.loadUrl(url)
            return false
        }

         if (!depositURL.isNullOrEmpty() && url.endsWith(depositURL, true)) {
             jumpToDeposit("游戏内充值跳转")
             return true
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
        if (!OKGamesRepository.isSingleWalletType(firmType)&&isThirdTransferOpen()) {
            LoginRepository.allTransferOut()
        }
    }

    private fun setupMenu() =binding.run{
        ivDeposit.setOnClickListener {
            //需要判断用户未登录的情况
            if (LoginRepository.isLogined()){
                jumpToDeposit("顶部充值按钮")
            }else{
                startLogin()
            }
        }
        tvLogin.clickDelay {
            OKGamesRepository.enterGameAfterLogin= okGameBean
            startLogin()
        }
        tvRegist.clickDelay {
            OKGamesRepository.enterGameAfterLogin= okGameBean
            LoginOKActivity.startRegist(this@ThirdGameActivity)
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
