package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_game_v3.*
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.PlayCateMenuFilterUtils
import org.cxct.sportlottery.util.SocketUpdateUtil

class GamePublicityActivity : BaseSocketActivity<GamePublicityViewModel>(GamePublicityViewModel::class),
    View.OnClickListener {
    private lateinit var binding: ActivityGamePublicityBinding

    companion object {
        fun reStart(context: Context) {
            val intent = Intent(context, GamePublicityActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private var isNewestDataFromApi = false
    private var mRecommendList: List<Recommend> = listOf()
    private val mPublicityAdapter =
        GamePublicityAdapter(GamePublicityAdapter.PublicityAdapterListener(onItemClickListener = {
            goLoginPage()
        }))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
        initSocketObservers()
    }

    override fun onResume() {
        super.onResume()

        getSportMenuFilter()
    }

    private fun initViews() {
        initToolBar()
        initOnClickListener()
        initRecommendView()
        initTitle()
        initBottomView()
    }

    private fun initToolBar() {
        with(binding) {
            publicityToolbar.ivLanguage.setImageResource(LanguageManager.getLanguageFlag(this@GamePublicityActivity))
            publicityToolbar.tvLanguage.text = LanguageManager.getLanguageStringResource(this@GamePublicityActivity)
        }
    }

    private fun initOnClickListener() {
        binding.tvRegister.setOnClickListener(this)
        binding.tvLogin.setOnClickListener(this)
        binding.publicityToolbar.blockLanguage.setOnClickListener(this)
        binding.rvPublicity.setOnClickListener(this)
    }

    private fun initRecommendView() {
        with(binding.rvPublicity) {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mPublicityAdapter
            itemAnimator = null
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
            addSubTitle()
        }
    }

    private fun initBottomView() {
        mPublicityAdapter.addBottomView()
    }

    private fun initObservers() {
        viewModel.isLogin.observe(this) {
            if (it) {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
        }

        viewModel.oddsType.observe(this, {
            it?.let { oddsType ->
                mPublicityAdapter.oddsType = oddsType
            }
        })

        viewModel.publicityRecommend.observe(this, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                hideLoading()
                isNewestDataFromApi = true
                mRecommendList = result.recommendList
                mPublicityAdapter.addRecommend(result.recommendList)
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(result.recommendList)
            }
        })
    }

    // TODO subscribe leagueChange: 此處尚無需實作邏輯, 看之後有沒有相關需求
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(this) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    loading()
                    queryData()
                }
            }
        }

        receiver.matchStatusChange.observe(this, { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    val matchList = listOf(recommend).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            this
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.matchClock.observe(this, {
            it?.let { matchClockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.oddsChange.observe(this, { event ->
            event?.let { oddsChangeEvent ->
                val targetList = getNewestRecommendData()
                targetList.forEachIndexed { index, recommend ->
                    if (recommend.id == oddsChangeEvent.eventId) {
                        recommend.sortOddsMap()
                        recommend.updateOddsSort() //篩選玩法

                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            recommend.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion

                        if (SocketUpdateUtil.updateMatchOdds(this, recommend, oddsChangeEvent)) {
                            recommend.sortOddsByMenu()
                            updateRecommendList(index, recommend)
                        }

                        if (isNewestDataFromApi)
                            isNewestDataFromApi = false
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(this, {
            it?.let { matchOddsLockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.globalStop.observe(this, {
            it?.let { globalStopEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.producerUp.observe(this, {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(mPublicityAdapter.getRecommendData())
            }
        })
    }

    private fun queryData() {
        viewModel.getRecommend()
    }

    private fun getSportMenuFilter() {
        viewModel.getSportMenuFilter()
    }

    private fun subscribeChannelHall(recommend: Recommend) {
        subscribeChannelHall(recommend.gameType, recommend.id)
    }

    private fun subscribeQueryData(recommendList: List<Recommend>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    private fun getNewestRecommendData(): List<Recommend> =
        if (isNewestDataFromApi) mRecommendList else mPublicityAdapter.getRecommendData()


    private fun updateRecommendList(index: Int, recommend: Recommend) {
        with(binding) {
            if (rvPublicity.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rvPublicity.isComputingLayout) {
                mPublicityAdapter.updateRecommendData(index, recommend)
            }
        }
    }

    private fun Recommend.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }

    /**
     * 根據menuList的PlayCate排序賠率玩法
     */
    //TODO 20220323 等新版socket更新方式調整完畢後再確認一次此處是否需要移動至別處進行
    private fun Recommend.sortOddsByMenu() {
        val sortOrder = this.menuList.firstOrNull()?.playCateList?.map { it.code }

        oddsMap?.let { map ->
            val filterPlayCateMap = map.filter { sortOrder?.contains(it.key) == true }
            val sortedMap = filterPlayCateMap.toSortedMap(compareBy<String> {
                sortOrder?.indexOf(it)
            }.thenBy { it })

            map.clear()
            map.putAll(sortedMap)
        }
    }

    /**
     * 篩選玩法
     * 更新翻譯、排序
     * */
    private fun Recommend.updateOddsSort() {
        val nowGameType = gameType
        val playCateMenuCode = menuList.firstOrNull()?.code
        val oddsSortFilter = PlayCateMenuFilterUtils.filterOddsSort(nowGameType, playCateMenuCode)
        val playCateNameMapFilter = PlayCateMenuFilterUtils.filterPlayCateNameMap(nowGameType, playCateMenuCode)

        oddsSort = oddsSortFilter
        playCateNameMap = playCateNameMapFilter
    }

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                tvRegister -> {
                    goRegisterPage()
                }
                tvLogin -> {
                    goLoginPage()
                }
                publicityToolbar.blockLanguage -> {
                    goSwitchLanguagePage()
                }
                rvPublicity -> {
                    goLoginPage()
                }
            }
        }
    }

    private fun goRegisterPage() {
        startActivity(Intent(this@GamePublicityActivity, RegisterActivity::class.java))
    }

    private fun goLoginPage() {
        startActivity(Intent(this@GamePublicityActivity, LoginActivity::class.java))
    }

    private fun goSwitchLanguagePage() {
        startActivity(Intent(this@GamePublicityActivity, SwitchLanguageActivity::class.java))
    }
}