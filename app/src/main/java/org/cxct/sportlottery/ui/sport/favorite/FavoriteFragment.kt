package org.cxct.sportlottery.ui.sport.favorite

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.data.DetailParams
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.EventBus

/**
 * @app_destination 我的賽事
 */
@SuppressLint("LogNotTimber")
class FavoriteFragment : BaseBottomNavigationFragment<FavoriteViewModel>(FavoriteViewModel::class) {

    companion object {
        fun newInstance(): FavoriteFragment {
            val args = Bundle()
            val fragment = FavoriteFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var dataSport = listOf<Item>()
        set(value) {
            field = value
            field.forEachIndexed { index, item ->
                favoriteAdapter.notifyItemChanged(index, item)
            }
        }
    private val mListPop by lazy { ListPopupWindow(requireContext()) }
    private val favoriteAdapter by lazy {
        FavoriteAdapter(MatchType.MY_EVENT).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                subscribeChannelHall(it)
            }

            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, _, gameMatchType, _ ->
                    data.forEach {
                        it.matchOdds.find {
                            TextUtils.equals(matchId, it.matchInfo?.id)
                        }?.let {
                            navMatchDetailPage(it.matchInfo)
                            return@LeagueOddListener
                        }
                    }
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
                    setQuickPlayCateSelected(matchOdd, quickPlayCate)
                },
                clickListenerQuickCateClose = {
                    clearQuickPlayCateSelected()
                },
                clickListenerFavorite = { matchId ->
                    viewModel.pinFavorite(FavoriteType.MATCH, matchId)
                },
                clickListenerStatistics = { matchId ->
                    data.forEach {
                        it.matchOdds.find {
                            TextUtils.equals(matchId, it.matchInfo?.id)
                        }?.let {
                            navMatchDetailPage(it.matchInfo)
                            return@LeagueOddListener
                        }
                    }
                },
                refreshListener = {},
                clickLiveIconListener = { matchId, _, gameMatchType, _ ->
                    if (viewModel.checkLoginStatus()) {
                        data.forEach {
                            it.matchOdds.find {
                                TextUtils.equals(matchId, it.matchInfo?.id)
                            }?.let {
                                navMatchDetailPage(it.matchInfo)
                                return@LeagueOddListener
                            }
                        }
                    }
                },
                clickAnimationIconListener = { matchId, _, gameMatchType, _ ->
                    if (viewModel.checkLoginStatus()) {
                        data.forEach {
                            it.matchOdds.find {
                                TextUtils.equals(matchId, it.matchInfo?.id)
                            }?.let {
                                navMatchDetailPage(it.matchInfo)
                                return@LeagueOddListener
                            }
                        }
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false).apply {
            setupLeagueOddList(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initSocketObserver()
        viewModel.getFavoriteMatch()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            viewModel.getSportQuery(getLastPick = true, false, getFavoriteMatch = true)
    }

    private fun initView() {
        setupToolbar()
        btn_sport.setOnClickListener {
            showSportType()
        }
    }

    private fun setupToolbar() {
        ImmersionBar.with(this)
            .statusBarView(v_statusbar)
            .statusBarDarkFont(true)
            .fitsSystemWindows(true)
            .init()

        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }
    }

    private fun showSportType() {
        if (dataSport.isEmpty()) {
            return
        }
        mListPop.width = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bg_pop_up_arrow
            )
        )
        mListPop.setAdapter(SportTypeTextAdapter(dataSport))
        mListPop.setAnchorView(btn_sport) //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.verticalOffset = 5
        mListPop.setModal(true) //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,

                position: Int,
                id: Long,
            ) {
                mListPop.dismiss()
                var sportItem = dataSport[position]
                sportItem.isSelected = true
                btn_sport.text = sportItem.name
                unSubscribeChannelHallAll()
                viewModel.switchGameType(sportItem)
            }
        })
        mListPop.setOnDismissListener {
            btn_sport.isSelected = false
        }
        if (mListPop.isShowing) {
            btn_sport.isSelected = false
            mListPop.dismiss()
        } else {
            btn_sport.isSelected = true
            mListPop.show()
        }
    }

    private fun setupLeagueOddList(view: View) {
        view.favorite_game_list.apply {
            adapter = favoriteAdapter
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    if (favoriteAdapter.data.isNotEmpty()) {
                        it.forEach { p ->
                            Log.d(
                                "[subscribe]",
                                "訂閱 ${favoriteAdapter.data[p.first].league.name} -> " +
                                        "${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                        "${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                            )
                            subscribeChannelHall(
                                favoriteAdapter.data[p.first].gameType?.key,
                                favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                            )
                        }
                    }
                }
            )
        }
    }


    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                val leagueOdds = favoriteAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            dataSport.find { gameType -> gameType.isSelected }?.code,
                            leagueOdd.matchOdds.toMutableList(),
                            matchStatusChangeEvent,
                            context
                        ) &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        if (leagueOdd.matchOdds.isNullOrEmpty()) {
                            favoriteAdapter.data.remove(leagueOdd)
                        }

                        updateGameList(index, leagueOdd)
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val leagueOdds = favoriteAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchClock(
                                matchOdd,
                                matchClockEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code) {
                    }
                }
            }
        }

        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
                //該處篩選於viewModel內是不同寫法 暫時不同步於ServiceBroadcastReceiver內

                val leagueOdds = favoriteAdapter.data

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

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateMatchOdds(
                                context,
                                matchOdd,
                                oddsChangeEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        updateBetInfo(leagueOdd, oddsChangeEvent)
                        updateGameList(index, leagueOdd)
                    }
                }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val leagueOdds = favoriteAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val leagueOdds = favoriteAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.matchOdds.any { matchOdd ->
                            SocketUpdateUtil.updateOddStatus(
                                matchOdd,
                                globalStopEvent
                            )
                        } &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()
                favoriteAdapter.data.forEach { leagueOdd ->
                    subscribeChannelHall(leagueOdd)
                }
            }
        }

        receiver.leagueChange.observe(this.viewLifecycleOwner) { event ->
            event?.let {
                viewModel.getSportQuery(getLastPick = true) //而收到事件之后, 重新调用/api/front/sport/query用以加载上方球类选单

                if (event.gameType == dataSport.find { gameType -> gameType.isSelected }?.code) {
                    val updateLeague =
                        favoriteAdapter.data.find { it.league.id == event?.matchIdList?.firstOrNull() }
                    if (updateLeague != null) {
                        val updateMatch =
                            updateLeague.matchOdds.find { it.matchInfo?.id == event?.matchIdList?.firstOrNull() }
                        if (updateMatch != null) {
                            viewModel.getFavoriteMatch()
                        }
                    }
                }
            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.peekContent()?.let {
                if (dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
                favoriteAdapter.data.closePlayCate(it)
                favoriteAdapter.notifyDataSetChanged()
            }
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


    private fun initObserver() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            favoriteAdapter.discount = it?.discount ?: 1.0F
        }

        viewModel.sportQueryData.observe(this.viewLifecycleOwner) {
            it?.peekContent()?.let { sportQueryData ->
                updateGameTypeList(sportQueryData.items?.map { item ->
                    Item(
                        code = item.code ?: "",
                        name = item.name ?: "",
                        num = item.num ?: 0,
                        play = null,
                        sortNum = item.sortNum ?: 0
                    ).apply {
                        isSelected = true
                    }
                })

            }
        }

        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner) {
            it.peekContent()?.let { leagueOddList ->
                hideLoading()
                favorite_game_list.layoutManager =
                    SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                val leagueData = leagueOddList.toMutableList()

                //檢查是否有取得我的賽事資料, 對介面進行調整
                when {
                    leagueData.isNullOrEmpty() && dataSport.size > 1 -> {
                        unSubscribeChannelHallAll()
                        viewModel.getSportQuery(getLastPick = false,
                            isReloadPlayCate = true,
                            getFavoriteMatch = true)
                        return@observe
                    }
                    leagueData.isNullOrEmpty() -> noFavoriteMatchViewState()
                    else -> {
                        showFavoriteMatchViewState()
                    }
                }

                favoriteAdapter.data = leagueData
                try {
                    /*目前流程 需要先解除再綁定 socket流程下才會回傳內容*/
                    favoriteAdapter.data.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    favorite_game_list?.firstVisibleRange(favoriteAdapter,
                        activity ?: requireActivity())

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let {

                val leagueOdds = favoriteAdapter.data

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

                updateAllGameList()
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) { favorMatchList ->

            //若用戶的最愛清單無賽事id則隱藏相關的介面, 若有則等待後續我的賽事資料判斷需不需要顯示
            if (favorMatchList.isNullOrEmpty()) {
                noFavoriteMatchViewState()
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                favoriteAdapter.oddsType = oddsType
            }
        }
    }

    /**
     * 若我的賽事無資料時顯示的介面
     */
    private fun noFavoriteMatchViewState() {
        fl_no_game.visibility = View.VISIBLE
        favorite_game_list.visibility = View.GONE
    }

    /**
     * 若我的賽事有資料時顯示的介面
     */
    private fun showFavoriteMatchViewState() {
        fl_no_game.visibility = View.GONE
        favorite_game_list.visibility = View.VISIBLE
    }

    private fun updateGameTypeList(items: List<Item>?) {
        dataSport = mutableListOf<Item>().apply {
            add(Item(code = "", name = getString(R.string.all_sport), 0, null, 0))
            items?.let {
                addAll(it)
            }
        }
        //如果没有选中的就默认选中第一个
        items?.find {
            it.isSelected
        }.let {
            if (it == null) {
                dataSport[0].isSelected = true
            }
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
            GameType.getGameType(dataSport.find { item -> item.isSelected }?.code)

        if (gameType == null || matchInfo == null) {
            return
        }

        val fastBetDataBean = FastBetDataBean(
            matchType = MatchType.MY_EVENT,
            gameType = gameType,
            playCateCode = playCateCode,
            playCateName = playCateName,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap,
        )
        (activity as MainTabActivity).setupBetData(fastBetDataBean)
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

    private fun navMatchDetailPage(matchInfo: MatchInfo?) {
        matchInfo?.let { it ->
            val gameType =
                GameType.getGameType(matchInfo.gameType)
            gameType?.let {
                if (gameType != null) {
                    SportDetailActivity.startActivity(requireContext(),
                        DetailParams(matchType = null,
                            gameType = gameType,
                            matchId = matchInfo.id,
                            matchInfo = matchInfo))
                }
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        favorite_game_list?.let {
            favoriteAdapter.data[index] = leagueOdd
            if (it.scrollState == RecyclerView.SCROLL_STATE_IDLE && !it.isComputingLayout) {
                favoriteAdapter.updateLeague(index, leagueOdd)
            }
        }
    }

    private fun updateAllGameList() {
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            favoriteAdapter.data.forEachIndexed { index, leagueOdd ->
                favoriteAdapter.updateLeague(index,
                    leagueOdd)
            }
        }
    }

    private fun clearQuickPlayCateSelected() {
        favoriteAdapter.data.forEach { leagueOdd ->
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
        favoriteAdapter.data.forEach { leagueOdd ->
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