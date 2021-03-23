package org.cxct.sportlottery.ui.odds

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import com.squareup.moshi.Types
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.MoshiUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

class OddsDetailFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    Animation.AnimationListener, OnOddClickListener {


    companion object {

        const val TIME_LENGTH = 5

        const val GAME_TYPE = "gameType"
        const val TYPE_NAME = "typeName"//leagueName
        const val MATCH_ID = "matchId"
        const val ODDS_TYPE = "oddsType"

        fun newInstance(gameType: String?, typeName: String?, matchId: String, oddsType: String) =
            OddsDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(GAME_TYPE, gameType)
                    putString(TYPE_NAME, typeName)
                    putString(MATCH_ID, matchId)
                    putString(ODDS_TYPE, oddsType)
                }
            }
    }


    private var gameType: String? = null
    private var typeName: String? = null
    private var matchId: String? = null
    private var oddsType: String? = null


    private lateinit var dataBinding: FragmentOddsDetailBinding


    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var oddsGameCardAdapter: OddsGameCardAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.getString(GAME_TYPE)
            typeName = it.getString(TYPE_NAME)
            matchId = it.getString(MATCH_ID)
            oddsType = it.getString(ODDS_TYPE)
        }

        service.subscribeEventChannel(matchId)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false)
        dataBinding.apply {
            view = this@OddsDetailFragment
            gameViewModel = this@OddsDetailFragment.viewModel
            lifecycleOwner = this@OddsDetailFragment.viewLifecycleOwner
        }
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        observeSocketData()
        getData()
        initRecyclerView()
        setupWebView(web_view)
        setWebView()
    }

    private fun setWebView() {
        web_view.loadUrl("https://sports.cxct.org/animation/?matchId=${matchId}")
    }

    private fun initRecyclerView() {
        rv_game_card.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        oddsGameCardAdapter = OddsGameCardAdapter(OddsGameCardAdapter.ItemClickListener {
            it.let {
                this@OddsDetailFragment.matchId = it.id
                getData()
                setWebView()
            }
        })
        rv_game_card.adapter = oddsGameCardAdapter
        oddsGameCardAdapter?.data = viewModel.getGameCard()
    }


    private fun observeSocketData() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            //TODO Cheryl: 改變UI (取odds list 中的前兩個, 做顯示判斷, 根據)
            val newList = arrayListOf<OddsDetailListData>()

            it.odds.forEach { map ->
                val key = map.key
                val value = map.value
                val filteredOddList = mutableListOf<Odd>()
                value.odds?.forEach { odd ->
                    if (odd != null)
                        filteredOddList.add(odd)
                }
                newList.add(
                    OddsDetailListData(
                        key,
                        TextUtil.split(value.typeCodes),
                        value.name,
                        filteredOddList
                    )
                )
            }

            oddsDetailListAdapter?.updatedOddsDetailDataList = newList
        })
    }


    private fun initUI() {

        (dataBinding.rvDetail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailFragment)

        dataBinding.rvDetail.apply {
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {

            it?.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                it.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                    val strVerse = getString(R.string.verse_)
                    val strMatch = "$home${strVerse}$away"
                    val color = ContextCompat.getColor(requireContext(), R.color.colorOrange)
                    val startPosition = strMatch.indexOf(strVerse)
                    val endPosition = startPosition + strVerse.length
                    val style = SpannableStringBuilder(strMatch)
                    style.setSpan(
                        ForegroundColorSpan(color),
                        startPosition,
                        endPosition,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    oddsDetailListAdapter?.homeName = home
                    oddsDetailListAdapter?.awayName = away

                }
            }

        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            if (it.size > 0) {
                oddsDetailListAdapter?.oddsDetailDataList?.clear()
                oddsDetailListAdapter?.oddsDetailDataList?.addAll(it)
                oddsDetailListAdapter?.notifyDataSetChanged()
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.setBetInfoList(it)
        })

        viewModel.betInfoRepository.isParlayPage.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.setCurrentMatchId(if (it) matchId else null)
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.peekContent()
            if (eventResult?.success != true) {
                val dialog = CustomAlertDialog(requireActivity())
                dialog.setTitle(getString(R.string.prompt))
                dialog.setMessage(eventResult?.msg ?: getString(R.string.unknown_error))
                dialog.setNegativeButtonText(null)
                dialog.setTextColor(R.color.red2)
                dialog.show()
            }
        })

    }


    private fun getData() {
        gameType?.let { gameType ->
            viewModel.getPlayCateList(gameType)
        }

        matchId?.let { matchId ->
            oddsType?.let { oddsType ->
                viewModel.getOddsDetail(matchId, oddsType)
            }
        }
    }


    fun refreshData(gameType: String?, matchId: String?, typeName: String?) {
        this.gameType = gameType
        this.matchId = matchId
        getData()
    }


    override fun getBetInfoList(odd: Odd) {
        viewModel.getBetInfoList(listOf(org.cxct.sportlottery.network.bet.Odd(odd.id, odd.odds)))
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    fun back() {
        //比照h5特別處理退出動畫
        val animation: Animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.exit_to_right)
        animation.duration = resources.getInteger(R.integer.config_navAnimTime).toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                parentFragmentManager.popBackStack()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        this.view?.startAnimation(animation)
    }


    fun setupWebView(webView: WebView) {
        if (BuildConfig.DEBUG)
            WebView.setWebContentsDebuggingEnabled(true)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.domStorageEnabled = true //对H5支持
        settings.useWideViewPort = true //将图片调整到适合webview的大小
        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.databaseEnabled = false
        settings.setAppCacheEnabled(false)
        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val newWebView = WebView(view.context)
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        //20191120 記錄問題： target=_black 允許跳轉新窗口處理
                        //在此处进行跳转URL的处理, 一般情况下_black需要重新打开一个页面
                        try {
                            //使用系統默認外部瀏覽器跳轉
                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(i)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            // For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
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

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                //此方法是为了处理在5.0以上Https的问题，必须加上
                handler.proceed()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                back()
                return@OnKeyListener true
            }
            false
        })
    }


    override fun onAnimationRepeat(animation: Animation?) {
    }


    override fun onAnimationEnd(animation: Animation?) {
    }


    override fun onAnimationStart(animation: Animation?) {
    }


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            val anim = AnimationUtils.loadAnimation(activity, R.anim.enter_from_right)
            anim.setAnimationListener(this)
            anim
        } else {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (matchId?.let { viewModel.checkInBetInfo(it) } == false) {
            service.unSubscribeEventChannel(matchId)
        }
        viewModel.removeOddsDetailPageValue()
    }
}