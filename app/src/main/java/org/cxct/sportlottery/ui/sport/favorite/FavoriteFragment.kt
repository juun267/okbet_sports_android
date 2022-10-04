package org.cxct.sportlottery.ui.sport.favorite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.search.SportSearchtActivity
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

    var dataSport = mutableListOf<Item>()
        set(value) {
            field = value
            field.forEachIndexed { index, item ->
                favoriteAdapter.notifyItemChanged(index, item)
            }
        }
    private lateinit var mListPop: ListPopupWindow
    private var gameType: GameType? = null
    private val favoriteAdapter by lazy {
        FavoriteAdapter(MatchType.MY_EVENT).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                if (it.unfold == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                Handler().postDelayed(
                    { favorite_game_list?.firstVisibleRange(this, activity ?: requireActivity()) },
                    400
                )
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
        initPopwindow()
        initObserver()
        initSocketObserver()
        favoriteAdapter.setPreloadItem()
        viewModel.getSportList()
        viewModel.getFavoriteMatch()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            viewModel.getFavoriteMatch(gameType?.key)
    }


    private fun initView() {
        setupToolbar()
        cl_bet_all_sports.setOnClickListener {
            if (mListPop.isShowing) {
                cl_bet_all_sports.isSelected = false
                mListPop.dismiss()
            } else {
                cl_bet_all_sports.isSelected = true
                mListPop.show()
            }
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
        lin_search.setOnClickListener {
            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
        }
    }

    private fun initPopwindow() {
        mListPop = ListPopupWindow(requireContext())
        mListPop.width = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bg_pop_up_arrow
            )
        )
        mListPop.setAdapter(SportTypeTextAdapter(dataSport))
        mListPop.anchorView = cl_bet_all_sports //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.verticalOffset = 5
        mListPop.isModal = true //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                mListPop.dismiss()
                var sportItem = dataSport[position]
                dataSport.forEachIndexed { index, item ->
                    item.isSelected = index == position
                }
                tv_all_sports.text = sportItem.name
                unSubscribeChannelHallAll()
                gameType =
                    if (sportItem.code.isNullOrBlank()) null else GameType.getGameType(sportItem.code)
                viewModel.switchGameType(sportItem)
                favoriteAdapter.setPreloadItem()
            }
        })
        mListPop.setOnDismissListener {
            cl_bet_all_sports.isSelected = false
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
                                        "${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}" +
                                        ",${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.id}"
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

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                favoriteAdapter.data.forEachIndexed { index, leagueOdd ->
                    if (matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value) {
                        leagueOdd.matchOdds.find { m ->
                            m.matchInfo?.id == matchStatusChangeEvent.matchStatusCO.matchId
                        }?.let { mo ->
                            leagueOdd.matchOdds.remove(mo)
                            if (leagueOdd.matchOdds.size > 0) {
                                favoriteAdapter.notifyItemChanged(index)
                            } else {
                                unSubscribeChannelHall(leagueOdd)
                                favoriteAdapter.data.remove(leagueOdd)
                                favoriteAdapter.notifyItemRemoved(index)
                            }
                        }
                    }
                }

                val leagueOdds = favoriteAdapter.data

                leagueOdds.forEachIndexed { index, leagueOdd ->
                    if (SocketUpdateUtil.updateMatchStatus(
                            gameType = gameType?.key,
                            leagueOdd.matchOdds.toMutableList(),
                            matchStatusChangeEvent,
                            context
                        ) &&
                        leagueOdd.unfold == FoldState.UNFOLD.code
                    ) {
                        if (leagueOdd.matchOdds.isNullOrEmpty()) {
                            unSubscribeChannelHall(leagueOdd)
                            favoriteAdapter.data.remove(leagueOdd)
                            favoriteAdapter.notifyItemRemoved(index)
                        }
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        val leagueOdds = favoriteAdapter.data

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

        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            when (favorite_game_list?.adapter) {
                is FavoriteAdapter -> {
                    val leagueOdds = favoriteAdapter.data
                    leagueOdds.sortOddsMap()
                    //翻譯更新

                    leagueOdds.forEach { LeagueOdd ->
                        LeagueOdd.matchOdds.forEach { MatchOdd ->
                            if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                //馬克說betPlayCateNameMap還是由socket更新
                                oddsChangeEvent.betPlayCateNameMap?.let {
                                    MatchOdd.betPlayCateNameMap?.putAll(it)
                                }
//                                    oddsChangeEvent.odds?.let {
//                                        MatchOdd.oddsMap?.putAll(it)
//                                    }
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
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        val leagueOdds = favoriteAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfold == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        val leagueOdds = favoriteAdapter.data
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

                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        favoriteAdapter.data.forEach { leagueOdd ->
                            subscribeChannelHall(leagueOdd)
                        }
                    }
                }
            }
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
//            it?.let { leagueChangeEvent ->
//                viewModel.checkGameInList(
//                    leagueChangeEvent = leagueChangeEvent,
//                )
//                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
//            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.peekContent()?.let {
                if (dataSport.find { item -> item.isSelected }?.code != it.gameType) return@observe
                favoriteAdapter.data.closePlayCate(it)
                favoriteAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {
        favoriteAdapter.data[index] = leagueOdd
        if (favorite_game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !favorite_game_list.isComputingLayout) {
            favoriteAdapter.updateLeague(index, leagueOdd)
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


    private fun initObserver() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            favoriteAdapter.discount = it?.discount ?: 1.0F
        }
        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            updateSportList(it)
        }
        viewModel.favorMatchOddList.observe(this.viewLifecycleOwner) {
            it.peekContent()?.toMutableList().let { leagueOddList ->
                hideLoading()
                favoriteAdapter.removePreloadItem()
                favorite_game_list.layoutManager =
                    SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                //檢查是否有取得我的賽事資料, 對介面進行調整
                when {
                    leagueOddList.isNullOrEmpty() && dataSport.size > 1 -> {
                        unSubscribeChannelHallAll()
                        noFavoriteMatchViewState()
                    }
                    leagueOddList.isNullOrEmpty() -> noFavoriteMatchViewState()
                    else -> {
                        showFavoriteMatchViewState()
                    }
                }
                favoriteAdapter.data = leagueOddList
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
                //全部球类的时候，赛事数据为空，则隐藏筛选按钮，显示搜索
                (gameType == null && leagueOddList.isEmpty()).let {
                    cl_bet_all_sports.isVisible = !it
                    lin_search.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
            hideLoading()
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

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        val gameType = GameType.getGameType(matchInfo?.gameType)

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
            SportDetailActivity.startActivity(requireContext(), matchInfo = it)
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

    private fun updateSportList(list: List<StatusSheetData>) {
        if (dataSport.isNotEmpty()) return
        list.forEach {
            dataSport.add(
                Item(
                    code = it.code.orEmpty(),
                    name = it.showName.orEmpty(),
                    num = 0,
                    play = null,
                    sortNum = 0
                )
            )
        }
        //如沒有選過的，預設選第一個
        dataSport.find {
            it.isSelected
        }.let {
            if (it == null) {
                dataSport.firstOrNull()?.isSelected = true
            }
        }
    }
}