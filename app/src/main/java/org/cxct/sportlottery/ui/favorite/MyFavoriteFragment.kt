package org.cxct.sportlottery.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_my_favorite.*
import kotlinx.android.synthetic.main.fragment_my_favorite.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.PlayCategoryListener
import org.cxct.sportlottery.ui.statistics.KEY_MATCH_ID
import org.cxct.sportlottery.ui.statistics.StatisticsActivity
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration


class MyFavoriteFragment : BaseSocketFragment<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                viewModel.switchGameType(it)
            }
        }
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener {
                viewModel.switchPlay(it)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(MatchType.MY_EVENT).apply {
            leagueListener = LeagueListener {
                subscribeChannelHall(it)
            }

            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList ->
                    if (matchInfoList.firstOrNull()?.isInPlay == true) {
                        matchId?.let {
                            navOddsDetailLive(matchId)
                        }
                    } else {
                        matchId?.let {
                            navOddsDetail(matchId, matchInfoList)
                        }
                    }
                },
                { matchInfo, odd, playCateName ->
                    addOddsDialog(matchInfo, odd, playCateName)
                },
                { matchId ->
                    viewModel.getQuickList(matchId)
                },
                {
                    viewModel.clearQuickPlayCateSelected()
                },
                { matchId ->
                    viewModel.pinFavorite(FavoriteType.MATCH, matchId)
                    loading()
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_favorite, container, false).apply {
            setupToolbar(this)
            setupGameTypeList(this)
            setupPlayCategory(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupToolbar(view: View) {
        (activity as AppCompatActivity).setSupportActionBar(view.favorite_toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupGameTypeList(view: View) {
        view.favorite_game_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupPlayCategory(view: View) {
        view.favorite_play_category.apply {
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
        view.favorite_game_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = leagueAdapter
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initSocketObserver()
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            gameTypeAdapter.dataSport.find { gameType -> gameType.isSelected }?.code,
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

                val playSelected = playCategoryAdapter.data.find { play -> play.isSelected }
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchOdds(
                                matchOdd.apply {
                                    this.oddsMap.filter { odds -> playSelected?.code == MenuCode.MAIN.code || odds.key == playSelected?.playCateList?.firstOrNull()?.code }
                                }, oddsChangeEvent
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
            it?.let {
                viewModel.getFavoriteMatch()
                loading()
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

    override fun onStart() {
        super.onStart()

        viewModel.getSportQuery()
        loading()
    }

    private fun initObserver() {
        viewModel.sportQueryData.observe(this.viewLifecycleOwner, {
            hideLoading()
            it?.getContentIfNotHandled()?.let { sportQueryData ->

                updateGameTypeList(sportQueryData.items?.map { item ->
                    Item(
                        code = item.code ?: "",
                        name = item.name ?: "",
                        num = item.num ?: 0,
                        play = null,
                        sortNum = item.sortNum ?: 0
                    ).apply {
                        this.isSelected = item.isSelected
                    }
                })

                updatePlayCategory(sportQueryData.items?.find { item ->
                    item.isSelected
                }?.play)
            }
        })

        viewModel.curPlay.observe(this.viewLifecycleOwner, {
            if (it.selectionType == SelectionType.SELECTABLE.code && it.isLocked == false)
                showPlayCateBottomSheet(it)
        })

        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner, {
            hideLoading()
            leagueAdapter.data = it.toMutableList()
            try {
                unSubscribeChannelHallAll()
                it.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                    }
                }

                leagueAdapter.notifyDataSetChanged()
            }
        })

        viewModel.favorMatchList.observe(this.viewLifecycleOwner, { favorMatchList ->
            if (favorMatchList.isNullOrEmpty()) {
                fl_no_game.visibility = View.VISIBLE
            } else {
                fl_no_game.visibility = View.GONE
            }
        })
    }

    private fun updateGameTypeList(items: List<Item>?) {
        gameTypeAdapter.dataSport = items ?: listOf()

        favorite_game_type.text = when (items?.find {
            it.isSelected
        }?.code) {
            GameType.FT.key -> getString(GameType.FT.string)
            GameType.BK.key -> getString(GameType.BK.string)
            GameType.TN.key -> getString(GameType.TN.string)
            GameType.VB.key -> getString(GameType.VB.string)
            else -> ""
        }

        Glide.with(this).load(
            when (items?.find {
                it.isSelected
            }?.code) {
                GameType.FT.key -> R.drawable.soccer108
                GameType.BK.key -> R.drawable.basketball108
                GameType.TN.key -> R.drawable.tennis108
                GameType.VB.key -> R.drawable.volleyball108
                else -> null
            }
        ).into(favorite_bg_layer2)
    }

    private fun updatePlayCategory(plays: List<Play>?) {
        playCategoryAdapter.data = plays ?: listOf()
    }

    private fun showPlayCateBottomSheet(play: Play) {
        viewModel.switchPlayCategory(play.playCateList?.firstOrNull()?.code)
        showBottomSheetDialog(
            play.name,
            play.playCateList?.map { playCate -> StatusSheetData(playCate.code, playCate.name) }
                ?: listOf(),
            StatusSheetData(
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.code,
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                viewModel.switchPlayCategory(data.code)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
            })
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        if (gameType == null || matchInfo == null) {
            return
        }

        viewModel.updateMatchBetList(
            MatchType.MY_EVENT,
            gameType,
            playCateName,
            matchInfo,
            odd,
            ChannelType.HALL
        )//TODO 訂閱HALL需傳入CateMenuCode
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.isExpand) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        MenuCode.MAIN.code,
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
                        MenuCode.MAIN.code,
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

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                MyFavoriteFragmentDirections.actionMyFavoriteFragmentToOddsDetailFragment(
                    MatchType.MY_EVENT,
                    gameType,
                    matchId,
                    matchInfoList.toTypedArray()
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                MyFavoriteFragmentDirections.actionMyFavoriteFragmentToOddsDetailLiveFragment(
                    MatchType.MY_EVENT,
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
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
}