package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.item_play_spinner.viewDivider
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.adapter.RecyclerGameListAdapter
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.onClick

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding
//    private val gameAllAdapter by lazy {
//        GameCategroyAdapter(
//            clickCollect = ::onCollectClick,
//            clickGame = ::enterGame, okGamesFragment().gameItemViewPool
//        )
//    }
    private val gameListAdapter= RecyclerGameListAdapter()
//    private var collectGameAdapter: GameChildAdapter? = null
    private val providersAdapter by lazy { OkGameProvidersAdapter() }
    private var categoryList = mutableListOf<OKGamesCategory>()

    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3

    private var lastRequestTimeStamp = 0L


    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentAllOkgamesBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        unSubscribeChannelHallAll()
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initSportObserve()
        //初始化热门赛事
        binding.hotMatchView.onCreate(viewModel.publicityRecommend,viewModel.oddsType,this)
        viewModel.getRecommend()
    }

    private fun initSportObserve(){
        //体育服务开关监听
        setupSportStatusChange(this){
            binding.hotMatchView.goneWithSportSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        if (okGamesFragment().activity is MainTabActivity
            && okGamesFragment().getCurrentFragment() == this
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
        val noData = okGamesFragment().viewModel.gameHall.value == null
        val time = System.currentTimeMillis()
        if (noData || time - lastRequestTimeStamp > 60_000) { // 避免短时间重复请求
            lastRequestTimeStamp = time
            okGamesFragment().viewModel.getOKGamesHall()
        }
        binding.winsRankView.loadData()
    }

    private fun initObserve() = okGamesFragment().viewModel.run {
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
                    return@observe
                }
                binding.gameViewCollect.visible()
                //初始化收藏数据
                binding.gameViewCollect
                    .setIcon(GameTab.TAB_FAVORITES.labelIcon)
                    .setCategoryName(GameTab.TAB_FAVORITES.name)
                    .setListData(it.second)
                    .setOnFavoriteClick {gameBean->
                        okGamesFragment().collectGame(gameBean)
                    }
                    .setOnGameClick {gameBean->
                        enterGame(gameBean)
                    }
                    .setOnMoreClick {
                        okGamesFragment().changeGameTable(GameTab.TAB_FAVORITES)
                    }
            }else{
                binding.gameViewCollect.gone()
            }

        }

        collectOkGamesResult.observe(viewLifecycleOwner) { result ->
            //更新列表
            var tempIndex=0
            gameListAdapter.data.forEachIndexed {index,it->
                 it.gameList?.forEach {
                     if(result.second.id==it.id){
                         it.markCollect=result.second.markCollect
                         tempIndex=index
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
                        okGamesFragment().collectGame(it)
                    }
                    .setOnGameClick {
                        enterGame(it)
                    }
                    .setOnMoreClick {
                        okGamesFragment().changeGameTable(GameTab.TAB_RECENTLY)
                    }
            }else{
                binding.gameViewRecent.gone()
            }
        }

        newRecentPlay.observe(viewLifecycleOwner) { okgameBean ->

        }

    }

    private fun onBindGamesView() = binding.run {
        rvGamesAll.setLinearLayoutManager()
        rvGamesAll.adapter=gameListAdapter
        gameListAdapter.setOnMoreClick {
            okGamesFragment().changeGameTable(it)
        }
        gameListAdapter.setOnGameClick {
            enterGame(it)
        }
        gameListAdapter.setOnFavoriteClick {
            okGamesFragment().collectGame(it)
        }
    }

    private fun onBindPart3View() {
        binding.winsRankView.setUp( this, { viewModel.getOKGamesRecordNew() }, { viewModel.getOKGamesRecordResult() })

        binding.ivProvidersLeft.alpha = 0.5F
        providersAdapter.setOnItemClickListener { _, _, position ->
            okGamesFragment().changePartGames(providersAdapter.getItem(position))
        }

        var okGameProLLM = binding.rvOkgameProviders.setLinearLayoutManager(LinearLayoutManager.HORIZONTAL)
        binding.rvOkgameProviders.adapter = providersAdapter
        binding.rvOkgameProviders.layoutManager = okGameProLLM
        binding.rvOkgameProviders.addOnScrollListener(object : OnScrollListener() {
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

                binding.ivProvidersRight.isClickable = p3ogProviderLastPosi != providersAdapter.data.size - 1
            }
        })

        viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
            val firmList = resultData?.firmList ?: return@observe

            providersAdapter.setNewInstance(firmList.toMutableList())
            if (firmList.isNotEmpty()) {
                binding.run { setViewVisible(rvOkgameProviders, okgameP3LayoutProivder) }
            } else {
                binding.run { setViewGone(rvOkgameProviders, okgameP3LayoutProivder) }
            }

            if (firmList.size > 3) {
                binding.run { setViewVisible(ivProvidersLeft, ivProvidersRight) }
            } else {
                binding.run { setViewGone(ivProvidersLeft, ivProvidersRight) }
            }
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
        receiver.recordNewOkGame.collectWith(lifecycleScope) {
            if (it != null) {
                binding.winsRankView.onNewWSBetData(it)
            }
        }
        receiver.recordResultOkGame.collectWith(lifecycleScope) {
            if (it != null) {
                binding.winsRankView.onNewWSWinsData(it)
            }
        }

        //供应商左滑按钮
        binding.ivProvidersLeft.setOnClickListener {
            if (p3ogProviderFirstPosi >= 3) {
                binding.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderFirstPosi - 2
                )
            } else {
                binding.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkgameProviders, RecyclerView.State(), 0
                )
            }
        }
        //供应商右滑按钮
        binding.ivProvidersRight.setOnClickListener {
            if (p3ogProviderLastPosi < providersAdapter.data.size - 4) {
                binding.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderLastPosi + 2
                )
            } else {
                binding.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.rvOkgameProviders,
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





    private inline fun enterGame(okGameBean: OKGameBean) {
        if(LoginRepository.isLogined()){
            //已登录
            okGamesFragment().enterGame(okGameBean)
        }else{
            //请求试玩路线
            loading()
            viewModel.requestEnterThirdGameNoLogin(okGameBean.firmType,okGameBean.gameCode,okGameBean.thirdGameCategory)
        }
    }



    private fun onCollectClick(view: View, gameData: OKGameBean) {
        if (okGamesFragment().collectGame(gameData)) {
            view.animDuang(1.3f)
        }
    }
}