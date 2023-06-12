package org.cxct.sportlottery.ui.sport.search

import android.os.Bundle
import android.text.Selection
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout.OnTagClickListener
import kotlinx.android.synthetic.main.activity_sport_search.btnToolbarBack
import kotlinx.android.synthetic.main.activity_sport_search.etSearch
import kotlinx.android.synthetic.main.activity_sport_search.historyTitle
import kotlinx.android.synthetic.main.activity_sport_search.ivClear
import kotlinx.android.synthetic.main.activity_sport_search.rvSearchResult
import kotlinx.android.synthetic.main.activity_sport_search.sportSearchHistoryTag
import kotlinx.android.synthetic.main.activity_sport_search.tvSearch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.chat.hideSoftInput
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.EmptyView

class SportSearchtActivity : BaseSocketActivity<SportViewModel>(SportViewModel::class) {

    private var searchHistoryList = mutableListOf<String>()
    private val searchResultAdapter by lazy { SportSearchResultAdapter() }
    private var searchKey = ""
    private var searchFlag = true
    private val tvNoMore by lazy {
        AppCompatTextView(this).apply {
            val padding = 10.dp
            setPadding(0, padding, 0, padding)
            gravity = Gravity.CENTER_HORIZONTAL
            setTextColor(getColor(R.color.color_6C7BA8_A7B2C4))
            setBackgroundColor(getColor(R.color.color_F2F5FA))
            setText(R.string.no_data)
            textSize = 12f
        }
    }

    private val searchHisTagAdapter by lazy {
        object : TagAdapter<String>(searchHistoryList) {
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val pv = layoutInflater.inflate(R.layout.item_sport_search_history, null)
                var tvName = pv.findViewById<TextView>(R.id.sportSearchHistoryName)
                var ivDel = pv.findViewById<ImageView>(R.id.sportSearchHistoryDelete)
                ivDel.setOnClickListener {
                    searchHistoryList.remove(t)
                    MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
                    notifyDataChanged()
                }
                tvName.text = t
                return pv
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_FFFFFF, true)
        setContentView(R.layout.activity_sport_search)
        btnToolbarBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
        initSearchView()
        initObservable()
        viewModel.getSearchResult()
    }

    private fun setHistoryLayoutVisible(visible: Boolean) {
        sportSearchHistoryTag.isVisible = visible
        historyTitle.isVisible = visible
        val searchResultVisible = !visible
        rvSearchResult.isVisible = searchResultVisible
        tvNoMore.isVisible = (searchResultVisible && searchResultAdapter.getDataCount() > 0)
    }

    private fun initSearchView() {
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setHistoryLayoutVisible(true)
                initSearch()
            } else {
                setHistoryLayoutVisible(false)
            }
        }
        ivClear.setOnClickListener {
            if (searchHistoryList.size != 0) {
                searchHistoryList.clear()
            }
            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchHisTagAdapter.notifyDataChanged()
        }
        etSearch.post { etSearch.requestFocus() }
        tvSearch.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                if (searchFlag) {
                    //点了搜索 显示取消
                    startSearch()
                } else {
                    //点了取消 显示搜索
                    searchFlag = true
                    tvSearch.text = getString(R.string.C001)
                    setHistoryLayoutVisible(true)
                    searchHisTagAdapter.notifyDataChanged()
                }
            }
        })
    }

    private fun initSearch() {
        setHistoryLayoutVisible(true)
        MultiLanguagesApplication.searchHistory?.let { searchHistoryList = it }
        sportSearchHistoryTag.adapter = searchHisTagAdapter
        sportSearchHistoryTag.setOnTagClickListener(object : OnTagClickListener {
            override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
                var item = searchHistoryList[position]
                etSearch.setText(item)
                val editable = etSearch.text
                Selection.setSelection(editable, editable.length)
                startSearch()
                return true
            }
        })
        searchFlag = true
        tvSearch.text = getString(R.string.C001)
    }

    private fun startSearch() {
        searchKey = etSearch.text.toString()
        if (searchKey.isNotEmpty()) {
            if (searchHistoryList.any { it == searchKey }) {
                searchHistoryList.remove(searchKey)
                searchHistoryList.add(0, searchKey)
            } else if (searchHistoryList.size == 10) {
                searchHistoryList.removeAt(9)
                searchHistoryList.add(0, searchKey)
            } else {
                searchHistoryList.add(0, searchKey)
            }

            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchFlag = false
            tvSearch.text = getString(R.string.D037)
            viewModel.getSportSearch(searchKey)
            hideSoftInput()
            etSearch.clearFocus()
        } else {
            searchFlag = true
            setHistoryLayoutVisible(true)
            searchHisTagAdapter.notifyDataChanged()
            searchResultAdapter.setNewInstance(null)
        }
    }

    private fun initRecyclerView() {
        rvSearchResult.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val emptyView = EmptyView(this)
        emptyView.setEmptyImg(R.drawable.bg_search_nodata)
        emptyView.setEmptyText(getString(R.string.no_search_match))
        searchResultAdapter.setEmptyView(emptyView)
        searchResultAdapter.addFooterView(tvNoMore)
        rvSearchResult.adapter = searchResultAdapter
    }

    private fun initObservable() {

        viewModel.searchResult.observe(this) {
            val event = it.getContentIfNotHandled() ?: return@observe
            if (event.first != searchKey) {
                return@observe
            }

            val list = event.second
            searchResultAdapter.setNewData(searchKey, list?.toMutableList())
            setHistoryLayoutVisible(false)
        }
    }

}