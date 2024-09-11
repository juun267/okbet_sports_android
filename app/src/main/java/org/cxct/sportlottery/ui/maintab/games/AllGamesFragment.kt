package org.cxct.sportlottery.ui.maintab.games

import android.view.View
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.service.dispatcher.DataResourceChange
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerGameListAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.*

// OkGames所有分类
class AllGamesFragment : BaseSocketFragment<OKGamesViewModel,FragmentAllOkgamesBinding>() {

    private val gameListAdapter= RecyclerGameListAdapter()
    private var categoryList = mutableListOf<OKGamesCategory>()


    private fun okGamesFragment() = parentFragment as OKGamesFragment
    private fun getMainTabActivity() = activity as MainTabActivity

    override fun onInitView(view: View) {
        unSubscribeChannelHallAll()
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initSportObserve()
        //初始化热门赛事
        initHotMatchView()
        observerGameMaintain()
        viewModel.getRecommend()
    }

    private fun observerGameMaintain() {
        gameListAdapter.bindLifecycleOwner(this)
        binding.gameViewCollect.bindLifecycleOwner(this)
        binding.gameViewRecent.bindLifecycleOwner(this)
    }

    private fun initHotMatchView() {
        binding.hotMatchView.onCreate("OKGame页面", viewModel.publicityRecommend,viewModel.oddsType,this)
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.hotMatchView.resubscribe()
        }
    }

    private fun initSportObserve(){
        //体育服务开关监听
        setupSportStatusChange(this){
            binding.hotMatchView.goneWithSportSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        if (getMainTabActivity().getCurrentFragment() == okGamesFragment()
            && okGamesFragment().getCurrentFragment() == this) {
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
        okGamesFragment().viewModel.getOKGamesHall()
        binding.winsRankView.loadData()
    }

    private fun initObserve() = okGamesFragment().viewModel.run {
        DataResourceChange.observe(viewLifecycleOwner) { getRecommend() }
        gameHall.observe(viewLifecycleOwner) {
            categoryList = it.categoryList?.filter {category->
                !category.gameList.isNullOrEmpty()
            }?.toMutableList() ?: mutableListOf()
            gameListAdapter.setList(categoryList)
            viewModel.getRecentPlay()
        }

        GameCollectManager.collectGameList.observe(viewLifecycleOwner) {
            if(LoginRepository.isLogined()){
                if(it.isNullOrEmpty()){
                    binding.gameViewCollect.gone()
                    return@observe
                }
                binding.gameViewCollect.visible()
                //初始化收藏数据
                binding.gameViewCollect
                    .setIcon(GameTab.TAB_FAVORITES.labelIcon)
                    .setCategoryName(GameTab.TAB_FAVORITES.name)
                    .setListData(it)
                    .setOnFavoriteClick {gameBean->
                        okGamesFragment().collectGame(gameBean)
                    }
                    .setOnGameClick {gameBean->
                        enterGame(gameBean, "Games-收藏游戏列表")
                    }
                    .setOnMoreClick {
                        okGamesFragment().changeGameTable(GameTab.TAB_FAVORITES)
                    }
            }else{
                binding.gameViewCollect.gone()
            }
        }
        GameCollectManager.observerGameCollect(viewLifecycleOwner) { result ->
            //更新列表
            gameListAdapter.data.forEachIndexed {index,it->
                it.gameList?.forEach {
                    if(result.first==it.id){
                        it.markCollect=result.second
                        return@forEachIndexed
                    }
                }
            }
            gameListAdapter.notifyDataSetChanged()
            //更新最近游戏
            binding.gameViewRecent.getDataList().forEach {

                it.forEach {
                    if(result.first==it.id){
                        it.markCollect=result.second
                        return@forEach
                    }
                }
            }
            binding.gameViewRecent.notifyDataChange()
        }
        GameCollectManager.gameCollectNum.observe(viewLifecycleOwner) {
            binding.gameViewRecent.notifyDataChange()
            gameListAdapter.notifyDataSetChanged()
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
                        okGamesFragment().collectGame(it)
                    }
                    .setOnGameClick {
                        enterGame(it, "Games-最近游戏列表")
                    }
                    .setOnMoreClick {
                        okGamesFragment().changeGameTable(GameTab.TAB_RECENTLY)
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
            okGamesFragment().changeGameTable(it)
        }
        gameListAdapter.setOnGameClick {
            enterGame(it, "Games-游戏列表")
        }
        gameListAdapter.setOnFavoriteClick {
            okGamesFragment().collectGame(it)
        }
    }

    private fun onBindPart3View() {
        binding.winsRankView.setUp( this, { viewModel.getOKGamesRecordNew() }, { viewModel.getOKGamesRecordResult() })

        viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
            val firmList = resultData?.firmList ?: return@observe
            okGamesFragment().setupProvider(firmList.toMutableList())
        }

        viewModel.recordNewBetHttpOkGame.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpBetData(it.reversed())
            }
        }
        viewModel.recordResultWinsHttpOkGame.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpWinsData(it.reversed())
            }
        }

    }

    private fun onBindPart5View() {
       binding.bottomView.bindServiceClick(childFragmentManager)
    }

    private inline fun enterGame(okGameBean: OKGameBean, from: String?) {
        getMainTabActivity().enterThirdGame(okGameBean, from)
    }

    private fun onCollectClick(view: View, gameData: OKGameBean) {
        if (okGamesFragment().collectGame(gameData)) {
            view.animDuang(1.3f)
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