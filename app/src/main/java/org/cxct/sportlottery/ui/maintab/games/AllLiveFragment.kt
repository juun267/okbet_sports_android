package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentAllOkliveBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerLiveListAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.goneWithSportSwitch
import org.cxct.sportlottery.util.setupSportStatusChange

// OkGames所有分类
class AllLiveFragment : BaseSocketFragment<OKLiveViewModel,FragmentAllOkliveBinding>() {
    private val gameListAdapter= RecyclerLiveListAdapter()
    private fun getMainTabActivity() = activity as MainTabActivity
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()
    private var categoryList = mutableListOf<OKGamesCategory>()

    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3

    private var lastRequestTimeStamp = 0L


    private fun okLiveFragment() = parentFragment as OKLiveFragment

    override fun onInitView(view: View) {
        unSubscribeChannelHallAll()
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initSportObserve()
        //初始化热门赛事
        initHotMatchView()
//        binding.okLiveGameView.initOkLiveGames(this)
        initRecommendLiveGame()
        viewModel.getRecommend()
        observerGameMaintain()
    }

    private fun observerGameMaintain() {
        gameListAdapter.bindLifecycleOwner(this)
        binding.gameViewCollect.bindLifecycleOwner(this)
        binding.gameViewRecent.bindLifecycleOwner(this)
    }

    private fun initHotMatchView() {
        binding.hotMatchView.onCreate(viewModel.publicityRecommend,viewModel.oddsType,this)
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.hotMatchView.resubscribe()
        }
    }

    private fun initSportObserve() {
        //体育服务开关监听
        setupSportStatusChange(this) {
            binding.hotMatchView.goneWithSportSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        if (getMainTabActivity().getCurrentFragment() == okLiveFragment()
            && okLiveFragment().getCurrentFragment() == this) {
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
        okLiveFragment().viewModel.getOKLiveHall()
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
        viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
            val firmList = resultData?.firmList ?: return@observe
            okLiveFragment().setupProvider(firmList.toMutableList())
        }
        collectList.observe(viewLifecycleOwner) {
            if(LoginRepository.isLogined()){
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
            if(LoginRepository.isLogined()){
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
    }

    private fun onBindPart5View() {
        binding.bottomView.bindServiceClick(childFragmentManager)
    }


    private inline fun enterGame(bean: OKGameBean) {
        okLiveFragment().enterGame(bean)
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
    override fun onVisibleExceptFirst() {
        super.onVisibleExceptFirst()
        binding.winsRankView.startLoopCall()
    }
    override fun onInvisible() {
        super.onInvisible()
        binding.winsRankView.stopLoopCall()
    }
}