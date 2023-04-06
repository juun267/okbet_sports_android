package org.cxct.sportlottery.ui.sport.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.activity_sport_search.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.SearchResponse
import org.cxct.sportlottery.network.sport.SearchResult
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.view.VerticalDecoration
import org.cxct.sportlottery.view.highLightTextView.HighlightTextView
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper


class SportSearchtActivity :
    BaseSocketActivity<SportViewModel>(SportViewModel::class) {


    private var searchHistoryList = mutableListOf<String>()
    private lateinit var searchResultAdapter: CommonAdapter<SearchResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_FFFFFF, true)
        setContentView(R.layout.activity_sport_search)
        setupToolbar()
        initSearchView()
        initRecyclerView()
        initObservable()
        viewModel.getSearchResult()
    }

    private fun setupToolbar() {
        custom_tool_bar.setOnBackPressListener { onBackPressed() }
        tv_toolbar_title.text = getString(R.string.text_search)

    }

    private fun initSearchView() {
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                layoutSearchHistory.visibility = View.VISIBLE
                initSearch()
            } else {
                layoutSearchHistory.visibility = View.GONE
            }
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (etSearch.text.isNotEmpty()) {
                    startSearch()
                } else {
                    layoutSearchHistory.visibility = View.VISIBLE
                    layoutSearchResult.visibility = View.GONE
                    layoutNoData.visibility = View.GONE
                    searchHistoryAdapter?.notifyDataSetChanged()
                }
            }
        })
        tvClear.setOnClickListener {
            if (searchHistoryList.size != 0) {
                searchHistoryList.clear()
            }
            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchHistoryAdapter?.notifyDataSetChanged()
        }
        btn_close.setOnClickListener {
            etSearch.text = null
        }
    }

    var searchResult: MutableList<SearchResult> = ArrayList()
    private var searchHistoryAdapter: CommonAdapter<String>? = null

    private fun initSearch() {
        layoutSearchHistory.visibility = View.VISIBLE
        layoutSearchResult.visibility = View.GONE
        layoutNoData.visibility = View.GONE
        MultiLanguagesApplication.searchHistory?.let {
            searchHistoryList = it
        }
        searchHistoryList.let {
            if (it.size > 0) {
                rvHistory.layoutManager =
                    LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                rvHistory.isNestedScrollingEnabled = false
                layoutHistory.visibility = View.VISIBLE
                searchHistoryAdapter =
                    object : CommonAdapter<String>(this, R.layout.item_search_history, it) {
                        override fun convert(holder: ViewHolder, t: String, position: Int) {
                            //holder.setText(R.id.tvHistory, t)
                            val tvHistory = holder.getView<TextView>(R.id.tvHistory)
                            tvHistory.text = t
                            tvHistory.setOnClickListener {
                                etSearch.setText(t)
                            }
                        }
                    }
                rvHistory.adapter = searchHistoryAdapter
                OverScrollDecoratorHelper.setUpOverScroll(rvHistory,
                    OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
            }
        }
    }

    private fun startSearch() {
        if (searchHistoryList.any {
                it == etSearch.text.toString()
            }) {
            searchHistoryList.remove(etSearch.text.toString())
            searchHistoryList.add(0, etSearch.text.toString())
        } else if (searchHistoryList.size == 10) {
            searchHistoryList.removeAt(9)
            searchHistoryList.add(0, etSearch.text.toString())
        } else {
            searchHistoryList.add(0, etSearch.text.toString())
        }
        MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
        viewModel.getSportSearch(etSearch.text.toString())
        //rvHostory.adapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        rvSearchResult.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvSearchResult.isNestedScrollingEnabled = false
        searchResultAdapter = object :
            CommonAdapter<SearchResult>(this, R.layout.item_search_result_sport, searchResult) {
            override fun convert(holder: ViewHolder, t: SearchResult, position: Int) {
                holder.setText(R.id.tvResultTittle, t.sportTitle)
                val rvResultLeague = holder.getView<RecyclerView>(R.id.rvResultLeague)
                rvResultLeague.layoutManager =
                    LinearLayoutManager(this@SportSearchtActivity, RecyclerView.VERTICAL, false)
                rvResultLeague.isNestedScrollingEnabled = false
                val adapter =
                    object :
                        CommonAdapter<SearchResult.SearchResultLeague>(this@SportSearchtActivity,
                            R.layout.item_search_result_league,
                            t.searchResultLeague) {
                        override fun convert(
                            holder: ViewHolder,
                            it: SearchResult.SearchResultLeague,
                            position: Int,
                        ) {
                            val tvLeagueTittle =
                                holder.getView<HighlightTextView>(R.id.tvLeagueTittle)
                            tvLeagueTittle.setCustomText(it.league)
                            tvLeagueTittle.highlight(etSearch.text.toString())
                            val rvResultMatch = holder.getView<RecyclerView>(R.id.rvResultMatch)
                            rvResultMatch.layoutManager =
                                LinearLayoutManager(this@SportSearchtActivity,
                                    RecyclerView.VERTICAL,
                                    false)
                            rvResultMatch.addItemDecoration(VerticalDecoration(this@SportSearchtActivity,
                                R.drawable.divider_gray))
                            rvResultMatch.isNestedScrollingEnabled = false
                            val adapter = object :
                                CommonAdapter<SearchResponse.Row.LeagueMatch.MatchInfo>(
                                    this@SportSearchtActivity,
                                    R.layout.item_search_result_match,
                                    t.searchResultLeague[position].leagueMatchList
                                ) {
                                override fun convert(
                                    holder: ViewHolder,
                                    itt: SearchResponse.Row.LeagueMatch.MatchInfo,
                                    position: Int,
                                ) {
                                    val tvTime = holder.getView<TextView>(R.id.tv_time)
                                    tvTime.text = TimeUtil.timeFormat(itt.startTime.toLong(),
                                        TimeUtil.YMD_FORMAT) + "\n" + TimeUtil.timeFormat(itt.startTime.toLong(),
                                        TimeUtil.HM_FORMAT_SS)

                                    val tvHomeName =
                                        holder.getView<HighlightTextView>(R.id.tv_home_name)
                                    tvHomeName.setCustomText(itt.homeName)
                                    tvHomeName.highlight(etSearch.text.toString())

                                    val tvAwayName =
                                        holder.getView<HighlightTextView>(R.id.tv_away_name)
                                    tvAwayName.setCustomText(itt.awayName)
                                    tvAwayName.highlight(etSearch.text.toString())
                                    holder.itemView.setOnClickListener {
                                        var matchInfo = MatchInfo(
                                            id = itt.matchId,
                                            gameType = t.gameType,
                                            homeName = itt.homeName,
                                            awayName = itt.awayName,
                                            startTime = itt.startTime.toLong(),
                                            endTime = null,
                                            parlay = null,
                                            playCateNum = null,
                                            source = null,
                                            status = null,
                                        )
                                        SportDetailActivity.startActivity(this@SportSearchtActivity,
                                            matchInfo,
                                            null)
                                    }

                                }
                            }
                            rvResultMatch.adapter = adapter
                        }
                    }
                rvResultLeague.adapter = adapter
            }
        }
        rvSearchResult.adapter = searchResultAdapter
        OverScrollDecoratorHelper.setUpOverScroll(rvSearchResult,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
    }

    private fun initObservable() {
        viewModel.searchResult.observe(this) {
            it.getContentIfNotHandled()?.let { list ->
                layoutSearchHistory.visibility = View.GONE
                if (list.isNotEmpty()) {
                    layoutSearchResult.visibility = View.VISIBLE
                    layoutNoData.visibility = View.GONE
                    searchResult.clear()
                    searchResult.addAll(list)
                    searchResultAdapter.notifyDataSetChanged()
                } else {
                    layoutSearchResult.visibility = View.GONE
                    layoutNoData.visibility = View.VISIBLE
                }
            }

        }
    }
}