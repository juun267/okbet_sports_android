package org.cxct.sportlottery.ui.sport.filter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.didichuxing.doraemonkit.util.GsonUtils
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_league_select.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.TimeRangeEvent
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
import kotlin.Comparator
import kotlin.collections.ArrayList


class LeagueSelectActivity :
    BaseSocketActivity<LeagueSelectViewModel>(LeagueSelectViewModel::class) {
    companion object {
        fun start(
            context: Context,
            gameType: String,
            matchType: MatchType,
            timeRangeParams: TimeRangeParams?,
            matchIdList: ArrayList<String>?,
        ) {
            var intent = Intent(context, LeagueSelectActivity::class.java)
            intent.putExtra("gameType", gameType)
            intent.putExtra("matchType", matchType)
            intent.putExtra("startTime", timeRangeParams?.startTime?:"")
            intent.putExtra("endTime", timeRangeParams?.endTime?:"")
            intent.putExtra("matchIdList", matchIdList)
            context.startActivity(intent)
        }
    }

    var itemData = mutableListOf<LeagueOdd>()

    private val gameType by lazy { intent?.getStringExtra("gameType") ?: GameType.FT.key }
    private val matchType by lazy { (intent?.getSerializableExtra("matchType") as MatchType?) ?: MatchType.IN_PLAY }

    private val matchIdList: ArrayList<String>? by lazy { intent?.getStringArrayListExtra("matchIdList") as ArrayList<String> }
    private val startTime: String? by lazy { intent?.getStringExtra("startTime") }
    private val endTime: String? by lazy { intent?.getStringExtra("endTime") }

    private var selectStartTime:String = ""
    private var selectEndTime:String = ""
    private lateinit var selectDateAdapter: SelectDateAdapter

    lateinit var leagueSelectAdapter: LeagueSelectAdapter
    lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_FFFFFF, true)
        setContentView(R.layout.activity_league_select)
        setupToolbar()
        setDateListView()
        setupMatchListView()
        initObserve()
        viewModel.getOddsList(gameType,
            matchType.postValue,
            startTime ?: "",
            endTime ?: "",
            matchIdList)
    }

    private fun setupToolbar() {
        btnCancel.setOnClickListener {
            finish()
        }
        btnAllSelect.setOnClickListener {
            itemData.forEach {
                it.league.isSelected = true
                it.matchOdds.forEach {
                    it.isSelected = true
                }
            }
            leagueSelectAdapter.notifyDataSetChanged()
            setSelectSum()
        }
        btnReverseSelect.setOnClickListener {
            itemData.forEach {
                it.matchOdds.forEach {
                    it.isSelected = !it.isSelected
                }
                it.league.isSelected = it.matchOdds.all { it.isSelected }
            }
            leagueSelectAdapter.notifyDataSetChanged()
            setSelectSum()
        }
        btnConfirm.setOnClickListener {
            var matchSelectList = arrayListOf<String>()
            val countLeague = itemData.count { it.league.isSelected }
            if (countLeague!=itemData.size){
                itemData.forEach {
                    if (it.league.isSelected) {
                        matchSelectList.addAll(it.matchOdds.map { it.matchInfo?.id?:"" })
                    }
                }
            }
            EventBus.getDefault().post(matchSelectList)
            if (selectStartTime!=startTime||selectEndTime!=endTime){
                EventBus.getDefault().post(TimeRangeEvent(selectStartTime,selectEndTime))
            }
            onBackPressed()
        }
    }

    private fun setIndexbar(indexArr: Array<CharSequence>) {
        //自定义索引数组，默认是26个大写字母
        indexBar.setTextArray(indexArr);
        //添加相关监听
        indexBar.setOnIndexLetterChangedListener(object : IndexBar.OnIndexLetterChangedListener {
            override fun onTouched(touched: Boolean) {
                //TODO 手指按下和抬起会回调这里
                iv_union.isVisible = touched
            }

            override fun onLetterChanged(indexChar: CharSequence?, index: Int, y: Float) {
                //TODO 索引字母改变时会回调这里
                val pos = leagueSelectAdapter.data.indexOfFirst {
                    (it is LeagueOdd) && it.league.firstCap == indexChar
                }
                if (pos > 0) {
                    linearLayoutManager.scrollToPositionWithOffset(pos, 0)
                }
                iv_union.y = y + cv_index.top
                iv_union.text = indexChar
            }
        })
    }

    private fun setupMatchListView() {
        linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_league.layoutManager = linearLayoutManager
        rv_league.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.recycleview_decoration)))
        leagueSelectAdapter = LeagueSelectAdapter() { position, view, item ->
             if (item is LeagueOdd){
                 //联赛选择，更新关联赛事的选中状态
                 item.matchOdds.forEach {
                     it.isSelected = item.league.isSelected
                 }
             }else if(item is MatchOdd){
                 (item.parentNode as LeagueOdd).apply {
                     league.isSelected = matchOdds.all { it.isSelected }
                 }
             }
            leagueSelectAdapter.notifyDataSetChanged()
            setSelectSum()
        }
        rv_league.adapter = leagueSelectAdapter
        rv_league.setOnScrollChangeListener(object : View.OnScrollChangeListener {
            override fun onScrollChange(
                v: View?,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int,
            ) {
                val firstCompletelyVisibleItemPosition =
                    linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                var leagueSection = leagueSelectAdapter.getItem(firstCompletelyVisibleItemPosition)
                leagueSection.let {
                    var cap = when (it){
                        is LeagueOdd-> it.league.firstCap
                        is MatchOdd-> (it.parentNode as LeagueOdd).league.firstCap
                        else->""
                    }
                    indexBar.updateIndex(indexBar.getTextArray().indexOf(cap))
                }
            }
        })
    }
    private fun setDateListView(){
        when (matchType) {
            MatchType.EARLY, MatchType.CS -> {
                linDate.isVisible = true
                rvDate.layoutManager = GridLayoutManager(this,3)
                rvDate.addItemDecoration(GridItemDecoration(8.dp,12.dp,Color.TRANSPARENT,false))
                val names = mutableListOf<String>(
                    getString(R.string.label_all),
                    getString(R.string.home_tab_today),
                    getString(R.string.N930),
                    getString(R.string.N931),
                    getString(R.string.N931)
                )

                val itemData = mutableListOf<SelectDate>().apply {
                    val calendar = Calendar.getInstance()
                    repeat(names.size) {
                        if (it == 0) calendar.add(Calendar.DATE, 0) else calendar.add(Calendar.DATE, 1)
                        val date = calendar.time
                        val label=if (it==0) getString(R.string.date_row_all) else TimeUtil.dateToFormat(date,TimeUtil.SELECT_MATCH_FORMAT)
                        val timeRangeParams=TimeUtil.getDayDateTimeRangeParams(TimeUtil.dateToFormat(date,TimeUtil.YMD_FORMAT))
                        val startTime = if(it==0) "" else timeRangeParams.startTime?:""
                        val endTime = if(it==0) "" else timeRangeParams.endTime?:""
                        add(SelectDate(date,names[it],label,startTime,endTime))
                    }
                }

                selectDateAdapter= SelectDateAdapter(itemData){
                    selectStartTime = it.startTime
                    selectEndTime = it.endTime
                }.apply {
                    itemData.indexOfFirst { it.startTime==startTime&&it.endTime==endTime}.also {
                       it>0
                   }.let {
                       selectPos = it
                   }
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
            it.let {
                var map: Map<String, List<LeagueOdd>> = it.groupBy {
                    it.league.firstCap?:""
                }.toSortedMap(Comparator<String> { o1: String, o2: String ->
                    o1.compareTo(o2)
                })
                map.keys.forEach { name ->
                    itemData.addAll(map[name] ?: mutableListOf())
                }
                leagueSelectAdapter.setNewInstance(itemData as MutableList<BaseNode>)
                setIndexbar(map.keys.toTypedArray())
                setSelectSum()
            }
        }
    }

    private fun setSelectSum() {
        var sum = itemData.sumOf {
            it.matchOdds?.count{ it.isSelected }
        }
        btnConfirm.isEnabled = sum > 0
    }


}