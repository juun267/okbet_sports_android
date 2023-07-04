package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentWorldcupBinding
import org.cxct.sportlottery.databinding.FragmentWorldcupGameBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebViewClient
import org.cxct.sportlottery.view.webView.WebViewCallBack
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class WorldCupGameFragment : BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {


    private lateinit var binding: FragmentWorldcupGameBinding


    private inline fun mainTabActivity() = activity as MainTabActivity

    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentWorldcupGameBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        ImmersionBar.with(this)
            .fullScreen(true)
            .statusBarDarkFont(false)
            .init()
        initToolBar()
        initObservable()
        initWeb()
        loadWebURL()
    }
    fun initToolBar() = binding.run {
        homeToolbar.attach(this@WorldCupGameFragment, mainTabActivity(), viewModel)
        homeToolbar.setBackgroundResource(R.drawable.bg_title_fiba_game)
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
            }

            override fun pageFinished(view: View?, url: String?) {
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
            }

        }

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
    private fun loadWebURL() {
        val url = Constants.getWorldCupActivityH5Url(requireContext(),viewModel.token?:"")
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
        binding.okWebView.onPause()
    }
    fun reloadWeb() {
        loadWebURL()
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

        viewModel.oddsType.observe(viewLifecycleOwner) { loadWebURL() }
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
          //todo
        }else{
            binding.homeToolbar.onRefreshMoney()
            reloadWeb()
        }
    }
    fun overrideUrlLoading(view: WebView, url: String): Boolean {
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


}