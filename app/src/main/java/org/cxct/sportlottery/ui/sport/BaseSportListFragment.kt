//package org.cxct.sportlottery.ui.sport
//
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.view.View
//import androidx.core.view.isVisible
//import androidx.recyclerview.widget.RecyclerView
//import com.chad.library.adapter.base.BaseQuickAdapter
//import org.cxct.sportlottery.common.loading.Gloading
//import org.cxct.sportlottery.databinding.FragmentSportList2Binding
//import org.cxct.sportlottery.network.common.GameType
//import org.cxct.sportlottery.network.common.MatchType
//import org.cxct.sportlottery.network.sport.Item
//import org.cxct.sportlottery.service.ServiceBroadcastReceiver
//import org.cxct.sportlottery.ui.base.BindingSocketFragment
//import org.cxct.sportlottery.ui.sport.common.GameTypeAdapter2
//import org.cxct.sportlottery.ui.sport.list.SportLeagueAdapter
//import org.cxct.sportlottery.ui.sport.list.SportListViewModel
//import org.cxct.sportlottery.ui.sport.list.adapter.SportLeagueAdapter2
//import org.cxct.sportlottery.util.doOnVisiableRange
//import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
//
//abstract class BaseSportListFragment: BindingSocketFragment<SportListViewModel, FragmentSportList2Binding>() {
//
//    private val matchType = MatchType.OUTRIGHT
//    private var gameType: String = GameType.BK.key
//
//    private val gameTypeAdapter by lazy { GameTypeAdapter2(::onGameTypeChanged) }
//    private val loadingHolder by lazy { Gloading.wrapView(binding.gameList) }
//    override fun dismissLoading() = loadingHolder.showLoadSuccess()
//    override fun showLoading() = loadingHolder.showLoading()
//
//    protected abstract fun getGameListAdapter(): BaseQuickAdapter<Any, *>
//    protected abstract fun getOddsChangeListener(): ServiceBroadcastReceiver.OddsChangeListener
//    protected abstract fun resubscribeChannel(delay: Long = 0)
//
//    override fun onBindViewStatus(view: View) {
//        _initObserver()
//    }
//
//    private fun _initObserver() {
//        receiver.producerUp.observe(viewLifecycleOwner) { //開啟允許投注
//            if (it == null) {
//                return@observe
//            }
//            resubscribeChannel()
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        resubscribeChannel(20)
//    }
//
//    override fun onHiddenChanged(hidden: Boolean) {
//        if (!hidden) {
//            setupOddsChangeListener()
//            resubscribeChannel(80)
//        } else {
//            clearSubscribeChannels()
//        }
//    }
//
//    private fun setupOddsChangeListener() {
//        if (isAdded) {
//            receiver.oddsChangeListener = getOddsChangeListener()
//        }
//    }
//
//    private fun onGameTypeChanged(item: Item, position: Int) {
//        //切換球種，清除日期記憶
//        viewModel.tempDatePosition = 0
//        //日期圖示選取狀態下，切換球種要重置UI狀態
//        gameType = item.code
//        clearData()
//        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
//        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
//        clearSubscribeChannels()
//        load(item)
//        binding.ivFilter.isVisible = gameType != GameType.ALL.key
//        binding.tvSportName
//    }
//
//    protected open fun load(item: Item) {
//        setMatchInfo(item.name, item.num.toString())
//        viewModel.switchGameType(matchType, item, Any())
//    }
//
//    protected inline fun setMatchInfo(name: String, num: String) {
//        binding.tvSportName.text = name
//        binding.tvMatchNum.text = num
//    }
//
//    private fun clearData() {
//        subscribedMatchOdd.clear()
//        getGameListAdapter().setNewInstance(mutableListOf<Any>())
//    }
//
//    protected val subscribedMatchOdd = mutableMapOf<String, org.cxct.sportlottery.network.outright.odds.MatchOdd>()
//
//    protected fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd) {
//        val gameType = GameType.getGameType(gameTypeAdapter.currentItem?.code)
//        gameType?.let {
//            subscribedMatchOdd["${matchOdd.matchInfo?.id}"] = matchOdd
//            subscribeChannelHall(it.key, matchOdd?.matchInfo?.id)
//            matchOdd?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
//        }
//    }
//
//    protected fun unSubscribeAll() {
//        subscribedMatchOdd.clear()
//        unSubscribeChannelHallAll()
//    }
//
//    protected val subscribedChannel = mutableListOf<Pair<String?, String?>>()
//    protected val subscribeHandler = Handler(Looper.getMainLooper())
//    protected fun clearSubscribeChannels() {
//        subscribedMatchOdd.clear()
//        if (subscribedChannel.size > 0) {
//            unSubscribeChannelHallAll()
//            subscribedChannel.clear()
//        }
//        subscribeHandler.removeCallbacksAndMessages(null)
//    }
//}