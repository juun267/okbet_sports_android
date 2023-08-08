package org.cxct.sportlottery.ui.sport.list

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAUtil
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.list.adapter.*
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
open class SportListFragment2<M, VB>: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>(), OnOddClickListener {

    override var matchType = MatchType.IN_PLAY

    override fun getGameListAdapter() = sportLeagueAdapter2
    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
        sportLeagueAdapter2.onOddsChangeEvent(it)
    }

    private val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            onNodeExpand = { resubscribeChannel(200) },
            onOddClick = this@SportListFragment2,
            onFavorite = { matchId ->
            loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, matchId) }
        })
    }


    // 该方法中不要引用与生命周期有关的(比如：ViewModel、Activity)
    private fun reset() {
        gameTypeAdapter.setNewInstance(null)
        binding.linOpt.gone()
        clearData()
        setMatchInfo("", "")
        clearSubscribeChannels()
        setupSportTypeList()
        setupToolbarStatus()
    }

    fun reload(matchType: MatchType, gameType: String?) {
        this.matchType = matchType
        this.gameType = gameType ?: FIBAUtil?.takeFIBAItem()?.code ?: GameType.BK.key
        sportLeagueAdapter2.matchType = this.matchType
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
        reload((arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY, arguments?.getString("gameType"))
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
//                setSportDataList(mutableListOf(testLeague))
//            } else {
                val mLeagueOddList = (oddsListData.leagueOddsFilter ?: leagueOdds).toMutableList()
                setSportDataList(mLeagueOddList as MutableList<BaseNode>)
//            }

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

    override fun onFavorite(favoriteIds: Set<String>) {
        if (sportLeagueAdapter2.getCount() < 1) {
            return
        }

        sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
            if (baseNode is org.cxct.sportlottery.network.odds.list.MatchOdd) {
                baseNode.matchInfo?.let {
                    val isFavorited = favoriteIds.contains(it.id)
                    if (it.isFavorite != isFavorited) {
                        it.isFavorite = isFavorited
                        sportLeagueAdapter2.notifyMatchItemChanged(index, SportMatchEvent.FavoriteChanged)
                    }
                }
            }
        }
    }


    private fun initSocketObserver() {

        MatchOddsRepository.observerMatchStatus(this) {
            val matchId = it?.matchStatusCO?.matchId ?: return@observerMatchStatus
            val isFinished = it.matchStatusCO?.status == GameMatchStatus.FINISH.value
            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observerMatchStatus
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
            if (sportLeagueAdapter2.getCount() < 1) {
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

    override fun oddClick(
        matchInfo: MatchInfo,
        odd: Odd,
        playCateCode: String,
        betPlayCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        view: View
    ) {
        addOddsDialog(matchInfo, odd, playCateCode,betPlayCateName, betPlayCateNameMap)
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

        if (sportLeagueAdapter2.getCount() < 1) {
            return@postDelayed
        }

        if (binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE
            || binding.gameList.isComputingLayout) {
            resubscribeChannel(40)
            return@postDelayed
        }

        sportLeagueAdapter2.recodeRangeMatchOdd().forEach { matchOdd ->
            matchOdd.matchInfo?.let {
                Log.e("[subscribe]","====>>> 訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName} (${it.gameType} ${it.id})")
                subscribeChannel(it.gameType, it.id)
            }
        }

    }, delay)

}