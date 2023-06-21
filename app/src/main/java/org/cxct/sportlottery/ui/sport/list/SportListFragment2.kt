package org.cxct.sportlottery.ui.sport.list

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.list.adapter.*
import org.cxct.sportlottery.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
open class SportListFragment2<M, VB>: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>(), OnOddClickListener {

    override var matchType = MatchType.IN_PLAY

    override fun getGameListAdapter() = sportLeagueAdapter2
    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
        sportLeagueAdapter2.onOddsChangeEvent(it)
    }

    protected val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            onNodeExpand = { resubscribeChannel(320) },
            onOddClick = this@SportListFragment2,
            onFavorite = { matchId ->
            loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, matchId) }
        })
    }


    // 该方法中不要引用与生命周期有关的(比如：ViewModel、Activity)
    private fun reset() {
        matchType = (arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY
        sportLeagueAdapter2.matchType = matchType
        gameType = arguments?.getString("gameType") ?: GameType.BK.key
        selectMatchIdList = arrayListOf()
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
        observeSportList()
        initSocketObserver()
    }

    override fun onInitData() {
        reset()
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
    protected open fun observeSportList() = viewModel.run {

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

        addOddsDialog(matchInfo, odd, playCateCode, betPlayCateNameMap)
    }

    override fun setSelectMatchIds(matchIdList: ArrayList<String>) {
        selectMatchIdList = matchIdList
        gameTypeAdapter.currentItem?.let {
            clearData()
            load(it)
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

        sportLeagueAdapter2.recodeRangeMatchOdd().forEach { matchOdd ->
            matchOdd.matchInfo?.let {
                Log.e("[subscribe]","訂閱${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}")
                subscribeChannel(it.gameType, it.id)
            }
        }

    }, delay)

}