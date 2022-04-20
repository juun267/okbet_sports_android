package org.cxct.sportlottery.ui.game.hall

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.content_match_record.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_eps.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.*
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import kotlinx.android.synthetic.main.view_match_category_v4.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.season.Season
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.*
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.*
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.game.outright.GameOutrightFragmentDirections
import org.cxct.sportlottery.ui.game.outright.OutrightLeagueOddAdapter
import org.cxct.sportlottery.ui.game.outright.OutrightOddListener
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import java.util.*
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.M)
class GameV3Fragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), Animation.AnimationListener {

    private val args: GameV3FragmentArgs by navArgs()
    private var childMatchType = MatchType.OTHER
    private var mView: View? = null
    private var isReload = true // 重新加載用
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                loading()
                isReload = true
                unSubscribeChannelHallAll()
                viewModel.getSportMenu(args.matchType, onlyRefreshSportMenu = true)
                if (args.matchType == MatchType.OTHER) {
                    viewModel.getAllPlayCategoryBySpecialMatchType(item = it)
                } else {
                    viewModel.getAllPlayCategory(args.matchType)
                }
                viewModel.switchSportType(args.matchType, it)
//                notifyDataSetChanged()
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(args.matchType, it)
                loading()
            }
        }
    }

    private val matchCategoryPagerAdapter by lazy {
        MatchCategoryViewPagerAdapter(OnItemClickListener {
            navGameLeague(matchIdList = it.matchList, matchCategoryName = it.categoryDesc)
        })
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener(
                onClickSetItemListener = {
                    viewModel.switchPlay(args.matchType, it)
                    leagueAdapter.data.updateOddsSort()
                    leagueAdapter.notifyDataSetChanged()
                }, onClickNotSelectableListener = {
                    viewModel.switchPlay(args.matchType, it)
                    upDateSelectPlay(it)
                    leagueAdapter.data.updateOddsSort()
                    leagueAdapter.notifyDataSetChanged()
                }, onSelectPlayCateListener = { play, playCate ->
                    viewModel.switchPlayCategory(args.matchType, play, playCate.code)
                    upDateSelectPlay(play)
                    leagueAdapter.data.updateOddsSort()
                    leagueAdapter.notifyDataSetChanged()
                })
        }
    }

    private val countryAdapter by lazy {
        CountryAdapter().apply {
            countryLeagueListener = CountryLeagueListener(
                { league ->
                    navGameLeague(leagueIdList = listOf(league.id))
                },
                { league ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, league.id)
                },
                { league ->
                    viewModel.selectLeague(league)
                })
        }
    }

    private val outrightCountryAdapter by lazy {
        OutrightCountryAdapter().apply {
            outrightCountryLeagueListener = OutrightCountryLeagueListener(
                { season ->
                    season.id?.let { navGameOutright(it) }
                },
                { leagueId ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, leagueId)
                }
            )
        }
    }

    private val outrightLeagueOddAdapter by lazy {
        OutrightLeagueOddAdapter().apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            outrightOddListener = OutrightOddListener(
                { matchOdd, odd, playCateCode ->
                    matchOdd?.let {
                        addOutRightOddsDialog(matchOdd, odd, playCateCode)
                        //addOddsDialog(matchOdd.matchInfo, odd, playCateCode,"",null)
                    }
                },
                { oddsKey, matchOdd ->
                    val action =
                        GameV3FragmentDirections.actionGameV3FragmentToGameOutrightMoreFragment(
                            oddsKey,
                            matchOdd
                        )
                    findNavController().navigate(action)
                },
                { matchOdd, oddsKey ->

                    subscribeChannelHall(matchOdd)

                    this.data.find { it == matchOdd }?.oddsMap?.get(oddsKey)?.forEach { odd ->
                        odd?.isExpand?.let { isExpand ->
                            odd.isExpand = !isExpand
                        }
                    }
                    this.notifyItemChanged(this.data.indexOf(matchOdd))
                }
            )
        }
    }


    private val leagueAdapter by lazy {
        LeagueAdapter(
            args.matchType,
            getPlaySelectedCodeSelectionType(),
            getPlaySelectedCode()
        ).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener({
                subscribeChannelHall(it)
            }, {
                loading()
                if (args.matchType == MatchType.OTHER) {

                } else {
                    viewModel.refreshGame(
                        args.matchType,
                        listOf(it.league.id),
                        listOf()
                    )
                }
            })
            leagueOddListener = LeagueOddListener(
                { matchId, matchInfoList, _ ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it)
                            }
                        }
                        MatchType.AT_START -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
                            }
                        }
                        MatchType.OTHER -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
                            }
                        }
                        else -> {
                        }
                    }
                },
                { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    addOddsDialog(matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)
                },
                { matchOdd, quickPlayCate ->
                    matchOdd.matchInfo?.let {
                        //mOpenedQuickListMap.clear()
                        //viewModel.getQuickList2(it.id)
                        //mOpenedQuickListMap[matchOdd.matchInfo.id] = matchOdd
                        setQuickPlayCateSelected(matchOdd, quickPlayCate)
                    }
                },
                {
                    //mOpenedQuickListMap.forEach { t, u -> u.isExpand = false }
                    //viewModel.clearQuickPlayCateSelected()
                    clearQuickPlayCateSelected()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                },
                { matchId ->
                    navStatistics(matchId)
                },
                { matchId ->
                    loading()
                    viewModel.refreshGame(
                        args.matchType,
                        listOf(),
                        listOf(matchId)
                    )
                }
            )
        }
    }

    private val epsListAdapter by lazy {
        EpsListAdapter(EpsListAdapter.EpsOddListener(
            {
                subscribeChannelHall(it)
            },
            { odd, betMatchInfo, betPlayCateNameMap ->
                addOddsDialog(
                    betMatchInfo,
                    odd,
                    PlayCate.EPS.value,
                    getString(R.string.game_tab_price_boosts_odd),
                    betPlayCateNameMap
                )
            }) { matchInfo ->
            setEpsBottomSheet(matchInfo)
        }
        )
    }

    var isUpdatingLeague = false

    var mLeagueOddList = ArrayList<LeagueOdd>()
    var mOpenedQuickListMap = HashMap<String, MatchOdd>()
    var mQuickOddListMap = HashMap<String, MutableList<QuickPlayCate>>()

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    val gameToolbarMatchTypeText = { matchType: MatchType ->
        when (matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start_2)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            MatchType.EPS -> getString(R.string.home_title_eps)
            MatchType.OTHER -> viewModel.specialEntrance.value?.couponName
            else -> ""
        }
    }

    override fun loading() {
        super.loading()
        stopTimer()
    }

    override fun hideLoading() {
        super.hideLoading()
        if (timer == null) startTimer()
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            game_list.adapter = null
            isReload = true
            when (tab?.text.toString()) { //固定寫死
                getString(R.string.game_tab_league_odd) -> { //賽事
                    if (args.matchType == MatchType.OTHER) {
                        game_play_category.visibility = View.VISIBLE
                    }
                    childMatchType = args.matchType
                    viewModel.switchChildMatchType(childMatchType = args.matchType)
                }
                getString(R.string.game_tab_outright_odd) -> { //冠軍
                    if (args.matchType == MatchType.OTHER) {
                        game_play_category.visibility = View.GONE
                        childMatchType = MatchType.OTHER_OUTRIGHT
                        viewModel.switchChildMatchType(childMatchType = MatchType.OTHER_OUTRIGHT)
                    } else {
                        viewModel.switchChildMatchType(childMatchType = MatchType.OUTRIGHT)
                    }
                }
                getString(R.string.game_tab_price_boosts_odd) -> { //特優賠率
                    if (args.matchType == MatchType.OTHER) {
                        viewModel.switchChildMatchType(childMatchType = MatchType.OTHER_EPS)
                    } else {
                        viewModel.switchChildMatchType(childMatchType = MatchType.EPS)
                    }
                }
            }
            game_match_category_pager.isVisible =
                tab?.text.toString() == getString(R.string.game_tab_league_odd) && (args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && matchCategoryPagerAdapter.itemCount > 0
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }

    init {
        afterAnimateListener = AfterAnimateListener {
            try {
                initObserve()
                initSocketObserver()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.resetOtherSeelectedGameType()
        mView = inflater.inflate(R.layout.fragment_game_v3, container, false)
        return mView
//            .apply {
//            setupSportTypeList(this)
//            setupToolbar(this)
//            setupOddTab(this)
//            setupSportBackground(this)
//            setupMatchCategoryPager(this)
//            setupPlayCategory(this)
//            setupGameRow(this)
//            setupGameListView(this)
//        }
    }

    private fun setupSportTypeList(view: View) {
        view.sport_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = gameTypeAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupToolbar(view: View) {
        when (args.matchType) {
            MatchType.OTHER -> view.game_toolbar_match_type.text =
                gameToolbarMatchTypeText(args.matchType)
            else -> {
            }
        }

        view.game_toolbar_champion.apply {
            visibility = when (args.matchType) {
                MatchType.IN_PLAY, MatchType.AT_START -> {
                    View.VISIBLE
                }
                else -> {
                    View.GONE
                }
            }

            setOnClickListener {
                GameType.getGameType(viewModel.getSportSelectedCode(args.matchType))?.let {
                    val action =
                        GameV3FragmentDirections.actionGameV3FragmentToLeagueFilterFragment(
                            it,
                            args.matchType
                        )
                    findNavController().navigate(action)
                }
            }
        }

        view.game_toolbar_calendar.apply {
            visibility = when (args.matchType) {
                MatchType.EARLY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                isSelected = !isSelected

                view.game_filter_type_list.visibility = when (isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }

        view.game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun refreshToolBarUI(view: View?) {
        if (view != null) {
            if (leagueAdapter.data.isEmpty()) {
                if (args.matchType == MatchType.AT_START)
                    game_toolbar_champion.isVisible = false
            } else {
                if (args.matchType == MatchType.AT_START)
                    game_toolbar_champion.isVisible = true
            }

            if (args.matchType == MatchType.OTHER) {
                game_toolbar_champion.isVisible = false
            }
        }
    }

    private fun setupOddTab(view: View) {
        view.game_tabs.apply {
            addOnTabSelectedListener(onTabSelectedListener)
        }

        view.game_tab_odd_v4.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY, MatchType.OTHER -> View.VISIBLE
            else -> View.GONE
        }

        val epsItem = (view.game_tab_odd_v4.game_tabs.getChildAt(0) as ViewGroup).getChildAt(2)
        if (view.game_tab_odd_v4.visibility == View.VISIBLE && args.matchType == MatchType.PARLAY) {
            epsItem.visibility = View.GONE
        } else if (args.matchType == MatchType.OTHER || args.matchType == MatchType.OTHER_OUTRIGHT) {
            epsItem.visibility = View.GONE
        } else {
            epsItem.visibility = View.GONE//0401 特優賠率入口隱藏
        }
    }

    private fun setOtherOddTab(isGameListNull: Boolean) {
        val gameItem = (view?.game_tab_odd_v4?.game_tabs?.getChildAt(0) as ViewGroup).getChildAt(0)

        if (isGameListNull) {
            game_tabs.selectTab(game_tabs.getTabAt(1))
        }
        gameItem.isVisible = !isGameListNull
    }

    private fun setupSportBackground(view: View) {
        view.game_bg_layer2.visibility = when (args.matchType) {
            MatchType.IN_PLAY, MatchType.AT_START, MatchType.OUTRIGHT, MatchType.EPS -> View.VISIBLE
            else -> View.GONE
        }

        view.game_bg_layer3.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY, MatchType.OTHER -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setupMatchCategoryPager(view: View) {
        view.match_category_pager.adapter = matchCategoryPagerAdapter
        view.match_category_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
        OverScrollDecoratorHelper.setUpOverScroll(
            view.match_category_pager.getChildAt(0) as RecyclerView,
            OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
        )
        view.match_category_indicator.setupWithViewPager2(view.match_category_pager)
        view.game_match_category_pager.visibility =
            if ((args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && matchCategoryPagerAdapter.itemCount > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupPlayCategory(view: View) {
        view.game_play_category.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = playCategoryAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )

            visibility =
                if (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START || (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun setupGameRow(view: View) {
        view.game_filter_type_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_date
                )
            )
        }

        view.game_filter_type_list.visibility =
            if (args.matchType == MatchType.EARLY && view.game_toolbar_calendar.isSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    @SuppressLint("LogNotTimber")
    private fun setupGameListView(view: View) {
        view.game_list.apply {
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = leagueAdapter
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    if (leagueAdapter.data.isNotEmpty()) {
                        it.forEach { p ->
                            Log.d(
                                "[subscribe]",
                                "訂閱 ${leagueAdapter.data[p.first].league.name} -> " +
                                        "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                        "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                            )
                            subscribeChannelHall(
                                leagueAdapter.data[p.first].gameType?.key,
                                leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                            )
                        }
                    }
                }
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserve() {
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it != null && it.isNotBlank()) {
                context?.let { context ->
                    val dialog = CustomAlertDialog(context)
                    dialog.setTitle(resources.getString(R.string.prompt))
                    dialog.setMessage(it)
                    dialog.setTextColor(R.color.colorRed)
                    dialog.setNegativeButtonText(null)
                    dialog.setPositiveClickListener {
                        viewModel.resetErrorDialogMsg()
                        dialog.dismiss()
                        back()
                    }
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.isCancelable = false
                    dialog.show(childFragmentManager, null)
                }
            }
        }

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner) {
            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.inPlay?.items ?: listOf(),
                        it?.sportMenuData?.menu?.inPlay?.num
                    )
                }

                MatchType.TODAY -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.today?.items ?: listOf(),
                        it?.sportMenuData?.menu?.today?.num
                    )
                }

                MatchType.EARLY -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.early?.items ?: listOf(),
                        it?.sportMenuData?.menu?.early?.num
                    )
                }

                MatchType.PARLAY -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.parlay?.items ?: listOf(),
                        it?.sportMenuData?.menu?.parlay?.num
                    )
                }

                MatchType.OUTRIGHT -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.outright?.items ?: listOf(),
                        it?.sportMenuData?.menu?.outright?.num
                    )
                }

                MatchType.AT_START -> {
                    updateSportType(
                        it?.sportMenuData?.atStart?.items ?: listOf(),
                        it?.sportMenuData?.atStart?.num
                    )
                }

                MatchType.EPS -> {
                    updateSportType(
                        it?.sportMenuData?.menu?.eps?.items ?: listOf(),
                        it?.sportMenuData?.menu?.eps?.num
                    )
                }

                MatchType.OTHER -> {
                    val tempItem: MutableList<Item> = mutableListOf()
                    viewModel.specialMenuData?.items?.forEach { it ->
                        val item = Item(
                            code = it.code ?: "",
                            name = it.name ?: "",
                            num = it.num ?: 0,
                            play = null,
                            sortNum = it.sortNum ?: 0,
                        )
                        item.hasPlay = (it.play != null)
                        item.isSelected = it.isSelected
                        tempItem.add(item)
                    }
                    updateSportType(tempItem)
                }

                else -> {
                }
            }
        }

        viewModel.matchCategoryQueryResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.rows?.let { resultList ->
                val isCateShow =
                    ((args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && resultList.isNotEmpty())
                game_match_category_pager.isVisible = isCateShow
                // TODO view_space_first.isVisible = !isCateShow
                matchCategoryPagerAdapter.data = resultList
            }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            (game_filter_type_list.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                it, game_filter_type_list.width / 2
            )
        }

        viewModel.curChildMatchType.observe(this.viewLifecycleOwner) {
            //TODO childMatchType更新選中
            //預設第一項
            when (it) {
                null -> {
                    //init tab select
                    game_tabs.clearOnTabSelectedListeners()
                    game_tabs.addOnTabSelectedListener(onTabSelectedListener)
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                }
                MatchType.OTHER_OUTRIGHT -> {
                    game_tabs.selectTab(game_tabs.getTabAt(1))
                }
                MatchType.OUTRIGHT -> {
                    game_tabs.selectTab(game_tabs.getTabAt(1))
                }
                MatchType.OTHER_EPS -> {
                    game_tabs.selectTab(game_tabs.getTabAt(2))
                }
                MatchType.EPS -> {
                    game_tabs.selectTab(game_tabs.getTabAt(2))
                }
                else -> {
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                }
            }
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            when (game_list.adapter) {
                is LeagueAdapter -> {
                    leagueAdapter.discount = userInfo?.discount ?: 1.0F
                }

                is EpsListAdapter -> {
                    epsListAdapter.discount = userInfo?.discount ?: 1.0F
                }
            }
        }

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    mLeagueOddList.clear()
                    mLeagueOddList.addAll(
                        oddsListResult.oddsListData?.leagueOddsFilter
                            ?: oddsListResult.oddsListData?.leagueOdds ?: listOf()
                    )

                    val gameType = GameType.getGameType(oddsListResult.oddsListData?.sport?.code)

                    if (mLeagueOddList.isNotEmpty()) {
                        leagueAdapter.playSelectedCodeSelectionType =
                            getPlaySelectedCodeSelectionType()
                        leagueAdapter.data = mLeagueOddList.onEach { leagueOdd ->
                            // 將儲存的賠率表指定的賽事列表裡面
                            val leagueOddFromMap = leagueOddMap[leagueOdd.league.id]
                            leagueOddFromMap?.let {
                                leagueOdd.matchOdds.forEach {
                                    it.oddsMap =
                                        leagueOddFromMap.matchOdds.find { matchOdd -> it.matchInfo?.id == matchOdd.matchInfo?.id }?.oddsMap
                                }
                            }
                            leagueOdd.gameType = gameType
                        }.toMutableList()
                        leagueAdapter.playSelectedCodeSelectionType =
                            getPlaySelectedCodeSelectionType()
                        leagueAdapter.playSelectedCode = getPlaySelectedCode()
                    } else {
                        leagueAdapter.data = mLeagueOddList
                        // Todo: MatchType.OTHER 要顯示無資料與隱藏篩選清單
//                        leagueAdapter.data = mutableListOf()

                    }
                    if (game_list.adapter !is LeagueAdapter) game_list.adapter = leagueAdapter

