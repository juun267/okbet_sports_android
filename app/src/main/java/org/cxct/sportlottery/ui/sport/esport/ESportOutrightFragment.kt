package org.cxct.sportlottery.ui.sport.esport

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.ui.sport.outright.SportOutrightAdapter2
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.showLoginSnackbar
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import org.cxct.sportlottery.view.layoutmanager.SocketGridManager
import java.util.ArrayList

class ESportOutrightFragment: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.OUTRIGHT
    override var gameType = GameType.ES.key
    override fun getGameListAdapter() = sportOutrightAdapter2
    override fun getGameLayoutManger() = SocketGridManager(context(), 2)
    protected val esportTypeAdapter by lazy { ESportTypeAdapter(::onESportTypeChanged) }
    var currentCategoryItem :CategoryItem? =null

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

        SportOutrightAdapter2(this@ESportOutrightFragment) { _, _, item ->
            if (item is Odd) {  // 赔率
                val matchOdd = (item.parentNode as CategoryOdds).matchOdd
                val matchInfo = matchOdd.matchInfo ?: return@SportOutrightAdapter2
                addOddsDialog(matchInfo, item, item.outrightCateKey ?: "", betPlayCateNameMap=matchOdd.betPlayCateNameMap, outRightMatchOdd = matchOdd)
            } else { // 展开或收起
                resubscribeChannel(300)
            }
        }
    }
    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.ivFilter.gone()
        setupSportTypeList()
        setESportType()
    }
    override fun onBindViewStatus(view: View) {
        currentItem = null
        currentCategoryItem = null
        super.onBindViewStatus(view)
        arguments?.getString("gameType")?.let { gameType = it }
        initObserve()
        showLoading()
        getMenuDataByParent(true)
    }

    fun reload(gameType: String) {
        this.gameType = gameType
        gameTypeAdapter.selectGameType(gameType)
    }

    private fun setupSportTypeList() {
        binding.sportTypeList.visible()
        binding.sportTypeList.adapter = esportTypeAdapter
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
            val data = it?.getContentIfNotHandled()?.outrightOddsListData?.leagueOdds ?: return@observe
            val list = mutableListOf<MatchOdd>()
            data.forEach { it.matchOdds?.let { list.addAll(it) } }
            dismissLoading()
            if (list.isEmpty()) {
                return@observe
            }
            setSportDataList(list as MutableList<BaseNode>, list.size.toString())
        }
        viewModel.esportTypeMenuData.observe(this@ESportOutrightFragment.viewLifecycleOwner){
            it.first?.let {
                updateESportType(it)
                return@observe
            }
            dismissLoading()
            setSportDataList(null)
            if (!it.second) {
                ToastUtil.showToast(activity, it.third)
                return@observe
            }
        }
    }
    override fun updateSportType(gameTypeList: List<Item>) {
        //这里是体育大厅的结果显示，电竞用 updateESportType 方法，
    }
    protected fun updateESportType(item: Item) {
        if (item?.categoryList.isNullOrEmpty()) {
            dismissLoading()
            setSportDataList(null)
            return
        }
        currentItem = item
        //处理默认不选中的情况
        var targetItem: CategoryItem? = null
        item.categoryList?.forEach {
            it.isSelected = false
        }
        binding.sportTypeList.show()
        targetItem = item.categoryList?.first()
        if (currentCategoryItem==null){
            currentCategoryItem = targetItem
            currentCategoryItem?.isSelected = true
            load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
        }else{
            val existItem = item.categoryList?.firstOrNull { it.code == currentCategoryItem!!.code }
            currentCategoryItem = existItem?:targetItem
            currentCategoryItem?.isSelected = true
            if (existItem!=currentCategoryItem){
                load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
            }
        }
        esportTypeAdapter.setNewInstance(item.categoryList)
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            esportTypeAdapter.data.indexOfFirst { it.isSelected })

    }
    fun onESportTypeChanged(item: CategoryItem, position: Int){
        currentCategoryItem = item
        clearData()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        currentItem?.let { load(it, categoryCodeList = item.categoryCodeList) }
    }
    fun setESportType(){
        //电竞主题背景增加
        binding.sportTypeList.setBackgroundResource(R.drawable.bg_esport_game)
        binding.linOpt.setBackgroundResource(R.drawable.bg_white_alpha70_radius_8_top)
        binding.gameList.setBackgroundResource(R.color.color_FFFFFF)
    }

}