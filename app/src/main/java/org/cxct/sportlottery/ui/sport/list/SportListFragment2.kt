package org.cxct.sportlottery.ui.sport.list

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.SportFragment2
import org.cxct.sportlottery.ui.sport.common.*
import org.cxct.sportlottery.ui.sport.list.adapter.*
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportListFragment2: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>(), OnOddClickListener {

    override var matchType = MatchType.IN_PLAY

    private val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }
    override fun getGameListAdapter() = sportLeagueAdapter2
    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
        sportLeagueAdapter2.onOddsChangeEvent(it)
    }


    private val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            onOddClick = this@SportListFragment2,
            onFavorite = { matchId ->
            loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, matchId) }
        })
    }

    override fun onGameTypeChanged(item: Item, position: Int) {
        super.onGameTypeChanged(item, position)
        binding.ivFilter.isVisible = gameType != GameType.ALL.key
    }

    private fun reset() {
        matchType = (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
        gameType = arguments?.getString("gameType") ?: GameType.BK.key
        viewModel.gameType = gameType ?: GameType.FT.key
        viewModel.selectMatchIdList = arrayListOf()
        gameTypeAdapter.setNewInstance(null)
        clearData()
        setMatchInfo("", "")
        clearSubscribeChannels()
        setupSportTypeList()
        setupToolbarStatus()
    }

    fun reload() {
        reset()
        scrollBackTop()
        binding.appbarLayout.scrollBy(0, 0)
        viewModel.loadMatchType(matchType)
        showLoading()
    }


    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        reset()
        setupOddsChangeListener()
        EventBusUtil.targetLifecycle(this)
        initObserve()
        initSocketObserver()
    }

    override fun onInitData() {
        reload()
    }

    override fun onResume() {
        super.onResume()
        resubscribeChannel(20)
    }

    private fun setupSportTypeList() {
        val visiable = matchType != MatchType.CS //波胆不需要显示球类
        binding.sportTypeList.isVisible = visiable
        binding.sportTypeList.adapter = if (visiable) gameTypeAdapter else null
    }

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

            if (it.second.isNullOrEmpty()) {
                dismissLoading()
            }
            if (!it.first.succeeded()) {
                ToastUtil.showToast(activity, it.first.msg)
                return@observe
            }
            updateSportType(it.second)
            it?.let { (parentFragment as SportFragment2).updateSportMenuResult(it.first) }
        }

        oddsListGameHallResult.observe(this@SportListFragment2.viewLifecycleOwner) {

            val oddsListData = it.getContentIfNotHandled()?.oddsListData ?: return@observe
            dismissLoading()
            val leagueOdds: List<LeagueOdd>? = oddsListData.leagueOdds
            if (leagueOdds.isNullOrEmpty()) {
                return@observe
            }

//            val testLeague = leagueOdds.first()
//            if (testLeague.matchOdds.isNotEmpty()) {
//                val matchOdd = testLeague.matchOdds.first()
//                testLeague.matchOdds.clear()
//                testLeague.matchOdds.add(matchOdd)
//                sportLeagueAdapter2.setNewInstance(mutableListOf(testLeague))
//            } else {
                val mLeagueOddList = (oddsListData.leagueOddsFilter ?: leagueOdds).toMutableList()
                sportLeagueAdapter2.setNewInstance(mLeagueOddList as MutableList<BaseNode>)
                sportLeagueAdapter2.footerLayout?.let { footerLayout->
                    footerLayout.postDelayed({ footerLayout.getChildAt(0)?.visible() }, 200)
                }
//            }

            resubscribeChannel(80)
        }

    }

    override fun onBetInfoChanged(betInfoList: List<BetInfoListData>) {
        if (sportLeagueAdapter2.dataCount() < 1) {
            return
        }
        sportLeagueAdapter2.updateOddsSelectStatus()
    }

    override fun onOddTypeChanged(oddsType: OddsType) {
        sportLeagueAdapter2.oddsType = oddsType
    }

    override fun onFavorite(favoriteMatchIds: List<String>) {
        if (sportLeagueAdapter2.getCount() < 1) {
            return
        }

        val favoriteIds = favoriteMatchIds.toSet()
        sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
            if (baseNode is org.cxct.sportlottery.network.odds.list.MatchOdd) {
                baseNode.matchInfo?.let {
                    val isFavorited = favoriteIds.contains(it.id)
                    if (it.isFavorite != isFavorited) {
                        it.isFavorite = isFavorited
                        sportLeagueAdapter2.notifyItemChanged(index, SportMatchEvent.FavoriteChanged)
                    }
                }
            }
        }
    }


    private fun initSocketObserver() {

        receiver.matchStatusChange.observe(this@SportListFragment2.viewLifecycleOwner) {
            val matchId = it?.matchStatusCO?.matchId ?: return@observe
            val isFinished = it.matchStatusCO?.status == GameMatchStatus.FINISH.value
            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observe
            if (isFinished) {
                sportLeagueAdapter2.removeMatchOdd(matchOdd)
            } else {
                if (SocketUpdateUtil.updateMatchStatus(matchOdd.matchInfo?.gameType, matchOdd, it, context)) {
                    sportLeagueAdapter2.matchStatuChanged(matchOdd)
                }
            }
        }

        receiver.matchClock.observe(this@SportListFragment2.viewLifecycleOwner) { event->
            val matchId =  event?.matchClockCO?.matchId ?: return@observe
            if (matchId == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observe
            matchOdd.matchInfo?.let { matchInfo->
                if (SocketUpdateUtil.updateMatchClockStatus(matchInfo, event)) {
                    sportLeagueAdapter2.matchStatuChanged(matchOdd)
                }
            }
        }

        receiver.matchOddsLock.observe(this@SportListFragment2.viewLifecycleOwner) { event->
            val matchId =  event?.matchId ?: return@observe
            if (matchId == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observe
            if (SocketUpdateUtil.updateOddStatus(matchOdd, event)) {
                sportLeagueAdapter2.notifyMatchOddChanged(matchOdd)
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

        receiver.closePlayCate.observe(this@SportListFragment2.viewLifecycleOwner) { event ->
            val closeEvent = event?.peekContent() ?: return@observe
            if (gameTypeAdapter.currentItem?.code == closeEvent.gameType) {
                sportLeagueAdapter2.closePlayCate(closeEvent)
            }
        }

    }

    private fun clearData() {
        sportLeagueAdapter2.setNewInstance(null)
    }

    private fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
            binding.sportTypeList.isVisible = matchType != MatchType.CS
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


    @Subscribe
    fun onSelectMatch(matchIdList: ArrayList<String>) {
        viewModel.selectMatchIdList = matchIdList
        gameTypeAdapter.currentItem?.let {
            clearData()
            load(it)
        }
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


    override fun resubscribeChannel(delay: Long) {
        clearSubscribeChannels()
        if (!isVisible) {
            return
        }
        if (sportLeagueAdapter2.getCount() > 0) {
            firstVisibleRange(delay)
        }
    }


    private fun firstVisibleRange(delay: Long = 100) = subscribeHandler.postDelayed({

        if (binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE
            || sportLeagueAdapter2.getCount() < 1
            || binding.gameList.isComputingLayout) {
            return@postDelayed
        }

        sportLeagueAdapter2.visiableRangeMatchOdd().forEach { matchOdd ->
            matchOdd.matchInfo?.let {
                Log.e("[subscribe]","訂閱${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}")
                subscribeChannel(it.gameType, it.id)
            }
        }

    }, delay)

}