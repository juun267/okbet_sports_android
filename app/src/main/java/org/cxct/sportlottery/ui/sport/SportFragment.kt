package org.cxct.sportlottery.ui.sport

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.distinctUntilChanged
import kotlinx.android.synthetic.main.fragment_sport.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.endscore.EndScoreFragment
import org.cxct.sportlottery.ui.sport.list.SportListFragment
import org.cxct.sportlottery.ui.sport.outright.SportOutrightFragment
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.checkMainPosition
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class SportFragment : BaseBottomNavigationFragment<SportTabViewModel>(SportTabViewModel::class) {

    override fun layoutId() = R.layout.fragment_sport

    companion object {

        val matchTypeTabPositionMap = mapOf(
            MatchType.END_SCORE to 0,
            MatchType.IN_PLAY to 1,
            MatchType.AT_START to 2,
            MatchType.TODAY to 3,
            MatchType.EARLY to 4,
            MatchType.PARLAY to 5,
            MatchType.CS to 6,
            MatchType.OUTRIGHT to 7,
            MatchType.MAIN to 99
        )

        //判断是否显示篮球末位比分弹窗
        var showBKEndDialog = true
    }

    private var showFragment: Fragment? = null

    var jumpMatchType: MatchType? = null
    var jumpGameType: GameType? = null

    //根据赛事数量判断默认的分类
    var defaultMatchType: MatchType? = null

    override fun onBindView(view: View) {
        initToolBar()
        initTabLayout()
        initObserve()
        viewModel.getMatchData()
        jumpMatchType?.let {
            viewModel.firstSwitchMatch(it)
            navGameFragment(it)
        }

//        if (showBKEndDialog) {
//            showBKEndDialog = false
//            showBKEndDialog()
//        }
    }

    private inline fun getMainTabActivity() = activity as MainTabActivity

    fun initToolBar() = homeToolbar.run {
        attach(this@SportFragment, getMainTabActivity(), viewModel, false)
        setBackgroundColor(Color.WHITE)
        searchView.setOnClickListener { startActivity(SportSearchtActivity::class.java) }
        ivMenuLeft.setOnClickListener {
            getMainTabActivity().showSportLeftMenu(getCurMatchType(), getCurGameType())
            EventBusUtil.post(MenuEvent(true))
        }
    }

    override fun onResume() {
        super.onResume()
        //从侧边栏返回，检查体育服务是否维护
        val isClose=(activity as MainTabActivity).checkMainPosition(1)
        if(isClose){
            (activity as MainTabActivity).backMainHome()
        }
    }

    fun showLoginNotify() {
        (activity as MainTabActivity).showLoginNotify()
    }

    fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        (activity as MainTabActivity).showMyFavoriteNotify(myFavoriteNotifyType)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        showFragment?.let {
            if (it.isAdded)
                it.onHiddenChanged(hidden)
        }
    }

    private fun initTabLayout() {
        tabLayout.setBackgroundColor(Color.WHITE)
        tabLayout.addOnTabSelectedListener(TabSelectedAdapter{ selectTab(it.position) })
        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)
    }

    private fun refreshTabLayout(sportMenuResult: SportMenuResult) {

        val sportMenuData = sportMenuResult.sportMenuData
        val countInPlay = sportMenuData?.menu?.inPlay?.items?.sumOf { it.num } ?: 0
        val countAtStart = sportMenuData?.atStart?.items?.sumOf { it.num } ?: 0
        val countToday = sportMenuData?.menu?.today?.items?.sumOf { it.num } ?: 0
        val countEarly = sportMenuData?.menu?.early?.items?.sumOf { it.num } ?: 0
        val countCS = sportMenuData?.menu?.cs?.items?.sumOf { it.num } ?: 0
        val countOutright = sportMenuData?.menu?.outright?.items?.sumOf { it.num } ?: 0
        val countParlay = sportMenuData?.menu?.parlay?.items?.sumOf { it.num } ?: 0
        val countBkEnd = sportMenuData?.menu?.bkEnd?.items?.sumOf { it.num } ?: 0
        defaultMatchType =
            when {
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
            tabLayout.getTabAt(position)!!
        } else {
            tabLayout.newTab().setCustomView(R.layout.home_cate_tab).apply {
                tabLayout.addTab(this, position, false)
            }
        }

        tab.customView?.run {
            tv_title.text = name
            tv_number.text = num.toString()
        }
    }


    private fun selectTab(position: Int?) {
        if (position == null) return
        var matchType =
            matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
        viewModel.setCurMatchType(matchType)
        navGameFragment(matchType)
    }

    private fun initObserve() = viewModel.run {

        //使用者沒有電話號碼
        showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        curMatchType.distinctUntilChanged().observe(viewLifecycleOwner) {
            it?.let {
                matchTypeTabPositionMap[it]?.let { it1 ->
                    tabLayout.getTabAt(it1)?.select()
                }
            }
        }
        sportMenuResult.distinctUntilChanged().observe(viewLifecycleOwner) {
            hideLoading()
            updateUiWithResult(it)
        }
    }

    private fun updateUiWithResult(sportMenuResult: SportMenuResult?) {
        if (sportMenuResult?.success == true) {
            val isFirstSwitch = defaultMatchType == null
            refreshTabLayout(sportMenuResult)
            EventBusUtil.post(sportMenuResult)
            if (isFirstSwitch) {
                if (viewModel.curMatchType.value == null) {
                    if (defaultMatchType != null) {
                        viewModel.setCurMatchType(defaultMatchType)
                        navGameFragment(defaultMatchType!!)
                    }
                } else {
                    viewModel.curMatchType.value?.let {
                        matchTypeTabPositionMap[it]?.let { position ->
                            tabLayout.getTabAt(position)?.select()
                        }
                    }
                }
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
        var gameType = jumpGameType?.key
        showFragment = when (matchType) {
            MatchType.OUTRIGHT ->
                SportOutrightFragment().apply {
                    offsetScrollListener = ::setTabElevation
                }

            MatchType.END_SCORE ->
                EndScoreFragment().apply {
                    offsetScrollListener = ::setTabElevation
                }

            else ->
                SportListFragment().apply {
                    offsetScrollListener = ::setTabElevation
                }

        }.apply {
            val args = Bundle()
            args.putSerializable("matchType", matchType)
            args.putString("gameType", gameType)
            arguments = args
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.fl_content, showFragment!!)
            .commitAllowingStateLoss()

        jumpMatchType = null
        jumpGameType = null
    }

    private val maxElevation = 5.dp
    private fun setTabElevation(elevation: Double) {
        val elevation = (elevation * elevation * maxElevation).toFloat()
        tabLayout.elevation = elevation
        homeToolbar.elevation = elevation
        vDivider1.elevation = elevation
        vDivider2.elevation = elevation
    }

    fun setJumpSport(matchType: MatchType? = null, gameType: GameType? = null) {
        jumpMatchType = matchType
        jumpGameType = gameType
        if (isAdded) {
            //如果体育当前已经在指定的matchType页面时，跳过检查重复选中的机制，强制筛选sportListFragment
            jumpMatchType = jumpMatchType ?: defaultMatchType
            jumpMatchType?.let {
                viewModel.setCurMatchType(it)
                navGameFragment(it)
            }
        }
    }
    fun updateSportMenuResult(sportMenuResult: SportMenuResult) {
        viewModel.setSportMenuResult(sportMenuResult)
    }

    fun getCurMatchType(): MatchType {
        return kotlin.runCatching {
            matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
        }.getOrNull() ?: MatchType.IN_PLAY
    }

    fun getCurGameType(): GameType? {
        return when (showFragment) {
            is SportListFragment -> {
                (showFragment as SportListFragment).getCurGameType()
            }

            is SportOutrightFragment -> {
                (showFragment as SportOutrightFragment).getCurGameType()
            }

            is EndScoreFragment -> {
                (showFragment as EndScoreFragment).getCurGameType()
            }

            else -> {
                null
            }
        }
    }

//    fun showBKEndDialog() {
//        requireContext().newInstanceFragment<PopImageDialog>(Bundle().apply {
//            putInt(PopImageDialog.DrawableResID, R.drawable.img_bk_end)
//        }).apply {
//            onClick = {
//                this@SportFragment.viewModel.setCurMatchType(MatchType.END_SCORE)
//                navGameFragment(MatchType.END_SCORE)
//            }
//            onDismiss = {
//            }
//        }.show(childFragmentManager, PopImageDialog::class.simpleName)
//    }

}
