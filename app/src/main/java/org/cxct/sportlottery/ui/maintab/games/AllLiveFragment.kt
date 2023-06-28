package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
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
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.setTrialPlayGameDataObserve
import org.cxct.sportlottery.util.goneWithSportSwitch
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.util.setupSportStatusChange
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

// OkGames所有分类
class AllLiveFragment : BaseBottomNavigationFragment<OKLiveViewModel>(OKLiveViewModel::class) {

    private fun getMainTabActivity() = activity as MainTabActivity
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()
    private lateinit var binding: FragmentAllOkliveBinding
    private val gameAllAdapter by lazy {
        GameCategroyAdapter(
            clickCollect = ::onCollectClick,
            clickGame = ::enterGame, okLiveFragment().gameItemViewPool
        )
    }
    private var collectGameAdapter: GameChildAdapter? = null
    private var recentGameAdapter: GameChildAdapter? = null
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
        initRecent()
        initCollectLayout()
        initSportObserve()
        //初始化热门赛事
        binding.hotMatchView.onCreate(viewModel.publicityRecommend, viewModel.oddsType, this)
        binding.okLiveOkGamesView.setOkGamesData(this)
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
            binding.hotMatchView.postDelayed({
                binding.hotMatchView.onResume(this)
            }, 500)
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
            categoryList = it.categoryList?.filter {
                it.gameList?.let {
                    //最多显示12个
                    if (it.size > 12) it.subList(0, 12)
                }
                !it.gameList.isNullOrEmpty()
            }?.toMutableList() ?: mutableListOf()
            //设置游戏分类
            gameAllAdapter.setList(categoryList)
            viewModel.getRecentPlay()
        }

        collectList.observe(viewLifecycleOwner) {
            if (!it.first && collectGameAdapter?.dataCount() ?: 0 > 0) { //如果当前收藏列表可见，切收藏列表不为空则走全部刷新逻辑（走单挑刷新逻辑）
                return@observe
            }

            val list = it.second
            if (list.isNotEmpty() && list.size > 12) {
                setCollectList(list.subList(0, 12))
            } else {
                setCollectList(list)
            }
        }

        collectOkGamesResult.observe(viewLifecycleOwner) { result ->

            gameAllAdapter.updateMarkCollect(result.second)
            //更新收藏列表
            collectGameAdapter?.let { adapter ->
                //添加收藏或者移除
                adapter.removeOrAdd(result.second)
                binding.includeLiveAll.inclueCollect.root.isGone = adapter.data.isNullOrEmpty()
                setItemMoreVisiable(binding.includeLiveAll.inclueCollect, adapter.dataCount() > 3)
            }
            //更新最近列表
            recentGameAdapter?.data?.forEachIndexed { index, okGameBean ->
                if (okGameBean.id == result.first) {
                    okGameBean.markCollect = result.second.markCollect
                    recentGameAdapter?.notifyItemChanged(index, okGameBean)
                }
            }
        }

        recentPlay.observe(viewLifecycleOwner) {
            if (it.size > 12) {
                setRecent(it.subList(0, 12))
            } else {
                setRecent(it)
            }
        }

        newRecentPlay.observe(viewLifecycleOwner) { okgameBean ->

            recentGameAdapter?.let { adapter ->
                binding.includeLiveAll.inclueRecent.root.visible()
                adapter.data.find { it.id == okgameBean.id }?.let { adapter.remove(it) }
                adapter.addData(0, okgameBean)
                setItemMoreVisiable(binding.includeLiveAll.inclueRecent, adapter.dataCount() > 3)
            }
        }

    }

    private fun onBindGamesView() = binding.includeLiveAll.run {
        rvLiveAll.setLinearLayoutManager()
        rvLiveAll.adapter = gameAllAdapter
        gameAllAdapter.setOnItemChildClickListener { _, _, position ->
            gameAllAdapter.getItem(position).let {
                okLiveFragment().changeGameTable(it)
            }
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


    private fun initCollectLayout() {
        collectGameAdapter =
            bindGameCategroyLayout(GameTab.TAB_FAVORITES, binding.includeLiveAll.inclueCollect)
    }

    private fun initRecent() {
        recentGameAdapter =
            bindGameCategroyLayout(GameTab.TAB_RECENTLY, binding.includeLiveAll.inclueRecent)
    }

    private fun bindGameCategroyLayout(gameTab: GameTab, binding: ItemGameCategroyBinding) =
        binding.run {
            root.gone()
            linCategroyName.setOnClickListener { okLiveFragment().changeGameTable(gameTab) }
            gameTab.bindLabelIcon(ivIcon)
            gameTab.bindLabelName(tvName)
            rvGameItem.setRecycledViewPool(okLiveFragment().gameItemViewPool)
            rvGameItem.layoutManager = SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
            rvGameItem.addItemDecoration(SpaceItemDecoration(root.context, R.dimen.margin_10))
            val gameAdapter = GameChildAdapter(onFavoriate = ::onCollectClick)
            gameAdapter.setOnItemClickListener { _, _, position ->
                enterGame(gameAdapter.getItem(position))
            }

            rvGameItem.adapter = gameAdapter
            return@run gameAdapter
        }

    private inline fun enterGame(bean: OKGameBean) {
        if (LoginRepository.isLogined()) {
            //已登录
            okLiveFragment().enterGame(bean)
        } else {
            //不支持试玩
            loginedRun(requireContext()) {}
        }
    }

    /**
     * 设置收藏游戏列表
     */
    private fun setCollectList(collectList: List<OKGameBean>) {
        val emptyData = collectList.isNullOrEmpty()
        setItemMoreVisiable(binding.includeLiveAll.inclueCollect, collectList.size > 3)
        binding.includeLiveAll.inclueCollect.root.isGone = emptyData
        if (!emptyData) {
            collectGameAdapter?.setNewInstance(collectList?.toMutableList())
        }
    }


    /**
     * 设置最近游戏列表
     */
    private fun setRecent(recentList: List<OKGameBean>) {
        setItemMoreVisiable(binding.includeLiveAll.inclueRecent, recentList.size > 3)
        val emptyData = recentList.isNullOrEmpty()
        binding.includeLiveAll.inclueRecent.root.isGone = emptyData
        if (!emptyData) {
            recentGameAdapter?.setNewInstance(recentList?.toMutableList())
        }
    }

    private fun setItemMoreVisiable(binding: ItemGameCategroyBinding, visisable: Boolean) {
//        binding.ivMore.isVisible = visisable
        binding.tvMore.isVisible = visisable
    }


    private fun onCollectClick(view: View, gameData: OKGameBean) {
        if (okLiveFragment().collectGame(gameData)) {
            view.animDuang(1.3f)
        }
    }
}