//                    if (leagueOdds.isNotEmpty()) {
//                        game_list.apply {
//                            adapter = leagueAdapter.apply {
//                                updateType = null
//                                data = leagueOdds.onEach { leagueOdd ->
//                                    leagueOdd.gameType = gameType
//                                }.toMutableList()
//                            }
//                        }
//                    }

                    //如果data資料為空時，又有其他球種的情況下，自動選取第一個
                    if (mLeagueOddList.isNullOrEmpty() && gameTypeAdapter.dataSport.size > 1) {
                        if (args.matchType == MatchType.OTHER) {
                            // 待觀察，再決定是否要補
                        } else {
                            viewModel.getSportMenu(
                                args.matchType,
                                switchFirstTag = true,
                                onlyRefreshSportMenu = true
                            )
                        }
                    }
                    //game_list.itemAnimator = null

                    mLeagueOddList.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                        subscribeChannelHall(leagueOdd)
                    }

                    oddsListResult.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEachIndexed { index, matchOdd ->
                            mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                matchOdd.quickPlayCateList ?: mutableListOf()
                            //leagueAdapter.updateLeague(index, leagueOdd)
                        }
                    }
                    leagueAdapter.limitRefresh()
                    // TODO 這裡要確認是否有其他地方重複呼叫
                    Log.d("Hewie", "observe => OddsListGameHallResult")
                    isReload = true

