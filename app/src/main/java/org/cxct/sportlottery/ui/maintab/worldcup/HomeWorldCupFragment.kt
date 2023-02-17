package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home_live.lin_toolbar
import kotlinx.android.synthetic.main.fragment_home_live.rv_tab_home
import kotlinx.android.synthetic.main.fragment_home_worldcup.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.HomeTabEvent
import org.cxct.sportlottery.event.MainTabEvent
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.startLogin
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class HomeWorldCupFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val worldCupPosition = 2
    private val homeTabAdapter by lazy { HomeWorldCupTabAdapter().apply {
            setOnItemClickListener { adapter, view, position ->
                if (position == worldCupPosition) { //世界杯
                    return@setOnItemClickListener
                }

                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }

    override fun layoutId() = R.layout.fragment_home_worldcup

    override fun onBindView(view: View) {
        initToolBar()
        setTheme()
        initTabView()
        initObservable()
        initWeb(view)
        EventBusUtil.targetLifecycle(this)
    }

    var isInitedWeb = false
    private fun initWeb(view: View) {
        isInitedWeb = true
        Glide.with(ivBg).load(R.drawable.bg_worldcup_top_0).into(ivBg)

//        view.findViewById<View>(R.id.rl_loading).setBackgroundColor(0)
        loading.isVisible = true
        webView.setBackgroundColor(0)
        webView.webChromeClient = MyWebChromeClient()
        webView.settings.run {
            javaScriptEnabled = true
            domStorageEnabled = true
            setJavaScriptCanOpenWindowsAutomatically(true)
            setUseWideViewPort(true); // 关键点
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                loading.isVisible = false
            }
        }

        webView.addJavascriptInterface(WorldCupJsInterface(webView.context), WorldCupJsInterface.name)
        val url = Constants.getWorldCupH5Url(requireContext())
        webView.loadUrl(url)
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null

        override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
            super.onShowCustomView(view, callback)
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            mCustomView = view
            (requireActivity().window.decorView as ViewGroup).addView(mCustomView)
            mCustomViewCallback = callback
            webView.setVisibility(View.GONE)
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }

        override fun onHideCustomView() {
            webView.setVisibility(View.VISIBLE)
            if (mCustomView == null) {
                return
            }
            mCustomView!!.visibility = View.GONE
            (requireActivity().window.decorView as ViewGroup).removeView(mCustomView)
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomView = null
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            super.onHideCustomView()
        }
    }

    override fun onConfigurationChanged(config: Configuration) {
        super.onConfigurationChanged(config)
        when (config.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        pauseWebVideo()
        webView.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTableChanged(event: HomeTabEvent) {
        if (!event.isWorldCupTab()) {
            pauseWebVideo()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTableChanged(event: MainTabEvent) {
        if (!event.isHomeTab()) {
            pauseWebVideo()
        }
    }

    private fun pauseWebVideo() {
        try {
            webView.loadUrl("javascript:window._player.stop()")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playWebVideo() {
        try {
            webView.loadUrl("javascript:window._player.play()")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadWeb() {
        loading.isVisible = true
        webView.reload()
    }

    private fun setTheme() {
        iv_menu_left.setImageResource(R.drawable.icon_menu_withe)
        iv_money_refresh.setImageResource(R.drawable.ic_refresh_withe)
        tv_home_money.setTextColor(Color.WHITE)
        iv_logo.setImageResource(R.drawable.logo_okbet_withe)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getRecommend()
        }
    }


    fun initToolBar() {
        lin_toolbar.fitsSystemStatus()
        rv_tab_home.fitsSystemStatus()
        lin_toolbar.setBackgroundColor(Color.TRANSPARENT)
        iv_menu_left.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(1)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_login.setOnClickListener {
            requireActivity().startLogin()
        }
        iv_money_refresh.setOnClickListener {
            iv_money_refresh.startAnimation(RotateAnimation(0f,
                720f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
                duration = 1000
            })
            viewModel.getMoney()
        }
        setupLogin()
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

        viewModel.isLogin.observe(viewLifecycleOwner) { setupLogin() }
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
    }

    private fun initTabView() {
        with(rv_tab_home) {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeTabAdapter
            }

//            scrollToPosition(worldCupPosition)
        }
    }


    private fun setupLogin() {
        btn_login.text = "${getString(R.string.btn_register)} / ${getString(R.string.btn_login)}"
        viewModel.isLogin.value?.let {
            btn_login.isVisible = !it
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.destroy()
    }
}
