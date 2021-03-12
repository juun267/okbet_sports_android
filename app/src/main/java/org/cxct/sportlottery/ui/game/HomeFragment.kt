package org.cxct.sportlottery.ui.game

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
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.home.gameDrawer.GameEntity


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
        drawer_in_play.release()
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
        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, Observer {
            unSubscribeAllHallChannel() //先清除之前訂閱項目

            //訂閱所有滾球賽事
            it.matchPreloadData?.datas?.forEach { data ->
                data.matchs.forEach { match ->
                    subscribeHallChannel(data.code, match)
                }
            }

            drawer_in_play.setCount(it.matchPreloadData?.num?.toString())
            drawer_in_play.setRvGameData(it.matchPreloadData)
            drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    //使用於投注細項 -> [更多]
                    val selectData =
                        it.matchPreloadData?.datas?.find { data -> select.code == data.code }
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

}