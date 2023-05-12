package org.cxct.sportlottery.ui.maintab.games.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.event.JumpInPlayEvent
import org.cxct.sportlottery.common.extentions.doOnStop
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.HotMatchAdapter
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.isCreditSystem
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.onClick

@Suppress("NAME_SHADOWING")
class HotMatchView(context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private var adapter: HotMatchAdapter? = null

    init {
        orientation=VERTICAL
        initView()
    }


    private fun initView(){
        LayoutInflater.from(context).inflate(R.layout.view_hot_game, this, true)
        val manager=LinearLayoutManager(context)
        initRecyclerView(manager)
        //右滑动
        iv_right.onClick {
            scrollRecycler(manager, true)
        }
        //左滑动
        iv_left.onClick {
            scrollRecycler(manager, false)
        }
        //查看更多
        tvHotMore.onClick {
            EventBusUtil.post(JumpInPlayEvent())
        }
        ivHotMore.onClick {
            EventBusUtil.post(JumpInPlayEvent())
        }

    }

    private fun initRecyclerView(manager:LinearLayoutManager){
        recycler_hot_game.let {
            manager.orientation=LinearLayoutManager.HORIZONTAL
            it.layoutManager = manager
            it.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context,
                R.drawable.divider_trans)))
            it.itemAnimator?.changeDuration=0

            //滚动监听   显示/隐藏 左右两个滑动按钮
            it.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val position=manager.findFirstCompletelyVisibleItemPosition()
                    //屏幕中没有完整的item 前后都能滑动
                    if(position==-1){
                        iv_left.visible()
                        iv_right.visible()
                    }else{
                        //检测是否需要隐藏 前进/后退 imageView
                        scrollImageStatus(position)
                    }
                }
            })
        }
    }


    /**
     * 初始化热门赛事控件
     */
    fun<T: BaseSocketViewModel> onCreate(data:LiveData<Event<List<Recommend>>>, fragment: BaseSocketFragment<T>?){
        if(fragment==null){
            return
        }
        //初始化api变量监听
        initDataObserve(data,fragment)
        //初始化adapter
        initAdapter(fragment)
        //设置item attached 后，订阅该赛事
        adapter?.setOnViewAttach {
            subscribeChannelHall(it,fragment)
        }
        //初始化ws广播监听
        initSocketObservers(fragment)
    }

    /**
     * 数据变量监听
     */
    private fun<T: BaseSocketViewModel> initDataObserve(data:LiveData<Event<List<Recommend>>>, fragment: BaseSocketFragment<T>){
        data.observe(fragment.viewLifecycleOwner){
            this.visible()
            //api获取热门赛事列表
            it.peekContent().let { data ->
                //清除上次订阅的赛事记录
                adapter?.clearSubCache()
                //取消所有订阅
                fragment.unSubscribeChannel2HotMatch()
                adapter?.data = data
                data.forEach {
                    subscribeChannelHall(it, fragment)
                }
            }
        }
    }

    /**
     * ws订阅监听
     */
    private fun<T: BaseSocketViewModel> initSocketObservers(fragment: BaseSocketFragment<T>?){
        fragment?.let {
            //观察比赛状态改变
            fragment.receiver.matchStatusChange.observe(fragment.viewLifecycleOwner) { matchStatusChangeEvent ->
                if (matchStatusChangeEvent == null) {
                    return@observe
                }

                if (adapter == null || adapter!!.data.isEmpty()) {
                    return@observe
                }
                val adapterData = adapter?.data
                adapterData?.forEachIndexed { index, recommend ->

                    //丢进去判断是否要更新
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.matchInfo?.gameType,
                            recommend,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        adapter?.notifyItemChanged(index,recommend)
                    }
                }
            }


            fragment.receiver.matchClock.observe(fragment.viewLifecycleOwner) {
                it?.let { matchClockEvent ->
                    val targetList = adapter?.data
                    targetList?.forEachIndexed { index, recommend ->
                        if (
                            SocketUpdateUtil.updateMatchClock(
                                recommend,
                                matchClockEvent
                            )
                        ) {
                            adapter?.notifyItemChanged(index,recommend)
                        }
                    }
                }
            }

            fragment.receiver.matchClock.observe(fragment.viewLifecycleOwner) {
                it?.let { matchClockEvent ->
                    val targetList = adapter?.data
                    targetList?.forEachIndexed { index, recommend ->
                        if (
                            SocketUpdateUtil.updateMatchClock(
                                recommend,
                                matchClockEvent
                            )
                        ) {
                            adapter?.notifyItemChanged(index,recommend)
                        }
                    }

                }
            }
            fragment.receiver.oddsChangeListener = mOddsChangeListener

            fragment.receiver.matchOddsLock.observe(fragment.viewLifecycleOwner) {
                it?.let { matchOddsLockEvent ->
                    val targetList = adapter?.data

                    targetList?.forEachIndexed { index, recommend ->
                        if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                        ) {
                            adapter?.notifyItemChanged(index,recommend)
                        }
                    }

                }
            }


            fragment.receiver.globalStop.observe(fragment.viewLifecycleOwner) {
                it?.let { globalStopEvent ->
                    adapter?.data?.forEachIndexed { index, recommend ->
                        if (SocketUpdateUtil.updateOddStatus(
                                recommend,
                                globalStopEvent
                            )
                        ) {
                            adapter?.notifyItemChanged(index,recommend)
                        }
                    }
                }
            }

            fragment.receiver.producerUp.observe(fragment.viewLifecycleOwner) {
                it?.let {
                    //先解除全部賽事訂閱
                    fragment.unSubscribeChannel2HotMatch()
                    adapter?.data ?: listOf<Recommend>().forEach {recommend->
                        subscribeChannelHall(recommend,fragment)
                    }
                }
            }

            fragment.receiver.closePlayCate.observe(fragment.viewLifecycleOwner) { event ->
                val it = event?.getContentIfNotHandled() ?: return@observe
                adapter?.data?.forEachIndexed {index, recommend ->
                    if (recommend.gameType == it.gameType) {
                        recommend.oddsMap?.forEach { map ->
                            if (map.key == it.playCateCode) {
                                map.value?.forEach { odd ->
                                    odd.status = BetStatus.DEACTIVATED.code
                                }
                                adapter?.notifyItemChanged(index,recommend)
                            }
                        }
                    }
                }

            }
        }
    }


    private fun<T: BaseSocketViewModel> initAdapter(fragment:BaseSocketFragment<T>){
        setUpAdapter(fragment.viewLifecycleOwner, HomeRecommendListener(
            onItemClickListener = { matchInfo ->
                if (isCreditSystem() && fragment.viewModel.isLogin.value != true) {
                    (fragment.requireActivity() as MainTabActivity).showLoginNotify()
                } else {
                    matchInfo?.let {
                        SportDetailActivity.startActivity(context, it)
                    }
                }
            },

            onClickBetListener = { gameTypeCode, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                if (!fragment.mIsEnabled) {
                    return@HomeRecommendListener
                }
                fragment.avoidFastDoubleClick()
                if (isCreditSystem() && fragment.viewModel.isLogin.value != true) {
                    (fragment.requireActivity() as MainTabActivity).showLoginNotify()
                    return@HomeRecommendListener
                }
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
                    playCateMenuCode
                )

                fragment.requireActivity().doOnStop(true) { // 延时加入注单，不然当前页面会弹出来注单列表
                    fragment.viewModel.updateMatchBetListData(fastBetDataBean)
                }
                SportDetailActivity.startActivity(
                    fragment.requireContext(),
                    matchInfo,
                    matchType,
                    false
                )
            }, onClickPlayTypeListener = { _, _, _, _ ->

            }
        ))
    }

    private fun setUpAdapter(lifecycleOwner: LifecycleOwner, homeRecommendListener: HomeRecommendListener) {
        adapter = HotMatchAdapter(lifecycleOwner, homeRecommendListener)
        recycler_hot_game.adapter = adapter
        scrollImageStatus(0)
    }

    private fun<T: BaseSocketViewModel> subscribeChannelHall(recommend: Recommend,fragment:BaseSocketFragment<T>) {
        fragment.subscribeChannel2HotMatch(recommend.matchInfo?.gameType, recommend.matchInfo?.id)
    }

    fun<T: BaseSocketViewModel> onResume(fragment: BaseSocketFragment<T>?) {
        fragment?.receiver?.oddsChangeListener = mOddsChangeListener
        adapter?.clearSubCache()
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
                            context,
                            recommend,
                            oddsChangeEvent
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
    private fun scrollRecycler(manager: LinearLayoutManager,isNext:Boolean){
        //第一个完全显示的item
        val visiblePosition=manager.findFirstCompletelyVisibleItemPosition()
        //第一个显示的item
        val visiblePosition2=manager.findFirstVisibleItemPosition()
        var position = if(visiblePosition==-1){
            if(isNext){
                visiblePosition2+1
            }else{
                visiblePosition2-1
            }
        }else{
            if(isNext){
                visiblePosition+1
            }else{
                visiblePosition-1
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


    private fun scrollImageStatus(position:Int){
        if(position==0){
            iv_left.gone()
        }else{
            iv_left.visible()
        }
        adapter?.let {
            if(position==it.data.size-1){
                iv_right.gone()
            }else{
                iv_right.visible()
            }
        }
    }
}