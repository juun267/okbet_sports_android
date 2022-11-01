package org.cxct.sportlottery.ui.maintab.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sport_left.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.observe
import org.greenrobot.eventbus.EventBus

class SportLeftFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    companion object {
        fun newInstance(): SportLeftFragment {
            val args = Bundle()
            val fragment = SportLeftFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val sportClassifyAdapter by lazy {
        SportClassifyAdapter(mutableListOf())
    }
    private val sportInPlayAdapter by lazy {
        SportInPlayAdapter(mutableListOf())
    }

    private var expandSportClassify = true
    var currentTab = 0
        set(value) {
            if (field != value) {
                field = value
                if (isAdded) {
                    when (value) {
                        0 -> {
                            if (rbtn_sport.isChecked) {
                                viewModel.getSportList()
                            } else {
                                rbtn_sport.isChecked = true
                            }
                        }
                        else -> {
                            if (rbtn_inplay.isChecked) {
                                viewModel.getInPlayList()
                            } else {
                                rbtn_inplay.isChecked = true
                            }
                        }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_sport_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initSportClassifyView()
        initSportInPlayView()
        initObserver()
    }

    private fun initView() {
        rg_type.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_sport -> {
                    lin_sport.isVisible = true
                    lin_inplay.isVisible = false
                    viewModel.getSportList()
                    currentTab = 0
                    (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
                }
                R.id.rbtn_inplay -> {
                    lin_sport.isVisible = false
                    lin_inplay.isVisible = true
                    viewModel.getInPlayList()
                    currentTab = 1
                    (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
                }
            }
        }
        rg_type.check(when (currentTab) {
            0 -> R.id.rbtn_sport
            else -> R.id.rbtn_inplay
        })
        lin_game_result.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
        }
        lin_worldcup.isVisible = sConfigData?.worldCupOpen == 1
        lin_worldcup.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(3)
        }
        lin_today.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.TODAY, GameType.FT)
        }
        lin_sport_classify.isSelected = expandSportClassify
        lin_sport_classify.setOnClickListener {
            expandSportClassify = !expandSportClassify
            rv_sport_classify.isVisible = expandSportClassify
            lin_sport_classify.isSelected = expandSportClassify
        }
        lin_all_inplay.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
        }
        iv_search.setOnClickListener {
            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
        }
        iv_setting.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingCenterActivity::class.java))
        }
    }

    private fun initSportClassifyView() {
        rv_sport_classify.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        sportClassifyAdapter.setOnItemClickListener { adapter, view, position ->
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY,
                GameType.getGameType(sportClassifyAdapter.getItem(position)?.code) ?: GameType.FT)
        }
        rv_sport_classify.adapter = sportClassifyAdapter
    }

    private fun initSportInPlayView() {
        rv_sport_inplay.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        sportInPlayAdapter.setOnItemClickListener { adapter, view, position ->
            EventBus.getDefault().post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY,
                GameType.getGameType(sportInPlayAdapter.getItem(position)?.code) ?: GameType.ALL)
        }
        rv_sport_inplay.adapter = sportInPlayAdapter
    }

    private fun initObserver() {
        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            it?.let {
                sportClassifyAdapter.setNewData(it)
            }
        }
        viewModel.inplayList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.size > 8) {
                    sportInPlayAdapter.setNewData(it.subList(0, 7))
                } else {
                    sportInPlayAdapter.setNewData(it)
                }

            }
        }
        viewModel.countByToday.observe(viewLifecycleOwner) {
            it?.let {
                tv_today_count.setText(it.toString())
            }
        }
    }

}