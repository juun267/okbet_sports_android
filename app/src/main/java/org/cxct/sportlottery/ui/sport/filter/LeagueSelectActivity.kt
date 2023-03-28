package org.cxct.sportlottery.ui.sport.filter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_league_select.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.view.IndexBar
import org.cxct.sportlottery.view.VerticalDecoration
import org.greenrobot.eventbus.EventBus


class LeagueSelectActivity :
    BaseSocketActivity<LeagueSelectViewModel>(LeagueSelectViewModel::class) {
    companion object {
        fun start(
            context: Context,
            gameType: String,
            matchType: MatchType,
            startTime: String?,
            endTime: String?,
            leagueIdList: List<String>,
        ) {
            var intent = Intent(context, LeagueSelectActivity::class.java)
            intent.putExtra("gameType", gameType)
            intent.putExtra("matchType", matchType)
            intent.putExtra("startTime", startTime)
            intent.putExtra("endTime", endTime)
            intent.putExtra("leagueIdList", leagueIdList as ArrayList)
            context.startActivity(intent)
        }
    }

    var leagueList = mutableListOf<League>()
    var itemData = mutableListOf<LeagueSection>()

    private val gameType by lazy { intent?.getStringExtra("gameType") ?: GameType.FT.key }
    private val matchType by lazy {
        (intent?.getSerializableExtra("matchType") as MatchType?) ?: MatchType.IN_PLAY
    }

    private val startTime: String? by lazy { intent?.getStringExtra("startTime") }
    private val endTime: String? by lazy { intent?.getStringExtra("endTime") }
    private val leagueIdList: List<String>? by lazy { intent?.getSerializableExtra("leagueIdList") as List<String> }

    lateinit var leagueSelectAdapter: LeagueSelectAdapter
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_FFFFFF, true)
        setContentView(R.layout.activity_league_select)
        setupToolbar()
        setupMatchListView()
        initObserve()
        viewModel.getLeagueList(gameType,
            matchType.postValue,
            startTime ?: "",
            endTime ?: "",
            null,
            leagueIdList)
    }

    private fun setupToolbar() {
        custom_tool_bar.backPressListener = {
            onBackPressed()
        }
        tv_all_select.setOnClickListener {
            leagueList.forEach {
                it.isSelected = true
            }
            leagueSelectAdapter.notifyDataSetChanged()
            setSelectSum()
        }
        tv_reverse_select.setOnClickListener {
            leagueList.forEach {
                it.isSelected = !it.isSelected
            }
            leagueSelectAdapter.notifyDataSetChanged()
            setSelectSum()
        }
        btn_sure.setOnClickListener {
            var leagueSelectList = mutableListOf<League>()
            leagueList.forEach {
                if (it.isSelected) {
                    leagueSelectList.add(it)
                }
            }
            EventBus.getDefault().post(leagueSelectList.toList())
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
                val pos = itemData.indexOfFirst {
                    it.isHeader && TextUtils.equals(it.header, indexChar)
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
        rv_league.addItemDecoration(VerticalDecoration(this, R.drawable.divider_vertical_6))
        leagueSelectAdapter = LeagueSelectAdapter(mutableListOf())
        leagueSelectAdapter.setOnItemClickListener { adapter, view, position ->

            var item = itemData[position]
            if (item.isHeader || item.t == null) { // 字母标签不让点
                return@setOnItemClickListener
            }

            item.t.apply {
                isSelected = !isSelected
            }
            leagueSelectAdapter.notifyItemChanged(position)
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
                    var cap =
                        if (leagueSection?.isHeader == true) leagueSection.header else leagueSection?.t?.firstCap
                    indexBar.updateIndex(indexBar.getTextArray().indexOf(cap))
                }
            }
        })
    }

    private fun initObserve() {
        viewModel.leagueList.observe(this) {
            it.let {
                leagueList = it
                var map: Map<String, List<League>> = it.groupBy {
                    it.firstCap
                }.toSortedMap(Comparator<String> { o1: String, o2: String ->
                    o1.compareTo(o2)
                })
                map.keys.forEach { name ->
                    itemData.add(LeagueSection(
                        true,
                        name))
                    map.get(name)?.forEach {
                        itemData.add(LeagueSection(
                            it))
                    }
                }
                leagueSelectAdapter.setNewData(itemData)
                setIndexbar(map.keys.toTypedArray())
                setSelectSum()
            }
        }
    }

    private fun setSelectSum() {
        var sum = leagueList.count {
            it.isSelected
        }
        btn_sure.isEnabled = sum > 0
        tv_sum.text = getString(R.string.league) + ":" + sum
    }

}