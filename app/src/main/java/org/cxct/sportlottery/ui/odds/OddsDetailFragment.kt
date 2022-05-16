package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import kotlinx.android.synthetic.main.fragment_odds_detail.rv_cat
import kotlinx.android.synthetic.main.fragment_odds_detail.rv_detail
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.enum.MatchSource
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.component.LiveViewToolbar
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import java.util.*


@Suppress("DEPRECATION")
class OddsDetailFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class) {

    private val args: OddsDetailFragmentArgs by navArgs()
    private var mStartTimer: Timer? = Timer()
    private val mStartTimeTask = object: TimerTask() {
        override fun run() {

        }
    }

    var matchId: String? = null
    private var matchOdd: MatchOdd? = null


    private var oddsDetailListAdapter: OddsDetailListAdapter? = null

    private val tabCateAdapter: TabCateAdapter by lazy {
        TabCateAdapter(OnItemSelectedListener {
            tabCateAdapter.selectedPosition = it
            viewModel.oddsDetailResult.value?.peekContent()?.oddsDetailData?.matchOdd?.playCateTypeList?.getOrNull(
                it
            )?.code?.let { code ->
                oddsDetailListAdapter?.notifyDataSetChangedByCode(code)
            }
        })
    }


    private val liveToolBarListener by lazy {
        object : LiveViewToolbar.LiveToolBarListener {
            override fun getLiveInfo(newestUrl: Boolean) {
                //do nothing
            }

            override fun showStatistics() {
                StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
                    .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchId = args.matchId
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentOddsDetailBinding.inflate(inflater, container, false).apply {
        fragment = this@OddsDetailFragment
        gameViewModel = this@OddsDetailFragment.viewModel
        lifecycleOwner = this@OddsDetailFragment.viewLifecycleOwner
        executePendingBindings()
    }.root


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        initSocketObserver()
        initBottomNavigation()
    }

    override fun onStart() {
        super.onStart()

        //TODO if args.matchInfoList is empty than need to get match list to find same league match for more button used.
        getData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mStartTimer?.cancel()
        mStartTimer = null
    }

    private fun initUI() {
        oddsDetailListAdapter = OddsDetailListAdapter(
            OnOddClickListener { odd, oddsDetail, scoPlayCateNameForBetInfo ->
                matchOdd?.let { matchOdd ->
                    val fastBetDataBean = FastBetDataBean(
                        matchType = MatchType.TODAY,
                        gameType = args.gameType,
                        playCateCode = oddsDetail?.gameType ?: "",
                        playCateName = oddsDetail?.name ?: "",
                        matchInfo = matchOdd.matchInfo,
                        matchOdd = null,
                        odd = odd,
                        subscribeChannelType = ChannelType.EVENT,
                        betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                        otherPlayCateName = scoPlayCateNameForBetInfo
                    )
                    when (activity) {
                        is GameActivity -> (activity as GameActivity).showFastBetFragment(fastBetDataBean)
                        is GamePublicityActivity -> (activity as GamePublicityActivity).showFastBetFragment(fastBetDataBean)
                    }

//                    viewModel.updateMatchBetList(
//                        matchType = MatchType.TODAY,
//                        gameType = args.gameType,
//                        playCateCode = oddsDetail?.gameType ?: "",
//                        playCateName = oddsDetail?.name ?: "",
//                        matchInfo = matchOdd.matchInfo,
//                        odd = odd,
//                        subscribeChannelType = ChannelType.EVENT,
//                        betPlayCateNameMap = matchOdd.betPlayCateNameMap,
//                        otherPlayCateName = scoPlayCateNameForBetInfo
//                    )
                }
            }
        ).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            oddsDetailListener = OddsDetailListener {
                viewModel.pinFavorite(FavoriteType.PLAY_CATE, it, args.gameType.key)
            }

            sportCode = args.gameType
        }

        rv_detail.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            adapter = oddsDetailListAdapter
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }

        rv_cat.apply {
            adapter = tabCateAdapter
            itemAnimator?.changeDuration = 0
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        }

    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun observeData() {
        viewModel.oddsDetailResult.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.success) {
                    true -> {
                        result.setupPlayCateTab()

                        matchOdd = result.oddsDetailData?.matchOdd
                        matchOdd?.matchInfo?.leagueName = result.oddsDetailData?.league?.name
                        result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                            result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.homeName = home
                                oddsDetailListAdapter?.awayName = away
                            }
                        }
                        setupStartTime(matchOdd?.matchInfo)
                        setupLiveView()
                    }
                    false -> {
                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        }

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                } else {
                    navGameInPlay()
                }
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        }

        viewModel.betInfoResult.observe(this.viewLifecycleOwner) {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            oddsDetailListAdapter?.oddsType = it
        }

        viewModel.favorPlayCateList.observe(this.viewLifecycleOwner) {
            oddsDetailListAdapter?.let { oddsDetailListAdapter ->
                val playCate = it.find { playCate ->
                    playCate.gameType == args.gameType.key
                }

                val playCateCodeList = playCate?.code?.let { it1 ->
                    if (it1.isNotEmpty()) {
                        TextUtil.split(it1)
                    } else {
                        mutableListOf()
                    }
                }

                val pinList = oddsDetailListAdapter.oddsDetailDataList.filter {
                    playCateCodeList?.contains(it.gameType) ?: false
                }.sortedByDescending { oddsDetailListData ->
                    playCateCodeList?.indexOf(oddsDetailListData.gameType)
                }

                val epsSize = oddsDetailListAdapter.oddsDetailDataList.groupBy {
                    it.gameType == PlayCate.EPS.value
                }[true]?.size ?: 0

                oddsDetailListAdapter.oddsDetailDataList.sortBy { it.originPosition }
                oddsDetailListAdapter.oddsDetailDataList.forEach {
                    it.isPin = false
                }

                pinList.forEach {
                    it.isPin = true

                    oddsDetailListAdapter.oddsDetailDataList.add(
                        epsSize,
                        oddsDetailListAdapter.oddsDetailDataList.removeAt(
                            oddsDetailListAdapter.oddsDetailDataList.indexOf(
                                it
                            )
                        )
                    )
                }

                oddsDetailListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initSocketObserver() {
        receiver.matchOddsChange.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsChangeEvent ->
                matchOddsChangeEvent.updateOddsSelectedState()

                oddsDetailListAdapter?.oddsDetailDataList?.forEachIndexed { index, oddsDetailListData ->
                    if (SocketUpdateUtil.updateMatchOdds(
                            oddsDetailListData,
                            matchOddsChangeEvent
                        )
                        && oddsDetailListData.isExpand
                    ) {
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                oddsDetailListAdapter?.oddsDetailDataList?.forEachIndexed { index, oddsDetailListData ->
                    if (SocketUpdateUtil.updateOddStatus(
                            oddsDetailListData,
                            globalStopEvent
                        ) && oddsDetailListData.isExpand
                    ) {
                        oddsDetailListAdapter?.notifyItemChanged(index)
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchId)
            }
        }
    }

    private fun MatchOddsChangeEvent.updateOddsSelectedState(): MatchOddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.odds?.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    private fun getData() {
        matchId?.let { matchId ->
            viewModel.getOddsDetail(matchId)
            subscribeChannelEvent(matchId)
        }
    }


    private fun navGameInPlay() {
        findNavController().navigate(OddsDetailFragmentDirections.actionOddsDetailFragmentToGameV3Fragment(MatchType.IN_PLAY))
    }


    @SuppressLint("InflateParams")
    private fun OddsDetailResult.setupPlayCateTab() {
        val playCateTypeList = this.oddsDetailData?.matchOdd?.playCateTypeList
        if (playCateTypeList?.isNotEmpty() == true) {
            tabCateAdapter.dataList = playCateTypeList
        } else {
            rv_cat.visibility = View.GONE
        }
    }


    private fun setupStartTime(matchInfo: MatchInfo?) {
        matchInfo?.apply {
            tv_time_top.text =
                if (TimeUtil.isTimeToday(startTime)) getString(R.string.home_tab_today) else TimeUtil.timeFormat(
                    startTime,
                    TimeUtil.DM_FORMAT
                )
            if(!TimeUtil.isLastHour(startTime)) {
                tv_time_bottom.text = TimeUtil.timeFormat(startTime, TimeUtil.HM_FORMAT)
            }
            checkStartTime(startTime)
        }
    }

    private fun checkStartTime(startTime: Long?) {
        mStartTimer?.schedule(object: TimerTask() {
            override fun run() {
                lifecycleScope.launch {
                    if (TimeUtil.isLastHour(startTime)) {
                        tv_time_bottom.text = String.format(getString(R.string.at_start_remain_minute), TimeUtil.getRemainMinute(startTime))
                    } else {
                        tv_time_bottom.text = TimeUtil.timeFormat(startTime, TimeUtil.HM_FORMAT)
                    }
                }
            }
        }, 0, 1000)
    }

    private fun setupLiveView() {
        with(live_view_tool_bar) {
            setupToolBarListener(liveToolBarListener)
            setStatisticsState(matchOdd?.matchInfo?.source == MatchSource.SHOW_STATISTICS.code)
            setupPlayerControl(false)
        }
    }


    override fun onStop() {
        super.onStop()
        unSubscribeChannelEvent(matchId)
    }
}