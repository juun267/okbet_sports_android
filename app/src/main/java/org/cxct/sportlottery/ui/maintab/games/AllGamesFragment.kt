package org.cxct.sportlottery.ui.maintab.games

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.third_game.third_games.hot.HandicapData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding
    private val gameAllAdapter by lazy {
        GameCategroyAdapter(
            clickCollect = {
                okGamesFragment().viewModel.collectGame(it)
            },
            clickGame = {
                okGamesFragment().viewModel.requestEnterThirdGame(it, this@AllGamesFragment)
                viewModel.addRecentPlay(it.id.toString())
            }
        )
    }
    private var collectGameAdapter: GameChildAdapter? = null
    private var recentGameAdapter: GameChildAdapter? = null
    private val providersAdapter by lazy { OkGameProvidersAdapter() }
    private val gameRecordAdapter by lazy { OkGameRecordAdapter() }
    private var categoryList = mutableListOf<OKGamesCategory>()
    private val p3RecordNData: MutableList<RecordNewEvent> = mutableListOf()
    private val p3RecordRData: MutableList<RecordNewEvent> = mutableListOf()
    private var p3ogProviderFirstPosi: Int = 0
    private var p3ogProviderLastPosi: Int = 3

    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAllOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initObserve()
        onBindGamesView()
        onBindPart3View()
        onBindPart5View()
        initHotGameData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            okGamesFragment().viewModel.getOKGamesHall()
        }
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
            if (it.isNotEmpty() && it.size > 12) {
                setCollectList(it.subList(0, 12))
            } else {
                setCollectList(it)
            }
        }
        collectOkGamesResult.observe(viewLifecycleOwner) { result ->
            var needUpdate = false
            gameAllAdapter.data.forEach {
                it.gameList?.forEach { gameBean ->
                    if (gameBean.id == result.first) {
                        gameBean.markCollect = result.second.markCollect
                        needUpdate = true
                    }
                }
            }

            if (needUpdate) {
                gameAllAdapter.notifyDataSetChanged()
            }
            //更新收藏列表
            collectGameAdapter?.let { adapter ->
                //添加收藏
                if (result.second.markCollect) {
                    adapter.data.firstOrNull() { it.id == result.first }?.let {
                        adapter.data.remove(it)
                    }
                    adapter.data.add(0, result.second)
                    adapter.notifyDataSetChanged()
                } else {//取消收藏
                    adapter.data.firstOrNull { it.id == result.first }?.let {
                        adapter.data.remove(it)
                        adapter.notifyDataSetChanged()
                    }
                }
                binding.includeGamesAll.inclueCollect.root.isGone = adapter.data.isNullOrEmpty()
            }
            //更新最近列表
            recentGameAdapter?.data?.forEachIndexed { index, okGameBean ->
                if (okGameBean.id == result.first) {
                    okGameBean.markCollect = result.second.markCollect
                    recentGameAdapter?.notifyItemChanged(index)
                }
            }
        }

        recentPlay.observe(viewLifecycleOwner) {
            val recentCategory = OKGamesCategory(
                id = -1,
                "Recent",
                "recentPlay",
                iconSelected = null,
                iconUnselected = null,
                it
            )
            val insertPos = if (categoryList.firstOrNull { it.id == 1 } == null) 0 else 1
            val insertOrUpdate = categoryList.firstOrNull { it.id == recentCategory.id } == null
            if (insertOrUpdate) {
                categoryList.add(insertPos, recentCategory)
                if (it.size > 12) {
                    setRecent(it.subList(0, 12))
                } else {
                    setRecent(it)
                }
            }
        }
    }

    var recordNewhttpFlag = false //最新投注接口请求完成
    var recordResulthttpFlag = false//最新大奖接口请求完成

    private fun onBindGamesView() = binding.includeGamesAll.run {
        rvGamesAll.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvGamesAll.adapter = gameAllAdapter
        gameAllAdapter.setOnItemChildClickListener(OnItemChildClickListener { _, _, position ->
            gameAllAdapter.getItem(position).let {
                okGamesFragment().changeGameTable(it)
            }
        })
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
        binding.include3.apply {
            binding.include3.ivProvidersLeft.alpha = 0.5F
            providersAdapter.setOnItemClickListener { _, _, position ->
                okGamesFragment().changePartGames(providersAdapter.getItem(position))
            }
            rvOkgameProviders.apply {
                var okGameProLLM =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            rvOkgameRecord.adapter = gameRecordAdapter
            rvOkgameRecord.itemAnimator = DefaultItemAnimator()
            viewModel.providerResult.observe(viewLifecycleOwner) { resultData ->
                resultData?.firmList?.let {
                    if(!providersAdapter.data.containsAll(it)){
                        providersAdapter.addData(it)
                    }
                }
            }
            viewModel.recordNewHttp.observe(viewLifecycleOwner) {
                if(it!=null){
                    if (binding.include3.rbtnLb.isChecked) {
                        gameRecordAdapter.addData(0, it.subList(0, 10))
                    }
                    p3RecordNData.addAll(0, it.subList(0, 10))
                    recordNewhttpFlag = true
                }
            }
            viewModel.recordResultHttp.observe(viewLifecycleOwner) {
                if (binding.include3.rbtnLbw.isChecked) {
                    gameRecordAdapter.addData(0, it.subList(0, 10))
                }
                p3RecordRData.addAll(0, it.subList(0, 10))
                recordResulthttpFlag = true
            }
            receiver.recordNew.observe(viewLifecycleOwner) {
                if (recordNewhttpFlag && it != null) {
                    if (binding.include3.rbtnLb.isChecked) {
                        gameRecordAdapterNotify(it)
                    }
                    if (p3RecordNData.size >= 10) {
                        p3RecordNData.removeAt(p3RecordNData.size - 1)
                    }
                    p3RecordNData.add(0, it)
                }
            }
            receiver.recordResult.observe(viewLifecycleOwner) {
                if (recordResulthttpFlag && it != null) {
                    if (binding.include3.rbtnLbw.isChecked) {
                        gameRecordAdapterNotify(it)
                    }

                    if (p3RecordRData.size >= 10) {
                        p3RecordRData.removeAt(p3RecordRData.size - 1)
                    }
                    p3RecordRData.add(0, it)

                }
            }
        }

    }

    private fun onBindPart5View() {
        val include3 = binding.include3
        val include5 = binding.include5
        val tvPrivacyPolicy = include5.tvPrivacyPolicy
        val tvTermConditions = include5.tvTermConditions
        val tvResponsibleGaming = include5.tvResponsibleGaming
        val tvLiveChat = include5.tvLiveChat
        val tvContactUs = include5.tvContactUs
        val tvFaqs = include5.tvFaqs
        val rBtnLb = include3.rbtnLb
        val rBtnLbw = include3.rbtnLbw
        val prLeft = binding.include3.ivProvidersLeft//供应商左滑按钮
        val prRight = binding.include3.ivProvidersRight//供应商右滑按钮
        setUnderline(
            tvPrivacyPolicy, tvTermConditions, tvResponsibleGaming, tvLiveChat, tvContactUs, tvFaqs
        )
        setOnClickListener(
            tvPrivacyPolicy, tvTermConditions, tvResponsibleGaming, tvLiveChat, tvContactUs, tvFaqs,
            rBtnLb, rBtnLbw, prLeft, prRight
        ) {
            when (it.id) {
                R.id.tvPrivacyPolicy -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getPrivacyRuleUrl(requireContext()),
                        getString(R.string.privacy_policy)
                    )
                }

                R.id.tvTermConditions -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getAgreementRuleUrl(requireContext()),
                        getString(R.string.terms_conditions)
                    )
                }

                R.id.tvResponsibleGaming -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getDutyRuleUrl(requireContext()),
                        getString(R.string.responsible)
                    )
                }

                R.id.tvLiveChat, R.id.tvContactUs -> {
                    it.setServiceClick(childFragmentManager)
                }

                R.id.tvFaqs -> {
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getFAQsUrl(requireContext()),
                        getString(R.string.faqs)
                    )
                }
                R.id.rbtn_lb -> {
                    gameRecordAdapter.data.clear()
                    gameRecordAdapter.addData(p3RecordNData)
                }
                R.id.rbtn_lbw -> {
                    gameRecordAdapter.data.clear()
                    gameRecordAdapter.addData(p3RecordRData)
                }
                R.id.iv_providers_left -> {
                    if (p3ogProviderFirstPosi >= 3) {
                        binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                            binding.include3.rvOkgameProviders,
                            RecyclerView.State(),
                            p3ogProviderFirstPosi - 2
                        )
                    } else {
                        binding.include3.rvOkgameProviders.layoutManager?.smoothScrollToPosition(
                            binding.include3.rvOkgameProviders,
                            RecyclerView.State(),
                            0
                        )
                    }

                }
                R.id.iv_providers_right -> {
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
        }

    }

    private fun setUnderline(vararg view: TextView) {
        view.forEach {
            it.paint.flags = Paint.UNDERLINE_TEXT_FLAG; //下划线
            it.paint.isAntiAlias = true;//抗锯齿
        }
    }


    private fun initHotGameData() {
        //请求热门赛事列表
        viewModel.getRecommend()

        viewModel.publicityRecommend.observe(this) {
            //api获取热门赛事列表
            it.getContentIfNotHandled()?.let { data ->
                //订阅监听
                subscribeQueryData(data)
                binding.hotGameView.setGameData(receiver, data)

                receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
                    //取消订阅
                    unSubscribeChannelHallSport()
                    unSubscribeChannelHallAll()
                    //重新请求列表
                    viewModel.getRecommend()
                }

                //观察比赛状态改变
                receiver.matchStatusChange.observe(viewLifecycleOwner) { matchStatusChangeEvent ->
                    if (matchStatusChangeEvent == null || binding.hotGameView.getAdapter().data.isEmpty()) {
                        return@observe
                    }

                    var needUpdate = false
                    val adapterData = binding.hotGameView.getAdapter().data
                    adapterData.forEachIndexed { index, recommend ->
                        //取一个赛事，装成集合
                        val testList = mutableListOf<Recommend>()
                        testList.add(recommend)
                        //丢进去判断是否要更新
                        if (SocketUpdateUtil.updateMatchStatus(
                                recommend.matchInfo?.gameType,
                                testList as MutableList<MatchOdd>,
                                matchStatusChangeEvent,
                                context
                            )
                        ) {
                            needUpdate = true
                        }

                        if (needUpdate) {
                            binding.hotGameView.notifyAdapterData(index)
                        }
                    }

                }
            }
        }
    }

    /**
     * 设置收藏游戏列表
     */
    private fun setCollectList(collectList: List<OKGameBean>) {
        binding.includeGamesAll.inclueCollect.root.isGone = collectList.isNullOrEmpty()
        if (!collectList.isNullOrEmpty()) {
            binding.includeGamesAll.inclueCollect.apply {
                linCategroyName.setOnClickListener {
                    okGamesFragment().changeGameTable(GameTab.TAB_FAVORITES)
                }
                ivIcon.setImageResource(GameTab.TAB_FAVORITES.labelIcon)
                tvName.setText(GameTab.TAB_FAVORITES.name)
                rvGameItem.apply {
                    if (adapter == null) {
                        layoutManager =
                            SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
                        if (itemDecorationCount == 0)
                            addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
                        collectGameAdapter = GameChildAdapter().apply {
                            setList(collectList)
                            setOnItemChildClickListener { adapter, view, position ->
                                data[position].let {
                                    okGamesFragment().viewModel.collectGame(it)
                                }
                            }
                            setOnItemClickListener { adapter, view, position ->
                                data[position].let {
                                    okGamesFragment().viewModel.requestEnterThirdGame(it,
                                        this@AllGamesFragment)
                                    viewModel.addRecentPlay(it.id.toString())
                                }
                            }
                        }
                        adapter = collectGameAdapter
                    } else {
                        (adapter as GameChildAdapter).setList(collectList)
                    }
                }
            }
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
        binding.includeGamesAll.inclueRecent.root.isGone = recentList.isNullOrEmpty()
        if (!recentList.isNullOrEmpty()) {
            binding.includeGamesAll.inclueRecent.apply {
                linCategroyName.setOnClickListener {
                    okGamesFragment().changeGameTable(GameTab.TAB_RECENTLY)
                }
                ivIcon.setImageResource(GameTab.TAB_RECENTLY.labelIcon)
                tvName.setText(GameTab.TAB_RECENTLY.name)
                rvGameItem.apply {
                    if (adapter == null) {
                        layoutManager =
                            SocketLinearManager(context, RecyclerView.HORIZONTAL, false)
                        if (itemDecorationCount == 0)
                            addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
                        recentGameAdapter = GameChildAdapter().apply {
                            setList(recentList)
                            setOnItemChildClickListener { adapter, view, position ->
                                data[position].let {
                                    okGamesFragment().viewModel.collectGame(it)
                                }
                            }
                            setOnItemClickListener { adapter, view, position ->
                                data[position].let {
                                    okGamesFragment().viewModel.requestEnterThirdGame(it,
                                        this@AllGamesFragment)
                                    viewModel.addRecentPlay(it.id.toString())
                                }
                            }
                        }
                        adapter = recentGameAdapter
                    } else {
                        (adapter as GameChildAdapter).setList(recentList)
                    }
                }
            }
        }
    }
}