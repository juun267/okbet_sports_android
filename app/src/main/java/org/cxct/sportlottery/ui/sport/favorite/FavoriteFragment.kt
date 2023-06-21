package org.cxct.sportlottery.ui.sport.favorite

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_favorite.view.*
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
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
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.favorite.FavoriteActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.common.LeagueOddListener
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import timber.log.Timber

/**
 * @app_destination 我的賽事
 */
@SuppressLint("LogNotTimber")
class FavoriteFragment : BaseBottomNavigationFragment<FavoriteViewModel>(FavoriteViewModel::class) {

    var dataSport = mutableListOf<Item>()
        set(value) {
            field = value
            field.forEachIndexed { index, item ->
                favoriteAdapter.notifyItemChanged(index, item)
            }
        }
    private lateinit var mListPop: ListPopupWindow
    private var sportTypeTextAdapter = SportTypeTextAdapter(dataSport)
    private var gameType: GameType? = null
    private val favoriteAdapter by lazy {
        FavoriteAdapter(MatchType.MY_EVENT).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            leagueListener = LeagueListener {
                if (it.unfoldStatus == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                Handler().postDelayed(
                    { favorite_game_list?.firstVisibleRange(this, activity ?: requireActivity()) },
                    400
                )
            }
            leagueOddListener =
                LeagueOddListener(clickListenerPlayType = { matchId, _, gameMatchType, _ ->
                    data.forEach {
                        it.matchOdds.find {
                            TextUtils.equals(matchId, it.matchInfo?.id)
                        }?.let {
                            navMatchDetailPage(it.matchInfo)
                            return@LeagueOddListener
                        }
                    }
                },
                    clickListenerBet = { view, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOddsDialog(
                                matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap
                            )
                        }
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
                    })
        }
    }
    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
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
                            } && leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
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
        viewModel.getFavoriteMatch()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            receiver.oddsChangeListener = mOddsChangeListener
            viewModel.getFavoriteMatch()
        }
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
        ImmersionBar.with(this).statusBarView(v_statusbar).statusBarDarkFont(true)
            .fitsSystemWindows(true).init()

//        iv_menu_left.setOnClickListener {
//            EventBus.getDefault().post(MenuEvent(true))
//        }
        iv_logo.isInvisible = activity is MainTabActivity
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).backMainHome()
        }
