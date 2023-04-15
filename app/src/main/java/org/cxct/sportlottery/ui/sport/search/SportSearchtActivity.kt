package org.cxct.sportlottery.ui.sport.search

import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.activity_sport_search.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.EmptyView

class SportSearchtActivity : BaseSocketActivity<SportViewModel>(SportViewModel::class) {

    private var searchHistoryList = mutableListOf<String>()
    private val searchResultAdapter by lazy { SportSearchResultAdapter() }
    private var searchKey = ""
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

    private val searchHistoryAdapter by lazy {
        object : BaseQuickAdapter<String, BaseViewHolder>(0) {
            val lPrams = LinearLayout.LayoutParams(-1, -2)

            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val textView = AppCompatTextView(this@SportSearchtActivity)
                val pV = 10.dp
                val pH = 14.dp
                textView.setPadding(pH, pV, pH, pV)
                textView.textSize = 12f
                textView.setTextColor(getColor(R.color.color_A7B2C4))
                textView.layoutParams = lPrams
                return BaseViewHolder(textView)
            }

            override fun convert(holder: BaseViewHolder, item: String)  {
                (holder.itemView as TextView).text = item
                holder.itemView.setOnClickListener {
                    etSearch.setText(item)
                    val editable = etSearch.text
                    Selection.setSelection(editable, editable.length)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_FFFFFF, true)
        setContentView(R.layout.activity_sport_search)
        setupToolbar()
        initRecyclerView()
        initSearchView()
        initObservable()
        viewModel.getSearchResult()
    }

    private fun setupToolbar() {
        custom_tool_bar.setOnBackPressListener { onBackPressed() }
        tv_toolbar_title.text = getString(R.string.text_search)
    }

    private fun setHistoryLayoutVisible(visible: Boolean) {
        rvHistory.isVisible = visible
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

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(p0: Editable?) {
                searchKey = etSearch.text.toString()
                if (searchKey.isNotEmpty()) {
                    startSearch()
                } else {
                    setHistoryLayoutVisible(true)
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

        btn_close.setOnClickListener { etSearch.text = null }
        etSearch.post { etSearch.requestFocus() }
    }

    private inline fun newLinearLayoutManager() = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

    private fun initSearch() {
        setHistoryLayoutVisible(true)
        MultiLanguagesApplication.searchHistory?.let { searchHistoryList = it }
        rvHistory.layoutManager = newLinearLayoutManager()
        rvHistory.isNestedScrollingEnabled = false
        searchHistoryAdapter.setNewInstance(searchHistoryList)
        rvHistory.adapter = searchHistoryAdapter
    }

    private fun startSearch() {
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
        if (searchKey.isEmptyStr()) {
            searchResultAdapter.setNewInstance(null)
        } else {
            viewModel.getSportSearch(searchKey)
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