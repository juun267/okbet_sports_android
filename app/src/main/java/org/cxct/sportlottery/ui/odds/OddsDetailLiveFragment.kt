package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import kotlinx.android.synthetic.main.view_toolbar_live.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailLiveBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.DM_FORMAT
import org.cxct.sportlottery.util.TimeUtil.HM_FORMAT


@Suppress("DEPRECATION", "SetTextI18n")
class OddsDetailLiveFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class), OnOddClickListener {


    private val args: OddsDetailLiveFragmentArgs by navArgs()


    private var mSportCode: String? = null
    private var matchId: String? = null
    private var matchOdd: MatchOdd? = null


    private val matchOddList: MutableList<MatchInfo?> = mutableListOf()


    private lateinit var dataBinding: FragmentOddsDetailLiveBinding


    private var oddsDetailListAdapter: OddsDetailListAdapter? = null
    private var oddsGameCardAdapter: OddsGameCardAdapter? = null


    private var sport = ""


    private var curHomeScore: Int? = null
    private var curAwayScore: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSportCode = args.gameType.key
        matchId = args.matchId
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail_live, container, false)
        dataBinding.apply {
            view = this@OddsDetailLiveFragment
            gameViewModel = this@OddsDetailLiveFragment.viewModel
            lifecycleOwner = this@OddsDetailLiveFragment.viewLifecycleOwner
            receiver = this@OddsDetailLiveFragment.receiver //TODO 後續需要將比分帶入
        }
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        observeData()
        observeSocketData()
        initRecyclerView()
    }


    override fun onStart() {
        super.onStart()
        getData()
        live_view_tool_bar.setWebViewUrl(matchId)
    }


    override fun onStop() {
        super.onStop()
        unsubscribeAllHallChannel()
        service.unsubscribeAllEventChannel()
    }


    private fun initRecyclerView() {
        rv_game_card.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        oddsGameCardAdapter =
            OddsGameCardAdapter(context = context, this@OddsDetailLiveFragment.matchId, mSportCode, OddsGameCardAdapter.ItemClickListener {
                it.let {
                    matchId = it.id
                    getData()
                    live_view_tool_bar.setWebViewUrl(matchId)
                }
            })
        rv_game_card.adapter = oddsGameCardAdapter
    }


    private fun observeSocketData() {
        receiver.matchOddsChange.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.updateOddForOddsDetail(it)
            }
        })

        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {

            oddsGameCardAdapter?.updateGameCard(it?.matchStatusCO)

            it?.matchStatusCO?.takeIf { ms -> ms.matchId == this.matchId }?.apply {
                curHomeScore = homeScore
                curAwayScore = awayScore
            }
        })

        receiver.matchClock.observe(viewLifecycleOwner, {
            oddsGameCardAdapter?.updateGameCard(it?.matchClockCO)
        })

        receiver.producerUp.observe(viewLifecycleOwner, {
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
                when (result.success) {
                    true -> {
                        matchOdd = result.oddsDetailData?.matchOdd
                        result.oddsDetailData?.matchOdd?.matchInfo?.homeName?.let { home ->
                            result.oddsDetailData.matchOdd.matchInfo.awayName.let { away ->
                                oddsDetailListAdapter?.homeName = home
                                oddsDetailListAdapter?.awayName = away
                            }
                        }
                        setupStartTime(matchOdd?.matchInfo)
                    }
                    false -> {
                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        })

        viewModel.oddsDetailList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNotEmpty()) {
                    oddsDetailListAdapter?.oddsDetailDataList = list
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let { list ->
                oddsDetailListAdapter?.betInfoList = list
            }
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, {
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, {
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


    private fun setupStartTime(matchInfo: MatchInfo?) {
        matchInfo?.apply {
            tv_time_top.text = TimeUtil.timeFormat(startTime.toLong(), DM_FORMAT)
            tv_time_bottom.text = TimeUtil.timeFormat(startTime.toLong(), HM_FORMAT)
        }
    }


    private fun subscribeHallChannel(code: String, match: String?) {
        service.subscribeHallChannel(code, PlayCate.OU_HDP.value, match)
    }


    private fun unsubscribeAllHallChannel() {
        //離開畫面時取消訂閱所有賽事
        service.unsubscribeAllHallChannel()
    }


    private fun getData() {
        matchId?.let { matchId ->
            viewModel.getOddsDetailByMatchId(matchId)
            service.subscribeEventChannel(matchId)
        }

        viewModel.getOddsList(args.gameType.key, MatchType.IN_PLAY.postValue)
        loading()
    }


    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->

            matchOdd.matchInfo.homeScore = curHomeScore
            matchOdd.matchInfo.awayScore = curAwayScore

            viewModel.updateMatchBetList(
                matchType = MatchType.IN_PLAY,
                gameType = args.gameType,
                playCateName = oddsDetail.name,
                playName = odd.name ?: "",
                matchInfo = matchOdd.matchInfo,
                odd = odd
            )

        }
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }


    fun back() {
        findNavController().navigateUp()
    }


}