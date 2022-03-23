package org.cxct.sportlottery.ui.game.publicity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_game_v3.*
import org.cxct.sportlottery.databinding.ActivityGamePublicityBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.SocketUpdateUtil
import timber.log.Timber

//TODO 玩法賠率顯示順序錯誤，需再寫排序邏輯
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
    private val mPublicityAdapter = GamePublicityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGamePublicityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
        initSocketObservers()
        queryData()
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
                mPublicityAdapter.oddsType = it
            }
        })

        viewModel.publicityRecommend.observe(this, { event ->
            event?.getContentIfNotHandled()?.let { result ->
                isNewestDataFromApi = true
                mRecommendList = result.recommendList
                mPublicityAdapter.addRecommend(result.recommendList)
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(result.recommendList)
            }
        })
    }

    //region
    // TODO subscribe serviceConnectStatus, matchOddsLock, globalStop, producerUp, leagueChange
    //endregion
    private fun initSocketObservers() {
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
                        if (matchList.isNullOrEmpty()) {
                            //TODO 移除該賽事
//                                    leagueAdapter.data.remove(leagueOdd)
                        }
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                        //leagueAdapter.updateBySocket(index)
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
                        //leagueAdapter.updateBySocket(index)
                        //leagueAdapter.updateLeague(index, leagueOdd)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.oddsChange.observe(this, { event ->
            event?.let { oddsChangeEvent ->
//                oddsChangeEvent.sortOddsMap()

                val targetList = getNewestRecommendData()
                targetList.forEachIndexed { index, recommend ->
                    if (recommend.id == oddsChangeEvent.eventId) {
                        //region 主要市場 以外的過濾邏輯
                        /*
                        when {
                            getPlaySelectedCodeSelectionType() == SelectionType.SELECTABLE.code -> leagueOdds.filterMenuPlayCate()
                            getPlaySelectedCode() == "MAIN" -> { }
                            else -> {
                                leagueOdds.forEach { LeagueOdd ->
                                    LeagueOdd.matchOdds.forEach { MatchOdd ->
                                        if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                            MatchOdd.oddsMap = oddsChangeEvent.odds
                                        }
                                    }
                                }
                            }
                        }*/
//                        recommend.oddsMap = oddsChangeEvent.odds
                        //endregion

                        recommend.sortOddsMap()

                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            recommend.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion

                        //region 主要市場那列選擇狀態配置
                        /*leagueAdapter.playSelectedCodeSelectionType =
                            getPlaySelectedCodeSelectionType()
                        leagueAdapter.playSelectedCode = getPlaySelectedCode()*/
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