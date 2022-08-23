package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.odds.OutrightShowMoreItem
import org.cxct.sportlottery.network.outright.odds.OutrightSubTitleItem
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.game.outright.OutrightLeagueOddAdapter
import org.cxct.sportlottery.ui.game.outright.OutrightOddListener
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.SportViewModel
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
@SuppressLint("NotifyDataSetChanged", "LogNotTimber")
@RequiresApi(Build.VERSION_CODES.M)
class SportListFragment : BaseBottomNavigationFragment<SportViewModel>(SportViewModel::class),
    Animation.AnimationListener {
    companion object {
        fun newInstance(
            matchType: MatchType? = MatchType.IN_PLAY,
            gameType: String? = GameType.FT.key,
            outrightLeagueId: String? = null,
        ): SportListFragment {
            val args = Bundle()
            args.putSerializable("matchType", matchType)
            args.putString("gameType", gameType)
            outrightLeagueId?.let {
                args.putString("outrightLeagueId", outrightLeagueId)
            }
            val fragment = SportListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType by lazy {
        (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
    }
    private val gameType by lazy { arguments?.getString("gameType", GameType.FT.key) }
    private val outrightLeagueId by lazy { arguments?.getString("outrightLeagueId", null) }
    private var mView: View? = null
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾
    private var mCalendarSelected = false //紀錄日期圖示選中狀態

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {

                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                    //日期圖示選取狀態下，切換球種要重置UI狀態
                    if (iv_calendar.isSelected) iv_calendar.performClick()
                    (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                        sport_type_list,
                        RecyclerView.State(),
                        dataSport.indexOfFirst { item -> TextUtils.equals(it.code, item.code) })
                }

                sportListAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()

                //切換球種後要重置位置
                loading()
                unSubscribeChannelHallAll()
                viewModel.switchGameType(it)
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(matchType, it)
                loading()
            }
        }
    }


    private val outrightLeagueOddAdapter by lazy {
        OutrightLeagueOddAdapter().apply {
            outrightOddListener = OutrightOddListener(
                clickListenerBet = { matchOdd, odd, playCateCode ->
                    matchOdd?.let {
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOutRightOddsDialog(matchOdd, odd, playCateCode)
                        }
                    }
                },
                clickListenerMore = { oddsKey, matchOdd ->
                    matchOdd.oddsMap?.get(oddsKey)?.forEachIndexed { index, odd ->
                        if (index >= 5) {
                            odd?.isExpand?.let { isExpand ->
                                odd.isExpand = !isExpand
                                this.notifyItemChanged(this.data.indexOf(odd))
                            }
                        }
                    }
                },
                clickExpand = { matchOdd, oddsKey ->
                    //TODO 訂閱邏輯需重新調整
                    subscribeChannelHall(matchOdd)

                    matchOdd?.oddsExpand?.get(oddsKey)?.let { oddExpand ->
                        matchOdd.oddsExpand?.put(oddsKey, !oddExpand)
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        this@apply.data.filter { any ->
                            //同一場聯賽內的賠率項(Odd)及顯示更多(OutrightShowMoreItem)
                            when (any) {
                                is OutrightShowMoreItem -> {
                                    any.matchOdd == matchOdd
                                }
                                is Odd -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                else -> {
                                    false
                                }
                            }
                        }.forEach { any ->
                            val oddsExpand = matchOdd?.oddsExpand?.get(oddsKey) ?: false

                            when (any) {
                                is OutrightShowMoreItem -> {
                                    if (any.playCateCode == oddsKey) {
                                        if (any.playCateExpand != oddsExpand) {
                                            any.playCateExpand = oddsExpand
                                            updateOutrightAdapterInMain(any)
                                        }
                                    }
                                }
                                is Odd -> {
                                    if (any.outrightCateKey == oddsKey) {
                                        if (any.playCateExpand != oddsExpand) {
                                            any.playCateExpand = oddsExpand
                                            updateOutrightAdapterInMain(any)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                onClickMatch = { matchOdd ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val newExpanded = !(matchOdd?.isExpand ?: true)
                        matchOdd?.isExpand = newExpanded

                        this@apply.data.filter { any ->
                            when (any) {
                                is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                    any == matchOdd
                                }
                                is OutrightSubTitleItem -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                is OutrightShowMoreItem -> {
                                    any.matchOdd == matchOdd
                                }
                                is Odd -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                else -> {
                                    false
                                }
                            }
                        }.forEach { any ->
                            when (any) {
                                is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                    //聯賽標題需更新與其他Item的間隔
                                    updateOutrightAdapterInMain(any)
                                }
                                is OutrightSubTitleItem -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                                is OutrightShowMoreItem -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                                is Odd -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun updateOutrightAdapterInMain(any: Any) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (game_list.adapter is OutrightLeagueOddAdapter) {
                outrightLeagueOddAdapter.notifyItemChanged(outrightLeagueOddAdapter.data.indexOf(any))
            }
        }
    }

    private val sportListAdapter by lazy {
        SportListAdapter(matchType).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                if (it.unfold == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                Handler().postDelayed(
                    { game_list?.firstVisibleRange(this, activity ?: requireActivity()) },
                    400
                )
            }
            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
                    navMatchDetailPage(matchId, matchInfoList, liveVideo)
                },
                clickListenerBet = { matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    if (mIsEnabled) {
                        avoidFastDoubleClick()
                        addOddsDialog(
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap
                        )
                    }
                },
                clickListenerQuickCateTab = { matchOdd, quickPlayCate ->
                    matchOdd.matchInfo?.let {
                        setQuickPlayCateSelected(matchOdd, quickPlayCate)
                    }
                },
                clickListenerQuickCateClose = {
                    clearQuickPlayCateSelected()
                },
                clickListenerFavorite = { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                },
                clickListenerStatistics = { matchId ->
                    navStatistics(matchId)
                },
                refreshListener = { matchId ->
                    loading()
                    viewModel.refreshGame(
                        matchType,
                        listOf(),
                        listOf(matchId)
                    )
                },
                clickLiveIconListener = { matchId, matchInfoList, _, liveVideo ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, matchInfoList, liveVideo)
                    }
                },
                clickAnimationIconListener = { matchId, matchInfoList, _, liveVideo ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, matchInfoList, liveVideo)
                    }
                },
                clickCsTabListener = { playCate, matchOdd ->
                    data.forEachIndexed { index, l ->
                        l.matchOdds.find { m ->
                            m == matchOdd
                        }?.let {
                            it.csTabSelected = playCate
                            updateLeagueBySelectCsTab(index, matchOdd)
                        }
                    }
                }
            )
        }
    }

    private fun navMatchDetailPage(
        matchId: String?,
        matchInfoList: List<MatchInfo>,
        liveVideo: Int,
    ) {
        matchId?.let {
            navOddsDetailLive(it, liveVideo)
        }
    }

    private val epsListAdapter by lazy {
        EpsListAdapter(EpsListAdapter.EpsOddListener(
            {
                subscribeChannelHall(it)
            },
            { odd, betMatchInfo, betPlayCateNameMap ->
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    addOddsDialog(
                        betMatchInfo,
                        odd,
                        PlayCate.EPS.value,
                        getString(R.string.game_tab_price_boosts_odd),
                        betPlayCateNameMap
                    )
                }
            }) { matchInfo ->
            setEpsBottomSheet(matchInfo)
        }
        )
    }

    private var isUpdatingLeague = false

    private var mLeagueOddList = ArrayList<LeagueOdd>()
    private var mQuickOddListMap = HashMap<String, MutableList<QuickPlayCate>>()

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    override fun loading() {
        stopTimer()
    }

    override fun hideLoading() {
        if (timer == null) startTimer()
    }
    init {
        afterAnimateListener = AfterAnimateListener {
            try {
//                initObserve()
//                initSocketObserver()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.resetOtherSeelectedGameType()
        mView = inflater.inflate(R.layout.fragment_sport_list, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //若為起始fragment不會有轉場動畫, 故無法透過afterAnimateListener動作
        initObserve()
        initSocketObserver()
    }

    private fun setupSportTypeList() {
        sport_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = gameTypeAdapter
            removeItemDecorations()
//            addItemDecoration(
//                SpaceItemDecoration(
//                    context,
//                    R.dimen.recyclerview_item_dec_spec_sport_type
//                )
//            )
        }
    }

    private fun setupToolbar() {


        iv_calendar.apply {
            visibility = when (matchType) {
                MatchType.EARLY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                val newSelectedStatus = !isSelected
                mCalendarSelected = newSelectedStatus
                isSelected = newSelectedStatus

                view?.game_filter_type_list?.visibility = when (iv_calendar.isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }
    }


    private fun setupGameRow() {
        game_filter_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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

        game_filter_type_list.visibility =
            if (matchType == MatchType.EARLY && mCalendarSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupGameListView() {
        game_list.apply {
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = sportListAdapter
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    when (adapter) {
                        is SportListAdapter -> {
                            if (sportListAdapter.data.isNotEmpty()) {
                                it.forEach { p ->
                                    Log.d(
                                        "[subscribe]",
                                        "訂閱 ${sportListAdapter.data[p.first].league.name} -> " +
                                                "${sportListAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                                "${sportListAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                                    )
                                    subscribeChannelHall(
                                        sportListAdapter.data[p.first].gameType?.key,
                                        sportListAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                                    )
                                }
                            }
                        }
                        //冠軍
                        is OutrightLeagueOddAdapter -> {
                            it.forEach { pair ->
                                if (pair.first < outrightLeagueOddAdapter.data.size) {
                                    val outrightDataList = outrightLeagueOddAdapter.data[pair.first]
                                    when (outrightDataList) {
                                        is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                            outrightDataList
                                        }
                                        is OutrightSubTitleItem -> {
                                            outrightDataList.belongMatchOdd
                                        }
                                        is Odd -> {
                                            outrightDataList.belongMatchOdd
                                        }
                                        is OutrightShowMoreItem -> {
                                            outrightDataList.matchOdd
                                        }
                                        else -> {
                                            null
                                        }
                                    }?.let { itemMatchOdd ->
                                        Log.d(
                                            "[subscribe]",
                                            "訂閱 ${itemMatchOdd.matchInfo?.name} -> " +
                                                    "${itemMatchOdd.matchInfo?.homeName} vs " +
                                                    "${itemMatchOdd.matchInfo?.awayName}"
                                        )
                                        subscribeChannelHall(
                                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                                            itemMatchOdd.matchInfo?.id
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
            if (viewModel.getMatchCount(matchType) < 1) {
                sportListAdapter.removePreloadItem()
            } else {
                sportListAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()
            }
        }
    }


    private fun initObserve() {
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it != null && it.isNotBlank()) {
                context?.let { context ->
                    val dialog = CustomAlertDialog(context)
                    dialog.setTitle(resources.getString(R.string.prompt))
                    dialog.setMessage(it)
                    dialog.setTextColor(R.color.color_E44438_e44438)
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
            when (matchType) {
                MatchType.IN_PLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }

                MatchType.TODAY -> {
                    updateSportType(it?.sportMenuData?.menu?.today?.items ?: listOf())
                }

                MatchType.EARLY -> {
                    updateSportType(it?.sportMenuData?.menu?.early?.items ?: listOf())
                }
                MatchType.CS -> {
                    updateSportType(it?.sportMenuData?.menu?.cs?.items ?: listOf())
                }

                MatchType.PARLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.parlay?.items ?: listOf())
                }

                MatchType.OUTRIGHT -> {
                    updateSportType(it?.sportMenuData?.menu?.outright?.items ?: listOf())
                }

                MatchType.AT_START -> {
                    updateSportType(it?.sportMenuData?.atStart?.items ?: listOf())
                }

                MatchType.EPS -> {
                    updateSportType(it?.sportMenuData?.menu?.eps?.items ?: listOf())
                }

                MatchType.OTHER -> {
                    val tempItem: MutableList<Item> = mutableListOf()
                    viewModel.specialMenuData?.items?.forEach { item ->
                        val mItem = Item(
                            code = item.code ?: "",
                            name = item.name ?: "",
                            num = item.num ?: 0,
                            play = null,
                            sortNum = item.sortNum ?: 0,
                        )
                        mItem.hasPlay = (item.play != null)
                        mItem.isSelected = item.isSelected
                        tempItem.add(mItem)
                    }
                    updateSportType(tempItem)
                }

                else -> {
                }
            }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
            (game_filter_type_list.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                game_filter_type_list,
                RecyclerView.State(),
                position
            )
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            when (game_list.adapter) {
                is SportListAdapter -> {
                    sportListAdapter.discount = userInfo?.discount ?: 1.0F
                }

                is EpsListAdapter -> {
                    epsListAdapter.discount = userInfo?.discount ?: 1.0F
                }

                is OutrightLeagueOddAdapter -> {
                    viewModel.updateOutrightDiscount(userInfo?.discount ?: 1.0F)
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
                        game_list.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                        sportListAdapter.data = mLeagueOddList.onEach { leagueOdd ->
                            // 將儲存的賠率表指定的賽事列表裡面
                            val leagueOddFromMap = leagueOddMap[leagueOdd.league.id]
                            leagueOddFromMap?.let {
                                leagueOdd.matchOdds.forEach { mMatchOdd ->
                                    mMatchOdd.oddsMap =
                                        leagueOddFromMap.matchOdds.find { matchOdd -> mMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id }?.oddsMap
                                }
                            }
                            leagueOdd.gameType = gameType
                        }.toMutableList()
                    } else {
                        game_list.layoutManager =
                            SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                        sportListAdapter.data = mLeagueOddList
                        // Todo: MatchType.OTHER 要顯示無資料與隱藏篩選清單
                    }
                    if (game_list.adapter !is SportListAdapter) game_list.adapter = sportListAdapter

                    //如果data資料為空時，又有其他球種的情況下，自動選取第一個
                    if (mLeagueOddList.isNullOrEmpty() && gameTypeAdapter.dataSport.size > 1) {
                        if (matchType == MatchType.OTHER) {
                            // 待觀察，再決定是否要補
                        } else {
                            viewModel.switchFirstSportType(matchType)
                        }
                    }

                    mLeagueOddList.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    oddsListResult.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                            mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                matchOdd.quickPlayCateList ?: mutableListOf()
                        }
                    }
                    sportListAdapter.limitRefresh()
                    // TODO 這裡要確認是否有其他地方重複呼叫
                    Log.d("Hewie", "observe => OddsListGameHallResult")

                    game_list?.firstVisibleRange(sportListAdapter, activity ?: requireActivity())

                }
            } ?: run {
                sportListAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()
            }
            hideLoading()
        }

        viewModel.oddsListGameHallIncrementResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueListIncrementResult ->
                val leagueListIncrement = leagueListIncrementResult.oddsListResult

                leagueListIncrementResult.leagueIdList?.forEach { leagueId ->
                    //判斷此聯賽是否已經於畫面顯示
                    sportListAdapter.data.find { adapterLeagueOdd -> adapterLeagueOdd.league.id == leagueId }
                        ?.let { onScreenLeague ->

                            val targetIndex = sportListAdapter.data.indexOf(onScreenLeague)
                            val changedLeague =
                                leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == onScreenLeague.league.id }

                            //若api response沒有該聯賽資料則不顯示該聯賽
                            changedLeague?.let { changedLeagueOdd ->
                                unSubscribeLeagueChannelHall(sportListAdapter.data[targetIndex])
                                val targetLeagueOdd = sportListAdapter.data[targetIndex]
                                sportListAdapter.data[targetIndex] = changedLeagueOdd.apply {
                                    this.unfold = targetLeagueOdd.unfold
                                    this.gameType = targetLeagueOdd.gameType
                                    this.searchMatchOdds = targetLeagueOdd.searchMatchOdds
                                }
                                //更新快捷玩法
                                changedLeagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                                    mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                        matchOdd.quickPlayCateList ?: mutableListOf()
                                }
                                subscribeChannelHall(sportListAdapter.data[targetIndex])
                                Log.d("Hewie", "更新聯賽：${targetLeagueOdd.league.name}")
                            } ?: run {
                                Log.d("Hewie",
                                    "移除聯賽：${sportListAdapter.data[targetIndex].league.name}")
                                unSubscribeChannelHall(sportListAdapter.data[targetIndex])
                                sportListAdapter.data.removeAt(targetIndex)
                                sportListAdapter.notifyItemRemoved(targetIndex)
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
                            //更新快捷玩法
                            changedLeagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                                mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                    matchOdd.quickPlayCateList ?: mutableListOf()
                            }
                            sportListAdapter.data.add(insertLeagueOdd)
                            sportListAdapter.notifyItemInserted(sportListAdapter.data.size - 1)
                            subscribeChannelHall(insertLeagueOdd)
                            Log.d("Hewie", "增加聯賽：${insertLeagueOdd.league.name}")
                        }
                    }
                }
            }
            hideLoading()
        }


        //KK要求，當球類沒有資料時，自動選取第一個有賽事的球種
        viewModel.isNoHistory.observe(this.viewLifecycleOwner) {
            when {
                //當前MatchType有玩法數量，只是目前的球種沒有
                it && curMatchTypeHasMatch() -> {
                    unSubscribeChannelHallAll()
                    if (matchType != MatchType.OTHER) {
                        viewModel.switchMatchType(matchType)
                    }
                }
            }

            hideLoading()
        }

        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it
            iv_calendar.isVisible = matchType == MatchType.EARLY && !it
            sportListAdapter.removePreloadItem()
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { betInfoList ->

                sportListAdapter.betInfoList = betInfoList

                val epsOdds = epsListAdapter.dataList

                epsOdds.forEach { epsLeagueOddsItem ->
                    epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.oddsEps?.eps?.forEach { odd ->
                            odd?.isSelected = betInfoList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                        }
                    }
                }
                epsListAdapter.notifyDataSetChanged()

                outrightLeagueOddAdapter.data.filterIsInstance<Odd>().forEach { odd ->
                    val betInfoSelected = betInfoList.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd.id
                    }
                    if (odd.isSelected != betInfoSelected) {
                        odd.isSelected = betInfoSelected
                        outrightLeagueOddAdapter.notifyItemChanged(outrightLeagueOddAdapter.data.indexOf(
                            odd))
                    }
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                sportListAdapter.oddsType = oddsType
                epsListAdapter.oddsType = oddsType
            }
        }

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner) {
//            countryAdapter.apply {
//                data.forEach { row ->
//                    row.list.forEach { league ->
//                        league.isSelected = it.any { it.id == league.id }
//                    }
//                }
//
//                notifyDataSetChanged()
//            }
        }

        viewModel.playList.observe(this.viewLifecycleOwner) { event ->

        }

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {

        }
        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueList ->
                navGameLeague(leagueIdList = leagueList.map { league ->
                    league.id
                })
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            sportListAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }
            updateAllGameList()
        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }

        viewModel.checkInListFromSocket.observe(this.viewLifecycleOwner) { leagueChangeEvent ->
            if (leagueChangeEvent != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isUpdatingLeague) {
                        isUpdatingLeague = true

                        //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
                        val nowGameType =
                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key

                        if (sportListAdapter.data.isNotEmpty()) {
                            if (nowGameType == leagueChangeEvent.gameType && leagueChangeEvent.leagueIdList?.isNotEmpty() == true) {
                                //不管是否相同聯賽，也要確認是否要更新賽事資訊
                                withContext(Dispatchers.Main) {
                                    if (matchType == MatchType.OTHER) {
                                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                                    } else {
                                        viewModel.getGameHallList(
                                            matchType,
                                            leagueIdList = leagueChangeEvent.leagueIdList,
                                            isReloadDate = false,
                                            isIncrement = true
                                        )
                                    }
                                }
                            } else if (matchType == MatchType.OTHER) {
                                viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                            }
                        }
                        isUpdatingLeague = false
                    }
                }
            }
        }
    }

    /**
     * 判斷當前MatchType是否有玩法數量
     */
    private fun curMatchTypeHasMatch(): Boolean {
        return when (matchType) {
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
    }

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.firstSwitchMatch(matchType = matchType)
                    if (matchType == MatchType.OTHER) {
                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = true)
                    } else if (!gameType.isNullOrEmpty() && matchType == MatchType.OUTRIGHT) {
                        gameType?.let { gameType ->
                            viewModel.getOutrightOddsList(gameType = gameType,
                                outrightLeagueId = outrightLeagueId)
                        }
                    } else {
                        viewModel.getGameHallList(
                            matchType = matchType,
                            isReloadDate = true,
                            isReloadPlayCate = false,
                            isLastSportType = true
                        )
                    }
                    viewModel.getMatchCategoryQuery(matchType)
                    subscribeSportChannelHall()
                } else {
                    stopTimer()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                when (game_list.adapter) {
                    is SportListAdapter -> {

                        sportListAdapter.data.toList().forEachIndexed { index, leagueOdd ->
                            if (matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value) {
                                leagueOdd.matchOdds.find { m ->
                                    m.matchInfo?.id == matchStatusChangeEvent.matchStatusCO.matchId
                                }?.let { mo ->
                                    leagueOdd.matchOdds.remove(mo)
                                    if (leagueOdd.matchOdds.size > 0) {
                                        sportListAdapter.notifyItemChanged(index)
                                    } else {
                                        unSubscribeChannelHall(leagueOdd)
                                        sportListAdapter.data.remove(leagueOdd)
                                        sportListAdapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }

                        val leagueOdds = sportListAdapter.data

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
                                    unSubscribeChannelHall(leagueOdd)
                                    sportListAdapter.data.remove(leagueOdd)
                                    sportListAdapter.notifyItemRemoved(index)
                                }
                            }
                        }
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is SportListAdapter -> {
                        val leagueOdds = sportListAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchClock(
                                        matchOdd,
                                        matchClockEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }
                }
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { oddsChangeEvent ->
                when (game_list.adapter) {
                    is SportListAdapter -> {
                        sportListAdapter.data.forEach { leagueOdd ->
                            leagueOdd.matchOdds.forEach { matchOdd ->
                                matchOdd.quickPlayCateList =
                                    mQuickOddListMap[matchOdd.matchInfo?.id]
                            }
                        }

                        val leagueOdds = sportListAdapter.data
                        leagueOdds.sortOddsMap()
                        //翻譯更新
                        leagueOdds.forEach { LeagueOdd ->
                            LeagueOdd.matchOdds.forEach { MatchOdd ->
                                if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                    //馬克說betPlayCateNameMap還是由socket更新
                                    oddsChangeEvent.betPlayCateNameMap?.let {
                                        MatchOdd.betPlayCateNameMap?.putAll(it)
                                    }
                                }
                            }
                        }

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchOdds(
                                        context, matchOdd, oddsChangeEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                leagueOddMap[leagueOdd.league.id] = leagueOdd
                                updateGameList(index, leagueOdd)
                                updateBetInfo(leagueOdd, oddsChangeEvent)
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
                                } == true && !leagueOdd.isClose) {
                                updateBetInfo(leagueOdd, oddsChangeEvent)
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }

                    is OutrightLeagueOddAdapter -> {
                        viewModel.updateOutrightOddsChange(context, oddsChangeEvent)
                    }
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is SportListAdapter -> {
                        val leagueOdds = sportListAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
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
                    is SportListAdapter -> {
                        val leagueOdds = sportListAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                //暫時不處理 防止過多更新
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
                                } == true && !epsLeagueOddsItem.isClose) {
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
                    is SportListAdapter -> {
                        sportListAdapter.data.forEach { leagueOdd ->
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

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                viewModel.checkGameInList(
                    matchType = matchType,
                    leagueChangeEvent = leagueChangeEvent,
                )
                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
                sportListAdapter.data.closePlayCate(it)
                sportListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        sportListAdapter.data[index] = leagueOdd
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportListAdapter.updateLeague(index, leagueOdd)
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (leagueOdd.matchOdds.filter { matchOdd ->
                    matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                }.any { matchOdd ->
                    matchOdd.oddsMap?.values?.any { oddList ->
                        oddList?.any { odd ->
                            odd?.isSelected == true
                        } == true
                    } == true
                }) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun updateBetInfo(
        epsLeagueOddsItem: EpsLeagueOddsItem,
        oddsChangeEvent: OddsChangeEvent,
    ) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (epsLeagueOddsItem.leagueOdds?.matchOdds?.filter { matchOddsItem -> matchOddsItem.matchInfo?.id == oddsChangeEvent.eventId }
                    ?.any { matchOdd ->
                        matchOdd.oddsMap?.values?.any { oddList ->
                            oddList?.any { odd ->
                                odd?.isSelected == true
                            } == true
                        } == true
                    } == true) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun updateAllGameList() {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            sportListAdapter.data.forEachIndexed { index, leagueOdd ->
                sportListAdapter.updateLeague(index,
                    leagueOdd)
            }
        }
    }

    private fun MutableList<LeagueOdd>.sortOddsMap() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (_, value) ->
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

            }

            moreEpsInfoBottomSheet.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        gameTypeAdapter.dataSport = gameTypeList
        //post待view繪製完成
        sport_type_list?.post {
            //球種如果選過，下次回來也需要滑動置中
            if (!gameTypeList.isNullOrEmpty()) {
                (sport_type_list?.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                    sport_type_list,
                    RecyclerView.State(),
                    gameTypeList.indexOfFirst { item ->
                        item.isSelected
                    }
                )
            }
            if (gameTypeList.isEmpty()) {
                sport_type_list?.visibility = View.GONE
                iv_calendar?.visibility = View.GONE
                game_filter_type_list?.visibility = View.GONE
            } else {
                sport_type_list?.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
                iv_calendar?.apply {
                    visibility = when (matchType) {
                        MatchType.EARLY -> View.VISIBLE
                        else -> View.GONE
                    }
                    isSelected = mCalendarSelected
                }
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
        matchCategoryName: String? = null,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        val matchType = when (dateAdapter.data.find {
            it.isSelected
        }?.date) {
            MatchType.IN_PLAY.postValue -> MatchType.IN_PLAY
            else -> null
        }

//        gameType?.let {
//            val action = GameV3FragmentDirections.actionGameV3FragmentToGameLeagueFragment(
//                matchType ?: matchType,
//                gameType,
//                leagueIdList.toTypedArray(),
//                matchIdList.toTypedArray(),
//                matchCategoryName
//            )
//            findNavController().navigate(action)
//        }
    }

    private fun navOddsDetailLive(matchId: String, liveVideo: Int) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

//        gameType?.let {
//            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
//                matchType,
//                gameType,
//                matchId,
//                liveVideo
//            )
//
//            findNavController().navigate(action)
//        }
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId,
            StatisticsDialog.StatisticsClickListener { clickMenu() })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            val fastBetDataBean = FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName = "",
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null,
            )
            (activity as GameActivity).setupBetData(fastBetDataBean)
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
                    matchType = matchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName,
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                )
                (activity as GameActivity).setupBetData(fastBetDataBean)
            }
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
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
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
                        matchOdd.matchInfo?.id
                    )

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                matchOdd.matchInfo?.id
            )
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
                viewModel.setCurMatchType(null) //避免matchType和curMatchType相同，導致後續流程中斷的問題
                viewModel.switchMatchType(matchType)
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
            setupSportTypeList()
            setupToolbar()
            setupGameRow()
            setupGameListView()
            //從內頁返回後要重置位置
        }

        if (MultiLanguagesApplication.colorModeChanging) {
            initObserve()
            initSocketObserver()
            MultiLanguagesApplication.colorModeChanging = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelectedLeague()
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
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
        selectedQuickPlayCate: QuickPlayCate,
    ) {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected =
                            true
                    }
                }
            }
        }
    }
}