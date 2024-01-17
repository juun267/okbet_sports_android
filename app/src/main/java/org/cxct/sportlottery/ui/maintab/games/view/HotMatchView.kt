package org.cxct.sportlottery.ui.maintab.games.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.doOnStop
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHotGameBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.*
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.HotMatchAdapter
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HotMatchView(
    context: Context, attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    val binding = ViewHotGameBinding.inflate(layoutInflater, this, true)
    private var adapter: HotMatchAdapter? = null
    private var fragment: BaseFragment<*>? = null

    init {
        initView()
    }

    private fun initView() {
        val manager = LinearLayoutManager(context)
        initRecyclerView(manager)
        //右滑动
        iv_right.onClick {
            scrollRecycler(manager, true)
        }
        //左滑动
        iv_left.onClick {
            scrollRecycler(manager, false)
        }

        ivBackPage.onClick {
            scrollRecycler(manager, false)
        }
        ivForwardPage.onClick {
            scrollRecycler(manager, true)
        }

        //查看更多
        tvHotMore.onClick {
            (fragment?.activity as MainTabActivity).jumpToInplaySport()
        }
//        ivHotMore.onClick {
//            EventBusUtil.post(JumpInPlayEvent())
//        }

    }

    private fun initRecyclerView(manager: LinearLayoutManager) {
        manager.orientation = LinearLayoutManager.HORIZONTAL
        recycler_hot_game.layoutManager = manager
        recycler_hot_game.addItemDecoration(
            DividerItemDecorator(
                ContextCompat.getDrawable(
                    context, R.drawable.divider_trans
                )
            )
        )
        recycler_hot_game.itemAnimator?.changeDuration = 0
        PagerSnapHelper().attachToRecyclerView(recycler_hot_game)

        //滚动监听   显示/隐藏 左右两个滑动按钮
        recycler_hot_game.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = manager.findFirstCompletelyVisibleItemPosition()
                //屏幕中没有完整的item 前后都能滑动
                if (position == -1) {
                    iv_left.visible()
                    iv_right.visible()
                    ivBackPage.alpha = 1.0f
                    ivForwardPage.alpha = 1.0f
                } else {
                    //检测是否需要隐藏 前进/后退 imageView
                    scrollImageStatus(position)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState) { // 开始滑动
                    fragment?.let {
                        unSubscribeChannelHall(it)
                    }
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState) { // 滑动停止
                    fragment?.let {
                        firstVisibleRange()
                    }
                }
            }
        })
    }


    /**
     * 初始化热门赛事控件
     */
    fun onCreate(data: LiveData<Event<List<Recommend>>>,oddsTypeLiveData: LiveData<OddsType>, fragment: BaseFragment<*>?) {
        if (fragment == null) {
            return
        }
        this.fragment = fragment
        //初始化api变量监听
        initDataObserve(data, oddsTypeLiveData, fragment)
        //初始化adapter
        initAdapter(fragment)

        //初始化ws广播监听
        if (fragment is BaseSocketFragment) {
            initSocketObservers(fragment.receiver, fragment.getViewLifecycleOwner(), fragment)
        }
        //初始化ws广播监听
        if (fragment is org.cxct.sportlottery.ui.base.BindingSocketFragment<*, *>) {
            initSocketObservers(fragment.receiver, fragment.getViewLifecycleOwner(), fragment)
        }

    }

    /**
     * 数据变量监听
     */
    private fun initDataObserve(data: LiveData<Event<List<Recommend>>>, oddsTypeLiveData: LiveData<OddsType>,fragment: BaseFragment<*>) {
        data.observe(fragment.viewLifecycleOwner) {

            //api获取热门赛事列表
            it.peekContent().let { data ->
                    //如果没数据
                    if(data.isEmpty()){
                        //隐藏
                        gone()
                    }else{
                        visible()
                    }
                    //如果体育服务关闭
                    this.goneWithSportSwitch()

                    unSubscribeChannelHall(fragment)
                if (isVisible) {
                    adapter?.data = data
                    recycler_hot_game.post { firstVisibleRange() }
                }
            }
        }
        oddsTypeLiveData.observe(fragment.viewLifecycleOwner){
            adapter?.oddsType = it
        }
    }

    /**
     * ws订阅监听
     */
    private fun initSocketObservers(
        receiver: ServiceBroadcastReceiver,
        viewLifecycleOwner: LifecycleOwner,
        fragment: BaseFragment<*>
    ) {

        //观察比赛状态改变
        MatchOddsRepository.observerMatchStatus(viewLifecycleOwner) { matchStatusChangeEvent ->
            if (matchStatusChangeEvent == null) {
                return@observerMatchStatus
            }

            if (adapter == null || adapter!!.data.isEmpty()) {
                return@observerMatchStatus
            }
            val adapterData = adapter?.data
            adapterData?.forEachIndexed { index, recommend ->

                //丢进去判断是否要更新
                if (SocketUpdateUtil.updateMatchStatus(
                        recommend.matchInfo?.gameType, recommend, matchStatusChangeEvent, context
                    )
                ) {
                    adapter?.notifyItemChanged(index, recommend)
                }
            }
        }


        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = adapter?.data
                targetList?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateMatchClock(
                            recommend, matchClockEvent
                        )
                    ) {
                        adapter?.notifyItemChanged(index, recommend)
                    }
                }
            }
        }

        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = adapter?.data
                targetList?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateMatchClock(
                            recommend, matchClockEvent
                        )
                    ) {
                        adapter?.notifyItemChanged(index, recommend)
                    }
                }

            }
        }
        receiver.addOddsChangeListener(viewLifecycleOwner, mOddsChangeListener)

        receiver.matchOddsLock.collectWith(fragment.lifecycleScope) {
            it?.let { matchOddsLockEvent ->
                val targetList = adapter?.data

                targetList?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)) {
                        adapter?.notifyItemChanged(index, recommend)
                    }
                }

            }
        }


        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                adapter?.data?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend, globalStopEvent
                        )
                    ) {
                        adapter?.notifyItemChanged(index, recommend)
                    }
                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHall(fragment)
                firstVisibleRange()
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            val it = event?.getContentIfNotHandled() ?: return@observe
            adapter?.data?.forEachIndexed { index, recommend ->
                if (recommend.gameType == it.gameType) {
                    recommend.oddsMap?.forEach { map ->
                        if (map.key == it.playCateCode) {
                            map.value?.forEach { odd ->
                                odd.status = BetStatus.DEACTIVATED.code
                            }
                            adapter?.notifyItemChanged(index, recommend)
                        }
                    }
                }
            }

        }
    }


    private fun initAdapter(fragment: BaseFragment<*>) {
        setUpAdapter(fragment,
            HomeRecommendListener(onItemClickListener = { matchInfo ->
                matchInfo?.let {
                    SportDetailActivity.startActivity(context, it)
                }
            },

                onClickBetListener = { gameTypeCode, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                    if (!fragment.mIsEnabled) {
                        return@HomeRecommendListener
                    }
                    fragment.avoidFastDoubleClick()
                    val gameType = GameType.getGameType(gameTypeCode)
                    if (gameType == null || matchInfo == null || fragment.requireActivity() !is MainTabActivity) {
                        return@HomeRecommendListener
                    }
                    val fastBetDataBean = FastBetDataBean(
                        matchType = matchType,
                        gameType = gameType,
                        playCateCode = playCateCode,
                        playCateName = playCateName,
                        matchInfo = matchInfo,
                        matchOdd = null,
                        odd = odd,
                        subscribeChannelType = ChannelType.HALL,
                        betPlayCateNameMap = betPlayCateNameMap,
                        playCateMenuCode = playCateMenuCode,
                        categoryCode = matchInfo.categoryCode
                    )

                    fragment.requireActivity().doOnStop(true) { // 延时加入注单，不然当前页面会弹出来注单列表
                        (fragment.viewModel as BaseSocketViewModel).updateMatchBetListData(fastBetDataBean)
                    }
                    SportDetailActivity.startActivity(fragment.requireContext(), matchInfo = matchInfo, matchType=matchType)
                }, onClickPlayTypeListener = { _, _, _, _ ->

                })
        )
    }

    private fun setUpAdapter(
        lifecycleOwner: LifecycleOwner, homeRecommendListener: HomeRecommendListener
    ) {
        adapter = HotMatchAdapter(lifecycleOwner, homeRecommendListener)
        recycler_hot_game.adapter = adapter
        scrollImageStatus(0)
    }

    private fun subscribeChannelHall(recommend: Recommend, fragment: BaseFragment<*>) {
        if (fragment is BaseSocketFragment) {
            fragment.subscribeChannel2HotMatch(
                recommend.matchInfo?.gameType, recommend.matchInfo?.id
            )
        }
        if (fragment is org.cxct.sportlottery.ui.base.BindingSocketFragment<*, *>) {
            fragment.subscribeChannel2HotMatch(
                recommend.matchInfo?.gameType, recommend.matchInfo?.id
            )
        }
    }

    private fun unSubscribeChannelHall(fragment: BaseFragment<*>) {
        if (fragment is org.cxct.sportlottery.ui.base.BindingSocketFragment<*, *>) {
            fragment.unSubscribeChannel2HotMatch()
        }
        if (fragment is BaseSocketFragment) {
            fragment.unSubscribeChannel2HotMatch()
        }
    }

    fun onResume(fragment: BaseFragment<*>) {
        //关闭/显示   热门赛事
        goneWithSportSwitch()
        if (fragment is BaseSocketFragment) {
            fragment.receiver.addOddsChangeListener(fragment, mOddsChangeListener)
        }
        if (fragment is BindingSocketFragment<*, *>) {
            fragment.receiver.addOddsChangeListener(fragment, mOddsChangeListener)
        }
        adapter?.notifyDataSetChanged()
    }

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val targetList = adapter?.data
            targetList?.forEachIndexed { index, recommend ->
                if (recommend.matchInfo?.id == oddsChangeEvent.eventId) {
                    recommend.sortOddsMap()
                    //region 翻譯更新
                    oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                        recommend.playCateNameMap?.putAll(playCateNameMap)
                    }
                    oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                        recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                    }
                    //endregion
                    if (SocketUpdateUtil.updateMatchOdds(
                            context, recommend, oddsChangeEvent
                        )
                    ) {
                        adapter?.notifyItemChanged(index, recommend)
                    }
                }
            }
        }
    }

    private fun Recommend.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if ((value?.size ?: 0) > 3
                && value?.first()?.marketSort != null
                && value?.first()?.marketSort != 0
                && (value?.first()?.odds != value?.first()?.malayOdds)
            ) {
                value?.sortBy { it.marketSort }
            }
        }
    }


    /**
     * 前后滚动recycler
     */
    private fun scrollRecycler(manager: LinearLayoutManager, isNext: Boolean) {
        //第一个完全显示的item
        val visiblePosition = manager.findFirstCompletelyVisibleItemPosition()
        //第一个显示的item
        val visiblePosition2 = manager.findFirstVisibleItemPosition()
        var position = if (visiblePosition == -1) {
            if (isNext) {
                visiblePosition2 + 1
            } else {
                visiblePosition2 - 1
            }
        } else {
            if (isNext) {
                visiblePosition + 1
            } else {
                visiblePosition - 1
            }
        }
        if (position > manager.itemCount - 1) {
            return
        }
        if (position < 0) {
            position = 0
        }
        recycler_hot_game.smoothScrollToPosition(position)
    }


    private fun scrollImageStatus(position: Int) {
        if (position == 0) {
            iv_left.gone()
            ivBackPage.alpha = 0.5f
        } else {
            iv_left.visible()
            ivBackPage.alpha = 1f
        }
        adapter?.let {
            if (position == it.data.size - 1) {
                iv_right.gone()
                ivForwardPage.alpha = 0.5f
            } else {
                iv_right.visible()
                ivForwardPage.alpha = 1f
            }
        }
    }

    private val delayObserver by lazy { DelayRunable { firstVisibleRange() } }

    fun resubscribe() {
        delayObserver.doOnDelay(200)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun firstVisibleRange(): Boolean {
        if (fragment?.activity == null
            || recycler_hot_game == null
            || recycler_hot_game.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return false
        }
        val adapter = adapter as HotMatchAdapter
        if (adapter.data.isNullOrEmpty()) {
            return false
        }
        var needSubscribe = false
        recycler_hot_game.getVisibleRangePosition().forEach { position ->
            if (position < adapter.data.size) {
                val recommend = adapter.data[position]
                subscribeChannelHall(recommend, fragment!!)
            }
            needSubscribe = true
        }
        return needSubscribe
    }
}