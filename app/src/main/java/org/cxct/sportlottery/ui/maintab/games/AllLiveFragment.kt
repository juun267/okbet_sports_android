package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentAllOkliveBinding
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerGameListAdapter
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerLiveListAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.setTrialPlayGameDataObserve
import org.cxct.sportlottery.util.goneWithSportSwitch
import org.cxct.sportlottery.util.setupSportStatusChange
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

// OkGames所有分类
class AllLiveFragment : BaseBottomNavigationFragment<OKLiveViewModel>(OKLiveViewModel::class) {
    private val gameListAdapter= RecyclerLiveListAdapter()
    private fun getMainTabActivity() = activity as MainTabActivity
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()
    private lateinit var binding: FragmentAllOkliveBinding
    private val providersAdapter by lazy { OkGameProvidersAdapter() }
    private var categoryList = mutableListOf<OKGamesCategory>()

    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3

    private var lastRequestTimeStamp = 0L


    private fun okLiveFragment() = parentFragment as OKLiveFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentAllOkliveBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        unSubscribeChannelHallAll()
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initSportObserve()
        //初始化热门赛事
        binding.hotMatchView.onCreate(viewModel.publicityRecommend, viewModel.oddsType, this)
//        binding.okLiveGameView.initOkLiveGames(this)
        initRecommendLiveGame()
        viewModel.getRecommend()
    }

    private fun initSportObserve() {
        //体育服务开关监听
        setupSportStatusChange(this) {
            binding.hotMatchView.goneWithSportSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((activity as MainTabActivity).getCurrentPosition() == 0
            && (okLiveFragment().parentFragment as HomeFragment).getCurrentFragment() == okLiveFragment()
            && okLiveFragment().getCurrentFragment() == this
        ) {
            unSubscribeChannelHallAll()
            //重新设置赔率监听
            binding.hotMatchView.onResume(this)
            viewModel.publicityRecommend.value?.peekContent()?.let {
                it.forEach {
                    subscribeChannelHall(it.gameType, it.id)
                }
            }
            binding.winsRankView.loadData()
        }
        //请求热门赛事数据  在hotMatchView初始化之后
//        viewModel.getRecommend()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
            return
        }
        //重新设置赔率监听
        binding.hotMatchView.onResume(this)
        viewModel.getRecommend()
        val noData = okLiveFragment().viewModel.gameHall.value == null
        val time = System.currentTimeMillis()
        if (noData || time - lastRequestTimeStamp > 60_000) { // 避免短时间重复请求
            lastRequestTimeStamp = time
            okLiveFragment().viewModel.getOKGamesHall()

        }
        binding.winsRankView.loadData()
    }

    private fun initObserve() = okLiveFragment().viewModel.run {
        gameHall.observe(viewLifecycleOwner) {
            categoryList = it.categoryList?.filter {category->
                !category.gameList.isNullOrEmpty()
            }?.toMutableList() ?: mutableListOf()
            gameListAdapter.setList(categoryList)
            viewModel.getRecentPlay()
        }

        collectList.observe(viewLifecycleOwner) {
            if(viewModel.loginRepository.isLogined()){
                if(it.second.isNullOrEmpty()){
                    binding.gameViewCollect.gone()
                    return@observe
                }
                binding.gameViewCollect.visible()
                //初始化收藏数据
                binding.gameViewCollect
                    .setIcon(GameTab.TAB_FAVORITES.labelIcon)
                    .setCategoryName(GameTab.TAB_FAVORITES.name)
                    .setListData(it.second)
                    .setOnFavoriteClick {gameBean->
                        okLiveFragment().collectGame(gameBean)
                    }
                    .setOnGameClick {gameBean->
                        enterGame(gameBean)
                    }
                    .setOnMoreClick {
                        okLiveFragment().changeGameTable(GameTab.TAB_FAVORITES)
                    }
            }else{
                binding.gameViewCollect.gone()
            }
        }

        collectOkGamesResult.observe(viewLifecycleOwner) { result ->
            //更新列表
            gameListAdapter.data.forEachIndexed {index,it->
                it.gameList?.forEach {
                    if(result.second.id==it.id){
                        it.markCollect=result.second.markCollect
                        return@forEachIndexed
                    }
                }
            }
            gameListAdapter.notifyDataSetChanged()
            //更新最近游戏
            binding.gameViewRecent.getDataList().forEach {
                it.forEach {
                    if(result.second.id==it.id){
                        it.markCollect=result.second.markCollect
                        return@forEach
                    }
                }
            }
            binding.gameViewRecent.notifyDataChange()
        }

        recentPlay.observe(viewLifecycleOwner) {list->
            if(list.isNullOrEmpty()){
                return@observe
            }
            if(viewModel.loginRepository.isLogined()){
                binding.gameViewRecent.visible()
                //初始化最近游戏数据
                binding.gameViewRecent
                    .setIcon(GameTab.TAB_RECENTLY.labelIcon)
                    .setCategoryName(GameTab.TAB_RECENTLY.name)
                    .setListData(list)
                    .setOnFavoriteClick {
                        okLiveFragment().collectGame(it)
                    }
                    .setOnGameClick {
                        enterGame(it)
                    }
                    .setOnMoreClick {
                        okLiveFragment().changeGameTable(GameTab.TAB_RECENTLY)
                    }
            }else{
                binding.gameViewRecent.gone()
            }
        }

    }

    private fun onBindGamesView() = binding.run {
        rvGamesAll.setLinearLayoutManager()
        rvGamesAll.adapter=gameListAdapter
        gameListAdapter.setOnMoreClick {
            okLiveFragment().changeGameTable(it)
        }
        gameListAdapter.setOnGameClick {
            enterGame(it)
        }
        gameListAdapter.setOnFavoriteClick {
            okLiveFragment().collectGame(it)
        }
    }

    private fun onBindPart3View() {
        binding.winsRankView.setUp(
            this,
            { viewModel.getOKGamesRecordNew() },
            { viewModel.getOKGamesRecordResult() })

        binding.ivProvidersLeft.alpha = 0.5F
        providersAdapter.setOnItemClickListener { _, _, position ->
            okLiveFragment().changePartGames(providersAdapter.getItem(position))
        }

        var okGameProLLM =
            binding.rvOkLiveProviders.setLinearLayoutManager(LinearLayoutManager.HORIZONTAL)
        binding.rvOkLiveProviders.adapter = providersAdapter
        binding.rvOkLiveProviders.layoutManager = okGameProLLM
        binding.rvOkLiveProviders.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(rvView: RecyclerView, newState: Int) {
                // 获取当前滚动到的条目位置
                p3ogProviderFirstPosi = okGameProLLM.findFirstVisibleItemPosition()
                p3ogProviderLastPosi = okGameProLLM.findLastVisibleItemPosition()
                binding.ivProvidersLeft.isClickable = p3ogProviderFirstPosi > 0

                if (p3ogProviderFirstPosi > 0) {
                    binding.ivProvidersLeft.alpha = 1F
                } else {
                    binding.ivProvidersLeft.alpha = 0.5F
                }
                if (p3ogProviderLastPosi == providersAdapter.data.size - 1) {
                    binding.ivProvidersRight.alpha = 0.5F
                } else {
                    binding.ivProvidersRight.alpha = 1F
                }

                binding.ivProvidersRight.isClickable =
                    p3ogProviderLastPosi != providersAdapter.data.size - 1
            }
        })

        viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
            val firmList = resultData?.firmList ?: return@observe
            if (firmList.size < 2) {
                binding.rvOkLiveProviders.isGone = true
                binding.okLiveP3LayoutProvider.isGone = true
                return@observe
            }
            binding.rvOkLiveProviders.isVisible = true
            binding.okLiveP3LayoutProvider.isVisible = true
            providersAdapter.setNewInstance(firmList.toMutableList())
            if (firmList.isNotEmpty()) {
                binding.run { setViewVisible(rvOkLiveProviders, okLiveP3LayoutProvider) }
            } else {
                binding.run { setViewGone(rvOkLiveProviders, okLiveP3LayoutProvider) }
            }

            if (firmList.size > 3) {
                binding.run { setViewVisible(ivProvidersLeft, ivProvidersRight) }
            } else {
                binding.run { setViewGone(ivProvidersLeft, ivProvidersRight) }
            }
        }

        viewModel.recordNewBetHttpOkLive.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpBetData(it.reversed())
            }
        }
        viewModel.recordResultWinsHttpOkLive.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpWinsData(it.reversed())
            }
        }
        receiver.recordNewOkLive.collectWith(lifecycleScope) {
            if (it != null) {
                binding.winsRankView.onNewWSBetData(it)
            }
        }
        receiver.recordResultOkLive.collectWith(lifecycleScope) {
            if (it != null) {
                binding.winsRankView.onNewWSWinsData(it)
            }
        }

        //供应商左滑按钮
        binding.ivProvidersLeft.setOnClickListener {
            if (p3ogProviderFirstPosi >= 3) {
                binding.rvOkLiveProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkLiveProviders,
                    RecyclerView.State(),
                    p3ogProviderFirstPosi - 2
                )
            } else {
                binding.rvOkLiveProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkLiveProviders, RecyclerView.State(), 0
                )
            }
        }
        //供应商右滑按钮
        binding.ivProvidersRight.setOnClickListener {
            if (p3ogProviderLastPosi < providersAdapter.data.size - 4) {
                binding.rvOkLiveProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkLiveProviders,
                    RecyclerView.State(),
                    p3ogProviderLastPosi + 2
                )
            } else {
                binding.rvOkLiveProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkLiveProviders,
                    RecyclerView.State(),
                    providersAdapter.data.size - 1
                )
            }
        }
        //设置监听游戏试玩
        setTrialPlayGameDataObserve()
    }

    private fun onBindPart5View() {
        binding.homeBottumView.bindServiceClick(childFragmentManager)
    }


    private inline fun enterGame(bean: OKGameBean) {
        if (LoginRepository.isLogined()) {
            //已登录
            okLiveFragment().enterGame(bean)
        } else {
            //请求试玩路线
            loading()
            viewModel.requestEnterThirdGameNoLogin(bean.firmType,bean.gameCode,bean.thirdGameCategory)
        }
    }


    private fun initRecommendLiveGame(){
        viewModel.getHomeOKGamesList300()
        viewModel.homeGamesList300.observe(this){
            binding.okLiveGameView.visible()
            //初始化最近游戏数据
            binding.okLiveGameView
                .setIcon(R.drawable.ic_home_okgames_title)
                .setIsShowCollect(false)
                .setMoreGone()
                .setCategoryName(R.string.N704)
                .setListData(it,false)
                .setOnFavoriteClick {
                    okLiveFragment().collectGame(it)
                }
                .setOnGameClick {
                    enterGame(it)
                }
                .setOnMoreClick {
                    okLiveFragment().mainTabActivity().jumpToOKGames()
                }
        }
    }
}