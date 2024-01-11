package org.cxct.sportlottery.ui.sport.esport

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.rotationAnimation
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.sport.BaseSportListFragment
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.ui.sport.list.adapter.OnOddClickListener
import org.cxct.sportlottery.ui.sport.list.adapter.SportLeagueAdapter2
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import java.util.ArrayList

open class ESportListFragment<M, VB>: BaseSportListFragment<SportListViewModel, FragmentSportList2Binding>(), OnOddClickListener {

    override var matchType = MatchType.IN_PLAY
    override var gameType = GameType.ES.key

    override fun getGameListAdapter() = sportLeagueAdapter2
    override val oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener {
        sportLeagueAdapter2.onOddsChangeEvent(it)
    }

    private val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            esportTheme = true,
            onNodeExpand = { },
            onOddClick = this,
            onFavorite = { loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, it) } },
            onAttachMatch = { subscribeChannel(it.gameType, it.id) },
            onDetachMatch = { unSubscribeChannel(it.gameType, it.id) }
        )
    }

    protected val esportTypeAdapter by lazy { ESportTypeAdapter(::onESportTypeChanged) }
    var currentCategoryItem :CategoryItem? =null

    override fun onInitView(view: View) {
        binding.gameList.itemAnimator = null
        super.onInitView(view)
        binding.sportTypeList.isVisible =true
        //电竞主题背景增加
        binding.sportTypeList.setBackgroundResource(R.drawable.bg_esport_game)
        binding.sportTypeList.elevation = 1.dp.toFloat()
        binding.linOpt.setBackgroundResource(R.drawable.bg_white_alpha70_radius_8_top)
        binding.gameList.setBackgroundResource(R.color.color_FFFFFF)
    }


    // 该方法中不要引用与生命周期有关的(比如：ViewModel、Activity)
    private fun reset() {
        esportTypeAdapter.setNewInstance(null)
        binding.linOpt.gone()
        clearData()
        setMatchInfo("", "")
        clearSubscribeChannels()
        setupSportTypeList()
        setupToolbarStatus()
        currentItem = null
        currentCategoryItem = null
    }

    open fun reload(matchType: MatchType, gameType: String?) {
        this.matchType = matchType
        this.gameType = gameType ?: GameType.ES.key
        sportLeagueAdapter2.matchType = this.matchType
        reset()
        scrollBackTop()
        binding.appbarLayout.scrollBy(0, 0)
        showLoading()
        getMenuDataByParent(true)
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        observeSportList()
        initSocketObserver()
        esportTypeAdapter.applyEventView(requireActivity())
    }

    override fun onInitData() {
        reload((arguments?.getSerializable("matchType") as MatchType?) ?: MatchType.IN_PLAY, arguments?.getString("gameType"))
    }

    open fun setupSportTypeList() {
        val visiable = matchType != MatchType.CS //波胆不需要显示球类
        binding.sportTypeList.isVisible = visiable
        binding.sportTypeList.adapter = esportTypeAdapter
    }

    private fun setupToolbarStatus() = binding.run {
        ivArrow.isSelected = false
        ivArrow.rotationAnimation(0f, 0)
    }

    private inline fun BaseNode.isMatchOdd() =  this is org.cxct.sportlottery.network.odds.list.MatchOdd
    protected open fun observeSportList() = viewModel.run {

        oddsListGameHallResult.observe(this@ESportListFragment.viewLifecycleOwner) {
            val oddsListData = it.getContentIfNotHandled()?.oddsListData ?: return@observe
            dismissLoading()
            val leagueOdds: List<LeagueOdd>? = oddsListData.leagueOdds
            if (leagueOdds.isNullOrEmpty()) {
                return@observe
            }
            val mLeagueOddList = (oddsListData.leagueOddsFilter ?: leagueOdds).toMutableList()
            setSportDataList(mLeagueOddList as MutableList<BaseNode>)
        }
    }

    override fun observerMenuData() {
        viewModel.esportTypeMenuData.observe(this@ESportListFragment.viewLifecycleOwner){
            it.first?.let {
                updateESportType(it)
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

    override fun onBetInfoChanged(betInfoList: List<BetInfoListData>) {
        if (sportLeagueAdapter2.dataCount() < 1) {
            return
        }
        sportLeagueAdapter2.updateOddsSelectStatus()
    }

    override fun onOddTypeChanged(oddsType: OddsType) {
        sportLeagueAdapter2.oddsType = oddsType
    }

    override fun onFavorite(favoriteIds: Set<String>) {
        if (sportLeagueAdapter2.getCount() < 1) {
            return
        }

        sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
            if (baseNode is org.cxct.sportlottery.network.odds.list.MatchOdd) {
                baseNode.matchInfo?.let {
                    val isFavorited = favoriteIds.contains(it.id)
                    if (it.isFavorite != isFavorited) {
                        it.isFavorite = isFavorited
                        sportLeagueAdapter2.notifyMatchItemChanged(index, SportMatchEvent.FavoriteChanged)
                    }
                }
            }
        }
    }


    private fun initSocketObserver() {

        MatchOddsRepository.observerMatchStatus(this) {
            val matchId = it?.matchStatusCO?.matchId ?: return@observerMatchStatus
            val isFinished = it.matchStatusCO?.status == GameMatchStatus.FINISH.value

            if (isFinished) {
                sportLeagueAdapter2.removeMatchOdd(matchId)
            } else {
                val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observerMatchStatus
                if (SocketUpdateUtil.updateMatchStatus(matchOdd.matchInfo?.gameType, matchOdd, it, context)) {
                    sportLeagueAdapter2.matchStatuChanged(matchOdd)
                }
            }
        }

        receiver.matchClock.observe(this@ESportListFragment.viewLifecycleOwner) { event->
            val matchId =  event?.matchId ?: return@observe
            if (sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }
            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@observe
            matchOdd.matchInfo?.let { matchInfo->
                if (SocketUpdateUtil.updateMatchClockStatus(matchInfo, event)) {
                    sportLeagueAdapter2.matchStatuChanged(matchOdd)
                }
            }
        }

        receiver.matchOddsLock.collectWith(lifecycleScope) { event->
            val matchId =  event?.matchId ?: return@collectWith
            if (matchId == null || sportLeagueAdapter2.getCount() < 1) {
                return@collectWith
            }

            val matchOdd = sportLeagueAdapter2.findVisiableRangeMatchOdd(matchId) ?: return@collectWith
            if (SocketUpdateUtil.updateOddStatus(matchOdd, event)) {
                sportLeagueAdapter2.notifyMatchOddChanged(matchOdd)
            }
        }

        receiver.globalStop.observe(this@ESportListFragment.viewLifecycleOwner) { event->
            if (event == null || sportLeagueAdapter2.getCount() < 1) {
                return@observe
            }

            sportLeagueAdapter2.data.forEachIndexed { index, baseNode ->
                if (baseNode.isMatchOdd() && SocketUpdateUtil.updateOddStatus(baseNode as MatchOdd, event)) {
                    //暫時不處理 防止過多更新
                    sportLeagueAdapter2.notifyItemChanged(index, baseNode)
                }
            }
        }

        receiver.closePlayCate.observe(this@ESportListFragment.viewLifecycleOwner) { event ->
            val closeEvent = event?.peekContent() ?: return@observe
            if (gameType == closeEvent.gameType) {
                sportLeagueAdapter2.closePlayCate(closeEvent)
            }
        }

    }

    override fun oddClick(
        matchInfo: MatchInfo,
        odd: Odd,
        playCateCode: String,
        betPlayCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        view: View
    ) {
        addOddsDialog(matchInfo, odd, playCateCode,betPlayCateName, betPlayCateNameMap)
    }

    override fun onResume() {
        super.onResume()
        resubscribeMatch()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            resubscribeMatch()
        }
    }

    override fun onScrollStopped() { }
    override fun onStartScroll() { }
    override fun resubscribeChannel(delay: Long) { }

    private fun resubscribeMatch() {
        clearSubscribeChannels()
        sportLeagueAdapter2.recodeRangeMatchOdd().forEach { matchOdd->
            matchOdd.matchInfo?.let { subscribeChannel(it.gameType, it.id) }
        }
    }

    open fun updateESportType(item: Item) {

        val categoryList = item.categoryList
        if (categoryList.isNullOrEmpty()) {
            setSportDataList(null)
            dismissLoading()
            return
        }

        currentItem = item
        //处理默认不选中的情况
        var targetItem: CategoryItem? = null
        categoryList.forEach { it.isSelected = false }
        binding.sportTypeList.show()
        targetItem = categoryList.find { it.code == gameType } ?: categoryList.first()
        if (currentCategoryItem == null){
            currentCategoryItem = targetItem
            currentCategoryItem!!.isSelected = true
            load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
        } else {
            val existItem = categoryList.firstOrNull { it.code == currentCategoryItem!!.code }
            currentCategoryItem = existItem ?: targetItem
            currentCategoryItem?.isSelected = true
            if (existItem!=currentCategoryItem) {
                load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
            }
        }
        esportTypeAdapter.setNewInstance(item.categoryList)
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            esportTypeAdapter.data.indexOfFirst { it.isSelected })

    }

    override fun setSelectMatch(leagueIdList: ArrayList<String>,matchIdList: ArrayList<String>) {
        esportTypeAdapter.currentItem?.let {
            clearData()
            currentItem?.let { it1 ->
                load(it1, leagueIdList,matchIdList,it.categoryCodeList)
            }
        }
    }

   open fun onESportTypeChanged(item: CategoryItem, position: Int){
        currentCategoryItem = item
        clearData()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        currentItem?.let { load(it, categoryCodeList = item.categoryCodeList) }
    }
}