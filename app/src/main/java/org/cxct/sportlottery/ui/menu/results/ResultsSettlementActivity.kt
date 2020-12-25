package org.cxct.sportlottery.ui.menu.results

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_results_settlement.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityResultsSettlementBinding
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

//TODO Dean : 篩選邏輯完成後,進行重構整理
class ResultsSettlementActivity : BaseActivity<SettlementViewModel>(SettlementViewModel::class) {
    private lateinit var settlementBinding: ActivityResultsSettlementBinding
    private val settlementViewModel: SettlementViewModel by viewModel()
    private val settlementRvAdapter by lazy {
        SettlementRvAdapter()
    }
    private val settlementDateRvAdapter by lazy {
        SettlementDateRvAdapter()
    }
    private val pagingParams by lazy { PagingParams(null, null) }
    private var timeRangeParams: TimeRangeParams = TimeRangeParams()
    private var gameType = "FT"

    interface RequestListener {
        fun requestIng(loading: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settlementBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_results_settlement)
        settlementBinding.apply {
            settlementViewModel = this@ResultsSettlementActivity.settlementViewModel
            lifecycleOwner = this@ResultsSettlementActivity
            rv_results.adapter = settlementRvAdapter
            rv_date.adapter = settlementDateRvAdapter
        }

        initView()
        initEvent()

        setupSpinnerGameType() //設置體育種類列表

        settlementViewModel.matchResultListResult.observe(this) {
            settlementRvAdapter.mDataList = it?.rows ?: listOf()
            settlementRvAdapter.gameType = gameType
            settlementRvAdapter.mSettlementRvListener = object :
                SettlementRvAdapter.SettlementRvListener {
                override fun getGameResultDetail(
                    settleRvPosition: Int,
                    gameResultRvPosition: Int,
                    matchId: String
                ) {
                    settlementViewModel.getSettlementDetailData(
                        settleRvPosition = settleRvPosition,
                        gameResultRvPosition = gameResultRvPosition,
                        matchId = matchId
                    )
                }

            }
            val spinnerGameZoneItem = it?.rows?.map { rows ->
                rows.league.name
            }?.toMutableList()
            setupSpinnerGameZone(spinnerGameZoneItem ?: mutableListOf())
        }

        settlementViewModel.setGameTypeFilter(spinner_game_type.selectedItemPosition, null, null)
        //TODO Dean : 問題第一次進來的時候不會觸發loadingView
        /*settlementViewModel.getSettlementData(
            gameType = gameType,
            pagingParams = pagingParams,
            timeRangeParams = setupTimeApiFormat(0)
        )*/

        settlementViewModel.gameResultDetailResult.observe(this) {
            settlementRvAdapter.mGameDetail = it //set Game Detail Data
        }

        //日期選擇初始化
        settlementDateRvAdapter.mDateList = setupWeekList(System.currentTimeMillis())
        timeRangeParams = setupTimeApiFormat(0)
        settlementDateRvAdapter.refreshDateListener = object :
            SettlementDateRvAdapter.RefreshDateListener {
            override fun refreshDate(date: Int) {
                //0:今日, 1:明天, 2:後天 ... 7:冠軍
                when (date) {
                    7 -> {
                        timeRangeParams = TimeRangeParams()
                        settlementViewModel.getSettlementData(gameType, pagingParams, timeRangeParams)
                    }
                    else -> {
                        timeRangeParams = setupTimeApiFormat(date)
                        settlementViewModel.getSettlementData(gameType, pagingParams, timeRangeParams)
                    }
                }
            }

        }

        settlementViewModel.apply {
            settlementFilter.observe(this@ResultsSettlementActivity) {
                gameType = it.gameType
                getSettlementData(gameType = it.gameType, pagingParams = pagingParams, timeRangeParams = timeRangeParams)
            }
        }
    }

    private fun initView() {
        spinner_game_type.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("${getString(R.string.football)}")
        )
        spinner_game_zone.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("${getString(R.string.league)}")
        )
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

        //日期選擇
        spinner_game_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                settlementViewModel.setGameTypeFilter(position)
            }
        }
    }

    private fun setupSpinnerGameType() { //TODO Dean : review, 加入聯賽篩選後需重新順一次流程
        spinner_game_type.let {
            val spinnerGameTypeItem = mutableListOf<String>()
            GameType.values().forEach { gameType -> spinnerGameTypeItem.add(getString(gameType.string)) }
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerGameTypeItem)

        }
    }

    //設置聯賽列表
    private fun setupSpinnerGameZone(spinnerLeagueItem: MutableList<String>) {
        spinner_game_zone.let {

            it.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerLeagueItem)
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //TODO Dean : review filter, 聯賽篩選UI更換
                    /*settlementViewModel.settlementFilter.value?.gameZone =
                        spinnerLeagueItem[position]
                    settlementViewModel.setGameTypeFilter(spinnerLeagueItem[position])*/
                }
            }
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
                1 to getString(R.string.sunday),
                2 to getString(R.string.monday),
                3 to getString(R.string.tuesday),
                4 to getString(R.string.wednesday),
                5 to getString(R.string.thursday),
                6 to getString(R.string.friday),
                7 to getString(R.string.saturday),
            ) //1:星期日, 2:星期一, ...
            weekList.add("${weekName[week]}\n$month${getString(R.string.month)}$day${getString(R.string.day)}") //格式：星期一\n12月21日
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
        return TimeRangeParams(
            startTime = startTimeStamp.toString(),
            endTime = endTimeStamp.toString()
        )
    }
}
