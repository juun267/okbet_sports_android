package org.cxct.sportlottery.ui.maintab.worldcup

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_worldcup.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentWorldcupBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.pxToDp
import org.cxct.sportlottery.view.webView.OkWebChromeClient
import org.cxct.sportlottery.view.webView.OkWebViewClient
import org.cxct.sportlottery.view.webView.WebViewCallBack
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class WorldCupFragment : BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {


    private lateinit var binding: FragmentWorldcupBinding
    private val loadingHolder by lazy { Gloading.wrapView(binding.okWebView) }
    private inline fun mainTabActivity() = activity as MainTabActivity
    private var mOddType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value?:OddsType.EU

    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentWorldcupBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        ImmersionBar.with(this)
            .statusBarDarkFont(false)
            .init()
        initToolBar()
        initObservable()
        initWeb()
        homeToolbar.post {
            loadWebURL()
        }
    }
    fun initToolBar() = binding.run {
        homeToolbar.attach(this@WorldCupFragment, mainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            mainTabActivity().showMainLeftMenu(this@WorldCupFragment.javaClass)
        }
    }
    var isInitedWeb = false
    private fun initWeb() =binding.okWebView.run {
        isInitedWeb = true
        setBackgroundColor(context.getColor(R.color.color_F8F9FD))
        webChromeClient = object : OkWebChromeClient(){}
        webViewClient = object : OkWebViewClient(object : WebViewCallBack {

            override fun pageStarted(view: View?, url: String?) {
                loadingHolder.showLoading()
            }

            override fun pageFinished(view: View?, url: String?) {
                loadingHolder.showLoadSuccess()
            }
            override fun onError() {
            }
        }) {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                JumpUtil.toInternalWeb(requireContext(),request?.url.toString(),"")
                return true
            }

            override fun onReceivedSslError(
                view: WebView, handler: SslErrorHandler, error: SslError,
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
    private fun loadWebURL() {
        val url = Constants.getWorldCupH5Url(requireContext(),homeToolbar.height.pxToDp)
        LogUtil.d(url)
        binding.okWebView.loadUrl(url)
    }

    override fun onPause() {
        super.onPause()
        pauseWebVideo()
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

    private fun initObservable() {
        if (viewModel == null) {
            return
        }
        viewModel.oddsType.observe(viewLifecycleOwner) {
            if (mOddType != it) {
                mOddType = it
                loadWebURL()
            }
        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .init()
            pauseWebVideo()
        }else{
            ImmersionBar.with(this)
                .statusBarDarkFont(false)
                .init()
            binding.homeToolbar.onRefreshMoney()
            loadWebURL()
        }
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
       fun toNewPage(url: String,title: String,inApp: Boolean) {
           LogUtil.d("toNewPage=$url")
           if (inApp){
               JumpUtil.toInternalWeb(fragment.requireContext(),url,title)
           }else{
               JumpUtil.toExternalWeb(fragment.requireContext(),url)
           }
       }
        @JavascriptInterface
        fun toDetailEndScore(matchId: String,endScore: Boolean) {
            LogUtil.d("toDetailEndScore=$matchId,$endScore")
            if (TextUtils.isEmpty(matchId)) {
                ToastUtil.showToast(fragment.requireContext(), R.string.error)
                return
            }
            SportDetailActivity.startActivity(fragment.mainTabActivity(), matchId = matchId, tabCode = if (endScore) MatchType.END_SCORE.postValue else null)
        }
    }

}