//                    if (isReload) {
//                        leagueAdapter.notifyDataSetChanged()
//                        isReload = false
//                    }
                    //leagueAdapter.notifyDataSetChanged()

//                    when (args.matchType) {
//                        MatchType.OTHER -> {
//                            setOtherOddTab(mLeagueOddList.isNullOrEmpty())
//                        }
//                        else->{}
//                    }
                }
                refreshToolBarUI(this.view)
            }
            hideLoading()
        }

        // 接收快選列表資料
//        viewModel.quickOddsListGameHallResult.observe(this.viewLifecycleOwner) {
//            it.getContentIfNotHandled()?.let { oddsListResult ->
//                if (oddsListResult.success) {
//                    oddsListResult.oddsListData?.leagueOdds?.forEach { leagueOdd ->
//                        leagueOdd.matchOdds.forEach { matchOdd ->
//                            matchOdd.isExpand = mOpenedQuickListMap[matchOdd.matchInfo?.id]?.isExpand ?: false
//                            matchOdd.quickPlayCateList = mOpenedQuickListMap[matchOdd.matchInfo?.id]?.quickPlayCateList
//
//                            mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] = matchOdd.quickPlayCateList ?: mutableListOf()
//                        }
//                    }
//                }
//            }
//        }

        viewModel.oddsListGameHallIncrementResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueListIncrementResult ->
                val leagueListIncrement = leagueListIncrementResult.oddsListResult

                leagueListIncrementResult.leagueIdList?.forEach { leagueId ->
                    //判斷此聯賽是否已經於畫面顯示
                    leagueAdapter.data.find { adapterLeagueOdd -> adapterLeagueOdd.league.id == leagueId }
                        ?.let { onScreenLeague ->

                            val targetIndex = leagueAdapter.data.indexOf(onScreenLeague)
                            val changedLeague =
                                leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == onScreenLeague.league.id }

                            //若api response沒有該聯賽資料則不顯示該聯賽
                            changedLeague?.let { changedLeagueOdd ->
                                unSubscribeLeagueChannelHall(leagueAdapter.data[targetIndex])
                                val targetLeagueOdd = leagueAdapter.data[targetIndex]
                                leagueAdapter.data[targetIndex] = changedLeagueOdd.apply {
                                    this.unfold = targetLeagueOdd.unfold
                                    this.gameType = targetLeagueOdd.gameType
                                    this.searchMatchOdds = targetLeagueOdd.searchMatchOdds
                                }
                                subscribeChannelHall(leagueAdapter.data[targetIndex])
                            } ?: run {
                                leagueAdapter.data.removeAt(targetIndex)
                                leagueAdapter.notifyItemRemoved(targetIndex)
                                Log.d(
                                    "Hewie",
                                    "移除聯賽：${leagueAdapter.data[targetIndex].league.name}"
                                )
                            }
                        } ?: run {
                        //不在畫面上的League
                        val changedLeague =
                            leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == leagueId }
                        changedLeague?.let { changedLeagueOdd ->
                            val gameType =
                                GameType.getGameType(leagueListIncrement.oddsListData.sport.code)
                            val insertLeagueOdd = changedLeagueOdd.apply {
                                this.gameType = gameType
                            }
                            leagueAdapter.data.add(insertLeagueOdd)
                            leagueAdapter.notifyItemInserted(leagueAdapter.data.size - 1)
                            subscribeChannelHall(insertLeagueOdd)
                            Log.d("Hewie", "增加聯賽：${insertLeagueOdd.league.name}")
                        }
                    }
                }
            }
            hideLoading()
        }

        viewModel.leagueListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueListResult ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 0 && !isReload)
                    return@observe

                if (leagueListResult.success) {
                    val rows = leagueListResult.rows ?: listOf()
                    when (args.matchType) {
                        MatchType.OTHER -> {
                            game_list.apply {
                                adapter = countryAdapter.apply {
                                    data = rows
                                }
                                isReload = false
                            }
                        }
                        else -> {
                            game_list.apply {
                                adapter = countryAdapter.apply {
                                    data = rows
//                                    if (args.matchType == MatchType.PARLAY)
//                                        view?.game_toolbar_match_type?.text = data.firstOrNull()?.name
                                }
                                isReload = false
                            }
                        }
                    }
                }
            }
            hideLoading()
        }

        viewModel.outrightLeagueListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { outrightSeasonListResult ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 1 && !isReload)
                    return@observe

                if (outrightSeasonListResult.success) {
                    val rows = outrightSeasonListResult.rows ?: listOf()
                    game_list.apply {
                        adapter = outrightCountryAdapter.apply {
                            data = rows
                        }
                        isReload = false
                    }
                }
            }
            hideLoading()
        }

        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner) {
            hideLoading()

            it.getContentIfNotHandled()?.let { outrightOddsListResult ->
                if (outrightOddsListResult.success) {
                    GameConfigManager.getTitleBarBackground(outrightOddsListResult.outrightOddsListData?.sport?.code)
                        ?.let { gameImg ->
                            game_toolbar_bg.setBackgroundResource(gameImg)
                        }

                    var outrightLeagueOddDataList:MutableList<org.cxct.sportlottery.network.outright.odds.MatchOdd?> = mutableListOf()
                        outrightOddsListResult.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds
                            ?: listOf()
                    outrightOddsListResult.outrightOddsListData?.leagueOdds?.forEach {  leagueOdd ->
                        leagueOdd.matchOdds?.forEach { matchOdds ->
                            outrightLeagueOddDataList.add(matchOdds)
                        }

                    }

                    outrightLeagueOddDataList.forEachIndexed { index, matchOdd ->
                        val firstKey = matchOdd?.oddsMap?.keys?.firstOrNull()

                        matchOdd?.oddsMap?.forEach { oddsMap ->
                            oddsMap.value?.filterNotNull()?.forEach { odd ->
                                odd.isExpand = true
                            }
                        }
                    }
                    if(outrightLeagueOddDataList.isEmpty()){

                    }
                    outrightLeagueOddAdapter.data = outrightLeagueOddDataList
                    outrightLeagueOddDataList.forEach { matchOdd ->
                        subscribeChannelHall(matchOdd)
                    }
                    game_list.apply {
                        adapter = outrightLeagueOddAdapter
                        isReload = false
                    }
                }
            }
        }


        viewModel.epsListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { epsListResult ->

                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 2 && !isReload)
                    return@observe

                if (epsListResult.success) {
                    val oddsEpsListDatas = epsListResult.rows
                    val epsLeagueOddsItemList = mutableListOf<EpsLeagueOddsItem>()

                    oddsEpsListDatas.forEachIndexed { indexDate, oddsEpsListData ->
                        val newLeagueOddsItem =
                            EpsLeagueOddsItem(
                                date = oddsEpsListData.date,
                                leagueOdds = null,
                            )
                        epsLeagueOddsItemList.add(newLeagueOddsItem)

                        oddsEpsListData.leagueOdd.forEachIndexed { indexLeague, leagueOdds ->
                            epsLeagueOddsItemList.add(
                                EpsLeagueOddsItem(
                                    date = 0,
                                    leagueOdds = leagueOdds
                                ).apply {
                                    isClose = !(indexDate == 0 && indexLeague == 0)
                                }
                            )
                        }
                    }

                    game_list.apply {
                        adapter = epsListAdapter.apply {
                            dataList = epsLeagueOddsItemList
                        }
                        isReload = false
                    }

                    epsLeagueOddsItemList.forEach { epsLeagueOddsItem ->
                        subscribeChannelHall(epsLeagueOddsItem)
                    }
                }
            }
            hideLoading()
        }

        viewModel.countryListSearchResult.observe(this.viewLifecycleOwner) {
            countryAdapter.data = it
//            if (args.matchType == MatchType.PARLAY)
//                view?.game_toolbar_match_type?.text = it.firstOrNull()?.name
            hideLoading()
        }

        viewModel.outrightCountryListSearchResult.observe(this.viewLifecycleOwner) {
            outrightCountryAdapter.data = it
            hideLoading()
        }


        //KK要求，當球類沒有資料時，自動選取第一個有賽事的球種
        viewModel.isNoHistory.observe(this.viewLifecycleOwner) {

            //判斷當前MatchType是否有玩法數量
            val hasGame = when (args.matchType) {
                MatchType.IN_PLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.inPlay?.num ?: 0 > 0
                MatchType.TODAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.today?.num ?: 0 > 0
                MatchType.AT_START -> viewModel.sportMenuResult.value?.sportMenuData?.atStart?.num ?: 0 > 0
                MatchType.EARLY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.early?.num ?: 0 > 0
                MatchType.PARLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.parlay?.num ?: 0 > 0
                MatchType.OUTRIGHT -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.outright?.num ?: 0 > 0
                MatchType.EPS -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.eps?.num ?: 0 > 0
                MatchType.OTHER -> viewModel.specialMenuData?.items?.size ?: 0 > 0
                else -> false
            }
            when {
                //當前MatchType有玩法數量，只是目前的球種沒有
                it && hasGame -> {
                    unSubscribeChannelHallAll()
                    if (args.matchType == MatchType.OTHER) {
//                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = true)
                    } else {
                        viewModel.switchMatchType(args.matchType)
                    }
                }
            }

            hideLoading()
        }

        //當前玩法無賽事
        viewModel.isNoEvents.observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it
            game_toolbar_sport_type.isVisible = !it
            //game_no_record_bg.isVisible = it
            game_play_category.isVisible =
                (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START || (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)) && !it
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let {

                val leagueOdds = leagueAdapter.data
                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.oddsMap?.values?.forEach { oddList ->
                            oddList?.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds.forEach { map ->
                                map.value?.forEach { odd ->
                                    odd?.isSelected = it.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                                }
                            }
                        }
                    }
                }

