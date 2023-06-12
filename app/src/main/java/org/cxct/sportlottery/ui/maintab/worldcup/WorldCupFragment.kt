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
import kotlinx.android.synthetic.main.fragment_worldcup.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.FragmentWorldcupBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.fromJson
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.webView.OkWebChromeClient


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
            .statusBarDarkFont(true)
            .init()
        initObservable()
        initWeb()
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
        okWebView.addJavascriptInterface(WorldCupJsInterface(mainTabActivity()),
            WorldCupJsInterface.name)
        loadWebURL(MultiLanguagesApplication.mInstance.sOddsType)
    }

    var currentOdsType: String? = null

    private fun loadWebURL(oddsType: String?) {
        if (currentOdsType != null && currentOdsType.equals(oddsType)) {
            return
        }

        currentOdsType = oddsType
        val url = Constants.getWorldCupH5Url(requireContext())
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
            if (!it) mainTabActivity().startLogin()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.okWebView.destroy()
    }

    class WorldCupJsInterface(val context: Context) {

        companion object {
            const val name = "worldCupJsInterface"
        }

        @JavascriptInterface
        fun tapAndroidEvent(infoString: String) {

            if (TextUtils.isEmpty(infoString)) {
                ToastUtil.showToast(context, R.string.error)
                return
            }

            val matchInfo: MatchInfo? = infoString.fromJson()
            if (matchInfo == null) {
                ToastUtil.showToast(context, R.string.error)
                return
            }

            SportDetailActivity.startActivity(context, matchInfo)
        }
    }

}