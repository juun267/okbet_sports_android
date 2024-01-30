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
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseSocketFragment
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
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.*
import kotlin.reflect.KClass

/**
 * 2023/11/03 anderson
 * 1.优化sport/menu的请求，将请求代码放在了SportFragment上面
 * 2.除了首次需要先加载menu接口外，之后都是simple/list接口和menu并发执行，并更新上下各自相关的布局
 * 3.updateSportType的时候，需要判断是否需要更新列表
 */
abstract class BaseSportListFragment<M, VB>: BaseSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    override fun createVM(clazz: KClass<SportListViewModel>) = getViewModel(clazz = clazz)

    protected abstract var matchType: MatchType
    open fun getCurGameType() = GameType.getGameType(gameType) ?:GameType.ES
    open var gameType: String = GameType.BK.key
    private var categoryCodeList: List<String>? = null
    protected var currentItem:Item? = null

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

    fun currentMatchType(): MatchType = matchType
    fun currentGameType(): String = gameType

    protected fun scrollBackTop() = binding.run {
        //解决到顶部无法滑动的问题
        gameList.scrollToPosition(0)
        //拿到 appbar 的 behavior,让 appbar 滚动
        val layoutParams: ViewGroup.LayoutParams = appbarLayout.getLayoutParams()
        val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior is AppBarLayout.Behavior) {
            //拿到下方tabs的y坐标，即为我要的偏移量
            val topAndBottomOffset = behavior.topAndBottomOffset
            if (topAndBottomOffset != 0) {
                behavior.topAndBottomOffset = 0
                appbarLayout.setExpanded(true, true)
            }
        }
    }

    override fun onInitView(view: View){
        initToolbar()
        initSportTypeList()
        initGameListView()
    }

    override fun onBindViewStatus(view: View) {
        scrollBackTop()
        gameTypeAdapter.applyEventView(requireActivity())
        currentItem = null
        gameTypeAdapter.setNewInstance(null)
        EventBusUtil.targetLifecycle(this)
        setupOddsChangeListener()
        _initObserver()
    }

    private fun _initObserver() {
        observerMenuData()
        viewModel.favorMatchList.observe(viewLifecycleOwner) { onFavorite(it) }
        viewModel.oddsType.observe(viewLifecycleOwner) { onOddTypeChanged(it) }
        viewModel.betInfoList.observe(viewLifecycleOwner) { onBetInfoChanged(it.peekContent()) }
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
    }

    open fun updateSportType(gameTypeList: List<Item>) {
        if (gameTypeList.isEmpty()) {
            setSportDataList(null)
            dismissLoading()
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

        binding.sportTypeList.show()
        if (targetGameType == null) {
            targetGameType = gameTypeList.find { it.num > 0 }
        }
        if (targetGameType == null) {
            targetGameType = gameTypeList.first()
        }
        if (currentItem==null){
            gameType = targetGameType!!.code
            currentItem = targetGameType
            currentItem!!.isSelected = true
            load(currentItem!!)
        }else{
            val existItem = gameTypeList.firstOrNull { it.code == currentItem!!.code }
            currentItem = existItem?:targetGameType
            currentItem?.isSelected = true
            if (existItem!=targetGameType){
                load(currentItem!!)
            }
        }
        gameTypeAdapter.setNewInstance(gameTypeList.toMutableList())
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            gameTypeAdapter.data.indexOfFirst { it.isSelected })

    }

    fun resetFooterView(footerView: View) {
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
                categoryCodeList = categoryCodeList,
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
        setupBackTop(binding.ivBackTop, 500.dp, tabCode = matchType.postValue){
            scrollBackTop()
            return@setupBackTop true
        }
        layoutManager = getGameLayoutManger()
        adapter = getGameListAdapter().apply { setEmptyView(EmptySportGamesView(context())) }
        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    onStartScroll()
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    onScrollStopped()
                }
            }

        })
    }


    protected open fun onStartScroll() { clearSubscribeChannels() }

    protected open fun onScrollStopped() { resubscribeChannel(20) }

    override fun onResume() {
        super.onResume()
        resubscribeChannel(20)
        setupOddsChangeListener()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupOddsChangeListener()
            resubscribeChannel(80)
        } else {
            clearSubscribeChannels()
        }
    }

    private fun setupOddsChangeListener() {
        if (isAdded) {
            receiver.addOddsChangeListener(this, oddsChangeListener)
        }
    }

    open fun onGameTypeChanged(item: Item, position: Int) {
        if (binding.ivBackTop.isVisible()) {
            binding.ivBackTop.performClick()
        } else {
            scrollBackTop()
        }

        //日期圖示選取狀態下，切換球種要重置UI狀態
        gameType = item.code
        currentItem = item
        clearData()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        load(item)
    }

    protected open fun load(
        item: Item,
        selectLeagueIdList:
        ArrayList<String> = arrayListOf(),
        selectMatchIdList: ArrayList<String> = arrayListOf(),
        categoryCodeList: List<String>? = null,
    ) {
        resetArrow()
        showLoading()
        if(categoryCodeList==null){
            setMatchInfo(item.name, "")
        }else{
            //电竞主页的情况，名称要换成游戏名字
            val name = item.categoryList?.firstOrNull { it.categoryCodeList == categoryCodeList }?.name?:item.name
            setMatchInfo(name, "")
        }
        this.categoryCodeList =categoryCodeList
        viewModel.switchGameType(matchType, item, selectLeagueIdList,selectMatchIdList,categoryCodeList)
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

    private var lastDataSetRunable: Runnable? = null
    protected fun setSportDataList(list: MutableList<BaseNode>?, sizeNumber: String? = null) {
        /**
         * RecyclerView.assertNotInLayoutOrScroll
         *  IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
         */
        if (binding.gameList.isComputingLayout) {
            lastDataSetRunable?.let { binding.gameList.removeCallbacks(it) }
            binding.gameList.post { setSportDataList(list, sizeNumber) }
            return
        }

        val adapter = getGameListAdapter()
        adapter.setNewInstance(list)
        if (sizeNumber == null) setMatchNum((list?.sumOf { it.childNode?.size ?: 0 })?.toString() ?: "") else setMatchNum(sizeNumber)
        if (!list.isNullOrEmpty()) {
            resubscribeChannel(120)
            binding.linOpt.visible()
        }
        val footerLayout = adapter.footerLayout?.getChildAt(0) as SportFooterGamesView? ?: return
//        footerLayout.visible()
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

    protected val subscribedChannel = mutableMapOf<String, Pair<String?, String?>>()
    protected val subscribeHandler = Handler(Looper.getMainLooper())

    protected fun generateChannelKey(gameType: String?, eventId: String?) = "$gameType---$eventId"
    protected fun subscribeChannel(gameType: String?, eventId: String?) : String? {
        val key = generateChannelKey(gameType, eventId)
        if (subscribedChannel.containsKey(key)) {
            return null
        }
        subscribedChannel[key] = Pair(gameType, eventId)
        subscribeChannelHall(gameType, eventId)
        return key
    }

    protected fun unSubscribeChannel(gameType: String?, eventId: String?) {
        subscribedChannel.remove(generateChannelKey(gameType, eventId)) ?: return
        unSubscribeChannelHall(gameType, eventId)
    }

    protected open fun clearSubscribeChannels() {
        unSubscribeAllChannel()
        subscribeHandler.removeCallbacksAndMessages(null)
    }

    private fun unSubscribeAllChannel() {
        unSubscribeChannelHallAll()
        getGameListAdapter().resetRangeMatchOdd()
        if (subscribedChannel.isNotEmpty()) {
            subscribedChannel.forEach { unSubscribeChannelHall(it.value.first, it.value.second) }
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
        betPlayCateName: String? = null,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        outRightMatchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd? = null,  // 冠军时必传
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
            betPlayCateNameMap = betPlayCateNameMap,
            categoryCode = matchInfo.categoryCode
            )
        )
    }
    /**
     * 通过父fragment来加载sport/menu 接口数据
     */
    protected fun getMenuDataByParent(isESportType: Boolean=false ){
        SportRepository._sportMenuResultEvent.value?.let { viewModel.loadSportMenu(it, matchType, isESportType = isESportType) }
    }


}