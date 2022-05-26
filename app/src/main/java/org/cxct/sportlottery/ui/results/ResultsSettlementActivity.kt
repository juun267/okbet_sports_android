package org.cxct.sportlottery.ui.results

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_results_settlement.*
import kotlinx.android.synthetic.main.activity_results_settlement.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_settlement_game_type.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_settlement_league_type.*
import kotlinx.android.synthetic.main.item_listview_settlement_game_type.view.*
import kotlinx.android.synthetic.main.item_listview_settlement_league.view.*
import kotlinx.android.synthetic.main.item_listview_settlement_league_all.*
import kotlinx.android.synthetic.main.item_listview_settlement_league_all.view.*
import kotlinx.android.synthetic.main.view_status_spinner.*
import kotlinx.android.synthetic.main.view_status_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.login.afterTextChanged
import java.text.SimpleDateFormat
import java.util.*

/**
 * @app_destination 賽果結算
 */
class ResultsSettlementActivity :
    BaseSocketActivity<SettlementViewModel>(SettlementViewModel::class) {
    lateinit var settlementLeagueBottomSheet: BottomSheetDialog
    private lateinit var settlementLeagueAdapter: SettlementLeagueAdapter
    private var bottomSheetLeagueItemDataList = mutableListOf<LeagueItemData>()

    private val settlementDateRvAdapter by lazy {
        SettlementDateRvAdapter()
    }

    //refactor
    private val matchResultDiffAdapter by lazy {
        MatchResultDiffAdapter(MatchItemClickListener(
            titleClick = {
                viewModel.clickResultItem(expandPosition = it)
            }, matchClick = {
                viewModel.clickResultItem(gameType, it)
            })
        )
    }
    private val outrightResultDiffAdapter by lazy {
        OutrightResultDiffAdapter(OutrightItemClickListener {
            viewModel.clickOutrightItem(it)
        })
    }

    private var gameType = ""

    private var timeRangeParams = setupTimeApiFormat(0) //預設為當日
    private var leagueSelectedSet: MutableSet<String> = mutableSetOf()

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
//        initServiceButton()
    }

    private fun setupToolbar() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }
    }

    private fun setupAdapter() {
        rv_date.adapter = settlementDateRvAdapter
        refactor_rv.adapter = matchResultDiffAdapter
    }

    private fun initEvent() {
        viewModel.requestListener = object : RequestListener {
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


        et_key_word.afterTextChanged {
            viewModel.setKeyWordFilter(it)
        }

        btn_refresh.setOnClickListener {
            when (settleType) {
                SettleType.OUTRIGHT -> viewModel.getOutrightResultList(gameType)
                SettleType.MATCH -> viewModel.getMatchResultList(gameType, null, timeRangeParams)
            }
        }
    }

    private fun setupSpinnerGameType() {
        initSettleGameTypeBottomSheet()
        setupSettleGameTypeBottomSheet()
    }

    private fun observeData() {
        viewModel.apply {
            //過濾後賽果資料
            showMatchResultData.observe(this@ResultsSettlementActivity, Observer {
                matchResultDiffAdapter.gameType = gameType
                if (it.isEmpty()) {
                    matchResultDiffAdapter.submitList(listOf(MatchResultData(ListType.NO_DATA)))
                } else {
                    matchResultDiffAdapter.submitList(it)
                }
            })

            //更新聯賽列表
            leagueFilterList.observe(this@ResultsSettlementActivity, Observer {
                leagueSelectedSet.clear()
                bottomSheetLeagueItemDataList = it
                setupLeagueList(it)
                it.forEach { data ->
                    leagueSelectedSet.add(data.name)
                }
                setLeagueFilter(leagueSelectedSet)
            })

            //過濾後冠軍資料
            showOutrightData.observe(this@ResultsSettlementActivity, Observer {
                if (it.isEmpty()) {
                    outrightResultDiffAdapter.submitList(listOf(OutrightResultData(OutrightType.NO_DATA)))
                } else {
                    outrightResultDiffAdapter.submitList(it)
                }
            })

            matchResultPlayListResult.observe(this@ResultsSettlementActivity) {
                showErrorPromptDialog(message = it.msg) {}
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
                        refactor_rv.adapter = outrightResultDiffAdapter
                        refactor_rv.scrollToPosition(0)
                        settleType = SettleType.OUTRIGHT
                        viewModel.getOutrightResultList(gameType)
                    }
                    else -> {
                        refactor_rv.adapter = matchResultDiffAdapter
                        refactor_rv.scrollToPosition(0)
                        settleType = SettleType.MATCH
                        timeRangeParams = setupTimeApiFormat(date)
                        viewModel.getMatchResultList(gameType, null, timeRangeParams)
                    }
                }
                dateSelected(date)
            }
        }
    }

    /*private fun initServiceButton() {
        btn_floating_service.setView(this)
    }*/

    private fun dateSelected(datePosition: Int) {
        (rv_date.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
            datePosition, rv_date.width / 2
        )
    }

    private fun initSettleGameTypeBottomSheet() {
        status_game_type.tv_name.text = getString(GameType.values()[0].string)
        gameType = GameType.values()[0].key
        viewModel.getMatchResultList(gameType, null, timeRangeParams)
    }
    private fun setupSettleGameTypeBottomSheet() {

        val gameTypeItem = mutableListOf<StatusSheetData>()
        GameType.values().forEach { gameType ->
            if (gameType != GameType.OTHER) gameTypeItem.add(StatusSheetData(gameType.key, getString(gameType.string)))
        }
        status_game_type.setItemData(gameTypeItem)
        status_game_type.setOnItemSelectedListener {
            gameType = it.code!!
            this@ResultsSettlementActivity.tv_name.text = it.showName
            when (settleType) {
                SettleType.MATCH -> {
                    viewModel.getMatchResultList(gameType, null, timeRangeParams)
                }
                SettleType.OUTRIGHT -> {
                    viewModel.getOutrightResultList(gameType)
                }
            }
        }
    }

    private fun settleLeagueBottomSheet() {
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
            bottomSheetLeagueItemDataList.forEachIndexed { index, it ->
                it.isSelected = cbAll.isChecked

                if (it.isSelected) {
                    leagueSelectedSet.add(it.name)
                } else {
                    leagueSelectedSet.remove(it.name)
                }
            }
            viewModel.setLeagueFilter(leagueSelectedSet)
            settlementLeagueAdapter.notifyDataSetChanged()
        }

        //取消選擇
        val tvCancel = settlementLeagueBottomSheet.layout_all.tv_cancel_selections
        tvCancel.setOnClickListener {
            cbAll.isChecked = false

            bottomSheetLeagueItemDataList.forEach {
                it.isSelected = false
            }
            leagueSelectedSet.clear()
            viewModel.setLeagueFilter(leagueSelectedSet)
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
                        leagueSelectedSet.add(select.name)
                    } else {
                        leagueSelectedSet.remove(select.name)
                    }
                    viewModel.setLeagueFilter(leagueSelectedSet)
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
                1 to getString(R.string.sunday2_cn),
                2 to getString(R.string.monday2),
                3 to getString(R.string.tuesday2),
                4 to getString(R.string.wednesday2),
                5 to getString(R.string.thursday2),
                6 to getString(R.string.friday2),
                7 to getString(R.string.saturday2)
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
                ll_game_league_item.setBackgroundColor(ContextCompat.getColor(context, R.color.color_505050_E2E2E2))
            else
                ll_game_league_item.setBackgroundColor(ContextCompat.getColor(context, R.color.color_191919_FCFCFC))
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


