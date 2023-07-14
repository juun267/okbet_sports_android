package org.cxct.sportlottery.ui.sport

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.SelectMatchEvent
import org.cxct.sportlottery.common.extentions.getPlayCateName
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
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
import org.cxct.sportlottery.ui.sport.list.adapter.SportFooterGamesView
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import java.util.*

abstract class BaseSportListFragment<M, VB>: BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    protected abstract var matchType: MatchType
    open fun getCurGameType() = GameType.getGameType(gameType) ?: GameType.ALL
    protected var gameType: String = GameType.BK.key

    protected val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }
    private val loadingHolder by lazy { Gloading.wrapView(binding.gameList) }

    override fun dismissLoading() = loadingHolder.showLoadSuccess()
    override fun showLoading() = loadingHolder.showLoading()

    protected abstract fun getGameListAdapter(): ExpanableOddsAdapter<*>
    protected abstract val oddsChangeListener: ServiceBroadcastReceiver.OddsChangeListener
    protected abstract fun resubscribeChannel(delay: Long = 0)
    protected abstract fun onFavorite(favoriteMatchIds: Set<String>)
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
        viewModel.sportTypeMenuData.observe(viewLifecycleOwner) {
            if (!it.first.isNullOrEmpty()) {
                updateSportType(it.first)
                return@observe
            }

            setSportDataList(null)
            dismissLoading()
            if (!it.second) {
                ToastUtil.showToast(activity, it.third)
                return@observe
            }
        }

        viewModel.sportMenuApiResult.observe(viewLifecycleOwner) {
            (parentFragment as SportFragment2?)?.updateSportMenuResult(it)
        }
    }

    protected fun updateSportType(gameTypeList: List<Item>) {

        if (gameTypeList.isEmpty()) {
            setSportDataList(null)
            return
        }
        //处理默认不选中的情况

        var targetGameType: Item? = null
        gameTypeList.forEach {
            it.isSelected = false
            if (it.code == gameType) {
                targetGameType = it
            }
        }
        if (targetGameType == null) {
            targetGameType = gameTypeList.find { it.num > 0}
        }
        if (targetGameType == null) {
            targetGameType = gameTypeList.first()
        }

        gameType = targetGameType!!.code
        targetGameType!!.isSelected = true
        load(targetGameType!!)

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
        adapter.addFooterView(footerView)
    }

    protected fun resetArrow() {
        if (binding.ivArrow.isSelected) {
            binding.ivArrow.isSelected= false
            binding.ivArrow.rotationAnimation(0f)
        }
    }

    private fun initToolbar()  = binding.run {
        ivArrow.bindExpanedAdapter(getGameListAdapter()) { resubscribeChannel(320) }
        ivFilter.setOnClickListener {
            if (TextUtils.isEmpty(gameType)) {
                return@setOnClickListener
            }
            LeagueSelectActivity.start(
                requireContext(),
                gameType,
                matchType,
            )
        }


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

        setupBackTop(binding.ivBackTop, 500.dp, tabCode = matchType.postValue)
        layoutManager = getGameLayoutManger()
        adapter = getGameListAdapter().apply { setEmptyView(EmptySportGamesView(context())) }
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
        setupOddsChangeListener()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            setupOddsChangeListener()
            resubscribeChannel(80)
        } else {
            clearSubscribeChannels()
        }
    }

    private fun setupOddsChangeListener() {
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

    protected open fun load(item: Item, selectLeagueIdList: ArrayList<String> = arrayListOf(),selectMatchIdList: ArrayList<String> = arrayListOf()) {
        resetArrow()
        showLoading()
        setMatchInfo(item.name, "")
        viewModel.switchGameType(matchType, item, selectLeagueIdList,selectMatchIdList)
    }

    protected fun setMatchInfo(name: String, num: String) {
        binding.tvSportName.text = name
        binding.tvMatchNum.text = num
    }

    private fun setMatchNum(num: String) {
        binding.tvMatchNum.text = num
    }

    protected fun clearData() {
        unSubscribeAllChannel()
        setSportDataList(null)
    }

    protected fun setSportDataList(list: MutableList<BaseNode>?, sizeNumber: String? = null) {
        val adapter = getGameListAdapter()
        adapter.setNewInstance(list)
        if (sizeNumber == null) setMatchNum((list?.sumOf { it.childNode?.size ?: 0 })?.toString() ?: "") else setMatchNum(sizeNumber)
        if (!list.isNullOrEmpty()) {
            resubscribeChannel(120)
            binding.linOpt.visible()
        }
        val footerLayout = adapter.footerLayout?.getChildAt(0) as SportFooterGamesView? ?: return
        footerLayout.visible()
        footerLayout.sportNoMoreEnable(!list.isNullOrEmpty())
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
        unSubscribeAllChannel()
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    private fun unSubscribeAllChannel() {
        unSubscribeChannelHallAll()
        getGameListAdapter().resetRangeMatchOdd()
        if (subscribedChannel.size > 0) {
            subscribedChannel.forEach { unSubscribeChannelHall(it.first, it.second) }
            subscribedChannel.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearData()
    }

    open fun setSelectMatch(leagueIdList: ArrayList<String>,matchIdList: ArrayList<String>) {
        gameTypeAdapter.currentItem?.let {
            clearData()
            load(it, leagueIdList,matchIdList)
        }
    }

    // 赛选联赛
    @Subscribe
    fun onSelectMatch(selectMatchEvent: SelectMatchEvent) {
        setSelectMatch(selectMatchEvent.leagueIds,selectMatchEvent.matchIds)
    }

    protected fun addOddsDialog(
        matchInfo: MatchInfo,
        odd: Odd,
        playCateCode: String,
        betPlayCateName:String? = null,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        outRightMatchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd? = null  // 冠军时必传
    ) {
       val playCateName=when(matchType){
           MatchType.END_SCORE-> getString(R.string.home_tab_end_score)
           MatchType.OUTRIGHT,MatchType.OTHER_OUTRIGHT-> (odd.parentNode as CategoryOdds).name
           else->betPlayCateName?:betPlayCateNameMap?.get(playCateCode).getPlayCateName(requireContext())
       }
        (activity as MainTabActivity).setupBetData(
            FastBetDataBean(
            matchType = matchType,
            gameType = getCurGameType(),
            playCateCode = playCateCode,
            playCateName = playCateName,
            matchInfo = matchInfo,
            matchOdd = outRightMatchOdd,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = betPlayCateNameMap)
        )
    }

}