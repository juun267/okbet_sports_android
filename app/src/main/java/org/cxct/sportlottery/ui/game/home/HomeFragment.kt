package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_game.*
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
import org.cxct.sportlottery.ui.game.data.SpecialEntranceSource
import org.cxct.sportlottery.ui.game.home.gameTable4.GameBean
import org.cxct.sportlottery.ui.game.home.gameTable4.GameEntity4
import org.cxct.sportlottery.ui.game.home.gameTable4.RvGameTable4Adapter
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity


/**
 * TODO:
 * 1. 上下滑動 ToolBar 固定
 * 2. 即將開賽盤 時間倒數
 * 3. 賠率串接、玩法數
 */
class HomeFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    private val mRvGameTable4Adapter = RvGameTable4Adapter()
    private var mSelectMatchType: MatchType = MatchType.IN_PLAY
    private var mInPlayResult: MatchPreloadResult? = null
    private var mAtStartResult: MatchPreloadResult? = null

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

        initTable()
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

    private fun initTable() {
        rv_game_table.adapter = mRvGameTable4Adapter

        rb_in_play.setOnClickListener {
            mSelectMatchType = MatchType.IN_PLAY
            refreshTable(mSelectMatchType, mInPlayResult)
        }

        rb_soon.setOnClickListener {
            mSelectMatchType = MatchType.AT_START
            refreshTable(mSelectMatchType, mAtStartResult)
        }
    }

    private fun refreshTable(selectMatchType: MatchType, result: MatchPreloadResult?) {
        if (selectMatchType == MatchType.IN_PLAY) {
            unsubscribeAllHallChannel() //先清除之前訂閱項目

            //訂閱所有滾球賽事
            result?.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    subscribeHallChannel(data.code, match)
                }
            }
        }

        mRvGameTable4Adapter.setRvGameData(result?.matchPreloadData?.apply {
            matchType = selectMatchType
        })
        mRvGameTable4Adapter.setOnSelectItemListener(object : OnSelectItemListener<GameBean> {
            override fun onClick(select: GameBean) {
                scroll_view.smoothScrollTo(0, 0)
                navOddsDetailLive(select.code, select.match?.id)

                //TODO simon test 今日 使用下方跳轉
//                navOddsDetailFragment(select.code, select.match?.id)
            }
        })
        mRvGameTable4Adapter.setOnSelectAllListener(object : OnSelectItemListener<GameEntity4> {
            override fun onClick(select: GameEntity4) {
                scroll_view.smoothScrollTo(0, 0)
                viewModel.navSpecialEntrance(
                    SpecialEntranceSource.HOME,
                    selectMatchType,
                    getSportType(select.code)
                )
            }
        })
    }

    private fun initEvent() {
        card_football.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.FOOTBALL
            )
        }

        card_basketball.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.BASKETBALL
            )
        }

        card_tennis.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.TENNIS
            )
        }

        card_badminton.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.BADMINTON
            )
        }

        card_volleyball.setOnClickListener {
            viewModel.navSpecialEntrance(
                SpecialEntranceSource.HOME,
                MatchType.TODAY,
                SportType.VOLLEYBALL
            )
        }

        card_game_soon.setOnClickListener {
            viewModel.navSpecialEntrance(SpecialEntranceSource.HOME, MatchType.AT_START, null)
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
        viewModel.gameCateDataList.observe(viewLifecycleOwner, {
            updateInPlayUI(it)
        })

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                mInPlayResult = result
                if (mSelectMatchType == MatchType.IN_PLAY)
                    refreshTable(mSelectMatchType, mInPlayResult)
            }
        })

        viewModel.matchPreloadAtStart.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                mAtStartResult = result
                if (mSelectMatchType == MatchType.AT_START)
                    refreshTable(mSelectMatchType, mAtStartResult)
            }
        })
    }

    private fun observeSocketData() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            if (mSelectMatchType == MatchType.IN_PLAY)
                mRvGameTable4Adapter.setMatchStatusData(it?.matchStatusCO)
        })

        receiver.matchClock.observe(viewLifecycleOwner, {
            if (mSelectMatchType == MatchType.IN_PLAY)
                mRvGameTable4Adapter.setMatchClockData(it?.matchClockCO)
        })
    }

    private fun queryData() {
        viewModel.getMatchPreload()
    }

    private fun updateInPlayUI(gameCateList: List<GameCateData>?) {
        val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
        val lotteryCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val liveCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val pokerCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val slotCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0
        val fishingCount =
            gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size }
                ?: 0

        card_lottery.visibility =
            if (isShowThirdGame && lotteryCount > 0) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0) View.VISIBLE else View.GONE
        card_fishing.visibility =
            if (isShowThirdGame && fishingCount > 0) View.VISIBLE else View.GONE
    }

    private fun getSportType(sportCode: String?): SportType? {
        return when (sportCode) {
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.TENNIS.code -> SportType.TENNIS
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.BADMINTON.code -> SportType.BADMINTON
            else -> null
        }
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

    private fun navOddsDetailFragment(
        sportTypeCode: String?,
        matchId: String?,
        matchType: MatchType = MatchType.TODAY,
    ) {
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
                        matchType, sportType, matchId, arrayOf()
                    )
                findNavController().navigate(action)
            }
        }
    }
}