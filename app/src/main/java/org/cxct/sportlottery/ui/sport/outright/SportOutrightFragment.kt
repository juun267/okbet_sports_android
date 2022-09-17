package org.cxct.sportlottery.ui.sport.outright

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_sport_list.*
import kotlinx.android.synthetic.main.fragment_sport_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.odds.OutrightItem
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.SportViewModel
import org.cxct.sportlottery.ui.sport.OutrightOddListener
import org.cxct.sportlottery.ui.sport.SportLeagueAdapter
import org.cxct.sportlottery.ui.sport.SportOutrightAdapter
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.widget.VerticalDecoration
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
@SuppressLint("NotifyDataSetChanged", "LogNotTimber")
@RequiresApi(Build.VERSION_CODES.M)
class SportOutrightFragment : BaseBottomNavigationFragment<SportViewModel>(SportViewModel::class) {
    companion object {
        fun newInstance(
            gameType: String? = GameType.FT.key,
            outrightLeagueId: String? = null,
        ): SportOutrightFragment {
            val args = Bundle()
            args.putString("gameType", gameType)
            outrightLeagueId?.let {
                args.putString("outrightLeagueId", outrightLeagueId)
            }
            val fragment = SportOutrightFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    private val args: GameV3FragmentArgs by navArgs()
    private val matchType = MatchType.OUTRIGHT
    private val gameType by lazy { arguments?.getString("gameType", GameType.FT.key) }
    private val outrightLeagueId by lazy { arguments?.getString("outrightLeagueId", null) }
    private var mView: View? = null
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾
    private var mCalendarSelected = false //紀錄日期圖示選中狀態
    var leagueIdList = mutableListOf<String>()

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {

                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                    //日期圖示選取狀態下，切換球種要重置UI狀態
                    if (iv_calendar.isSelected) iv_calendar.performClick()
                    (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                        sport_type_list,
                        RecyclerView.State(),
                        dataSport.indexOfFirst { item -> TextUtils.equals(it.code, item.code) })
                }

                sportOutrightAdapter.setPreloadItem()

                //切換球種後要重置位置
                loading()
                unSubscribeChannelHallAll()
                viewModel.switchGameType(it)
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(matchType, it)
                loading()
            }
        }
    }
    private val sportOutrightAdapter by lazy {
        SportOutrightAdapter().apply {
            outrightOddListener = OutrightOddListener(
                clickListenerBet = { outrightItem, odd, playCateCode ->
                    outrightItem.matchOdd.let {
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOutRightOddsDialog(it, odd, playCateCode)
                        }
                    }
                },
                onClickMatch = { outrightItem ->
                    outrightItem.matchOdd.matchInfo?.let {
                        navMatchDetailPage(it)
                    }
                },
                clickExpand = { outrightItem ->
                    outrightItem.leagueExpanded = !outrightItem.leagueExpanded
                    outrightItem.matchOdd.isExpand = outrightItem.leagueExpanded
                    subscribeChannelHall(outrightItem?.matchOdd)
                    updateOutrightAdapterInMain(outrightItem)
                }
            )
        }
    }

    private fun updateOutrightAdapterInMain(any: Any) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (game_list.adapter is SportOutrightAdapter) {
                sportOutrightAdapter.notifyItemChanged(sportOutrightAdapter.data.indexOf(any))
            }
        }
    }

    private fun navMatchDetailPage(matchInfo: MatchInfo?) {
        matchInfo?.let { it ->
            SportDetailActivity.startActivity(requireContext(),
                matchInfo = it,
                matchType = matchType)
        }
    }

    private var isUpdatingLeague = false

    private var mLeagueOddList = ArrayList<LeagueOdd>()
    private var mQuickOddListMap = HashMap<String, MutableList<QuickPlayCate>>()

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    override fun loading() {
        stopTimer()
    }

    override fun hideLoading() {
        if (timer == null) startTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.resetOtherSeelectedGameType()
        mView = inflater.inflate(R.layout.fragment_sport_list, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //若為起始fragment不會有轉場動畫, 故無法透過afterAnimateListener動作
        setupSportTypeList()
        setupToolbar()
        setupGameRow()
        setupGameListView()
        initObserve()
        initSocketObserver()
    }

    private fun setupSportTypeList() {
        sport_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = gameTypeAdapter
            removeItemDecorations()
//            addItemDecoration(
//                SpaceItemDecoration(
//                    context,
//                    R.dimen.recyclerview_item_dec_spec_sport_type
//                )
//            )
        }
    }

    private fun setupToolbar() {
        iv_calendar.apply {
            visibility = when (matchType) {
                MatchType.EARLY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                val newSelectedStatus = !isSelected
                mCalendarSelected = newSelectedStatus
                isSelected = newSelectedStatus

                view?.game_filter_type_list?.visibility = when (iv_calendar.isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }
        lin_filter.setOnClickListener {
            gameType?.let {
                LeagueSelectActivity.start(requireContext(),
                    it,
                    matchType,
                    null,
                    null,
                    leagueIdList)
            }
        }
        iv_arrow.setOnClickListener {
            iv_arrow.isSelected = !iv_arrow.isSelected
            sportOutrightAdapter.data.forEach { it ->
                when (it) {
                    is OutrightItem ->
                        it.leagueExpanded = iv_arrow.isSelected
                }
            }
            sportOutrightAdapter.notifyDataSetChanged()
        }
        iv_arrow.isSelected = true
    }


    private fun setupGameRow() {
        game_filter_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_date
                )
            )
        }
        if (matchType == MatchType.EARLY) {
            mCalendarSelected = true
            iv_calendar.visibility = View.VISIBLE
            game_filter_type_list.visibility = View.VISIBLE
        } else {
            mCalendarSelected = false
            iv_calendar.visibility = View.GONE
            game_filter_type_list.visibility = View.GONE
        }
    }

    private fun setupGameListView() {
        game_list.apply {
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(VerticalDecoration(context, R.drawable.bg_divide_light_blue_8))
            adapter = sportOutrightAdapter
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    when (adapter) {
                        //冠軍
                        is SportOutrightAdapter -> {
                            it.forEach { pair ->
                                if (pair.first < sportOutrightAdapter.data.size) {
                                    val outrightDataList = sportOutrightAdapter.data[pair.first]
                                    when (outrightDataList) {
                                        is OutrightItem -> {
                                            outrightDataList.matchOdd
                                        }
                                        else -> {
                                            null
                                        }
                                    }?.let { itemMatchOdd ->
                                        Log.d(
                                            "[subscribe]",
                                            "訂閱 ${itemMatchOdd.matchInfo?.name} -> " +
                                                    "${itemMatchOdd.matchInfo?.homeName} vs " +
                                                    "${itemMatchOdd.matchInfo?.awayName}"
                                        )
                                        subscribeChannelHall(
                                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                                            itemMatchOdd.matchInfo?.id
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
            if (viewModel.getMatchCount(matchType) < 1) {

            } else {
                sportOutrightAdapter.setPreloadItem()
            }
        }
    }

    /**
     * 設置冠軍adapter, 訂閱當前頁面上的資料
     */
    private fun setOutrightLeagueAdapter() {
        if (game_list.adapter !is SportOutrightAdapter) {
            game_list.adapter = sportOutrightAdapter
        }

        if (game_list.adapter is SportOutrightAdapter) {
            Handler().postDelayed(
                {
                    game_list?.firstVisibleRange(
                        GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key,
                        sportOutrightAdapter,
                        activity ?: requireActivity()
                    )
                },
                400
            )
        }
    }

    private fun initObserve() {
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it != null && it.isNotBlank()) {
                context?.let { context ->
                    val dialog = CustomAlertDialog(context)
                    dialog.setTitle(resources.getString(R.string.prompt))
                    dialog.setMessage(it)
                    dialog.setTextColor(R.color.color_E44438_e44438)
                    dialog.setNegativeButtonText(null)
                    dialog.setPositiveClickListener {
                        viewModel.resetErrorDialogMsg()
                        dialog.dismiss()
                        back()
                    }
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.isCancelable = false
                    dialog.show(childFragmentManager, null)
                }
            }
        }

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner) {
            when (matchType) {
                MatchType.IN_PLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }

                MatchType.TODAY -> {
                    updateSportType(it?.sportMenuData?.menu?.today?.items ?: listOf())
                }

                MatchType.EARLY -> {
                    updateSportType(it?.sportMenuData?.menu?.early?.items ?: listOf())
                }
                MatchType.CS -> {
                    updateSportType(it?.sportMenuData?.menu?.cs?.items ?: listOf())
                }

                MatchType.PARLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.parlay?.items ?: listOf())
                }

                MatchType.OUTRIGHT -> {
                    updateSportType(it?.sportMenuData?.menu?.outright?.items ?: listOf())
                }

                MatchType.AT_START -> {
                    updateSportType(it?.sportMenuData?.atStart?.items ?: listOf())
                }

                MatchType.EPS -> {
                    updateSportType(it?.sportMenuData?.menu?.eps?.items ?: listOf())
                }

                MatchType.OTHER -> {
                    val tempItem: MutableList<Item> = mutableListOf()
                    viewModel.specialMenuData?.items?.forEach { item ->
                        val mItem = Item(
                            code = item.code ?: "",
                            name = item.name ?: "",
                            num = item.num ?: 0,
                            play = null,
                            sortNum = item.sortNum ?: 0,
                        )
                        mItem.hasPlay = (item.play != null)
                        mItem.isSelected = item.isSelected
                        tempItem.add(mItem)
                    }
                    updateSportType(tempItem)
                }

                else -> {
                }
            }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
            (game_filter_type_list.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                game_filter_type_list,
                RecyclerView.State(),
                position
            )
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->

        }
        viewModel.outrightMatchList.observe(this.viewLifecycleOwner) {
            it.peekContent()?.let { outrightMatchList ->
                sportOutrightAdapter.data = outrightMatchList as List<OutrightItem>
                setOutrightLeagueAdapter()
                hideLoading()
            }
        }

        //KK要求，當球類沒有資料時，自動選取第一個有賽事的球種
        viewModel.isNoHistory.observe(this.viewLifecycleOwner) {
            when {
                //當前MatchType有玩法數量，只是目前的球種沒有
                it && curMatchTypeHasMatch() -> {
                    unSubscribeChannelHallAll()
                    if (matchType != MatchType.OTHER) {
                        viewModel.switchMatchType(matchType)
                    }
                }
            }

            hideLoading()
        }

        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { betInfoList ->
                sportOutrightAdapter.data.filterIsInstance<OutrightItem>().forEach { outrightItem ->
                    outrightItem.oddsList.forEach { odds ->
                        odds.forEach { odd ->
                            val betInfoSelected = betInfoList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd.id
                            }
                            if (odd.isSelected != betInfoSelected) {
                                odd.isSelected = betInfoSelected
                                sportOutrightAdapter.updateOdds(odd)
                            }
                        }
                    }
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {

        }

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner) {
//            countryAdapter.apply {
//                data.forEach { row ->
//                    row.list.forEach { league ->
//                        league.isSelected = it.any { it.id == league.id }
//                    }
//                }
//
//                notifyDataSetChanged()
//            }
        }

        viewModel.playList.observe(this.viewLifecycleOwner) { event ->

        }

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {

        }
        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner) {

        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {

        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            sport_type_list.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
        }

        viewModel.checkInListFromSocket.observe(this.viewLifecycleOwner) { leagueChangeEvent ->
            if (leagueChangeEvent != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isUpdatingLeague) {
                        isUpdatingLeague = true

                        //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
                        val nowGameType =
                            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)?.key

                        isUpdatingLeague = false
                    }
                }
            }
        }
    }

    /**
     * 判斷當前MatchType是否有玩法數量
     */
    private fun curMatchTypeHasMatch(): Boolean {
        return when (matchType) {
            MatchType.IN_PLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.inPlay?.num ?: 0 > 0
            MatchType.TODAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.today?.num ?: 0 > 0
            MatchType.AT_START -> viewModel.sportMenuResult.value?.sportMenuData?.atStart?.num ?: 0 > 0
            MatchType.EARLY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.early?.num ?: 0 > 0
            MatchType.PARLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.parlay?.num ?: 0 > 0
            MatchType.OUTRIGHT -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.outright?.num ?: 0 > 0
            MatchType.EPS -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.eps?.num ?: 0 > 0
            MatchType.OTHER -> viewModel.specialMenuData?.items?.size ?: 0 > 0
            else -> false
        }
    }

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.firstSwitchMatch(matchType = matchType)
                if (!gameType.isNullOrEmpty() && matchType == MatchType.OUTRIGHT) {
                    gameType?.let { gameType ->
                        viewModel.getOutrightOddsList(gameType = gameType,
                            leagueIdList = leagueIdList)
                    }
                } else {
                    viewModel.getGameHallList(
                        matchType = matchType,
                        isReloadDate = true,
                        isReloadPlayCate = false,
                        isLastSportType = true
                        )
                    }
                    subscribeSportChannelHall()
                } else {
                    stopTimer()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                when (game_list.adapter) {

                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }
                }
            }
        }

        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
                when (game_list?.adapter) {
                    is SportOutrightAdapter -> {
                        viewModel.updateOutrightOddsChange(context, oddsChangeEvent)
                    }
                }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is SportLeagueAdapter -> {
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }

                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelHallAll()

                when (game_list.adapter) {
                    is SportLeagueAdapter -> {

                    }
                }
            }
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                viewModel.checkGameInList(
                    matchType = matchType,
                    leagueChangeEvent = leagueChangeEvent,
                )
                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->

        }
    }

    private fun updateGameList(index: Int, leagueOdd: LeagueOdd) {

    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (leagueOdd.matchOdds.filter { matchOdd ->
                    matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                }.any { matchOdd ->
                    matchOdd.oddsMap?.values?.any { oddList ->
                        oddList?.any { odd ->
                            odd?.isSelected == true
                        } == true
                    } == true
                }) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun updateBetInfo(
        epsLeagueOddsItem: EpsLeagueOddsItem,
        oddsChangeEvent: OddsChangeEvent,
    ) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (epsLeagueOddsItem.leagueOdds?.matchOdds?.filter { matchOddsItem -> matchOddsItem.matchInfo?.id == oddsChangeEvent.eventId }
                    ?.any { matchOdd ->
                        matchOdd.oddsMap?.values?.any { oddList ->
                            oddList?.any { odd ->
                                odd?.isSelected == true
                            } == true
                        } == true
                    } == true) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }


    private fun MutableList<LeagueOdd>.sortOddsMap() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { MatchOdd ->
                MatchOdd.oddsMap?.forEach { (_, value) ->
                    if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                        value?.sortBy {
                            it?.marketSort
                        }
                    }
                }
            }
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        gameTypeAdapter.dataSport = gameTypeList
        //post待view繪製完成
        sport_type_list?.post {
            //球種如果選過，下次回來也需要滑動置中
            if (!gameTypeList.isNullOrEmpty()) {
                (sport_type_list?.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                    sport_type_list,
                    RecyclerView.State(),
                    gameTypeList.indexOfFirst { item ->
                        item.isSelected
                    }
                )
            }
            if (gameTypeList.isEmpty()) {
                sport_type_list?.visibility = View.GONE
                iv_calendar?.visibility = View.GONE
                game_filter_type_list?.visibility = View.GONE
            } else {
                sport_type_list?.visibility = if (mLeagueIsFiltered) View.GONE else View.VISIBLE
                iv_calendar?.apply {
                    visibility = when (matchType) {
                        MatchType.EARLY -> View.VISIBLE
                        else -> View.GONE
                    }
                    isSelected = mCalendarSelected
                }
            }
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }


    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            val fastBetDataBean = FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName = "",
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null,
            )
            (activity as MainTabActivity).setupBetData(fastBetDataBean)
        }

    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            matchInfo?.let { matchInfo ->
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
                )
                (activity as MainTabActivity).setupBetData(fastBetDataBean)
            }
        }
    }


    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd?) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
        gameType?.let {
            subscribeChannelHall(
                it.key,
                matchOdd?.matchInfo?.id
            )
        }
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                matchOdd.matchInfo?.id
            )
        }
    }

    private fun subscribeChannelHall(epsLeagueOddsItem: EpsLeagueOddsItem) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
            when (epsLeagueOddsItem.isClose) {
                true -> {
                    unSubscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
                false -> {
                    subscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
            }
        }
    }

    private var timer: Timer? = null

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModel.setCurMatchType(null) //避免matchType和curMatchType相同，導致後續流程中斷的問題
                viewModel.switchMatchType(matchType)
            }
        }, 60 * 3 * 1000L, 60 * 3 * 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelectedLeague()
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    // region handle LeagueOdd data
    private fun clearQuickPlayCateSelected() {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.isExpand = false
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }
    }

    private fun setQuickPlayCateSelected(
        selectedMatchOdd: MatchOdd,
        selectedQuickPlayCate: QuickPlayCate,
    ) {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected =
                            true
                    }
                }
            }
        }
    }

    @Subscribe
    fun onSelectLeague(leagueList: List<League>) {
        viewModel.filterLeague(leagueList)
        leagueIdList.clear()
        leagueList.forEach {
            leagueIdList.add(it.id)
        }
        viewModel.getGameHallList(
            matchType = matchType,
            isReloadDate = true,
            isReloadPlayCate = false,
            isLastSportType = true,
            leagueIdList = leagueIdList
        )
    }
}