package org.cxct.sportlottery.ui.sport.outright

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.showLoading
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.common.*
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.ui.sport.list.adapter.EmptySportGamesView
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportOutrightFragment: BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    private val matchType = MatchType.OUTRIGHT
    private var gameType: String = GameType.BK.key
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾

    private val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }

    private fun onGameTypeChanged(item: Item, position: Int) {
        if (!item.isSelected) {
            //切換球種，清除日期記憶
            viewModel.tempDatePosition = 0
        }
        gameType = item.code

        viewModel.cleanGameHallResult()
        //切換球種後要重置位置
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            position)
        unSubscribeAll()
        load(item)
        binding.ivArrow.isSelected = true
    }

    private fun load(item: Item) {
        showLoading()
        setMatchInfo(item.name, item.num.toString())
        viewModel.switchGameType(matchType, item, Any())
    }

    private inline fun setMatchInfo(name: String, num: String) {
        binding.tvSportName.text = name
        binding.tvMatchNum.text = num
    }


    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            sportOutrightAdapter2.onMatchOdds(subscribedMatchOdd, oddsChangeEvent)
        }
    }

    private fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val sportOutrightAdapter2: SportOutrightAdapter2 by lazy {

        SportOutrightAdapter2(this@SportOutrightFragment) { _, _, item ->
            if (item is Odd) {  // 赔率
                addOutRightOddsDialog((item.parentNode as CategoryOdds).matchOdd, item, item.outrightCateKey ?: "")
            } else { // 展开或收起
                setOutrightLeagueAdapter(200)
            }
        }
    }

    override fun onInitView(view: View) {
        setupSportTypeList()
        setupToolbar()
        setupGameListView()
    }

    override fun onBindViewStatus(view: View) {
        EventBusUtil.targetLifecycle(this)
        arguments?.getString("gameType")?.let { gameType = it }
        gameType?.let { viewModel.gameType = it  }
        initObserve()
        initSocketObserver()
        viewModel.cleanGameHallResult()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            unSubscribeAll()
        } else {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            setupOddsChangeListener()
            setOutrightLeagueAdapter(0)
        }
    }

    private val loadingHolder by lazy {
        Gloading.wrapView(binding.gameList)
    }

    override fun showLoading() {
        if (!loadingHolder.isLoading) {
            loadingHolder.showLoading()
        }
    }

    override fun dismissLoading() {
        loadingHolder.showLoadSuccess()
    }

    fun resetFooterView(footerView: View) {
        if (footerView.tag == sportOutrightAdapter2) {
            return
        }
        (footerView.parent as ViewGroup?)?.let { it.removeView(footerView) }
        footerView.tag = sportOutrightAdapter2
        sportOutrightAdapter2.addFooterView(footerView)
    }

    private fun setupSportTypeList() {
        binding.sportTypeList.apply {
            layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
            adapter = gameTypeAdapter
        }
        binding.gameList.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    setOutrightLeagueAdapter(0)
                } else {
                    unSubscribeAll()
                }
            }
        })
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbar() {

        binding.appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke((-verticalOffset) / Math.max(1.0, binding.appbarLayout.measuredHeight.toDouble()))
        })

        //冠军不需要筛选
        binding.ivFilter.gone()
        binding.ivArrow.bindExpanedAdapter(sportOutrightAdapter2) { setOutrightLeagueAdapter(120) }
    }
    private fun setupGameListView() = binding.gameList.run {
        layoutManager = SocketGridManager(context, 2)
        adapter = sportOutrightAdapter2
        sportOutrightAdapter2.setEmptyView(EmptySportGamesView(context()))
    }

    /**
     * 設置冠軍adapter, 訂閱當前頁面上的資料
     */
    private fun setOutrightLeagueAdapter(delay: Long = 0) {
        binding.gameList.removeCallbacks(subscribeVisibleRange)
        unSubscribeAll()
        binding.gameList.postDelayed(subscribeVisibleRange, delay)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (activity == null
                || sportOutrightAdapter2.getCount() < 1
                || binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                return@Runnable
            }

            val matchOdds = mutableSetOf<org.cxct.sportlottery.network.outright.odds.MatchOdd>()
            sportOutrightAdapter2.doOnVisiableRange { _, item ->
                if(item is Odd) {
                    matchOdds.add((item.parentNode as CategoryOdds).matchOdd)
                }
            }
            matchOdds.forEach { subscribeChannelHall(it) }
        }
    }

    private fun initObserve() = viewModel.run {
        notifyLogin.observe(viewLifecycleOwner) {
            (activity as MainTabActivity).showLoginNotify()
        }

        showErrorDialogMsg.observe(viewLifecycleOwner) {
            if (requireContext() == null || TextUtils.isEmpty(it)) {
                return@observe
            }

            showErrorMsgDialog(it)
        }

        sportMenuResult.observe(viewLifecycleOwner) {
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

                else -> {
                }
            }
        }
        outrightList.observe(viewLifecycleOwner) {
            if (gameType != it.tag) {
                return@observe
            }
            val data = it?.getContentIfNotHandled()?.outrightOddsListData?.leagueOdds ?: return@observe
            val list = mutableListOf<MatchOdd>()
            data.forEach { it.matchOdds?.let { list.addAll(it) } }

            if (list.isEmpty()) {
                dismissLoading()
                return@observe
            }
            sportOutrightAdapter2.setNewInstance(list as MutableList<BaseNode>)
            setOutrightLeagueAdapter(120)
            dismissLoading()
        }

        //當前玩法無賽事
        isNoEvents.distinctUntilChanged().observe(viewLifecycleOwner) {
            binding.sportTypeList.isVisible = !it
            hideLoading()
        }

        betInfoList.observe(viewLifecycleOwner) {
            it.peekContent().let {
                sportOutrightAdapter2.updateOddsSelectedStatus(it)
            }
        }

        oddsType.observe(viewLifecycleOwner) { sportOutrightAdapter2.oddsType = it }

    }

    private fun initSocketObserver() = receiver.run {
        serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.switchMatchType(matchType = matchType)
                    subscribeSportChannelHall()
                }
            }
        }

        producerUp.observe(viewLifecycleOwner) {
            it?.let {
                unSubscribeAll()
            }
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        if (gameTypeList.isEmpty()) {
            binding.sportTypeList.isVisible = true
            dismissLoading()
            return
        }

        //处理默认不选中的情况
        if (gameType.isNullOrEmpty()) {
            gameTypeList.find { it.num > 0 }?.let {
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

        gameTypeAdapter.setNewInstance(gameTypeList.toMutableList())
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            gameTypeList.indexOfFirst { it.isSelected })
        binding.sportTypeList.post {
            //球種如果選過，下次回來也需要滑動置中
            if (gameTypeList.isEmpty()) {
                binding.sportTypeList.gone()

            } else {
                binding.sportTypeList.visible()
            }
        }
    }

    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String) {

        GameType.getGameType(gameTypeAdapter.currentItem?.code)?.let {

            (activity as MainTabActivity).setupBetData(FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName = (odd.parentNode as CategoryOdds).name,
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null))
        }

    }

    private val subscribedMatchOdd = mutableMapOf<String, org.cxct.sportlottery.network.outright.odds.MatchOdd>()

    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd) {
        val gameType = GameType.getGameType(gameTypeAdapter.currentItem?.code)
        gameType?.let {
            subscribedMatchOdd["${matchOdd.matchInfo?.id}"] = matchOdd
            subscribeChannelHall(it.key, matchOdd?.matchInfo?.id)
            matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
        }
    }

    private fun unSubscribeAll() {
        subscribedMatchOdd.clear()
        unSubscribeChannelHallAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offsetScrollListener = null
        binding.sportTypeList.removeCallbacks(subscribeVisibleRange)
        unSubscribeAll()
        unSubscribeChannelHallSport()
    }

    // 赛选联赛
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
}