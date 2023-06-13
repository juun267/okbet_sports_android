package org.cxct.sportlottery.ui.sport.endscore

import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.TimeRangeEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.showEmpty
import org.cxct.sportlottery.common.extentions.showLoading
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager
import org.greenrobot.eventbus.Subscribe

/**
 * @app_destination 末位比分
 */
class EndScoreFragment: BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {

    // 篮球末尾比分组合玩法
    private val playCate = PlayCate.FS_LD_CS.value
    private val matchType = MatchType.END_SCORE
    private val gameType: String = GameType.BK.key

    fun getCurGameType() = GameType.BK

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (binding.gameList == null || context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            endScoreAdapter.onMatchOdds(subscribedMatchOdd, oddsChangeEvent)
        }
    }

    private fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val endScoreAdapter: EndScoreAdapter by lazy {

        binding.gameList.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    subscribeMatchOdds(0)
                } else {
                    unSubscribeAll()
                }
            }
        })

        EndScoreAdapter(playCate) { _, view, item ->
            if (item is Odd) {  // 赔率
                addOutRightOddsDialog(item.parentNode as MatchOdd, item, playCate)
                return@EndScoreAdapter
            }

            if (item is MatchOdd) { // 赛事栏相关点击
                if (view is ViewGroup) { // 赛事详情
                    item.matchInfo?.let {
                        SportDetailActivity.startActivity(view.context,
                            it,
                            MatchType.EARLY,
                            tabCode = MatchType.END_SCORE.postValue)
                    }
                } else { // 收藏赛事
                    viewModel.pinFavorite(FavoriteType.MATCH, item.matchInfo?.id)
                }
                return@EndScoreAdapter
            }

             // 展开或收起后重新订阅
            subscribeMatchOdds(200)
        }
    }

    override fun onInitView(view: View) {
        binding.sportTypeList.gone()
        setupToolbar()
        setupGameRow()
        setupGameListView()
    }

    override fun onBindViewStatus(view: View) {
        EventBusUtil.targetLifecycle(this)
        viewModel.gameType = gameType
        binding.tvMatchNum.text = "0"
        initObserve()
        initSocketObserver()
        loadData()
    }

    private fun loadData() {
        endScoreAdapter.showLoading(R.layout.view_list_loading)
        viewModel.getGameHallList(matchType)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            unSubscribeAll()
        } else {
            //receiver.oddsChangeListener為activity底下共用, 顯示當前畫面時需重新配置listener
            setupOddsChangeListener()
            subscribeMatchOdds()
        }
    }

    var offsetScrollListener: ((Double) -> Unit)? = null

    private fun setupToolbar() {
        binding.appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke((-verticalOffset) / Math.max(1.0, binding.appbarLayout.measuredHeight.toDouble()))
        })
        binding.ivArrow.bindExpanedAdapter(endScoreAdapter) { subscribeMatchOdds(100) }
    }

    private fun setupGameRow() {
        binding.tvSportName.setText(R.string.basketball)
        binding.ivFilter.setOnClickListener {
            LeagueSelectActivity.start(
                it.context,
                gameType!!,
                matchType,
                null,
                viewModel.selectMatchIdList
            )
        }
    }

    private fun setupGameListView() = binding.gameList.run {
        layoutManager = SocketGridManager(context, 4)
        adapter = endScoreAdapter
//        addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
//        addItemDecoration(GridItemDecoration(9, 9, Color.RED, false))
//        StickyHeaderItemDecorator(endScoreAdapter).attachToRecyclerView(this)
    }

    private fun subscribeMatchOdds(delay: Long = 0) {
        unSubscribeAll()
        binding.gameList.postDelayed(subscribeVisibleRange, delay)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (endScoreAdapter.getCount() < 1
                || binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                return@Runnable
            }

            val matchOdds = mutableSetOf<MatchOdd>()
            endScoreAdapter.doOnVisiableRange { _, item->
                if (item is MatchOdd) {
                    matchOdds.add(item)
                } else if (item is Odd) {
                    matchOdds.add(item.parentNode as MatchOdd)
                }
            }

            matchOdds.forEach { subscribeChannelHall(it) }
        }
    }

    private fun initObserve() = viewModel.run {
        oddsType.observe(viewLifecycleOwner) { it?.let { endScoreAdapter.oddsType = it } }
        notifyLogin.observe(viewLifecycleOwner) { (activity as MainTabActivity).showLoginNotify() }

        showErrorDialogMsg.observe(viewLifecycleOwner) {
            if (requireContext() == null || TextUtils.isEmpty(it)) {
                return@observe
            }

            showErrorMsgDialog(it)
        }

        //當前玩法無賽事
        isNoEvents.distinctUntilChanged().observe(viewLifecycleOwner) {
            binding.sportTypeList.isVisible = !it
            hideLoading()
        }

        betInfoList.observe(viewLifecycleOwner) {
            it.peekContent().let {
                endScoreAdapter.updateOddsSelectedStatus(it)
            }
        }

        oddsListGameHallResult.observe(viewLifecycleOwner) {
            val result = it.getContentIfNotHandled() ?: return@observe
            val list = result.oddsListData?.leagueOdds as MutableList<BaseNode>?
            //api拿到到数据，第一个默认展开
            list?.forEachIndexed { index, baseNode ->
                (baseNode as BaseExpandNode).isExpanded = (index == 0)
            }
            endScoreAdapter.setNewInstance(list)
            binding.tvMatchNum.text = "${list?.size ?: 0}"

            if (list.isNullOrEmpty()) {
                endScoreAdapter.showEmpty(R.layout.itemview_game_no_record)
            } else {
                subscribeMatchOdds(120)
            }
        }

        favorMatchList.observe(viewLifecycleOwner) { favorMatchIds ->

            val rootNodes = endScoreAdapter.rootNodes ?: return@observe
            if (rootNodes.isNullOrEmpty()) {
                return@observe
            }

            val matchOddMap = mutableMapOf<String, MatchOdd>()
            rootNodes.forEach { rootNode ->
                rootNode.childNode?.forEach {
                    val matchOdd = (it as MatchOdd)
                    matchOdd.matchInfo?.run {
                        isFavorite = favorMatchIds.contains(id)
                        matchOddMap[id] = matchOdd
                    }
                }
            }

            if (binding.gameList.scrollState == RecyclerView.SCROLL_STATE_IDLE && !binding.gameList.isComputingLayout) {
                endScoreAdapter.doOnVisiableRange { position, item ->
                    if (item is MatchOdd) {
                        endScoreAdapter.notifyItemChanged(position, item)
                    }
                }
            }
        }
    }

    private fun initSocketObserver() = receiver.run {
        serviceConnectStatus.distinctUntilChanged().observe(viewLifecycleOwner) {
            if (it == ServiceConnectStatus.CONNECTED) {
                viewModel.switchMatchType(matchType = matchType)
                subscribeSportChannelHall()
            }
        }

        setupOddsChangeListener()
    }

    private fun addOutRightOddsDialog(matchOdd: MatchOdd, odd: Odd, playCateCode: String) {

        val matchInfo = matchOdd.matchInfo ?: return
        (activity as MainTabActivity).setupBetData(FastBetDataBean(
            matchType = MatchType.END_SCORE,
            gameType = GameType.BK,
            playCateCode = playCateCode,
            playCateName = getString(R.string.home_tab_end_score),
            matchInfo = matchInfo,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = matchOdd.betPlayCateNameMap))
    }

    private val subscribedMatchOdd = mutableMapOf<String, MatchOdd>()

    private fun subscribeChannelHall(matchOdd: MatchOdd) {
        subscribedMatchOdd["${matchOdd.matchInfo?.id}"] = matchOdd
        subscribeChannelHall(GameType.BK.key, matchOdd?.matchInfo?.id)
        matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
    }

    private fun unSubscribeAll() {
        binding.gameList.removeCallbacks(subscribeVisibleRange)
        subscribedMatchOdd.clear()
        unSubscribeChannelHallAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offsetScrollListener = null
        endScoreAdapter.setNewInstance(null)
        unSubscribeAll()
        unSubscribeChannelHallSport()
    }

    @Subscribe
    fun onSelectMatch(matchIdList: ArrayList<String>?) {
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
}