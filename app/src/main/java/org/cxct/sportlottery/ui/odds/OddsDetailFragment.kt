package org.cxct.sportlottery.ui.odds


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import kotlinx.android.synthetic.main.fragment_odds_detail.rv_detail
import kotlinx.android.synthetic.main.fragment_odds_detail.tab_cat
import kotlinx.android.synthetic.main.fragment_odds_detail_live.*
import kotlinx.android.synthetic.main.view_odds_detail_toolbar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil


@Suppress("DEPRECATION")
class OddsDetailFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),
    OnOddClickListener {


    private val args: OddsDetailFragmentArgs by navArgs()


    var matchId: String? = null
    private var matchOdd: MatchOdd? = null


    private var oddsDetailListAdapter: OddsDetailListAdapter? = null


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
    }
    
    override fun onStart() {
        super.onStart()

        //TODO if args.matchInfoList is empty than need to get match list to find same league match for more button used.
        getData()
    }

    private fun initUI() {
        oddsDetailListAdapter = OddsDetailListAdapter(this@OddsDetailFragment).apply {
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

        tab_cat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { t ->
                    viewModel.playCateListResult.value?.peekContent()?.rows?.get(t)?.code?.let {
                        oddsDetailListAdapter?.notifyDataSetChangedByCode(it)
                    }
                }
            }
        })
    }


    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.playCateListResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                result.success.let { success ->
                    if (success) {
                        tab_cat.removeAllTabs()
                        if (result.rows.isNotEmpty()) {
                            for (row in result.rows) {
                                val customTabView = layoutInflater.inflate(R.layout.tab_odds_detail, null).apply {
                                    findViewById<TextView>(R.id.tv_tab).text = row.name
                                }

                                tab_cat.addTab(
                                    tab_cat.newTab().setCustomView(customTabView),
                                    false
                                )
                            }
                            tab_cat.getTabAt(0)?.select()
                        } else {
                            tab_cat.visibility = View.GONE
                        }
                    }
                }
            }
        })

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
                } else {
                    navGameInPlay()
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

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsDetailListAdapter?.oddsType = it
        })

        viewModel.favorPlayCateList.observe(this.viewLifecycleOwner, {
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
                }.sortedByDescending { it.originPosition }

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
        })
    }

    private fun initSocketObserver() {
        receiver.matchOddsChange.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsChangeEvent ->
                viewModel.updateOddForOddsDetail(matchOddsChangeEvent)
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let {
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchId)
            }
        })
    }

    private fun getData() {
        args.gameType.let { gameType ->
            matchId?.let { matchId ->
                viewModel.getPlayCateListAndOddsDetail(gameType.key, matchId)
                subscribeChannelEvent(matchId)
            }
        }
    }


    private fun navGameInPlay() {
        findNavController().navigate(OddsDetailFragmentDirections.actionOddsDetailFragmentToGameV3Fragment(MatchType.IN_PLAY))
    }


    private fun setupStartTime(matchInfo: MatchInfo?) {
        matchInfo?.apply {
            tv_time_top.text = TimeUtil.timeFormat(startTime, TimeUtil.DM_FORMAT)
            tv_time_bottom.text = TimeUtil.timeFormat(startTime, TimeUtil.HM_FORMAT)
        }
    }


    override fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData) {
        matchOdd?.let { matchOdd ->
            viewModel.updateMatchBetList(
                matchType = args.matchType,
                gameType = args.gameType,
                playCateName = oddsDetail.name,
                playName = odd.name ?: "",
                matchInfo = matchOdd.matchInfo,
                odd = odd,
                subscribeChannelType = ChannelType.EVENT
            )
        }
    }


    override fun removeBetInfoItem(odd: Odd) {
        viewModel.removeBetInfoItem(odd.id)
    }

    
    override fun onStop() {
        super.onStop()
        unSubscribeChannelEvent(matchId)
    }
}