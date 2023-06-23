package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.fromJson
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
        okWebView.webChromeClient = OkWebChromeClient()
        okWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
            }
        }
        okWebView.addJavascriptInterface(WorldCupJsInterface(this@WorldCupFragment),
            WorldCupJsInterface.name)
        loadWebURL(MultiLanguagesApplication.mInstance.sOddsType)
    }

    var currentOdsType: String? = null

    private fun loadWebURL(oddsType: String?) {
        if (currentOdsType != null && currentOdsType.equals(oddsType)) {
            return
        }

        currentOdsType = oddsType
        val url = Constants.getWorldCupH5Url(requireContext(),viewModel.token?:"")
        binding.okWebView.loadUrl("https://www.fiba.basketball/")
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
            if (!it) mainTabActivity().startLogin()
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

   open class WorldCupJsInterface(val fragment: WorldCupFragment) {

        companion object {
            const val name = "app"
        }
       @JavascriptInterface
       fun toBetRecord() {
           (fragment.activity as MainTabActivity)?.jumpToBetInfo(1)
       }
       @JavascriptInterface
       fun toLogin() {
           (fragment.activity as MainTabActivity)?.startLogin()
       }
        @JavascriptInterface
        fun toDetailEndScore(matchInfoJson: String) {
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