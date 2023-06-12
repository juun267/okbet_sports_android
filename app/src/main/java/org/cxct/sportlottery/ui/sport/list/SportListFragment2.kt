package org.cxct.sportlottery.ui.sport.list

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.clean
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.SportFragment2
import org.cxct.sportlottery.ui.sport.common.*
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.ui.sport.list.adapter.OnOddClickListener
import org.cxct.sportlottery.ui.sport.list.adapter.SportLeagueAdapter2
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportListFragment2
    : BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>(), OnOddClickListener {

    private var matchType = MatchType.IN_PLAY
    private var gameType: String? = null
        set(value) {
            if (!Objects.equals(value, field)) { // 清除赛选条件
                viewModel.selectMatchIdList = arrayListOf()
            }
            field = value
        }

    private val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }

    private val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            onOddClick = this@SportListFragment2,
            onFavorite = { matchId ->
            loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, matchId) }
        })
    }

    private fun onGameTypeChanged(item: Item, position: Int) {

        //切換球種，清除日期記憶
        viewModel.tempDatePosition = 0
        //日期圖示選取狀態下，切換球種要重置UI狀態
        gameType = item.code
        sportLeagueAdapter2.setNewInstance(null)
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        load(item)
        binding.ivFilter.isVisible = gameType != GameType.ALL.key
    }

    private fun reset() {
        matchType = (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
        gameType = arguments?.getString("gameType")
        viewModel.gameType = gameType ?: GameType.FT.key
        viewModel.sportMenuResult.clean()
        viewModel.selectMatchIdList = arrayListOf()
        gameTypeAdapter.setNewInstance(null)
        sportLeagueAdapter2.setNewInstance(null)
        setMatchInfo("", "")
        clearSubscribeChannels()
        setupSportTypeList()
        setupToolbarStatus()
    }

    fun reload() {
        reset()
        viewModel.loadMatchType(matchType)
    }

    private inline fun setMatchInfo(name: String, num: String) {
        binding.tvSportName.text = name
        binding.tvMatchNum.text = num
    }

    override fun onInitView(view: View){
        initToolbar()
        initSportTypeList()
        initGameListView()
    }

    override fun onBindViewStatus(view: View) {

        reset()
        setupOddsChangeListener()
        EventBusUtil.targetLifecycle(this)
        initObserve()
        initSocketObserver()
    }

    override fun onInitData() {
        reload()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            setupOddsChangeListener()
            resubscribeChannel(if (needDalay()) 80 else 0)
        } else {
            clearSubscribeChannels()
        }
    }

    override fun onResume() {
        super.onResume()
        resubscribeChannel(if (needDalay()) 80 else 0)
    }

    private fun initToolbar()  = binding.run {

        appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke((-verticalOffset) / Math.max(1.0, appbarLayout.measuredHeight.toDouble()))
        })

        ivFilter.setOnClickListener {
            if (TextUtils.isEmpty(gameType)) {
                return@setOnClickListener
            }
            LeagueSelectActivity.start(
                requireContext(),
                gameType!!,
                matchType,
                viewModel.selectTimeRangeParams,
                viewModel.selectMatchIdList
            )
        }

        ivArrow.bindExpanedAdapter(sportLeagueAdapter2)
    }

    private fun initSportTypeList() = binding.run {
        sportTypeList.layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sportTypeList.edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
    }

    private fun initGameListView() = binding.gameList.run {
        layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        sportLeagueAdapter2.setEmptyView(R.layout.view_list_loading)
        adapter = sportLeagueAdapter2
        addOnScrollListener(object : OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    clearSubscribeChannels()
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    resubscribeChannel()
                }
            }
        })
    }


    private fun setupSportTypeList() {
        val visiable = matchType != MatchType.CS //波胆不需要显示球类
        binding.sportTypeList.isVisible = visiable
        binding.sportTypeList.adapter = if (visiable) gameTypeAdapter else null
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbarStatus() = binding.run {
        ivArrow.isSelected = false
        ivArrow.rotationAnimation(0f, 0)
    }

    private inline fun BaseNode.isMatchOdd() =  this is org.cxct.sportlottery.network.odds.list.MatchOdd
    private fun initObserve() = viewModel.run {

        showErrorDialogMsg.observe(this@SportListFragment2.viewLifecycleOwner) {
            if (it == null || it.isBlank() || requireContext() == null) {
                return@observe
            }

            showErrorMsgDialog(it)
        }

        sportMenuData.observe(this@SportListFragment2.viewLifecycleOwner) {
            if (!it.first.succeeded()) {
                ToastUtil.showToast(activity, it.first.msg)
                return@observe
            }
            updateSportType(it.second)
            it?.let { (parentFragment as SportFragment2).updateSportMenuResult(it.first) }
        }

        oddsListGameHallResult.observe(this@SportListFragment2.viewLifecycleOwner) {

            val oddsListData = it.getContentIfNotHandled()?.oddsListData ?: return@observe
            val leagueOdds: List<LeagueOdd>? = oddsListData.leagueOdds
            if (leagueOdds.isNullOrEmpty()) {
                return@observe
            }
            val mLeagueOddList = (oddsListData.leagueOddsFilter ?: leagueOdds).toMutableList()
            sportLeagueAdapter2.setNewInstance(mLeagueOddList as MutableList<BaseNode> )
            resubscribeChannel(80)
        }

        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this@SportListFragment2.viewLifecycleOwner) {

            hideLoading()
        }

        viewModel.betInfoList.observe(this@SportListFragment2.viewLifecycleOwner) {
            if (subscribedMatchOdd.isEmpty()) {
                return@observe
            }
            sportLeagueAdapter2.updateOddsSelectStatus(subscribedMatchOdd.values)
        }

        viewModel.oddsType.observe(this@SportListFragment2.viewLifecycleOwner) {
            sportLeagueAdapter2.oddsType = it
        }

        viewModel.favorMatchList.observe(this@SportListFragment2.viewLifecycleOwner) { favoritList->
            if (sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            val favoriteIds = favoritList.toSet()
            sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
                if (baseNode is org.cxct.sportlottery.network.odds.list.MatchOdd) {
                    baseNode.matchInfo?.let {
                        it.isFavorite = favoriteIds.contains(it.id)
                        sportLeagueAdapter2.notifyItemChanged(index)
                    }
                }
            }
        }

    }


    private fun initSocketObserver() {

//        receiver.serviceConnectStatus.observe(this@SportListFragment2.viewLifecycleOwner) {
//            if (it == null) {
//                return@observe
//            }
//
//            if (it == ServiceConnectStatus.CONNECTED) {
//                viewModel.switchMatchType(matchType = matchType)
//                subscribeSportChannelHall()
//            }
//        }

        receiver.matchStatusChange.observe(this@SportListFragment2.viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }

            val matchStatusChangeEvent = it!!
            val isFinished = matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value
            val matchId = matchStatusChangeEvent.matchStatusCO?.matchId
            val matchOddPosition = subscribedMatchOdd[matchId] ?: return@observe
            if (isFinished) {
                sportLeagueAdapter2.removeMatchOdd(matchOddPosition.first)
            } else {
                if (SocketUpdateUtil.updateMatchStatus(matchOddPosition.first.matchInfo?.gameType,
                        matchOddPosition.first, matchStatusChangeEvent, context)) {
                    sportLeagueAdapter2.notifyItemChanged(matchOddPosition.second)
                }
            }

        }

        receiver.matchClock.observe(this@SportListFragment2.viewLifecycleOwner) { event->
            if (event == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            val matchPosition = subscribedMatchOdd[event.matchClockCO?.matchId] ?: return@observe
            matchPosition.first.matchInfo?.let { matchInfo->
                if (SocketUpdateUtil.updateMatchClockStatus(matchInfo, event)) {
                    sportLeagueAdapter2.notifyItemChanged(matchPosition.second, matchPosition.first)
                }
            }
        }

        receiver.matchOddsLock.observe(this@SportListFragment2.viewLifecycleOwner) { event->
            if (event == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            val matchPosition = subscribedMatchOdd[event.matchId] ?: return@observe
            if (SocketUpdateUtil.updateOddStatus(matchPosition.first, event)) {
                sportLeagueAdapter2.notifyItemChanged(matchPosition.second, matchPosition.first)
            }
        }

        receiver.globalStop.observe(this@SportListFragment2.viewLifecycleOwner) { event->
            if (event == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
                if (baseNode.isMatchOdd() && SocketUpdateUtil.updateOddStatus(baseNode as MatchOdd, event)) {
                    //暫時不處理 防止過多更新
                    sportLeagueAdapter2.notifyItemChanged(index, baseNode)
                }
            }
        }

        receiver.producerUp.observe(this@SportListFragment2.viewLifecycleOwner) { //開啟允許投注
            if (it == null) {
                return@observe
            }
            resubscribeChannel()
        }


        receiver.closePlayCate.observe(this@SportListFragment2.viewLifecycleOwner) { event ->
            val closeEvent = event?.peekContent() ?: return@observe
            if (sportLeagueAdapter2.getCount() < 1
                || gameTypeAdapter.currentItem?.code != closeEvent.gameType
                || sportLeagueAdapter2.rootNodes.isNullOrEmpty()
            ) {
                return@observe
            }

            (sportLeagueAdapter2.rootNodes!!.toMutableList() as MutableList<LeagueOdd>).closePlayCate(closeEvent)
            sportLeagueAdapter2.notifyDataSetChanged()
        }
    }

    private fun setupOddsChangeListener() {
        if (isAdded) {
            receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
                sportLeagueAdapter2.onOddsChangeEvent(it, subscribedMatchOdd)
            }
        }
    }

    private fun load(item: Item) {
        setMatchInfo(item.name, item.num.toString())
        viewModel.switchGameType(matchType, item, Any())
    }

    private fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
            binding.sportTypeList.isVisible = matchType != MatchType.CS
            hideLoading()
            return
        }
        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            (gameTypeList.find { it.num > 0 } ?: gameTypeList.first()).let {
                it.isSelected = true
                gameType = it.code
                load(it)
            }
        } else {
            (gameTypeList.find { it.code == gameType } ?: gameTypeList.first()).let {
                gameType = it.code
                if (!it.isSelected) {
                    it.isSelected = true
                    load(it)
                }
            }
        }
        //全部球类tab不支持联赛筛选
        binding.ivFilter.isVisible = gameType != GameType.ALL.key
        gameTypeAdapter.setNewInstance(gameTypeList.toMutableList())
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            gameTypeAdapter.data.indexOfFirst { it.isSelected })

    }

    override fun oddClick(
        matchInfo: MatchInfo,
        odd: Odd,
        playCateCode: String,
        betPlayCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        view: View
    ) {

        var gameType = GameType.getGameType(gameTypeAdapter.currentItem?.code)
        if (gameType == null || matchInfo == null) {
            return
        }
        if (gameType == GameType.ALL) {
            gameType = GameType.getGameType(matchInfo.gameType)
        }
        val fastBetDataBean = FastBetDataBean(
            matchType = matchType,
            gameType = gameType!!,
            playCateCode = playCateCode,
            playCateName = betPlayCateName,
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap,
        )
        (activity as MainTabActivity).setupBetData(fastBetDataBean, view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sportLeagueAdapter2.setNewInstance(null)
        offsetScrollListener = null
        clearSubscribeChannels()
        unSubscribeChannelHallSport()
    }

    @Subscribe
    fun onSelectMatch(matchIdList: ArrayList<String>) {
        viewModel.selectMatchIdList = matchIdList
    }

    @Subscribe
    fun onSelectDate(timeRangeEvent: TimeRangeEvent) {
        viewModel.selectTimeRangeParams = object : TimeRangeParams {
            override val startTime: String
                get() = timeRangeEvent.startTime
            override val endTime: String
                get() = timeRangeEvent.endTime
        }
    }

    open fun getCurGameType(): GameType {
        return GameType.getGameType(gameType) ?: GameType.ALL
    }

    private val subscribedChannel = mutableListOf<Pair<String?, String?>>()
    private val subscribeHandler = Handler(Looper.getMainLooper())

    private fun subscribeChannel(gameType: String?, eventId: String?) {
        subscribedChannel.add(Pair(gameType, eventId))
        subscribeChannelHall(gameType, eventId)
    }

    private fun clearSubscribeChannels() {
        subscribedMatchOdd.clear()
        if (subscribedChannel.size > 0) {
            unSubscribeChannelHallAll()
            subscribedChannel.clear()
        }
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    private fun needDalay(): Boolean {
        if (sportLeagueAdapter2.getCount() < 1) {
            return false
        }

        val view = binding.gameList.layoutManager?.findViewByPosition(0) ?: return false
        val viewHolder = binding.gameList.getChildViewHolder(view)
        return (viewHolder !is SportLeagueAdapter.ItemViewHolder)
    }

    private fun resubscribeChannel(delay: Long = 0) {
        clearSubscribeChannels()
        if (!isVisible) {
            return
        }
        val adapter = binding.gameList.adapter as SportLeagueAdapter2
        if (adapter.getCount() > 0) {
            firstVisibleRange(delay)
        }
    }

    private val subscribedMatchOdd = mutableMapOf<String, Pair<org.cxct.sportlottery.network.odds.list.MatchOdd, Int>>()
    private fun firstVisibleRange(delay: Long = 100) = subscribeHandler.postDelayed({

        if (binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE
            || sportLeagueAdapter2.getCount() < 1
            || binding.gameList.isComputingLayout) {
            return@postDelayed
        }

        sportLeagueAdapter2.doOnVisiableRange { i, baseNode ->
            if (baseNode is org.cxct.sportlottery.network.odds.list.MatchOdd) {
                baseNode.matchInfo?.let {
                    subscribedMatchOdd[it.id] = Pair(baseNode, i)
                    subscribeChannel(it.gameType, it.id)
                }
            }
        }
    }, delay)

}