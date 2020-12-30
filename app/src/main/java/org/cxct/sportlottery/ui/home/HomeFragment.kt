package org.cxct.sportlottery.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
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
            drawer_early.setCount(it.matchPreloadData?.num?.toString())
            drawer_early.setRvGameData(it.matchPreloadData)
            drawer_early.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    toOddsDetail(select)
                }
            })
        })

        viewModel.matchPreloadInPlay.observe(this.viewLifecycleOwner, Observer {
            drawer_in_play.setCount(it.matchPreloadData?.num?.toString())
            drawer_in_play.setRvGameData(it.matchPreloadData)
            drawer_in_play.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    toOddsDetail(select)
                }
            })
        })

        viewModel.matchPreloadToday.observe(this.viewLifecycleOwner, Observer {
            drawer_today.setCount(it.matchPreloadData?.num?.toString())
            drawer_today.setRvGameData(it.matchPreloadData)
            drawer_today.setOnSelectItemListener(object : OnSelectItemListener<GameEntity> {
                override fun onClick(select: GameEntity) {
                    toOddsDetail(select)
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

    private fun switchFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, 0)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toOddsDetail(select: GameEntity) {

        (requireActivity() as MainActivity).getScrollView().smoothScrollTo(0, 0)
        (requireActivity() as MainActivity).getAppBarLayout().setExpanded(true, true)

        switchFragment(
            OddsDetailFragment.newInstance(
                select.code,
                select.name,
                select.match!!.id,
                "EU",
                (requireActivity() as MainActivity).getScrollView().getChildAt(0).height,
                (requireActivity() as MainActivity).getHeight()
            )
        )
    }




}