//                leagueAdapter.notifyDataSetChanged()
                updateAllGameList()

                val epsOdds = epsListAdapter.dataList

                epsOdds.forEach { epsLeagueOddsItem ->
                    epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.oddsEps?.eps?.forEach { odd ->
                            odd?.isSelected = it.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                        }
                    }
                }
                epsListAdapter.notifyDataSetChanged()

                val odds = mutableListOf<Odd>()

                outrightLeagueOddAdapter.data.forEach { matchOdd ->
                    matchOdd?.oddsMap?.values?.forEach { oddList ->
                        odds.addAll(oddList?.filterNotNull() ?: mutableListOf())
                    }
                }

                odds.forEach { odd ->
                    odd.isSelected = it.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd.id
                    }
                }

                outrightLeagueOddAdapter.notifyDataSetChanged()
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
                epsListAdapter.oddsType = oddsType
            }
        }

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner) {
            countryAdapter.apply {
                data.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = it.any { it.id == league.id }
                    }
                }

                notifyDataSetChanged()
            }
        }

        viewModel.playList.observe(this.viewLifecycleOwner) {
            playCategoryAdapter.data = it
        }

        viewModel.playCate.observe(this.viewLifecycleOwner) {
            playCategoryAdapter.apply {
                data.find { it.isSelected }?.playCateList?.forEach { playCate ->
                    playCate.isSelected = (playCate.code == it)
                }
                notifyDataSetChanged()
            }
        }

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {
            updateLeaguePin(it)
            updateLeaguePinOutright(it)
        }

        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueList ->
                navGameLeague(leagueIdList = leagueList.map { league ->
                    league.id
                })
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            leagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            //leagueAdapter.notifyDataSetChanged()
            updateAllGameList()
        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            game_toolbar_champion.isSelected = mLeagueIsFiltered
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }

        viewModel.checkInListFromSocket.observe(this.viewLifecycleOwner) {
            if (it) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isUpdatingLeague) {
                        isUpdatingLeague = true
                        //收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单
                        withContext(Dispatchers.Main) {
                            if (args.matchType == MatchType.OTHER) {
                                // 後面處理
                            } else {
                                viewModel.getAllPlayCategory(args.matchType)
                            }
                            viewModel.getSportMenu(args.matchType, onlyRefreshSportMenu = true)
                        }
                        //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
                        val nowGameType =
                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key

                        val hasLeagueIdList =
                            leagueAdapter.data.any { leagueOdd -> leagueOdd.league.id == mLeagueChangeEvent?.leagueIdList?.firstOrNull() }

//                        when (nowGameType) {
//                            leagueChangeEvent.gameType -> {
//                                unSubscribeChannelHall(nowGameType ?: GameType.FT.key,getPlayCateMenuCode(),leagueChangeEvent.matchIdList?.firstOrNull())
//                                subscribeChannelHall(nowGameType ?: GameType.FT.key,getPlayCateMenuCode(),leagueChangeEvent.matchIdList?.firstOrNull())
//                            }
//                        }

                        if (nowGameType == mLeagueChangeEvent?.gameType) {
                            when {
                                !hasLeagueIdList ||
                                        args.matchType == MatchType.AT_START -> {
                                    //全刷
                                    unSubscribeChannelHallAll()
                                    withContext(Dispatchers.Main) {
                                        if (args.matchType == MatchType.OTHER) {
                                            viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                                        } else {
                                            viewModel.getGameHallList(args.matchType, false)
                                        }
                                    }
                                }
                                else -> {
                                    unSubscribeChannelHall(
                                        nowGameType ?: GameType.FT.key,
                                        getPlaySelectedCode(),
                                        mLeagueChangeEvent?.matchIdList?.firstOrNull()
                                    )
                                    subscribeChannelHall(nowGameType ?: GameType.FT.key, mLeagueChangeEvent?.matchIdList?.firstOrNull())
                                    if (args.matchType == MatchType.OTHER) {
                                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                                    }
                                }
                            }
                        } else if (args.matchType == MatchType.OTHER) {
                            viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                        }
                        isUpdatingLeague = false
                    }
                }
            }
        }
    }

    private fun updateLeaguePin(leagueListPin: List<String>) {
        val leaguePinList = mutableListOf<League>()

        countryAdapter.data.forEach { row ->
            val pinLeague = row.list.filter { league ->
                leagueListPin.contains(league.id)
            }

            row.list.forEach { league ->
                league.isPin = leagueListPin.contains(league.id)
            }

            leaguePinList.addAll(pinLeague)
        }

        countryAdapter.datePin = leaguePinList.sortedBy {
            leagueListPin.indexOf(it.id)
        }
    }

    private fun updateLeaguePinOutright(leagueListPin: List<String>) {
        val leaguePinList = mutableListOf<Season>()

        outrightCountryAdapter.data.forEach { row ->
            val pinLeague = row.list.filter { league ->
                leagueListPin.contains(league.id)
            }

            row.list.forEach { league ->
                league.isPin = leagueListPin.contains(league.id)
            }

            leaguePinList.addAll(pinLeague)
        }

        outrightCountryAdapter.datePin = leaguePinList.sortedBy {
            leagueListPin.indexOf(it.id)
        }
    }

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private var mLeagueChangeEvent: LeagueChangeEvent? = null
    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    loading()
                    if (args.matchType == MatchType.OTHER) {
                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = true)
                    } else {
                        viewModel.getGameHallList(
                            matchType = args.matchType,
                            isReloadDate = true,
                            isReloadPlayCate = true,
                            isLastSportType = true
                        )
                    }
                    viewModel.getMatchCategoryQuery(args.matchType)
                    subscribeSportChannelHall(args.matchType.name)
                } else {
                    stopTimer()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {

                        leagueAdapter.data.toList().forEachIndexed { index, leagueOdd ->
                            if (matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value) {
                                leagueOdd.matchOdds.find { m ->
                                    m.matchInfo?.id == matchStatusChangeEvent.matchStatusCO.matchId
                                }?.let { mo ->
                                    leagueOdd.matchOdds.remove(mo)
                                    if (leagueOdd.matchOdds.size > 0) {
                                        leagueAdapter.notifyItemChanged(index)
                                    } else {
                                        leagueAdapter.data.remove(leagueOdd)
                                        leagueAdapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (SocketUpdateUtil.updateMatchStatus(
                                    gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code,
                                    leagueOdd.matchOdds.toMutableList(),
                                    matchStatusChangeEvent,
                                    context
                                ) &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                if (leagueOdd.matchOdds.isNullOrEmpty()) {
                                    leagueAdapter.data.remove(leagueOdd)
                                    leagueAdapter.notifyItemRemoved(index)
                                }
                                //leagueAdapter.updateBySocket(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchClock(
                                        matchOdd,
                                        matchClockEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //leagueAdapter.updateBySocket(index)
                                //leagueAdapter.updateLeague(index, leagueOdd)
                            }
                        }
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.filterMenuPlayCate()
                oddsChangeEvent.sortOddsMap()
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEach { leagueOdd ->
                            leagueOdd.matchOdds.forEach { matchOdd ->
                                matchOdd.quickPlayCateList =
                                    mQuickOddListMap[matchOdd.matchInfo?.id]
                            }
                        }

                        val leagueOdds = leagueAdapter.data
                        leagueOdds.sortOddsMap()
                        leagueOdds.updateOddsSort() //篩選玩法

                        //翻譯更新
                        leagueOdds.forEach { LeagueOdd ->
                            LeagueOdd.matchOdds.forEach { MatchOdd ->
                                if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                    //馬克說betPlayCateNameMap還是由socket更新
                                    oddsChangeEvent.betPlayCateNameMap?.let {
                                        MatchOdd.betPlayCateNameMap?.putAll(oddsChangeEvent.betPlayCateNameMap!!)
                                    }
                                }
                            }
                        }


                        leagueAdapter.playSelectedCodeSelectionType =
                            getPlaySelectedCodeSelectionType()
                        leagueAdapter.playSelectedCode = getPlaySelectedCode()

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchOdds(
                                        context, matchOdd, oddsChangeEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                //leagueAdapter.updateBySocket(index)
                                leagueOddMap[leagueOdd.league.id] = leagueOdd

                                // Safety update list
                                updateGameList(index, leagueOdd)
                                if (isReload) {
                                    leagueAdapter.limitRefresh()
                                    isReload = false
                                }
                            } else {
                                updateGameList(index, leagueOdd)
                            }
                        }
                    }

                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.leagueOdds?.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchOdds(
                                        context, matchOdd, oddsChangeEvent
                                    )
                                } == true &&
                                !leagueOdd.isClose) {

                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //leagueAdapter.updateBySocket(index)
                                //leagueAdapter.updateLeague(index, leagueOdd)
                            }
                        }
                    }
                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.leagueOdds?.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } == true && !leagueOdd.isClose) {
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                //leagueAdapter.updateBySocket(index)
                                //leagueAdapter.updateLeague(index, leagueOdd)
                            }
                        }
                    }

                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, epsLeagueOddsItem ->
                            if (epsLeagueOddsItem.leagueOdds?.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } == true &&
                                !epsLeagueOddsItem.isClose) {
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEach { leagueOdd ->
                            subscribeChannelHall(leagueOdd)
                        }
                    }

                    is EpsListAdapter -> {
                        epsListAdapter.dataList.forEach { epsLeagueOddsItem ->
                            subscribeChannelHall(epsLeagueOddsItem)
                        }
                    }
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                mLeagueChangeEvent = leagueChangeEvent
                viewModel.checkGameInList(
                    matchType = args.matchType,
                    leagueIdList = leagueChangeEvent.leagueIdList,
                )
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        leagueAdapter.data[index] = leagueOdd
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            leagueAdapter.updateLeague(index, leagueOdd)
        }
    }

    private fun updateAllGameList() {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            leagueAdapter.data.forEachIndexed { index, leagueOdd -> leagueAdapter.updateLeague(index, leagueOdd) }
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value?.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    /**
     * 邏輯和 dev2 不同，這裡要再過濾一次玩法資料
     * */

    private fun MutableList<LeagueOdd>.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }
        val playCateMenuCode = playSelected?.playCateList?.find { it.isSelected }?.code
        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                this.forEach { LeagueOdd ->
                    LeagueOdd.matchOdds.forEach { MatchOdd ->
                        MatchOdd.oddsMap?.entries?.retainAll { oddMap -> oddMap.key == playCateMenuCode }
                    }
                }
            }
        }
    }

    /**
     * 篩選玩法
     * 更新翻譯、排序
     * */

    private fun MutableList<LeagueOdd>.updateOddsSort() {
        val nowGameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key
        val playCateMenuCode =
            if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else getPlaySelectedCode()
        val oddsSortFilter =
            if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) getPlayCateMenuCode() else PlayCateMenuFilterUtils.filterOddsSort(
                nowGameType,
                playCateMenuCode
            )
        val playCateNameMapFilter =
            if (getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code) PlayCateMenuFilterUtils.filterSelectablePlayCateNameMap(
                nowGameType,
                getPlaySelectedCode(),
                playCateMenuCode
            ) else PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        this.forEach { LeagueOdd ->
            LeagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsSort = oddsSortFilter
                MatchOdd.playCateNameMap = playCateNameMapFilter
            }
        }
    }

    /**
     * 只有有下拉篩選玩法的才需要過濾odds
     */
    private fun OddsChangeEvent.filterMenuPlayCate() {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                val playCateMenuCode = playSelected.playCateList?.find { it.isSelected }?.code
                this.odds?.entries?.retainAll { oddMap -> oddMap.key == playCateMenuCode }
            }
        }
    }

    /**
     * 賠率排序
     */
    private fun OddsChangeEvent.sortOddsMap() {
        this.odds?.forEach { (key, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }

    private fun MutableList<LeagueOdd>.sortOddsMap() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (key, value) ->
                    if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                        value?.sortBy {
                            it?.marketSort
                        }
                    }
                }
            }
        }
    }

    private fun setEpsBottomSheet(matchInfo: MatchInfo) {
        try {
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_eps, contentView, false)
            moreEpsInfoBottomSheet = BottomSheetDialog(this.requireContext())
            moreEpsInfoBottomSheet.apply {
                setContentView(bottomSheetView)
                btn_close.setOnClickListener {
                    this.dismiss()
                }
                tv_league_title.text = matchInfo.leagueName
                rv_more_eps_info_item.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                rv_more_eps_info_item.adapter = EpsMoreInfoAdapter().apply {
                    dataList = listOf(matchInfo)
                }
            }

            moreEpsInfoBottomSheet.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSportType(gameTypeList: List<Item>, num: Int? = -1) {
        gameTypeAdapter.dataSport = gameTypeList

        if (args.matchType != MatchType.OTHER) {
            gameTypeList.find { it.isSelected }.let { item ->
                game_toolbar_sport_type.text =
                    context?.let { getGameTypeString(it, item?.code) } ?: resources.getString(
                        GameType.FT.string
                    )
                        .toUpperCase(Locale.getDefault())
                updateSportBackground(item)
            }
        } else {
            gameTypeList.find { it.isSelected }.let { item ->
                item?.let {
                    setOtherOddTab(!it.hasPlay)
                    updateSportBackground(it)
                }
            }
        }

        if (gameTypeList.isEmpty()) {
            sport_type_list.visibility = View.GONE
            game_toolbar_sport_type.visibility = View.GONE
            game_toolbar_champion.visibility = View.GONE
            game_toolbar_calendar.visibility = View.GONE
            game_tab_odd_v4.visibility = View.GONE
            game_match_category_pager.visibility = View.GONE
            game_play_category.visibility = View.GONE
            game_filter_type_list.visibility = View.GONE
            return
        } else {
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
            game_toolbar_sport_type.visibility = View.VISIBLE
            game_toolbar_calendar.apply {
                visibility = when (args.matchType) {
                    MatchType.EARLY -> View.VISIBLE
                    else -> View.GONE
                }
                isSelected = false
            }
            game_tab_odd_v4.visibility = when (args.matchType) {
                MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY, MatchType.OTHER -> View.VISIBLE
                else -> View.GONE
            }
            game_match_category_pager.visibility =
                if ((args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && matchCategoryPagerAdapter.itemCount > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            game_play_category.visibility = if (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START ||
                (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun updateSportBackground(sport: Item?) {
        when {
            game_bg_layer2.isVisible -> game_bg_layer2
            game_bg_layer3.isVisible -> game_bg_layer3
            else -> null
        }?.let {
            Glide.with(requireContext()).load(
                when (sport?.code) {
                    GameType.FT.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.soccer108
                            game_bg_layer3.isVisible -> R.drawable.soccer140
                            else -> null
                        }
                    }
                    GameType.BK.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.basketball108
                            game_bg_layer3.isVisible -> R.drawable.basketball140
                            else -> null
                        }
                    }
                    GameType.TN.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.tennis108
                            game_bg_layer3.isVisible -> R.drawable.tennis140
                            else -> null
                        }
                    }
                    GameType.VB.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.volleyball108
                            game_bg_layer3.isVisible -> R.drawable.volleyball140
                            else -> null
                        }
                    }
                    GameType.BM.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.badminton_100
                            game_bg_layer3.isVisible -> R.drawable.badminton_132
                            else -> null
                        }
                    }
                    GameType.TT.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.pingpong_100
                            game_bg_layer3.isVisible -> R.drawable.pingpong_140
                            else -> null
                        }
                    }
                    GameType.BX.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.boxing_100
                            game_bg_layer3.isVisible -> R.drawable.boxing_132
                            else -> null
                        }
                    }
                    GameType.CB.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.snooker_100
                            game_bg_layer3.isVisible -> R.drawable.snooker_140
                            else -> null
                        }
                    }
                    GameType.CK.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.cricket_100
                            game_bg_layer3.isVisible -> R.drawable.cricket_132
                            else -> null
                        }
                    }
                    GameType.BB.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.baseball_100
                            game_bg_layer3.isVisible -> R.drawable.baseball_132
                            else -> null
                        }
                    }
                    GameType.RB.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.rugby_100
                            game_bg_layer3.isVisible -> R.drawable.rugby_140
                            else -> null
                        }
                    }
                    GameType.AFT.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.amfootball_100
                            game_bg_layer3.isVisible -> R.drawable.amfootball_132
                            else -> null
                        }
                    }
                    GameType.MR.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.rancing_100
                            game_bg_layer3.isVisible -> R.drawable.rancing_140
                            else -> null
                        }
                    }
                    GameType.GF.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.golf_108
                            game_bg_layer3.isVisible -> R.drawable.golf_132
                            else -> null
                        }
                    }
                    GameType.IH.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.icehockey_100
                            game_bg_layer3.isVisible -> R.drawable.icehockey_140
                            else -> null
                        }
                    }
                    else -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.soccer108
                            game_bg_layer3.isVisible -> R.drawable.soccer140
                            else -> null
                        }
                    }
                }
            ).into(it)
        }
    }

    //更新isLocked狀態
    private fun upDateSelectPlay(play: Play) {
        val platData = playCategoryAdapter.data.find { it == play }
        if (platData?.selectionType == SelectionType.SELECTABLE.code) {
            platData.isLocked = when {
                platData.isLocked == null || platData.isSelected -> false
                else -> true
            }
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun navGameLeague(
        leagueIdList: List<String> = listOf(),
        matchIdList: List<String> = listOf(),
        matchCategoryName: String? = null
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        val matchType = when (dateAdapter.data.find {
            it.isSelected
        }?.date) {
            MatchType.IN_PLAY.postValue -> MatchType.IN_PLAY
            else -> null
        }

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToGameLeagueFragment(
                matchType ?: args.matchType,
                gameType,
                leagueIdList.toTypedArray(),
                matchIdList.toTypedArray(),
                matchCategoryName
            )

            findNavController().navigate(action)
        }
    }

    private fun navGameOutright(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                GameV3FragmentDirections.actionGameV3FragmentToGameOutrightFragment(
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                args.matchType,
                gameType,
                matchId,
            )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailFragment(
                args.matchType,
                gameType,
                matchId,
                matchInfoList.toTypedArray()
            )

            findNavController().navigate(action)
        }
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId)
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }
    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            val fastBetDataBean = FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName =  "",
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null,
            )
            (activity as GameActivity).showFastBetFragment(fastBetDataBean)
        }

    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = args.matchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName ?: "",
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    getPlayCateMenuCode()
                )
                (activity as GameActivity).showFastBetFragment(fastBetDataBean)