//        lin_search.setOnClickListener {
//            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
//        }
        iv_logo.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun initPopwindow() {
        mListPop = ListPopupWindow(requireContext())
        mListPop.width = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_pop_up_arrow
            )
        )
        mListPop.setAdapter(sportTypeTextAdapter)
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

            addScrollWithItemVisibility(onScrolling = {
                unSubscribeChannelHallAll()
            }, onVisible = {
                if (favoriteAdapter.data.isNotEmpty()) {
                    it.forEach { p ->
                        Log.d(
                            "[subscribe]",
                            "訂閱 ${favoriteAdapter.data[p.first].league.name} -> " + "${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " + "${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}" + ",${favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.id}"
                        )
                        subscribeChannelHall(
                            favoriteAdapter.data[p.first].gameType?.key,
                            favoriteAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                        )
                    }
                }
            })
        }
    }

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {
        //监听体育服务
        setupSportStatusChange(this) {
            if (it) {
                //如果首页不做处理
                if (activity is MainTabActivity) {
                    //体育服务分支没有 FavoriteActivity ， 只好判断非MainTabActivity
                } else {
                    activity?.finish()
                }
            }
        }
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) { matchStatusChangeEvent ->

            if (matchStatusChangeEvent == null) {
                return@observe
            }

            val unSubscribed = mutableListOf<LeagueOdd>()
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
                            unSubscribed.add(leagueOdd)
//                            favoriteAdapter.data.remove(leagueOdd)
//                            favoriteAdapter.notifyItemRemoved(index)
                        }
                    }
                }
            }

            removeLeagues(unSubscribed)

            favoriteAdapter.data.forEach { leagueOdd ->
                if (SocketUpdateUtil.updateMatchStatus(
                        gameType = gameType?.key,
                        leagueOdd.matchOdds?.toMutableList(),
                        matchStatusChangeEvent,
                        context
                    ) && leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                ) {
                    if (leagueOdd.matchOdds.isNullOrEmpty()) {
                        unSubscribeChannelHall(leagueOdd)
                        unSubscribed.add(leagueOdd)
//                        favoriteAdapter.data.remove(leagueOdd)
//                        favoriteAdapter.notifyItemRemoved(index)
                    }
                }
            }

            removeLeagues(unSubscribed)
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        val leagueOdds = favoriteAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchClock(
                                        matchOdd, matchClockEvent
                                    )
                                } && leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                                //暫時不處理 防止過多更新
                            }
                        }
                    }
                }
            }
        }

        receiver.oddsChangeListener = mOddsChangeListener

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (favorite_game_list.adapter) {
                    is FavoriteAdapter -> {
                        val leagueOdds = favoriteAdapter.data

                        leagueOdds.forEachIndexed { _, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
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
                                        matchOdd, globalStopEvent
                                    )
                                } && leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
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

    private fun removeLeagues(leagueOdd: MutableList<LeagueOdd>) {
        leagueOdd.forEach {
            val index = favoriteAdapter.data.indexOf(it)
            if (index >= 0) {
                favoriteAdapter.data.removeAt(index)
                favoriteAdapter.notifyItemRemoved(index)
            }
        }
        leagueOdd.clear()
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
            sportTypeTextAdapter = SportTypeTextAdapter(dataSport)
            mListPop.setAdapter(sportTypeTextAdapter)
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


//                val newList = mutableListOf<LeagueOdd>()
//                leagueOddList.forEachIndexed { index, lol ->
//                    if (newList.any { nl -> nl.gameType == lol.gameType }) {
//                        val index1 =
//                            newList.indexOf(newList.find { nl -> nl.gameType == lol.gameType })
//                        newList[index1].matchOdds.addAll(lol.matchOdds)
//                    } else {
//                        newList.add(lol)
//                    }
//                }

                favoriteAdapter.data = leagueOddList
                try {/*目前流程 需要先解除再綁定 socket流程下才會回傳內容*/
                    favoriteAdapter.data.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    favorite_game_list?.firstVisibleRange(
                        favoriteAdapter, activity ?: requireActivity()
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //全部球类的时候，赛事数据为空，则隐藏筛选按钮，显示搜索
//                (gameType == null && leagueOddList.isEmpty()).let {
//                    cl_bet_all_sports.isVisible = !it
//                    lin_search.visibility = if (it) View.VISIBLE else View.INVISIBLE
//                }
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

        if (activity is FavoriteActivity) {
            (activity as FavoriteActivity).setupBetData(fastBetDataBean)
        } else if (activity is MainTabActivity) {
            (activity as MainTabActivity).setupBetData(fastBetDataBean)
        }
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key, matchOdd.matchInfo?.id
                    )
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key, matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                true -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key, matchOdd.matchInfo?.id
                    )

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key, matchOdd.matchInfo?.id
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
                favoriteAdapter.updateLeague(
                    index, leagueOdd
                )
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
        dataSport.clear()
        list.forEach {
            dataSport.add(
                Item(
                    code = it.code.orEmpty(),
                    name = it.showName.orEmpty(),
                    num = 0,
                    sortNum = 0
                )
            )
        }
        //如沒有選過的，預設選第一個
        dataSport.firstOrNull()?.let {
            it.isSelected = true
            tv_all_sports.text = it.name
        }

//        dataSport.find {
//            it.isSelected
//        }.let {
//            if (it == null) {
//                dataSport.firstOrNull()?.isSelected = true
//            }
//        }
    }
}