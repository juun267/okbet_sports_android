package org.cxct.sportlottery.ui.results

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_results_settlement.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_settlement_game_type.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_settlement_league_type.*
import kotlinx.android.synthetic.main.item_listview_settlement_game_type.view.*
import kotlinx.android.synthetic.main.item_listview_settlement_league.view.*
import kotlinx.android.synthetic.main.item_listview_settlement_league_all.*
import kotlinx.android.synthetic.main.item_listview_settlement_league_all.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.matchresult.list.MatchResultList
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class ResultsSettlementActivity :
    BaseNoticeActivity<SettlementViewModel>(SettlementViewModel::class) {
    lateinit var settlementLeagueBottomSheet: BottomSheetDialog
    private lateinit var settlementLeagueAdapter: SettlementLeagueAdapter
    lateinit var settlementGameTypeBottomSheet: BottomSheetDialog
    private lateinit var settlementGameTypeAdapter: SettlementGameTypeAdapter
    private var bottomSheetLeagueItemDataList = mutableListOf<LeagueItemData>()

    private val settlementViewModel: SettlementViewModel by viewModel()
    private val settlementRvAdapter by lazy {
        SettlementRvAdapter()
    }
    private val settlementDateRvAdapter by lazy {
        SettlementDateRvAdapter()
    }

    //refactor
    private val matchResultDiffAdapter by lazy {
        MatchResultDiffAdapter(MatchItemClickListener(
            titleClick = {
                Log.e("Dean", "ItemClickListener")
                viewModel.clickResultItem(expandPosition = it)
            }, matchClick = {
                viewModel.clickResultItem(gameType, it)
            })
        )
    }

    private var gameType = ""
    private val selectNameList = mutableListOf<String>()
    private var timeRangeParams = setupTimeApiFormat(0) //預設為當日
    private var leagueSelectedSet: MutableSet<Int> = mutableSetOf()

    private var settleType = SettleType.MATCH

    interface RequestListener {
        fun requestIng(loading: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_results_settlement)

        setupToolbar()
        setupAdapter()
        initEvent()
        setupSpinnerGameType() //設置體育種類列表
        observeData()
        initTimeSelector()
    }

    private fun setupToolbar() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun setupAdapter() {
        rv_results.adapter = settlementRvAdapter
        rv_date.adapter = settlementDateRvAdapter

        //refactor
        refactor_rv.adapter = matchResultDiffAdapter
    }

    private fun initEvent() {
        settlementViewModel.requestListener = object : RequestListener {
            override fun requestIng(loading: Boolean) {
                if (loading)
                    loading()
                else
                    hideLoading()
            }
        }

        ll_game_league.setOnClickListener {
            settlementLeagueBottomSheet.show()
        }

        ll_game_type.setOnClickListener {
            settlementGameTypeBottomSheet.show()
        }

        et_key_word.afterTextChanged {
            settlementViewModel.setKeyWordFilter(it)
        }

        btn_refresh.setOnClickListener {
            //TODO Dean : 重構 review 後刪除
//            settlementViewModel.getSettlementData(gameType, null, timeRangeParams)
            settlementViewModel.getMatchResultList(gameType, null, timeRangeParams)
        }
    }

    private fun setupSpinnerGameType() {
        initSettleGameTypeBottomSheet()
        setupSettleGameTypeBottomSheet()
    }

    private fun observeData() {
        settlementViewModel.apply {
            //獲取賽果資料,更新聯賽列表
            matchResultListResult.observe(this@ResultsSettlementActivity) {
                setSettleRvData(it.matchResultList)

                bottomSheetLeagueItemDataList = it.matchResultList?.map { rows ->
                    LeagueItemData(null, rows.league.name, true)
                }?.toMutableList<LeagueItemData>() ?: mutableListOf()

                setupLeagueList(bottomSheetLeagueItemDataList)
            }

            //比賽詳情
            gameResultDetailResult.observe(this@ResultsSettlementActivity) {
                settlementRvAdapter.mGameDetail = it //set Game Detail Data
            }

            //獲取冠軍資料,更新聯賽列表
            outRightListResult.observe(this@ResultsSettlementActivity) {
                bottomSheetLeagueItemDataList = it.rows?.map { rows ->
                    LeagueItemData(null, rows.season.name, true)
                }?.toMutableList<LeagueItemData>() ?: mutableListOf()

                setupLeagueList(bottomSheetLeagueItemDataList)
            }

            /*//過濾後賽果資料
            matchResultList.observe(this@ResultsSettlementActivity) {
                setSettleRvData(it)
            }*/
            //過濾後賽果資料
            showMatchResultData.observe(this@ResultsSettlementActivity, Observer {
                Log.e("Dean", "showMatchResultData observe gameType = $gameType")
                Log.e("Dean", "showMatchResultData observe List = $it")
                matchResultDiffAdapter.gameType = gameType
                matchResultDiffAdapter.submitList(it)
            })

            //過濾後冠軍資料
            outRightList.observe(this@ResultsSettlementActivity) {
                setSettleRvOutRightData(it)
            }
        }
    }

    private fun initTimeSelector() {
        //日期選擇初始化
        settlementDateRvAdapter.mDateList = setupWeekList(System.currentTimeMillis())
        settlementDateRvAdapter.refreshDateListener = object :
            SettlementDateRvAdapter.RefreshDateListener {
            override fun refreshDate(date: Int) {
                //0:今日, 1:明天, 2:後天 ... 7:冠軍
                when (date) {
                    7 -> {
                        settleType = SettleType.OUTRIGHT
                        settlementViewModel.getOutrightResultList(gameType)
                    }
                    else -> {
                        settleType = SettleType.MATCH
                        timeRangeParams = setupTimeApiFormat(date)
                        //TODO Dean : 重構 review 後刪除
//                        settlementViewModel.getSettlementData(gameType, null, timeRangeParams)
                        settlementViewModel.getMatchResultList(gameType, null, timeRangeParams)
                    }
                }
            }
        }
    }

    private fun initSettleGameTypeBottomSheet() {
        tv_game_type.text = getString(GameType.values()[0].string)
        gameType = GameType.values()[0].key
        //TODO Dean : 重構 review 後刪除
//        settlementViewModel.getSettlementData(gameType, null, timeRangeParams)
        settlementViewModel.getMatchResultList(gameType, null, timeRangeParams)
    }

    private fun setupSettleGameTypeBottomSheet() {
        val gameTypeBottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_settlement_game_type, null)
        settlementGameTypeBottomSheet = BottomSheetDialog(this@ResultsSettlementActivity)
        settlementGameTypeBottomSheet.apply {
            setContentView(gameTypeBottomSheetView)
            val gameTypeItem = mutableListOf<GameTypeItemData>()
            GameType.values().forEach { gameType -> gameTypeItem.add(GameTypeItemData(null, getString(gameType.string))) }
            settlementGameTypeAdapter = SettlementGameTypeAdapter(lv_game_type.context, gameTypeItem)
            lv_game_type.adapter = settlementGameTypeAdapter
            settlementGameTypeAdapter.setOnItemCheckedListener(object :
                OnSelectItemWithPositionListener<GameTypeItemData> {
                override fun onClick(select: GameTypeItemData, position: Int) {
                    gameType = GameType.values()[position].key
                    this@ResultsSettlementActivity.tv_game_type.text = select.name
                    when (settleType) {
                        SettleType.MATCH -> {
                            //TODO Dean : 重構 review 後刪除
//                            settlementViewModel.getSettlementData(gameType, null, timeRangeParams)
                            settlementViewModel.getMatchResultList(gameType, null, timeRangeParams)
                        }
                        SettleType.OUTRIGHT -> {
                            settlementViewModel.getOutrightResultList(gameType)
                        }
                    }
                    settlementGameTypeBottomSheet.dismiss()
                }

            })
        }
    }

    private fun settleLeagueBottomSheet() {
        //TODO Dean : 看有沒有font-family可以做使用, 此處文字的style沒有與Zeplin相符
        tv_league.text = getString(R.string.league)
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_settlement_league_type, null)
        settlementLeagueBottomSheet = BottomSheetDialog(this@ResultsSettlementActivity)
        settlementLeagueBottomSheet.apply {
            setContentView(bottomSheetView)
            settlementLeagueAdapter = SettlementLeagueAdapter(lv_league.context, mutableListOf()) //先預設為空, 等待api獲取資料
            lv_league.adapter = settlementLeagueAdapter

        }

        //避免bottomSheet的滑動與listView發生衝突
        settlementLeagueBottomSheet.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    settlementLeagueBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun leagueSelectorEvent() {
        val cbAll = settlementLeagueBottomSheet.layout_all.checkbox_select_all

        //全選按鈕
        cbAll.setOnClickListener {
            selectNameList.clear()
            bottomSheetLeagueItemDataList.forEachIndexed { index, it ->
                it.isSelected = cbAll.isChecked

                if (it.isSelected) {
                    selectNameList.add(it.name)
                    leagueSelectedSet.add(index)
                } else {
                    selectNameList.remove(it.name)
                    leagueSelectedSet.remove(index)
                }
            }
            settlementViewModel.setLeagueFilter(leagueSelectedSet)
            settlementLeagueAdapter.notifyDataSetChanged()
        }

        //取消選擇
        val tvCancel = settlementLeagueBottomSheet.layout_all.tv_cancel_selections
        tvCancel.setOnClickListener {
            selectNameList.clear()
            cbAll.isChecked = false

            bottomSheetLeagueItemDataList.forEach {
                it.isSelected = false
            }
            leagueSelectedSet.clear()
            settlementViewModel.setLeagueFilter(leagueSelectedSet)
            settlementLeagueAdapter.notifyDataSetChanged()
        }
    }

    private fun setupLeagueList(leagueList: MutableList<LeagueItemData>) {
        settleLeagueBottomSheet()
        leagueSelectorEvent()
        settlementLeagueBottomSheet.apply {
            settlementLeagueAdapter = SettlementLeagueAdapter(lv_league.context, leagueList)
            lv_league.adapter = settlementLeagueAdapter

            val cbAll = settlementLeagueBottomSheet.layout_all.checkbox_select_all
            settlementLeagueAdapter.setOnItemCheckedListener(object :
                OnSelectItemWithPositionListener<LeagueItemData> {
                override fun onClick(select: LeagueItemData, position: Int) {
                    if (select.isSelected) {
                        selectNameList.add(select.name)
                        leagueSelectedSet.add(position)
                    } else {
                        selectNameList.remove(select.name)
                        leagueSelectedSet.remove(position)
                    }
                    settlementViewModel.setLeagueFilter(leagueSelectedSet)
                    //判斷全選按鈕是否需選取
                    var selectCount = 0
                    bottomSheetLeagueItemDataList.forEach {
                        if (it.isSelected) selectCount++
                    }
                    cbAll.isChecked = selectCount == bottomSheetLeagueItemDataList.size
                }
            })
            checkbox_select_all.performClick() //預設為聯盟全選
        }
    }

    /**
     * 設置賽果資料
     * result : MatchResultListResult.row: List<Row>
     */
    private fun setSettleRvData(result: List<MatchResultList>?) {
        settlementRvAdapter.gameType = gameType
        settlementRvAdapter.settleType = settleType
        settlementRvAdapter.mDataList = result ?: listOf()
        settlementRvAdapter.mSettlementRvListener = object :
            SettlementRvAdapter.SettlementRvListener {
            override fun getGameResultDetail(
                settleRvPosition: Int, gameResultRvPosition: Int, matchId: String
            ) {
                settlementViewModel.getSettlementDetailData(settleRvPosition = settleRvPosition, gameResultRvPosition = gameResultRvPosition, matchId = matchId)
            }

        }
    }

    /**
     * 設置賽果冠軍資料
     * result : OutRightListResult.row: List<Row>
     */
    private fun setSettleRvOutRightData(result: List<org.cxct.sportlottery.network.outright.Row>?) {
        settlementRvAdapter.settleType = settleType
        settlementRvAdapter.mOutRightDatList = result ?: listOf()
    }

    /**
     * return : List, 一週及冠軍的日期選項
     */
    private fun setupWeekList(todayMillis: Long): MutableList<String> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = todayMillis

        val weekList = mutableListOf<String>()
        weekList.add(getString(R.string.home_tab_today))
        for (day in 1..6) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val week = calendar.get(Calendar.DAY_OF_WEEK)
            val weekName = mapOf<Int, String>(
                1 to getString(R.string.sunday),
                2 to getString(R.string.monday),
                3 to getString(R.string.tuesday),
                4 to getString(R.string.wednesday),
                5 to getString(R.string.thursday),
                6 to getString(R.string.friday),
                7 to getString(R.string.saturday)
            ) //1:星期日, 2:星期一, ...
            weekList.add("${weekName[week]}\n${String.format("%02d", month)}-${String.format("%02d", day)}")
        }
        weekList.add(getString(R.string.champion))
        return weekList
    }

    /**
     * return : api所需時間格式
     * addDay: 0: //0:今日, 1:明天, 2:後天 ...
     * format : startTime:yyyy-MM-dd 00:00:00 endTime:yyyy-MM-dd 23:59:59
     */
    private fun setupTimeApiFormat(addDay: Int): TimeRangeParams {
        //startTime:yyyy-MM-dd 00:00:00 endTime:yyyy-MM-dd 23:59:59
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_MONTH, -addDay)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startTimeStamp = timeFormat.parse("$year-$month-$day 00:00:00").time
        val endTimeStamp = timeFormat.parse("$year-$month-$day 23:59:59").time
        return object : TimeRangeParams {
            override val startTime: String
                get() = startTimeStamp.toString()
            override val endTime: String
                get() = endTimeStamp.toString()

        }
    }
}

