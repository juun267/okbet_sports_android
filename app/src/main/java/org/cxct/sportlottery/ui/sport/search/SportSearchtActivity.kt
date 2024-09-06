package org.cxct.sportlottery.ui.sport.search

import android.text.Selection
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout.OnTagClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ActivitySportSearchBinding
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.chat.hideSoftInput
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.bindSportMaintenance
import org.cxct.sportlottery.view.EmptyView

class SportSearchtActivity : BaseSocketActivity<SportViewModel,ActivitySportSearchBinding>(SportViewModel::class) {
    override fun pageName() = "赛事搜索页面"
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
            setText(R.string.N111)
            textSize = 12f
        }
    }
    private val gameType by lazy { intent.getStringExtra("gameType") }

    private fun getSearchTagAdapter(): TagAdapter<String> {
        return object : TagAdapter<String>(searchHistoryList) {
            override fun getView(parent: FlowLayout?, position: Int, t: String?): View {
                val pv = layoutInflater.inflate(R.layout.item_sport_search_history, null)
                var tvName = pv.findViewById<TextView>(R.id.sportSearchHistoryName)
                var ivDel = pv.findViewById<ImageView>(R.id.sportSearchHistoryDelete)
                ivDel.setOnClickListener {
                    searchHistoryList.remove(t)
                    binding.ivClear.isGone = searchHistoryList.isEmpty()
                    MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
                    binding.sportSearchHistoryTag.adapter = getSearchTagAdapter()
                }
                tvName.text = t
                return pv
            }
        }

    }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.btnToolbarBack.setOnClickListener { onBackPressed() }
        initRecyclerView()
        initSearchView()
        initObservable()
        viewModel.getSearchResult()
        bindSportMaintenance()
    }

    private fun setHistoryLayoutVisible(visible: Boolean) {
        binding.sportSearchHistoryTag.isVisible = visible
        binding.historyTitle.isVisible = visible
        val searchResultVisible = !visible
        binding.rvSearchResult.isVisible = searchResultVisible
        tvNoMore.isVisible = (searchResultVisible && searchResultAdapter.getDataCount() > 0)
    }

    private fun initSearchView()=binding.run {
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
            ivClear.isGone = searchHistoryList.isEmpty()
            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            sportSearchHistoryTag.adapter = getSearchTagAdapter()
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
                    sportSearchHistoryTag.adapter = getSearchTagAdapter()

                }
            }
        })
    }

    private fun initSearch()=binding.run {
        setHistoryLayoutVisible(true)
        MultiLanguagesApplication.searchHistory?.let { searchHistoryList = it }
        ivClear.isGone = searchHistoryList.isEmpty()
        sportSearchHistoryTag.adapter = getSearchTagAdapter()
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

    private fun startSearch()=binding.run {
        searchKey = etSearch.text.toString()
        if (searchKey.isNotEmpty()) {
            if (searchHistoryList.any { it == searchKey }) {
                searchHistoryList.remove(searchKey)
                searchHistoryList.add(0, searchKey)
            } else if (searchHistoryList.size == 15) {
                searchHistoryList.removeAt(9)
                searchHistoryList.add(0, searchKey)
            } else {
                searchHistoryList.add(0, searchKey)
            }

            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchFlag = false
            tvSearch.text = getString(R.string.D037)
            viewModel.getSportSearch(searchKey,gameType)
            hideSoftInput()
            etSearch.clearFocus()
        } else {
            searchFlag = true
            setHistoryLayoutVisible(true)
            sportSearchHistoryTag.adapter = getSearchTagAdapter()
            searchResultAdapter.setNewInstance(null)
        }
        ivClear.isGone = searchHistoryList.isEmpty()
    }

    private fun initRecyclerView()=binding.run {
        rvSearchResult.layoutManager = LinearLayoutManager(this@SportSearchtActivity, RecyclerView.VERTICAL, false)
        val emptyView = EmptyView(this@SportSearchtActivity)
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