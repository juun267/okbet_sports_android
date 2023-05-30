package org.cxct.sportlottery.ui.maintab.home


import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.RegisterInfoEvent
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.newInstanceFragment
import org.cxct.sportlottery.databinding.FragmentMainHome2Binding
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.news.HomeNewsAdapter
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.goneWithSportSwitch
import org.cxct.sportlottery.util.setTrialPlayGameDataObserve
import org.cxct.sportlottery.util.setupBackTop
import org.cxct.sportlottery.util.setupSportStatusChange
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.cxct.sportlottery.view.dialog.PopImageDialog

class MainHomeFragment2 : BindingSocketFragment<MainHomeViewModel, FragmentMainHome2Binding>() {

    private fun getMainTabActivity() = activity as MainTabActivity
    private fun getHomeFragment() = parentFragment as HomeFragment

    fun jumpToInplaySport() = getMainTabActivity().jumpToInplaySport()
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()

    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp) {
            if (hotMatchView.isVisible) {
                hotMatchView.firstVisibleRange(this@MainHomeFragment2)
            }
        }
        homeBottumView.bindServiceClick(childFragmentManager)
        binding.winsRankView.setTipsIcon(R.drawable.ic_okgame_p2)
        initToolBar()
        initNews()
        EventBusUtil.targetLifecycle(this@MainHomeFragment2)
    }

    override fun onInitData() {
        viewModel.getHomeNews(1, 5, listOf(NewsRepository.NEWS_OKBET_ID))
        viewModel.getBettingStationList()
        //刷新config
        viewModel.getConfigData()
        viewModel.getAnnouncement()

        //设置监听游戏试玩
        setTrialPlayGameDataObserve()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSportStatusChange(event: SportStatusEvent) {
        checkToCloseView()
    }

    override fun onBindViewStatus(view: View) = binding.run {
        homeTopView.setup(this@MainHomeFragment2)
        hotMatchView.onCreate(viewModel.publicityRecommend, this@MainHomeFragment2)
        okGamesView.setOkGamesData(this@MainHomeFragment2)
        initBetWinsRecodeLayout()
        initObservable()
    }

    private fun initBetWinsRecodeLayout() {
        binding.winsRankView.setUp(
            viewLifecycleOwner,
            { viewModel.getRecordNew() },
            { viewModel.getRecordResult() })
        receiver.recordBetNew.observe(viewLifecycleOwner) {
            it?.let {
                binding.winsRankView.onNewWSBetData(
                    it
                )
            }
        }
        receiver.recordWinsResult.observe(viewLifecycleOwner) {
            it?.let {
                binding.winsRankView.onNewWSWinsData(
                    it
                )
            }
        }//最新大奖
        viewModel.recordBetNewHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpBetData(it.reversed())
            }
        }
        viewModel.recordWinsResultHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpWinsData(it.reversed())
            }
        }
    }

    fun initToolBar() = binding.run {
        homeToolbar.attach(this@MainHomeFragment2, getMainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showMainLeftMenu(null)
        }
    }


    override fun onResume() {
        super.onResume()
        //返回页面时，刷新体育相关view状态
        checkToCloseView()
        if (getMainTabActivity().getCurrentPosition() == 0 && getHomeFragment().getCurrentFragment() == this) {
            refreshHotMatch()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
        } else {
            homeToolbar.onRefreshMoney()
            refreshHotMatch()
            //返回页面时，刷新体育相关view状态
            checkToCloseView()
        }

    }

    private fun initObservable() {
        viewModel.homeNewsList.observe(viewLifecycleOwner) {
            val dataList = if (it.size > 4) it.subList(0, 4) else it
            setupNews(dataList)
        }
        viewModel.bettingStationList.observe(viewLifecycleOwner) {
            setupBettingStation(it)
        }
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            viewModel.getSportMenuFilter()
            if (!PopImageDialog.firstShow || !PopImageDialog.checkImageTypeAvailable(7)) {
                MultiLanguagesApplication.showPromotionPopupDialog(requireActivity())
                return@observe
            }
            requireContext().newInstanceFragment<PopImageDialog>(Bundle().apply {
                putInt(PopImageDialog.IMAGE_TYPE, 7)
            }).apply {
                onDismiss = {
                    MultiLanguagesApplication.showPromotionPopupDialog(requireActivity())
                }
            }.show(childFragmentManager, PopImageDialog::class.simpleName)
        }

        //新版宣傳頁
        viewModel.messageListResult.observe(viewLifecycleOwner) {

            val messageListResult = it.getContentIfNotHandled() ?: return@observe
            val sortMsgList =
                messageListResult.rows?.sortedWith(compareByDescending<Row> { it.sort }.thenByDescending { it.addTime })
            val titleList: MutableList<String> = mutableListOf()
            sortMsgList?.forEach { data ->
                if (data.type.toInt() == 1) {
                    titleList.add(data.title + " - " + data.message)
                }
            }
            setupAnnouncement(titleList)
        }
    }
    //hot match
    private fun refreshHotMatch(){

        //重新设置赔率监听
        binding.hotMatchView.postDelayed({
            binding.hotMatchView.onResume(this@MainHomeFragment2)
            viewModel.getRecommend()
        },500)
    }

    /**
     * 检查体育服务状态
     */
    private fun checkToCloseView(){
        context?.let {
            //关闭/显示 sports入口
            binding.homeTopView.initSportEnterStatus()
            //关闭/显示   热门赛事
            binding.hotMatchView.goneWithSportSwitch()
            if(binding.hotMatchView.isVisible){
                viewModel.getRecommend()
            }
        }
    }
    //hot match end
    private fun initNews() {
        binding.includeNews.apply {
            tabNews.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val categoryId =
                        if (tab?.position == 0) NewsRepository.NEWS_OKBET_ID else NewsRepository.NEWS_SPORT_ID
                    viewModel.getHomeNews(1, 5, listOf(categoryId))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
            linTab.setOnClickListener {
                getHomeFragment().jumpToNews()
            }
        }
    }

    private fun setupNews(newsList: List<NewsItem>) {
        binding.includeNews.apply {
            if (rvNews.adapter == null) {
                rvNews.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                rvNews.adapter = HomeNewsAdapter().apply {
                    setList(newsList)
                    setOnItemClickListener(listener = OnItemClickListener { adapter, view, position ->
                        NewsDetailActivity.start(
                            requireContext(),
                            (adapter.data[position] as NewsItem)
                        )
                    })
                }
            } else {
                (rvNews.adapter as HomeNewsAdapter).setList(newsList)
            }
        }
    }

    private fun setupBettingStation(newsList: List<BettingStation>) {
        binding.includeBettingStation.apply {
            if (rvBettingStation.adapter == null) {
                rvBettingStation.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                rvBettingStation.addItemDecoration(
                    SpaceItemDecoration(
                        requireContext(),
                        R.dimen.margin_10
                    )
                )
                PagerSnapHelper().attachToRecyclerView(rvBettingStation)
                rvBettingStation.adapter = HomeBettingStationAdapter().apply {
                    setList(newsList)
                    setOnItemChildClickListener { adapter, view, position ->
                        val data = (adapter as HomeBettingStationAdapter).data[position]
                        JumpUtil.toExternalWeb(
                            requireContext(),
                            "https://maps.google.com/?q=@" + data.lat + "," + data.lon
                        )
                    }
                }
            } else {
                (rvBettingStation.adapter as HomeBettingStationAdapter).setList(newsList)
            }
        }
    }

    private fun setupAnnouncement(titleList: List<String>) {
        if (titleList.isEmpty()) {
            lin_announcement.visibility = View.GONE
        } else {
            lin_announcement.visibility = View.VISIBLE
            var marqueeAdapter = MarqueeAdapter()
            lin_announcement.setOnClickListener {
                startActivity(Intent(requireActivity(), NewsActivity::class.java))
            }
            rv_marquee.apply {
                layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = marqueeAdapter
            }

            marqueeAdapter.setData(titleList.toMutableList())
            if (titleList.isNotEmpty()) {
                rv_marquee.startAuto(false) //啟動跑馬燈
            } else {
                rv_marquee.stopAuto(true) //停止跑馬燈
            }
        }

    }

}