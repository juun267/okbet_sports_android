package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import com.google.android.material.tabs.TabLayout
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import kotlinx.android.synthetic.main.fragment_main_home.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.tv_balance
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.tv_balance_currency
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import kotlinx.android.synthetic.main.view_toolbar_main.btn_login
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.main.news.NewsDialog
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.sport.SportListFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.*


class SportFragment : BaseBottomNavigationFragment<SportViewModel>(SportViewModel::class) {

    companion object {
        fun newInstance(): SportFragment {
            val args = Bundle()
            val fragment = SportFragment()
            fragment.arguments = args
            return fragment
        }
    }


    private var betListFragment = BetListFragment()

    private var mOutrightLeagueId: String? = null //主頁跳轉冠軍頁時傳遞的聯賽Id


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
        viewModel.firstSwitchMatch(MatchType.IN_PLAY)
        setupDataSourceChange()
    }

    override fun onStart() {
        super.onStart()

//        if (isFromPublicity) {
        arguments?.let {
            val matchId = it.getString(GamePublicityActivity.PUBLICITY_MATCH_ID)
            val gameTypeCode = it.getString(GamePublicityActivity.PUBLICITY_GAME_TYPE)
            val gameType = GameType.getGameType(gameTypeCode) ?: GameType.OTHER
            val intentMatchType = it.getSerializable(GamePublicityActivity.PUBLICITY_MATCH_TYPE)
            val matchType = if (intentMatchType != null) intentMatchType as MatchType else null
            val matchList =
                it.getParcelableArrayList<MatchInfo>(GamePublicityActivity.PUBLICITY_MATCH_LIST)
            matchId?.let {
//                   navDetailLiveFragment(
//                       matchID = matchId, gameType = gameType, matchType = matchType, matchList = matchList
//                   )
            }
        }

//        }
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        tv_balance_currency.text = sConfigData?.systemCurrencySign
        setupLogin()
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }
        iv_money_refresh.setOnClickListener {
            refreshMoneyLoading()
            viewModel.getMoney()
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
    }


    fun showBetListPage() {
        val transaction = childFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )

        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean,
                ) {
//                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }
            })

        transaction
            .add(R.id.fl_bet_list, betListFragment, BetListFragment::class.java.simpleName)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()

    }

    fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
        cl_bet_list_bar.isVisible = num > 0
        line_shadow.isVisible = !cl_bet_list_bar.isVisible
        tv_bet_list_count.text = num.toString()
        if (num > 0) viewModel.getMoney()
    }

    fun showLoginNotify() {
//        snackBarLoginNotify.apply {
//            setAnchorView(R.id.game_bottom_navigation)
//            show()
//        }
    }

    fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
//        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
//        snackBarMyFavoriteNotify?.apply {
//            setAnchorView(R.id.game_bottom_navigation)
//            show()
//        }
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
//            val countParlay =
//                sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
            val countOutright =
                sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0


            //20220728 不要有主頁
            /*tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.MAIN) ?: 0)?.view?.visibility = View.GONE
            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.MAIN) ?: 0)?.customView?.apply {
                visibility = View.GONE
                *//*tv_title?.setTextWithStrokeWidth(getString(R.string.home_tan_main), 0.7f)
                tv_number?.text = countParlay.plus(countInPlay).plus(countAtStart).plus(countToday).plus(countEarly)
                    .plus(countOutright).plus(countEps).toString()*//*
            }*/

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.IN_PLAY) ?: 1)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_in_play), 0.7f)
                tv_number?.text = countInPlay.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.AT_START)
                ?: 2)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_at_start), 0.7f)
                tv_number?.text = countAtStart.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.TODAY) ?: 3)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_today), 0.7f)
                tv_number?.text = countToday.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.EARLY) ?: 4)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_early), 0.7f)
                tv_number?.text = countEarly.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.CS) ?: 5)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_cs), 0.7f)
                tv_number?.text = countCS.toString()
            }

            tabLayout.getTabAt(
                getMatchTypeTabPosition(MatchType.OUTRIGHT) ?: 6
            )?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_outright), 0.7f)
                tv_number?.text = countOutright.toString()
            }


            //0401需求先隱藏特優賠率
