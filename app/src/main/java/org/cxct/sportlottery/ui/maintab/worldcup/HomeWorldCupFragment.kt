package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home_live.lin_toolbar
import kotlinx.android.synthetic.main.fragment_home_live.rv_tab_home
import kotlinx.android.synthetic.main.fragment_home_worldcup.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.elec.HomeElecAdapter
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.observe

class HomeWorldCupFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val homeTabAdapter by lazy { HomeWorldCupTabAdapter().apply {
            setOnItemClickListener { adapter, view, position ->
                if (position == 0) {
                    return@setOnItemClickListener
                }
                if (position == 1) { //体育
                    (parentFragment as HomeFragment).onTabClickByPosition(3)
                    return@setOnItemClickListener
                }

                if (position == 2) { //老虎机
                    (parentFragment as HomeFragment).onTabClickByPosition(4)
                    return@setOnItemClickListener
                }

                if (position == 3) { //棋牌
                    (parentFragment as HomeFragment).onTabClickByPosition(5)
                    return@setOnItemClickListener
                }

            }
        }
    }

    override fun layoutId() = R.layout.fragment_home_worldcup

    override fun onBindView(view: View) {
        viewModel.getConfigData()
        initToolBar()
        setTheme()
        initTabView()
        initObservable()
        initWeb()
    }

    private fun initWeb() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(WorldCupJsInterface(), "WorldCupJsInterface")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }

      webView.loadUrl("https://okbet-v2.cxsport.net/sports-rule/#/worldcup?platform=OKbet")
//        webView.loadUrl(Constants.getWorldCupH5Url(requireContext()))
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
        iv_menu_left.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
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
        }
    }


    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
    }
}
