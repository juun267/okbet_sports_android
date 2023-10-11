package org.cxct.sportlottery.ui.maintab.home


import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.include_home_bettingstation.*
import kotlinx.android.synthetic.main.include_home_news.*
import kotlinx.android.synthetic.main.item_sport_news.view.*
import kotlinx.android.synthetic.main.view_hot_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.newInstanceFragment
import org.cxct.sportlottery.databinding.FragmentMainHomeBinding
import org.cxct.sportlottery.net.news.NewsRepository
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.login.signUp.RegisterSuccessDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.news.HomeNewsAdapter
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainHomeFragment : BindingSocketFragment<MainHomeViewModel, FragmentMainHomeBinding>() {

    private fun getMainTabActivity() = activity as MainTabActivity
    private fun getHomeFragment() = parentFragment as HomeFragment

    fun jumpToInplaySport() = getMainTabActivity().jumpToInplaySport()
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()
    fun jumpToOKLive() = getMainTabActivity().jumpToOkLive()

    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp) {
            if (hotMatchView.isVisible) {
                hotMatchView.resubscribe()
            }
        }
        homeBottumView.bindServiceClick(childFragmentManager)
        binding.winsRankView.setTipsIcon(R.drawable.ic_okgame_p2)
        initToolBar()
        initNews()
        EventBusUtil.targetLifecycle(this@MainHomeFragment)
        ToGcashDialog.showByLogin(viewModel)
        setHalloweenStyle()
    }

    override fun onInitData() {
        viewModel.getHomeNews(1, 5, listOf(NewsRepository.NEWS_OKBET_ID))
        viewModel.getBettingStationList()
        //刷新config
        viewModel.getConfigData()

        //设置监听游戏试玩
        setTrialPlayGameDataObserve()
        viewModel.getAnnouncement()
    }

    private fun setHalloweenStyle() = binding.run {
        hotMatchView.ivHotMatch.setImageResource(R.drawable.ic_hot_match_title_h)
        includeNews.ivNews.setImageResource(R.drawable.ic_cate_news_h)
        winsRankView.setHalloweenStyle()
        includeBettingStation.ivBetStation.setImageResource(R.drawable.ic_home_bettingstation_h)
        homeBottumView.setHalloweenStyle()
        homeTopView.setHalloweenStyle()
        hotMatchView.setHalloweenStyle()
    }

    private fun setHalloweenStyle2() = binding.run {
        gameViewOkGame.setPadding(12.dp, 0, 2.dp, 0)
        gameViewOkGame.setIcon(R.drawable.ic_home_okgames_title_h)
        gameViewOkGame.setBackgroundResource(R.drawable.bg_halloween_part5)
        (gameViewOkGame.layoutParams as MarginLayoutParams).topMargin = -10.dp
        gameViewOkLive.setPadding(12.dp, 13.dp, 2.dp, 0)
        gameViewOkLive.setIcon(R.drawable.ic_home_oklive_title_h)
        gameViewOkLive.setBackgroundResource(R.drawable.bg_halloween_part4)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSportStatusChange(event: SportStatusEvent) {
        checkToCloseView()
    }

    override fun onBindViewStatus(view: View) = binding.run {
        homeTopView.setup(this@MainHomeFragment)
        hotMatchView.onCreate(viewModel.publicityRecommend, viewModel.oddsType,this@MainHomeFragment)
//        okGamesView.setOkGamesData(this@MainHomeFragment)
        gameViewOkGame.initOkGames(this@MainHomeFragment)
        gameViewOkGame.bindLifecycleOwner(this@MainHomeFragment)
//        if (StaticData.okLiveOpened()){
            gameViewOkLive.initOkLiveList(this@MainHomeFragment)
            gameViewOkLive.bindLifecycleOwner(this@MainHomeFragment)
//        }
        setHalloweenStyle2()
        initBetWinsRecodeLayout()
        initObservable()
        binding.winsRankView.loadData()
    }

    private fun initBetWinsRecodeLayout() {
        binding.winsRankView.setUp(
            this,
            { viewModel.getBetRecord() },
            { viewModel.getWinRecord() })
        viewModel.recordBetHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpBetData(it.reversed())
            }
        }
        viewModel.recordWinHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpWinsData(it.reversed())
            }
        }
    }

    fun initToolBar() = binding.run {
        homeToolbar.setHalloweenStyle()
        homeToolbar.attach(this@MainHomeFragment, getMainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            getMainTabActivity().showMainLeftMenu(null)
        }
        homeToolbar.tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true,Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
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
        super.onHiddenChanged(hidden)
        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
        } else {
            binding.scrollView.smoothScrollTo(0, 0)
            binding.homeToolbar.onRefreshMoney()
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
            if (PopImageDialog.showHomeDialog) {
                PopImageDialog.showHomeDialog = false
                MultiLanguagesApplication.showPromotionPopupDialog(getMainTabActivity()){}
                if (PopImageDialog.checkImageTypeAvailable(ImageType.DIALOG_HOME.code)) {
                    requireContext().newInstanceFragment<PopImageDialog>(Bundle().apply {
                        putInt(PopImageDialog.IMAGE_TYPE, ImageType.DIALOG_HOME.code)
                    }).show(childFragmentManager, PopImageDialog::class.simpleName)
                }
            }
            if (viewModel.isLogin.value==true&&RegisterSuccessDialog.ifNew){
                RegisterSuccessDialog.ifNew=false
                RegisterSuccessDialog{
                    viewModel.checkRechargeKYCVerify()
                }.show(parentFragmentManager,RegisterSuccessDialog::class.simpleName)
            }
        }
        //体育服务开关监听
//        receiver.sportMaintenance.observe(this){
//            it?.let {
//                sConfigData?.sportMaintainStatus="${it.status}"
//                binding.homeTopView.initSportEnterStatus()
//            }
//        }
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
    private fun refreshHotMatch() {
        binding.hotMatchView.onResume(this@MainHomeFragment)
        viewModel.getRecommend()
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
            //判断当前fragment是否可见
            if(binding.hotMatchView.isVisible&&isVisibleToUser()){
                viewModel.getRecommend()
            }
        }
    }

    //hot match end
    private fun initNews() {
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

    private fun setupNews(newsList: List<NewsItem>) {
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

    private fun setupBettingStation(newsList: List<BettingStation>) {
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

    private fun setupAnnouncement(titleList: List<String>) {
        val lin_announcement = binding.homeTopView.binding.linAnnouncement
        if (titleList.isEmpty()) {
            lin_announcement.visibility = View.GONE
        } else {
            lin_announcement.visibility = View.VISIBLE
            var marqueeAdapter = MarqueeAdapter()
            lin_announcement.setOnClickListener {
                startActivity(Intent(requireActivity(), NewsActivity::class.java))
            }

            val rv_marquee = binding.homeTopView.binding.rvMarquee
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