package org.cxct.sportlottery.ui.maintab.menu

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sport_left.*
import kotlinx.android.synthetic.main.fragment_sport_left.tvOddsChangedTips
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.results.ResultsSettlementActivity
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.OddsModeUtil
import kotlin.math.exp

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
    private var expandBetsWay = true
    var matchType: MatchType = MatchType.MAIN
        set(value) {
            field = value
            if (isAdded) {
                updateMatchType()
            }
        }
    var gameType: GameType? = null
        set(value) {
            field = value
            if (isAdded) {
                updateGameType()
            }
        }

    private var worldcupSelected = false
        set(value) {
            field = value
            lin_worldcup?.isSelected = value
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
        updateMatchType()
        updateGameType()
    }

    private fun initView() {
        rg_type.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_sport -> {
                    lin_sport.isVisible = true
                    lin_inplay.isVisible = false
                    viewModel.getSportList()
                    if (!worldcupSelected) {
                        (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
                    }
                }
                R.id.rbtn_inplay -> {
                    lin_sport.isVisible = false
                    lin_inplay.isVisible = true
                    viewModel.getInPlayList()
                    if (!worldcupSelected) {
                        (activity as MainTabActivity).jumpToTheSport(
                            MatchType.IN_PLAY, GameType.ALL
                        )
                    }
                }
            }
        }
        lin_game_result.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            startActivity(Intent(requireActivity(), ResultsSettlementActivity::class.java))
        }

        lin_worldcup.isSelected = worldcupSelected
        lin_worldcup.isVisible = StaticData.worldCupOpened()
        lin_worldcup.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToHome(2)
        }
        lin_today.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            worldcupSelected = false
            (activity as MainTabActivity).jumpToTheSport(MatchType.TODAY, GameType.FT)
        }
        lin_sport_classify.setOnClickListener {
            expandSportClassify = !expandSportClassify
            rv_sport_classify.isVisible = expandSportClassify
            lin_sport_classify.isSelected = expandSportClassify
        }
        llBetWay.setOnClickListener {
            expandBetsWay = !expandBetsWay
            rgBetWays.isVisible = expandBetsWay
            llBetWay.isSelected = expandBetsWay
        }

        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        when (userInfo?.oddsChangeOption ?: 0) {
            OddsModeUtil.accept_any_odds -> rbAcceptAny.isChecked = true
            OddsModeUtil.accept_better_odds -> rbAcceptBetter.isChecked = true
            OddsModeUtil.never_accept_odds_change -> rbNeverAccept.isChecked = true
        }

        rgBetWays.setOnCheckedChangeListener { group, checkedId ->
            val option: Int = when (checkedId) {
                R.id.rbAcceptAny -> {
                    OddsModeUtil.accept_any_odds
                }
                R.id.rbAcceptBetter -> {
                    OddsModeUtil.accept_better_odds
                }
                else -> {
                    OddsModeUtil.never_accept_odds_change
                }
            }
            viewModel.updateOddsChangeOption(option)
        }


        lin_all_inplay.setOnClickListener {
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
        }
        iv_search.setOnClickListener {
            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
        }
        iv_setting.setOnClickListener {
            startActivity(Intent(requireActivity(), SettingCenterActivity::class.java))
        }
        lin_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
        }
        tvOddsChangedTips.setOnClickListener {
            showOddsChangeTips()
        }
    }

    private fun showOddsChangeTips() {
        val dialog = CustomAlertDialog(requireContext())
        dialog.setTitle(getString(R.string.str_if_accept_odds_changes_title))
        val message = """
                    ${getString(R.string.str_if_accept_odds_changes_des_subtitle)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des1)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des2)}
                    
                     ${getString(R.string.str_if_accept_odds_changes_des3)}
                """.trimIndent()
        dialog.setMessage(message)
        dialog.setCanceledOnTouchOutside(true)
        dialog.isCancelable = true
        dialog.setNegativeButtonText(null)
        dialog.setPositiveButtonText(getString(R.string.str_ok_i_got_it))
        dialog.setGravity(Gravity.START)
        dialog.mScrollViewMarginHorizon = 20
        dialog.setPositiveClickListener {
            dialog.dismiss()
        }
        dialog.show(childFragmentManager, null)
    }

    private fun initSportClassifyView() {
        rv_sport_classify.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        sportClassifyAdapter.setOnItemClickListener { adapter, view, position ->
            gameType = GameType.getGameType(sportClassifyAdapter.getItem(position)?.code)
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(
                MatchType.EARLY,
                GameType.getGameType(sportClassifyAdapter.getItem(position)?.code) ?: GameType.FT
            )
        }
        rv_sport_classify.adapter = sportClassifyAdapter
    }

    private fun initSportInPlayView() {
        rv_sport_inplay.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        sportInPlayAdapter.setOnItemClickListener { _, _, position ->
            gameType = GameType.getGameType(sportInPlayAdapter.getItem(position)?.code)
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(
                MatchType.IN_PLAY, gameType ?: GameType.ALL
            )
        }
        rv_sport_inplay.adapter = sportInPlayAdapter
    }

    private fun initObserver() {
        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            it?.let {
                sportClassifyAdapter.setNewInstance(it?.toMutableList())
            }
        }
        viewModel.inplayList.observe(viewLifecycleOwner) {
            it?.let {
                if (it.size > 8) {
                    sportInPlayAdapter.setNewInstance(it.subList(0, 8)?.toMutableList())
                } else {
                    sportInPlayAdapter.setNewInstance(it?.toMutableList())
                }

            }
        }
        viewModel.countByToday.observe(viewLifecycleOwner) {
            it?.let {
                tv_today_count.setText(it.toString())
            }
        }
    }

    private fun updateMatchType() {
        when (matchType) {
            MatchType.IN_PLAY -> {
                if (rbtn_inplay.isChecked) {
                    viewModel.getInPlayList()
                } else {
                    rbtn_inplay.isChecked = true
                }
            }
            else -> {
                if (rbtn_sport.isChecked) {
                    viewModel.getSportList()
                } else {
                    rbtn_sport.isChecked = true
                }
            }
        }
    }

    private fun updateGameType() {
        lin_sport_classify.isSelected = false
        when (matchType) {
            MatchType.IN_PLAY -> {
                sportInPlayAdapter.gameType = gameType
                sportClassifyAdapter.gameType = null
                lin_sport_classify.isSelected = false
                lin_today.isSelected = false
                worldcupSelected = false
            }
            MatchType.EARLY -> {
                sportInPlayAdapter.gameType = null
                sportClassifyAdapter.gameType = gameType
                lin_sport_classify.isSelected = expandSportClassify
                lin_today.isSelected = false
                worldcupSelected = false
            }
            MatchType.TODAY -> {
                sportInPlayAdapter.gameType = null
                sportClassifyAdapter.gameType = null
                lin_sport_classify.isSelected = false
                lin_today.isSelected = true
                worldcupSelected = false
            }
            else -> {
                sportInPlayAdapter.gameType = null
                sportClassifyAdapter.gameType = null
                lin_today.isSelected = false
            }
        }
    }

    fun selectWorldCup() {
        matchType = MatchType.MAIN
        gameType = null
        worldcupSelected = true
    }

}