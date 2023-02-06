package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.distinctUntilChanged
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_sport.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.sport.SportListFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import org.cxct.sportlottery.util.HomePageStatusManager
import org.cxct.sportlottery.util.isUAT
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.util.startRegister
import org.greenrobot.eventbus.EventBus


class SportFragment : BaseBottomNavigationFragment<SportTabViewModel>(SportTabViewModel::class) {

    companion object {
        fun newInstance(matchType: MatchType? = null, gameType: GameType? = null): SportFragment {
            val args = Bundle()
            val fragment = SportFragment()
            matchType?.let {
                args.putSerializable("matchType", it)
            }
            gameType?.let {
                args.putSerializable("gameType", it)
            }
            fragment.arguments = args
            return fragment
        }

        val matchTypeTabPositionMap = mapOf<MatchType, Int>(
            MatchType.IN_PLAY to 0,
            MatchType.AT_START to 1,
            MatchType.TODAY to 2,
            MatchType.EARLY to 3,
            MatchType.PARLAY to 4,
            MatchType.CS to 5,
            MatchType.OUTRIGHT to 6,
            MatchType.MAIN to 99
        )
    }

    private var betListFragment = BetListFragment()
    private var showFragment: Fragment? = null

    var jumpMatchType: MatchType? = null
    var jumpGameType: GameType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_sport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolBar()
        initTabLayout()
        initObserve()
        viewModel.getMatchData()
        viewModel.firstSwitchMatch(jumpMatchType ?: MatchType.IN_PLAY)
        navGameFragment(jumpMatchType ?: MatchType.IN_PLAY)
    }

    fun initToolBar() {
        lin_toolbar.fitsSystemStatus()
//        lin_toolbar.toolBar.setBackgroundColor(Color.parseColor("#ffffff"))
        lin_toolbar.setBackgroundColor(Color.parseColor("#ffffff"))
        iv_menu_left.setOnClickListener {
            (activity as MainTabActivity).showLeftFrament(1, tabLayout.selectedTabPosition)
            EventBus.getDefault().post(MenuEvent(true))
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startRegister(requireContext())
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        lin_search.setOnClickListener {
            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
        }
        setupLogin()
    }

    fun showLoginNotify() {
        (activity as MainTabActivity).showLoginNotify()
    }

    fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        (activity as MainTabActivity).showMyFavoriteNotify(myFavoriteNotifyType)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        showFragment?.let {
            if (it.isAdded)
                it.onHiddenChanged(hidden)
        }
    }

    private fun initTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectTab(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)

    }

    private fun refreshTabLayout(sportMenuResult: SportMenuResult?) {
        try {
            val countInPlay =
                sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
            val countAtStart =
                sportMenuResult?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0
            val countToday =
                sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            val countEarly =
                sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            val countCS =
                sportMenuResult?.sportMenuData?.menu?.cs?.items?.sumBy { it.num } ?: 0
            val countOutright =
                sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0
            val countParlay =
                sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0


            tabLayout.getTabAt(0)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_in_play)
                tv_number?.text = countInPlay.toString()
            }

            tabLayout.getTabAt(1)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_at_start)
                tv_number?.text = countAtStart.toString()
            }

            tabLayout.getTabAt(2)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_today)
                tv_number?.text = countToday.toString()
            }

            tabLayout.getTabAt(3)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_early)
                tv_number?.text = countEarly.toString()
            }
            tabLayout.getTabAt(4)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_parlay)
                tv_number?.text = countParlay.toString()
            }
            tabLayout.getTabAt(5)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_cs)
                tv_number?.text = countCS.toString()
            }
            tabLayout.getTabAt(6)?.customView?.apply {
                tv_title?.text = getString(R.string.home_tab_outright)
                tv_number?.text = countOutright.toString()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun selectTab(position: Int?) {
        if (position == null) return
        var matchType =
            matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
        viewModel.setCurMatchType(matchType)
        navGameFragment(matchType)
    }

    private fun initObserve() {
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        viewModel.userMoney.observe(viewLifecycleOwner) {

        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        viewModel.curMatchType.distinctUntilChanged().observe(viewLifecycleOwner) {
            it?.let {
                matchTypeTabPositionMap[it]?.let { it1 -> tabLayout.getTabAt(it1)?.select() }
            }
        }

        viewModel.sportMenuResult.distinctUntilChanged().observe(viewLifecycleOwner) {
            hideLoading()
            updateUiWithResult(it)
        }
    }

    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(requireActivity(), MainActivity::class.java)
                .putExtra(ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)
            return
        }

        GamePublicityActivity.reStart(requireContext())
    }


    private fun updateUiWithResult(sportMenuResult: SportMenuResult?) {
        if (sportMenuResult?.success == true) {
            refreshTabLayout(sportMenuResult)
            EventBus.getDefault().post(sportMenuResult)
        }
    }

    private fun updateSelectTabState(matchType: MatchType?) {
        matchTypeTabPositionMap[matchType]?.let {
            updateSelectTabState(it)
        }
    }

    private fun updateSelectTabState(position: Int) {
        val tab = tabLayout.getTabAt(position)?.customView

        tab?.let {
            clearSelectTabState()
            tabLayout.getTabAt(position)?.select()
            it.tv_title?.isSelected = true
            it.tv_number?.isSelected = true
        }
    }

    private fun clearSelectTabState() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)?.customView

            tab?.tv_title?.isSelected = false
            tab?.tv_number?.isSelected = false
        }
    }

    private fun removeBetListFragment() {
        childFragmentManager.beginTransaction().remove(betListFragment).commit()
    }


    override fun onDestroy() {
        expandCheckList.clear()
        HomePageStatusManager.clear()
        super.onDestroy()
    }

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it && !isUAT()
            btn_login.isVisible = !it
            lin_search.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }


    private fun navGameFragment(matchType: MatchType) {
        var gameType = jumpGameType?.key
        showFragment = when (matchType) {
            MatchType.OUTRIGHT ->
                SportOutrightFragment.newInstance(gameType = gameType).apply {
                    offsetScrollListener = ::setTabElevation
                }

            else ->
                SportListFragment.newInstance(matchType = matchType, gameType = gameType).apply {
                    offsetScrollListener = ::setTabElevation
                }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_content, showFragment!!)
            .commit()
        jumpMatchType = null
        jumpGameType = null
    }

    private val maxElevation = 5.dp
    private fun setTabElevation(elevation: Double) {
        val elevation = (elevation * elevation * maxElevation).toFloat()
        tabLayout.elevation = elevation
        lin_toolbar.elevation = elevation
        vDivider1.elevation = elevation
        vDivider2.elevation = elevation
    }

    fun setJumpSport(matchType: MatchType, gameType: GameType) {
        jumpMatchType = matchType
        jumpGameType = gameType
        if (isAdded) {
            //如果体育当前已经在指定的matchType页面时，跳过检查重复选中的机制，强制筛选sportListFragment
            viewModel.setCurMatchType(matchType)
            navGameFragment(matchType)
        }
    }

    fun updateSportMenuResult(sportMenuResult: SportMenuResult) {
        viewModel.setSportMenuResult(sportMenuResult)
    }

    fun getCurMatchType(): MatchType {
        return matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
    }

    fun getCurGameType(): GameType? {
        return when (showFragment) {
            is SportListFragment -> {
                (showFragment as SportListFragment).getCurGameType()
            }

            is SportOutrightFragment -> {
                (showFragment as SportOutrightFragment).getCurGameType()
            }

            else -> {
                null
            }
        }
    }
}
