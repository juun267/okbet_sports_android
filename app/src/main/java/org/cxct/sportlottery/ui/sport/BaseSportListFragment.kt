package org.cxct.sportlottery.ui.sport

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.common.GameTypeAdapter2
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.ui.sport.list.adapter.EmptySportGamesView
import org.cxct.sportlottery.util.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.bindExpanedAdapter
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

abstract class BaseSportListFragment<M, VB>: BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    protected abstract var matchType: MatchType
    open fun getCurGameType() = GameType.getGameType(gameType) ?: GameType.ALL
    protected var selectMatchIdList = arrayListOf<String>()
    protected var gameType: String = GameType.BK.key
        set(value) {
            if (!Objects.equals(value, field)) { // 清除赛选条件
                selectMatchIdList = arrayListOf()
            }
            field = value
        }

    var offsetScrollListener: ((Double) -> Unit)? = null
    protected val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }
    private val loadingHolder by lazy { Gloading.wrapView(binding.gameList) }
    override fun dismissLoading() = loadingHolder.showLoadSuccess()
    override fun showLoading() = loadingHolder.showLoading()

    protected abstract fun getGameListAdapter(): ExpanableOddsAdapter<*>
    protected abstract val oddsChangeListener: ServiceBroadcastReceiver.OddsChangeListener
    protected abstract fun resubscribeChannel(delay: Long = 0)
    protected abstract fun onFavorite(favoriteMatchIds: List<String>)
    protected abstract fun onOddTypeChanged(oddsType: OddsType)
    protected abstract fun onBetInfoChanged(betInfoList: List<BetInfoListData>)


    protected fun scrollBackTop() = binding.appbarLayout.run {
        ((layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior?)?.setTopAndBottomOffset(0)
    }

    override fun onInitView(view: View){
        initToolbar()
        initSportTypeList()
        initGameListView()

    }

    override fun onBindViewStatus(view: View) {
        EventBusUtil.targetLifecycle(this)
        setupOddsChangeListener()
        _initObserver()
    }

    private fun _initObserver() {
        observerMenuData()
        viewModel.favorMatchList.observe(viewLifecycleOwner) { onFavorite(it) }
        viewModel.oddsType.observe(viewLifecycleOwner) { onOddTypeChanged(it) }
        viewModel.betInfoList.observe(viewLifecycleOwner) { onBetInfoChanged(it.peekContent()) }
        viewModel.notifyLogin.observe(viewLifecycleOwner) { (activity as MainTabActivity).showLoginNotify() }
        receiver.producerUp.observe(viewLifecycleOwner) { //開啟允許投注
            if (it == null) {
                return@observe
            }
            resubscribeChannel()
        }
    }

    protected open fun observerMenuData() {
        viewModel.sportMenuData.observe(viewLifecycleOwner) {

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
    }

    private fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
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

    fun resetFooterView(footerView: View) {

        footerView.gone()
        val adapter = getGameListAdapter()
        if (footerView.tag == adapter) {
            return
        }

        (footerView.parent as ViewGroup?)?.let { it.removeView(footerView) }
        footerView.tag = adapter
        adapter.addFooterView(footerView, 1)
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
                selectMatchIdList
            )
        }

        ivArrow.bindExpanedAdapter(getGameListAdapter()) { resubscribeChannel(320) }
    }

    private fun initSportTypeList() = binding.run {
        sportTypeList.layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        sportTypeList.edgeEffectFactory = EdgeBounceEffectHorizontalFactory()
        sportTypeList.adapter = gameTypeAdapter
    }

    protected open fun getGameLayoutManger(): LayoutManager {
        return SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun initGameListView() = binding.gameList.run {

        layoutManager = getGameLayoutManger()
        adapter = getGameListAdapter()
        getGameListAdapter().setEmptyView(EmptySportGamesView(context()))
        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    clearSubscribeChannels()
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    resubscribeChannel(20)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        resubscribeChannel(20)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            setupOddsChangeListener()
            resubscribeChannel(80)
        } else {
            clearSubscribeChannels()
        }
    }

    protected fun setupOddsChangeListener() {
        if (isAdded) {
            receiver.oddsChangeListener = oddsChangeListener
        }
    }

    protected open fun onGameTypeChanged(item: Item, position: Int) {
        //切換球種，清除日期記憶
        viewModel.tempDatePosition = 0
        //日期圖示選取狀態下，切換球種要重置UI狀態
        gameType = item.code
        clearData()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        load(item)
    }

    protected open fun load(item: Item) {
        showLoading()
        setMatchInfo(item.name, item.num.toString())
        viewModel.switchGameType(matchType, item)
    }

    protected fun setMatchInfo(name: String, num: String) {
        binding.tvSportName.text = name
        binding.tvMatchNum.text = num
    }

    private fun clearData() {
        getGameListAdapter().setNewInstance(null)
    }


    protected fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd) {
        val gameType = GameType.getGameType(gameTypeAdapter.currentItem?.code)
        gameType?.let {
            subscribeChannelHall(it.key, matchOdd?.matchInfo?.id)
            matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
        }
    }

    protected fun unSubscribeAll() {
        unSubscribeChannelHallAll()
    }

    protected val subscribedChannel = mutableListOf<Pair<String?, String?>>()
    protected val subscribeHandler = Handler(Looper.getMainLooper())

    protected fun subscribeChannel(gameType: String?, eventId: String?) {
        subscribedChannel.add(Pair(gameType, eventId))
        subscribeChannelHall(gameType, eventId)
    }
    protected open fun clearSubscribeChannels() {
        if (subscribedChannel.size > 0) {
            unSubscribeChannelHallAll()
            subscribedChannel.clear()
        }
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearData()
        offsetScrollListener = null
        clearSubscribeChannels()
        unSubscribeChannelHallSport()
    }

    open fun setSelectMatchIds(matchIdList: ArrayList<String>) {
        selectMatchIdList = matchIdList
    }

    // 赛选联赛
    @Subscribe
    fun onSelectMatch(matchIdList: ArrayList<String>) {
        setSelectMatchIds(matchIdList)
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

    protected fun addOddsDialog(
        matchInfo: MatchInfo,
        odd: Odd,
        playCateCode: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        outRightMatchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd? = null  // 冠军时必传
    ) {

        (activity as MainTabActivity).setupBetData(
            FastBetDataBean(
            matchType = matchType,
            gameType = getCurGameType(),
            playCateCode = playCateCode,
            playCateName = getString(R.string.home_tab_end_score),
            matchInfo = matchInfo,
            matchOdd = outRightMatchOdd,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap)
        )
    }

}