//                viewModel.updateMatchBetList(
//                    args.matchType,
//                    gameType,
//                    playCateCode,
//                    playCateName,
//                    matchInfo,
//                    odd,
//                    ChannelType.HALL,
//                    betPlayCateNameMap,
//                    getPlayCateMenuCode()
//                )
            }
        }
    }

    /**
     * 取得當前篩選玩法是否可下拉
     * */
    private fun getPlaySelectedCodeSelectionType(): Int? {
        return playCategoryAdapter.data.find { it.isSelected }?.selectionType
    }

    private fun getPlaySelectedCode(): String? {
        return playCategoryAdapter.data.find { it.isSelected }?.code
    }

    private fun getPlayCateMenuCode(): String? {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        return when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                playSelected.playCateList?.find { it.isSelected }?.code
            }
            SelectionType.UN_SELECTABLE.code -> {
                playSelected.code
            }
            else -> null
        }
    }


    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd?) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            subscribeChannelHall(
                it.key,
                matchOdd?.matchInfo?.id
            )
        }
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        subscribeChannelHall(
                            leagueOdd.gameType?.key,
                            matchOdd.matchInfo.id
                        )
                    }

//                    matchOdd.quickPlayCateList?.forEach {
//                        when (it.isSelected) {
//                            true -> {
//                                subscribeChannelHall(
//                                    leagueOdd.gameType?.key,
//                                    matchOdd.matchInfo?.id
//                                )
//                            }
//                            false -> {
//                                unSubscribeChannelHall(
//                                    leagueOdd.gameType?.key,
//                                    it.code,
//                                    matchOdd.matchInfo?.id
//                                )
//                            }
//                        }
//                    }
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlaySelectedCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

