package org.cxct.sportlottery.ui.sport.outright

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.showLoginSnackbar
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class SportOutrightFragment : BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.OUTRIGHT
    override fun getGameListAdapter() = sportOutrightAdapter2
    override fun getGameLayoutManger() = SocketGridManager(context(), 2)

    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent->
        if (context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
            return@OddsChangeListener
        }

        sportOutrightAdapter2.onMatchOdds(oddsChangeEvent)
    }

    override fun onFavorite(favoriteMatchIds: Set<String>) { }

    override fun onOddTypeChanged(oddsType: OddsType) {
        sportOutrightAdapter2.oddsType = oddsType
    }

    override fun onBetInfoChanged(betInfoList: List<BetInfoListData>) {
        sportOutrightAdapter2.updateOddsSelectedStatus(betInfoList)
    }

    override fun onGameTypeChanged(item: Item, position: Int) {
        unSubscribeAll()
        binding.ivArrow.isSelected = true
        super.onGameTypeChanged(item, position)
    }


    private val sportOutrightAdapter2 by lazy {

        SportOutrightAdapter2(this@SportOutrightFragment) { _, _, item ->
            if (item is Odd) {  // 赔率
                val matchOdd = (item.parentNode as CategoryOdds).matchOdd
                val matchInfo = matchOdd.matchInfo ?: return@SportOutrightAdapter2
                addOddsDialog(matchInfo, item, item.outrightCateKey ?: "", betPlayCateNameMap=matchOdd.betPlayCateNameMap, outRightMatchOdd = matchOdd)
            } else { // 展开或收起
                resubscribeChannel(300)
            }
        }
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        binding.ivFilter.gone()
        arguments?.getString("gameType")?.let { gameType = it }
        initObserve()
        showLoading()
        getMenuDataByParent()
    }

    fun reload(gameType: String) {
        this.gameType = gameType
        gameTypeAdapter.selectGameType(gameType)
    }


    /**
     * 設置冠軍adapter, 訂閱當前頁面上的資料
     */
    override fun resubscribeChannel(delay: Long) {
        subscribeHandler.removeCallbacks(subscribeVisibleRange)
        unSubscribeAll()
        subscribeHandler.postDelayed(subscribeVisibleRange, delay)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (activity == null) {
                return@Runnable
            }

            if (binding.gameList.scrollState == RecyclerView.SCROLL_STATE_IDLE
                || binding.gameList.isComputingLayout) {
                resubscribeChannel(50)
                return@Runnable
            }

            sportOutrightAdapter2.recodeRangeMatchOdd().forEach {
                subscribeChannelHall(it)
            }
        }
    }

    private fun initObserve() = viewModel.run {

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
            setSportDataList(list as MutableList<BaseNode>, list.size.toString())
            dismissLoading()
        }

    }

}