data class LeagueItemData(val code: Int? = null, val name: String = "", var isSelected: Boolean = true)
data class GameTypeItemData(val code: Int? = null, val name: String = "")

interface OnSelectItemWithPositionListener<LeagueItemData> {
    fun onClick(select: LeagueItemData, position: Int)
}

class SettlementLeagueAdapter(private val context: Context, private val dataList: MutableList<LeagueItemData>) : BaseAdapter() {

    private var mOnSelectItemListener: OnSelectItemWithPositionListener<LeagueItemData>? = null

    fun setOnItemCheckedListener(onSelectItemListener: OnSelectItemWithPositionListener<LeagueItemData>) {
        this.mOnSelectItemListener = onSelectItemListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_listview_settlement_league, parent, false)
        val data = dataList[position]

        view.apply {
            checkbox.text = data.name
            checkbox.isChecked = data.isSelected
            if (data.isSelected)
                ll_game_league_item.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                ll_game_league_item.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
                ll_game_league_item.isSelected = isChecked
                notifyDataSetChanged()
                mOnSelectItemListener?.onClick(data, position)
            }
        }

        return view
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}

class SettlementGameTypeAdapter(private val context: Context, private val dataList: MutableList<GameTypeItemData>) : BaseAdapter() {

    private var mOnSelectItemListener: OnSelectItemWithPositionListener<GameTypeItemData>? = null
    private var selectedPosition = 0

    fun setOnItemCheckedListener(onSelectItemListener: OnSelectItemWithPositionListener<GameTypeItemData>) {
        this.mOnSelectItemListener = onSelectItemListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_listview_settlement_game_type, parent, false)
        val data = dataList[position]

        view.apply {
            tv_game_type.text = data.name
            if (position == selectedPosition)
                ll_game_type_item.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else
                ll_game_type_item.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            ll_game_type_item.setOnClickListener {
                if (selectedPosition != position) {
                    //                data.isSelected = !data.isSelected
                    selectedPosition = position
                    notifyDataSetChanged()
                    mOnSelectItemListener?.onClick(data, position)
                }
            }
        }

        return view
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}
