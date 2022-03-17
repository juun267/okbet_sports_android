package org.cxct.sportlottery.ui.game.home

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.home_game_highlight_title.view.*
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import kotlinx.android.synthetic.main.home_game_table_4.view.indicator_view
import kotlinx.android.synthetic.main.home_game_table_4.view.view_pager
import kotlinx.android.synthetic.main.home_sport_table_4.view.*
import kotlinx.android.synthetic.main.itemview_sport_type_list.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchCategory.result.MatchRecommendResult
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.matchCategory.result.RECOMMEND_OUTRIGHT
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.hall.adapter.GameTypeListener
import org.cxct.sportlottery.ui.game.home.gameTable4.GameEntity
import org.cxct.sportlottery.ui.game.home.gameTable4.OtherMatch
import org.cxct.sportlottery.ui.game.home.gameTable4.Vp2GameTable4Adapter
import org.cxct.sportlottery.ui.game.home.recommend.OddBean
import org.cxct.sportlottery.ui.game.home.recommend.RecommendGameEntity
import org.cxct.sportlottery.ui.game.interfaces.UpdateHighLightInterface
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager.getGameIcon
import org.cxct.sportlottery.util.GameConfigManager.getTitleBarBackground
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.MatchOddUtil.updateEPSDiscount
import org.cxct.sportlottery.util.RecyclerViewGridDecoration
import org.cxct.sportlottery.util.TimeUtil
import kotlin.collections.ArrayList

/**
 * @author Hewie
 * 整合RvGameTable4Adapter, GameTypeAdapter, RvHighlightAdapter, RvRecommendAdapter
 * 以及相關元件，包含標題及底層資訊等
 */
/**
 * TODO 預期架構
 * 架構調整為API只更新資料，不更新列表
 * 列表由Adapter本身自行更新
 * 只有顯示中的ViewItem會根據Adapter的統一資料更新自己
 * 在外部除了初始化之外，不呼叫任何notifyDataSetChanged
 */
class HomeListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 排序對應表
    private val SORTMAP = mapOf<Any, Int>(
        HomeGameTableBarItemData::class to 1,
        GameEntity::class to 2,
        HomeRecommendBarItemData::class to 3,
        RecommendGameEntity::class to 4,
        HomeHighlightGameBarItemData::class to 5,
        HighlightGameTypeItemData::class to 6,
        HomeHighlightGameTitleItemData::class to 7,
        MatchOdd::class to 8,
        HomeBottomNavigationItemData::class to 9
    )

    // 接收任何型別
    private var mDataList = mutableListOf<Any>()

    private var mUpdateHighLightInterfaceListener = HashMap<Int, UpdateHighLightInterface>()

    private var mLastMatchOdd: MatchOdd? = null

    // region GameTable params
    private var mMatchType: MatchType = MatchType.IN_PLAY
    private var selectedOdds = mutableListOf<String>()
    private var oddsType: OddsType = OddsType.EU
    var isLogin: Boolean? = false

    var onClickOddListener: OnClickOddListener? = null
    var onClickMatchListener: OnSelectItemListener<MatchInfo>? = null
    var onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null
    var onClickSportListener: OnSelectItemListener<OtherMatch>? = null
    var onClickFavoriteListener: OnClickFavoriteListener? = null
    var onClickStatisticsListener: OnClickStatisticsListener? = null
    var onSubscribeChannelHallListener: OnSubscribeChannelHallListener? = null

    var onGameTableBarViewHolderListener: GameTableBarViewHolder.Listener? = null
    // endregion

    // region GameType params
    var gameTypeListener: GameTypeListener? = null
    // endregion

    // region Recommend params
    private var discount: Float = 1.0F
    var recommendOddsType: OddsType = OddsType.EU
    var onRecommendClickMatchListener: OnSelectItemListener<RecommendGameEntity>? = null
    var onRecommendClickOddListener: OnClickOddListener? = null
    var onRecommendClickOutrightOddListener: OnClickOddListener? = null
    var onRecommendClickMoreListener: OnClickMoreListener? = null
    // endregion

    // region HighLight
    var highLightOddsType: OddsType = OddsType.EU
    var onHighLightClickOddListener: OnClickOddListener? = null
    var onHighLightClickMatchListener: OnSelectItemListener<MatchOdd>? = null //賽事畫面跳轉
    var onHighLightClickFavoriteListener: OnClickFavoriteListener? = null
    var onHighLightClickStatisticsListener: OnClickStatisticsListener? = null
    // endregion

    enum class ItemType {
        NONE,
        GAME_TABLE_BAR, // rg_table_bar
        ODD_DATA, SPORT_GRID_REPAY, // RvGameTable4Adapter
        RECOMMEND_GAME_BAR, RECOMMEND_GAME, // RvRecommendAdapter
        HIGH_LIGHT_BAR, HIGH_LIGHT_TITLE, SPORT_HOME, // GameTypeAdapter
        MATCHODD, // RvHighlightAdapter
        BOTTOM_NAVIGATION
    }

    // region ItemClass
    class HomeGameTableBarItemData {
        var inPlayResult: MatchPreloadResult? = null
        var atStartResult: MatchPreloadResult? = null
    }
    class HomeRecommendBarItemData {

    }
    class HighlightGameTypeItemData {
        var dataSport = arrayListOf<Item>()
    }
    class HomeHighlightGameTitleItemData {
        var highlightGameTitle: String = ""
        var highlightGameIcon: Int = R.drawable.ic_soccer
        var highlightGameBackground: Int = R.drawable.img_home_title_soccer_background
    }
    class HomeHighlightGameBarItemData {}
    class HomeBottomNavigationItemData {}
    // endregion

    override fun getItemViewType(position: Int): Int {
        return when(val data = mDataList[position]) {
            is HomeGameTableBarItemData -> {
                ItemType.GAME_TABLE_BAR.ordinal
            }
            // RvGameTable4Adapter的列表
            is GameEntity -> {
                when(data.otherMatch.isNullOrEmpty()) {
                    true -> ItemType.ODD_DATA.ordinal
                    false -> ItemType.SPORT_GRID_REPAY.ordinal
                }
            }
            // GameTypeAdapter的列表
            is HighlightGameTypeItemData -> {
                ItemType.SPORT_HOME.ordinal
            }
            // RvRecommendAdapter的列表
            is HomeRecommendBarItemData -> {
                ItemType.RECOMMEND_GAME_BAR.ordinal
            }
            is RecommendGameEntity -> {
                ItemType.RECOMMEND_GAME.ordinal
            }
            // RvHighlightAdapter的列表
            is HomeHighlightGameBarItemData -> {
                ItemType.HIGH_LIGHT_BAR.ordinal
            }
            is HomeHighlightGameTitleItemData -> {
                ItemType.HIGH_LIGHT_TITLE.ordinal
            }
            is MatchOdd -> {
                ItemType.MATCHODD.ordinal
            }
            is HomeBottomNavigationItemData -> {
                ItemType.BOTTOM_NAVIGATION.ordinal
            }
            else -> { ItemType.NONE.ordinal }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ItemType.HIGH_LIGHT_BAR.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_highlight_bar, parent, false)
                return GameHighLightBarViewHolder(layout)
            }
            ItemType.HIGH_LIGHT_TITLE.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_highlight_title, parent, false)
                return GameHighLightTitleViewHolder(layout)
            }
            ItemType.RECOMMEND_GAME_BAR.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_recommend_bar, parent, false)
                return GameRecommendBarViewHolder(layout)
            }
            ItemType.GAME_TABLE_BAR.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_bar, parent, false)
                return GameTableBarViewHolder(layout)
            }
            ItemType.ODD_DATA.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_4, parent, false)
                return GameTableViewHolder(layout)
            }
            ItemType.SPORT_GRID_REPAY.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_sport_table_4, parent, false)
                return SportGridViewHolder(layout)
            }
            ItemType.SPORT_HOME.ordinal -> { return HighlightGameTypeViewHolder.from(parent) }
            ItemType.RECOMMEND_GAME.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_recommend_item, parent, false)
                return RecommendViewHolder(layout)
            }
            ItemType.MATCHODD.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_highlight_item, parent, false)
                return ViewHolderHdpOu(layout)
            }
            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_bottom_navigation, parent, false)
                return HomeBottomViewHolder(layout)
            }
            else -> {
                return UndefinedViewHolder(View(parent.context))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mDataList[position]
        when (holder) {
            is GameTableBarViewHolder -> {
                holder.apply {
                    this.setOnGameTableSelectListener(onGameTableBarViewHolderListener)
                    bind((data as HomeGameTableBarItemData))
                }
            }
            is GameTableViewHolder -> {
                holder.apply {
                    bind(data as GameEntity)
                }
            }
            is SportGridViewHolder ->{
                holder.apply {
                    bind((data as GameEntity).otherMatch)
                }
            }
            // 賽事精選(選單)
            is HighlightGameTypeViewHolder -> {
                //val item = dataSport[position]
                holder.bind((data as HighlightGameTypeItemData), gameTypeListener)
            }
            // 賽事推薦
            is RecommendViewHolder -> {
                holder.onClickMatchListener = onRecommendClickMatchListener
                holder.onClickOddListener = onRecommendClickOddListener
                holder.onClickOutrightOddListener = onRecommendClickOutrightOddListener
                holder.onClickMoreListener = onRecommendClickMoreListener
                holder.bind((data as RecommendGameEntity), recommendOddsType)
            }
            is GameHighLightTitleViewHolder -> {
                holder.bind((data as HomeHighlightGameTitleItemData))
            }
            // 賽事精選(列表)
            is ViewHolderHdpOu -> {
                holder.onClickOddListener = onHighLightClickOddListener
                holder.onClickMatchListener = onHighLightClickMatchListener
                holder.onClickFavoriteListener = onHighLightClickFavoriteListener
                holder.onClickStatisticsListener = onHighLightClickStatisticsListener
                holder.bind((data as MatchOdd), mLastMatchOdd ?: data, highLightOddsType)
                mUpdateHighLightInterfaceListener[position] = holder.getUpdateHighLightInterface()
            }
            // 底部資訊
            is HomeBottomViewHolder -> {
                // TODO
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if(holder is HighlightGameTypeViewHolder) { // TODO
            holder.saveInstanceState = holder.itemView.rvSportType.layoutManager?.onSaveInstanceState()
        }
        if(holder is RecommendViewHolder) {
            holder.saveInstanceState = holder.itemView.view_pager.currentItem
        }
        if(holder is GameTableViewHolder) {
            holder.saveInstanceState = holder.itemView.view_pager.currentItem
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if(holder is HighlightGameTypeViewHolder) {
            holder.itemView.rvSportType.layoutManager?.onRestoreInstanceState(holder.saveInstanceState)
        }
        if(holder is RecommendViewHolder) {
            holder.itemView.view_pager.setCurrentItem(holder.saveInstanceState, false)
        }
        if(holder is GameTableViewHolder) {
            holder.itemView.view_pager.setCurrentItem(holder.saveInstanceState, false)
        }
    }

    override fun getItemCount(): Int = mDataList.size

    fun updateHighLightList() {
        mUpdateHighLightInterfaceListener.forEach {
            val data = mDataList[it.key]
            if(data is MatchOdd) {
                it.value.doUpdate(data, data, highLightOddsType)
            }
        }
    }

    fun setGameHighLightTitle(homeHighlightGameTitleItemData: HomeHighlightGameTitleItemData = HomeHighlightGameTitleItemData()) {
        removeDatas(homeHighlightGameTitleItemData)
        addDataWithSort(homeHighlightGameTitleItemData)
    }
    fun updateHightLightTitle(selectItem: Item) {
        val homeHighlightGameTitleItemData = HomeHighlightGameTitleItemData().apply {
            this.highlightGameTitle = selectItem.name
            this.highlightGameIcon = getGameIcon(selectItem.code) ?: R.drawable.ic_soccer
            this.highlightGameBackground = getTitleBarBackground(selectItem.code) ?: R.drawable.img_home_title_soccer_background
        }
        removeDatas(homeHighlightGameTitleItemData)
        addDataWithSort(homeHighlightGameTitleItemData)
    }

    // region set/get Data
    // region HighLight Bar Data
    fun setGameHighLightBar(homeHighlightGameBarItemData: HomeHighlightGameBarItemData = HomeHighlightGameBarItemData()) {
        removeDatas(homeHighlightGameBarItemData)
        addDataWithSort(homeHighlightGameBarItemData)
    }
    // endregion
    // region GameTable Bar Data
    fun setGameTableBar(homeGameTableBarItemData: HomeGameTableBarItemData? = HomeGameTableBarItemData()) {
        homeGameTableBarItemData?.let {
            removeDatas(it)
            val inPlayCount = homeGameTableBarItemData.inPlayResult?.matchPreloadData?.num ?: 0
            val atStartCount = homeGameTableBarItemData.atStartResult?.matchPreloadData?.num ?: 0
            if (inPlayCount != 0 && atStartCount != 0) addDataWithSort(it)
        }
    }
    // endregion
    // region Recommend Bar Data
    fun setGameRecommendBar(homeRecommendBarItemData: HomeRecommendBarItemData = HomeRecommendBarItemData()) {
        removeDatas(homeRecommendBarItemData)
        addDataWithSort(homeRecommendBarItemData)
    }
    // endregion
    // region GameTable Data
    fun setGameTableData(dataList: MutableList<GameEntity>, matchType: MatchType, selectedOdds: MutableList<String>) {
        this.mMatchType = matchType
        this.selectedOdds = selectedOdds
        removeDatas(dataList.firstOrNull())
        dataList.forEach { addDataWithSort(it) }
    }
    fun getGameEntityData(): MutableList<GameEntity> {
        val result = mutableListOf<GameEntity>()
        mDataList.filter { it is GameEntity }.forEach { result.add(it as GameEntity) }
        return result
    }
    // endregion
    // region 賽事精選資料(選單)
    fun setDataSport(dataSport: ArrayList<Item>) {
        val highlightGameType = HighlightGameTypeItemData()
        highlightGameType.dataSport = dataSport
        removeDatas(highlightGameType)
        addDataWithSort(highlightGameType)
    }
    fun getDataSport(): ArrayList<Item> {
        var result = HighlightGameTypeItemData()
        mDataList.filter { it is HighlightGameTypeItemData }.forEach { result = (it as HighlightGameTypeItemData) }
        return result.dataSport
    }
    fun updateDataSport() {
        mDataList.forEachIndexed { index, any ->
            if(any is HighlightGameTypeItemData) {
                notifyItemChanged(index)
            }
        }
    }
    // endregion
    // region 賽事精選資料
    fun setMatchOdd(sportCode: String?, newList: List<OddData>?, selectedOdds: MutableList<String>) {
        val list = newList?.mapIndexed { index, it ->
            val matchInfo = MatchInfo(
                gameType = sportCode,
                awayName = it.matchInfo?.awayName ?: "",
                endTime = it.matchInfo?.endTime,
                homeName = it.matchInfo?.homeName ?: "",
                id = it.matchInfo?.id ?: "",
                playCateNum = it.matchInfo?.playCateNum ?: 0,
                startTime = it.matchInfo?.startTime,
                eps = it.matchInfo?.eps,
                spt = it.matchInfo?.spt,
                liveVideo = it.matchInfo?.liveVideo,
                status = it.matchInfo?.status ?: -1).apply {
                startDateDisplay = TimeUtil.timeFormat(this.startTime, "MM/dd")
                startTimeDisplay = TimeUtil.timeFormat(this.startTime, "HH:mm")
                isAtStart = TimeUtil.isTimeAtStart(this.startTime)
                isStartPosition = index == 0
                isLastPosition = index == newList.size - 1
            }

            val odds: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()
            it.oddsMap.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.id?.let {
                        odd?.isSelected = selectedOdds.contains(it)
                    }
                }
                odds[key] = value?.toMutableList()
            }

            MatchOdd(
                it.betPlayCateNameMap,
                it.playCateNameMap,
                matchInfo,
                odds,
                it.dynamicMarkets,
                it.quickPlayCateList,
                it.oddsSort
            )
        } ?: listOf()
        mLastMatchOdd = list.last()
        removeDatas(list.firstOrNull())
        list.forEach { addDataWithSort(it) }
    }
    fun getMatchOdd(): MutableList<MatchOdd> {
        val result = mutableListOf<MatchOdd>()
        mDataList.filter { it is MatchOdd }.forEach { result.add(it as MatchOdd) }
        return result
    }
    // endregion
    // region 賽事推薦資料
    fun setRecommendData(result: MatchRecommendResult, selectedOdds: MutableList<String>) {
        val dataList = mutableListOf<RecommendGameEntity>()
        result.rows?.forEach { row ->
            row.leagueOdds?.matchOdds?.forEach { oddData ->
                val beans = oddData.oddsMap.toSortedMap(compareBy<String> {
                    val sortOrder = oddData.oddsSort?.split(",")
                    sortOrder?.indexOf(it)
                }.thenBy { it }).map {
                    OddBean(it.key, it.value?.toList() ?: listOf())
                }

                beans.forEach {
                    it.oddList?.forEach { odd ->
                        odd?.id?.let { id ->
                            odd?.isSelected = selectedOdds.contains(id)
                        }
                    }
                }

                val entity = RecommendGameEntity(
                    code = row.sport?.code,
                    name = row.sport?.name,
                    leagueId = row.leagueOdds.league?.id,
                    leagueName = if (row.isOutright == RECOMMEND_OUTRIGHT) row.leagueOdds.matchOdds.firstOrNull()?.matchInfo?.name else row.leagueOdds.league?.name,
                    matchInfo = oddData.matchInfo,
                    isOutright = row.isOutright,
                    oddBeans = beans,
                    dynamicMarkets = oddData.dynamicMarkets,
                    playCateMappingList = oddData.playCateMappingList,
                    betPlayCateNameMap = oddData.betPlayCateNameMap,
                    playCateNameMap = oddData.playCateNameMap
                )
                dataList.add(entity)
            }
        }
        removeDatas(dataList.firstOrNull())
        dataList.forEach { addDataWithSort(it) }
    }
    fun getRecommendData(): MutableList<RecommendGameEntity> {
        val result = mutableListOf<RecommendGameEntity>()
        mDataList.filter { it is RecommendGameEntity }.forEach { result.add(it as RecommendGameEntity) }
        return result
    }
    fun setDiscount(newDiscount: Float) {
        getRecommendData().forEach { recommendGameEntity ->
            recommendGameEntity.oddBeans.forEach { oddBean ->
                oddBean.oddList.forEach { odd ->
                    if (oddBean.playTypeCode == PlayCate.EPS.value)
                        odd?.updateEPSDiscount(discount, newDiscount)
                    else
                        odd?.updateDiscount(discount, newDiscount)
                }
            }
        }
        // TODO notifyDataSetChanged()
    }
    // endregion
    // region 底部資訊
    fun setBottomNavigation(homeBottomNavigationItemData: HomeBottomNavigationItemData = HomeBottomNavigationItemData()) {
        removeDatas(homeBottomNavigationItemData)
        addDataWithSort(homeBottomNavigationItemData)
    }
    // endregion
    // endregion

    // region ViewHolders
    class UndefinedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Recommend Game Bar
    inner class GameRecommendBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

        }

        fun bind() {

        }
    }
    // HighLight Bar
    inner class GameHighLightBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

        }

        fun bind() {

        }
    }
    // HighLight Title
    inner class GameHighLightTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

        }

        fun bind(data: HomeHighlightGameTitleItemData) {
            itemView.highlight_tv_game_name.text = data.highlightGameTitle
            itemView.highlight_iv_game_icon.setImageResource(data.highlightGameIcon)
            itemView.highlight_titleBar.setBackgroundResource(data.highlightGameBackground)
        }
    }
    // RvGameTable4Adapter
    inner class GameTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var saveInstanceState: Int = 0

        init {
            itemView.apply {
                view_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
            }
        }

        fun bind(data: GameEntity) {
            itemView.apply {
                tv_game_name.text = data.name
                tv_game_num.text = data.num.toString()
                val gameCode = data.code
                getGameIcon(gameCode)?.let {
                    iv_game_icon.setImageResource(it)
                }
                getTitleBarBackground(gameCode)?.let {
                    titleBar.setBackgroundResource(it)
                }
                titleBar.setOnClickListener {
                    onClickTotalMatchListener?.onClick(data)
                }

                data.matchOdds?.let {
                    // TODO 這裡存在一個隱性的效能問題
                    if (data.vpTableAdapter == null) data.vpTableAdapter = Vp2GameTable4Adapter(mMatchType)
                    data.vpTableAdapter?.onClickMatchListener = onClickMatchListener
                    data.vpTableAdapter?.onClickOddListener = onClickOddListener
                    data.vpTableAdapter?.onClickFavoriteListener = onClickFavoriteListener
                    data.vpTableAdapter?.onClickStatisticsListener = onClickStatisticsListener
                    data.vpTableAdapter?.setData(data.code ?: "", it, isLogin ?: false, oddsType, data.playCateNameMap ?: mapOf(), selectedOdds)
                    view_pager.adapter = data.vpTableAdapter

                    indicator_view.setupWithViewPager2(view_pager)
                    indicator_view.apply {
                        visibility = if (it.size <= 1) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                }

                OverScrollDecoratorHelper.setUpOverScroll(
                    view_pager.getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
            }
        }
    }
    inner class SportGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var adapter: CommonAdapter<OtherMatch>
        init {
            itemView.apply {
                rvSport.layoutManager =
                    LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                rvSport.apply {
                    layoutManager = GridLayoutManager(context, 2)
                }
                rvSport.addItemDecoration(
                    RecyclerViewGridDecoration(
                        2,
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt()
                    )
                )
                rvSport.isNestedScrollingEnabled = false
            }
        }

        fun bind(data: List<OtherMatch>?) {
            itemView.apply {
                adapter = object : CommonAdapter<OtherMatch>( context, R.layout.item_home_sport, data ) {
                    override fun convert( holder: ViewHolder, t: OtherMatch, position: Int ) {
                        getGameIcon(t.code)?.let {
                            holder.getView<ImageView>(R.id.ivSportLogo).setImageResource(it)
                        }
                        holder.setText(R.id.tvSport, t.name)
                        holder.setText(R.id.tvSportCount, t.num.toString())
                        holder.getView<LinearLayout>(R.id.layoutSport).setOnClickListener {
                            onClickSportListener?.onClick(t)
                        }
                    }
                }
                rvSport.adapter = adapter
            }
        }
    }
    // endregion

    // region GameTable 指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexMatchOdd: Int) {
        if (index >= 0 && indexMatchOdd >= 0) {
            val data = mDataList[index]
            if (data is GameEntity) {
                data.vpTableAdapter?.notifyItemChanged(indexMatchOdd)
            }
        }
    }

    fun notifyOddsDiscountChanged(discount: Float) {
        mDataList.forEach{
            if(it is GameEntity) {
                it.vpTableAdapter?.notifyOddsDiscountChanged(discount)
            }
        }
    }

    fun notifyOddsTypeChanged(oddsType: OddsType) {
        this.oddsType = oddsType
        mDataList.forEach{
            if(it is GameEntity) {
                it.vpTableAdapter?.notifyOddsTypeChanged(oddsType)
            }
        }
    }

    fun notifyMatchStatusChanged(matchStatusCO: MatchStatusCO, statusValue: String?) {
        mDataList.forEach {
            if(it is GameEntity) {
                it.vpTableAdapter?.notifyMatchStatusChanged(matchStatusCO, statusValue)
            }
        }
    }

    fun notifyTimeChanged(diff: Int) {
        mDataList.forEach{
            if(it is GameEntity) {
                it.vpTableAdapter?.notifyTimeChanged(diff)
            }
        }
    }

    fun notifyUpdateTime(matchClockCO: MatchClockCO?) {
        matchClockCO?.let { matchClock ->
            mDataList.forEach{
                if(it is GameEntity) {
                    it.vpTableAdapter?.notifyUpdateTime(matchClock)
                }
            }
        }
    }

    fun notifySelectedOddsChanged(selectedOdds: MutableList<String>) {
        mDataList.forEach {
            if(it is GameEntity) {
                it.vpTableAdapter?.notifySelectedOddsChanged(selectedOdds)
            }
        }
    }
    // endregion

    // region Recommend 指定刷新內部 ViewPager 的 subItem
    //指定刷新內部 ViewPager 的 subItem
    fun notifyRecommendSubItemChanged(index: Int, indexVpAdapter: Int) {
        if (index >= 0 && indexVpAdapter >= 0) {
            val data = mDataList[index]
            if (data is RecommendGameEntity) {
                data.vpRecommendAdapter?.notifyItemChanged(indexVpAdapter)
            }
        }
    }

    fun notifyRecommendSelectedOddsChanged(selectedOdds: MutableList<String>) {
        mDataList.forEach {
            if (it is RecommendGameEntity) {
                it.vpRecommendAdapter?.notifySelectedOddsChanged(selectedOdds)
            }
        }
    }
    // endregion

    // region TODO HighLight 指定刷新內部 ViewPager 的 subItem
    fun notifyHighLightTimeChanged(diff: Int) {
        var isUpdate = false
        val list = getMatchOdd()
        list.forEach { odd ->
            odd.matchInfo?.let {
                it.isAtStart = TimeUtil.isTimeAtStart(it.startTime)
                if (it.isAtStart == true) {
                    it.remainTime = it.startTime?.minus(System.currentTimeMillis())
                    it.remainTime?.let { remainTime ->
                        val newTimeDisplay = TimeUtil.longToMinute(remainTime * 1000)
                        if (it.timeDisplay != newTimeDisplay) {
                            it.timeDisplay = newTimeDisplay
                            isUpdate = true
                        }
                    }
                }
            }
        }
        if (isUpdate) {
            removeDatas(getMatchOdd().firstOrNull())
            list.forEach { addDataWithSort(it) }
//            Handler(Looper.getMainLooper()).post {
//                notifyDataSetChanged()
//            }
        }

    }

    fun notifyHighLightOddsDiscountChanged(discount: Float) {
        val list = getMatchOdd()
        list.forEach { matchOdd ->
            matchOdd.oddsMap.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.updateDiscount(this.discount, discount)
                }
            }
        }
