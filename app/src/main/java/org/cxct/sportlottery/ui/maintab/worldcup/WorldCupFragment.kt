package org.cxct.sportlottery.ui.maintab.worldcup

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.android.synthetic.main.fragment_worldcup.*
import kotlinx.android.synthetic.main.fragment_worldcup.homeToolbar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentWorldcupBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebViewClient
import org.cxct.sportlottery.view.webView.WebViewCallBack
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class WorldCupFragment : BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {


    private lateinit var binding: FragmentWorldcupBinding


    private inline fun mainTabActivity() = activity as MainTabActivity

    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentWorldcupBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        ImmersionBar.with(this)
            .fullScreen(true)
            .statusBarDarkFont(false)
            .init()
        initToolBar()
        initObservable()
        initWeb()
        loadWebURL(MultiLanguagesApplication.mInstance.sOddsType)
        homeBottumView.bindServiceClick(childFragmentManager)
    }
    fun initToolBar() = binding.run {
        homeToolbar.attach(this@WorldCupFragment, mainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            mainTabActivity().showMainLeftMenu(null)
        }
    }
    var isInitedWeb = false
    private fun initWeb() =binding.okWebView.run {
        isInitedWeb = true
        setOnTouchListener { view, p1 ->
            (view as WebView).requestDisallowInterceptTouchEvent(true)
            false
        }
        webChromeClient = object : OkWebChromeClient(){}
        webViewClient = object : OkWebViewClient(object : WebViewCallBack {

            override fun pageStarted(view: View?, url: String?) {
                loading()
            }

            override fun pageFinished(view: View?, url: String?) {
                hideLoading()
                binding.okWebView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }

            override fun onError() {
            }
        }) {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return overrideUrlLoading(view, url)
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                handler.proceed()
//                AlertDialog.Builder(requireContext())
//                    .setMessage(android.R.string.httpErrorUnsupportedScheme).setPositiveButton(
//                        "continue"
//                    ) { dialog, which -> handler.proceed() }.setNegativeButton(
//                        "cancel"
//                    ) { dialog, which -> handler.cancel() }.create().show()
            }

        }
        addJavascriptInterface(WorldCupJsInterface(this@WorldCupFragment),
            WorldCupJsInterface.name)

    }

    fun setCookie(url:String) {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val oldCookie = cookieManager.getCookie(url)
            Timber.i("Cookie:oldCookie:$oldCookie")

            cookieManager.setCookie(
                url, "x-session-token=" + URLEncoder.encode(viewModel.token, "utf-8")
            ) //cookies是在HttpClient中获得的cookie
            cookieManager.flush()

            val newCookie = cookieManager.getCookie(url)
            Timber.i("Cookie:newCookie:$newCookie")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
    var currentOdsType: String? = null

    private fun loadWebURL(oddsType: String?) {
        if (currentOdsType != null && currentOdsType.equals(oddsType)) {
            return
        }
        currentOdsType = oddsType
        val url = Constants.getWorldCupH5Url(requireContext(),"8TIPs2KPjJUqkMuQ/L5Vy7cOSJ1Z1tlozP551w==")
        setCookie(url)
        LogUtil.d("url="+url)
        binding.okWebView.loadUrl(url)
    }


    override fun onResume() {
        super.onResume()
        binding.okWebView.onResume()
    }

    override fun onPause() {
        super.onPause()
        pauseWebVideo()
        binding.okWebView.onPause()
    }
    private fun pauseWebVideo() {
        try {
            binding.okWebView.loadUrl("javascript:window._player.stop()")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playWebVideo() {
        try {
            binding.okWebView.loadUrl("javascript:window._player.play()")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadWeb() {
        showLoading()
        binding.okWebView.reload()
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

        viewModel.oddsType.observe(viewLifecycleOwner) { loadWebURL(it.code) }
        viewModel.isLogin.observe(viewLifecycleOwner) {
//            if (!it) mainTabActivity().startLogin()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.okWebView.destroy()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            pauseWebVideo()
        }else{
            playWebVideo()
//            homeToolbar.onRefreshMoney()
        }
    }
    protected open fun overrideUrlLoading(view: WebView, url: String): Boolean {
        if (!url.startsWith("http")) {
            try {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                i.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }

        view.loadUrl(url)
        return true
    }
   open class WorldCupJsInterface(val fragment: WorldCupFragment) {

        companion object {
            const val name = "app"
        }
       @JavascriptInterface
       fun toBetRecord() {
           LogUtil.d("toBetRecord")
           (fragment.activity as MainTabActivity)?.jumpToBetInfo(1)
       }
       @JavascriptInterface
       fun toLogin() {
           LogUtil.d("toLogin")
           (fragment.activity as MainTabActivity)?.startLogin()
       }
        @JavascriptInterface
        fun toDetailEndScore(matchInfoJson: String) {
            LogUtil.d("toDetailEndScore")
            if (TextUtils.isEmpty(matchInfoJson)) {
                ToastUtil.showToast(fragment.requireContext(), R.string.error)
                return
            }
            val matchInfo: MatchInfo? = matchInfoJson.fromJson()
            if (matchInfo == null) {
                ToastUtil.showToast(fragment.requireContext(), R.string.error)
                return
            }
            SportDetailActivity.startActivity(fragment.mainTabActivity(), matchInfo)
        }
    }

}