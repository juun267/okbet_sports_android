package org.cxct.sportlottery.ui.game.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity


class HomeFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding
    private val mSubscribeMatchMap = mutableMapOf<String, Match>() //<code, Match>

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

        queryData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unSubscribeAllHallChannel()
    }

    private fun initEvent() {
        card_football.setOnClickListener {
            viewModel.getGameHallList(MatchType.PARLAY, SportType.FOOTBALL)
        }

        card_basketball.setOnClickListener {
            viewModel.getGameHallList(MatchType.PARLAY, SportType.BASKETBALL)
        }

        card_tennis.setOnClickListener {
            viewModel.getGameHallList(MatchType.PARLAY, SportType.TENNIS)
        }

        card_badminton.setOnClickListener {
            viewModel.getGameHallList(MatchType.PARLAY, SportType.BADMINTON)
        }

        card_volleyball.setOnClickListener {
            viewModel.getGameHallList(MatchType.PARLAY, SportType.VOLLEYBALL)
        }

        card_game_soon.setOnClickListener {
            viewModel.getGameHallList(MatchType.AT_START, null)
        }

        card_lottery.setOnClickListener {
            viewModel.setGoToThirdGamePage(ThirdGameCategory.CGCP)
            activity?.finish()
        }

        card_live.setOnClickListener {
            viewModel.setGoToThirdGamePage(ThirdGameCategory.LIVE)
            activity?.finish()
        }

        card_poker.setOnClickListener {
            viewModel.setGoToThirdGamePage(ThirdGameCategory.QP)
            activity?.finish()
        }

        card_slot.setOnClickListener {
            viewModel.setGoToThirdGamePage(ThirdGameCategory.DZ)
            activity?.finish()
        }

        card_fishing.setOnClickListener {
            viewModel.setGoToThirdGamePage(ThirdGameCategory.BY)
            activity?.finish()
        }

        card_game_result.setOnClickListener {
            startActivity(Intent(activity, ResultsSettlementActivity::class.java))
        }

        card_update.setOnClickListener {
            startActivity(Intent(activity, VersionUpdateActivity::class.java))
        }
    }

    private fun subscribeHallChannel(code: String, match: Match) {
        //訂閱賽事並記錄到訂閱清單裡面
        mSubscribeMatchMap[code] = match
        service.subscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, match.id)
    }

    private fun unSubscribeAllHallChannel() {
        //離開畫面時取消訂閱所有賽事
        mSubscribeMatchMap.forEach {
            val code = it.key
            val matchId = it.value.id
            service.unSubscribeHallChannel(code, CateMenuCode.HDP_AND_OU.code, matchId)
        }
        mSubscribeMatchMap.clear()
    }

    private fun initObserve() {
        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, Observer {
            updateUI(it)
        })

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, Observer {
            updateUI(it)
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

    private fun updateUI(gameCateList: List<GameCateData>?) {
        val isShowThirdGame = sConfigData?.thirdOpen == FLAG_OPEN
        val lotteryCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.CGCP }?.tabDataList?.sumBy { it.gameList.size }?: 0
        val liveCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.LIVE }?.tabDataList?.sumBy { it.gameList.size }?: 0
        val pokerCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.QP }?.tabDataList?.sumBy { it.gameList.size }?: 0
        val slotCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.DZ }?.tabDataList?.sumBy { it.gameList.size }?: 0
        val fishingCount = gameCateList?.find { it.categoryThird == ThirdGameCategory.BY }?.tabDataList?.sumBy { it.gameList.size }?: 0

        card_lottery.visibility = if (isShowThirdGame && lotteryCount > 0) View.VISIBLE else View.GONE
        card_live.visibility = if (isShowThirdGame && liveCount > 0) View.VISIBLE else View.GONE
        card_poker.visibility = if (isShowThirdGame && pokerCount > 0) View.VISIBLE else View.GONE
        card_slot.visibility = if (isShowThirdGame && slotCount > 0) View.VISIBLE else View.GONE
        card_fishing.visibility = if (isShowThirdGame && fishingCount > 0) View.VISIBLE else View.GONE
    }

    private fun updateUI(result: MatchPreloadResult) {
        unSubscribeAllHallChannel() //先清除之前訂閱項目

        //訂閱所有滾球賽事
        result.matchPreloadData?.datas?.forEach { data ->
            data.matchs.forEach { match ->
                subscribeHallChannel(data.code, match)
            }
        }

        drawer_in_play.setCount(result.matchPreloadData?.num?.toString())
        drawer_in_play.setRvGameData(result.matchPreloadData)
        drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                //使用於投注細項 -> [更多]
                val selectData =
                    result.matchPreloadData?.datas?.find { data -> select.code == data.code }
                selectData?.matchs?.let { list -> viewModel.setOddsDetailMoreList(list) }

                scroll_view.smoothScrollTo(0, 0)
                viewModel.getOddsDetail(select)
            }
        })
        drawer_in_play.setOnSelectFooterListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                scroll_view.smoothScrollTo(0, 0)
                viewModel.getGameHallList(MatchType.IN_PLAY, select.code)
            }
        })
    }
}