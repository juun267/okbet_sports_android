package org.cxct.sportlottery.ui.maintab.home


import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.core.view.postDelayed

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.databinding.FragmentMainHome2Binding
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.net.news.data.NewsItem
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OkGameRecordAdapter
import org.cxct.sportlottery.ui.maintab.home.news.HomeNewsAdapter
import org.cxct.sportlottery.ui.maintab.home.news.NewsDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber
import kotlin.random.Random

class MainHomeFragment2: BindingSocketFragment<MainHomeViewModel, FragmentMainHome2Binding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity
    private inline fun getHomeFragment() = parentFragment as HomeFragment

    fun jumpToInplaySport() = getMainTabActivity().jumpToInplaySport()
    fun jumpToOKGames() = getMainTabActivity().jumpToOKGames()

    private val gameRecordAdapter by lazy { OkGameRecordAdapter() }
    private var categoryList = mutableListOf<OKGamesCategory>()
    private val p3RecordNData: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新投注
    private val p3RecordNwsData: MutableList<RecordNewEvent> = mutableListOf()//ws的最新投注
    private val p3RecordNShowData: MutableList<RecordNewEvent> = mutableListOf()//最新投注显示在界面上的数据
    private val HANDLER_RECORD_NEW_ADD = 1//最新投注  数据 添加
    private val HANDLER_RECORD_RESULT_ADD = 2//最新大奖数据 添加
    private val HANDLER_RECORD_GET = 3//最新投注 最新大奖数据 获取
    private val p3RecordRData: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新大奖
    private val p3RecordRwsData: MutableList<RecordNewEvent> = mutableListOf()//ws的最新大奖
    private val p3RecordRShowData: MutableList<RecordNewEvent> = mutableListOf()//最新大奖显示在界面上的数据

    private var lastRequestTimeStamp = 0L
    private var recordNewhttpFlag = false //最新投注接口请求完成
    private var recordResulthttpFlag = false//最新大奖接口请求完成
    private val NEWS_OKBET_ID = 12
    private val NEWS_SPORT_ID = 13

    private var recordHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                HANDLER_RECORD_NEW_ADD -> {
                    var wsData: RecordNewEvent = msg.obj as RecordNewEvent
                    Timber.v("RECORD_NEW_OK_GAMES 加数据: $wsData")
                    p3RecordNwsData.add(wsData)//最新投注//最新投注(当前正处于主线程，直接将数据加到队列里面去)
                    Timber.v("RECORD_NEW_OK_GAMES 加数据后: $p3RecordNwsData")
                }

                HANDLER_RECORD_RESULT_ADD -> {
                    var wsData: RecordNewEvent = msg.obj as RecordNewEvent
                    p3RecordRwsData.add(wsData)//最新大奖
                }
                HANDLER_RECORD_GET -> {
                    var newItem: RecordNewEvent? = null
                    if (binding.includeRecord.rbtnLb.isChecked) {
                        if (p3RecordNwsData.isNotEmpty()) {
                            newItem = p3RecordNwsData.removeAt(0)//ws 最新投注
                        } else if (p3RecordNData.isNotEmpty()) {
                            newItem = p3RecordNData.removeAt(0)
                        }
                    } else if (binding.includeRecord.rbtnLbw.isChecked) {
                        if (p3RecordRwsData.isNotEmpty()) {
                            newItem = p3RecordRwsData.removeAt(0)//ws 最新大奖

                        } else if (p3RecordRData.isNotEmpty()) {
                            newItem = p3RecordRData.removeAt(0)
                        }
                    }
                    if (newItem != null) {
                        reecordAdapterNotify(newItem)
                    }

                    sendEmptyMessageDelayed(HANDLER_RECORD_GET, (Random.nextLong(1000) + 400))
                }
            }

        }
    }

    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp)
        homeBottumView.bindServiceClick(childFragmentManager)
        initToolBar()
        initNews()
        initRecordView()
        initObservable()
        binding.hotMatchView.onCreate(viewModel.publicityRecommend, this@MainHomeFragment2)
        viewModel.getHomeNews(1, 5, listOf(NEWS_OKBET_ID))
        viewModel.getBettingStationList()
    }


    override fun onBindViewStatus(view: View) {
        binding.homeTopView.setup(this)
        initRecordView()
        initObservable()
        binding.hotMatchView.onCreate(viewModel.publicityRecommend,this@MainHomeFragment2)
        binding.okGamesView.setOkGamesData(this)
    }

    override fun onInitData() {

    }

    fun initToolBar() = binding.run {
        homeToolbar.attach(this@MainHomeFragment2, getMainTabActivity(), viewModel)
        homeToolbar.fitsSystemStatus()
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

    private fun initRecordView() {
        viewModel.getRecordNew()
        viewModel.getRecordResult()
        recordHandler.sendEmptyMessageDelayed(HANDLER_RECORD_GET, (Random.nextLong(1000) + 500))
        binding.includeRecord.apply {

            rvOkgameRecord.addItemDecoration(
                RCVDecoration().setDividerHeight(2f)
                    .setColor(rvOkgameRecord.context.getColor(R.color.color_EEF3FC))
                    .setMargin(10.dp.toFloat())
            )
            rvOkgameRecord.adapter = gameRecordAdapter
            rvOkgameRecord.itemAnimator = DefaultItemAnimator()
            binding.includeRecord.rGroupRecord.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.rbtn_lb -> {
                        if (!recordNewhttpFlag) {
                            viewModel.getRecordNew()
                        }
                        if (gameRecordAdapter.data.isNotEmpty()) {
                            p3RecordRShowData.clear()
                            p3RecordRShowData.addAll(gameRecordAdapter.data)
                            gameRecordAdapter.data.clear()
                            gameRecordAdapter.notifyDataSetChanged()
                            gameRecordAdapter.addData(p3RecordNShowData)
                        }
                    }


                    R.id.rbtn_lbw -> {
                        if (!recordResulthttpFlag) {
                            viewModel.getRecordResult()
                        }
                        if (gameRecordAdapter.data.isNotEmpty()) {
                            p3RecordNShowData.clear()
                            p3RecordNShowData.addAll(gameRecordAdapter.data)
                            gameRecordAdapter.data.clear()
                            gameRecordAdapter.notifyDataSetChanged()
                            gameRecordAdapter.addData(p3RecordRShowData)
                        }
                    }
                }
            }
            viewModel.recordNewHttp.observe(viewLifecycleOwner) {
                if (it != null) {
                    p3RecordNData.addAll(it.reversed())
                    recordNewhttpFlag = true
                }
            }
            viewModel.recordResultHttp.observe(viewLifecycleOwner) {
                if (it != null) {
                    p3RecordRData.addAll(it.reversed())
                    recordResulthttpFlag = true
                }
            }
            receiver.recordNew.observe(viewLifecycleOwner) {
                if (it != null) {
                    p3RecordNwsData.add(it)
//                    var msg = Message()
//                    msg.what = HANDLER_RECORD_NEW_ADD
//                    msg.obj = it
//                    recordHandler.sendMessage(msg)
                }
            }
            receiver.recordResult.observe(viewLifecycleOwner) {
                if (it != null) {
//                    var msg = Message()
//                    msg.what = HANDLER_RECORD_RESULT_ADD
//                    msg.obj = it
//                    recordHandler.sendMessage(msg)
                    p3RecordRwsData.add(it)//最新大奖
                }
            }

        }
        binding.includeRecord.rGroupRecord.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_lb -> {
                    if (!recordNewhttpFlag) {
                        viewModel.getRecordNew()
                    }
                    if (gameRecordAdapter.data.isNotEmpty()) {
                        p3RecordRShowData.clear()
                        p3RecordRShowData.addAll(gameRecordAdapter.data)
                        gameRecordAdapter.data.clear()
                        gameRecordAdapter.notifyDataSetChanged()
                        gameRecordAdapter.addData(p3RecordNShowData)
                    }
                }

                R.id.rbtn_lbw -> {
                    if (!recordResulthttpFlag) {
                        viewModel.getRecordResult()
                    }
                    if (gameRecordAdapter.data.isNotEmpty()) {
                        p3RecordNShowData.clear()
                        p3RecordNShowData.addAll(gameRecordAdapter.data)
                        gameRecordAdapter.data.clear()
                        gameRecordAdapter.notifyDataSetChanged()
                        gameRecordAdapter.addData(p3RecordRShowData)
                    }
                }
            }
        }
    }

    private fun reecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= 10) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
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

    override fun onDestroyView() {
        super.onDestroyView()
        recordHandler.removeCallbacksAndMessages(null)
    }
}