package org.cxct.sportlottery.ui.sport.endscore


import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager
import java.util.ArrayList

/**
 * @app_destination 末位比分
 */
class EndScoreFragment: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.END_SCORE
    override fun getCurGameType() = GameType.BK
    override fun getGameListAdapter() = endScoreAdapter
    override fun getGameLayoutManger() = SocketGridManager(context(), 4)
    override fun observerMenuData() { }

    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
        if (context == null || oddsChangeEvent.oddsList.isNullOrEmpty()) {
            return@OddsChangeListener
        }
        endScoreAdapter.onMatchOdds(oddsChangeEvent)
    }

    override fun resubscribeChannel(delay: Long) {
        clearSubscribeChannels()
        subscribeHandler.postDelayed(subscribeVisibleRange, delay)
    }

    override fun onFavorite(favoriteMatchIds: Set<String>) {
        endScoreAdapter.updateFavorite(favoriteMatchIds)
    }

    override fun onOddTypeChanged(oddsType: OddsType) {
        endScoreAdapter.oddsType = oddsType
    }

    override fun onBetInfoChanged(betInfoList: List<BetInfoListData>) {
        endScoreAdapter.updateOddsSelectedStatus(betInfoList)
    }

    override fun onReload() {
        loadData()
    }


    private val endScoreAdapter by lazy {

        EndScoreAdapter { _, view, item ->
            if (item is Odd) {  // 赔率
                val matchOdd = item.parentNode as MatchOdd
                val matchInfo = matchOdd.matchInfo ?: return@EndScoreAdapter
                addOddsDialog(matchInfo, item, matchOdd.selectPlayCode, betPlayCateNameMap = matchOdd.betPlayCateNameMap)
                return@EndScoreAdapter
            }

            if (item is MatchOdd) { // 赛事栏相关点击
                if (view is ViewGroup) { // 赛事详情
                    item.matchInfo?.let {
                        SportDetailActivity.startActivity(view.context,
                            matchInfo = it,
                            matchType = MatchType.EARLY,
                            tabCode = MatchType.END_SCORE.postValue)
                    }
                } else { // 收藏赛事
                    viewModel.pinFavorite(FavoriteType.MATCH, item.matchInfo?.id)
                }
                return@EndScoreAdapter
            }

             // 展开或收起后重新订阅
            resubscribeChannel(200)
        }
    }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.sportTypeList.gone()
        binding.tvSportName.setText(R.string.basketball)
        binding.gameList.setBackgroundColor(ContextCompat.getColor(context(), R.color.color_FAFDFF))
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        binding.tvMatchNum.text = "0"
        initObserve()
        loadData()
    }

    private fun loadData() {
        showLoading()
        viewModel.getGameHallList(matchType, gameType)
    }

    override fun setSelectMatch(leagueIdList: ArrayList<String>,matchIdList: ArrayList<String>) {
        clearData()
        showLoading()
        viewModel.getGameHallList(matchType, gameType, leagueIdList,matchIdList)
    }

    private val subscribeVisibleRange by lazy {
        Runnable {
            if (endScoreAdapter.getCount() < 1) {
                return@Runnable
            }

            if (binding.gameList.scrollState != RecyclerView.SCROLL_STATE_IDLE
                || binding.gameList.isComputingLayout) {
                resubscribeChannel(40)
                return@Runnable
            }

            endScoreAdapter.recodeRangeMatchOdd().forEach {
                subscribeChannel(GameType.BK.key, it.matchInfo?.id)
                it?.matchInfo?.let { Log.e("[subscribe]","訂閱 ${it.name} ${it.id} -> " + "${it.homeName} vs " + "${it.awayName}") }
            }
        }
    }

    private fun initObserve() = viewModel.run {

        oddsListGameHallResult.observe(viewLifecycleOwner) {

            val result = it.getContentIfNotHandled()
            if (result == null) {
                dismissLoading()
                return@observe
            }
            val list = result.oddsListData?.leagueOdds as MutableList<BaseNode>?
            //api拿到到数据，第一个默认展开
            list?.forEachIndexed { index, baseNode ->
                (baseNode as BaseExpandNode).isExpanded = (index == 0)
                baseNode.childNode?.forEach { (it as BaseExpandNode).isExpanded = false }
            }
            setSportDataList(list)
            dismissLoading()
        }

    }


}