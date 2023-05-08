package org.cxct.sportlottery.ui.maintab.home


import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentMainHome2Binding
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.news.HomeNewsAdapter
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

class MainHomeFragment2: BindingSocketFragment<MainHomeViewModel, FragmentMainHome2Binding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment

    fun jumpToInplaySport() = getMainTabActivity().jumpToInplaySport()
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()

    private val NEWS_OKBET_ID = 12
    private val NEWS_SPORT_ID = 13

    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp)
        homeBottumView.bindServiceClick(childFragmentManager)
        initToolBar()
        initNews()
        viewModel.getHomeNews(1, 5, listOf(NEWS_OKBET_ID))
        viewModel.getBettingStationList()
    }


    override fun onBindViewStatus(view: View) = binding.run {
        homeTopView.setup(this@MainHomeFragment2)
        hotMatchView.onCreate(viewModel.publicityRecommend,this@MainHomeFragment2)
        okGamesView.setOkGamesData(this@MainHomeFragment2)
        initBetWinsRecodeLayout()
        initObservable()
    }

    private fun initBetWinsRecodeLayout() {
        binding.winsRankView.setUp(viewLifecycleOwner, { viewModel.getRecordNew() }, { viewModel.getRecordResult() })
        receiver.recordBetNew.observe(viewLifecycleOwner) { it?.let { binding.winsRankView.onNewWSBetData(it) } }
        receiver.recordWinsResult.observe(viewLifecycleOwner) { it?.let { binding.winsRankView.onNewWSWinsData(it) } }//最新大奖
        viewModel.recordBetNewHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpBetData(it)
            }
        }
        viewModel.recordWinsResultHttp.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                binding.winsRankView.onNewHttpWinsData(it)
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
        refreshHotMatch()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        homeToolbar.onRefreshMoney()

        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
            return
        }
        refreshHotMatch()

    }

    private fun initObservable() {
        viewModel.homeNewsList.observe(viewLifecycleOwner) {
            setupNews(it)
        }
        viewModel.bettingStationList.observe(viewLifecycleOwner) {
            setupBettingStation(it)
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
    //hot match end
    private fun initNews() {
        binding.includeNews.apply {
            tabNews.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val categoryId = if (tab?.position == 0) NEWS_OKBET_ID else NEWS_SPORT_ID
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
                        NewsDetailActivity.start(requireContext(),(adapter.data[position] as NewsItem))
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
                rvBettingStation.addItemDecoration(SpaceItemDecoration(requireContext(),
                    R.dimen.margin_10))
                PagerSnapHelper().attachToRecyclerView(rvBettingStation)
                rvBettingStation.adapter = HomeBettingStationAdapter().apply {
                    setList(newsList)
                    setOnItemChildClickListener { adapter, view, position ->
                        val data = (adapter as HomeBettingStationAdapter).data[position]
                        JumpUtil.toExternalWeb(
                            requireContext(),
                            "https://maps.google.com/?q=@" + data.lon + "," + data.lat
                        )
                    }
                }
            } else {
                (rvBettingStation.adapter as HomeBettingStationAdapter).setList(newsList)
            }
        }
    }

}