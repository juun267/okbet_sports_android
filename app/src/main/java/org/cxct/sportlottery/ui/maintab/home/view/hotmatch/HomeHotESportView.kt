package org.cxct.sportlottery.ui.maintab.home.view.hotmatch

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
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
import org.cxct.sportlottery.common.enums.ChannelType
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.doOnStop
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ViewHomeHotEsportBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.*
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.onClick
import splitties.systemservices.layoutInflater

class HomeHotESportView(
    context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs) {

    val binding = ViewHomeHotEsportBinding.inflate(layoutInflater, this)
    private var adapter: HomeHotESportAdapter? = null
    private var fragment: BaseFragment<*,*>? = null

    init {
        orientation = VERTICAL
        gone()
        initView()
    }

    fun setVisible() {
        isVisible = !getSportEnterIsClose() && adapter != null && adapter!!.itemCount > 0
    }

    private fun initView()=binding.run{
        initRecyclerView()
        //查看更多
        tvHotMore.onClick {
            if(StaticData.okSportOpened()&&StaticData.okBingoOpened()) {
                (fragment?.activity as MainTabActivity).jumpToESport()
            }else{
                ToastUtil.showToast(context,context.getString(R.string.N700))
            }
        }

    }

    private fun initRecyclerView()=binding.recyclerHotGame.run {
        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        addItemDecoration(
            DividerItemDecorator(
                ContextCompat.getDrawable(
                    context, R.drawable.divider_trans_8
                )
            )
        )
        itemAnimator?.changeDuration = 0
        PagerSnapHelper().attachToRecyclerView(this)
        //滚动监听
        recycler_hot_game.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    fun onCreate(data: LiveData<Pair<String,List<Recommend>>>,oddsTypeLiveData: LiveData<OddsType>, fragment: BaseFragment<*,*>?) {
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
    }

    /**
     * 数据变量监听
     */
    private fun initDataObserve(data: LiveData<Pair<String,List<Recommend>>>, oddsTypeLiveData: LiveData<OddsType>,fragment: BaseFragment<*,*>) {

        data.observe(fragment.viewLifecycleOwner) {

            val data = it.second
            if(data.isEmpty()){ //如果没数据
                gone()
            } else{
                isVisible = !getSportEnterIsClose() //如果体育服务关闭
            }

            unSubscribeChannelHall(fragment)
            if (isVisible) {
                adapter?.data = data
                binding.recyclerHotGame.post { firstVisibleRange() }
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
        fragment: BaseFragment<*,*>
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


    private fun initAdapter(fragment: BaseFragment<*,*>) {
        setUpAdapter(fragment,
            HomeRecommendListener(onItemClickListener = { matchInfo ->
                matchInfo?.let {
                    SportDetailActivity.startActivity(context, it)
                }
            },

                onClickBetListener = { gameTypeCode, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
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
                        val viewModel = fragment.viewModel as BaseSocketViewModel
                        viewModel.updateMatchBetListData(fastBetDataBean)
                    }
                    SportDetailActivity.startActivity(fragment.requireContext(), matchInfo = matchInfo, matchType=matchType)
                }, onClickPlayTypeListener = { _, _, _, _ ->

                })
        )
    }

    private fun setUpAdapter(
        lifecycleOwner: LifecycleOwner, homeRecommendListener: HomeRecommendListener
    ) {
        adapter = HomeHotESportAdapter(lifecycleOwner, homeRecommendListener)
        binding.recyclerHotGame.adapter = adapter
        binding.recyclerHotGame.scrollToPosition(0)
    }

    private fun subscribeChannelHall(recommend: Recommend, fragment: BaseFragment<*,*>) {
        if (fragment is BaseSocketFragment) {
            fragment.subscribeChannel2HotMatch(
                recommend.matchInfo?.gameType, recommend.matchInfo?.id
            )
        }
    }

    private fun unSubscribeChannelHall(fragment: BaseFragment<*,*>) {
        if (fragment is BaseSocketFragment) {
            fragment.unSubscribeChannel2HotMatch()
        }
    }

    fun onResume(fragment: BaseFragment<*,*>) {
        //关闭/显示   热门赛事
        setVisible()
        adapter?.notifyDataSetChanged()
    }

    fun updateOddChange(oddsChangeEvent: OddsChangeEvent){
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
        binding.recyclerHotGame.smoothScrollToPosition(position)
    }


    private val delayObserver by lazy { DelayRunable { firstVisibleRange() } }

    fun resubscribe() {
        delayObserver.doOnDelay(200)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun firstVisibleRange(): Boolean =binding.run {
        if (fragment?.activity == null
            || recyclerHotGame == null
            || recyclerHotGame.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return false
        }
        val adapter = adapter as HomeHotESportAdapter
        if (adapter.data.isNullOrEmpty()) {
            return false
        }
        var needSubscribe = false
        recyclerHotGame.getVisibleRangePosition().forEach { position ->
            if (position < adapter.data.size) {
                val recommend = adapter.data[position]
                subscribeChannelHall(recommend, fragment!!)
            }
            needSubscribe = true
        }
        return needSubscribe
    }
}