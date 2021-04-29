package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil


@Suppress("DEPRECATION")
class OddsDetailLiveFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    OnOddClickListener {

    private val args: OddsDetailLiveFragmentArgs by navArgs()

    private var mSportCode: String? = null
    private var matchId: String? = null
    private var matchOdd: MatchOdd? = null

    private val matchOddList: MutableList<MatchInfo?> = mutableListOf()

    private lateinit var dataBinding: FragmentOddsDetailLiveBinding

    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var oddsGameCardAdapter: OddsGameCardAdapter? = null

    private var sport = ""

    private var curHomeScore = 0
    private var curAwayScore = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSportCode = args.sportType.code
        matchId = args.matchId
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail_live, container, false)
        dataBinding.apply {
            view = this@OddsDetailLiveFragment
            gameViewModel = this@OddsDetailLiveFragment.viewModel
            lifecycleOwner = this@OddsDetailLiveFragment.viewLifecycleOwner
        }
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        observeSocketData()
        initRecyclerView()

        setupWebView(web_view)
    }


    override fun onStart() {
        super.onStart()
        getData()
        setWebView()
    }


    override fun onStop() {
        super.onStop()
        unsubscribeAllHallChannel()
        service.unsubscribeAllEventChannel()
    }


    private fun setWebView() {
        web_view.loadUrl("${sConfigData?.sportAnimation}?matchId=${matchId?.replace("sr:match:", "")}&lang=${LanguageManager.getSelectLanguage(context).key}")
    }


    private fun initRecyclerView() {
        rv_game_card.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        oddsGameCardAdapter = OddsGameCardAdapter(this@OddsDetailLiveFragment.matchId, OddsGameCardAdapter.ItemClickListener {
            it.let {
                matchId = it.id
                getData()
                setWebView()
            }
        })
        rv_game_card.adapter = oddsGameCardAdapter
    }


    private fun observeSocketData() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList = arrayListOf<OddsDetailListData>()
            it.odds?.forEach { map ->
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

        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {

            oddsGameCardAdapter?.updateGameCard(it?.matchStatusCO)

            it?.matchStatusCO?.let { ms ->
                if(ms.matchId == this.matchId){
                    ms.homeScore?.let { h ->
                        ms.awayScore?.let { a ->
                            curHomeScore = h
                            curAwayScore = a
                        }
                    }
                }
            }
        })

        receiver.matchClock.observe(viewLifecycleOwner, Observer {
            oddsGameCardAdapter?.updateGameCard(it?.matchClockCO)
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            service.unsubscribeAllHallChannel()
            service.unsubscribeAllEventChannel()

            matchOddList.forEach { matchOddList ->
                subscribeHallChannel(sport, matchOddList?.id)
            }

            service.subscribeEventChannel(matchId)
        })
    }


    private fun initUI() {

        (dataBinding.rvDetail.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailLiveFragment).apply {
            sportCode = mSportCode
        }

        dataBinding.rvDetail.apply {
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                matchOdd = result.oddsDetailData?.matchOdd
                result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                    result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                        oddsDetailListAdapter?.homeName = home
                        oddsDetailListAdapter?.awayName = away
                    }
                }
            }
        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                if (list.size > 0) {
                    oddsDetailListAdapter?.oddsDetailDataList?.clear()
                    oddsDetailListAdapter?.oddsDetailDataList?.addAll(list)
                    oddsDetailListAdapter?.notifyDataSetChanged()
                }
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.setBetInfoList(it)
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            unsubscribeAllHallChannel()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    matchOddList.clear()
                    oddsListResult.oddsListData?.leagueOdds?.forEach { LeagueOdd ->
                        LeagueOdd.matchOdds.forEach { MatchOdd ->
                            matchOddList.add(MatchOdd.matchInfo)
                        }
                    }
                    sport = oddsListResult.oddsListData?.sport?.code.toString()
                    oddsGameCardAdapter?.data = matchOddList
                }
            }

            //訂閱所有賽事
            matchOddList.forEach { matchOddList ->
                subscribeHallChannel(sport, matchOddList?.id)
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.oddsType = it
        })

    }


    private fun subscribeHallChannel(code: String, match: String?) {
        service.subscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, match)
    }


    private fun unsubscribeAllHallChannel() {
        //離開畫面時取消訂閱所有賽事
        service.unsubscribeAllHallChannel()
    }


    private fun getData() {
        mSportCode?.let { mSportCode ->
            viewModel.getPlayCateList(mSportCode)
        }

        matchId?.let { matchId ->
            viewModel.getOddsDetailByMatchId(matchId)
            service.subscribeEventChannel(matchId)
        }

        viewModel.getOddsList(args.sportType.code, MatchType.IN_PLAY.postValue)
        loading()
    }


    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->

            matchOdd.matchInfo.homeScore = curHomeScore
            matchOdd.matchInfo.awayScore = curAwayScore

            mSportCode?.let { gameType ->
                viewModel.addInBetInfo(
                    matchType = MatchType.IN_PLAY,
                    args.sportType,
                    gameType = gameType,
                    playCateName = oddsDetail.name,
                    matchOdd = matchOdd,
                    odd = odd
                )
            }
        }
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    fun back() {
        findNavController().navigateUp()
    }


    private fun setupWebView(webView: WebView) {
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


            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                live_web_preload?.visibility = View.INVISIBLE
                view?.visibility = View.VISIBLE
            }

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


}