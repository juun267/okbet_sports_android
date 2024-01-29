package org.cxct.sportlottery.ui.sport.filter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_league_select.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.common.event.SelectMatchEvent
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityLeagueSelectBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.sport.common.SelectDate
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.IndexBar
import org.greenrobot.eventbus.EventBus
import java.util.*


class LeagueSelectActivity :
    BaseSocketActivity<LeagueSelectViewModel,ActivityLeagueSelectBinding>(LeagueSelectViewModel::class) {
    companion object {
        fun start(
            context: Context,
            gameType: String,
            matchType: MatchType,
            timeRangeParams: TimeRangeParams?=null,
            categoryCodeList: List<String>?=null,
        ) {
            var intent = Intent(context, LeagueSelectActivity::class.java)
            intent.putExtra("gameType", gameType)
            intent.putExtra("matchType", matchType)
            intent.putExtra("startTime", timeRangeParams?.startTime?:"")
            intent.putExtra("endTime", timeRangeParams?.endTime?:"")
            categoryCodeList?.let { intent.putStringArrayListExtra("categoryCodeList",ArrayList(it)) }
            context.startActivity(intent)
        }
    }


    private val gameType by lazy { intent?.getStringExtra("gameType") ?: GameType.FT.key }
    private val matchType by lazy { (intent?.getSerializableExtra("matchType") as MatchType?) ?: MatchType.IN_PLAY }
    private val categoryCodeList by lazy { intent?.getStringArrayListExtra("categoryCodeList") }

    private var selectStartTime:String = ""
    private var selectEndTime:String = ""
    private lateinit var selectDateAdapter: SelectDateAdapter

    private lateinit var leagueSelectAdapter: LeagueSelectAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var outRightLeagueAdapter: OutrightLeagueSelectAdapter

    private val loading by lazy { Gloading.wrapView(rv_league) }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        bindFinish(btnCancel)
        loading.showLoading()
        setDateListView()
        bindSportMaintenance()

        if (MatchType.OUTRIGHT == matchType) {
            initOutrightLeagues()
            initOutrightObserver()
            viewModel.getOutRightLeagueList(gameType)
        } else {
            initLeagues()
            initObserve()
            loadOddsList(gameType, matchType.postValue)
        }
    }

    private fun loadOddsList(gameType: String,
                             matchType: String,
                             startTime: String? = null,
                             endTime: String?=null,
                             isDateSelected: Boolean = false) {
        viewModel.getOddsList(gameType, matchType, startTime, endTime, isDateSelected,categoryCodeList)
    }

    private fun initLeagues() {
        leagueSelectAdapter = LeagueSelectAdapter(::setSelectSum)
        btnAllSelect.setOnClickListener { setSelectSum(leagueSelectAdapter.selectAll()) }
        btnReverseSelect.setOnClickListener { setSelectSum(leagueSelectAdapter.reverseSelect()) }
        btnConfirm.setOnClickListener {
            EventBus.getDefault().post(SelectMatchEvent(leagueSelectAdapter.getSelectedLeagueIds(),leagueSelectAdapter.getSelectedMatchIds()))
            onBackPressed()
        }
        setupMatchListView(leagueSelectAdapter) { position->
            var leagueSection = leagueSelectAdapter.getItem(position)
            leagueSection.let {
                var cap = when (it) {
                    is LeagueOdd -> it.league!!.firstCap
                    is MatchOdd -> (it.parentNode as LeagueOdd).league.firstCap
                    else -> ""
                }
                indexBar.updateIndex(indexBar.getTextArray().indexOf(cap))
            }
        }
    }

    private fun initOutrightLeagues() {
        outRightLeagueAdapter = OutrightLeagueSelectAdapter(::setSelectSum)
        btnAllSelect.setOnClickListener { setSelectSum(outRightLeagueAdapter.selectAll()) }
        btnReverseSelect.setOnClickListener { setSelectSum(outRightLeagueAdapter.reverseSelect()) }
        btnConfirm.setOnClickListener {
            EventBus.getDefault().post(SelectMatchEvent(outRightLeagueAdapter.getSelectedLeagueIds(),outRightLeagueAdapter.getSelectedMatchIds()))
            onBackPressed()
        }
        setupMatchListView(outRightLeagueAdapter) { position->
            var leagueSection = outRightLeagueAdapter.getItem(position)
            leagueSection.let {
                var cap = when (it) {
                    is org.cxct.sportlottery.network.outright.odds.LeagueOdd -> it.league!!.firstCap
                    is org.cxct.sportlottery.network.outright.odds.MatchOdd -> it.parentNode.league!!.firstCap
                    else -> ""
                }
                indexBar.updateIndex(indexBar.getTextArray().indexOf(cap))
            }
        }
    }

    private fun setIndexbar(indexArr: Array<CharSequence>, indexOfChar: (CharSequence) -> Int) {
        //自定义索引数组，默认是26个大写字母
        indexBar.textArray = indexArr
        //添加相关监听
        indexBar.setOnIndexLetterChangedListener(object : IndexBar.OnIndexLetterChangedListener {
            override fun onTouched(touched: Boolean) {
                //TODO 手指按下和抬起会回调这里
                iv_union.isVisible = touched
            }

            override fun onLetterChanged(indexChar: CharSequence, index: Int, y: Float) {
                //TODO 索引字母改变时会回调这里
                val pos = indexOfChar(indexChar)
                if (pos > 0) {
                    linearLayoutManager.scrollToPositionWithOffset(pos, 0)
                }
                iv_union.y = y + cv_index.top
                iv_union.text = indexChar
            }
        })
    }

    private fun setupMatchListView(adapter: BaseNodeAdapter, updateIndexBar: (Int) -> Unit) {
        linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_league.layoutManager = linearLayoutManager
        rv_league.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.bg_decoration_filter)))
        rv_league.adapter = adapter
        rv_league.setOnScrollChangeListener { _, _, _, _, _ ->
            val firstCompletelyVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstCompletelyVisibleItemPosition >= 0) {
                updateIndexBar(firstCompletelyVisibleItemPosition)
            }
        }
    }

    private fun setDateListView(){
        when (matchType) {
            MatchType.EARLY, MatchType.CS -> {
                linDate.isVisible = true
                rvDate.layoutManager = GridLayoutManager(this,3)
                if (rvDate.itemDecorationCount==0)
                rvDate.addItemDecoration(GridItemDecoration(8.dp,12.dp,Color.TRANSPARENT,false))
                val names = mutableListOf<String>(
                    getString(R.string.label_all),
                    getString(R.string.home_tab_today),
                    getString(R.string.N930),
                    getString(R.string.N931),
                    getString(R.string.N932)
                )

                val itemData = mutableListOf<SelectDate>().apply {
                    val calendar = Calendar.getInstance()
                    repeat(names.size) {
                        if (it==0){
                            add(SelectDate(calendar.time,names[it],getString(R.string.date_row_all),"",""))
                        }else{
                            if (it == 1) calendar.add(Calendar.DATE, 0) else calendar.add(Calendar.DATE, 1)
                            val date = calendar.time
                            val label=TimeUtil.dateToFormat(date,TimeUtil.SELECT_MATCH_FORMAT)
                            val timeRangeParams=TimeUtil.getDayDateTimeRangeParams(TimeUtil.dateToFormat(date,TimeUtil.YMD_FORMAT))
                            val startTime = timeRangeParams.startTime?:""
                            val endTime = if (it==names.size-1) "" else timeRangeParams.endTime?:""
                            add(SelectDate(date,names[it],label,startTime,endTime))
                        }
                    }
                }

                selectDateAdapter= SelectDateAdapter(itemData){
                    selectStartTime = it.startTime
                    selectEndTime = it.endTime
                    loading.showLoading()
                    loadOddsList(gameType,
                        matchType.postValue,
                        selectStartTime,
                        selectEndTime,
                        true
                    )
                }
                rvDate.adapter = selectDateAdapter
            }
            else -> {
                linDate.isVisible = false
            }
        }
    }

    private fun initObserve() {
        viewModel.leagueList.observe(this) {

            var map: Map<String, List<LeagueOdd>> = it.groupBy {
                it.league.firstCap?:""
            }.toSortedMap { o1: String, o2: String ->
                o1.compareTo(o2)
            }

            val itemData = mutableListOf<LeagueOdd>()
            map.keys.forEach { name ->
                itemData.addAll(map[name] ?: mutableListOf())
            }
            leagueSelectAdapter.setNewDataList(itemData)
            setIndexbar(map.keys.toTypedArray()) { indexChar->
                return@setIndexbar leagueSelectAdapter.data.indexOfFirst {
                    (it is LeagueOdd) && it.league.firstCap == indexChar
                }
            }
            setSelectSum(leagueSelectAdapter.getSelectSum())
            onLoadEnd(leagueSelectAdapter.itemCount)
        }
    }

    private fun initOutrightObserver() {
        viewModel.outrightLeagues.observe(this) {
            val map = outRightLeagueAdapter.setNewDataList(it)
            setIndexbar(map.keys.toTypedArray()) { indexChar->
                return@setIndexbar outRightLeagueAdapter.data.indexOfFirst {
                    (it is org.cxct.sportlottery.network.outright.odds.LeagueOdd) && it.league!!.firstCap == indexChar
                }
            }
            setSelectSum(outRightLeagueAdapter.getSelectSum())
            onLoadEnd(outRightLeagueAdapter.itemCount)
        }
    }

    private fun onLoadEnd(dataSize: Int) {
        if (dataSize == 0) {
            loading.showEmpty()
        } else {
            loading.showLoadSuccess()
        }
    }

    private fun setSelectSum(num: Int) {
        btnConfirm.isEnabled = num > 0
    }


}