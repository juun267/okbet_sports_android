package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.home.gameTable.GameEntity
import org.cxct.sportlottery.ui.game.v3.GameLeagueFragmentDirections
import org.cxct.sportlottery.ui.game.v3.GameV3FragmentDirections
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.odds.OddsDetailFragmentDirections
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity


class HomeFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeBinding.apply {
            gameViewModel = this@HomeFragment.viewModel
            lifecycleOwner = this@HomeFragment
        }
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEvent()
        initObserve()
        observeSocketData()
    }

    override fun onStart() {
        super.onStart()

        queryData()
    }

    override fun onStop() {
        super.onStop()

        unsubscribeAllHallChannel()
    }

    private fun initEvent() {
        card_football.setOnClickListener {
            viewModel.getGameHallList(MatchType.TODAY, SportType.FOOTBALL)
        }

        card_basketball.setOnClickListener {
            viewModel.getGameHallList(MatchType.TODAY, SportType.BASKETBALL)
        }

        card_tennis.setOnClickListener {
            viewModel.getGameHallList(MatchType.TODAY, SportType.TENNIS)
        }

        card_badminton.setOnClickListener {
            viewModel.getGameHallList(MatchType.TODAY, SportType.BADMINTON)
        }

        card_volleyball.setOnClickListener {
            viewModel.getGameHallList(MatchType.TODAY, SportType.VOLLEYBALL)
        }

        card_game_soon.setOnClickListener {
            viewModel.getGameHallList(MatchType.AT_START, null)
        }

        card_lottery.setOnClickListener {
            navThirdGame(ThirdGameCategory.CGCP)
        }

        card_live.setOnClickListener {
            navThirdGame(ThirdGameCategory.LIVE)
        }

        card_poker.setOnClickListener {
            navThirdGame(ThirdGameCategory.QP)
        }

        card_slot.setOnClickListener {
            navThirdGame(ThirdGameCategory.DZ)
        }

        card_fishing.setOnClickListener {
            navThirdGame(ThirdGameCategory.BY)
        }

        card_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
        }

        card_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun subscribeHallChannel(code: String, match: Match) {
        service.subscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, match.id)
    }

    private fun unsubscribeAllHallChannel() {
        //離開畫面時取消訂閱所有賽事
        service.unsubscribeAllHallChannel()
    }

    private fun initObserve() {
        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, Observer {
            updateInPlayUI(it)
        })

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { result ->
                updateInPlayUI(result)
            }
        })

        viewModel.matchPreloadEarly.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                updateTodayUI(result)
            }
        })
    }

    private fun observeSocketData() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
            drawer_in_play.setMatchStatusData(it?.matchStatusCO)
        })

        receiver.matchClock.observe(viewLifecycleOwner, Observer {
            drawer_in_play.setMatchClockData(it?.matchClockCO)
        })
    }

    private fun queryData() {
        viewModel.getInPlayMatchPreload()
    }

    private fun updateInPlayUI(gameCateList: List<GameCateData>?) {
        val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
        val lotteryCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val liveCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val pokerCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val slotCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size } ?: 0
        val fishingCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size } ?: 0

        card_lottery.visibility = if (isShowThirdGame && lotteryCount > 0) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0) View.VISIBLE else View.GONE
        card_fishing.visibility = if (isShowThirdGame && fishingCount > 0) View.VISIBLE else View.GONE
    }

    private fun updateInPlayUI(result: MatchPreloadResult) {
        unsubscribeAllHallChannel() //先清除之前訂閱項目

        //訂閱所有滾球賽事
        result.matchPreloadData?.datas?.forEach { data ->
            data.matchs.forEach { match ->
                subscribeHallChannel(data.code, match)
            }
        }

        val inPlayCount = result.matchPreloadData?.num ?: 0
        drawer_in_play.setCount(inPlayCount.toString())
        drawer_in_play.setRvGameData(result.matchPreloadData?.apply { matchType = MatchType.IN_PLAY })
        drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                navOddsDetailLive(select.code, select.match?.id)
            }
        })
        drawer_in_play.setOnSelectFooterListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                val sportType = when (select.code) {
                    SportType.FOOTBALL.code -> SportType.FOOTBALL
                    SportType.BASKETBALL.code -> SportType.BASKETBALL
                    SportType.TENNIS.code -> SportType.TENNIS
                    SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                    SportType.BADMINTON.code -> SportType.BADMINTON
                    else -> null
                }
                viewModel.getGameHallList(MatchType.IN_PLAY, sportType, isPreloadTable = true)
            }
        })
    }

    private fun updateTodayUI(result: MatchPreloadResult) {
        val todayCount = result.matchPreloadData?.num ?: 0
        drawer_today.setCount(todayCount.toString())
        drawer_today.setRvGameData(result.matchPreloadData?.apply { matchType = MatchType.TODAY })
        drawer_today.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                navOddsDetailFragment(MatchType.TODAY, select.code, select.match?.id)

            }
        })
        drawer_today.setOnSelectFooterListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                val sportType = when (select.code) {
                    SportType.FOOTBALL.code -> SportType.FOOTBALL
                    SportType.BASKETBALL.code -> SportType.BASKETBALL
                    SportType.TENNIS.code -> SportType.TENNIS
                    SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                    SportType.BADMINTON.code -> SportType.BADMINTON
                    else -> null
                }
                viewModel.getGameHallList(MatchType.TODAY, sportType, isPreloadTable = true)
            }
        })
    }

    private fun navOddsDetailLive(sportTypeCode: String?, matchId: String?) {
        val sportType = when (sportTypeCode) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }

        sportType?.let {
            matchId?.let {
                val action = HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    sportType,
                    matchId,
                )

                findNavController().navigate(action)
            }
        }
    }

    private fun navOddsDetailFragment(matchType:MatchType, sportTypeCode: String?, matchId: String?) {
        val sportType = when (sportTypeCode) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.TENNIS.code -> SportType.TENNIS
            else -> null
        }
        sportType?.let {
                matchId?.let {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToOddsDetailFragment(
                            matchType, sportType, matchId
                        )
                    findNavController().navigate(action)
                }
        }
    }
}