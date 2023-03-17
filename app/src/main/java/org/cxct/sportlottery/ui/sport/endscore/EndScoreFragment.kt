package org.cxct.sportlottery.ui.sport.endscore

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_sport_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.*
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketGridManager
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.*
import org.cxct.sportlottery.util.*

/**
 * @app_destination 末位比分
 */
class EndScoreFragment: BaseBottomNavigationFragment<SportListViewModel>(SportListViewModel::class) {

    // 篮球末尾比分组合玩法
    private val playCate = PlayCate.FS_LD_CS.value
    private val matchType = MatchType.END_SCORE
    private var gameType: String = GameType.BK.key

    override fun layoutId() = R.layout.fragment_sport_list
    fun getCurGameType() = GameType.BK

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            if (game_list == null || context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
                return@OddsChangeListener
            }

            endScoreAdapter.onMatchOdds(subscribedMatchOdd, oddsChangeEvent)
        }
    }

    private fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val endScoreAdapter: EndScoreAdapter by lazy {

        game_list?.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    subscribeMatchOdds(0)
                } else {
                    unSubscribeAll()
                }
            }
        })

        EndScoreAdapter(playCate) { _, _, item ->
            if (item is Odd) {  // 赔率
                addOutRightOddsDialog(item.parentNode as MatchOdd, item, playCate)
                return@EndScoreAdapter
            }

            if (item is MatchOdd) {
                viewModel.pinFavorite(FavoriteType.MATCH, item.matchInfo?.id)
                return@EndScoreAdapter
            }

             // 展开或收起后重新订阅
            subscribeMatchOdds(200)
        }
    }

    override fun onBindView(view: View) {
        ll_sport_type.gone()
        EventBusUtil.targetLifecycle(this)
        viewModel.gameType = gameType
        setupToolbar()
        setupGameRow()
        setupGameListView()
        initObserve()
        initSocketObserver()
        loadData()
    }

    private fun loadData() {
        endScoreAdapter.showLoading(R.layout.view_list_loading)
        viewModel.getGameHallList(matchType, true)
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

        appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            offsetScrollListener?.invoke((-verticalOffset) / Math.max(1.0, appbar_layout.measuredHeight.toDouble()))
        })

        //冠军不需要筛选
        lin_filter.gone()
        iv_arrow.bindExpanedAdapter(endScoreAdapter) { subscribeMatchOdds(100) }
    }

    private fun setupGameRow() {
        setViewGone(iv_calendar, game_filter_type_list)
    }

    private fun setupGameListView() = game_list.run {
        layoutManager = SocketGridManager(context, 2)
        adapter = endScoreAdapter
    }

    private fun subscribeMatchOdds(delay: Long = 0) {
        unSubscribeAll()
        game_list?.postDelayed(subscribeVisibleRange, delay)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (game_list == null
                || endScoreAdapter.getCount() < 1
                || game_list?.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
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
            sport_type_list.isVisible = !it
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
            endScoreAdapter.setNewInstance(list)

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

            if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
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

        (activity as MainTabActivity).setupBetData(FastBetDataBean(
            matchType = MatchType.END_SCORE,
            gameType = GameType.BK,
            playCateCode = playCateCode,
            playCateName = getString(R.string.home_tab_end_score),
            matchInfo = matchOdd.matchInfo!!,
            matchOdd = null,
            odd = odd,
            subscribeChannelType = ChannelType.HALL,
            betPlayCateNameMap = null))
    }

    private val subscribedMatchOdd = mutableMapOf<String, MatchOdd>()

    private fun subscribeChannelHall(matchOdd: MatchOdd) {
        subscribedMatchOdd["${matchOdd.matchInfo?.id}"] = matchOdd
        subscribeChannelHall(GameType.BK.key, matchOdd?.matchInfo?.id)
        matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
    }

    private fun unSubscribeAll() {
        game_list?.removeCallbacks(subscribeVisibleRange)
        subscribedMatchOdd.clear()
        unSubscribeChannelHallAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        offsetScrollListener = null
        game_list.adapter = null
        unSubscribeAll()
        unSubscribeChannelHallSport()
    }

}