//        Handler(Looper.getMainLooper()).post {
//            notifyDataSetChanged()
//        }
        removeDatas(getMatchOdd().firstOrNull())
        list.forEach { addDataWithSort(it) }
        this.discount = discount
    }

    fun notifyHighLightOddsTypeChanged(oddsType: OddsType) {
        this.highLightOddsType = oddsType
//        Handler(Looper.getMainLooper()).post {
//            notifyDataSetChanged()
//        }
    }

    fun notifyHighLightSelectedOddsChanged(selectedOdds: MutableList<String>) {
        val list = getMatchOdd()
        list.forEach { matchOdd ->
            matchOdd.oddsMap.forEach { (key, value) ->
                value?.forEach { odd ->
                    odd?.id?.let {
                        odd?.isSelected = selectedOdds.contains(it)
                    }
                }
            }
        }
        removeDatas(getMatchOdd().firstOrNull())
        list.forEach { addDataWithSort(it) }
    }
    // endregion

    // region private functions
    // 依照傳入參數刪除同一個類別的資料
    private fun removeDatas(src: Any?) {
        src?.let {
            val iterator = mDataList.iterator()
            while (iterator.hasNext()) {
                if (iterator.next()::class.isInstance(src))
                    iterator.remove()
            }
        }
    }
    // 依照SORTMAP的順序插入資料
    private fun addDataWithSort(src: Any) {
        Log.d("Hewie", "更新：${src::class.java.simpleName}")
        // 如果列表裡面沒東西，直接插
        if(mDataList.isEmpty()) {
            mDataList.add(src)
            notifyItemChanged(0)
            return
        }
        mDataList.forEachIndexed { index, target ->
            if(isPrev(src, target)) {
                mDataList.add(index, src)
                notifyItemChanged(index)
                return
            }
            if(index == mDataList.size) return
        }
        mDataList.add(src)
        notifyItemChanged(mDataList.size - 1)
        //print("${src}")
    }

    private fun isPrev(src: Any, target: Any): Boolean {
        if(getSortPoint(src) < getSortPoint(target)) return true
        return false
    }

    private fun getSortPoint(item: Any): Int = SORTMAP[item::class] ?: 0

//    private fun refresh(src: Any?) {
//        src?.let {
//            var start = 0
//            var last = 0
//            mDataList.forEachIndexed { index, any ->
//                if (any::class.isInstance(src)) {
//                    if (start == 0) start = index
//                    last = index
//                }
//            }
//            notifyItemRangeChanged(start, (last - start))
//        }
//    }

    private inline fun <reified T> getFilterData(): List<T> {
        val result = mutableListOf<T>()
        mDataList.filter { it is T }.forEach { result.add(it as T) }
        return result
    }
    // endregion

    // region Debug functions
    fun print(title: String) {
        println("SortData($title) ------------------------------------")
        mDataList.forEach {
            println("SortData => ${it}")
        }
    }
    // endregion
}