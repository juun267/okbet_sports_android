package org.cxct.sportlottery.ui.game.league

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.OddsListData
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.statistics.KEY_MATCH_ID
import org.cxct.sportlottery.ui.statistics.StatisticsActivity
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameLeagueFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameLeagueFragmentArgs by navArgs()

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener {
                viewModel.switchPlay(
                    args.matchType,
                    args.leagueId.toList(),
                    args.matchId.toList(),
                    it
                )
                loading()
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
            leagueListener = LeagueListener {
                subscribeChannelHall(it)
            }

            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it)
                            }
                        }
                        else -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
                            }
                        }
                    }
                },
                { matchInfo, odd, playCateName ->
                    addOddsDialog(matchInfo, odd, playCateName)
                    hideKeyboard()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.getQuickList(it)
                    }
                },
                {
                    viewModel.clearQuickPlayCateSelected()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                },
                { matchId ->
                    navStatistics(matchId)
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_league, container, false).apply {
            setupToolbar(this)
            setupPlayCategory(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupToolbar(view: View) {
        view.game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupPlayCategory(view: View) {
        view.game_league_play_category.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = playCategoryAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )
        }
    }

    private fun setupLeagueOddList(view: View) {
        view.game_league_odd_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketObserver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLeagueOddsList(
            args.matchType,
            args.leagueId.toList(),
            args.matchId.toList(),
            isReloadPlayCate = true
        )
        loading()
    }

    private fun initObserve() {
        viewModel.playList.observe(this.viewLifecycleOwner, {
            playCategoryAdapter.data = it

            it.find { play ->
                play.isSelected
            }?.let { selectedPlay ->
                if (selectedPlay.selectionType == SelectionType.SELECTABLE.code && selectedPlay.isLocked == false) {
                    showPlayCateBottomSheet(selectedPlay)
                }
            }
        })

        viewModel.playCate.observe(this.viewLifecycleOwner, {
            playCategoryAdapter.apply {
                data.find { it.isSelected }?.playCateList?.forEach { playCate ->
                    playCate.isSelected = (playCate.code == it)
                }
                notifyDataSetChanged()
            }
        })

        viewModel.oddsListResult.observe(this.viewLifecycleOwner, {
            hideLoading()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    updateToolbar(oddsListResult.oddsListData)

                    updateSportBackground(oddsListResult.oddsListData?.sport?.code)

                    game_league_odd_list.apply {
                        adapter = leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.gameType = args.gameType
                            }.toMutableList()
                        }
                    }

                    leagueOdds.forEach { leagueOdd ->
                        subscribeChannelHall(leagueOdd)
                    }
                }
            }
        })

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner, {
            leagueAdapter.data = it.toMutableList()
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap.values.forEach { oddList ->
                            oddList.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.forEach { map ->
                                map.value?.forEach { odd ->
                                    odd?.isSelected = it.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                                }
                            }
                        }
                    }
                }

                leagueAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        })

        viewModel.favorMatchList.observe(this.viewLifecycleOwner, {
            leagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            leagueAdapter.notifyDataSetChanged()
        })
    }

    private fun updateToolbar(oddsListData: OddsListData?) {
        when {
            (oddsListData?.leagueOdds?.size ?: 0 == 1) -> {
                game_toolbar_match_type.text = oddsListData?.sport?.name ?: ""
                game_toolbar_sport_type.text =
                    oddsListData?.leagueOdds?.firstOrNull()?.league?.name ?: ""
            }

            (oddsListData?.leagueOdds?.size ?: 0 > 1) -> {
                game_toolbar_match_type.text = oddsListData?.sport?.name ?: ""
                game_toolbar_sport_type.text = args.matchType.name
            }
        }
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            args.gameType.key,
                            leagueOdd.matchOdds.toMutableList(),
                            matchStatusChangeEvent
                        ) &&
                        leagueOdd.isExpand
                    ) {
                        if (leagueOdd.matchOdds.isNullOrEmpty()) {
                            leagueAdapter.data.remove(leagueOdd)
                        }

                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, {
            it?.let { matchClockEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchClock(
                                matchOdd,
                                matchClockEvent
                            )
                        } &&
                        leagueOdd.isExpand) {

                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchOdds(
                                matchOdd, oddsChangeEvent
                            )
                        } &&
                        leagueOdd.isExpand
                    ) {
                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsLockEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                        } &&
                        leagueOdd.isExpand
                    ) {
                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(
                                matchOdd,
                                globalStopEvent
                            )
                        } &&
                        leagueOdd.isExpand
                    ) {
                        leagueAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelHallAll()

                leagueAdapter.data.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
            }
        })

        receiver.leagueChange.observe(this.viewLifecycleOwner, {
            it?.let { leagueChangeEvent ->
                leagueChangeEvent.leagueIdList?.let { leagueIdList ->
                    unSubscribeChannelHallAll()
                    viewModel.getLeagueOddsList(args.matchType, leagueIdList, listOf())
                    loading()
                }
            }
        })
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    private fun updateSportBackground(sportCode: String?) {
        Glide.with(requireContext()).load(
            when (sportCode) {
                GameType.FT.key -> R.drawable.soccer48
                GameType.BK.key -> R.drawable.basketball48
                GameType.TN.key -> R.drawable.tennis48
                GameType.VB.key -> R.drawable.volleyball48
                else -> null
            }
        ).into(game_league_toolbar_bg)
    }

    private fun showPlayCateBottomSheet(play: Play) {
        showBottomSheetDialog(
            play.name,
            play.playCateList?.map { playCate -> StatusSheetData(playCate.code, playCate.name) }
                ?: listOf(),
            StatusSheetData(
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.code,
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                viewModel.switchPlayCategory(
                    args.matchType,
                    args.leagueId.toList(),
                    args.matchId.toList(),
                    data.code
                )
                (activity as BaseActivity<*>).bottomSheet.dismiss()
                loading()
            })
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val action =
            GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailFragment(
                args.matchType,
                args.gameType,
                matchId,
                matchInfoList.toTypedArray()
            )

        findNavController().navigate(action)
    }

    private fun navOddsDetailLive(matchId: String) {
        val action = GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailLiveFragment(
            args.matchType,
            args.gameType,
            matchId
        )

        findNavController().navigate(action)
    }

    private fun navStatistics(matchId: String?) {
        matchId?.let {
            activity?.apply {
                startActivity(Intent(requireContext(), StatisticsActivity::class.java).apply {
                    putExtra(KEY_MATCH_ID, matchId)
                })

                overridePendingTransition(
                    R.anim.push_bottom_to_top_enter,
                    R.anim.push_bottom_to_top_exit
                )
            }
        }
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String
    ) {
        matchInfo?.let {
            viewModel.updateMatchBetList(
                args.matchType,
                args.gameType,
                playCateName,
                matchInfo,
                odd,
                ChannelType.HALL,
                getPlayCateMenuCode()
            )
        }
    }

    private fun getPlayCateMenuCode(): String? {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        return when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                playSelected.playCateList?.find { it.isSelected }?.code
            }
            SelectionType.UN_SELECTABLE.code -> {
                playSelected.code
            }
            else -> null
        }
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.isExpand) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlayCateMenuCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        subscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }
                }
                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlayCateMenuCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        game_league_odd_list.adapter = null
    }
}