//            val tabEps = tabLayout.getTabAt(7)?.customView
//            tabEps?.tv_title?.setText(R.string.home_tab_eps)
//            tabEps?.tv_number?.text = countEps.toString()

            //英文 越南文稍微加寬padding 不然會太擠
            if (LanguageManager.getSelectLanguage(requireContext()) != LanguageManager.Language.ZH) {
                for (i in 0 until tabLayout.tabCount) {
                    tabLayout.getTabAt(i)?.customView.apply {
                        this?.setPadding(8.dp, 0, 16.dp, 0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val matchTypeTabPositionMap = mapOf<MatchType, Int>(
        //20220728 隱藏主頁
        /*MatchType.MAIN to 0,
        MatchType.IN_PLAY to 1,
        MatchType.AT_START to 2,
        MatchType.TODAY to 3,
        MatchType.EARLY to 4,
        MatchType.OUTRIGHT to 5,
        MatchType.PARLAY to 6,
        MatchType.EPS to 7*/
        MatchType.IN_PLAY to 0,
        MatchType.AT_START to 1,
        MatchType.TODAY to 2,
        MatchType.EARLY to 3,
        MatchType.CS to 4,
        MatchType.OUTRIGHT to 5,
//        MatchType.PARLAY to 6,
//        MatchType.EPS to 7,
        MatchType.MAIN to 99
    )

    private val matchTypeTabPositionONbetMap = mapOf<MatchType, Int>(
        MatchType.IN_PLAY to 0,
        MatchType.AT_START to 1,
        MatchType.TODAY to 2,
        MatchType.EARLY to 3,
        MatchType.CS to 4,
        MatchType.OUTRIGHT to 5,
//        MatchType.PARLAY to 5,
//        MatchType.EPS to 6,
        MatchType.MAIN to 99
    )

    /**
     * 根據MatchTypeTabPositionMap獲取MatchType的tab position
     *
     * @see MatchTypeTabPositionMap
     */
    private fun getMatchTypeTabPosition(matchType: MatchType?): Int? = when (matchType) {
        null -> {
            Timber.e("Unable to get $matchType tab position")
            null
        }
        else -> {
            when (val tabPosition = matchTypeTabPositionMap[matchType]) {
                null -> {
                    Timber.e("There is not tab position of $matchType")
                    null
                }
                else -> {
                    tabPosition
                }
            }
        }
    }

    private fun selectTab(position: Int?) {
        if (position == null) return

        when (position) {
            getMatchTypeTabPosition(MatchType.MAIN) -> {
                viewModel.switchMatchType(MatchType.MAIN)
            }
            getMatchTypeTabPosition(MatchType.IN_PLAY) -> {
                viewModel.switchMatchType(MatchType.IN_PLAY)
            }
            getMatchTypeTabPosition(MatchType.AT_START) -> {
                viewModel.switchMatchType(MatchType.AT_START)
            }
            getMatchTypeTabPosition(MatchType.TODAY) -> {
                viewModel.switchMatchType(MatchType.TODAY)
            }
            getMatchTypeTabPosition(MatchType.EARLY) -> {
                viewModel.switchMatchType(MatchType.EARLY)
            }
            getMatchTypeTabPosition(MatchType.CS) -> {
                viewModel.switchMatchType(MatchType.CS)
            }
            getMatchTypeTabPosition(MatchType.OUTRIGHT) -> {
                /**
                 * 若mOutrightLeagueId有值的話, 此行為為主頁點擊聯賽跳轉至冠軍頁, 跳轉行為於HomeFragment處理
                 *
                 * @see org.cxct.sportlottery.ui.game.home.HomeFragment.navGameOutright
                 */
                if (mOutrightLeagueId.isNullOrEmpty()) {
                    viewModel.switchMatchType(MatchType.OUTRIGHT)
                }
            }
//            getMatchTypeTabPosition(MatchType.PARLAY) -> {
//                viewModel.switchMatchType(MatchType.PARLAY)
//            }
//            getMatchTypeTabPosition(MatchType.EPS) -> {
//                viewModel.switchMatchType(MatchType.EPS)
//            }
        }
    }


    //用戶登入公告訊息彈窗
    private var mNewsDialog: NewsDialog? = null
    private fun setNewsDialog(messageListResult: MessageListResult) {

        //未登入、遊客登入都要顯示彈窗
        //顯示規則：帳號登入前= 公告含登入前、帳號登入後= 公告含登入前+登入後
        var list = listOf<Row>()
        list = if (viewModel.isLogin.value == true)
            messageListResult.rows?.filter { it.type.toInt() != 1 } ?: listOf()
        else
            messageListResult.rows?.filter { it.type.toInt() == 3 } ?: listOf()

        if (!list.isNullOrEmpty()) {
            if (!MultiLanguagesApplication.getInstance()?.isNewsShow()!!) {
                mNewsDialog?.dismiss()
                mNewsDialog = NewsDialog(list)
                mNewsDialog?.show(childFragmentManager, null)
                MultiLanguagesApplication.getInstance()?.setIsNewsShow(true)
            }
        }
    }

    private fun initObserve() {
        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let { money ->
                refreshMoneyHideLoading()
                tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.settlementNotificationMsg.observe(viewLifecycleOwner) {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {
            getAnnouncement()
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(requireContext(), childFragmentManager)
            }
        }

        viewModel.showBetUpperLimit.observe(viewLifecycleOwner) {
            if (it.getContentIfNotHandled() == true) {

            }
//                snackBarBetUpperLimitNotify.apply {
//                    setAnchorView(R.id.game_bottom_navigation)
//                    show()
//                }
        }

        viewModel.nowTransNum.observe(viewLifecycleOwner) {
            navigation_transaction_status.trans_number.text = it.toString()
        }

        viewModel.specialEntrance.observe(viewLifecycleOwner) {
            hideLoading()
            if (it?.couponCode.isNullOrEmpty()) {
                when (it?.entranceMatchType) {
                    MatchType.MAIN -> {
                        //do nothing
                    }
                    MatchType.OTHER -> {
                        goTab(3)
                    }
                    MatchType.DETAIL -> {
                        it.matchID?.let { matchId ->
//                            navDetailLiveFragment(matchId, it.gameType ?: GameType.OTHER, it.gameMatchType)
                        }
                    }
                    else -> {
                        getMatchTypeTabPosition(it?.entranceMatchType)?.let { matchTypePosition ->
                            goTab(matchTypePosition)
                        }
                    }
                }
            } else if (it?.entranceMatchType == MatchType.DETAIL) {

            } else {
//                navGameFragment(it!!.entranceMatchType)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        viewModel.curMatchType.distinctUntilChanged().observe(viewLifecycleOwner) {
            Log.d("hjq", "curMatchType=" + it?.name)
            it?.let {
                val tabSelectedPosition = tabLayout.selectedTabPosition
                when (it) {
                    MatchType.MAIN -> {

                    }
                    else -> {
                        //僅有要切換的MatchType與當前選中的Tab相同時才繼續進行後續的切頁行為, 避免快速切頁導致切頁邏輯進入無窮迴圈
                        when {
                            it == MatchType.IN_PLAY && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.IN_PLAY) ||
                                    it == MatchType.AT_START && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.AT_START) ||
                                    it == MatchType.TODAY && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.TODAY) ||
                                    it == MatchType.EARLY && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.EARLY) ||
                                    it == MatchType.CS && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.CS) ||
                                    it == MatchType.OUTRIGHT && tabSelectedPosition == getMatchTypeTabPosition(
                                MatchType.OUTRIGHT)
                            -> {
                                navGameFragment(it)
                            }
                            else -> {
                                //do nothing
                            }
                        }

                    }
                }
            }
        }

        viewModel.sportMenuResult.observe(viewLifecycleOwner) {
            hideLoading()
            updateUiWithResult(it)
        }
        viewModel.errorPromptMessage.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()
                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }

        }

        viewModel.showBetInfoSingle.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
    }

    /**
     * 前往指定的賽事種類
     * @since 若已經在該賽事種類, 點擊Tab不會觸發OnTabSelectedListener
     */
    private fun goTab(tabPosition: Int) {
        if (tabLayout.selectedTabPosition != tabPosition) {
            //賽事類別Tab不在滾球時, 點擊滾球Tab
            tabLayout.getTabAt(tabPosition)?.select()
        } else {
            selectTab(tabPosition)
        }
    }

    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility = View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
            iv_menu.visibility = View.GONE
            iv_notice.visibility = View.GONE
            btn_register.setVisibilityByCreditSystem()
            toolbar_divider.setVisibilityByCreditSystem()
        }
    }

    fun updateOddsType(oddsType: OddsType) {
        tv_odds_type.text = getString(oddsType.res)
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
        }
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
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

    private fun setupDataSourceChange() {
        setDataSourceChangeEvent {
            viewModel.fetchDataFromDataSourceChange(
                matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
            )
        }
    }

    private fun setupLogin() {
        val isLogin = viewModel.isLogin != null && viewModel.isLogin.value!!
        lin_money.visibility = if (isLogin) View.VISIBLE else View.GONE
        btn_login.visibility = if (isLogin) View.GONE else View.VISIBLE
    }

    private fun refreshMoneyLoading() {
        var anim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate)
        anim.interpolator = LinearInterpolator()
        iv_money_refresh.startAnimation(anim)
    }

    private fun refreshMoneyHideLoading() {
        iv_money_refresh.clearAnimation()
    }

    private fun navGameFragment(matchType: MatchType) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_content, SportListFragment.newInstance(matchType = matchType))
            .commit()

    }
}