package org.cxct.sportlottery.ui.home

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
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.gameDrawer.GameEntity


class HomeFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    private lateinit var homeBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeBinding.apply {
            mainViewModel = this@HomeFragment.viewModel
            lifecycleOwner = this@HomeFragment
        }
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initEvent()
        initObserve()
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

    private fun initObserve() {
        viewModel.isLogin.observe(viewLifecycleOwner, Observer {
            if (it) {
                queryData()
            }
        })

        viewModel.matchPreloadInPlay.observe(viewLifecycleOwner, Observer {
            drawer_in_play.setCount(it.matchPreloadData?.num?.toString())
            drawer_in_play.setRvGameData(it.matchPreloadData)
            drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {

                    //使用於投注細項 -> [更多]
                   val selectData = it.matchPreloadData?.datas?.find { data -> select.code == data.code }
                    selectData?.matchs?.let { list -> viewModel.setOddsDetailMoreList(list) }

                    scroll_view.smoothScrollTo(0,0)
                    viewModel.getOddsDetail(select)
                }
            })
        })
    }

    private fun queryData() {
        viewModel.getInPlayMatchPreload()
    }

}