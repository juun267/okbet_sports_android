package org.cxct.sportlottery.ui.sport

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.distinctUntilChanged
import kotlinx.android.synthetic.main.fragment_sport.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentSport2Binding
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.endscore.EndScoreFragment
import org.cxct.sportlottery.ui.sport.list.SportListFragment
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper2
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class SportFragment2: BindingSocketFragment<SportTabViewModel, FragmentSport2Binding>() {

    companion object {
        val matchTypeTabPositionMap = mapOf(
            0 to MatchType.END_SCORE,
            1 to MatchType.IN_PLAY,
            2 to MatchType.AT_START,
            3 to MatchType.TODAY,
            4 to MatchType.EARLY,
            5 to MatchType.PARLAY,
            6 to MatchType.CS,
            7 to MatchType.OUTRIGHT
        )
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private val fragmentHelper by lazy { FragmentHelper2(childFragmentManager, R.id.fl_content) }

    var jumpMatchType: MatchType? = null
    var jumpGameType: GameType? = null
    //根据赛事数量判断默认的分类
    var defaultMatchType: MatchType? = null

    override fun onHiddenChanged(hidden: Boolean) {
        fragmentHelper.currentFragment()?.let {
            if (it.isAdded)
                it.onHiddenChanged(hidden)
        }
    }

    override fun onInitView(view: View) {
        initToolBar()
        initTabLayout()
    }

    override fun onBindViewStatus(view: View) {
        initObserve()
        viewModel.getMatchData()
        jumpMatchType?.let {
            viewModel.firstSwitchMatch(it)
            navGameFragment(it)
        }
    }

    fun initToolBar() = binding.homeToolbar.run {
        attach(this@SportFragment2, getMainTabActivity(), viewModel, false)
        setBackgroundColor(Color.WHITE)
        searchView.setOnClickListener { startActivity(SportSearchtActivity::class.java) }
        ivMenuLeft.setOnClickListener {
            getMainTabActivity().showSportLeftMenu(getCurMatchType(), getCurGameType())
            EventBusUtil.post(MenuEvent(true))
        }
    }

    private fun initTabLayout() = binding.tabLayout.run {
        setBackgroundColor(Color.WHITE)
        addOnTabSelectedListener(TabSelectedAdapter{ selectTab(it.position) })
        OverScrollDecoratorHelper.setUpOverScroll(this)
    }

    private fun refreshTabLayout(sportMenuResult: ApiResult<SportMenuData>) {

        val sportMenuData = sportMenuResult.getData()
        val countInPlay = sportMenuData?.menu?.inPlay?.items?.sumOf { it.num } ?: 0
        val countAtStart = sportMenuData?.atStart?.items?.sumOf { it.num } ?: 0
        val countToday = sportMenuData?.menu?.today?.items?.sumOf { it.num } ?: 0
        val countEarly = sportMenuData?.menu?.early?.items?.sumOf { it.num } ?: 0
        val countCS = sportMenuData?.menu?.cs?.items?.sumOf { it.num } ?: 0
        val countOutright = sportMenuData?.menu?.outright?.items?.sumOf { it.num } ?: 0
        val countParlay = sportMenuData?.menu?.parlay?.items?.sumOf { it.num } ?: 0
        val countBkEnd = sportMenuData?.menu?.bkEnd?.items?.sumOf { it.num } ?: 0
        defaultMatchType = when {
            countInPlay > 0 -> MatchType.IN_PLAY
            countAtStart > 0 -> MatchType.AT_START
            countToday > 0 -> MatchType.TODAY
            else -> MatchType.EARLY
        }
        addTab(getString(R.string.home_tab_end_score), countBkEnd, 0)
        addTab(getString(R.string.home_tab_in_play), countInPlay, 1)
        addTab(getString(R.string.home_tab_at_start), countAtStart, 2)
        addTab(getString(R.string.home_tab_today), countToday, 3)
        addTab(getString(R.string.home_tab_early), countEarly, 4)
        addTab(getString(R.string.home_tab_parlay), countParlay, 5)
        addTab(getString(R.string.home_tab_cs), countCS, 6)
        addTab(getString(R.string.home_tab_outright), countOutright, 7)
    }

    private fun addTab(name: String, num: Int, position: Int) {

        val tab = if (tabLayout.tabCount > position) {
            binding.tabLayout.getTabAt(position)!!
        } else {
            binding.tabLayout.newTab().setCustomView(R.layout.home_cate_tab).apply {
                binding.tabLayout.addTab(this, position, false)
            }
        }

        tab.customView?.run {
            tv_title.text = name
            tv_number.text = num.toString()
        }
    }


    private fun selectTab(position: Int) {
        var matchType =  matchTypeTabPositionMap[position] ?: return
        viewModel.setCurMatchType(matchType)
        navGameFragment(matchType)
    }

    private var lastMatchType: MatchType? = null
    private var lastGameType: String? = null
    private fun navGameFragment(matchType: MatchType) {
        var gameType = jumpGameType?.key

        if (lastMatchType == matchType && lastGameType == gameType) {
            return
        }

        lastMatchType = matchType
        lastGameType = gameType

        val args = Bundle()
        args.putSerializable("matchType", matchType)
        args.putString("gameType", gameType)

        when (matchType) {
            MatchType.OUTRIGHT -> {
                fragmentHelper.show(SportOutrightFragment::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                }
            }

            MatchType.END_SCORE -> {
                fragmentHelper.show(EndScoreFragment::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                }
            }

            else -> {
                fragmentHelper.show(SportListFragment2::class.java, args) { fragment, newInstance ->
                    fragment.offsetScrollListener = ::setTabElevation
                    if (!newInstance) {
                        fragment.reload()
                    }
                }
            }
        }

        jumpMatchType = null
        jumpGameType = null
    }

    private val maxElevation = 5.dp
    private fun setTabElevation(elevation: Double) = binding.run {
        val elevation = (elevation * elevation * maxElevation).toFloat()
        tabLayout.elevation = elevation
        homeToolbar.elevation = elevation
        vDivider1.elevation = elevation
        vDivider2.elevation = elevation
    }

    private fun initObserve() = viewModel.run {

        //使用者沒有電話號碼
        showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        curMatchType.distinctUntilChanged().observe(viewLifecycleOwner) { matchType->
            if (matchType == null) {
                return@observe
            }
            val position = matchTypeTabPositionMap.entries.find { it.value == matchType }?.key ?: return@observe
            binding.tabLayout.getTabAt(position)?.select()
        }

        sportMenuResult.observe(viewLifecycleOwner) {
            hideLoading()
            updateUiWithResult(it)
        }
    }

    private fun updateUiWithResult(sportMenuResult: ApiResult<SportMenuData>) {
        if (!sportMenuResult.succeeded()) {
            return
        }

        val isFirstSwitch = defaultMatchType == null
        refreshTabLayout(sportMenuResult)
        EventBusUtil.post(sportMenuResult)
        if (!isFirstSwitch) {
            return
        }

        val matchType = viewModel.curMatchType.value
        if (matchType != null) {
            val position = matchTypeTabPositionMap.entries.find { it.value == matchType }?.key ?: return
            binding.tabLayout.getTabAt(position)?.select()
            return
        }

        defaultMatchType?.let {
            viewModel.setCurMatchType(it)
            navGameFragment(it)
        }
    }

    fun updateSportMenuResult(sportMenuResult: ApiResult<SportMenuData>) {
        viewModel.setSportMenuResult(sportMenuResult)
    }

    private fun getCurMatchType(): MatchType {
        return kotlin.runCatching {
            matchTypeTabPositionMap[tabLayout.selectedTabPosition]
        }.getOrNull() ?: MatchType.IN_PLAY
    }

    private fun getCurGameType(): GameType? = when (val fragment = fragmentHelper.currentFragment()) {
        is SportListFragment -> {
            fragment.getCurGameType()
        }

        is SportOutrightFragment -> {
            fragment.getCurGameType()
        }

        is EndScoreFragment -> {
            fragment.getCurGameType()
        }

        else -> {
            null
        }
    }


}