//                    matchOdd.quickPlayCateList?.forEach {
//                        unSubscribeChannelHall(
//                            leagueOdd.gameType?.key,
//                            it.code,
//                            matchOdd.matchInfo?.id
//                        )
//                    }
                }
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlaySelectedCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    it.code,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 切換playCateMenu時, 先將原本訂閱的類別解除訂閱
     */
    private fun unSubscribeChannelSwitchPlayCate() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    getPlaySelectedCode(),
                    matchOdd.matchInfo?.id
                )

                if (matchOdd.matchInfo?.eps == 1) {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        PlayCate.EPS.value,
                        matchOdd.matchInfo.id
                    )
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                getPlaySelectedCode(),
                matchOdd.matchInfo?.id
            )

            if (matchOdd.matchInfo?.eps == 1) {
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    PlayCate.EPS.value,
                    matchOdd.matchInfo.id
                )
            }
        }
    }

    private fun subscribeChannelHall(epsLeagueOddsItem: EpsLeagueOddsItem) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
            when (epsLeagueOddsItem.isClose) {
                true -> {
                    unSubscribeChannelHall(
                        gameType?.key,
                        PlayCate.EPS.value,
                        matchOddsItem.matchInfo?.id
                    )
                }
                false -> {
                    subscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
            }
        }
    }

    private var timer: Timer? = null

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (args.matchType == MatchType.OTHER) {
                    viewModel.getAllPlayCategoryBySpecialMatchType()
                } else {
                    viewModel.getAllPlayCategory(args.matchType)
                }
                viewModel.getSportMenu(args.matchType, onlyRefreshSportMenu = true)
                if (!isUpdatingLeague) {
                    viewModel.switchSportType(
                        args.matchType,
                        GameType.getGameType(viewModel.getSportSelectedCode(args.matchType))?.key
                            ?: GameType.FT.key
                    )
                }
            }
        }, 60 * 3 * 1000L, 60 * 3 * 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearSelectedLeague()
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    override fun onResume() {
        super.onResume()
        mView?.let {
            setupSportTypeList(it)
            setupToolbar(it)
            setupOddTab(it)
            setupSportBackground(it)
            setupMatchCategoryPager(it)
            setupPlayCategory(it)
            setupGameRow(it)
            setupGameListView(it)
        }
        viewModel.getSportMenuFilter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelectedLeague()
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    private fun reloadPage() {
        viewModel.getGameHallList(
            args.matchType,
            isReloadPlayCate = true,
            isReloadDate = true,
            isIncrement = false
        )
    }

    // region handle LeagueOdd data
    private fun clearQuickPlayCateSelected() {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.isExpand = false
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }
    }

    private fun setQuickPlayCateSelected(
        selectedMatchOdd: MatchOdd,
        selectedQuickPlayCate: QuickPlayCate
    ) {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected = true
                    }
                }
            }
        }
    }

    fun <T : RecyclerView> T.removeItemDecorations() {
        while (itemDecorationCount > 0) {
            removeItemDecorationAt(0)
        }
    }
    // endregion
}