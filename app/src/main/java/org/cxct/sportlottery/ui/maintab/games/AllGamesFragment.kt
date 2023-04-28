package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import timber.log.Timber
import kotlin.random.Random

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding
    private val gameAllAdapter by lazy {
        GameCategroyAdapter(
            clickCollect = ::onCollectClick,
            clickGame = ::enterGame, okGamesFragment().gameItemViewPool
        )
    }
    private var collectGameAdapter: GameChildAdapter? = null
    private var recentGameAdapter: GameChildAdapter? = null
    private val providersAdapter by lazy { OkGameProvidersAdapter() }
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
    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3

    private var lastRequestTimeStamp = 0L

    private var recordHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
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
                    if (binding.include3.rbtnLb.isChecked) {
                        if (p3RecordNwsData.isNotEmpty()) {
                            newItem = p3RecordNwsData.removeAt(0)//ws 最新投注
                        } else if (p3RecordNData.isNotEmpty()) {
                            newItem = p3RecordNData.removeAt(0)
                        }
                    } else if (binding.include3.rbtnLbw.isChecked) {
                        if (p3RecordRwsData.isNotEmpty()) {
                            newItem = p3RecordRwsData.removeAt(0)//ws 最新大奖

                        } else if (p3RecordRData.isNotEmpty()) {
                            newItem = p3RecordRData.removeAt(0)
                        }
                    }
                    if (newItem != null) {
                        gameRecordAdapterNotify(newItem)
                    }
                    sendEmptyMessageDelayed(HANDLER_RECORD_GET, (Random.nextLong(1000) + 400))
                }
            }

        }
    }

    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return FragmentAllOkgamesBinding.inflate(layoutInflater).apply { binding = this }.root
    }

    override fun onBindView(view: View) {
        unSubscribeChannelHallAll()
        initObserve()
        initHotGameAdapter()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initSocketObservers()
        initRecent()
        initCollectLayout()
        initHotGameData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            return
        }

        initHotGameData()
        setupOddsChangeListener()
        val noData = okGamesFragment().viewModel.gameHall.value == null
        val time = System.currentTimeMillis()
        if (noData || time - lastRequestTimeStamp > 60_000) { // 避免短时间重复请求
            lastRequestTimeStamp = time
            okGamesFragment().viewModel.getOKGamesHall()
//            initHotGameData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recordHandler.removeCallbacksAndMessages(null)
    }

    private fun initObserve() = okGamesFragment().viewModel.run {
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
                binding.includeGamesAll.inclueCollect.root.isGone = adapter.data.isNullOrEmpty()
                setItemMoreVisiable(binding.includeGamesAll.inclueCollect, adapter.dataCount() > 3)
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
                binding.includeGamesAll.inclueRecent.root.visible()
                adapter.data.find { it.id == okgameBean.id }?.let { adapter.remove(it) }
                adapter.addData(0, okgameBean)
                setItemMoreVisiable(binding.includeGamesAll.inclueRecent, adapter.dataCount() > 3)
            }
        }
    }

    private var recordNewhttpFlag = false //最新投注接口请求完成
    private var recordResulthttpFlag = false//最新大奖接口请求完成

    private fun onBindGamesView() = binding.includeGamesAll.run {
        rvGamesAll.setLinearLayoutManager()
        rvGamesAll.adapter = gameAllAdapter
        gameAllAdapter.setOnItemChildClickListener { _, _, position ->
            gameAllAdapter.getItem(position).let {
                okGamesFragment().changeGameTable(it)
            }
        }
    }

    private fun gameRecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= 10) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
    }

    private fun onBindPart3View() {
        viewModel.getOKGamesRecordNew()
        viewModel.getOKGamesRecordResult()
        recordHandler.sendEmptyMessageDelayed(HANDLER_RECORD_GET, (Random.nextLong(1000) + 500))
        binding.include3.apply {
            binding.include3.ivProvidersLeft.alpha = 0.5F
            providersAdapter.setOnItemClickListener { _, _, position ->
                okGamesFragment().changePartGames(providersAdapter.getItem(position))
            }
            rvOkgameProviders.apply {
                var okGameProLLM = setLinearLayoutManager(LinearLayoutManager.HORIZONTAL)
                adapter = providersAdapter
                layoutManager = okGameProLLM
                addOnScrollListener(object : OnScrollListener() {
                    override fun onScrollStateChanged(rvView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(rvView, newState)
                        // 获取当前滚动到的条目位置
                        p3ogProviderFirstPosi = okGameProLLM.findFirstVisibleItemPosition()
                        p3ogProviderLastPosi = okGameProLLM.findLastVisibleItemPosition()

                        binding.include3.ivProvidersLeft.isClickable = p3ogProviderFirstPosi > 0
                        if (p3ogProviderFirstPosi > 0) {
                            binding.include3.ivProvidersLeft.alpha = 1F
                        } else {
                            binding.include3.ivProvidersLeft.alpha = 0.5F
                        }
                        if (p3ogProviderLastPosi == providersAdapter.data.size - 1) {
                            binding.include3.ivProvidersRight.alpha = 0.5F
                        } else {
                            binding.include3.ivProvidersRight.alpha = 1F
                        }
                        binding.include3.ivProvidersRight.isClickable =
                            p3ogProviderLastPosi != providersAdapter.data.size - 1

                    }
                })
            }


            rvOkgameRecord.addItemDecoration(
                RCVDecoration().setDividerHeight(2f)
                    .setColor(rvOkgameRecord.context.getColor(R.color.color_EEF3FC))
                    .setMargin(10.dp.toFloat())
            )
            rvOkgameRecord.adapter = gameRecordAdapter
            rvOkgameRecord.itemAnimator = DefaultItemAnimator()
            viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
                val firmList = resultData?.firmList ?: return@observe

                providersAdapter.setNewInstance(firmList.toMutableList())
                if (firmList.isNotEmpty()) {
                    binding.include3.run {
                        setViewVisible(
                            rvOkgameProviders,
                            okgameP3LayoutProivder
                        )
                    }
                } else {
                    binding.include3.run { setViewGone(rvOkgameProviders, okgameP3LayoutProivder) }
                }

                if (firmList.size > 3) {
                    binding.include3.run { setViewVisible(ivProvidersLeft, ivProvidersRight) }
                } else {
                    binding.include3.run { setViewGone(ivProvidersLeft, ivProvidersRight) }
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
                    var msg = Message()
                    msg.what = HANDLER_RECORD_NEW_ADD
                    msg.obj = it
                    recordHandler.sendMessage(msg)
                }
            }
            receiver.recordResult.observe(viewLifecycleOwner) {
                if (it != null) {
                    var msg = Message()
                    msg.what = HANDLER_RECORD_RESULT_ADD
                    msg.obj = it
                    recordHandler.sendMessage(msg)
                }
            }

        }
        binding.include3.rGroupRecord.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbtn_lb -> {
                    if (!recordNewhttpFlag) {
                        viewModel.getOKGamesRecordNew()
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
                        viewModel.getOKGamesRecordResult()
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
        //供应商左滑按钮
        binding.include3.ivProvidersLeft.setOnClickListener {
            if (p3ogProviderFirstPosi >= 3) {
                binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.include3.rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderFirstPosi - 2
                )
            } else {
                binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.include3.rvOkgameProviders, RecyclerView.State(), 0
                )
            }
        }
        //供应商右滑按钮
        binding.include3.ivProvidersRight.setOnClickListener {
            if (p3ogProviderLastPosi < providersAdapter.data.size - 4) {
                binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.include3.rvOkgameProviders,
                    RecyclerView.State(),
                    p3ogProviderLastPosi + 2
                )
            } else {
                binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                    binding.include3.rvOkgameProviders,
                    RecyclerView.State(),
                    providersAdapter.data.size - 1
                )
            }
        }
    }

    private fun onBindPart5View() {
        val include5 = binding.include5
        val tvPrivacyPolicy = include5.tvPrivacyPolicy
        val tvTermConditions = include5.tvTermConditions
        val tvResponsibleGaming = include5.tvResponsibleGaming
        val tvLiveChat = include5.tvLiveChat
        val tvContactUs = include5.tvContactUs
        val tvFaqs = include5.tvFaqs
        val rcvPayment = include5.rcvPayment


        jumpToWebView(
            tvPrivacyPolicy,
            Constants.getPrivacyRuleUrl(requireContext()),
            R.string.privacy_policy
        )
        jumpToWebView(
            tvTermConditions,
            Constants.getAgreementRuleUrl(requireContext()),
            R.string.terms_conditions
        )
        jumpToWebView(
            tvResponsibleGaming,
            Constants.getDutyRuleUrl(requireContext()),
            R.string.responsible
        )
        jumpToWebView(
            include5.textView16,
            Constants.getDutyRuleUrl(requireContext()),
            R.string.responsible
        )
        jumpToWebView(tvFaqs, Constants.getFAQsUrl(requireContext()), R.string.faqs)

        tvLiveChat.setServiceClick(childFragmentManager)
        tvContactUs.setServiceClick(childFragmentManager)


        initRcvPaymentMethod(rcvPayment)
    }

    private fun jumpToWebView(view: View, url: String?, @StringRes title: Int) {
        view.setOnClickListener {
            val context = binding.root.context
            JumpUtil.toInternalWeb(context, url, context.getString(title))
        }
    }

    private fun initRcvPaymentMethod(rcvPayment: RecyclerView) {
        val list = mutableListOf(
            R.drawable.icon_gcash,
            R.drawable.icon_paymaya,
            R.drawable.icon_fortune_pay,
            R.drawable.icon_epon
        )
        rcvPayment.layoutManager = GridLayoutManager(context, 4)
        val paymentAdapter =
            object : BaseQuickAdapter<Int, BaseViewHolder>(R.layout.item_view_payment_method) {
                override fun convert(holder: BaseViewHolder, item: Int) {
                    holder.setImageResource(R.id.iv, item)
                }

            }
        rcvPayment.adapter = paymentAdapter
        paymentAdapter.setNewInstance(list)

    }

    private fun initHotGameAdapter() {
        binding.hotGameView.setUpAdapter(viewLifecycleOwner, HomeRecommendListener(
            onItemClickListener = { matchInfo ->
                if (isCreditSystem() && viewModel.isLogin.value != true) {
                    (activity as MainTabActivity).showLoginNotify()
                } else {
                    matchInfo?.let {
                        SportDetailActivity.startActivity(requireContext(), it)
//                        navOddsDetailFragment(MatchType.IN_PLAY, it)
                    }
                }
            },

            onClickBetListener = { gameTypeCode, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                if (!mIsEnabled) {
                    return@HomeRecommendListener
                }
                avoidFastDoubleClick()
                if (isCreditSystem() && viewModel.isLogin.value != true) {
                    (activity as MainTabActivity).showLoginNotify()
                    return@HomeRecommendListener
                }
                val gameType = GameType.getGameType(gameTypeCode)
                if (gameType == null || matchInfo == null || activity !is MainTabActivity) {
                    return@HomeRecommendListener
                }
                val fastBetDataBean = FastBetDataBean(
                    matchType = matchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName,
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    playCateMenuCode
                )

                activity?.doOnStop(true) { // 延时加入注单，不然当前页面会弹出来注单列表
                    viewModel.updateMatchBetListData(fastBetDataBean)
                }
                SportDetailActivity.startActivity(
                    requireContext(),
                    matchInfo,
                    matchType,
                    false
                )
            }, onClickPlayTypeListener = { _, _, _, _ ->

            }
        ))

        viewModel.publicityRecommend.observe(this) {
            //api获取热门赛事列表
            it.peekContent().let { data ->
                binding.hotGameView.visible()
                unSubscribeChannelHallAll()
                binding.hotGameView.setGameData(data)

                //订阅监听
                subscribeQueryData(data)

            }
        }
    }

    private fun initHotGameData() {
        if (binding.hotGameView.adapter == null) {
            binding.hotGameView.gone()
        }
//        unSubscribeChannelHallAll()
        //请求热门赛事列表
        viewModel.getRecommend()
    }


    //用户缓存最新赔率，方便当从api拿到新赛事数据时，赋值赔率信息给新赛事
    private val matchOddMap = HashMap<String, Recommend>()
    private fun initSocketObservers() {


        //观察比赛状态改变
        receiver.matchStatusChange.observe(viewLifecycleOwner) { matchStatusChangeEvent ->
            if (matchStatusChangeEvent == null) {
                return@observe
            }

            if (binding.hotGameView.adapter == null || binding.hotGameView.adapter!!.data.isEmpty()) {
                return@observe
            }
            val adapterData = binding.hotGameView.adapter?.data
            adapterData?.forEachIndexed { index, recommend ->

                //丢进去判断是否要更新
                if (SocketUpdateUtil.updateMatchStatus(
                        recommend.matchInfo?.gameType,
                        recommend,
                        matchStatusChangeEvent,
                        context
                    )
                ) {
                    binding.hotGameView.notifyAdapterData(index, recommend)
                }
            }

        }
        receiver.matchClock.observe(viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                val targetList = binding.hotGameView.adapter?.data
                targetList?.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        binding.hotGameView.notifyAdapterData(index, recommend)
                    }
                }

            }
        }
        setupOddsChangeListener()

        receiver.matchOddsLock.observe(viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                val targetList = binding.hotGameView.adapter?.data

                targetList?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        binding.hotGameView.notifyAdapterData(index, recommend)
                    }
                }

            }
        }


        receiver.globalStop.observe(viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                binding.hotGameView.adapter?.data?.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        binding.hotGameView.notifyAdapterData(index, recommend)
                    }
                }
            }
        }

        receiver.producerUp.observe(viewLifecycleOwner) {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(binding.hotGameView.adapter?.data ?: listOf())
            }
        }

        receiver.closePlayCate.observe(viewLifecycleOwner) { event ->
            val it = event?.getContentIfNotHandled() ?: return@observe

            binding.hotGameView.adapter?.data?.forEach { recommend ->
                if (recommend.gameType == it.gameType) {
                    recommend.oddsMap?.forEach { map ->
                        if (map.key == it.playCateCode) {
                            map.value?.forEach { odd ->
                                odd?.status = BetStatus.DEACTIVATED.code
                            }
                        }
                    }
                }
            }
            binding.hotGameView.adapter?.notifyDataSetChanged()
        }
    }

    private fun setupOddsChangeListener() {
        receiver.oddsChangeListener = mOddsChangeListener
    }

    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            val targetList = binding.hotGameView.adapter?.data
            targetList?.forEachIndexed { index, recommend ->
                if (recommend.matchInfo?.id == oddsChangeEvent.eventId) {
                    recommend.sortOddsMap()
                    //region 翻譯更新
                    oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                        recommend.playCateNameMap?.putAll(playCateNameMap)
                    }
                    oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                        recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                    }
                    //endregion
                    if (SocketUpdateUtil.updateMatchOdds(
                            context,
                            recommend,
                            oddsChangeEvent
                        )
                    ) {
                        matchOddMap[recommend.id] = recommend
//                        LogUtil.toJson(recommend.oddsMap?.get(PlayCate.SINGLE.value))
                        binding.hotGameView.adapter?.notifyItemChanged(index, recommend)
                    }
                }
            }
        }
    }

    private fun initCollectLayout() {
        collectGameAdapter =
            bindGameCategroyLayout(GameTab.TAB_FAVORITES, binding.includeGamesAll.inclueCollect)
    }

    private fun initRecent() {
        recentGameAdapter =
            bindGameCategroyLayout(GameTab.TAB_RECENTLY, binding.includeGamesAll.inclueRecent)
    }

    private fun bindGameCategroyLayout(gameTab: GameTab, binding: ItemGameCategroyBinding) =
        binding.run {
            root.gone()
            linCategroyName.setOnClickListener { okGamesFragment().changeGameTable(gameTab) }
            gameTab.bindLabelIcon(ivIcon)
            gameTab.bindLabelName(tvName)
            rvGameItem.setRecycledViewPool(okGamesFragment().gameItemViewPool)
            rvGameItem.layoutManager = SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
            rvGameItem.addItemDecoration(SpaceItemDecoration(root.context, R.dimen.margin_10))
            val gameAdapter = GameChildAdapter(onFavoriate = ::onCollectClick)
            gameAdapter.setOnItemClickListener { _, _, position ->
                enterGame(gameAdapter.getItem(position))
            }

            rvGameItem.adapter = gameAdapter
            return@run gameAdapter
        }

    private inline fun enterGame(okGameBean: OKGameBean) {
        okGamesFragment().enterGame(okGameBean)
    }

    /**
     * 设置收藏游戏列表
     */
    private fun setCollectList(collectList: List<OKGameBean>) {
        val emptyData = collectList.isNullOrEmpty()
        setItemMoreVisiable(binding.includeGamesAll.inclueCollect, collectList.size > 3)
        binding.includeGamesAll.inclueCollect.root.isGone = emptyData
        if (!emptyData) {
            collectGameAdapter?.setNewInstance(collectList?.toMutableList())
        }
    }

    private fun subscribeQueryData(recommendList: List<Recommend>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    private fun subscribeChannelHall(recommend: Recommend) {
        subscribeChannelHall(recommend.matchInfo?.gameType, recommend.matchInfo?.id)
    }


    /**
     * 设置最近游戏列表
     */
    private fun setRecent(recentList: List<OKGameBean>) {
        setItemMoreVisiable(binding.includeGamesAll.inclueRecent, recentList.size > 3)
        val emptyData = recentList.isNullOrEmpty()
        binding.includeGamesAll.inclueRecent.root.isGone = emptyData
        if (!emptyData) {
            recentGameAdapter?.setNewInstance(recentList?.toMutableList())
        }
    }

    private fun setItemMoreVisiable(binding: ItemGameCategroyBinding, visisable: Boolean) {
        binding.ivMore.isVisible = visisable
        binding.tvMore.isVisible = visisable
    }

    private fun Recommend.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if ((value?.size ?: 0) > 3
                && value?.first()?.marketSort != 0
                && (value?.first()?.odds != value?.first()?.malayOdds)
            ) {
                value?.sortBy { it?.marketSort }
            }
        }
    }

    private fun onCollectClick(view: View, gameData: OKGameBean) {
        if (okGamesFragment().collectGame(gameData)) {
            view.animDuang(1.3f)
        }
    }
}