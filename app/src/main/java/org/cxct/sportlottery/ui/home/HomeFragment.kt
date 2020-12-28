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
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.gameDrawer.GameEntity
import org.cxct.sportlottery.ui.odds.OddsDetailFragment

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

        viewModel.matchPreloadEarly.observe(this.viewLifecycleOwner, Observer {
            drawer_early.setCount(it.matchPreloadData?.num.toString())
            drawer_early.setRvGameData(it.matchPreloadData)
            drawer_early.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    //TODO simon test review 設定點擊事件跳轉畫面
                }
            })
    private val observerEarlyGame = Observer<MatchPreloadResult> {
        drawer_early.setCount(it.matchPreloadData?.num.toString())
        drawer_early.setRvGameData(it.matchPreloadData)
        drawer_early.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
            override fun onClick(select: GameEntity) {
                toOddsDetail(select, (requireActivity() as MainActivity).getScrollViewHeight())
            }
        })

        viewModel.matchPreloadInPlay.observe(this.viewLifecycleOwner, Observer {
            drawer_in_play.setCount(it.matchPreloadData?.num.toString())
            drawer_in_play.setRvGameData(it.matchPreloadData)
            drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    //TODO simon test review 設定點擊事件跳轉畫面
                }
            })
        })

        viewModel.matchPreloadToday.observe(this.viewLifecycleOwner, Observer {
            drawer_today.setCount(it.matchPreloadData?.num.toString())
            drawer_today.setRvGameData(it.matchPreloadData)
            drawer_today.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    //TODO simon test review 設定點擊事件跳轉畫面
                }
            })
        })

        initEvent()
        queryData()
    }

    private fun initEvent() {
        card_football.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }

        card_basketball.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }

        card_game_soon.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }

        card_tennis.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }

        card_badminton.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }

        card_volleyball.setOnClickListener {
            //TODO simon test 實作點擊跳轉事件
        }
    }

    private fun queryData() {
        viewModel.getMatchPreload()
    }

    private fun toOddsDetail(select: GameEntity, height: Int) {
        switchFragment(
            OddsDetailFragment.newInstance(
                select.code,
                select.name,
                select.match!!.id,
                "EU",
                height
            )
        )
    }

}